package com.example.androidprojekt;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 
 * @author sergej, boghyon
 * 
 */
public class AufgabeErstellen extends FragmentActivity implements DateTimePickerFragment.CalendarMessenger, View.OnClickListener,
		View.OnTouchListener {

	// private static final String TAG = AufgabeErstellen.class.getSimpleName();

	EditText txtSchlagW, txtBeschreib, txtPhone;

	Spinner spinner;
	Button date, bPickPhoneNumber;

	boolean alarmSet; // hilfsvariable

	int iconId;

	// DB
	OurOpenHandler our_db = new OurOpenHandler(this);

	// Immer getInstance() benutzen anstatt "new GregorianCalendar()" fuer die passende Zeitzone und passender Kalender.
	Calendar dateOfNote = Calendar.getInstance();

	View contentView; // Um Tastatur wieder zu verstecken, wenn der User auf ein leeres Feld klickt.
	View speichernLayoutView; // Fuer den manuellen Button-Ersatz RelativeLayout "Speichern".

	boolean moved = false;

	// ***********************************************************************************************
	// Override Methoden
	// ***********************************************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aufgabe_erstellen);

		speichernLayoutView = findViewById(R.id.speichernLayout);
		speichernLayoutView.setFocusable(true);
		contentView = getWindow().getDecorView().findViewById(android.R.id.content);

		txtAnlegen();
		spinnerAnlegen();

		// SET DATE & TIME geklickt
		date = (Button) findViewById(R.id.reminder_button);
		date.setOnClickListener(this);

		bPickPhoneNumber = (Button) findViewById(R.id.btnPickFromContacts);
		bPickPhoneNumber.setOnClickListener(this);

		contentView.setOnTouchListener(this);
		speichernLayoutView.setOnTouchListener(this);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case 1001:
			if (resultCode == Activity.RESULT_OK) {

				Cursor s = getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);

				if (s.moveToFirst()) {
					String phoneNum = "Tel.: " + s.getString(s.getColumnIndex(Phone.NUMBER));
					txtPhone.setText(phoneNum);
				}
			}
			break;
		}
	}

	// OPTIONS MENU
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		menu.removeItem(R.id.action_remove_all_tasks);
		return true;
	}

	@Override
	/**
	 * SETTINGS:
	 * LOGOUT: der User wird ausgeloggt und zur UnlockActivity navigiert.
	 * Die vorherigen Activities werden vom Task geloescht,
	 * sodass der User nicht mehr zurueckkehren kann. Er muss sich wieder anmelden.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_lock) // LOGOUT
			UnlockActivity.navigateToUnlock(this);

		return super.onOptionsItemSelected(item);
	}

	@Override
	// INTERFACE-METHODE
	public void getCalendarFromPicker(Calendar calendar, boolean alarmSet) {
		// ich kann jetzt diesen calendar benutzen.

		this.alarmSet = alarmSet;
		dateOfNote = calendar;

		// Buttontext aendern
		date.setText(dateOfNote.get(Calendar.DAY_OF_MONTH) + "." + (dateOfNote.get(Calendar.MONTH) + 1) + "."
				+ dateOfNote.get(Calendar.YEAR) + ", um " + dateOfNote.get(Calendar.HOUR_OF_DAY) + ":" + dateOfNote.get(Calendar.MINUTE));
		if (alarmSet)
			date.setText("Alarm set: " + date.getText().toString());
	}

	// SPEICHERN BUTTON geklickt
	public void aufgabeErstellen(View view) {

		String schlagworte = txtSchlagW.getText().toString();
		String beschreibung = txtBeschreib.getText().toString();
		String phoneNumber = txtPhone.getText().toString();

		// erst prüfen ob werte in den textView enthalten sind
		if (schlagworte.isEmpty()) {
			Toast.makeText(this, R.string.no_tags_no_task, Toast.LENGTH_SHORT).show();
			txtSchlagW.setBackgroundColor(Color.RED);
			txtSchlagW.setHintTextColor(Color.LTGRAY);
		}

		// if (beschreibung.isEmpty())
		// txtBeschreib.setBackgroundColor(Color.RED);

		if (!schlagworte.isEmpty()) {
			// && !beschreibung.isEmpty()) {

			int imgNr = 0;

			if (iconId == R.drawable.normal)
				imgNr = 1;
			else if (iconId == R.drawable.wichtig)
				imgNr = 2;
			else if (iconId == R.drawable.sehr_wichtig)
				imgNr = 3;

			// NEUE AUFGABE
			Aufgabe aufgabe = new Aufgabe(schlagworte, beschreibung, phoneNumber, imgNr, dateOfNote, alarmSet);
			our_db.insert(aufgabe.getId(), schlagworte, beschreibung, phoneNumber, imgNr, dateOfNote, alarmSet);

			if (alarmSet)
				OurAlarmManager.setAlarm(this, aufgabe, 0);
			// KOMISCHER BUG: Wenn das Intervall auf >9 festgesetzt ist, werden alle Alarme gleichzeitig ausgeloest.
			else
				OurAlarmManager.setAlarmEmpty();

			finish();
		}

	}

	private void txtAnlegen() {

		txtSchlagW = (EditText) findViewById(R.id.txtSchlagworte);
		// txtSchlagW.setOnClickListener(new MyOnClickListener());
		// txtSchlagW.setOnFocusChangeListener(new MyTxtOnFocusChangeListener());

		txtBeschreib = (EditText) findViewById(R.id.txtBeschreibung);
		// txtBeschreib.setOnClickListener(new MyOnClickListener());
		// txtBeschreib.setOnFocusChangeListener(new MyTxtOnFocusChangeListener());

		txtPhone = (EditText) findViewById(R.id.txtPhoneNumber);
	}

	private void spinnerAnlegen() {
		// hier wird der Spinner (werte: normal, wichtig und sehr wichtig aus der xml datei prio_werte_fuer_spinner.xml gezogen und
		// eingefügt
		spinner = (Spinner) findViewById(R.id.spinnerPrioritaet);
		spinner.setOnTouchListener(this);
		// ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_auswahl,
		// android.R.layout.simple_spinner_item);

		// spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			// hier wird geprüft welche position in dem Spinner ausgewählt wurde
			// dann wird in dem int die R.drawable.,,, gespeichert
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					iconId = R.drawable.normal;
					break;
				case 1:
					iconId = R.drawable.wichtig;
					break;
				case 2:
					iconId = R.drawable.sehr_wichtig;
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// wird nie aufgerufen, da wir immer eine auswahl
				// von 3 werten haben.
			}
		});

		spinner.setAdapter(new CustomSpinnerAdapter(this));

	}

	public void hideKeyborad(View view) {
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) AufgabeErstellen.this.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	// Ende - Eigene Methoden

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reminder_button:
			// DateTimePicker zum Auswaehlen des Datums und der Zeit wird angezeigt
			DialogFragment newFragment = new DateTimePickerFragment(dateOfNote, alarmSet, 0);
			newFragment.show(getSupportFragmentManager(), "dateTimePicker"); // 2nd Parameter wird spaeter gebraucht mit
																				// findFragmentByTag
			break;
		case R.id.btnPickFromContacts:
			// Starten ein Intent, und warten bis der Benutzer ein Kontakt auswählt
			Intent i = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
			startActivityForResult(i, 1001);
			break;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (v.getId()) {
		case android.R.id.content:
			hideKeyborad(findViewById(android.R.id.content));
			return false; // true zurueckgeben, wenn es auf weitere events reagieren soll. (ACTION_UP, ACTION_MOVE, ...)
			// Wenn nicht false, wird es mehrmals aufgerufen.
		case R.id.spinnerPrioritaet:
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) { // Ohne diese Zeile, wird das 2 mal ausgeführt, wegen DOWN und UP.
				// findViewById(R.id.spinnerCustomLayout).setBackgroundColor(0xFFeae994); // 0xFF == alpha
				// Im CustomSpinnerAdapter wird die Hintergrundfarbe wiederhergestellt.
				Toast.makeText(this, R.string.priority, Toast.LENGTH_SHORT).show();
				hideKeyborad(spinner); // Virtuelle Tastatur verstecken, wenn wo anders geklickt wurde
			}
			return false; // false zurueck geben, damit es nicht auf weitere Events reagiert und onTouch aufruft.
		case R.id.speichernLayout:
			int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN) { // Wenn Mouse pressed
				speichernLayoutView.setAlpha(0.92f); // RelativeLayout durchsichtig machen. 0.92 == Android default Button clicked alpha
				return true; // true --> it will go to the next layer down.
			}
			if (action == MotionEvent.ACTION_MOVE) {
				moved = true; // wenn der User die Maus gedrueckt von der Flaeche weg zieht
				return true;
			}
			if (action == MotionEvent.ACTION_UP) { // Mouse released
				v.playSoundEffect(android.view.SoundEffectConstants.CLICK); // Android's default Klickgeraeusch
				speichernLayoutView.setAlpha(1); // Alpha wieder zuruecksetzen (1 == komplett undurchsichtig)
				if (!moved)
					aufgabeErstellen(v);
				moved = false;
				return false;
			}
			return true;
		default:
			return false;
		}
	}

	// // ********************************************************************************
	// // Interne Klassen
	// // ********************************************************************************
	// class MyOnClickListener implements OnClickListener {
	// @Override
	// public void onClick(View v) {
	// if (v.getId() == R.id.txtSchlagworte)
	// txtSchlagW.setBackgroundColor(Color.WHITE);
	// if (v.getId() == R.id.txtBeschreibung)
	// txtBeschreib.setBackgroundColor(Color.WHITE);
	// }
	// }
	//
	// class MyTxtOnFocusChangeListener implements OnFocusChangeListener {
	// @Override
	// public void onFocusChange(View v, boolean hasFocus) {
	// if (hasFocus == true) {
	// if (v.getId() == R.id.txtSchlagworte)
	// txtSchlagW.setBackgroundColor(Color.WHITE);
	// if (v.getId() == R.id.txtBeschreibung)
	// txtBeschreib.setBackgroundColor(Color.WHITE);
	// }
	// }
	// }

}

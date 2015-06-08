package com.example.androidprojekt;

import java.util.Calendar;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author sergej, boghyon
 *         Eine entsprechende Detailansicht wird angezeigt, wenn der User auf
 *         eine Aufgabe in der ListView oder Notification geklickt hat.
 * 
 */
public class Detail extends FragmentActivity implements DateTimePickerFragment.CalendarMessenger, View.OnClickListener,
		View.OnLongClickListener, View.OnTouchListener {

	// private static final String TAG = Detail.class.getSimpleName();

	EditText txtTitel, txtBeschreibung, txtPhone;
	Button editDate, bPickPhoneNumber;
	ImageButton mEditTaskButton, mSendToHistoryButton;
	Spinner spinner;

	// Hilfsvariablen
	Calendar calendarEdited, copyOfAktuelleAufgabe;
	boolean alarmSet;
	int iconIdEdited, position, setSpinnerPosition;

	Aufgabe aktuelleAufgabe;

	// DB
	OurOpenHandler our_db = new OurOpenHandler(this);

	View contentView; // Um Tastatur wieder zu verstecken, wenn der User auf ein leeres Feld klickt.

	// ***********************************************************************************************
	// Override Methoden
	// ***********************************************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailansicht); // um auf Elemente im Layout zuzugreifen

		// Uebergebenes Aufgabe-Objekt aus dem Intent herausholen
		aktuelleAufgabe = (Aufgabe) getIntent().getParcelableExtra("Aktuelle_Aufgabe");
		copyOfAktuelleAufgabe = aktuelleAufgabe.getDatum();

		alarmSet = OurAlarmManager.alarmIsSet(aktuelleAufgabe.getId());
		iconIdEdited = aktuelleAufgabe.getPrioritaetIconId();

		// NOTIFICATION ENTFERNEN, wenn man auf sie geklickt hat
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationMgr.cancel(aktuelleAufgabe.getId());

		// BUTTONS u Sichtbarkeit
		editDate = (Button) findViewById(R.id.reminder_button);
		editDate.setText(aktuelleAufgabe.getDatum().get(Calendar.DAY_OF_MONTH) + "." + (aktuelleAufgabe.getDatum().get(Calendar.MONTH) + 1)
				+ "." + aktuelleAufgabe.getDatum().get(Calendar.YEAR) + ", um " + aktuelleAufgabe.getDatum().get(Calendar.HOUR_OF_DAY)
				+ ":" + aktuelleAufgabe.getDatum().get(Calendar.MINUTE));
		if (alarmSet)
			editDate.setText("Alarm set: " + editDate.getText().toString());

		if (iconIdEdited == 1)
			setSpinnerPosition = 0;
		else if (iconIdEdited == 2)
			setSpinnerPosition = 1;
		else if (iconIdEdited == 3)
			setSpinnerPosition = 2;

		spinnerAnlegen();
		txtAnlegen();

		// DATE TIME PICKER
		editDate.setEnabled(false);

		// calendarEdited nur bereit halten, (ohne aktuelleAufgabe.getDatum() zu aendern) falls der User ohne zu speichern zurueck klickt.
		// Erst wenn der User, nachdem er das Datum ausgewaehlt hat, auf SPEICHERN klickt, wird diese Variable uebernommen.
		calendarEdited = aktuelleAufgabe.getDatum();
		editDate.setOnClickListener(this);

		bPickPhoneNumber = (Button) findViewById(R.id.btnPickFromContacts);
		bPickPhoneNumber.setOnClickListener(this);
		bPickPhoneNumber.setVisibility(View.GONE);

		mEditTaskButton = (ImageButton) findViewById(R.id.editTaskButton); // EDIT & SAVE Button
		mEditTaskButton.setOnClickListener(this);
		mEditTaskButton.setOnLongClickListener(this);
		mSendToHistoryButton = (ImageButton) findViewById(R.id.sendToHistoryButton); // DONE Button
		mSendToHistoryButton.setOnClickListener(this);
		mSendToHistoryButton.setOnLongClickListener(this);

		contentView = getWindow().getDecorView().findViewById(android.R.id.content);
		contentView.setOnTouchListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setEditable();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reminder_button:

			// Dialog zum Auswaehlen des Datums wird angezeigt
			DialogFragment newFragment = new DateTimePickerFragment(calendarEdited, alarmSet, aktuelleAufgabe.getId()); // innere Klasse
			newFragment.show(getSupportFragmentManager(), "dateTimePicker");
			break;
		case R.id.btnPickFromContacts:
			// if (!txtPhone.getText().toString().trim().isEmpty()) {
			//
			// Intent call = new Intent(Intent.ACTION_DIAL);
			// call.setData(Uri.parse("tel:" + txtPhone.getText().toString()));
			// startActivity(call);
			// }

			// Starten ein Intent, und warten bis der Benutzer ein Kontakt auswählt
			Intent i = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
			startActivityForResult(i, 1001);
			break;
		case R.id.editTaskButton:
			if (!mEditTaskButton.isSelected()) {
				setEditable();
			} else {
				// hier wird explizit keine neue Aufgabe instanziert, damit die ID nicht inkrementiert.

				if (txtTitel.getText().toString().isEmpty()) {
					Toast.makeText(this, R.string.no_tags_no_task, Toast.LENGTH_SHORT).show();
					txtTitel.setBackgroundColor(Color.RED);
					txtTitel.setHintTextColor(Color.LTGRAY);
				}

				if (!txtTitel.getText().toString().isEmpty()) {

					aktuelleAufgabe.setTitel(txtTitel.getText().toString());
					aktuelleAufgabe.setBeschreibung(txtBeschreibung.getText().toString());
					aktuelleAufgabe.setPhone_number(txtPhone.getText().toString());
					aktuelleAufgabe.setPrioritaetIconId(iconIdEdited);
					aktuelleAufgabe.setDatum(calendarEdited);
					aktuelleAufgabe.setAlarmSet(alarmSet);

					our_db.update(aktuelleAufgabe.getId(), aktuelleAufgabe.getTitel(), aktuelleAufgabe.getBeschreibung(),
							aktuelleAufgabe.getPhone_number(), iconIdEdited, calendarEdited, aktuelleAufgabe.getAlarmSet());
					our_db.close();
				}

				// ALARM aktualisieren
				if (alarmSet)
					// Alarm wurde veraendert oder nicht ausgeschaltet.
					OurAlarmManager.editAlarm(this, aktuelleAufgabe, 0); // Alarm aktualisieren
				else
					// Alarm wurde ausgeschaltet.
					OurAlarmManager.cancelAlarm(aktuelleAufgabe.getId());

				finish();
			}

			break;
		case R.id.sendToHistoryButton:
			// AUFGABE ERLEDIGT geklickt
			// Aufgabe wird in die History db verschoben und aus der normalen db geloescht
			// Hier sollten nicht die editierten Daten uebernommen werden, sondern die urspruenglichen Daten von der Aufgabe.
			// zur History DB zufügen
			HistoryOpenHandler helper = new HistoryOpenHandler(this);
			helper.insert(aktuelleAufgabe.getId(), aktuelleAufgabe.getTitel(), aktuelleAufgabe.getBeschreibung(),
					aktuelleAufgabe.getPhone_number(), aktuelleAufgabe.getPrioritaetIconId(), Calendar.getInstance());
			helper.close();

			// von der alten DB löschen
			our_db.delete(aktuelleAufgabe.getId());
			// Alarm ausschalten.
			OurAlarmManager.cancelAlarm(aktuelleAufgabe.getId());

			finish();

			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {

		switch (v.getId()) {
		case R.id.editTaskButton:
			if (!mEditTaskButton.isSelected())
				Toast.makeText(this, R.string.edit_task, Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
			break;
		case R.id.sendToHistoryButton:
			Toast.makeText(this, "Task done", Toast.LENGTH_SHORT).show();
			break;
		}
		return false;
	}

	@Override
	// INTERFACE-METHODE
	public void getCalendarFromPicker(Calendar calendar, boolean alarmSet) {
		// ich kann jetzt diesen calendar benutzen.

		this.alarmSet = alarmSet;

		calendarEdited = calendar;
		editDate.setText(calendarEdited.get(Calendar.DAY_OF_MONTH) + "." + (calendarEdited.get(Calendar.MONTH) + 1) + "."
				+ calendarEdited.get(Calendar.YEAR) + ", um " + calendarEdited.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendarEdited.get(Calendar.MINUTE));
		if (alarmSet)
			editDate.setText("Alarm set: " + editDate.getText().toString());
	}

	// OPTIONS MENU
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail_actions, menu);

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
		switch (item.getItemId()) {

		// case R.id.action_settings:
		//
		// return true;
		case R.id.action_lock: // LOGOUT
			UnlockActivity.navigateToUnlock(this);
			return true;
			// case R.id.action_edit:
			//
			// setTrueOrFalse(txtTitel, true);
			// setTrueOrFalse(txtBeschreibung, true);
			// setTrueOrFalse(txtPhone, true);
			// spinner.setEnabled(true);
			// editDate.setEnabled(true);
			//
			// return true;
			//
			// case R.id.action_cancel:
			//
			// txtTitel.setText(aktuelleAufgabe.getTitel());
			// txtBeschreibung.setText(aktuelleAufgabe.getBeschreibung());
			// txtPhone.setText(aktuelleAufgabe.getPhone_number());
			// spinner.setSelection(aktuelleAufgabe.getPrioritaetIconId() - 1);
			// // editDate.setText(copyOfAktuelleAufgabe.get(Calendar.DAY_OF_MONTH) + "." + (copyOfAktuelleAufgabe.get(Calendar.MONTH) + 1)
			// // + "." + copyOfAktuelleAufgabe.get(Calendar.YEAR) + ", um " + copyOfAktuelleAufgabe.get(Calendar.HOUR_OF_DAY)
			// // + ":" + copyOfAktuelleAufgabe.get(Calendar.MINUTE));
			// System.out.println(copyOfAktuelleAufgabe.toString());
			//
			// setTrueOrFalse(txtTitel, false);
			// setTrueOrFalse(txtBeschreibung, false);
			// setTrueOrFalse(txtPhone, false);
			// spinner.setEnabled(false);
			// editDate.setEnabled(false);
			//
			// return true;
			//
			// case R.id.action_save:
			//
			// // hier wird explizit keine neue Aufgabe instanziert, damit die ID nicht inkrementiert.
			//
			// if (txtTitel.getText().toString().isEmpty()) {
			// Toast.makeText(this, R.string.no_tags_no_task, Toast.LENGTH_SHORT).show();
			// txtTitel.setBackgroundColor(Color.RED);
			// }
			//
			// if (!txtTitel.getText().toString().isEmpty()) {
			//
			// aktuelleAufgabe.setTitel(txtTitel.getText().toString());
			// aktuelleAufgabe.setBeschreibung(txtBeschreibung.getText().toString());
			// aktuelleAufgabe.setPhone_number(txtPhone.getText().toString());
			// aktuelleAufgabe.setPrioritaetIconId(iconIdEdited);
			// aktuelleAufgabe.setDatum(calendarEdited);
			//
			// our_db.update(aktuelleAufgabe.getId(), aktuelleAufgabe.getTitel(), aktuelleAufgabe.getBeschreibung(),
			// aktuelleAufgabe.getPhone_number(), iconIdEdited, calendarEdited, alarmSet);
			// our_db.close();
			//
			// setTrueOrFalse(txtTitel, false);
			// setTrueOrFalse(txtBeschreibung, false);
			// setTrueOrFalse(txtPhone, false);
			// spinner.setEnabled(false);
			// editDate.setEnabled(false);
			// }
			//
			// // ALARM aktualisieren
			// if (alarmSet) {
			// // Alarm wurde nicht ausgeschaltet oder veraendert.
			// OurAlarmManager.editAlarm(this, aktuelleAufgabe, 0); // Alarm aktualisieren
			// } else {
			// // Alarm wurde ausgeschaltet.
			// OurAlarmManager.cancelAlarm(aktuelleAufgabe.getId());
			// }
			//
			// return true;

		case R.id.action_discard:

			// Alarm ausschalten
			OurAlarmManager.cancelAlarm(aktuelleAufgabe.getId());

			// Von der DB löschen
			our_db.delete(aktuelleAufgabe.getId());

			Toast.makeText(
					// --> (Task "MIT CHRISTIAN ..." discarded)
					this, // Context
					getString(R.string.task_cap)
							+ " \""
							+ ((aktuelleAufgabe.getTitel().length() > 12) ? aktuelleAufgabe.getTitel().subSequence(0, 13) + "..."
									: aktuelleAufgabe.getTitel()) + "\" " + getString(R.string.deleted),
					Toast.LENGTH_LONG).show();

			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		our_db.close();
	}

	public void setEditable() {
		mEditTaskButton.setImageResource(R.drawable.ic_action_content_save);
		setTrueOrFalse(txtTitel, true);
		setTrueOrFalse(txtBeschreibung, true);
		setTrueOrFalse(txtPhone, true);
		spinner.setEnabled(true);
		editDate.setEnabled(true);
		bPickPhoneNumber.setVisibility(View.VISIBLE);

		// txtBeschreibungTextView.setMovementMethod(ArrowKeyMovementMethod.getInstance());
		txtBeschreibung.setMovementMethod(ArrowKeyMovementMethod.getInstance());
		txtPhone.setMovementMethod(ArrowKeyMovementMethod.getInstance());
		mEditTaskButton.setSelected(true);
	}

	// diese methode wird benötigt, um die views true/false für die bearbeitung zu setzen
	private void setTrueOrFalse(View view, boolean ok) {
		view.setFocusable(ok);
		view.setFocusableInTouchMode(ok);
		view.setClickable(ok);

	}

	private void spinnerAnlegen() {
		// hier wird der Spinner (werte: normal, wichtig und sehr wichtig aus
		// der xml datei prio_werte_fuer_spinner.xml gezogen und eingefügt
		spinner = (Spinner) findViewById(R.id.spDetail_Prio);
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
					iconIdEdited = 1;
					break;
				case 1:
					iconIdEdited = 2;
					break;
				case 2:
					iconIdEdited = 3;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// wird nie aufgerufen, da wir immer eine auswahl haben
			}
		});
		spinner.setEnabled(false);
		spinner.setAdapter(new CustomSpinnerAdapter(this));
		spinner.setSelection(setSpinnerPosition);
	}

	private void txtAnlegen() {
		txtTitel = (EditText) findViewById(R.id.txtDetail_Titel);
		txtTitel.setText(aktuelleAufgabe.getTitel());
		// txtTitel.setOnClickListener(new MyOnClickListener());
		// txtTitel.setOnFocusChangeListener(new MyTxtOnFocusChangeListener());
		setTrueOrFalse(txtTitel, false);

		txtBeschreibung = (EditText) findViewById(R.id.txtDetail_Beschreibung);
		txtBeschreibung.setText(aktuelleAufgabe.getBeschreibung());
		// txtBeschreibungTextView = (TextView) findViewById(R.id.txtDetail_Beschreibung);
		// txtBeschreibungTextView.setText(txtBeschreibung.getText().toString());
		// Linkify.addLinks(txtBeschreibungTextView, Linkify.ALL);
		// txtBeschreibung.setOnClickListener(new MyOnClickListener());
		// txtBeschreibung.setOnFocusChangeListener(new MyTxtOnFocusChangeListener());
		setTrueOrFalse(txtBeschreibung, false);
		txtBeschreibung.setMovementMethod(LinkMovementMethod.getInstance());

		txtPhone = (EditText) findViewById(R.id.txtDetail_PhoneNumber);
		txtPhone.setText(aktuelleAufgabe.getPhone_number());
		setTrueOrFalse(txtPhone, false);
		Linkify.addLinks(txtPhone, Linkify.PHONE_NUMBERS);
		txtPhone.setMovementMethod(LinkMovementMethod.getInstance());

	}

	public void hideKeyborad(View view) { // Virtuelle Tastatur verstecken, wenn wo anders geklickt wurde
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) Detail.this.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	// Ende - Eigene Methoden

	// ********************************************************************************
	// Interne Klassen
	// ********************************************************************************
	// class MyOnClickListener implements OnClickListener {
	// @Override
	// public void onClick(View v) {
	// if (v.getId() == R.id.txtDetail_Titel)
	// txtTitel.setBackgroundColor(Color.WHITE);
	// if (v.getId() == R.id.txtDetail_Beschreibung)
	// txtBeschreibung.setBackgroundColor(Color.WHITE);
	// }
	// }
	//
	// class MyTxtOnFocusChangeListener implements OnFocusChangeListener {
	// @Override
	// public void onFocusChange(View v, boolean hasFocus) {
	// if (hasFocus == true) {
	// if (v.getId() == R.id.txtDetail_Titel)
	// txtTitel.setBackgroundColor(Color.WHITE);
	// if (v.getId() == R.id.txtDetail_Beschreibung)
	// txtBeschreibung.setBackgroundColor(Color.WHITE);
	// }
	// }
	// }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case android.R.id.content:
			hideKeyborad(findViewById(android.R.id.content));
			return false; // true zurueckgeben, wenn es auf weitere events reagieren soll. (ACTION_UP, ACTION_MOVE, ...)
			// Wenn nicht false, wird es mehrmals aufgerufen.
		case R.id.spDetail_Prio:
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) { // Ohne diese Zeile, wird das 2 mal ausgeführt, wegen DOWN und UP.

				Toast.makeText(this, R.string.priority, Toast.LENGTH_SHORT).show();
				hideKeyborad(spinner); // Virtuelle Tastatur verstecken, wenn wo anders geklickt wurde
			}
			return false; // false zurueck geben, damit es nicht auf weitere Events reagiert und onTouch aufruft.
		}
		return false;
	}

	// Ende - Interne Klasse

}

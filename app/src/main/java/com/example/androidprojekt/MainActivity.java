package com.example.androidprojekt;

// ACTIVITY LEBENSZYKLUS:
// onCreate() == Activity wird aufgebaut. Hier werden XML-Daten in Java geparsed.
// onStart() == Activity wird gestartet. XML muss nicht nochmal geparsed werden.
// onResume() == Ab hier ist die Activity sichtbar.
// onPause() == Wenn die Activity durch einen Dialog oder Ähnliches "verdunkelt" wird, aber nicht geschlossen wird.
// onStop() == Wenn eine andere Activiy gestartet wird. Alle Einstellungen bleiben in Bundles gespeichert.
// Wenn Ressourcenmangel oder Back-Button von der onStop()-Methode --> onDestroy() == Ressourcen werden freigegeben.
// Wenn das Geraet um 90° gedreht wird --> ...onDestroy() und wieder onCreate() mit dem View in Querformat.

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {

	// private static final String TAG = MainActivity.class.getSimpleName();

	private static final int SORT_BY_DATE = 1, SORT_BY_PRIORITY_DESC = 2, SORT_BY_PRORITY_ASC = 3;
	int sortArgActual = 1;
	boolean sortByChanged = false;

	ImageButton mNewTaskButton, mHistoryButton;

	static ArrayList<Aufgabe> listeAufgaben;
	static ArrayAdapter<Aufgabe> adapter;

	TextView mTasksStatusTextView;
	ListView mListView;
	Spinner sSortByThis;

	OurOpenHandler our_db;// Datenbank

	boolean datumAusgewaehlt;

	Calendar actualDate;
	Button chosenDate;

	static String query = "";
	SearchView searchView;
	MenuItem searchItem, sortItem, calendarItem, removeAllItem;

	// ***********************************************************************************************
	// Override Methoden
	// ***********************************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setTitle(R.string.all);

		mTasksStatusTextView = (TextView) findViewById(R.id.tasksStatusTextView);
		/*
		 * Gucken, ob die App zum Ersten Mal ausgefuehrt wird (mittels SharedPreferences).
		 * Wenn Ja, Welcome-Nachricht anzeigen & Passwort festlegen.
		 * Wenn Nein, zur UnlockActivity navigieren.
		 * 
		 * SharedPreferences ist eine XML-Datei, gespeichert im Android-System (nicht im Eclipse-Projekt).
		 * Es besitzt key und value (also nur zwei Spalten) und bleibt persistent, unabhaengig von dem LifeCycle der Activities.
		 */
		if (getSharedPreferences("Einstellungen", MODE_PRIVATE).getBoolean("firstrun", true) == true) {
			/*
			 * Name ("Einstellungen") ist beliebig waehlbar und bleibt von nun an konsistent.
			 * MODE_PRIVATE = nur diese app und keine andere kann auf die Preferences zugreifen.
			 * getBoolean("key", defValue) guckt nach dem Schluessel "firstrun".
			 * Wenn so ein key nicht existiert, einfach den Standardwert zurueckgeben, was in dem Fall wahr ist.
			 */

			Intent intent = new Intent(this, SignUpActivity.class);
			/*
			 * MainActivity von der history entfernen, sodass beim Zurueckklicken
			 * von der SignUpActivity nicht die MainActivity angezeigt wird,
			 * sondern gleich HomeScreen.
			 */
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // SignUpActivity soll die neue Activity sein.
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // Entferne die vorherigen Activities aus der History.
			startActivity(intent);

			/*
			 * Hier wird der Key "firstrun" mit dem Wert false erstellt,
			 * sodass beim naechsten onCreate(..)-Aufruf getBoolean(firstrun, ...) false zurueck gibt.
			 */
			getSharedPreferences("Einstellungen", MODE_PRIVATE).edit().putBoolean("firstrun", false).commit();
		} else {
			// zum Ausblenden vom UpButton im ActionBar (siehe UnlockActivity.java).
			getSharedPreferences("Einstellungen", MODE_PRIVATE).edit().putBoolean("nofirstunlock", true).commit();

			// Gucken, ob der User im Cash gespeichert ist. Wenn nicht, (z.B. nach dem Lock) zur UnlockActivity navigieren.
			ParseUser currentUser = ParseUser.getCurrentUser();
			if (currentUser == null) {
				UnlockActivity.navigateToUnlock(this);
			}
		}

		View view = findViewById(R.id.main_view);
		view.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					searchItem.collapseActionView();
					updateMenuItemVisibility();
				}
				return false;
			}
		});

		mNewTaskButton = (ImageButton) findViewById(R.id.newTaskButton);
		mNewTaskButton.setOnClickListener(this);
		mNewTaskButton.setOnLongClickListener(this);
		mHistoryButton = (ImageButton) findViewById(R.id.historyButton);
		mHistoryButton.setOnClickListener(this);
		mHistoryButton.setOnLongClickListener(this);

		// datum für den Button erzeugen
		actualDate = Calendar.getInstance();

		our_db = new OurOpenHandler(this);

		listeAufgaben = new ArrayList<Aufgabe>();

		listViewBefuellen();
	}

	@Override
	// dbLesen sollte nicht im onCreate sein. Sonst Crash. kA warum.
	protected void onStart() {
		super.onStart();

		dbLesen();

	}

	// @Override
	// protected void onResume() {
	//
	// super.onResume();
	//
	// ImageView alarmImageView = (ImageView) findViewById(R.id.alarmSetImageView);
	// if (!alarmSet) {
	// alarmImageView.setImageResource(android.R.color.transparent);
	// }
	// }

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.newTaskButton:
			intent = new Intent(MainActivity.this, AufgabeErstellen.class);
			startActivity(intent);
			break;
		case R.id.historyButton:
			intent = new Intent(MainActivity.this, HistoryActivity.class);
			startActivity(intent);
			break;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.newTaskButton:
			Toast.makeText(this, mNewTaskButton.getContentDescription(), Toast.LENGTH_SHORT).show();
			break;
		case R.id.historyButton:
			Toast.makeText(this, mHistoryButton.getContentDescription(), Toast.LENGTH_SHORT).show();
			break;
		}
		return false;
	}

	// Innere Klasse zum Anzeigen des Datumauswahldialogs
	public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Hier koennen wir die Daten benutzen.
			actualDate.set(year, month, day); // Calender Daten aktualisieren.
			datumAusgewaehlt = true;
			dbLesen();
		}
	}

	@Override
	public void onBackPressed() {
		if (searchItem.isActionViewExpanded()) {
			searchItem.collapseActionView();
			updateMenuItemVisibility();
		} else
			super.onBackPressed();

	}

	@Override
	protected void onRestart() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// query zuruecksetzen, wenn eine andere Activity gestartet wird..
		query = ""; // .. damit wieder alle Aufgaben angezeigt werden, wenn man zurueck kommt.
	}

	// OPTIONS MENU
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu); // vertikales-"..."-Menu
		inflater.inflate(R.menu.main_actionbar, menu); // Suchfeld
		sortItem = menu.findItem(R.id.action_sort_by);
		calendarItem = menu.findItem(R.id.action_pick_calendar);
		removeAllItem = menu.findItem(R.id.action_remove_all_tasks);

		searchItem = menu.findItem(R.id.search);
		// magischerweise gibt es eine Klasse SearchManager, der das Suchen uebernimmt:
		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnClickListener(this);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) { // Wenn user auf enter drueckt
				if (!listeAufgaben.isEmpty()) {
					Intent intent = new Intent(getApplicationContext(), Detail.class); // anstatt MainActivity.this!
					intent.putExtra("Aktuelle_Aufgabe", listeAufgaben.get(0)); // Erste Aufgabe aufrufen, die gefunden wurde.
					startActivity(intent);
				}
				searchItem.collapseActionView();// Achtung: ohne diese Zeile wird die Detailansicht nicht angezeigt.

				updateMenuItemVisibility();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				MainActivity.query = newText;
				dbLesen();
				if (query.length() == 0)
					mTasksStatusTextView.setText(R.string.search_for_title_or_description);
				return false;
			}
		});
		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean queryTextFocused) {
				if (!queryTextFocused) {
					searchItem.collapseActionView();

					updateMenuItemVisibility();
					updateTasksStatus();
				}
			}
		});

		updateMenuItemVisibility();
		return true;
	}

	public void updateMenuItemVisibility() {
		if (sortItem != null)
			if (listeAufgaben.size() < 2 && !datumAusgewaehlt) {
				calendarItem.setEnabled(false);
				sortItem.setEnabled(false);
				sortItem.getIcon().setAlpha(50);
				if (listeAufgaben.size() < 1) {
					searchItem.setEnabled(false);
					searchItem.getIcon().setAlpha(66);
					removeAllItem.setEnabled(false);
				} else {
					searchItem.setEnabled(true);
					searchItem.getIcon().setAlpha(255);
					removeAllItem.setEnabled(true);
				}
			} else {
				calendarItem.setEnabled(true);
				sortItem.setEnabled(true);
				sortItem.getIcon().setAlpha(255);
				removeAllItem.setEnabled(true);
				searchItem.setEnabled(true);
				searchItem.getIcon().setAlpha(255);
				
			}
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

		case R.id.search:
			mTasksStatusTextView.setText(R.string.search_for_title_or_description);
			break;

		case R.id.action_lock: // LOGOUT
			// ParseUser.logOut();
			UnlockActivity.navigateToUnlock(this);
			break;

		case R.id.action_remove_all_tasks: // DATENBANK LOESCHEN
			searchItem.collapseActionView();
			updateMenuItemVisibility();
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.alertdialog_title_are_u_sure)
					.setMessage(R.string.alertdialog_message_remove_all_tasks)
					.setPositiveButton(android.R.string.yes, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							deleteDB();
							// searchItem.getIcon().setAlpha(66);
							// sortItem.getIcon().setAlpha(50);
						}
					}).setNegativeButton(R.string.never_mind, null);
			builder.create().show();
			break;

		case R.id.action_sort_by:
			if (listeAufgaben.size() > 1) {
				DialogFragment sortFragment = new SortByDialog();
				sortFragment.show(getSupportFragmentManager(), "sort");
			}
			break;

		case R.id.action_pick_calendar:
			searchItem.collapseActionView();
			updateMenuItemVisibility();
			// DatePickerDialog zum Auswaehlen des Datums wird angezeigt
			DialogFragment newFragment = new DatePickerFragment(); // innere Klasse
			newFragment.show(getSupportFragmentManager(), "timePicker");

			break;

		case R.id.about:
			searchItem.collapseActionView();
			AlertDialog.Builder builderAbout = new AlertDialog.Builder(this).setMessage(
					getString(R.string.app_name)
							+ " | Version: 1\n\nCreated by:\nAmar Javid, Boghyon Hoffmann, Christian Frei, Sergej But, Trang Nguyen")
					.setPositiveButton(android.R.string.ok, null);
			builderAbout.create().show();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private class SortByDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.sort).setItems(R.array.saSortByThis, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						sortArgActual = SORT_BY_DATE;
						sortByChanged = true;
						break;
					case 1:
						sortArgActual = SORT_BY_PRIORITY_DESC;
						sortByChanged = true;
						break;
					case 2:
						sortArgActual = SORT_BY_PRORITY_ASC;
						sortByChanged = true;
						break;
					}
					dbLesen();
				}
			});
			return builder.create();
		}
	}

	// Ende - Override Methoden

	// ***********************************************************************************************
	// Eigene Methoden
	// ***********************************************************************************************

	private void sortList() {
		switch (sortArgActual) {
		case SORT_BY_DATE:
			Collections.sort(MainActivity.listeAufgaben, new AufgabeDateComparator());
			sortByChanged = false;
			break;
		case SORT_BY_PRIORITY_DESC:
			Collections.sort(MainActivity.listeAufgaben, new AufgabePriority_DESC_Comparator());
			sortByChanged = false;
			break;
		case SORT_BY_PRORITY_ASC:
			Collections.sort(MainActivity.listeAufgaben, new AufgabePriority_ASC_Comparator());
			sortByChanged = false;
			break;
		}
	}

	// DATENBANK LESEN
	public void dbLesen() {

		File database = getApplicationContext().getDatabasePath("aufgabenVerwaltung.db");

		listeAufgaben.clear();
		adapter.clear();
		adapter.notifyDataSetChanged();

		if (database.exists()) {

			SQLiteDatabase db = our_db.getWritableDatabase();

			Cursor c;

			// wird geprüft, ob Bedingung nach der Sortierung geändert wurde

			if (datumAusgewaehlt) {
				String whereClause = OurOpenHandler.COLUMN_DAY + " = ?" + " AND " + OurOpenHandler.COLUMN_MONTH + " = ?" + " AND "
						+ OurOpenHandler.COLUMN_YEAR + " = ?";
				String[] whereArgs = new String[] { String.valueOf(actualDate.get(Calendar.DAY_OF_MONTH)),
						String.valueOf(actualDate.get(Calendar.MONTH)), String.valueOf(actualDate.get(Calendar.YEAR)) };
				c = getOurCursor(db, whereClause, whereArgs);
			} else if (query.length() >= 1) { // wenn im Suchfeld etwas eingegeben wurde:
				String whereClause = OurOpenHandler.COLUMN_TITLE + " LIKE ?" + " OR " + OurOpenHandler.COLUMN_DESCRIPTION + " LIKE ?";
				// MATCH-Operator nur mit einer virtuellen Tabelle kompatibel!
				String[] whereArgs = new String[] { "%" + query + "%", "%" + query + "%" }; // zwei parameter fuer zwei Like-Operatoren.
				c = getOurCursor(db, whereClause, whereArgs);
			} else {
				c = getOurCursor(db, null, null);
			}

			if (c.moveToFirst()) {
				// um sicherzustellen, dass der Cursor ganz am Anfang anfaengt.
				// Wenn die erste Zeile leer ist (bzw. c.isNull(0)), wird false zurueckgegeben.

				// Schleife, solange die naechste Zeile existiert.
				do {
					Aufgabe aufgabe = new Aufgabe();

					aufgabe.setId(c.getInt(0));
					aufgabe.setTitel(c.getString(1));
					aufgabe.setBeschreibung(c.getString(2));
					aufgabe.setPhone_number(c.getString(3));
					aufgabe.setPrioritaetIconId(c.getInt(4));

					Calendar cl = Calendar.getInstance(); // 5 == year, 6 == month, 7 == day, 8 == hour, 9 == min, 0 sec.
					cl.set(c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), c.getInt(9), 0);
					aufgabe.setDatum(cl);

					aufgabe.setAlarmSet((c.getInt(10) == 1));

					listeAufgaben.add(aufgabe);
				} while (c.moveToNext()); // gibt true zurueck, wenn naechste Zeile vorhanden.
			} else {
				System.out.println("DB Leer");
			}
			c.moveToFirst();

			our_db.close();

			if (sortByChanged) {
				sortList();
			}

		} else {
			System.out.println("Keine Datenbank");
		}

		updateTasksStatus();
		updateMenuItemVisibility();
	}

	public void updateTasksStatus() {
		String xTaskS = listeAufgaben.size() + " " + ((listeAufgaben.size() == 1) ? getString(R.string.task) : getString(R.string.tasks));

		if (searchItem != null && searchItem.isActionViewExpanded() && listeAufgaben.size() == 0)
			mTasksStatusTextView.setText(R.string.nothing_found);
		else if (searchItem != null && searchItem.isActionViewExpanded() && query.length() > 0)
			mTasksStatusTextView.setText(xTaskS + " " + getString(R.string.found));
		else
			mTasksStatusTextView.setText((listeAufgaben.size() == 0) ? getString(R.string.no_tasks)
					: (xTaskS + " " + getString(R.string.to_do)));

	}

	public Cursor getOurCursor(SQLiteDatabase db, String where, String[] args) {

		Cursor c = db.query(OurOpenHandler.TABLE_NAME, new String[] { OurOpenHandler.COLUMN_NOTE_ID, OurOpenHandler.COLUMN_TITLE,
				OurOpenHandler.COLUMN_DESCRIPTION, OurOpenHandler.COLUMN_PHONE_NUMBER, OurOpenHandler.COLUMN_ICON_ID,
				OurOpenHandler.COLUMN_YEAR, OurOpenHandler.COLUMN_MONTH, OurOpenHandler.COLUMN_DAY, OurOpenHandler.COLUMN_HOUR,
				OurOpenHandler.COLUMN_MINUTE, OurOpenHandler.COLUMN_ALARMSET }, // new String[]{...} == Attribute, die wir haben wollen.
				where, // select
				args, // selection args
				null, // group by
				null, // having
				null); // order ... brauchen wir hier alles nicht. Deshalb alles null.
		return c;
	}

	// DATENBANK-Inhalt LÖSCHEN
	public void deleteDB() {
		deleteDatabase("aufgabenVerwaltung.db");
		System.out.println("Gelöscht?");
		adapter.notifyDataSetChanged();
		listeAufgaben.clear();

		// Alle Alarme und Notifications ausschalten
		OurAlarmManager.cancelAll();
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationMgr.cancelAll();

		removeAllItem.setEnabled(false); // Options menu "remove all tasks" wieder deaktivieren
		searchItem.setEnabled(false);

		// updateTasksStatus();
		updateMenuItemVisibility();
		dbLesen();
	}

	// public void navigateToUnlock() {
	// Intent intent = new Intent(this, UnlockActivity.class);
	// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// // naechste Kommentarzeile BITTE NICHT LOESCHEN. Koennte noch gebraucht werden.
	// // getSharedPreferences("Einstellungen", MODE_PRIVATE).edit().putBoolean("destroyed", false).commit();
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	// startActivity(intent);
	// }

	// hier wird die ListView mit daten aus dem Array "listeAufgaben" befüllt
	private void listViewBefuellen() {
		adapter = new AufgabeAdapter();
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(adapter);

		// hier wird der listener für die klicks auf den listview registriert.
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent myIntent = new Intent(MainActivity.this, Detail.class);
				myIntent.putExtra("Aktuelle_Aufgabe", listeAufgaben.get(position));
				startActivity(myIntent);

			}
		});

		// Beim Long-Click die Beschreibung der Aufgabe einblenden
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!listeAufgaben.get(position).getBeschreibung().isEmpty())
					Toast.makeText(MainActivity.this, listeAufgaben.get(position).getBeschreibung(), Toast.LENGTH_LONG).show();
				return false;
			}
		});
	}

	// Ende - Eigene Methoden

	// ********************************************************************************
	// Interne Klassen
	// ********************************************************************************

	// Adapter
	public class AufgabeAdapter extends ArrayAdapter<Aufgabe> {

		public AufgabeAdapter() {
			super(MainActivity.this, R.layout.listview_item, listeAufgaben);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// sichergehen dass wir ein View haben (convertView koennte null sein)
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.listview_item, parent, false);
			}

			// Aufgabe herauspicken (aktuelle position)
			Aufgabe currentAufgabe = listeAufgaben.get(position);

			// ***************************************************************************************************
			// den View befüllen
			// wichtig hier !itemView.!findViewByID, weil mit dem View mit dem
			// wir arbeiten, enthaelt auch views ... falls ich es richtig
			// verstanden habe... also wenn jemand genau weiss, ergaenzen
			// ***************************************************************************************************

			// PRIORITAET festlegen

			ImageView imgPrio = (ImageView) itemView.findViewById(R.id.imagePrioritaet);
			int imgNr = 0;
			if (currentAufgabe.getPrioritaetIconId() == 1)
				imgNr = R.drawable.normal;
			else if (currentAufgabe.getPrioritaetIconId() == 2)
				imgNr = R.drawable.wichtig;
			else if (currentAufgabe.getPrioritaetIconId() == 3)
				imgNr = R.drawable.sehr_wichtig;
			imgPrio.setImageResource(imgNr);

			// ALARM ICON entsprechend anzeigen

			boolean alarmSet = currentAufgabe.getAlarmSet();
			ImageView alarmSetImageView = (ImageView) itemView.findViewById(R.id.alarmSetImageView);
			alarmSetImageView.setImageResource(R.drawable.ic_action_action_alarm_on); // <-- Sonst kein Alarmzeichen fuer die 1. Aufgabe!
			if (!alarmSet) {
				alarmSetImageView.setImageResource(android.R.color.transparent);
			}

			// SCHLAGWORTE textview bearbeiten

			TextView tvSchlagwort = (TextView) itemView.findViewById(R.id.tvTitel); // ist ellipsized == ".." am Ende.
			if (alarmSet) {
				int breitePx = getResources().getDisplayMetrics().widthPixels;
				int densitiyDpi = getResources().getDisplayMetrics().densityDpi;

				if (breitePx == 1080 && densitiyDpi == 480 || breitePx == 720 && densitiyDpi == 320) // z.B. Nexus 5, Nexus Galaxy
					tvSchlagwort.setMaxEms(15); // Breite des 'M's. Setzt die Grenze wo "..." beginnen soll.
				else if (breitePx == 768 && densitiyDpi == 320) // z.B. Nexus 4
					tvSchlagwort.setMaxEms(16);
				else if (densitiyDpi == 240) // z.B. Nexus S, Nexus One
					tvSchlagwort.setMaxEms(12);
			} else
				tvSchlagwort.setMaxEms(18); // selben Bug wie oben beheben - auch wenn ems in XML schon vorgenommen wurde.

			String titel = currentAufgabe.getTitel();
			tvSchlagwort.setText(titel);
			tvSchlagwort.setMovementMethod(null);
			tvSchlagwort.setText(titel);

			// genaue beschreibung bearbeiten
			// TextView tvBeschreibun = (TextView) itemView.findViewById(R.id.tvBeschreibung);
			// tvBeschreibun.setText(currentAufgabe.getBeschreibung());

			// DATUM bearbeiten

			TextView tvDate = (TextView) itemView.findViewById(R.id.tvDate);
			Calendar cl = currentAufgabe.getDatum();
			tvDate.setText(cl.get(Calendar.DAY_OF_MONTH) + "." + (cl.get(Calendar.MONTH) + 1) + "." + cl.get(Calendar.YEAR));

			return itemView;
		}
	}

	// Ende - Interne Klasse
}

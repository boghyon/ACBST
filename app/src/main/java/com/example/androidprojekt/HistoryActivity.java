package com.example.androidprojekt;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends Activity {

	// private static final String TAG = HistoryActivity.class.getSimpleName();

	static ArrayList<Aufgabe> listeAufgabenHistory;
	static ArrayAdapter<Aufgabe> adapter;
	ListView lvHistory;

	HistoryOpenHandler helper_db;

	static String query = ""; // fuer die Suchfunktion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
	}

	@Override
	protected void onStart() {
		super.onStart();
		query = ""; // suchquery wieder zuruecksetzen

		helper_db = new HistoryOpenHandler(this);
		listeAufgabenHistory = new ArrayList<Aufgabe>();

		adapter = new AufgabeAdapter();
		lvHistory = (ListView) findViewById(R.id.lvHistory);

		// hier wird der listener für die klicks auf den listview registriert.
		lvHistory.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// falls man hier auf die klicks auf den listview reagieren will
			}
		});

		lvHistory.setAdapter(adapter);
		lvHistory.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String beschreibung = listeAufgabenHistory.get(position).getBeschreibung();
				if (!beschreibung.isEmpty())
					Toast.makeText(HistoryActivity.this, beschreibung, Toast.LENGTH_LONG).show();
				return false;
			}
		});

		lvHistory.setOnTouchListener(new OnTouchListener() {
			// Nicht scrollbar machen
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE)
					return true; // true == auf keine weitere Aktionen mehr hoeren
				return false;
			}
		});

		dbLesen();
	}

	public void dbLesen() {
		File database = getApplicationContext().getDatabasePath("history.db");

		listeAufgabenHistory.clear();
		adapter.clear();
		adapter.notifyDataSetChanged();

		if (database.exists()) {

			SQLiteDatabase db = helper_db.getWritableDatabase();

			Cursor c;

			if (query.length() >= 1) {
				String whereClause = HistoryOpenHandler.COLUMN_TITLE + " LIKE ?" + " OR " + HistoryOpenHandler.COLUMN_DESCRIPTION
						+ " LIKE ?";
				// MATCH-Operator nur mit einer virtuellen Tabelle kompatibel!
				String[] whereArgs = new String[] { "%" + query + "%", "%" + query + "%" }; // zwei parameter fuer zwei Like-Operatoren.
				c = getOurCursor(db, whereClause, whereArgs);
			} else {
				c = getOurCursor(db, null, null);
			}

			if (c.moveToFirst()) {
				// um sicherzustellen, dass der Cursor ganz am Anfang anfaengt.
				// Wenn die erste Zeile leer ist (c.isNull(0) == true), DB ist leer.

				// Schleife, solange die naechste Zeile existiert.
				do {
					Aufgabe aufgabe = new Aufgabe();

					aufgabe.setId(c.getInt(0));
					aufgabe.setTitel(c.getString(1));
					aufgabe.setBeschreibung(c.getString(2));
					aufgabe.setPhone_number(c.getString(3));
					aufgabe.setPrioritaetIconId(c.getInt(4));

					Calendar cl = Calendar.getInstance();
					// 5 == year, 6 == month, 7 == day, 8 == hour, 9 == min, 0 sec.
					cl.set(c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), c.getInt(9), 0);
					aufgabe.setDatum(cl);

					listeAufgabenHistory.add(0, aufgabe);
				} while (c.moveToNext());

			} else {
				System.out.println("DB Leer");
			}
			c.moveToFirst();

			helper_db.close();
		} else {
			System.out.println("Keine Datenbank");
		}
	}

	public Cursor getOurCursor(SQLiteDatabase db, String where, String[] args) {

		Cursor c = db.query(HistoryOpenHandler.TABLE_NAME, new String[] { HistoryOpenHandler.COLUMN_NOTE_ID,
				HistoryOpenHandler.COLUMN_TITLE, HistoryOpenHandler.COLUMN_DESCRIPTION, HistoryOpenHandler.COLUMN_PHONE_NUMBER,
				HistoryOpenHandler.COLUMN_ICON_ID, HistoryOpenHandler.COLUMN_YEAR, HistoryOpenHandler.COLUMN_MONTH,
				HistoryOpenHandler.COLUMN_DAY, HistoryOpenHandler.COLUMN_HOUR, HistoryOpenHandler.COLUMN_MINUTE }, //
				where, // select
				args, // selection args
				null, // group by
				null, // having
				null); // order ... brauchen wir hier alles nicht. Deshalb alles null.
		return c;
	}

	// OPTIONS MENU
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.main, menu); // vertikales-"..."-Menu
		// inflater.inflate(R.menu.main_actionbar, menu); // Suchfeld
		inflater.inflate(R.menu.history, menu);
		//
		// // magischerweise gibt es eine Klasse SearchManager, der das Suchen uebernimmt:
		// SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		// SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		// searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		// searchView.setOnQueryTextListener(new OnQueryTextListener() {
		//
		// @Override
		// public boolean onQueryTextSubmit(String query) { // Wenn user auf enter drueckt: GEHT NICHT! WARUM??
		// Intent intent = new Intent(HistoryActivity.this, Detail.class);
		// intent.putExtra("Aktuelle_Aufgabe", listeAufgabenHistory.get(0));
		// startActivity(intent);
		//
		// return false;
		// }
		//
		// @Override
		// public boolean onQueryTextChange(String newText) {
		// HistoryActivity.query = newText;
		// dbLesen();
		// return false;
		// }
		// });
		//
		return true;
	}

	// @Override
	/**
	 * SETTINGS:
	 * LOGOUT: der User wird ausgeloggt und zur UnlockActivity navigiert.
	 * Die vorherigen Activities werden vom Task geloescht,
	 * sodass der User nicht mehr zurueckkehren kann. Er muss sich wieder anmelden.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_statistics:

			Intent intent = new Intent(this, StatisticsActivity.class);
			startActivity(intent);
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	// Adapter
	public class AufgabeAdapter extends ArrayAdapter<Aufgabe> {

		public AufgabeAdapter() {
			super(HistoryActivity.this, R.layout.listview_item, listeAufgabenHistory);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// sichergehen dass wir ein View haben (koennte null sein)
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.listview_item, parent, false);
			}

			// Aufgabe herauspicken (aktuelle position)
			Aufgabe currentAufgabe = listeAufgabenHistory.get(position);

			View relativeLayout = itemView.findViewById(R.id.rlBeschreibung);
			relativeLayout.setPadding(0, 0, 0, 0);
			// ***************************************************************************************************
			// den View befüllen
			// wichtig hier !itemView.!findViewByID, weil mit dem View mit dem
			// wir arbeiten, enthaelt auch views ... falls ich es richtig
			// verstanden habe... also wenn jemand genau weiss, ergaenzen
			// ******************************************************************************************************

			// PRIO bearbeiten

			ImageView imgPrio = (ImageView) itemView.findViewById(R.id.imagePrioritaet);
			int imgNr = 0;
			if (currentAufgabe.getPrioritaetIconId() == 1)
				imgNr = R.drawable.normal;
			else if (currentAufgabe.getPrioritaetIconId() == 2)
				imgNr = R.drawable.wichtig;
			else if (currentAufgabe.getPrioritaetIconId() == 3)
				imgNr = R.drawable.sehr_wichtig;
			imgPrio.setImageResource(imgNr);

			// ALARM Zeichen weg machen

			ImageView alarmSetImageView = (ImageView) itemView.findViewById(R.id.alarmSetImageView);
			alarmSetImageView.setImageResource(android.R.color.transparent);

			// schlagwort textview bearbeiten
			TextView tvSchlagwort = (TextView) itemView.findViewById(R.id.tvTitel);
			tvSchlagwort.setText(currentAufgabe.getTitel());

			// genaue beschreibung bearbeiten
			// TextView tvBeschreibun = (TextView) itemView.findViewById(R.id.tvBeschreibung);
			// tvBeschreibun.setText(currentAufgabe.getBeschreibung());

			// Datum bearbeiten

			TextView tvDate = (TextView) itemView.findViewById(R.id.tvDate);

			Calendar cl = currentAufgabe.getDatum();
			tvDate.setText("Done: " + cl.get(Calendar.DAY_OF_MONTH) + "." + (cl.get(Calendar.MONTH) + 1) + "." + cl.get(Calendar.YEAR));

			return itemView;
		}
	}
}

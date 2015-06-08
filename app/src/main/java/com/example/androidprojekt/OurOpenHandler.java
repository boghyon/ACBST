package com.example.androidprojekt;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class OurOpenHandler extends SQLiteOpenHelper {

	private static final String TAG = OurOpenHandler.class.getSimpleName();

	// *************************************************
	// Name und Version der DB
	// **************************************************
	private static final String DB_NAME = "aufgabenVerwaltung.db"; // Name egal, aber ".db" muss sein fuer SQLite-DB.
	private static final int DB_VERSION = 1;

	// *************************************************
	// Namen der Attribute. BaseColumns._ID wird als ID verwendet
	// *************************************************
	public static final String TABLE_NAME = "Aufgaben";
	public static final String COLUMN_NOTE_ID = "note_id";
	public static final String COLUMN_TITLE = "Titel";
	public static final String COLUMN_DESCRIPTION = "beschreibung";
	public static final String COLUMN_PHONE_NUMBER = "phone_number";
	public static final String COLUMN_ICON_ID = "icon_id";
	public static final String COLUMN_YEAR = "year";
	public static final String COLUMN_MONTH = "month";
	public static final String COLUMN_DAY = "day";
	public static final String COLUMN_HOUR = "hour";
	public static final String COLUMN_MINUTE = "minute";
	public static final String COLUMN_ALARMSET = "alarmSet"; // als 1 oder 0, anstatt boolean

	// ************************************************
	// zum Anlegen
	// ************************************************
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" //
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // BaseColumns._ID == Konvention zum Einsatz des PKs.
			// AUTOINCREMENT uebernimmt die Aenderung am PK, wenn I/S/U/D.
			+ COLUMN_NOTE_ID + " INTEGER, " // Spalte 0
			+ COLUMN_TITLE + " TEXT, " // Spalte 1
			+ COLUMN_DESCRIPTION + " TEXT, " // Spalte 2
			+ COLUMN_PHONE_NUMBER + " TEXT, " // 3
			+ COLUMN_ICON_ID + " INTEGER, " // 4
			+ COLUMN_YEAR + " INTEGER, " // 5
			+ COLUMN_MONTH + " INTEGER, " // 6
			+ COLUMN_DAY + " INTEGER, " // 7
			+ COLUMN_HOUR + " INTEGER, " // 8
			+ COLUMN_MINUTE + " INTEGER, " // 9
			+ COLUMN_ALARMSET + " INTEGER );"; // 10

	// ******************************************************
	// Konstruktor
	// *****************************************************
	public OurOpenHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public void insert(int aufgabe_id, String titel, String beschreibung, String phone, int icon_id, Calendar calendar, boolean alarmSet) {
		long rowId = -1;
		try {
			// Datenbank oeffnen
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			// transaction-Methoden tragen zur Thread-Sicherheit und Verbesserung des Performance bei.

			// die zu speichernden Werte
			ContentValues values = new ContentValues();
			values.put(COLUMN_NOTE_ID, aufgabe_id);
			values.put(COLUMN_TITLE, titel);
			values.put(COLUMN_DESCRIPTION, beschreibung);
			values.put(COLUMN_PHONE_NUMBER, phone);
			values.put(COLUMN_ICON_ID, icon_id);
			values.put(COLUMN_YEAR, calendar.get(Calendar.YEAR));
			values.put(COLUMN_MONTH, calendar.get(Calendar.MONTH));
			values.put(COLUMN_DAY, calendar.get(Calendar.DAY_OF_MONTH));
			values.put(COLUMN_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
			values.put(COLUMN_MINUTE, calendar.get(Calendar.MINUTE));
			values.put(COLUMN_ALARMSET, (alarmSet) ? 1 : 0);
			// in die Tabelle einfügen
			rowId = db.insert(TABLE_NAME, null, values);

			db.setTransactionSuccessful();
			db.endTransaction();

			// Datenbank schliessen, um potenzielle Crashes zu vermeiden.
			db.close();
		} catch (SQLiteException e) {
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId = " + rowId);
		}
	}

	// OVERLOAD, damit eine Aufgabe auch OHNE ein bestimmtes Datum erstellt werden kann.
	public void insert(String titel, String beschreibung, String phone, int icon_id) {
		long rowId = -1;
		try {
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();

			ContentValues values = new ContentValues();
			values.put(COLUMN_TITLE, titel);
			values.put(COLUMN_DESCRIPTION, beschreibung);
			values.put(COLUMN_PHONE_NUMBER, phone);
			values.put(COLUMN_ICON_ID, icon_id);

			rowId = db.insert(TABLE_NAME, null, values);
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			Log.e(TAG, "insert(..) ohne Calendar gewesen");
		} catch (SQLiteException e) {
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId = " + rowId);
		}
	}

	public int delete(int aufgabe_id) {
		SQLiteDatabase db = this.getWritableDatabase();

		int i = db.delete(TABLE_NAME, COLUMN_NOTE_ID + " = " + aufgabe_id, null);

		return i;
	}

	public int update(int aufgabe_id, String titel, String beschreibung, String phone, int icon_id, Calendar calendar, boolean alarmSet) {

		// Datenbank oeffnen
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COLUMN_TITLE, titel);
		values.put(COLUMN_DESCRIPTION, beschreibung);
		values.put(COLUMN_PHONE_NUMBER, phone);
		values.put(COLUMN_ICON_ID, icon_id);
		values.put(COLUMN_YEAR, calendar.get(Calendar.YEAR));
		values.put(COLUMN_MONTH, calendar.get(Calendar.MONTH));
		values.put(COLUMN_DAY, calendar.get((Calendar.DAY_OF_MONTH)));
		values.put(COLUMN_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
		values.put(COLUMN_MINUTE, calendar.get(Calendar.MINUTE));
		values.put(COLUMN_ALARMSET, (alarmSet) ? 1 : 0);

		int i = db.update(TABLE_NAME, values, COLUMN_NOTE_ID + " = " + aufgabe_id, null);

		db.close();

		return i;
	}

	// ********************************************************
	// Methoden
	// ********************************************************

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrade der Datenbank von Version " + oldVersion + " zu " + newVersion + "; alle Daten werden gelöscht");
	}

}

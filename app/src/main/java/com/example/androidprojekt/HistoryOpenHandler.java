package com.example.androidprojekt;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class HistoryOpenHandler extends SQLiteOpenHelper {

	private static final String TAG = HistoryOpenHandler.class.getSimpleName();

	// *************************************************
	// Name und Version der DB
	// **************************************************
	private static final String DB_NAME = "history.db";
	private static final int DB_VERSION = 1;

	// *************************************************
	// Namen der Attribute. BaseColumns._ID wird als ID verwendet
	// *************************************************
	public static final String TABLE_NAME = "History";
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

	// ************************************************
	// zum Anlegen
	// ************************************************
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" //
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // BaseColumns._ID == Konvention zum Einsatz des PKs.
			// AUTOINCREMENT uebernimmt die Aenderung am PK, wenn I/S/U/D.
			+ COLUMN_NOTE_ID + " INTEGER, " //
			+ COLUMN_TITLE + " TEXT, " //
			+ COLUMN_DESCRIPTION + " TEXT, " //
			+ COLUMN_PHONE_NUMBER + " TEXT, " //
			+ COLUMN_ICON_ID + " INTEGER, " //
			+ COLUMN_YEAR + " INTEGER, " //
			+ COLUMN_MONTH + " INTEGER, " //
			+ COLUMN_DAY + " INTEGER, " //
			+ COLUMN_HOUR + " INTEGER, " //
			+ COLUMN_MINUTE + " INTEGER );"; //

	/*
	 * BaseColumns._ID == Konvention zum Einsatz des PKs (Anstatt sein eigenes ID-Attribut).
	 * AUTOINCREMENT uebernimmt die Aenderung am PK, wenn I/S/U/D.
	 */

	// ******************************************************
	// Konstruktor
	// *****************************************************
	public HistoryOpenHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public void insert(int aufgabe_id, String titel, String beschreibung, String phone, int icon_id, Calendar calendar) {
		long rowId = -1;
		try {
			// Datenbank oeffnen
			SQLiteDatabase db = this.getWritableDatabase();
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

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrade der Datenbank von Version " + oldVersion + " zu " + newVersion + "; alle Daten werden gelöscht");

	}

}

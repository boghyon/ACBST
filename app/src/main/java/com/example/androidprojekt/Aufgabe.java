package com.example.androidprojekt;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author sergej
 *         In der Klasse Aufgabe werden die einzelnen Details zu jeder einzelnen
 *         Aufgabe gespeichert, wie: Titel, Beschreibung, Prioritaet, Datum, usw...
 *         Im activity_main.xml in der ListView werden dann die Aufträge gespeichert.
 * 
 *         WIHCITG: falls man weitere Variablen zur Aufgabe zufuegt, muss man auch
 *         den Konstruktor "Aufgabe(Parcel in)" und die Methode "writeToParcel" ändern!!!
 */

public class Aufgabe implements Parcelable {
	/*
	 * Mit dem Interface Parcelable wird es ermoeglicht, dass ein Instanz der Aufgabe-Klasse
	 * zwischen den Activities innerhalb des Intent-Objekts mittels der Methode putExtra
	 * transportiert werden kann.
	 */

	// Variablen der Klasse Aufgabe
	// 
	static private int all = 0;

	private int id = 0;
	private String titel = null;
	private String beschreibung = null;
	private String phone_number = null;
	private int prioritaetIconId = 0;
	private Calendar datum;
	private boolean alarmSet;

	// *********************************************************
	// Konstruktoren
	// *********************************************************

	public Aufgabe() {

	}

	public Aufgabe(String titel, String beschreibung, String number, int prioritaetIconId, Calendar date, boolean alarmSet) {
		super();
		id = ++all;
		this.titel = titel;
		this.beschreibung = beschreibung;
		this.phone_number = number;
		this.prioritaetIconId = prioritaetIconId;
		datum = date;
		this.alarmSet = alarmSet;
	}

	// ACHTUNG: Den Variablennamen "CREATOR" NICHT AENDERN!!! System guckt nach diesem Namen.
	public static final Parcelable.Creator<Aufgabe> CREATOR = new Parcelable.Creator<Aufgabe>() {
		@Override
		public Aufgabe createFromParcel(Parcel source) {
			return new Aufgabe(source);
		}

		@Override
		public Aufgabe[] newArray(int size) {
			return new Aufgabe[size];
		}
	};

	// Fuer Parcelable
	public Aufgabe(Parcel in) {
		// ACHTUNG: Die Reihenfolge der Zeilen muss gleich sein wie die von der Methode "writeToParcel(..)"!!!
		id = in.readInt();
		titel = in.readString();
		beschreibung = in.readString();
		phone_number = in.readString();
		prioritaetIconId = in.readInt();
		datum = (Calendar) in.readSerializable();
		alarmSet = in.readByte() != 0;
	}

	// *********************************************************
	// vom Parcelabe
	// *********************************************************
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// ACHTUNG: Die Reihenfolge der Zeilen muss mit der im Konstruktor "Aufgabe(Parcel in)" uebereinstimmen!!!
		dest.writeInt(id);
		dest.writeString(titel);
		dest.writeString(beschreibung);
		dest.writeString(phone_number);
		dest.writeInt(prioritaetIconId);
		dest.writeSerializable(datum);
		dest.writeByte((byte) (alarmSet ? 1 : 0));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	// **********************************************************
	// Getter und Setter
	// **********************************************************
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public int getPrioritaetIconId() {
		return prioritaetIconId;
	}

	public void setPrioritaetIconId(int prioritaetIconId) {
		this.prioritaetIconId = prioritaetIconId;
	}

	public Calendar getDatum() {
		return datum;
	}

	public void setDatum(Calendar datum) {
		this.datum = datum;
	}

	public boolean getAlarmSet() {
		return alarmSet;
	}

	public void setAlarmSet(boolean alarmSet) {
		this.alarmSet = alarmSet;
	}

	// *************************************************************
	// *************************************************************

	@Override
	public String toString() {
		String ausgabe = getId() + " " + getTitel() + " + " + getBeschreibung() + " + " + getPhone_number() + " + " + getPrioritaetIconId()
				+ " + " + datum.get(Calendar.DAY_OF_MONTH) + "." + (datum.get(Calendar.MONTH) + 1) + "." + datum.get(Calendar.YEAR);
		return ausgabe;
	}
}

package com.example.androidprojekt;

import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

/**
 * @author boghyon
 *         Ein DialogFragment, wo man das Datum UND die Zeit auswaehlen kann.
 *         (Im Android-API-Guide war nur von einem oder anderem die Rede.
 *         Deshalb musste ein spezielles Fragment erstellt werden, wo man beides in einem Dialog auswaehlen kann.)
 *         Hierfuer wurde dialog_datetimepicker.xml erstellt.
 * 
 *         Schritte, wie man den DateTimePicker benutzen kann:
 *         1. In der Activity das Interface "CalendarMessenger" und die entsprechende Methode implementieren.
 *         2. In der Activity eine Instanzvariable "boolean alarmSet;" erstellen.
 *         3. In der implementierten Methode "this.alarmSet = alarmSet;" und mit calendar machen, was man moechte.
 *         4. Im View.OnClickListener den DateTimePicker instanzieren.
 *         5. In der naechsten Zeile: "deinInstanz.show(getSupportFragmentManager(), "tag");".
 * 
 *         Siehe AufgabeErstellen.java oder Detail.java als Beispiele.
 */
public class DateTimePickerFragment extends DialogFragment {

	Calendar calendar; // ist schon instanziert durch den Konstruktor von der urspruenglichen Activity.
	Calendar calCurrent = Calendar.getInstance(); // Hat immer das heutige Datum.

	Button withoutAlarmButton; // Wenn der Alarm gesetzt ist, wird es zum "Turn Off Alarm"-Button.
	Button withAlarmButton;

	boolean alarmSet; // Hilfsvariable
	int aufgabeId;

	/**
	 * @param calendar
	 *            der zu verarbeitende Kalender.
	 * @param alarmSet
	 *            Ob der Alarm schon gesetzt ist. Bitte die Instanzvariable "alarmSet" von der Activity eingeben.
	 *            (Spaeter kann man den Alarm wieder ausschalten. Dafuer wird diese Variable gebraucht.)
	 *            In der implementierten Methode bitte so: this.alarmSet = alarmSet;
	 * @param aufgabeId
	 *            Beim AufgabeErstellen einfach 0 eingeben.
	 *            Ansonsten bitte die ID von der Aufgabe eingeben.
	 *            Die ID-1 entspricht der Position des entsprechenden Alarms in seiner ArrayList.
	 */
	public DateTimePickerFragment(Calendar calendar, boolean alarmSet, int aufgabeId) {
		this.calendar = calendar;
		this.alarmSet = alarmSet;
		if (aufgabeId != 0) // bei AufgabeErstellen-Activity wird dieser Konstruktor mit aufgabeId == 0 aufgerufen.
			this.aufgabeId = aufgabeId;
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.dialog_datetimepicker, null); // gibt View zurueck
		// LayoutInflater konvertiert die XML-File ins Java-Code. z.B TextView vom LayoutInflater geerbt.

		// calTemp = calendar; // calendar zwischen speichern. Wenn User abbricht, wird calTemp genommen.
		calCurrent.set(calCurrent.get(Calendar.YEAR), calCurrent.get(Calendar.MONTH), calCurrent.get(Calendar.DAY_OF_MONTH),
				calCurrent.get(Calendar.HOUR_OF_DAY), calCurrent.get(Calendar.MINUTE));

		// DATE PICKER
		DatePicker dp = (DatePicker) view.findViewById(R.id.datePicker);
		dp.setMinDate(calCurrent.getTimeInMillis()); // Verhindern, den Reminder auf den vergangenen Tag setzen zu koennen.

		// Anstatt setOnDateChangedListener gibt es beim DP die init-Methode.
		// Das Datum zum Anzeigen im DP soll das Datum von der vorher festgelegten Datum sein. Standard: heute.
		dp.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
				new DatePicker.OnDateChangedListener() {
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						calendar.set(year, monthOfYear, dayOfMonth);
					}
				});

		// TIME PICKER
		TimePicker tp = (TimePicker) view.findViewById(R.id.timePicker);
		// tp.setIs24HourView(true);
		tp.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY)); // hier ebenfalls vorher festgestzte Uhrzeit.
		tp.setCurrentMinute(calendar.get(Calendar.MINUTE));
		tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			}
		});

		// WITHOUT ALARM-BUTTON (if alarmSet: TURNOFF-ALARM-button)
		withoutAlarmButton = (Button) view.findViewById(R.id.withoutAlarmButton);
		if (alarmSet)
			withoutAlarmButton.setText(R.string.turn_off_alarm_button_label);
		withoutAlarmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (alarmSet)
					if (!getActivity().getClass().equals(AufgabeErstellen.class))// Nur wenn man NICHT von dieser Activity kommt:
						OurAlarmManager.cancelAlarm(aufgabeId);

				calMsgr.getCalendarFromPicker(calendar, false);
				dismiss(); // DialogFragment verlassen
			}
		});

		// WITH ALARM-BUTTON
		withAlarmButton = (Button) view.findViewById(R.id.withAlarmButton);
		withAlarmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// den verarbeiteten Kalender schicken, alarmSet ist true.
				calMsgr.getCalendarFromPicker(calendar, true); // wird von der Activity aufgerufen.
				dismiss();
			}
		});

		// setCancelable(false); // Fragment wird nicht geschlossen, wenn der User irgendwo ausserhalb des Fragments klickt.
		return view;
	}

	// ******************************************
	// MESSENGER zwischen der Activity und diesem Fragment.
	// Activity, die den DateTimePicker benutzt, bitte das untere Interface implementieren!
	// ******************************************

	CalendarMessenger calMsgr;

	// inneres INTERFACE kann Daten zur Activity schicken, WENN die Activity dieses Interface implementiert hat!
	interface CalendarMessenger {
		public void getCalendarFromPicker(Calendar calendar, boolean alarmSet);
	}

	/*
	 * onAttact-Methode (von der Klasse Fragment) wird mit der Referenz
	 * zur urspruenglichen Activity aufgerufen, bevor das Fragment aufgebaut wird.
	 */@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*
		 * damit die Variable calMsgr das Referenz auf die Activity bekommt
		 * und anschliessend den von hier verarbeiteten Kalender uebermittelt,
		 * wenn die Activity die entsprechende Methode (getCalendarFromPicker()) aufgerufen hat..
		 */calMsgr = (CalendarMessenger) activity;
	}
}

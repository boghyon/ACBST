package com.example.androidprojekt;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class OurAlarmManager {

	// private static final String TAG = OurAlarmManager.class.getSimpleName();

	// fuer den Reminder
	private static ArrayList<AlarmManager> alarmMgrList = new ArrayList<AlarmManager>(0);
	private static ArrayList<PendingIntent> alarmPIntentList = new ArrayList<PendingIntent>(0);

	/**
	 * NUR wenn der User, ohne einen Reminder gesetzt zu haben, eine Aufgabe erstellt!
	 */
	public static void setAlarmEmpty() {
		alarmMgrList.add(null);
		alarmPIntentList.add(null);
		// .. damit die aufgabeId-1 weiterhin der alarmPosition entspricht.
	}

	public static boolean alarmIsSet(int aufgabeID) { // AufgabeID faengt mit 1 an, nicht mit 0.
		return alarmMgrList.isEmpty() ? false : alarmMgrList.get(aufgabeID - 1) != null;
	}

	/**
	 * SetAlarm
	 * die add- anstatt set-Methode muss sein, weil die ArrayList mit der set-Methode nicht automatisch waechst.
	 * 
	 * @param context
	 *            einfach "this" bzw. DeineKlasse."this" eingeben.
	 * @param calendar
	 *            Variable mit dem Typ Calendar; Vom User festgelegten Datum
	 * @param schlagworte
	 *            String-Variable, was der User als Titel eingegeben hat
	 * @param beschreibung
	 *            String-Variable, was der User ins Beschreibungsfeld eingegeben hat.
	 * @param iconId
	 *            ID vom Prioritaetsicon (Normal? Wichtig? Sehr wichtig?)
	 * @param repeatIntervalSeconds
	 *            Wiederholungsintervall in Sekunden.
	 *            0 == Keine Wiederholung. NUR EINMAL wecken.
	 */
	public static void setAlarm(Context context, Aufgabe aufgabe, int repeatIntervalSeconds) {
		alarmMgrList.add((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
		int alarmPosition = alarmMgrList.size() - 1;
		alarmPIntentList.add(PendingIntent.getService(context, // Context
				alarmPosition, // Der requestCode muss eindeutig sein, damit mehrere Alarms ausgeloest werden koennen.
				new Intent(context, NotificationService.class).putExtra("Aktuelle_Aufgabe", aufgabe),
				// mit diesem NotificationService wird angestossen.
				PendingIntent.FLAG_CANCEL_CURRENT)); // Existiert das Selbe (mit gleichem requestCode), dann ueberschreiben.

		if (repeatIntervalSeconds != 0)
			alarmMgrList.get(alarmPosition).setRepeating(AlarmManager.RTC_WAKEUP, aufgabe.getDatum().getTimeInMillis(),
					1000 * repeatIntervalSeconds, alarmPIntentList.get(alarmPosition));
		// Wenn repeatIntervalSeconds > 9 alle Alarme werden gleichzeitig ausgeloest.
		else
			// ONE SHOT ALARM
			alarmMgrList.get(alarmPosition).set(AlarmManager.RTC_WAKEUP, aufgabe.getDatum().getTimeInMillis(),
					alarmPIntentList.get(alarmPosition));
	}

	public static void editAlarm(Context context, Aufgabe aufgabe, int repeatIntervalSeconds) {
		cancelAlarm(aufgabe.getId());

		int alarmPosition = aufgabe.getId() - 1;

		alarmMgrList.set(alarmPosition, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
		alarmPIntentList.set(alarmPosition, PendingIntent.getService(context, alarmPosition,
				new Intent(context, NotificationService.class).putExtra("Aktuelle_Aufgabe", aufgabe), PendingIntent.FLAG_CANCEL_CURRENT));

		if (repeatIntervalSeconds != 0)
			alarmMgrList.get(alarmPosition).setRepeating(AlarmManager.RTC_WAKEUP, aufgabe.getDatum().getTimeInMillis(),
					1000 * repeatIntervalSeconds, alarmPIntentList.get(alarmPosition));
		else
			alarmMgrList.get(alarmPosition).set(AlarmManager.RTC_WAKEUP, aufgabe.getDatum().getTimeInMillis(),
					alarmPIntentList.get(alarmPosition));
	}

	public static void cancelAlarm(int aufgabeId) {
		int alarmPosition = aufgabeId - 1;

		if (alarmMgrList.get(aufgabeId - 1) != null) {

			alarmMgrList.get(alarmPosition).cancel(alarmPIntentList.get(alarmPosition));
			alarmPIntentList.get(alarmPosition).cancel();

			// fuer den editText in Detail.java, damit "set Reminder" gezeigt wird, wenn der Alarm ausgeschaltet wurde.
			alarmMgrList.set(alarmPosition, null);
			alarmPIntentList.set(alarmPosition, null);
		}
	}

	/**
	 * ohne die ArrayListen zu entleeren.
	 */
	public static void cancelAll() {
		if (!alarmMgrList.isEmpty() && !alarmPIntentList.isEmpty()) {
			for (int i = 0; i < alarmMgrList.size(); i++) {
				if (alarmMgrList.get(i) != null) {
					alarmMgrList.get(i).cancel(alarmPIntentList.get(i));
					alarmPIntentList.get(i).cancel();
				}
			}
		}
	}

	/**
	 * @param emptyAlarmList
	 *            Nur zum Testen geeignet!! Benutze die andere Methode ohne Uebergabeparameter!
	 */
	public static void cancelAll(boolean emptyAlarmList) {
		cancelAll();
		if (emptyAlarmList) {
			alarmMgrList.clear();
			alarmPIntentList.clear();
		}
	}

}

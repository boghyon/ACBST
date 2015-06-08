package com.example.androidprojekt;

import java.util.Comparator;

/**
 * @author sergej
 *         hier wird es nach dem Datum Sortiert.
 *         Falls Datum1 == Datum2, dann wird auch absteigend nach der Priorität sortiert.
 */
public class AufgabeDateComparator implements Comparator<Aufgabe> {
	// private static final String TAG = AufgabeDateComparator.class.getSimpleName();

	@Override
	public int compare(Aufgabe first, Aufgabe second) {
		if (first.getDatum().compareTo(second.getDatum()) == 0) {
			if (first.getPrioritaetIconId() > second.getPrioritaetIconId()) {
				return -1;
			} else if (first.getPrioritaetIconId() < second.getPrioritaetIconId()) {
				return 1;
			}
		}
		return first.getDatum().compareTo(second.getDatum());
	}

	// getDatum().compareTo ignoriert die Uhrzeit irgendwie. Wenn die Uhrzeit auch mitberuecksichtigt werden soll..:

	// @Override
	// public int compare(Aufgabe first, Aufgabe second) {
	// long help = first.getDatum().getTimeInMillis() - second.getDatum().getTimeInMillis();
	// Log.e(TAG, "first "+String.valueOf(first.getDatum().getTimeInMillis()));
	// Log.e(TAG, "second "+String.valueOf(second.getDatum().getTimeInMillis()));
	// Log.e(TAG, "difference "+String.valueOf(help));
	//
	// if (help > 0) {
	// return 1;
	// } else if (help < 0)
	// return -1;
	// else {
	// if (first.getPrioritaetIconId() > second.getPrioritaetIconId()) {
	// return -1;
	// } else if (first.getPrioritaetIconId() < second.getPrioritaetIconId()) {
	// return 1;
	// } else
	// return 0;
	// }
	// }
}

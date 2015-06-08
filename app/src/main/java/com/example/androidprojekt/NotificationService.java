package com.example.androidprojekt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.LevelListDrawable;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

/**
 * @author boghyon
 *         Diese Klasse ist ein Service und sollte in AndroidManifest.xml als Service eingetragen werden.
 *         Services laufen im Hintergrund und können weiterhin laufen, auch wenn die App geschlossen ist.
 *         Sobald eine neue Aufgabe mit Alarm erstellt wird, wird dieser Service aufgerufen.
 * 
 */
public class NotificationService extends Service {

	Aufgabe targetAufgabe;

	// Um alle Notifications voneinander unterscheidbar zu machen; Wird um 1 inkrementiert

	@Override
	// Das System ruft diese Methode an, wenn andere Komponente mit diesem Service was machen will.
	public IBinder onBind(Intent intent) {
		return null; // wird nicht gebraucht.
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		targetAufgabe = (Aufgabe) intent.getParcelableExtra("Aktuelle_Aufgabe");
		int large_iconID = 0;
		if (targetAufgabe.getPrioritaetIconId() == 1)
			large_iconID = R.drawable.normal_big;
		else if (targetAufgabe.getPrioritaetIconId() == 2)
			large_iconID = R.drawable.wichtig_big;
		else if (targetAufgabe.getPrioritaetIconId() == 3)
			large_iconID = R.drawable.sehr_wichtig_big;

		// NOTIFICATION. http://developer.android.com/guide/topics/ui/notifiers/notifications.html

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setPriority(Notification.PRIORITY_MAX)
				// PRIORITY_MAX == Notification erscheint ueber dem ganzen Screen, egal was der User macht.
//				.setLargeIcon(BitmapFactory.decodeResource(getResources(), large_iconID)).setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), large_iconID)).setSmallIcon(R.drawable.ic_stat_notification_launcher)
				.setContentTitle(targetAufgabe.getTitel()) // Aufgabentitel, tags
				.setDefaults(Notification.DEFAULT_ALL) // mit Vibration && Sound && Light
				.setSubText("Today"); // (tomorrow, next week, next month) - Wird nicht angezeigt, wenn kein ContentText!
		if (!targetAufgabe.getBeschreibung().isEmpty())
			notificationBuilder.setContentText(targetAufgabe.getBeschreibung());

		// Explizite Intent fuer die Activity, die erscheint werden soll, wenn man auf die Notification klickt
		Intent resultIntent = new Intent(getApplicationContext(), Detail.class);
		resultIntent.putExtra("Aktuelle_Aufgabe", targetAufgabe);

		// Ein TaskStackBuilder, der einen kuenstlichen Back-Stack fuer die gestartete Activity beinhaltet:
		// Dies verhindert, dass man beim Backward-Navigation von der Activity zum Home-Screen hinausgefuehrt wird..
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Stack kennt die Hierachie meiner App noch nicht. Damit man auf die MainActivity gelangt, wenn man auf Back-/Up-Button klickt..
		stackBuilder.addParentStack(Detail.class); // Dafuer im Manifest-Detail android:parentActivityName=".MainActivity" notwendig.
		// Die Intent auf die oberste Ebene des Stacks hinzufuegen
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(targetAufgabe.getId(), PendingIntent.FLAG_UPDATE_CURRENT);
		// requestCode wird dafuer gebraucht, um alle Notifications voneinander unterscheidbar zu machen. ==> Mehrere Notifications moeglich
		// FLAG_UPDATE_CURRENT ersetzt die schon vorhandene Notification mit neuer Information.

		notificationBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(targetAufgabe.getId(), notificationBuilder.build());

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "onDestroy()", Toast.LENGTH_SHORT).show();
	}

}

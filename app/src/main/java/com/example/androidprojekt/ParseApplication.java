package com.example.androidprojekt;

import android.app.Application;

import com.parse.Parse;

/* Erklärung:
 * Android startet zuerst die Application-Klasse.
 * Eine von Application geerbte Klasse wird stattdessen verwendet, um das Parse-API beim FirstRun zu verwenden.
 * Mit Parse-APIs wird Unlock-Verwaltung fuer die Erinnerungsfunktion ermoeglicht.
 * Beim FirstRun wird man aufgefordert, sein Initialpasswort zu festzulegen.
 * Besitzt der User schon ein PW, kann er sich gleich einloggen. Ansonsten soll er sich registrieren.
 * Danach wird jedes mal nur die UnlockActivity angezeigt, wenn die App gestartet wird, sodass man sein PW
 * eingeben muss, um die App zu entsperren.
 * 
 *  - Neue Berechtigung (Verbindung mit Parse.com) hinzugefuegt 
 *  - ParseApplication als Application hinzugefuegt
 *  - Parse-SDK in die Private Bibliothek hinzugefueht
 */

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "5nc8A9LxUj02dtPMnULZAI6brOdhARiaaW4hfD84", "tJ6m7z3qkZKGDFlAA4QUm0QNXScc3EAUAnknD2W4");
	}

}

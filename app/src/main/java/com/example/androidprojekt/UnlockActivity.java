package com.example.androidprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseUser;

/**
 * @author boghyon
 *         Um die Sicherheit zu gewaehrleisten, wird die UnlockActivity jedesmal angezeigt, wenn die App gestartet wurde, oder ein anderes
 *         App waehrend der Laufzeit gestartet wurde. Vorherige Activities werden von der History geloescht, damit man nicht schummeln kann,
 *         mit der Zuruecktaste wieder auf die MainActivity zu gelangen.
 *
 */
public class UnlockActivity extends Activity {
	protected TextView mForgotPW;
	protected EditText mPassword;
	protected ImageButton mUnlockButton;
	protected ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlock);

		mProgressBar = (ProgressBar) findViewById(R.id.progressUnlock);
		mProgressBar.setVisibility(View.INVISIBLE);

		// UNLOCK BUTTON geklickt

		mPassword = (EditText) findViewById(R.id.passwordField);
		mUnlockButton = (ImageButton) findViewById(R.id.loginButton);
		mUnlockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String password = mPassword.getText().toString().trim();
				String username = password;
				/*
				 * getText() gibt Editable zurueck. deshalb toString().
				 * Methode trim() gibt String ohne Leerzeichen zurueck,
				 * damit wir wissen, ob das Feld wirklich leer ist.
				 */

				if (password.isEmpty()) {
					// Ist ein Feld leer, wird ein Dialogfenster angezeigt, mit der Error-Nachricht.
					AlertDialog.Builder builder = new AlertDialog.Builder(UnlockActivity.this);
					builder.setTitle(R.string.error_title).setMessage(R.string.login_error_message)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					// Unlock! (eigentlich "Login")
					mProgressBar.setVisibility(View.VISIBLE);// Lade-Kreis anzeigen lassen
					ParseUser.logInInBackground(username, password, new LogInCallback() {
						@Override
						public void done(ParseUser user, com.parse.ParseException e) {
							setProgressBarIndeterminateVisibility(false); // Antwort bekommen. Lade-Kreis ausblenden.
							if (e == null // Keine Exceptions zurueckbekommen (z.B. durch falsches Passwort)
									&& !password.equals(getSharedPreferences("Einstellungen", MODE_PRIVATE).getString(
											SignUpActivity.PASSWORD, ""))) { // und kein anderes im Parse.com vorhandenes Passwort benutzt.
								Intent intent = new Intent(UnlockActivity.this, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(UnlockActivity.this);
								builder.setTitle(R.string.error_title).setMessage(e.getMessage())
										.setPositiveButton(android.R.string.ok, new OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												mProgressBar.setVisibility(View.INVISIBLE);
												mPassword.setText("");
											}
										});
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						}
					});
				}
			}
		});

		// FORGOT PASSWORD geklickt

		mForgotPW = (TextView) findViewById(R.id.forgotPWText);
		mForgotPW.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UnlockActivity.this, ForgotPWActivity.class);
				startActivity(intent);
			}
		});
	}

	public static void navigateToUnlock(Activity activity) {
		ParseUser.logOut();
		Intent intent = new Intent(activity, UnlockActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		activity.startActivity(intent);
	}
}

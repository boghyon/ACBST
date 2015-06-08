package com.example.androidprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * @author boghyon
 *         SignUpActivity wird nur angezeigt, wenn der User die App nach der Installation zum Ersten Mal aufruft, um sein Initialpasswort
 *         festzulegen.
 *         Danach wird nur noch die UnlockActivity angezeigt, wenn man die App startet.
 *         Um nochmal auf die SignUpActivity zu gelangen, muss die App neu installiert werden.
 */
public class SignUpActivity extends Activity {

	protected EditText mPassword;
	protected EditText mEmail;
	protected TextView mAlready; // "Already hava a password"
	protected Button mSignUpButton;
	public static final String PASSWORD = "password";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		/*
		 * ACHTUNG: Die obere Zeile muss auf jeden Fall vor "setContentView(..)" kommen! Sonst CRASH!!
		 * wird hier gebraucht, um den Ladekreis anzeigen zu koennen, waehrend man auf die Antwort vom Backend wartet.
		 */
		setContentView(R.layout.activity_sign_up);
		

		// WELCOME MESSAGE - erscheint nur einmal (falls der user auf "I have already a PW" klickt und wieder zurueck navigiert).
		boolean welcome = getSharedPreferences("Einstellungen", MODE_PRIVATE).getBoolean("welcome", true);
		if (welcome) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.welcome_title, getString(R.string.app_name))).setMessage(R.string.setUpInitialPW_message)
					.setPositiveButton(android.R.string.ok, null);
			builder.create().show();
			getSharedPreferences("Einstellungen", MODE_PRIVATE).edit().putBoolean("welcome", false).commit();
		}

		mPassword = (EditText) findViewById(R.id.passwordField);
		mEmail = (EditText) findViewById(R.id.emailEditText);
		mSignUpButton = (Button) findViewById(R.id.signUpButton);
		mSignUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				 * getText() gibt Editable zurueck. deshalb toString().
				 * Methode trim() gibt String ohne Leerzeichen zurueck,
				 * damit wir wissen, ob das Feld wirklich leer ist.
				 */
				String password = mPassword.getText().toString().trim();
				String email = mEmail.getText().toString().trim();
				String username = password;
				getSharedPreferences("Einstellungen", MODE_PRIVATE).edit().putString(PASSWORD, password).commit();

				if (password.isEmpty() || email.isEmpty()) {
					// Dialog anzeigen, dass das Feld leer ist.
					AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
					builder.setTitle(R.string.error_title).setMessage(R.string.sign_up_error_message)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					// Einen neuen Benutzer erstellen! (InitialPW festlegen)
					setProgressBarIndeterminateVisibility(true); // Warten auf Antwort. Ladekreis anzeigen lassen
					ParseUser newUser = new ParseUser();
					newUser.setUsername(username);
					newUser.setPassword(password);
					newUser.setEmail(email);
					newUser.signUpInBackground(new SignUpCallback() { // Callback gibt das Feedback vom Backend zurueck.
						@Override
						public void done(com.parse.ParseException e) {
							setProgressBarIndeterminateVisibility(false); // Antwort bekommen. Ladekreis ausblenden
							if (e == null) {
								Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								// Damit man nicht mit der Zuruecktaste wieder auf die SignUpActivity kommen kann.
								startActivity(intent);
							} else {
								// Dialog: SignUp hat nicht funktioniert!
								AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
								builder.setTitle(R.string.error_title).setMessage(e.getMessage()) // Error-Nachricht von Parse.com
										.setPositiveButton(android.R.string.ok, null);
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						}
					});
				}
			}

		});

		mAlready = (TextView) findViewById(R.id.alreadyText);
		mAlready.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SignUpActivity.this, UnlockActivity.class);
				startActivity(intent);
			}
		});
	}
}

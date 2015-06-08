package com.example.androidprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

/**
 * @author boghyon
 *         ForgotPWActivity wird angezeigt, wenn der User in der UnlockActivity das Textfeld "Forgot Password" geklickt hat. Der User
 *         muss seine Email-Adresse eingeben, damit er den Link von Parse.com bekommt, wo er sein PW zuruecksetzen kann. Der User wird
 *         danach zur UnlockActivity gefuehrt.
 *
 */
public class ForgotPWActivity extends Activity {

	protected EditText mEmail;
	protected Button mResetButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_forgot_pw);

		// zum Testen
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();

		mEmail = (EditText) findViewById(R.id.forgotPWText);
		mResetButton = (Button) findViewById(R.id.reset_password_button);
		mResetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = mEmail.getText().toString().trim();
				if (email.isEmpty()) { // andere Fehler-Variationen mit entspr. Nachricht wird von Parse.com zur Verfuegung gestellt.
					AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPWActivity.this);
					builder.setTitle(R.string.error_title).setMessage(R.string.sign_up_error_message)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					setProgressBarIndeterminateVisibility(true); // Lade-Kreis anzeigen
					ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
						@Override
						public void done(com.parse.ParseException e) {
							setProgressBarIndeterminateVisibility(false); // Antwort bekommen. Lade-Kreis ausblenden.
							if (e == null) {
								Toast.makeText(ForgotPWActivity.this, "email verschickt!", Toast.LENGTH_SHORT).show(); // Bestaetigung
								Intent intent = new Intent(ForgotPWActivity.this, UnlockActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPWActivity.this);
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
	}

	// OptionsMenu wird nicht benutzt.
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.forgot_pw, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// // Handle action bar item clicks here. The action bar will
	// // automatically handle clicks on the Home/Up button, so long
	// // as you specify a parent activity in AndroidManifest.xml.
	// int id = item.getItemId();
	// if (id == R.id.action_settings) {
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }
}

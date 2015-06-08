package com.example.androidprojekt;

import java.io.File;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StatisticsActivity extends FragmentActivity {

	HistoryOpenHandler helper_db;
	Calendar calFrom;
	Calendar calTo;
	Button mStatisticFromButton;
	Button mStatisticToButton;
	Button mStatisticGoButton;
	ImageView fisher;
	ImageView fished;

	int mCounted;
	Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_statistic);

		mStatisticFromButton = (Button) findViewById(R.id.bStatistikFrom);
		mStatisticFromButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment(); // innere Klasse (s. unten)
				newFragment.show(getSupportFragmentManager(), "from");
			}
		});

		mStatisticToButton = (Button) findViewById(R.id.bStatistikTo);
		mStatisticToButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment(); // innere Klasse (s. unten)
				newFragment.show(getSupportFragmentManager(), "to");
			}
		});

		mStatisticGoButton = (Button) findViewById(R.id.bStatistikShow);
		mStatisticGoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (calFrom != null && calTo != null)
					if (calFrom.compareTo(calTo) == -1 || calFrom.compareTo(calTo) == 0) {
						fisher.setImageResource(R.drawable.fisher_fished);
						countDoneNotes();
						fished.setVisibility(View.VISIBLE);
						if (!mStatisticGoButton.getText().toString().endsWith("again"))
							mStatisticGoButton.setText(mStatisticGoButton.getText().toString() + " again");
					} else
						Toast.makeText(getApplicationContext(), "\"A wise man chooses to think first.\"", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(StatisticsActivity.this, R.string.statistics_error_pick_dates_first, Toast.LENGTH_LONG).show();
			}
		});

		fisher = (ImageView) findViewById(R.id.fisher);
		fished = (ImageView) findViewById(R.id.fished);
		fished.setVisibility(View.GONE);

		fisher.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(StatisticsActivity.this, "He doesn't like your finger.", Toast.LENGTH_SHORT).show();

			}
		});

		helper_db = new HistoryOpenHandler(this);

	}

	// Innere Klasse zum Anzeigen des Datumauswahldialogs
	public class DatePickerFragment extends DialogFragment implements OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			fisher.setImageResource(R.drawable.fisher_fishing);
			fished.setVisibility(View.GONE);
			mStatisticGoButton.setText("Ask");

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			if (getSupportFragmentManager().findFragmentByTag("from") != null) {
				calFrom = setCalendar(year, monthOfYear, dayOfMonth);
				mStatisticFromButton.setText(dayOfMonth + "." + (++monthOfYear) + "." + year);
			}

			if (getSupportFragmentManager().findFragmentByTag("to") != null) {
				calTo = setCalendar(year, monthOfYear, dayOfMonth);
				mStatisticToButton.setText(dayOfMonth + "." + (++monthOfYear) + "." + year);
			}
		}
	}

	private Calendar setCalendar(int year, int monthOfYear, int dayOfMonth) {

		Calendar cal = Calendar.getInstance();
		cal.set(year, monthOfYear, dayOfMonth);
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));

		return cal;
	}

	public void countDoneNotes() {
		mCounted = countDone(null, null);

		if (mCounted == 0) {
			// Toast.makeText(this, "\"I found nothing.\"", Toast.LENGTH_LONG).show();
			mToast = Toast.makeText(this, "\"There was nothing.\"", Toast.LENGTH_LONG);
			fished.setImageResource(R.drawable.fished_nothing_important);
		} else if (mCounted <= 10) {
			mToast = Toast.makeText(this, "\"I found a starving fish.\"\n" + mCounted + " tasks found.", Toast.LENGTH_LONG);
			fished.setImageResource(R.drawable.fished_starving);
		} else if (mCounted <= 25) {
			mToast = Toast.makeText(this, "\"I found a well-fed fish.\"\n" + mCounted + " tasks found.", Toast.LENGTH_LONG);
			fished.setImageResource(R.drawable.fished_well_fed);
		} else if (mCounted > 25 && mCounted < 500) {
			mToast = Toast.makeText(this, "\"Well done! I found a giant golden fish!\"\n" + mCounted + " tasks found.", Toast.LENGTH_LONG);
			fished.setImageResource(R.drawable.fished_giant);
		} else {
			mToast = Toast.makeText(this, mCounted
					+ " tasks found\n\"\"I found your moth...\"\n(She's so big, she doens't fit into your screen.)", Toast.LENGTH_LONG);
			fished.setVisibility(View.INVISIBLE);
		}

		// Toast Text zentrieren
		TextView v = (TextView) mToast.getView().findViewById(android.R.id.message);
		if (v != null)
			v.setGravity(Gravity.CENTER);
		mToast.show();
	}

	private int countDone(String whereClause, String[] whereArgs) {

		File database = getApplicationContext().getDatabasePath("history.db");
		int counter = 0;

		if (database.exists()) {

			SQLiteDatabase db = helper_db.getWritableDatabase();

			Cursor c = db.query(HistoryOpenHandler.TABLE_NAME, new String[] { HistoryOpenHandler.COLUMN_NOTE_ID,
					HistoryOpenHandler.COLUMN_TITLE, HistoryOpenHandler.COLUMN_DESCRIPTION, HistoryOpenHandler.COLUMN_ICON_ID,
					HistoryOpenHandler.COLUMN_DAY, HistoryOpenHandler.COLUMN_MONTH, HistoryOpenHandler.COLUMN_YEAR }, whereClause, // select
					whereArgs, // selection args
					null, // group by
					null, // having
					null); // order

			Calendar cTest = Calendar.getInstance();
			cTest.set(Calendar.HOUR_OF_DAY, cTest.getActualMinimum(Calendar.HOUR_OF_DAY));
			cTest.set(Calendar.MINUTE, cTest.getActualMinimum(Calendar.MINUTE));
			cTest.set(Calendar.SECOND, cTest.getActualMinimum(Calendar.SECOND));
			cTest.set(Calendar.MILLISECOND, cTest.getActualMinimum(Calendar.MILLISECOND));

			if (c.moveToFirst()) {
				do {
					cTest.set(Integer.valueOf(c.getString(6)), Integer.valueOf(c.getString(5)), Integer.valueOf(c.getString(4)));

					if ((cTest.after(calFrom) && cTest.before(calTo)) || (cTest.compareTo(calTo) == 0 || cTest.compareTo(calFrom) == 0))
						counter++;

				} while (c.moveToNext());
			}
			c.moveToFirst();
			helper_db.close();
		}
		return counter;
	}

}

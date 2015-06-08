package com.example.androidprojekt;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

	Activity currentActivity;
	static String[] prioritaeten = { "Normal", "Important", "Matter of life and death" };

	public CustomSpinnerAdapter(Context context) {
		super(context, R.id.prioTextView, prioritaeten);
		currentActivity = (Activity) context;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// currentActivity.findViewById(R.id.spinnerCustomLayout).setBackgroundResource(android.R.color.transparent);
//		Toast.makeText(currentActivity, R.string.priority, Toast.LENGTH_SHORT).show();
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = currentActivity.getLayoutInflater();
		View row = inflater.inflate(R.layout.custom_spinner, parent, false);
		TextView label = (TextView) row.findViewById(R.id.prioTextView);
		label.setText(prioritaeten[position]);

		ImageView icon = (ImageView) row.findViewById(R.id.icon);
		if (prioritaeten[position].equals("Normal")) {
			icon.setImageResource(R.drawable.normal);
		} else if (prioritaeten[position].equals("Important")) {
			icon.setImageResource(R.drawable.wichtig);
		} else
			icon.setImageResource(R.drawable.sehr_wichtig);

		return row;
	}
}

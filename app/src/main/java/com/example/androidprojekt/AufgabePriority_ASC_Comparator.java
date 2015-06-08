package com.example.androidprojekt;

import java.util.Comparator;

public class AufgabePriority_ASC_Comparator implements Comparator<Aufgabe>{

	// Aufsteigend
	
	@Override
	public int compare(Aufgabe first, Aufgabe second) {
		if(first.getPrioritaetIconId() > second.getPrioritaetIconId()){
			return 1;
		}
		else if (first.getPrioritaetIconId() < second.getPrioritaetIconId()){
			return -1;
		}
		else 
			return first.getDatum().compareTo(second.getDatum());
	}
}

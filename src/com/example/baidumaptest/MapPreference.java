package com.example.baidumaptest;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class MapPreference extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName("appPreferences");
		
		addPreferencesFromResource(R.xml.mappreference);
	}
	
}

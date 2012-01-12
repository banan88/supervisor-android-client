package org.supervisor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	private ListPreference lpref;
	private EditTextPreference pref;
	public static final String sync_pref_key = "sync";
	public static final String server_pref_key = "server";
	public static final String username_pref_key = "username";
	public static final String password_pref_key = "password";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		lpref = (ListPreference)findPreference(sync_pref_key);
		lpref.setSummary(lpref.getEntry());
		
		pref = (EditTextPreference) findPreference(server_pref_key);
		Log.d("test", pref.toString());
		if (pref.getText() != null && !pref.getText().trim().equals(""))
			pref.setSummary(pref.getText());
		
		pref = (EditTextPreference) findPreference(username_pref_key);
		if (pref.getText() != null && !pref.getText().trim().equals(""))
			pref.setSummary(pref.getText());
		
		pref = (EditTextPreference) findPreference(password_pref_key);
		if (pref.getText() != null && !pref.getText().trim().equals(""))
			pref.setSummary(null);
	}
	
	
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (key.equals(sync_pref_key)) {
			lpref = (ListPreference) findPreference(key);
			lpref.setSummary(lpref.getEntry());
		}
		
		else if (key.equals(server_pref_key)) {
			pref = (EditTextPreference) findPreference(key);
			if(!pref.getText().trim().equals(""))
				pref.setSummary(pref.getText());
		}
		
		else if (key.equals(username_pref_key) ) {
			pref = (EditTextPreference) findPreference(key);
			if(!pref.getText().trim().equals(""))
				pref.setSummary(pref.getText());
		}
		
		else if (key.equals(password_pref_key) ) {
			pref = (EditTextPreference) findPreference(key);
			if(!pref.getText().trim().equals(""))
				pref.setSummary(null);
		}
		
	}


	
	
	
	
}

package org.supervisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	private ListPreference lpref;
	private EditTextPreference pref;
	public static final String sync_pref_key = "sync";
	public static final String server_pref_key = "server";
	public static final String username_pref_key = "username";
	public static final String password_pref_key = "password";
	public static final String TAG = PreferencesActivity.class.getSimpleName();
	private SupervisorApplication global_app;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.preferences_menu_screen_title);
		global_app = (SupervisorApplication) getApplication();
		addPreferencesFromResource(R.xml.preferences);
		lpref = (ListPreference)findPreference(sync_pref_key);
		lpref.setSummary(lpref.getEntry());
		
		pref = (EditTextPreference) findPreference(server_pref_key);
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
		lpref = (ListPreference) findPreference(sync_pref_key);
		if (key.equals(sync_pref_key)) {
			lpref.setSummary(lpref.getEntry());
			if(lpref.getValue().equals("5") || ((SupervisorApplication) getApplication()).isNetworkOn() == false)
				stopService(new Intent(this, SynchronisationService.class));
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
		if (!lpref.getValue().equals("5") && 
				(global_app.isNetworkOn() == true) && //to do: launch service only if all data are set.
				(global_app.getUsername()!=null) && (global_app.getServerURL()!=null) && (global_app.getPassword()!=null))
			startService(new Intent(this, SynchronisationService.class));
	}


	
	
	
	
}

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
		Log.d(TAG, "sharedPreferenceChanged() called: " + key);
		
		// setting hints for user:
		
		lpref = (ListPreference) findPreference(sync_pref_key);
		if (key.equals(sync_pref_key)) {
			lpref.setSummary(lpref.getEntry());	
			if( allDataSupplied() )
				global_app.reloadAlarm();
		}
		
		else if (key.equals(server_pref_key)) {
			pref = (EditTextPreference) findPreference(key);
			if(!pref.getText().trim().equals(""))
				pref.setSummary(pref.getText());
			else
				pref.setSummary(R.string.server_url_hint);
			if( allDataSupplied() )
				global_app.reloadAlarm();
		}
		
		else if (key.equals(username_pref_key) ) {
			pref = (EditTextPreference) findPreference(key);
			if(!pref.getText().trim().equals(""))
				pref.setSummary(pref.getText());
			if( allDataSupplied() )
				global_app.reloadAlarm();
		}
		
		else if (key.equals(password_pref_key) ) {
			pref = (EditTextPreference) findPreference(key);
			if(!pref.getText().trim().equals(""))
				pref.setSummary(null);
			if( allDataSupplied() )
				global_app.reloadAlarm();
		}
		
	}
	
	private boolean allDataSupplied() {
		//if credentials & server are supplied AND sync period changed - reload scheduled alarm of alarmmanager
		return ((global_app.getUsername()!=null) && (global_app.getServerURL()!=null) && (global_app.getPassword()!=null));
	}
	
}

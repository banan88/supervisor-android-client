package org.supervisor;

import java.util.ArrayList;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

public class SupervisorApplication extends Application {
	
	private static final String TAG = SupervisorApplication.class.getSimpleName();
	private SharedPreferences prefs;
	private DataStorage dataStorage;
	private NotificationManager mgr;
	public static final String UPDATE_VIEW_INTENT = "org.supervisor.UPDATE_VIEW";
	
	public void onCreate() {
		super.onCreate();	
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		dataStorage = new DataStorage(this);	
		mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	synchronized public DataStorage getDataStorage() {
		return dataStorage;
	}
	
	synchronized public String getUsername() {
		return prefs.getString(PreferencesActivity.username_pref_key, null);
	}
	
	synchronized public String getPassword() {
		return prefs.getString(PreferencesActivity.password_pref_key, null);
	}
	
	synchronized public String getServerURL() {
		String url = prefs.getString(PreferencesActivity.server_pref_key, null);
		if (url == null || url.trim().equals(""))
			return null;
		else
			return "http://" + url +"/";
	}
	
	synchronized public String getSyncPeriod() {
		return prefs.getString(PreferencesActivity.sync_pref_key, null);
	}
	
	public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	synchronized public boolean isNetworkOn() {
		NetworkInfo networkInfo = ((ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return !(networkInfo == null || !networkInfo.isConnected());
	}
	
	public void setLastSyncTimeToNow(boolean wasSuccessful) {
		Editor preferenceEditor = prefs.edit();
		Time now = new Time();
		now.setToNow();
		preferenceEditor.putLong("DATETIME", now.toMillis(true));
		preferenceEditor.putBoolean("SUCCESS", wasSuccessful);
		preferenceEditor.commit();
	}
	
	public Long getLastSyncTime() {
		return prefs.getLong("DATETIME", 0);
	}
	
	public boolean wasLastSyncSuccessful() {
		return prefs.getBoolean("SUCCESS", false);
	}
	
	public int getLastSearchFilter() {
		return prefs.getInt("FILTER", 0);
	}
	
	public void setLastSearchFilter(int filter) {
		Editor preferenceEditor = prefs.edit();
		preferenceEditor.putInt("FILTER", filter);
		preferenceEditor.commit();
	}
	
	
	public void generateNotification(String[] content, int icon, long timeout, boolean isOngoing, Intent startActivity) {

		Notification not = new Notification(icon, content[0], System.currentTimeMillis());
		PendingIntent pi = PendingIntent.getActivity(this, 0, startActivity, 0);
		not.setLatestEventInfo(this, content[1], content[2], pi);
		not.flags |= Notification.FLAG_AUTO_CANCEL; 
		mgr.notify(1, not);
		if(!isOngoing) {
			final long tmp = timeout;
				new Thread() {
		
					public void run() {
						try {
							sleep(tmp);
						} catch (InterruptedException e) {
							mgr.cancel(1);
						}
						mgr.cancel(1);
					}
				}.run();
		}
	}
	
	public synchronized int insertTaskUpdates(ArrayList<Task> tasks) {
		
		if(tasks == null || tasks.size() == 0)
			return 0;
		
		ContentValues values = new ContentValues();
		for (Task task : tasks) {
			Log.d(TAG, "attempting to insert into db");
			values.put(DataStorage.C_ID, task.getId());
			values.put(DataStorage.C_NAME, task.getName());
			values.put(DataStorage.C_DESC, task.getDescription());
			values.put(DataStorage.C_LAT, task.getLatitude());
			values.put(DataStorage.C_LON, task.getLongitude());
			values.put(DataStorage.C_STATE, task.getState());
			values.put(DataStorage.C_CREATION_TIME, task.getCreationTime());
			values.put(DataStorage.C_LAST_MODIFIED, task.getLastModified());
			values.put(DataStorage.C_FINISH_TIME, task.getFinishTime());
			values.put(DataStorage.C_START_TIME, task.getStartTime());
			values.put(DataStorage.C_VERSION, task.getVersion());
			values.put(DataStorage.C_LAST_SYNC, task.getLastSynced());
			dataStorage.insert(values);		
		}
		dataStorage.close();
		return tasks.size();
	}		
}


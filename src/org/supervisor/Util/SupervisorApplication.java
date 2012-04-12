package org.supervisor.Util;

import java.util.ArrayList;

import org.supervisor.R;
import org.supervisor.Activities.MainScreenActivity;
import org.supervisor.Activities.PreferencesActivity;
import org.supervisor.Activities.TasksActivity;


import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

public class SupervisorApplication extends Application {
	
	private static final String TAG = SupervisorApplication.class.getSimpleName();
	private SharedPreferences prefs;
	private DataStorage dataStorage;
	private NotificationManager mgr;
	public static final String UPDATE_VIEW_INTENT = "org.supervisor.UPDATE_VIEW";
	public static final int CREDENTIALS_ERR = 401;
	public static final int SERVER_ERR = 404;
	public static final int SERVER_ERR_500 = 500;
	public static final int SYNC_START = 0;
	public static final int SYNC_COUNT = 200;
	private Location lastLocation;
	private LocationUpdateRequester locationUpdateRequester;
	private LocationManager locationManager;
	private AlarmManager alarmMgr;
	
	
	public void onCreate() {
		super.onCreate();	
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		dataStorage = new DataStorage(this);	
		mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		initLocationUpdates();
	}
	
	
	public void reloadAlarm() {
		Log.d(TAG, "reloadAlarm() begins");
		alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent =
            PendingIntent.getBroadcast(this, 0, new Intent(this, SyncRequestReceiver.class), 0);
		alarmMgr.cancel(pendingIntent);
		Long interval = 0L;
		if(!getSyncPeriod().equals("5")) {
			/* 0 - 5min  300000
			 * 1 - 10min 600000
			 * 2 - 15 min 900000
			 * 3 - 30min 1800000
			 * 4 - 60min 3600000
			 * 5 - off
			 * 6 - 10s 10000
			 */
			
			switch (Integer.parseInt(getSyncPeriod())){
				case 0:
					interval = 300000L;
					break;
				case 1:
					interval = 600000L;
					break;
				case 2:
					interval = 900000L;
					break;
				case 3:
					interval = 1800000L;
					break;
				case 4:
					interval = 3600000L;
					break;
				case 6:
					interval = 10000L;
					break;
				
			}	 Log.d(TAG, "getSyncPeriod: " + getSyncPeriod());
			
		    // use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
		    alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,  SystemClock.elapsedRealtime(), interval
	               , pendingIntent);
		}
		Log.d(TAG, "reloadAlarm() interval= " + Long.toString(interval));
	}
	
	
	public DataStorage getDataStorage() {
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
	
	public boolean wasPromptedForGPSEnabling() {
		return prefs.getBoolean("GPS_PROMPT", false);
	}
	
	public void setAlreadyPromptedForGPSEnabling() {
		Editor preferenceEditor = prefs.edit();
		preferenceEditor.putBoolean("GPS_PROMPT", true);
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
	
	
	public void cancelSyncNotification() {
		mgr.cancel(SYNC_START);
		Log.d(TAG, "called cancelSyncNotification");
	}
	
	
	public void generateNotificationSync() {
		Notification not = new Notification(R.drawable.ic_menu_refresh, 
				getString(R.string.sync_notification_body), 
				System.currentTimeMillis());
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainScreenActivity.class), 0);
		not.setLatestEventInfo(this, getString(R.string.sync_notification_title), 
				getString(R.string.sync_notification_body), pi);
		mgr.notify(SYNC_START, not);
	}
	
	
	public void generateNotificationChanges(int count) {
		Notification not = new Notification(R.drawable.ic_menu_refresh, 
				getString(R.string.sync_notification_changes) + " " + Integer.toString(count),
				System.currentTimeMillis());
		not.flags |= Notification.FLAG_AUTO_CANCEL;
		not.flags |= Notification.DEFAULT_SOUND;
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, TasksActivity.class), 0);
		not.setLatestEventInfo(this, getString(R.string.sync_notification_title), 
				getString(R.string.sync_notification_changes) + " " + Integer.toString(count) , pi);
		mgr.notify(SYNC_START, not);
	}
	
	
	public void generateNotificationError401() {
		Notification not = new Notification(R.drawable.ic_menu_refresh, 
				getString(R.string.sync_notification_body_err),
				System.currentTimeMillis());
		not.flags |= Notification.FLAG_AUTO_CANCEL; 
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PreferencesActivity.class), 0);
		not.setLatestEventInfo(this, getString(R.string.sync_notification_title), 
				getString(R.string.sync_notification_body_err), pi);
		mgr.notify(CREDENTIALS_ERR, not);
	}
	
	
	public void generateNotificationError404() {
		Notification not = new Notification(R.drawable.ic_menu_refresh, 
				getString(R.string.sync_notification_body_ser_err),
				System.currentTimeMillis());
		not.flags |= Notification.FLAG_AUTO_CANCEL; 
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PreferencesActivity.class), 0);
		not.setLatestEventInfo(this, getString(R.string.sync_notification_title), 
				getString(R.string.sync_notification_body_ser_err), pi);
		mgr.notify(SERVER_ERR, not);
	}
	
	
	public void generateNotificationError500() {
		Notification not = new Notification(R.drawable.ic_menu_refresh, 
				getString(R.string.sync_notification_body_ser_err_500),
				System.currentTimeMillis());
		not.flags |= Notification.FLAG_AUTO_CANCEL; 
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PreferencesActivity.class), 0);
		not.setLatestEventInfo(this, getString(R.string.sync_notification_title), 
				getString(R.string.sync_notification_body_ser_err_500), pi);
		mgr.notify(SERVER_ERR_500, not);
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
			values.put(DataStorage.C_SUPERVISOR, task.getSupervisor());
			dataStorage.insertTaskUpdates(values);		
		}
		dataStorage.close();
		return tasks.size();
	}		
	
	
	public boolean checkGPS() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	
	public Location getLastLocation() {
		if (lastLocation == null) {
			GeolocationHelper gh = new GeolocationHelper(this);
			lastLocation = gh.getLastBestLocation(50, 10000);
		}
		Log.d(TAG, "last location called");
		return lastLocation;
	}
	
	public void setLastLocation(Location newLocation) {
		lastLocation = new Location(newLocation);
	}
	
	public void initLocationUpdates() {
		locationUpdateRequester = new LocationUpdateRequester(locationManager, alarmMgr);
		Intent activeIntent = new Intent(this, LocationChangedReceiver.class);
		PendingIntent locationUpdates = PendingIntent.getBroadcast(this, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		locationUpdateRequester.requestLocationUpdates(1000, 0, new Criteria(), locationUpdates);
	}

}


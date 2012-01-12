package org.supervisor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.accounts.NetworkErrorException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;




public class SynchronisationService extends Service {

	private static final String TAG = SynchronisationService.class.getSimpleName();
	private SynchronisationThread thread;
	private long SYNC_DELAY = 360000; //6min
	private long NOTIFICATION_CANCEL_DELAY = 1000; //1s after GET
	private int NOTIFICATION_ID = 1;
	private NotificationManager mgr;
	private DataStorage dataStorage;
	private SharedPreferences prefs;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		thread = new SynchronisationThread();
		Log.d(TAG, "onCreate() called");
		dataStorage = new DataStorage(this);
	}
	
	public void onDestroy() {
		super.onDestroy();
		thread.interrupt();
		thread = null;
		Log.d(TAG, "onDestroy() called");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(thread.isAlive()) { //forced synchronisation
			thread.interrupt();
			thread = new SynchronisationThread();
		} 
			thread.start();
		Log.d(TAG, "onStartCommand() called");
		return START_STICKY;
	}


	private class SynchronisationThread extends Thread {
		
		final String TAG = SynchronisationThread.class.getSimpleName();
		
		public void run() {
			Log.d(TAG, "run");
			try{
				while(true){
						
						prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						String username = prefs.getString(PreferencesActivity.username_pref_key, null);
						String password = prefs.getString(PreferencesActivity.password_pref_key, null);
						URL server = null;
						ApiManager.setCredentials(username, password);
						String adress = "http://" + prefs.getString(PreferencesActivity.server_pref_key, null) +"/";
						try {
							new URL(adress);
						} catch (MalformedURLException e) {
							Log.d(TAG, adress);
							Log.d(TAG, e.getMessage());
						}
						Log.d(TAG,adress);
						ApiManager.HOST = adress;
						//http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/RemoteService.html
							
						String ns = Context.NOTIFICATION_SERVICE;
						mgr = (NotificationManager) getSystemService(ns);
						int icon = R.drawable.ic_menu_refresh;
									
						String status_text = getString(R.string.sync_status_bar_txt);
						String text = getString(R.string.sync_notification_body);
						String title = getString(R.string.sync_notification_title);
						ArrayList<Task> tasks = null;
						Intent intent = null;
						boolean request_ok = true;
							
						try {
							try {
								tasks = ApiManager.getTasks(); //actual rest call
							} catch (IllegalArgumentException e) {
								throw new NetworkErrorException("404");
							}
								
						} catch (NetworkErrorException e) {
							Log.d(TAG, e.getMessage());
							intent = new Intent(getApplicationContext(), PreferencesActivity.class);
							status_text = getString(R.string.sync_status_bar_txt_error);
							if (e.getMessage().equals("404"))
								text = getString(R.string.sync_notification_body_ser_err);
							else
								text = getString(R.string.sync_notification_body_err);
							request_ok = false;
						}
							
						Notification not = new Notification(icon, status_text, System.currentTimeMillis());
						not.flags |= Notification.FLAG_AUTO_CANCEL;
						PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
						not.setLatestEventInfo(getApplicationContext(), title,
								text, pi);
						mgr.notify(NOTIFICATION_ID, not);
							
						ContentValues values = new ContentValues();
						if (tasks != null)
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
						
						if(request_ok) {
							sleep(NOTIFICATION_CANCEL_DELAY);
							mgr.cancel(NOTIFICATION_ID);
						}	
					sleep(SYNC_DELAY);
				}
			} catch (InterruptedException e) {Log.d(TAG, "interrupted exception"); }
		}
		
	}

}

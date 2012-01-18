package org.supervisor;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;




public class SynchronisationService extends Service {

	private static final String TAG = SynchronisationService.class.getSimpleName();
	private SynchronisationThread thread;
	private String status_text;
	private String text;
	private String title;
	private boolean isRequestOk;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	public void onCreate(){
		super.onCreate();
		thread = new SynchronisationThread();
		Log.d(TAG, "onCreate() called");
	}
	
	
	public void onDestroy() {
		super.onDestroy();
		thread.interrupt();
		thread = null;
		Log.d(TAG, "SERVICE STOPPED : onDestroy() called");
	}

	
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(thread.isAlive()) { //forced synchronisation
			thread.interrupt();
			thread = new SynchronisationThread();
		} 
			thread.start();
		Log.d(TAG, "SERVICE STARTED : onStartCommand() called");
		return START_STICKY;
	}
	

	private class SynchronisationThread extends Thread {
		
		final String TAG = SynchronisationThread.class.getSimpleName();
		SupervisorApplication global_app = (SupervisorApplication) getApplication();
		public void run() {
			
			try{
				while(true){
					Long sync_delay = null;
					int sync_option = Integer.parseInt(global_app.getSyncPeriod());
					
					switch (sync_option) {
						case 0:
							sync_delay = new Long(300000);
							break;
						case 1:
							sync_delay = new Long(600000);
							break;
						case 2:
							sync_delay = new Long(900000);
							break;
						case 3:
							sync_delay = new Long(1800000);
							break;
						case 4:
							sync_delay = new Long(3600000);
							break;
						case 6:
							sync_delay = new Long(10000);
							break;
					}
					if( global_app.isNetworkOn() ) {
						Log.d(TAG, "Network on!");
						isRequestOk = true;
						status_text = getString(R.string.sync_status_bar_txt);
						text = getString(R.string.sync_notification_body);
						title = getString(R.string.sync_notification_title);
						ApiManager.setCredentials(global_app.getUsername(), global_app.getPassword());
						String adress = global_app.getServerURL();
						try {
							new URL(adress);
						} catch (MalformedURLException e) {
							Log.d(TAG, adress);
							Log.d(TAG, e.getMessage());
						}
						ApiManager.HOST = adress;
						int icon = R.drawable.ic_menu_refresh;
						
						ArrayList<Task> tasks = null;
						Intent intent = null;
							
						try {
							try {
								if(global_app.getDataStorage().isEmpty()) {
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.m");
									String week_ago = dateFormat.format(new Date(new Date().getTime() - 10*24*60*60*1000));
									tasks = ApiManager.getTasksSince(week_ago); 
								}
								else {
									tasks = ApiManager.getTasksSince(ApiManager.getLastSyncTime());
								}
							} catch (IllegalArgumentException e) {
								throw new NetworkErrorException(e.getMessage());
							}
								
						} catch (NetworkErrorException e) {					
							intent = new Intent(SynchronisationService.this, PreferencesActivity.class);
							status_text = getString(R.string.sync_status_bar_txt_error);
							if (e.getMessage().equals("401")) 
								text = getString(R.string.sync_notification_body_err);
							else 
								text = getString(R.string.sync_notification_body_ser_err);
							isRequestOk = false;
						}
						Log.d(TAG, Boolean.toString(isRequestOk));
						global_app.generateNotification(new String[]{status_text, title, text}, icon, 2000, !isRequestOk, intent);
						global_app.insertTaskUpdates(tasks);
							
					}
					try {
						sleep(sync_delay);
					} catch (NullPointerException e) {
						Log.d(TAG, e.getMessage());
					}
				}
			} catch (InterruptedException e) {
				Log.d(TAG, "interrupted exception");
			}
		}
		
	}

}

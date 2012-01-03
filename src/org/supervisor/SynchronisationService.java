package org.supervisor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;




public class SynchronisationService extends Service {

	private static final String TAG = SynchronisationService.class.getSimpleName();
	private SynchronisationThread thread;
	private long SYNC_DELAY = 360000; //6min
	private long NOTIFICATION_CANCEL_DELAY = 1000; //1s after GET
	private int NOTIFICATION_ID = 1;
	private NotificationManager mgr;
	
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
					
					
					
						
						String ns = Context.NOTIFICATION_SERVICE;
						mgr = (NotificationManager) getSystemService(ns);
						int icon = R.drawable.ic_menu_refresh;
						Notification not = new Notification(icon, getString(R.string.sync_status_bar_txt), System.currentTimeMillis());
						PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, null, 0);
						not.setLatestEventInfo(getApplicationContext(), getString(R.string.sync_notification_title),
								getString(R.string.sync_notification_body), pi);
						mgr.notify(NOTIFICATION_ID, not);
						
						ApiManager.setCredentials("robol2", "robol2");
						Log.d(TAG, ApiManager.getTasks(ApiManager.CURRENT_TASKS));
	

						sleep(NOTIFICATION_CANCEL_DELAY);
						mgr.cancel(NOTIFICATION_ID);
					
					sleep(SYNC_DELAY);
				}
			} catch (InterruptedException e) {Log.d(TAG, "interrupted exception");}
		}
		
	}

}

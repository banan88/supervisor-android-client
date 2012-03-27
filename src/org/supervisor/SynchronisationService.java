package org.supervisor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.accounts.NetworkErrorException;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;




public class SynchronisationService extends IntentService {

	private static final String TAG = SynchronisationService.class.getSimpleName();
	private DataStorage dataStorage;
	private SupervisorApplication global_app;
	
	
	public SynchronisationService() {
		super(TAG);
	}
	//.
	
	public void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent()");
		global_app = (SupervisorApplication) getApplication();
		if( global_app.isNetworkOn() ) {
			global_app.generateNotificationSync();
			dataStorage = global_app.getDataStorage();

			ApiManager.setCredentials(global_app.getUsername(), global_app.getPassword());
			String adress = global_app.getServerURL();
			try {
				new URL(adress);
			} catch (MalformedURLException e) {
				Log.d(TAG, adress + " : ");
				Log.d(TAG, e.getMessage() + " : ");
			}
			ApiManager.HOST = adress;
				
			ArrayList<Task> tasks = null;
				
			try {
				try {
					Cursor cursor;
					if(global_app.getTaskStatesTableVersion() < ApiManager.checkTaskStatesTableVersion()) {
						ArrayList<HashMap<String, Object>> updates = ApiManager.getTaskStatesUpdates();
						ContentValues cv = new ContentValues();
						HashMap<String, Object> singleState = new HashMap<String, Object>();
						for (int i = 0 ; i < updates.size(); ++i){
							singleState = updates.get(i);
							cv.put(DataStorage.C_STATE_DESCRIPTION, (String) singleState.get("state_description"));
							cv.put(DataStorage.C_IS_VISIBLE, (Boolean) singleState.get("is_displayed"));
							cv.put(DataStorage.C_TASKS_ARE_ARCHIVED, (Boolean) singleState.get("tasks_are_archived"));
							cv.put(DataStorage.C_CAN_BE_TOGGLED, (Boolean) singleState.get("can_be_toggled"));
							cv.put(DataStorage.C_TOGGLED_FROM, (Boolean) singleState.get("toggled_from"));
							dataStorage.insertTaskStatesTableUpdates(cv);
							Log.d(TAG, "inserted cvs!");
						}
					}
						ApiManager.getTaskStatesUpdates();
					if(global_app.getDataStorage().isEmpty()) 
						tasks = ApiManager.getNTasks(100);
					else {
						cursor = dataStorage.getNonSyncedTasks();
						if (ApiManager.changeTasksStates(cursor)) {
							dataStorage.clearNonSyncedTasks();
							Log.d(TAG, "clearNonSyncedTasks called");
						}
						cursor.close();
						tasks = ApiManager.getTasksSinceLastSync();
					}
					cursor = dataStorage.getNonSyncedWorkTimes();
					if (ApiManager.changeWorkTimes(cursor))
						dataStorage.clearNonSyncedWorkTimes();
					cursor.close();
					global_app.setLastSyncTimeToNow(true);
				} catch (IllegalArgumentException e) {
					Log.d(TAG, e.getMessage() + " illegal arg");
					throw new NetworkErrorException(e.getMessage());
				}
						
			} catch (NetworkErrorException e) {	
				Log.d(TAG, e.getMessage());
				if (e.getMessage().equals("401")) 
					global_app.generateNotificationError401();
				else if (e.getMessage().equals("404"))
					global_app.generateNotificationError404();
				else
					global_app.generateNotificationError500();
				global_app.setLastSyncTimeToNow(false);
			} finally {
				global_app.cancelSyncNotification();
			}
			int count = global_app.insertTaskUpdates(tasks);
			
			if (count != 0) {
				SynchronisationService.this.sendBroadcast(new Intent(SupervisorApplication.UPDATE_VIEW_INTENT));
				global_app.generateNotificationChanges(count);
			}	
		}
		WakeLocker.release();
	}
	//
	//change_work_times/ -- lokalnie na emulatorze dziala, na zdalnym serwerze jest 500 (unicode cos nei tak!?)

}

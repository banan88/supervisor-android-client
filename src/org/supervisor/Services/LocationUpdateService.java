package org.supervisor.Services;


import org.supervisor.Util.DataStorage;
import org.supervisor.Util.SupervisorApplication;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.text.format.Time;
import android.util.Log;

public class LocationUpdateService extends IntentService {

	private DataStorage dataStorage;
	private SupervisorApplication global_app;
	
	public LocationUpdateService() {
		super(LocationUpdateService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.d("locationUpdateService", "new location intent received;");
		global_app = (SupervisorApplication) getApplication();
		dataStorage = global_app.getDataStorage();
		Time t = new Time();
		t.setToNow();
		Location l = new Location((Location)intent.getExtras().get("newLocation"));
		dataStorage.insertUserPositionUpdate(t.toMillis(false), l.getLatitude(), l.getLongitude());
		sendBroadcast(new Intent(SupervisorApplication.UPDATE_VIEW_INTENT));
	}

}

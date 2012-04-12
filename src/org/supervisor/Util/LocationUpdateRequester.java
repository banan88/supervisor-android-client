package org.supervisor.Util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;

public class LocationUpdateRequester{

	protected AlarmManager alarmManager;
	protected LocationManager locationManager;
	  
	protected LocationUpdateRequester(LocationManager locationManager, AlarmManager alarmManager) {
		this.locationManager = locationManager;
		this.alarmManager = alarmManager;
	}

	  public void requestLocationUpdates(long minTime, long minDistance, Criteria criteria, PendingIntent pendingIntent) {
	    // Prior to Gingerbread we needed to find the best provider manually.
	    // Note that we aren't monitoring this provider to check if it becomes disabled - this is handled by the calling Activity.
	    String provider = locationManager.getBestProvider(criteria, true);
	    if (provider != null)
	      locationManager.requestLocationUpdates(provider, minTime, minDistance, pendingIntent);
	  }


	  public void requestPassiveLocationUpdates(long minTime, long minDistance, PendingIntent pendingIntent) {
	    // Pre-Froyo there was no Passive Location Provider, so instead we will set an inexact repeating, non-waking alarm
	    // that will trigger once the minimum time between passive updates has expired. This is potentially more expensive
	    // than simple passive alarms, however the Receiver will ensure we've transitioned beyond the minimum time and
	    // distance before initiating a background nearby loction update.
	    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis()+10000, 20000, pendingIntent);    
	  }
	}
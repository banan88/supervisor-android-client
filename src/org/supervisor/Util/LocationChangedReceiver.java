package org.supervisor.Util;

import org.supervisor.Services.LocationUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationChangedReceiver extends BroadcastReceiver {
	  
	  protected static String TAG = "LocationChangedReceiver";
	  private SupervisorApplication global_app;
	  
	  /**
	   * When a new location is received, extract it from the Intent and use
	   * it to start the Service used to update the list of nearby places.
	   * 
	   * This is the Active receiver, used to receive Location updates when 
	   * the Activity is visible. 
	   */
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    String locationKey = LocationManager.KEY_LOCATION_CHANGED;
	    
	    
	    if (intent.hasExtra(locationKey)) {
	      Location location = (Location)intent.getExtras().get(locationKey);
	      Log.d(TAG, "LocationChangedReceiver received location: " + Double.toString(location.getLatitude())
	    		  + " " + Double.toString(location.getLongitude()));
	      global_app = (SupervisorApplication) context.getApplicationContext();
	      global_app.setLastLocation(location);
	      Intent newLocation = new Intent(context, LocationUpdateService.class);
	      newLocation.putExtra("newLocation", location);
	      context.startService(newLocation);
	      Log.d(TAG, "LocationChangedReceiver started LocationUpdateService");
	    }
	  }
	}

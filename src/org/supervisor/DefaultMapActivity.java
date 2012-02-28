package org.supervisor;

import android.widget.LinearLayout;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;


public class DefaultMapActivity extends MapActivity {
	
	LinearLayout linearLayout;
	MapView mapView;
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	public void onCreate() {
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
	}
}

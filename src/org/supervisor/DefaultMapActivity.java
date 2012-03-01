package org.supervisor;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.widget.LinearLayout;



public class DefaultMapActivity extends MapActivity {
	
	LinearLayout linearLayout;
	MapView mapView;
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	public void onCreate() {
		mapView = (MapView) findViewById(R.id.map);
		mapView.setBuiltInZoomControls(true);
	}
}

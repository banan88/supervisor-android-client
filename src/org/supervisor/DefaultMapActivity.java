package org.supervisor;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;



public class DefaultMapActivity extends MapActivity implements LocationListener{

	
	private MapView mapView;
	private MapController mapController;
	private SupervisorApplication global_app;
	private GeoPoint geoPoint;
	private Cursor tasksCursor;
	
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {
		super.onPause();
	}



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_app = (SupervisorApplication) getApplication();
        
        setContentView(R.layout.layout_default_map);
        mapView = (MapView) findViewById(R.id.map);	
        mapView.setBuiltInZoomControls(false);
        mapView.setSatellite(false);
        
        mapController = mapView.getController();
        
        Location location = global_app.getLastLocation();
        int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		geoPoint = new GeoPoint(lat, lng);
		mapController.setCenter(geoPoint);
        mapController.setZoom(20);
    }
    
	protected void onResume() {
		super.onResume();
		setUp();
	}
	
    
    public void setUp() {
    	List<Overlay> overlays = mapView.getOverlays();
        overlays.clear();
        tasksCursor = global_app.getDataStorage().getActiveTasks();
        overlays.add(new MyOverlay());

        mapView.invalidate();
    }
    

    protected boolean isRouteDisplayed() { return false; }

	public void onLocationChanged(Location location) {
		int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		geoPoint = new GeoPoint(lat, lng);
		mapController.animateTo(geoPoint); // mapController.setCenter(point);
		mapView.invalidate();
	}

	public void onProviderDisabled(String provider) {}

	public void onProviderEnabled(String provider) {}

	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	private class MyOverlay extends com.google.android.maps.Overlay {

	    @Override
	    public void draw(Canvas canvas, MapView mapView, boolean shadow) {                              
	        super.draw(canvas, mapView, shadow);

	        if (!shadow) {                                                                             
	            Point pointGfx = new Point();
	            mapView.getProjection().toPixels(geoPoint, pointGfx);                                     

	            Bitmap bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_myplaces);   
	        
	            int x = pointGfx.x - bmp.getWidth() / 2;                                                  
	        
	            int y = pointGfx.y - bmp.getHeight();                                                     
	        
	            canvas.drawBitmap(bmp, x, y, null);      
	            
	            while(tasksCursor.moveToNext()) { // wonder if it will work...
	            	GeoPoint tmp = new Geo
	            	tasksCursor.getLong(tasksCursor.getColumnIndex(DataStorage.C_LAT));
	            }
	        }

	    }

	}
	
}


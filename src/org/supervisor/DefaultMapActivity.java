package org.supervisor;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;



public class DefaultMapActivity extends MapActivity implements LocationListener, OnClickListener{

	
	private static String TAG = DefaultMapActivity.class.getSimpleName();
	private Button searchButton;
	private Button logo;
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
		tasksCursor.close();
	}



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        global_app = (SupervisorApplication) getApplication();
        
        setContentView(R.layout.layout_default_map);
        searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
		
        mapView = (MapView) findViewById(R.id.map);	
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        
        mapController = mapView.getController();
        
        
    }
    
	protected void onResume() {
		super.onResume();
		setUp();
	}
	
    
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "menu item selected" + item.toString());
		switch(item.getItemId()) {
			case R.id.synchronise:
				Log.d(TAG, "synchronizacja");
				if ( global_app.isNetworkOn() )
					startService(new Intent(DefaultMapActivity.this, SynchronisationService.class));	
				else
					Toast.makeText(this, R.string.dialog_text_no_network, Toast.LENGTH_SHORT).show();		
				break;
				
			case R.id.preferences:
				Log.d(TAG, "ustawienia");
				startActivity(new Intent(this, PreferencesActivity.class));
				break;
		}
		return true;
	}
	
	
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.search:
				startActivity(new Intent(this, SearchActivity.class));
				break;
			case R.id.logo:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
		}
	}
		
	
    public void setUp() {
    	mapView.invalidate();
    	Location location = global_app.getLastLocation();
        int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		Log.d("defaultmap location", "lat: " + Integer.toString(lat) + " lon: " + Integer.toString(lng));
		geoPoint = new GeoPoint(lat, lng);
		mapController.setCenter(geoPoint);
        mapController.setZoom(10);
        mapController.animateTo(geoPoint);
        
        List<Overlay> overlays = mapView.getOverlays();
        overlays.clear();
        tasksCursor = global_app.getDataStorage().getActiveTasks();
        overlays.add(new MyOverlay());
        
    }
    

    protected boolean isRouteDisplayed() { return false; }

	public void onLocationChanged(Location location) {
		Log.d("onLocationChanged() from defaultMap","called");
		setUp();
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
	            	GeoPoint tmp = new GeoPoint(
	            			(int)(tasksCursor.getDouble(tasksCursor.getColumnIndex(DataStorage.C_LAT))*1E6), 
	            			(int)(tasksCursor.getDouble(tasksCursor.getColumnIndex(DataStorage.C_LAT))*1E6));
	            	mapView.getProjection().toPixels(tmp, pointGfx);
	            	bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.btn_star_big_on);   
	    	        
		            x = pointGfx.x - bmp.getWidth() / 2;                                                  
		        
		            y = pointGfx.y - bmp.getHeight();                                                     
		        
		            canvas.drawBitmap(bmp, x, y, null);
	            }
	        }

	    }

	}
	
}


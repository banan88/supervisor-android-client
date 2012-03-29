package org.supervisor;

import java.text.DecimalFormat;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;



public class DefaultMapActivity extends MapActivity implements LocationListener, OnClickListener, OnTouchListener{

	
	private static String TAG = DefaultMapActivity.class.getSimpleName();
	private Button searchButton;
	private Button logo;
	private MapView mapView;
	private MapController mapController;
	private SupervisorApplication global_app;
	private GeoPoint geoPoint;
	private Cursor tasksCursor;
	private LocationManager locationManager;
	private String provider;
	private Location lastLocation;
	private List<Overlay> allOverlays;
	private TasksOverlay updatablePositionOverlay;
	private TasksOverlay tasksPositionOverlay;
	private Drawable userMarker;
	private Drawable currentMarker;
	private Drawable doneMarker;
	private Drawable cancelMarker;
	private Drawable pendingMarker;
	private TaskUpdateReceiver receiver;
	private IntentFilter filter;
	private boolean animateToTaskPosition;
	private Long taskId;
	
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		unregisterReceiver(receiver);
	}



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        global_app = (SupervisorApplication) getApplication();
        
        setContentView(R.layout.layout_default_map);
        searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		searchButton.setOnTouchListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
		logo.setOnTouchListener(this);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if ( global_app.checkGPS())
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 15, this);
		else
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30*1000, 15, this);
		provider = locationManager.getBestProvider(new Criteria(), true);
		lastLocation = locationManager.getLastKnownLocation(provider);

		global_app.setLastLocation(lastLocation);
		Log.d(TAG, "lastLocation: LAT: " + Double.toString(lastLocation.getLatitude()) + " LON: " + 
		Double.toString(lastLocation.getLongitude()));
		
		userMarker = getResources().getDrawable(R.drawable.me);
		currentMarker = getResources().getDrawable(R.drawable.current_marker);
		pendingMarker = getResources().getDrawable(R.drawable.pending_marker);
		doneMarker = getResources().getDrawable(R.drawable.done_marker);
		cancelMarker = getResources().getDrawable(R.drawable.cancel_marker);
		
		try {
			taskId = getIntent().getExtras().getLong("taskId");
			Log.d(TAG + "task id", Long.toString(taskId));
			animateToTaskPosition = true;
		} catch (NullPointerException e) {
			animateToTaskPosition = false;
		}
		
        mapView = (MapView) findViewById(R.id.map);	
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        
        mapController = mapView.getController();
        mapController.setZoom(16);
        
        receiver = new TaskUpdateReceiver();
        filter = new IntentFilter(SupervisorApplication.UPDATE_VIEW_INTENT);
        registerReceiver(receiver, filter);
        
    }
    
	protected void onResume() {
		super.onResume();
		Log.d(TAG, Boolean.toString(animateToTaskPosition));
		registerReceiver(receiver, filter);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30*1000, 15, this);
		if(animateToTaskPosition) {
			Task task = global_app.getDataStorage().getTaskById(taskId);
			GeoPoint tmp = new GeoPoint((int) (task.getLatitude()*1E6), (int) (task.getLongitude()*1E6));
			mapController.animateTo(tmp);
		}
		else {
			int lat = (int) (lastLocation.getLatitude() * 1E6);
			int lng = (int) (lastLocation.getLongitude() * 1E6);
			geoPoint = new GeoPoint(lat, lng);
			mapController.animateTo(geoPoint);
		}
		paintCurrentPostion();
		paintTasks();
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
		
	public void paintCurrentPostion() {
		int lat = (int) (lastLocation.getLatitude() * 1E6);
		int lng = (int) (lastLocation.getLongitude() * 1E6);
		Log.d("defaultmap location", "lat: " + Integer.toString(lat) + " lon: " + Integer.toString(lng));
		geoPoint = new GeoPoint(lat, lng);
		
		allOverlays = mapView.getOverlays();
		allOverlays.remove(updatablePositionOverlay);
		updatablePositionOverlay = new TasksOverlay(userMarker, this);
		updatablePositionOverlay.addOverlay(new OverlayItem(geoPoint, global_app.getUsername(), Double.toString(lastLocation.getLatitude()) + " "
				 + Double.toString(lastLocation.getLongitude())));
		allOverlays.add(updatablePositionOverlay);
		mapView.invalidate();  
	}
	
    public void paintTasks() {
    	allOverlays = mapView.getOverlays();
    	allOverlays.remove(tasksPositionOverlay);
    	tasksPositionOverlay = new TasksOverlay(doneMarker, this);
    	tasksCursor = global_app.getDataStorage().getAllTasks();
    	DecimalFormat km = new DecimalFormat();
    	km.setMaximumFractionDigits(2);
    	DecimalFormat m = new DecimalFormat();
    	m.setMaximumFractionDigits(1);
        while(tasksCursor.moveToNext()) {
        	double lat = tasksCursor.getDouble(tasksCursor.getColumnIndex(DataStorage.C_LAT));
        	double lon = tasksCursor.getDouble(tasksCursor.getColumnIndex(DataStorage.C_LON));
        	String taskTitle = (tasksCursor.getString(tasksCursor.getColumnIndex(DataStorage.C_NAME)));
        	int taskState = (tasksCursor.getInt(tasksCursor.getColumnIndex(DataStorage.C_STATE)));
        	int taskId = (tasksCursor.getInt(tasksCursor.getColumnIndex(DataStorage.C_ID)));
        	GeoPoint taskLocation = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
        	Location tmp = new Location(provider);
        	tmp.setLatitude(lat);
        	tmp.setLongitude(lon);
        	double distance = lastLocation.distanceTo(tmp);
        	String prettyDistance;
        	if (distance > 1000)
        		prettyDistance = km.format(distance / 1000) + "km";
        	else 
        		prettyDistance = m.format(distance) + "m";
        	OverlayItem taskOverlayItem = new OverlayItem(taskLocation, taskTitle + " (id: " +  Integer.toString(taskId) + ")", 
        			"oddalone o: " + prettyDistance);
        	Drawable marker;
        	switch(taskState){
        		case 0:
        			marker = cancelMarker;
        			break;
        		case 1:
        			marker = pendingMarker;
        			break;
        		case 2:
        			marker = currentMarker;
        			break;
        		default:
        			marker = doneMarker;
        			break;
        	}
        	int w = marker.getIntrinsicWidth();
        	int h = marker.getIntrinsicHeight();
        	marker.setBounds(-w / 2, -h, w / 2, 0);
        	taskOverlayItem.setMarker(marker);
        	tasksPositionOverlay.addOverlay(taskOverlayItem);
        }
        allOverlays.add(tasksPositionOverlay);
        mapView.invalidate();
        tasksCursor.close();
    }
    
    
    public boolean onTouch(View v, MotionEvent event) {
		if(v instanceof Button) {
			Drawable d = v.getBackground();
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				d.setAlpha(50);
				v.setBackgroundDrawable(d);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				d.setAlpha(255);
				v.setBackgroundDrawable(d);
				
			}
		}
		return false;
	}

    

    protected boolean isRouteDisplayed() { return false; }

    
	public void onLocationChanged(Location location) {
		lastLocation = location;
		Log.d(TAG, "lastLocation change: LAT: " + Double.toString(lastLocation.getLatitude()) + " LON: " + 
				Double.toString(lastLocation.getLongitude()));
		paintCurrentPostion();
		paintTasks();
	}

	public void onProviderDisabled(String provider) {
		Log.d(TAG, "provider : " +provider + " disabled");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 15,  this);
		}else {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30*1000, 15, this);
		} 
	}

	public void onProviderEnabled(String provider) {
		Log.d(TAG, "provider : " +provider + " enabled");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);   
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 15, this);
		} else {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30*1000, 15, this);
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	
	
	class TaskUpdateReceiver extends BroadcastReceiver{

		public void onReceive(Context context, Intent intent) {
			paintTasks();
		}


	}
}


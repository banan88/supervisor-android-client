package org.supervisor.Activities;

import java.text.DecimalFormat;
import java.util.List;

import org.supervisor.R;
import org.supervisor.Services.SynchronisationService;
import org.supervisor.Util.DataStorage;
import org.supervisor.Util.SupervisorApplication;
import org.supervisor.Util.Task;
import org.supervisor.Util.TasksOverlay;

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
import android.location.Location;


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



public class DefaultMapActivity extends MapActivity implements OnClickListener, OnTouchListener{

	
	private static String TAG = DefaultMapActivity.class.getSimpleName();
	private Button searchButton;
	private Button logo;
	private MapView mapView;
	private MapController mapController;
	private SupervisorApplication global_app;
	private GeoPoint geoPoint;
	private Cursor tasksCursor;
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
	private DecimalFormat decimalFormat;
	
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        global_app = (SupervisorApplication) getApplication();
        decimalFormat = new DecimalFormat("#.########");
        
        setContentView(R.layout.layout_default_map);
        searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		searchButton.setOnTouchListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
		logo.setOnTouchListener(this);
		
		lastLocation = global_app.getLastLocation();
		//Log.d(TAG, "lastLocation: LAT: " + Double.toString(lastLocation.getLatitude()) + " LON: " + 
		//Double.toString(lastLocation.getLongitude()));
		
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
		if(animateToTaskPosition) {
			Task task = global_app.getDataStorage().getTaskById(taskId);
			GeoPoint tmp = new GeoPoint((int) (task.getLatitude()*1E6), (int) (task.getLongitude()*1E6));
			mapController.animateTo(tmp);
		}
		else {
			lastLocation = global_app.getLastLocation();
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
		int lat = (int) ((Double.valueOf(decimalFormat.format(lastLocation.getLatitude()))) * 1E6);
		int lng = (int) (lastLocation.getLongitude() * 1E6);
		geoPoint = new GeoPoint(lat, lng);
		
		allOverlays = mapView.getOverlays();
		allOverlays.remove(updatablePositionOverlay);
		updatablePositionOverlay = new TasksOverlay(userMarker, this);
		updatablePositionOverlay.addOverlay(new OverlayItem(geoPoint, global_app.getUsername(), 
				Double.toString(Double.valueOf(decimalFormat.format(lastLocation.getLatitude()))) + " " +
				Double.toString(Double.valueOf(decimalFormat.format(lastLocation.getLongitude())))));
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
	
	
	class TaskUpdateReceiver extends BroadcastReceiver{

		public void onReceive(Context context, Intent intent) {
			lastLocation = global_app.getLastLocation();
			Log.d(TAG, "received intent, redrawing");
			paintCurrentPostion();
			paintTasks();
		}


	}
}


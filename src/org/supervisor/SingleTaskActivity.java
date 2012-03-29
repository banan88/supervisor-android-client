package org.supervisor;

import java.text.DateFormat;
import java.util.Date;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class SingleTaskActivity extends BaseActivity {
	
	
	private Button logo;
	private Button searchButton;
	private TextView name;
	private Button location;
	private DataStorage dataStorage;
	private Task task;
	private TaskUpdateReceiver receiver;
	private IntentFilter filter;
	private Long getTaskById;
	private int taskState;
	private LinearLayout inner;
	private Button details;
	private Dialog dialog;
	private TextView desc;
	private Location taskLocation;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_task);
		dataStorage = global_app.getDataStorage();
		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		searchButton.setOnTouchListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
		logo.setOnTouchListener(this);
		name = (TextView) findViewById(R.id.category);
		location = (Button) findViewById(R.id.location);
		location.setOnTouchListener(this);
		details = (Button) findViewById(R.id.details);
		details.setOnTouchListener(this);
		receiver = new TaskUpdateReceiver();
		filter = new IntentFilter(SupervisorApplication.UPDATE_VIEW_INTENT);
		try {
			getTaskById = getIntent().getLongExtra("getTaskById", 0);
		} catch (NullPointerException e) {
			getTaskById = new Long(0);
		}
		inner = (LinearLayout) findViewById(R.id.inner);
	}
	
    
    public void setUp() {
    	if(getTaskById > 0)
    		task = dataStorage.getTaskById(getTaskById);
    	else 
    		task = dataStorage.getCurrentTask();
    	
       	if (task != null) {
        	taskLocation = new Location(LocationManager.NETWORK_PROVIDER);
    		taskLocation.setLatitude(task.getLatitude());
    		taskLocation.setLongitude(task.getLongitude());
       		inner.setVisibility(View.VISIBLE);
       		String defaultCancelled = " (Anulowane";
       		switch(task.getState()) {
       			case 3:
       				defaultCancelled = " (Wykonane";
       				break;
       			case 2:
       				defaultCancelled = " (Aktywne";
       				break;
       			case 1:
       				defaultCancelled = " (Oczekujące";
       				break;
       		}
       		Log.d("task id", Long.toString(task.getId()));
	       	name.setText(task.getName() + defaultCancelled + ")");
	       	location.setText("Pokaż na mapie: " + task.getLatitude() + " N " + task.getLongitude() + " S ");
	       	location.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent showOnMap = new Intent(SingleTaskActivity.this, DefaultMapActivity.class);
					showOnMap.putExtra("taskId", task.getId());
					startActivity(showOnMap);
				}
			});
	       	Log.d("single task", task.getSupervisor());
	       	desc = (TextView) findViewById(R.id.desc);
	       	desc.setText(task.getDescription());
	       	Log.d("single", task.getDescription());
	       	details.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
					int rotation = display.getRotation();
					 dialog = new Dialog(SingleTaskActivity.this);
		             dialog.setContentView(R.layout.task_details);
		             dialog.setCancelable(true);
		             dialog.setTitle(getString(R.string.task_details_title));
		             TextView text = (TextView) dialog.findViewById(R.id.created_by);
		             text.setText("utworzone przez: " + task.getSupervisor());
		             text = (TextView) dialog.findViewById(R.id.created_by_time);
		             text.setText("czas utworzenia: " + 
		            		 DateFormat.getDateTimeInstance().format(
		            				 new Date(Long.parseLong(task.getCreationTime()))));
		             text = (TextView) dialog.findViewById(R.id.last_modified);
		             if(Surface.ROTATION_90 == rotation) {
		            	 text.setText("ostatnio zmodyfikowane: " + 
		            			 DateFormat.getDateTimeInstance().format(
		            					 Long.parseLong(task.getLastModified())));
			             if(Long.parseLong(task.getLastSynced()) > 0) {
			            	 text = (TextView) dialog.findViewById(R.id.sync_state);
			            	 text.setText("ostatnio zsynchronizowane: " +
			            			 DateFormat.getDateTimeInstance().format(
			            					 Long.parseLong(task.getLastSynced())));
			             }
		             }
		             else {
		            	 text.setText("ostatnio zmodyfikowane: \n" + 
		            			 DateFormat.getDateTimeInstance().format(
		            					 Long.parseLong(task.getLastModified())));
			             if(Long.parseLong(task.getLastSynced()) > 0) {
			            	 text = (TextView) dialog.findViewById(R.id.sync_state);
			            	 text.setText("ostatnio zsynchronizowane: \n" +
			            			 DateFormat.getDateTimeInstance().format(
			            					 Long.parseLong(task.getLastSynced())));
			             }
		             }
		             if(Long.parseLong(task.getStartTime())>0) {
		            	 text = (TextView) dialog.findViewById(R.id.start);
		            	 text.setText("czas rozpoczęcia: " +
		            			 DateFormat.getDateTimeInstance().format(
		            					 Long.parseLong(task.getStartTime())));
		             }
		             if(Long.parseLong(task.getFinishTime())>0) {
		            	 Log.d("single", task.getFinishTime());
		            	 text = (TextView) dialog.findViewById(R.id.finish);
		            	 text.setText("czas zakończenia: " +
		            			 DateFormat.getDateTimeInstance().format(
		            					 Long.parseLong(task.getFinishTime())));
		             }
		            	 
		             dialog.setCanceledOnTouchOutside(true);
		             dialog.show();
		            }
		        });
	       	taskState = task.getState();
       	}
       	else {
       		if(getTaskById > 0) 
       			name.setText("Takie zadanie nie istnieje.");
       		else
       			name.setText("Brak aktywnego zadania.");
       			inner.setVisibility(View.INVISIBLE);
       	}
    }
   
    
    protected void onResume() {
    	super.onResume();
    	registerReceiver(receiver, filter);
    	setUp();
    }    
    
    
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(receiver);
    	if(dialog!=null)
    		dialog.cancel();
    }
    
    
    public void onClick(View v) {
    	super.onClick(v);
	}
    
    
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.removeItem(101);
    	menu.removeItem(102);
    	if(taskState == 1 && !dataStorage.isThereACurrentTask())
    		menu.add(Menu.NONE, 101, Menu.NONE, R.string.menu_task_current);
    	else if (taskState == 2)
    		menu.add(Menu.NONE, 102, Menu.NONE, R.string.menu_task_done);
    	setUp();
    	return true;
    }
    
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	Time t = new Time();
    	t.setToNow();
    	Log.d("JAVA millis time: " , Long.toString(t.toMillis(false)));
		switch(item.getItemId()) {	
			case 101:
				dataStorage.taskStarted(task.getId(), t.toMillis(false));
				taskState = 2;
				setUp();
				break;
			case 102:
				if(taskLocation.distanceTo(global_app.getLastLocation()) < 300) {
					dataStorage.taskFinished(task.getId(), t.toMillis(false));
					taskState = 0;
					setUp();
				}
				else 
					Toast.makeText(this, getString(R.string.too_far), Toast.LENGTH_LONG).show();
				break;
		}
		return true;
	}
    
    
    class TaskUpdateReceiver extends BroadcastReceiver{

		public void onReceive(Context context, Intent intent) {
			SingleTaskActivity.this.setUp();
		}
		
    }
	
}

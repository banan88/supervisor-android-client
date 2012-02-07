package org.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SingleTaskActivity extends BaseActivity {
	
	
	private Button logo;
	private Button searchButton;
	private Button map;
	private TextView name;
	private TextView syncTime;
	private TextView desc;
	private TextView added;
	private TextView localisation;
	private DataStorage dataStorage;
	private Task task;
	private TaskUpdateReceiver receiver;
	private IntentFilter filter;
	private Long getTaskById;
	private int taskState;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_task);
		dataStorage = global_app.getDataStorage();
		name = (TextView) findViewById(R.id.category);
		syncTime = (TextView) findViewById(R.id.task_sync_time);
		localisation = (TextView) findViewById(R.id.task_localisation);
		desc = (TextView) findViewById(R.id.task_desc);
		added = (TextView) findViewById(R.id.task_added);
		receiver = new TaskUpdateReceiver();
		filter = new IntentFilter(SupervisorApplication.UPDATE_VIEW_INTENT);
		try {
			getTaskById = getIntent().getLongExtra("getTaskById", 0);
		} catch (NullPointerException e) {
			getTaskById = new Long(0);
		}
		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		map = (Button) findViewById(R.id.map_icon);
		map.setOnClickListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
	}
	
    
    public void setUp() {
    	if(getTaskById > 0)
    		task = dataStorage.getTaskById(getTaskById);
    	else 
    		task = dataStorage.getCurrentTask();
    	
       	if (task != null) {
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
	       	name.setText(task.getName() + defaultCancelled + ")");
	       	syncTime.setText(task.getCreationTime());
	       	localisation.setText(task.getLatitude() + " " + task.getLongitude());
	       	desc.setText(task.getDescription());
	       	added.setText(task.getSupervisor());
	       	taskState = task.getState();
       	}
       	else {
       		if(getTaskById > 0) 
       			name.setText("ZADANIE NIE ISTNIEJE");
       		else
       			name.setText("NIE WYKONUJESZ ZADNEGO ZADANIA");
       		
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
    }
    
    
    public void onClick(View v) {
    	super.onClick(v);
		switch(v.getId()) {
			case R.id.map_icon:
				//to do: start mapActivity
				break;
		}
	}
    
    
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.removeItem(101);
    	menu.removeItem(102);
    	if(taskState == 1)
    		menu.add(Menu.NONE, 101, Menu.NONE, R.string.menu_task_current);
    	else if (taskState == 2)
    		menu.add(Menu.NONE, 102, Menu.NONE, R.string.menu_task_done);
    	setUp();
    	return true;
    }
    
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	Log.d("single task act", Integer.toString(item.getItemId()));
    	Time t = new Time();
    	t.setToNow();
		switch(item.getItemId()) {	
			case 101:
				dataStorage.taskStarted(task.getId(), t.toMillis(false));
				break;
			case 102:
				dataStorage.taskFinished(task.getId(), t.toMillis(false));
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

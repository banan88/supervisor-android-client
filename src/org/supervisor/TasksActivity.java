package org.supervisor;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class TasksActivity extends BaseActivity {
	
	private DataStorage dataStorage;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private ListView taskList;
	private SupervisorApplication global_app;
	private static final String []FROM = {DataStorage.C_NAME, 
		DataStorage.C_DESC, DataStorage.C_LAST_MODIFIED};
	private static final int []TO = {R.id.taskName, 
		R.id.taskDesc, R.id.taskDate};
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        /*
         * this is a main activity, after user launches it for first time 
         * (we assume that first time is when properties are empty)
         * we launch preferences activity - 
         * which after user fills server adress - starts the sync service
         */
        global_app = (SupervisorApplication) getApplication();
		if(global_app.getServerURL() == null || global_app.getUsername() == null || global_app.getPassword() == null) {
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			Toast.makeText(this, R.string.no_config, Toast.LENGTH_LONG).show();
		}
		
        setContentView(R.layout.tasklist);
        taskList = (ListView) findViewById(R.id.tasklist);
    }
    
    
    private void setUp() {
    	dataStorage = global_app.getDataStorage();
       	cursor = dataStorage.getAllTasks();
       	startManagingCursor(cursor);
       	adapter = new SimpleCursorAdapter(this, R.layout.tasklist_item, cursor, FROM, TO); //
    	taskList.setAdapter(adapter);

    }
    
    public void onDestroy() {
    	super.onDestroy();
    }
    
    public void onResume() {
    	super.onResume();
    	setUp();
    }


	@Override
	protected void onStop() {
		super.onStop();
	}
    
    
   
}
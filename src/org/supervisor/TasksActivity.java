package org.supervisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TasksActivity extends BaseActivity {
	
	private DataStorage dataStorage;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private ListView taskList;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString(PreferencesActivity.server_pref_key, null);
		String username = prefs.getString(PreferencesActivity.username_pref_key, null);
		String password = prefs.getString(PreferencesActivity.password_pref_key, null);
		if(server == null || username == null || password == null) {
			Intent intent = new Intent(this, PreferencesActivity.class).putE;
			startActivity(intent);
		}
		
        setContentView(R.layout.tasklist);
        taskList = (ListView) findViewById(R.id.tasklist);
    }
    
    
    private void setUp() {
    	dataStorage = new DataStorage(this);
       	cursor = dataStorage.getAllTasks();
       	startManagingCursor(cursor);
       	adapter = new SimpleCursorAdapter(this, R.layout.tasklist_item, cursor, FROM, TO); //
    	taskList.setAdapter(adapter);

    }
    
    public void onDestroy() {
    	super.onDestroy();
    	dataStorage.close();
    }
    
    public void onResume() {
    	super.onResume();
    	setUp();
    }
   
}
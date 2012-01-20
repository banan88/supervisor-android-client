package org.supervisor;

import android.database.Cursor;
import android.os.Bundle;
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
        setContentView(R.layout.layout_tasklist);
        taskList = (ListView) findViewById(R.id.tasklist);
        dataStorage = global_app.getDataStorage();
    }
    
    
    private void setUp() {
    	dataStorage = global_app.getDataStorage();
       	cursor = dataStorage.getAllTasks();
       	startManagingCursor(cursor);
       	adapter = new SimpleCursorAdapter(this, R.layout.tasklist_item, cursor, FROM, TO); //
    	taskList.setAdapter(adapter);

    }
   
    
    public void onResume() {
    	super.onResume();
    	setUp();
    }    
   
    
}
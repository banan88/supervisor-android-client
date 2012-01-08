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
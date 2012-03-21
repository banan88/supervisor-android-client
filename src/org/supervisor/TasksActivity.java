package org.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TasksActivity extends BaseActivity{
	
	
	private Button logo;
	private Button searchButton;
	private DataStorage dataStorage;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private ListView taskList;
	private TextView name;
	private final String TAG = TasksActivity.class.getSimpleName(); 
	private TaskUpdateReceiver receiver;
	private IntentFilter filter;
	private static final String []FROM = {DataStorage.C_NAME, DataStorage.C_LAST_MODIFIED};
	private static final int []TO = {R.id.firstline, R.id.secondline};
	private boolean archiveView;
   	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.layout_tasklist);
    	dataStorage = global_app.getDataStorage();
        taskList = (ListView) findViewById(R.id.tasklist);
		name = (TextView) findViewById(R.id.category);
		receiver = new TaskUpdateReceiver();
		filter = new IntentFilter(SupervisorApplication.UPDATE_VIEW_INTENT);
		try {
			archiveView = getIntent().getBooleanExtra("archiveView", false);
		} catch (NullPointerException e) {
			archiveView = false;
		}
		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
		
		taskList.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
				Long id_ = cursor.getLong(cursor.getColumnIndex(DataStorage.C_ID));
				Intent intent = new Intent(TasksActivity.this, SingleTaskActivity.class);
				intent.putExtra("getTaskById", id_);
				startActivity(intent);
			}
		});
    }
    
    
    public void setUp() {
    	if(archiveView) {
    		name.setText(getString(R.string.subtitle_task_archive));
    		cursor = dataStorage.getDoneAndCancelledTasks();
    	}
    	else {
    		name.setText(getString(R.string.subtitle_tasks_list));
    		cursor = dataStorage.getActiveTasks();
    	}
       	startManagingCursor(cursor);
       	adapter = new TasksAdapter(); 
    	taskList.setAdapter(adapter);
    	Log.d(TAG, Boolean.toString(archiveView));
    }
   
    
    protected void onResume() {
    	super.onResume();
    	registerReceiver(receiver, filter);
    	setUp();
    	adapter.notifyDataSetChanged();
    }    
    
    
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(receiver);
    }
    
    
    public void onClick(View v) {
    	super.onClick(v);
		
	}
    
    
    class TasksAdapter extends SimpleCursorAdapter {
    	
    	 LayoutInflater inflater =  (LayoutInflater) TasksActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	
    	public TasksAdapter() {
			super(TasksActivity.this, R.layout.tasklist_item, cursor, FROM, TO);
		}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		cursor.moveToPosition(position);
    		ViewHolder holder;
    		if(convertView == null) {
    			convertView = inflater.inflate(R.layout.tasklist_item,null,true);
    			holder = new ViewHolder();
    			holder.textView1 = (TextView) convertView.findViewById(R.id.firstline);
    			holder.textView2 = (TextView) convertView.findViewById(R.id.secondline);
    			
                holder.imageView = (ImageView) convertView.findViewById(R.id.listitem_icon);
                int state = cursor.getInt(cursor.getColumnIndex(DataStorage.C_STATE));
                Log.d(TAG, Integer.toString(state));
                switch(state) {
                	case 3:
                		holder.imageView.setImageResource(R.drawable.done);
                		break;
                	case 2:
                		holder.imageView.setImageResource(R.drawable.current);
                		break;
                	case 1:
                		holder.imageView.setImageResource(R.drawable.clock);
                		break;
                	case 0:
                		holder.imageView.setImageResource(R.drawable.cancel);
                		break;
                }
                
                convertView.setTag(holder);
    		}
    		else {
    			holder = (ViewHolder) convertView.getTag();
    		}
    		
    		String s = cursor.getString(cursor.getColumnIndex(DataStorage.C_NAME));
            holder.textView1.setText(s);
            Log.d(TAG, Long.toString((cursor.getLong(cursor.getColumnIndex(DataStorage.C_LAST_MODIFIED)))) + " tu");
            s = "zmodyfikowane: " + DateUtils.getRelativeTimeSpanString(
            		cursor.getLong(cursor.getColumnIndex(DataStorage.C_LAST_MODIFIED))).toString();
            holder.textView2.setText(s);
            
            return convertView;
        } 
    }
    
   	static class ViewHolder {
        public ImageView imageView;
        public TextView textView1;
        public TextView textView2;
    }
    
    
    class TaskUpdateReceiver extends BroadcastReceiver{

		public void onReceive(Context context, Intent intent) {
			TasksActivity.this.setUp();
		}


	}
    
}
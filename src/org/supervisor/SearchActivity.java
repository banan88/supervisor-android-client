package org.supervisor;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


public class SearchActivity extends BaseActivity {
	
	
	private final String TAG = SearchActivity.class.getSimpleName();
	private Button shortLogo;
	private Button startSearch;
	private Spinner spinner;
	private ListView searchResults;
	private EditText searchText;
	private Cursor cursor;
	private SimpleCursorAdapter cursorAdapter;
	private static final String []FROM = {DataStorage.C_NAME, DataStorage.C_DESC};
	private static final int []TO = {R.id.firstline, R.id.secondline};
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_search);
		startSearch = (Button) findViewById(R.id.start_search);
		startSearch.setOnClickListener(this);
		startSearch.setOnTouchListener(this);
		shortLogo = (Button) findViewById(R.id.logo_short);
		shortLogo.setOnClickListener(this);
		shortLogo.setOnTouchListener(this);
		spinner = (Spinner) findViewById(R.id.category);
		
		searchText = (EditText) findViewById(R.id.searchText);
		searchText.setOnEditorActionListener(new OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				Log.d(TAG, "onEditorAction()");
				if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)
				{
							Log.d(TAG, "insideAction");
					      loadSearchResults();
					   }
					   return true;
			}
		});
		searchResults = (ListView) findViewById(R.id.tasklist);
		searchResults.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
				Long id_ = cursor.getLong(cursor.getColumnIndex(DataStorage.C_ID));
				Intent intent = new Intent(SearchActivity.this, SingleTaskActivity.class);
				intent.putExtra("getTaskById", id_);
				startActivity(intent);
			}
		});
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.spinner_entries, R.layout.spinner_text);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	            	global_app.setLastSearchFilter(position);
	        }

	        public void onNothingSelected(AdapterView<?> parentView) {}
	    });
	}
	
	
	public void loadSearchResults() {
		global_app.getLastSearchFilter();
		int pos = spinner.getSelectedItemPosition();
		Log.d(TAG, "position: " + Integer.toString(pos));
		if(cursor != null)
			cursor.close();
		
		if(pos == 0) {
			cursor = dataStorage.searchAllTasks(searchText.getText().toString());
		}
		else if(pos == 1) {
			cursor = dataStorage.searchArchivedTasks(searchText.getText().toString());
		}
		else
			cursor = dataStorage.searchListOfTasks(searchText.getText().toString());
		startManagingCursor(cursor);
		Log.d(TAG, "cursor size: " + Integer.toString(cursor.getCount()));
		Log.d(TAG, "keyword: " + searchText.getText().toString());
    	cursorAdapter = new TasksAdapter(); 
		searchResults.setAdapter(cursorAdapter);
	}
	
	
	protected void onResume() {
    	super.onResume();
    	spinner.setSelection(global_app.getLastSearchFilter());
    	loadSearchResults();
    }    
	
	
	
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.start_search:
				Log.d(TAG, "filtering tasks");
				loadSearchResults();
				break;
			case R.id.logo_short:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
			case R.id.category:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
		}
			
	}
	
	
	class TasksAdapter extends SimpleCursorAdapter {
    	
   	 LayoutInflater inflater =  (LayoutInflater) SearchActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   	
   	public TasksAdapter() {
			super(SearchActivity.this, R.layout.tasklist_item, cursor, FROM, TO);
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

}

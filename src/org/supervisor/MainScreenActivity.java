package org.supervisor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainScreenActivity extends BaseActivity {
	
	
	private SupervisorApplication global_app;
	private Button searchButton;
	private Button activeTaskButton;
	private Button taskListButton;
	private Button taskArchiveButton;
	private Button timeButton;
	private TextView syncText;
	private Button syncImage;
	
	
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState); 
		 setContentView(R.layout.layout_main_screen);
		 global_app = (SupervisorApplication) getApplication();	
		 if(global_app.getServerURL() == null || global_app.getUsername() == null || global_app.getPassword() == null) {
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			Toast.makeText(this, R.string.no_config, Toast.LENGTH_LONG).show();
		 }       
	    
		 activeTaskButton = (Button) findViewById(R.id.active);
		 activeTaskButton.setOnClickListener(this);
		 taskListButton = (Button) findViewById(R.id.tasklist);
		 taskListButton.setOnClickListener(this);
		 taskArchiveButton = (Button) findViewById(R.id.archive);
		 taskArchiveButton.setOnClickListener(this);
		 timeButton = (Button) findViewById(R.id.time);
		 timeButton.setOnClickListener(this);
		 syncText = (TextView) findViewById(R.id.sync_textbutton);
		 syncText.setOnClickListener(this);
		 syncImage = (Button) findViewById(R.id.sync_button);
		 syncImage.setOnClickListener(this);
		 searchButton = (Button) findViewById(R.id.search);
		 searchButton.setOnClickListener(this);
	 }
	
	 
	 private void syncOnClick() {
		 if ( global_app.isNetworkOn() ) {
				super.runForcedSync();
			}
	 }
	 
	 
	 public void onClick(View v) {
		 super.onClick(v);
		 
		 switch (v.getId()){
			case R.id.active:
				startActivity(new Intent(this, SingleTaskActivity.class));
				break;
			case R.id.tasklist:
				startActivity(new Intent(this, TasksActivity.class));
				break;
			case R.id.archive:
				Intent intent = new Intent(this, TasksActivity.class);
				intent.putExtra("archiveView", true);
				startActivity(intent);
				break;
			case R.id.time:
				startActivity(new Intent(this, TimeActivity.class));
				break;
			case R.id.sync_button:
				syncOnClick();
				break;
			case R.id.sync_textbutton:
				syncOnClick();
				break;
			}
		}
}

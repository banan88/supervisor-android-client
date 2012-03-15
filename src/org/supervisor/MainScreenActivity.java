package org.supervisor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
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
	private Button settings;
	private Button map;
	private DialogInterface.OnClickListener dialogClickListener;
	private AlertDialog dialog;
	
	
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
		 settings = (Button) findViewById(R.id.settings);
		 settings.setOnClickListener(this);
		 map = (Button) findViewById(R.id.map);
		 map.setOnClickListener(this);
		 
		 dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			        	startActivity(settingsIntent);
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
						startActivity(new Intent(MainScreenActivity.this, DefaultMapActivity.class));
			            break;
			        }
			    }
			};
	 }
	 
	 
	 protected void onResume() {
	    	super.onResume();
	    	updateLastSyncText();
	    }    
	 
	 
	 private void updateLastSyncText() {
		 if(global_app.wasLastSyncSuccessful())
	    		syncText.setText("Ostatnia synchronizacja: " + DateUtils.getRelativeTimeSpanString(
	    				global_app.getLastSyncTime()));
	    	else if (global_app.getLastSyncTime() == 0)
	    		syncText.setText("Nie wykonano synchronizacji"); 
	    	
	    	else
	    		syncText.setText("Nieudana synchronizacja: " + DateUtils.getRelativeTimeSpanString(
	    				global_app.getLastSyncTime()));    		
	 }
	 
	 
	 public void syncOnClick() {
		if ( global_app.isNetworkOn() ) 
			super.runForcedSync();
		syncImage.startAnimation(AnimationUtils.loadAnimation(MainScreenActivity.this, R.anim.sync_button_rotate));
		syncText.postDelayed(new Thread() {
			public void run() {
					updateLastSyncText(); 
					interrupt();
			}
		}, 3000);
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
			case R.id.settings:
				startActivity(new Intent(this, PreferencesActivity.class));
				break;
			case R.id.map:
				if (!global_app.checkGPS() && !global_app.wasPromptedForGPSEnabling()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(R.string.gps_info).setPositiveButton("Tak", dialogClickListener)
					    .setNegativeButton("Nie", dialogClickListener);		
					global_app.setAlreadyPromptedForGPSEnabling();
					dialog = builder.show();
				}else
					startActivity(new Intent(this, DefaultMapActivity.class));
			}
		}
	 
	 protected void onPause() {
	    	super.onPause();
	    	if(dialog!=null)
	    		dialog.dismiss();
	    }
}

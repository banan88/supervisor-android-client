package org.supervisor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainScreenActivity extends BaseActivity implements OnClickListener{
	
	
	private SupervisorApplication global_app;
	private Button zoomButton;
	private Button sync;
	
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState); 
		 setContentView(R.layout.main_screen);
		 global_app = (SupervisorApplication) getApplication();	
		 if(global_app.getServerURL() == null || global_app.getUsername() == null || global_app.getPassword() == null) {
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			Toast.makeText(this, R.string.no_config, Toast.LENGTH_LONG).show();
		 }       
	    
		 zoomButton = (Button) findViewById(R.id.zoom);
		 zoomButton.setOnClickListener(this);
	 }
	
	 
	 public void onClick(View v) {
			switch (v.getId()){
				case R.id.zoom:
					startActivity(new Intent(this, SearchActivity.class));
					break;
			}
				
		}
}

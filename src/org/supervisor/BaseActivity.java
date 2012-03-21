package org.supervisor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class BaseActivity extends Activity implements OnClickListener{
	

	private static final String TAG = BaseActivity.class.getSimpleName();
	protected SupervisorApplication global_app;
	protected DataStorage dataStorage;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		global_app = (SupervisorApplication) getApplication();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		dataStorage = global_app.getDataStorage();
	}
		

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "menu item selected" + item.toString());
		switch(item.getItemId()) {
			case R.id.synchronise:
				Log.d(TAG, "synchronizacja");
				if ( global_app.isNetworkOn() )
					syncOnClick();
				else
					Toast.makeText(this, R.string.dialog_text_no_network, Toast.LENGTH_SHORT).show();		
				break;
				
			case R.id.preferences:
				Log.d(TAG, "ustawienia");
				startActivity(new Intent(this, PreferencesActivity.class));
				break;
		}
		return true;
	}
	
	
	public void syncOnClick() { runForcedSync(); }
	
	
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.search:
				startActivity(new Intent(this, SearchActivity.class));
				break;
			case R.id.logo:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
			case R.id.logo_short:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
		}
	}
	
	
	public void runForcedSync() {
		startService(new Intent(BaseActivity.this, SynchronisationService.class));	
	}
	
	
	public boolean onSearchRequested() {
		if(!(this instanceof SearchActivity))
			startActivity(new Intent(this, SearchActivity.class));
		return false;
	}

}

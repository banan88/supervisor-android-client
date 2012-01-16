package org.supervisor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.synchronise:
				NetworkInfo networkInfo = ((ConnectivityManager) getApplicationContext()
						.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
				if ( !(networkInfo == null || !networkInfo.isConnected()) ) {
					startService(new Intent(this, SynchronisationService.class));
				}
				else
					Toast.makeText(this, R.string.dialog_text_no_network, Toast.LENGTH_SHORT).show();		
				break;
				
			case R.id.tasklist:
				startActivity(new Intent(this, TasksActivity.class));
				break;
			
			case R.id.preferences:
				startActivity(new Intent(this, PreferencesActivity.class));
				break;
		}
		return true;
	}
	


}

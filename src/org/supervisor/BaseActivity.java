package org.supervisor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {
	
	private static final String TAG = BaseActivity.class.getSimpleName();
	private SupervisorApplication global_app;
	private String text;
	private String status_text;
	private String title;
	private boolean isRequestOk;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		global_app = (SupervisorApplication) getApplication();
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
				if ( global_app.isNetworkOn() ) {
					new ForcedSync().execute();
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
	


	private class ForcedSync extends AsyncTask<String[], Void, Boolean> { 
		
		@Override
		protected Boolean doInBackground(String[]... params) {
			Log.d(TAG, "asynctask running in background");
			if( global_app.isNetworkOn() ) {
				text = getString(R.string.sync_notification_body);
				status_text = getString(R.string.sync_status_bar_txt);
				title = getString(R.string.sync_notification_title);
				isRequestOk = true;
				Log.d(TAG, "Network on!");
				ApiManager.setCredentials(global_app.getUsername(), global_app.getPassword());
				String adress = global_app.getServerURL();
				try {
					new URL(adress);
				} catch (MalformedURLException e) {
					Log.d(TAG, adress);
					Log.d(TAG, e.getMessage());
				}
				ApiManager.HOST = adress;
				int icon = R.drawable.ic_menu_refresh;
				
				ArrayList<Task> tasks = null;
				Intent intent = null;
					
				try {
					try {
						tasks = ApiManager.getTasks(); //actual rest call
					} catch (IllegalArgumentException e) {
						throw new NetworkErrorException(e.getMessage());
					}
						
				} catch (NetworkErrorException e) {					
					intent = new Intent(BaseActivity.this, PreferencesActivity.class);
					status_text = getString(R.string.sync_status_bar_txt_error);
					if (e.getMessage().equals("401")) 
						text = getString(R.string.sync_notification_body_err);
					else 
						text = getString(R.string.sync_notification_body_ser_err);
					isRequestOk = false;
				}
						
				global_app.generateNotification(new String[]{status_text, title, text}, icon, 2000, !isRequestOk, intent);
				Log.d(TAG + " IS DB EMPTY:", Boolean.toString(global_app.getDataStorage().isEmpty()));
				global_app.insertTaskUpdates(tasks);
				Log.d(TAG + " IS DB EMPTY:", Boolean.toString(global_app.getDataStorage().isEmpty()));
					
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
		
		
		
	}
}

package org.supervisor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
	private String text;
	private String status_text;
	private String title;
	private boolean isRequestOk;
	private DataStorage dataStorage;
	
	
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
					runForcedSync();
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
			new ForcedSync().execute();
	}


	private class ForcedSync extends AsyncTask<String[], Void, Boolean> { 
		
		protected Boolean doInBackground(String[]... params) {
			if( global_app.isNetworkOn() ) {
				text = getString(R.string.sync_notification_body);
				status_text = getString(R.string.sync_status_bar_txt);
				title = getString(R.string.sync_notification_title);
				isRequestOk = true;
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
						if(global_app.getDataStorage().isEmpty()) 
							tasks = ApiManager.getNTasks(100);
						else {
							Cursor cursor = dataStorage.getNonSyncedTasks();
							ApiManager.changeTasksStates(cursor);
							cursor.close();
							tasks = ApiManager.getTasksSinceLastSync();
						}
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
				dataStorage.clearNonSyncedTasks();
				int count = global_app.insertTaskUpdates(tasks);
				if (count != 0) 
					BaseActivity.this.sendBroadcast(new Intent(SupervisorApplication.UPDATE_VIEW_INTENT));
				global_app.generateNotification(new String[]{status_text, title, text}, icon, 2000, !isRequestOk, intent);
				
			}
			return null;
		}
		
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}
	
}

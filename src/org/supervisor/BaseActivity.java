package org.supervisor;

import android.app.Activity;
import android.content.Intent;
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
				startService(new Intent(this, SynchronisationService.class));
				//ProgressDialog dialog = ProgressDialog.show(BaseActivity.this, "",
						//getString(R.string.dialog_text), true);
				Toast.makeText(this, R.string.dialog_text, Toast.LENGTH_SHORT).show();	
				break;
				
			case R.id.tasklist:
				startActivity(new Intent(this, TasksActivity.class));
			break;
			
			case R.id.preferences:
				startActivity(new Intent(this, PreferencesActivity.class));
		}
		return true;
	}
	


}

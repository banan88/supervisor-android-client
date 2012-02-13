package org.supervisor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class SearchActivity extends BaseActivity {
	
	
	private final String TAG = SearchActivity.class.getSimpleName();
	private Button shortLogo;
	private Button startSearch;
	private Spinner spinner;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_search);
		startSearch = (Button) findViewById(R.id.start_search);
		startSearch.setOnClickListener(this);
		shortLogo = (Button) findViewById(R.id.logo_short);
		shortLogo.setOnClickListener(this);
		spinner = (Spinner) findViewById(R.id.category);
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
	
	
	protected void onResume() {
    	super.onResume();
    	spinner.setSelection(global_app.getLastSearchFilter());
    }    
	
	
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.start_search:
				Log.d(TAG, "filtering tasks");
				break;
			case R.id.logo_short:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
			case R.id.category:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
		}
			
	}

}

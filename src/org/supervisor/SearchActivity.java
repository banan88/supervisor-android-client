package org.supervisor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SearchActivity extends BaseActivity {
	
	
	private final String TAG = SearchActivity.class.getSimpleName();
	private Button shortLogo;
	private Button startSearch;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_search);
		startSearch = (Button) findViewById(R.id.start_search);
		startSearch.setOnClickListener(this);
		shortLogo = (Button) findViewById(R.id.logo_short);
		shortLogo.setOnClickListener(this);
	}
	
	
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.start_search:
				Log.d(TAG, "filtering tasks");
				break;
			case R.id.logo_short:
				startActivity(new Intent(this, MainScreenActivity.class));
				break;
		}
			
	}

}

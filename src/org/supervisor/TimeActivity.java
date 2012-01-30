package org.supervisor;

import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TimeActivity extends BaseActivity{
	
	
	private TextView name;
	private Button logo;
	private Button searchButton;
	private Time time;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_time);
		time = new Time();
		time.setToNow();
		name = (TextView) findViewById(R.id.category);
		name.setText("Czas pracy (" + time.format("%Y-%m-%d") + ")");
		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
	}
	
	
	public void onClick(View v) {
		super.onClick(v);	
	}
	
}

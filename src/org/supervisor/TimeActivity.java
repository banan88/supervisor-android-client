package org.supervisor;


import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TimeActivity extends BaseActivity{
	
	
	private TextView name;
	private Button logo;
	private Button searchButton;
	private Time time;
	private Button toggle;
	private DataStorage dataStorage;
	private TextView tv1;
	private TextView tv2;
	private int state;
	private Time currentTime;
	
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
		toggle = (Button) findViewById(R.id.buttonToggle);
		toggle.setOnClickListener(this);
		tv1 = (TextView) findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		dataStorage = global_app.getDataStorage();
	}
	
	protected void onResume() {
		super.onResume();
		currentTime = new Time();
		currentTime.setToNow();
		toggleButtonOnDayState();
	}
	
	private void toggleButtonOnDayState() {
		state = dataStorage.dayState(Integer.parseInt(currentTime.format("%Y%m%d")));
		Log.d("Timeactivity!", "State: " + Integer.toString(state));
		switch(state) {
			case 0:
				toggle.setEnabled(true);
				toggle.setText("Rozpocznij pracę");
				break;
			case 1:
				toggle.setEnabled(true);
				toggle.setText("Rozpocznij pracę");
				break;
			case 2:
				toggle.setEnabled(false);
				break;
			case 3:
				toggle.setEnabled(true);
				toggle.setText("Zakończ pracę");
				break;
		}
	}
	
	
	public void onClick(View v) {
		super.onClick(v);	
		if(v.getId() == R.id.buttonToggle) {
			switch(state) {
				case 3:
					dataStorage.finishWork(Integer.parseInt(currentTime.format("%Y%m%d")), currentTime.toMillis(false));
					break;
				default:
					dataStorage.startWork(Integer.parseInt(currentTime.format("%Y%m%d")), currentTime.toMillis(false));
					break;
			}
			toggleButtonOnDayState();
		}
	}
	
}

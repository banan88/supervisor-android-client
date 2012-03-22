package org.supervisor;

import android.database.Cursor;
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
	private Button toggle;
	private DataStorage dataStorage;
	private TextView tv1;
	private TextView tv2;
	private int state;
	private Time currentTime;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_time);
		name = (TextView) findViewById(R.id.category);
		searchButton = (Button) findViewById(R.id.search);
		searchButton.setOnClickListener(this);
		searchButton.setOnTouchListener(this);
		logo = (Button) findViewById(R.id.logo);
		logo.setOnClickListener(this);
		logo.setOnTouchListener(this);
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
		name.setText("Czas pracy (" + currentTime.format("%Y-%m-%d") + ")");
		toggleButtonOnDayState();
	}
	
	private void toggleButtonOnDayState() {
		state = dataStorage.dayState(Integer.parseInt(currentTime.format("%Y%m%d")));
		Log.d("Timeactivity!", "State: " + Integer.toString(state));
		switch(state) {
			case 0:
				toggle.setEnabled(true);
				toggle.setText("Rozpocznij pracę");
				tv1.setText("");
				tv2.setText("");
				break;
			case 1:
				toggle.setEnabled(true);
				toggle.setText("Rozpocznij pracę");
				tv1.setText("");
				tv2.setText("");
				break;
			case 2:
				toggle.setEnabled(false);
				Cursor c = dataStorage.getDay(Integer.parseInt(currentTime.format("%Y%m%d")));
				c.moveToFirst();
				Long start = c.getLong(c.getColumnIndex(DataStorage.C_WORK_START));
				Long finish = c.getLong(c.getColumnIndex(DataStorage.C_WORK_FINISH));
				Time t = new Time();
				t.set(start);				
				tv1.setText("Rozpoczęcie pracy: " + t.format("%H:%M:%S"));
				t.set(finish);
				tv2.setText("Zakończenie pracy: " + t.format("%H:%M:%S"));
				c.close();
				break;
			case 3:
				toggle.setEnabled(true);
				toggle.setText("Zakończ pracę");
				Cursor c1 = dataStorage.getDay(Integer.parseInt(currentTime.format("%Y%m%d")));
				c1.moveToFirst();
				Long start1 = c1.getLong(c1.getColumnIndex(DataStorage.C_WORK_START));
				Time t1 = new Time();
				t1.set(start1);				
				tv1.setText("Rozpoczęcie pracy: " + t1.format("%H:%M:%S"));
				c1.close();
				break;
		}
	}
	
	
	public void onClick(View v) {
		super.onClick(v);	
		if(v.getId() == R.id.buttonToggle) {
			currentTime.setToNow();
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

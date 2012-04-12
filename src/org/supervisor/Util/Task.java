package org.supervisor.Util;

import android.util.Log;

public class Task {
	
	private long id;
	private String name;
	private String description;
	private double latitude;
	private double longitude;
	private int state;
	private String creationTime;
	private String lastModified;
	private String finishTime;
	private String startTime;
	private int version;
	private String lastSynced;
	private String supervisor;
	
	public Task(long a0, String a1, String a2, double a3, double a4, int a5,
			String a6, String a7, String a8, String a9, int a10, String a11, String a12){
		
		id = a0;
		name = a1;
		description = a2;
		latitude = a3;
		longitude = a4;
		state = a5;
		creationTime = a6;
		lastModified = a7;
		finishTime = a8;
		startTime = a9;
		version = a10;
		lastSynced = a11;
		supervisor = a12;
		Log.d("TASK", startTime);
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public double[] getCoords() {
		return new double[]{latitude, longitude };
	}

	public int getState() {
		return state;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public String getLastModified() {
		return lastModified;
	}

	public String getFinishTime() {
		
		return finishTime;
	}

	public String getStartTime() {
		Log.d("TASK return", startTime);
		return startTime;
	}

	public int getVersion() {
		return version;
	}

	public String getLastSynced() {
		return lastSynced;
	}
	
	public String getSupervisor() {
		return supervisor;
	}
	
	
}

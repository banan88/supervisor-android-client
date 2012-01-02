package org.supervisor;

public class Task {
	long id;
	String name;
	double latitude;
	double longitude;
	int state;
	String creation_time;
	String last_modified;
	String finish_time;
	String start_time;
	int version;
	String last_synced;
	
	public Task(long a0, String a1, double a2, double a3, int a4,
			String a5, String a6, String a7, String a8, int a9, String a10){
		id = a0;
		name = a1;
		latitude = a2;
		longitude = a3;
		state = a4;
		creation_time = a5;
		last_modified = a6;
		finish_time = a7;
		start_time = a8;
		version = a9;
		last_synced = a10;
	}
	
}

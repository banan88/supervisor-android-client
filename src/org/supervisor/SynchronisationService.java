package org.supervisor;


import java.io.IOException;
import java.io.InputStream;

import java.util.Scanner;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;



public class SynchronisationService extends Service {

	private static final String TAG = SynchronisationService.class.getSimpleName();
	private SynchronisationThread thread;
	private int delay = 360000; //6min
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		thread = new SynchronisationThread();
		Log.d(TAG, "onCreate() called");
	}
	
	public void onDestroy() {
		super.onDestroy();
		thread.interrupt();
		thread = null;
		Log.d(TAG, "onDestroy() called");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(thread.isAlive()) { //forced synchronisation
			thread.interrupt();
			thread = new SynchronisationThread();
		} 
			thread.start();
		Log.d(TAG, "onStartCommand() called");
		return START_STICKY;
	}

	public String convertStreamToString(InputStream is) { 
	    return new Scanner(is).useDelimiter("\\A").next();
	}

	private class SynchronisationThread extends Thread {
		
		final String TAG = SynchronisationThread.class.getSimpleName();
		
		public void run() {
			Log.d(TAG, "run");
			try{
				while(true){
					HttpClient httpclient = new DefaultHttpClient();
					try{
						HttpGet get = new HttpGet("http://10.0.2.2/"); //127.0.0.1 used internally in android
						ResponseHandler<String> responseHandler = new BasicResponseHandler();
						String response = httpclient.execute(get, responseHandler);
						Log.d(TAG, response);
					}catch (IOException e){
						Log.d("TAG", e.getMessage());
					}
					finally {
						httpclient.getConnectionManager().shutdown();
					}
					sleep(delay);
				}
			}catch (InterruptedException e) {Log.d(TAG, "interrupted exception");}
		}
		
	}

}

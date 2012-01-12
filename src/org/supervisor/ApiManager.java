package org.supervisor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.util.Log;

public class ApiManager {

	private static final String TAG = ApiManager.class.getSimpleName();
	private static final int CONN_TIMEOUT = 3000;
	private static final int MAX_TOTAL_CONN = 10;
	private static final int MAX_CONN_PER_ROUTE = 10;
	private static UsernamePasswordCredentials credentials;
	private static HttpClient httpClient = clientFactory();
	public static String HOST;
	public static final int DONE_TASKS = 3;
	public static final int CURRENT_TASKS = 2;
	public static final int PENDING_TASKS = 1;
	
	public static void setCredentials(String username, String password) {
		credentials = new UsernamePasswordCredentials(username, password);
	}
	
	public static boolean testConnParameters() throws NetworkErrorException{ //should be dependant on network state
		HttpGet get = new HttpGet(HOST + "test/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		try {
			if (apiCall(get).equals("True"))
				return true;
			else
				return false;
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	private static DefaultHttpClient clientFactory() {
		
		SchemeRegistry sr = new SchemeRegistry();
		sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		
		BasicHttpParams params = new BasicHttpParams();
		ConnManagerParams.setTimeout(params, CONN_TIMEOUT);
		ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONN);
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(MAX_CONN_PER_ROUTE));
		
		ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(params,sr);
		DefaultHttpClient client = new DefaultHttpClient(mgr, params);
		return client;
	}
	
	private static String apiCall(HttpRequestBase method) throws NetworkErrorException{
		
		if (credentials == null) {
			return "call setCredentials(user, pass) first!";
		}
		String result = null;
		try {
			Log.d(TAG, HOST);
			HttpResponse response = httpClient.execute(method);
			int httpStatus = response.getStatusLine().getStatusCode();
		
			if (httpStatus != 200) {
				Log.d(TAG, Integer.toString(httpStatus));
					throw new NetworkErrorException(Integer.toString(httpStatus));
			}
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			if(entity != null)
				entity.consumeContent();
			try {
				result = convertStreamToString(stream);
			} catch (NoSuchElementException e) {
				Log.d(TAG, "no stream in response found"); 
			}
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
			throw new NetworkErrorException("404");
		}
		return result;
	}
	
	
	private static String convertStreamToString(InputStream is) throws NoSuchElementException{
		return new Scanner(is).useDelimiter("\\A").next();
	}
	
	
	private static ArrayList<Task> buildTaskList(String jsonString) {
		
		ArrayList<Task> tasks = new ArrayList<Task>();
		if (jsonString != null)
			try {
				JSONArray all = new JSONArray(jsonString);
				JSONObject single;
				for(int i=0; i < all.length(); ++i) {
					single = all.getJSONObject(i);
					tasks.add(new Task(
						single.getLong("pk"),
						single.getString("name"),
						single.getString("desc"),
						Double.parseDouble(single.getString("lat")),
						Double.parseDouble(single.getString("lon")),
						Integer.parseInt(single.getString("state")),
						single.getString("created"),
						single.getString("modified"),
						single.getString("finished"),
						single.getString("started"),
						Integer.parseInt(single.getString("ver")),
						single.getString("last_sync")
					));
				}
			} catch (JSONException e) {
				Log.d(TAG, e.getMessage());
			}
		return tasks;
	}
	
	private static HashMap<String, Long> buildUpdatesList(String jsonString) {
		HashMap<String, Long> map = new HashMap<String, Long>();
		if (jsonString != null)
			try {
				JSONArray array = new JSONArray(jsonString);
				JSONObject dict;
				for(int i=0; i < array.length(); ++i) {
					dict = array.getJSONObject(i);
					map.put(dict.getString("pk"), dict.getLong("version"));
				}
			} catch (JSONException e) {
				Log.d(TAG, e.getMessage());
			}
		return map;
	}
	
	
	public static ArrayList<Task> getTasks() throws NetworkErrorException {
		
		HttpGet get = new HttpGet(HOST + "get_tasks/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		return buildTaskList(apiCall(get));
	}
	
	
	public static ArrayList<Task> getTasks(int state) throws NetworkErrorException {
		if( state > 3 || state < 0)
			throw new IllegalArgumentException("illegal state");
		HttpGet get = new HttpGet(HOST + "get_tasks/" + Integer.toString(state) + "/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		return buildTaskList(apiCall(get));
	}
	
	
	public static ArrayList<Task> getTasksSince(String date) throws NetworkErrorException {
		// YYYY-MM-DD hh:mm:ss -format
		date = date.replace('"', ' ').trim();
		date = date.replace('-', 'X');
		date = date.replace(' ', 'X');
		date = date.replace(':', 'X');
		date = date.replace('.', 'X');
		HttpGet get = new HttpGet(HOST + "get_tasks_since/" + date + "/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		return buildTaskList(apiCall(get));
	}
	
	public static void changeTaskState(int task_id, int state) throws NetworkErrorException {
		HttpGet get = new HttpGet(HOST + "change_task_state/" + Integer.toString(task_id) +
				"/" + Integer.toString(state) + "/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		apiCall(get);
	}
	
	public static String getLastSyncTime() throws NetworkErrorException {
		HttpGet get = new HttpGet(HOST + "get_last_sync_time/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		return apiCall(get);
	}
	
	public static HashMap<String, Long> checkUpdates(int rowLimit) throws NetworkErrorException {
		HttpGet get = new HttpGet(HOST + "check_updates/" + Integer.toString(rowLimit) + "/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		return buildUpdatesList(apiCall(get));
	} 
	
	//public static taskFinished
}

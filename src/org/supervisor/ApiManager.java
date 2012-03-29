package org.supervisor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;



import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.database.Cursor;
import android.util.Log;

public class ApiManager {

	private static final String TAG = ApiManager.class.getSimpleName();
	private static final int CONN_TIMEOUT = 10000;
	private static final int MAX_TOTAL_CONN = 10;
	private static final int MAX_CONN_PER_ROUTE = 10;
	private static UsernamePasswordCredentials credentials;
	private static HttpClient httpClient = clientFactory();
	public static String HOST;
	public static final int DONE_TASKS = 3;
	public static final int CURRENT_TASKS = 2;
	public static final int PENDING_TASKS = 1;
	
	
	public static void setCredentials(String username, String password) {
		if (username != null && password != null) 
			credentials = new UsernamePasswordCredentials(username, password);
	}


	private static DefaultHttpClient clientFactory() {
		
		SchemeRegistry sr = new SchemeRegistry();
		sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONN_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, CONN_TIMEOUT);
		ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONN);
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(MAX_CONN_PER_ROUTE));
		
		ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(params,sr);
		DefaultHttpClient client = new DefaultHttpClient(mgr, params);
		return client;
	}
	
	
	private static String apiCall(HttpRequestBase method) throws NetworkErrorException{
		
		Log.d("apiCall()", method.getURI().toString());
		String response = null;
		method.setHeader("User-Agent", "ANDROID DEBUG DEVICE"); 
		try {
			
			try {
				try {
					response = httpClient.execute(method, new BasicResponseHandler());
				} catch (IllegalStateException e) {
					throw new NetworkErrorException("404");
				}
				Log.d(TAG, response);
			} catch (HttpResponseException e) {
				Log.d(TAG, Integer.toString(e.getStatusCode()));
				throw new NetworkErrorException(Integer.toString(e.getStatusCode()));
			}
			
		} catch (IOException e) {
			Log.d(TAG, "IO");
			throw new NetworkErrorException("404");
		}
		Log.d("odpowiedz: ", response);
		return response;
	}
	
	
	private static ArrayList<Task> buildTaskList(String jsonString) throws NetworkErrorException {
		Log.d(TAG, "buildTaskList()");
		ArrayList<Task> tasks = new ArrayList<Task>();
		Log.d("JSONstring:", "content: " + jsonString);
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
						single.getString("last_sync"),
						single.getString("supervisor")
					));
				}
			} catch (JSONException e) {
				Log.d("JSONException in buildTaskList()", e.getLocalizedMessage());
			}
		return tasks;
	}
	
	
	public static ArrayList<Task> getNTasks(int count) throws NetworkErrorException {
		Log.d(TAG, "getNTasks()");
		
		HttpGet get = new HttpGet(HOST + "get_n_tasks/" + Integer.toString(count) + "/");
		try {
			try {
				get.addHeader(new BasicScheme().authenticate(credentials, get));
			} catch (AuthenticationException e) {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			throw new NetworkErrorException("401");
		}
		return buildTaskList(apiCall(get));
	}
	
	/*
	public static int checkTaskStatesTableVersion() throws NetworkErrorException {
		Log.d(TAG, "called checkTasksTableVersion()");
		
		HttpGet get = new HttpGet(HOST + "get_task_state_table_version/");
		try {
			try {
				get.addHeader(new BasicScheme().authenticate(credentials, get));
			} catch (AuthenticationException e) {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			throw new NetworkErrorException("401");
		}
		String v = apiCall(get);  
		Log.d(TAG, "tasksTableVersion: " + v);
		return Integer.parseInt(v);
	}
	
	
	public static ArrayList<HashMap<String, Object>> getTaskStatesUpdates() throws NetworkErrorException {
		Log.d(TAG, "getTaskStatesUpdates()");
		
		HttpGet get = new HttpGet(HOST + "get_task_state_table_updates/");
		ArrayList<HashMap<String, Object>> updates = new ArrayList<HashMap<String,Object>>();
		try {
			try {
				get.addHeader(new BasicScheme().authenticate(credentials, get));
			} catch (AuthenticationException e) {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			throw new NetworkErrorException("401");
		}
		try {
			JSONArray states = new JSONArray(apiCall(get));
			JSONObject singleState;
			HashMap<String, Object> map = new HashMap<String, Object>();
			
			for (int i = 0 ; i < states.length(); ++i){
				singleState = states.getJSONObject(i);
				map.put(DataStorage.C_STATE_DESCRIPTION, singleState.getString("state_description"));
				map.put(DataStorage.C_IS_VISIBLE, singleState.getBoolean("is_displayed"));
				map.put(DataStorage.C_TASKS_ARE_ARCHIVED, singleState.getBoolean("tasks_are_archived"));
				map.put(DataStorage.C_CAN_BE_TOGGLED, singleState.getBoolean("can_be_toggled"));
				map.put(DataStorage.C_TOGGLED_FROM, singleState.getBoolean("toggled_from"));
				updates.add(map);
			}
		} catch (JSONException e) {
			Log.d("JSONException in buildTaskList()", e.getLocalizedMessage());
			throw new NetworkErrorException("500");
		}
		return updates;
	}
	*/
	
	
	
	public static ArrayList<Task> getTasksSinceLastSync() throws NetworkErrorException {
		try {
			HttpGet get = new HttpGet(HOST + "get_tasks_since_last_sync/");
			Log.d(TAG, "request to: " + HOST);
			try {
				try {
					get.addHeader(new BasicScheme().authenticate(credentials, get));
				} catch (AuthenticationException e) {
					Log.d(TAG, e.getMessage() + "lalal");
				}
			} catch (IllegalArgumentException e) {
				throw new NetworkErrorException("401");
			}
			return buildTaskList(apiCall(get));
		} catch (NullPointerException e) {
			throw new NetworkErrorException("404");
		}
	}
	
	
	public static boolean changeTasksStates(Cursor c) throws NetworkErrorException {
		
		if (c.getCount() == 0) 
			return false; //no need to reset nonsynced task states, there are none pending
		JSONArray pendingTasks = new JSONArray();
		StringEntity entity = null;
		JSONArray entry;
		
		while(c.moveToNext()) {
					entry = new JSONArray();
					entry.put(Long.toString(c.getLong(c.getColumnIndex(DataStorage.C_ID)))); 
					entry.put(c.getInt(c.getColumnIndex(DataStorage.C_STATE)));
					entry.put(c.getLong(c.getColumnIndex(DataStorage.C_START_TIME)));
					entry.put(c.getLong(c.getColumnIndex(DataStorage.C_FINISH_TIME)));
					pendingTasks.put(entry);
		}
		c.close();
		try {
			Log.d("JSON TIME FROM DATABASE start, finish", pendingTasks.toString());

			entity = new StringEntity(pendingTasks.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
		try {
			HttpPost post = new HttpPost(HOST + "change_tasks_states/");
			post.setEntity(entity);
			post.addHeader("Accept", "application/json");
			post.addHeader("Content-type", "application/json");
			try {
				post.addHeader(new BasicScheme().authenticate(credentials, post));
			} catch (AuthenticationException e) {
				Log.d(TAG, e.getMessage());
			}
			apiCall(post);
		} catch (NullPointerException e) {
			throw new NetworkErrorException("404");
		}
		return true;
	}
	
	
	
	public static boolean changeWorkTimes(Cursor c) throws NetworkErrorException {
		
		if (c.getCount() == 0) 
			return false; //no need to reset nonsynced work times, there are none pending
		JSONArray pendingTimes = new JSONArray();
		StringEntity entity = null;
		JSONArray entry;
		
		while(c.moveToNext()) {
					entry = new JSONArray();
					entry.put(Integer.toString(c.getInt(c.getColumnIndex(DataStorage.C_WORK_DATE)))); 
					entry.put(c.getLong(c.getColumnIndex(DataStorage.C_WORK_START)));
					entry.put(c.getLong(c.getColumnIndex(DataStorage.C_WORK_FINISH)));
					pendingTimes.put(entry);
		}
		c.close();
		try {
			Log.d("JSON TIME FROM DATABASE start, finish", pendingTimes.toString());

			entity = new StringEntity(pendingTimes.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
		try {
			HttpPost post = new HttpPost(HOST + "change_work_times/");
			post.setEntity(entity);
			post.addHeader("Accept", "application/json");
			post.addHeader("Content-type", "application/json");
			try {
				post.addHeader(new BasicScheme().authenticate(credentials, post));
			} catch (AuthenticationException e) {
				Log.d(TAG, e.getMessage());
			}
			apiCall(post);
		} catch (NullPointerException e) {
			throw new NetworkErrorException("404");
		}
		return true;
	}
	
	
public static boolean sendTasksHistory(Cursor c) throws NetworkErrorException {
		
		if (c.getCount() == 0) 
			return false; 
		JSONArray unsyncedHistory = new JSONArray();
		StringEntity entity = null;
		JSONArray entry;
		
		while(c.moveToNext()) {
			try{
					entry = new JSONArray();
					entry.put(c.getInt(c.getColumnIndex(DataStorage.C_TASK_REFERENCE)));
					entry.put(c.getInt(c.getColumnIndex(DataStorage.C_TASK_STATE_CHANGED_TO)));
					entry.put(c.getLong(c.getColumnIndex(DataStorage.C_CHANGE_TIME)));
					entry.put(c.getString(c.getColumnIndex(DataStorage.C_CHANGE_DESCRIPTION)));
					entry.put(c.getDouble(c.getColumnIndex(DataStorage.C_CHANGE_LATITUDE)));
					entry.put(c.getDouble(c.getColumnIndex(DataStorage.C_CHANGE_LONGITUDE)));
					unsyncedHistory.put(entry);
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}
		c.close();
		try {
			Log.d("JSON TIME FROM DATABASE start, finish", unsyncedHistory.toString());

			entity = new StringEntity(unsyncedHistory.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
		try {
			HttpPost post = new HttpPost(HOST + "new_tasks_history/");
			post.setEntity(entity);
			post.addHeader("Accept", "application/json");
			post.addHeader("Content-type", "application/json");
			try {
				post.addHeader(new BasicScheme().authenticate(credentials, post));
			} catch (AuthenticationException e) {
				Log.d(TAG, e.getMessage());
			}
			apiCall(post);
		} catch (NullPointerException e) {
			throw new NetworkErrorException("404");
		}
		return true;
	}


public static boolean sendUserLocations(Cursor c) throws NetworkErrorException {
	if (c.getCount() == 0) 
		return false; 
	JSONArray unsyncedLocations = new JSONArray();
	StringEntity entity = null;
	JSONArray entry;
	
	while(c.moveToNext()) {
		try{
				entry = new JSONArray();
				entry.put(c.getLong(c.getColumnIndex(DataStorage.C_TIMESTAMP)));
				entry.put(c.getDouble(c.getColumnIndex(DataStorage.C_LAT)));
				entry.put(c.getDouble(c.getColumnIndex(DataStorage.C_LON)));
				unsyncedLocations.put(entry);
		}catch (JSONException e) {
			e.printStackTrace();
		}
	}
	c.close();
	try {
		Log.d("JSON TIME FROM DATABASE start, finish", unsyncedLocations.toString());

		entity = new StringEntity(unsyncedLocations.toString(), "UTF-8");
	} catch (UnsupportedEncodingException e) {
		
	}
	try {
		HttpPost post = new HttpPost(HOST + "new_user_locations/");
		post.setEntity(entity);
		post.addHeader("Accept", "application/json");
		post.addHeader("Content-type", "application/json");
		try {
			post.addHeader(new BasicScheme().authenticate(credentials, post));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		apiCall(post);
	} catch (NullPointerException e) {
		throw new NetworkErrorException("404");
	}
	return true;
}
}

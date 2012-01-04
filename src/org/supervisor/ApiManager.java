package org.supervisor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ApiManager {

	private static final String TAG = ApiManager.class.getSimpleName();
	private static final String HOST = "http://10.0.2.2:80/";
	private static final int CONN_TIMEOUT = 3000;
	private static final int MAX_TOTAL_CONN = 10;
	private static final int MAX_CONN_PER_ROUTE = 10;
	private static UsernamePasswordCredentials credentials;
	private static HttpClient httpClient = clientFactory();
	public static final int DONE_TASKS = 3;
	public static final int CURRENT_TASKS = 2;
	public static final int PENDING_TASKS = 1;
	public static final int CANCELLED_TASKS = 0;
	
	public static void setCredentials(String username, String password) {
		credentials = new UsernamePasswordCredentials(username, password);
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
	
	private static String apiCall(HttpRequestBase method) {
		
		if (credentials == null) {
			return "call setCredentials(user, pass) first!";
		}
		String result = null;
		try {
		HttpResponse response = httpClient.execute(method);
		int httpStatus = response.getStatusLine().getStatusCode();
		
			if (httpStatus != 200) 
				throw new IOException(Integer.toString(httpStatus)); 
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			if(entity != null)
				entity.consumeContent();
			try {
				result = convertStreamToString(stream);
			} catch (NoSuchElementException e) {
				Log.d(TAG, e.getMessage()); 
			}
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			Log.d(TAG, Integer.toString(((ThreadSafeClientConnManager) httpClient.getConnectionManager()).getConnectionsInPool()));
			httpClient.getConnectionManager().closeExpiredConnections();
		}
		return result;
	}
	
	
	private static String convertStreamToString(InputStream is) throws NoSuchElementException{
		return new Scanner(is).useDelimiter("\\A").next();
	}
	
	
	public static synchronized ArrayList<Task> getTasks() {
		
		HttpGet get = new HttpGet(HOST + "get_tasks/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		ArrayList<Task> tasks = new ArrayList<Task>();
		try {
			JSONObject all = new JSONObject(apiCall(get));
			JSONObject single;
			String id;
			while(all.keys().hasNext()) { //here heap_size increases steadily
				id = all.keys().next().toString();
				single = all.getJSONObject(id);
				tasks.add(new Task(
					Long.parseLong(id),
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
	
	
	public static String getTasks(int state) {
		if ( state > 3 || state < 0 )
			return "illegal state value!";
		HttpGet get = new HttpGet(HOST + "get_tasks/" + Integer.toString(state) + "/");
		try {
			get.addHeader(new BasicScheme().authenticate(credentials, get));
		} catch (AuthenticationException e) {
			Log.d(TAG, e.getMessage());
		}
		return apiCall(get);
	}
}

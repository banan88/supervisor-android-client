package org.supervisor;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;

import android.util.Log;

public class ApiManager {
	//http://stackoverflow.com/questions/8200038/httpclient-4-x-connection-reuse-not-happening -- connection pool shut down
	private static final String TAG = ApiManager.class.getSimpleName();
	private static final BasicHttpParams params = new BasicHttpParams();
	private static final SchemeRegistry sr = new SchemeRegistry();
	private static final Scheme sh = sr.register(
            new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	private static HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params,sr), params);
	private static final String HOST = "http://10.0.2.2:80/";
	private static UsernamePasswordCredentials credentials;
	public static final int DONE_TASKS = 3;
	public static final int CURRENT_TASKS = 2;
	public static final int PENDING_TASKS = 1;
	public static final int CANCELLED_TASKS = 0;
	
	public static void setCredentials(String username, String password) {
		credentials = new UsernamePasswordCredentials(username, password);
	}
	
	public static String convertStreamToString(InputStream is) throws NoSuchElementException{
		return new Scanner(is).useDelimiter("\\A").next();
	}
	
	public static String getTasks() {
		if (credentials == null) {
			return "call setCredentials(user, pass) first!";
		}
		if (httpClient == null)
			httpClient = new DefaultHttpClient();
		String result = "error occured";
		try {
			HttpGet get = new HttpGet(HOST + "get_tasks/");
			try {
				get.addHeader(new BasicScheme().authenticate(credentials, get));
			} catch (AuthenticationException e) {
				Log.d(TAG, e.getMessage());
			}
			HttpResponse response = httpClient.execute(get);
			int httpStatus = response.getStatusLine().getStatusCode();
			if (httpStatus != 200) 
				throw new IOException(Integer.toString(httpStatus)); 
			InputStream stream = response.getEntity().getContent();
			try {
				result = convertStreamToString(stream);
			} catch (NoSuchElementException e) {
				Log.d(TAG, e.getMessage());
			}
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}
	
	public static String getTasks(int state) {
		if (credentials == null) {
			return "call setCredentials(user, pass) first!";
		}
		if ( state > 3 || state < 0 )
			return "illegal state value!";
		//if (httpClient == null) lazy creation causes illegalstateexception: manager is shut down
		httpClient.getConnectionManager();
		String result = "error occured";
		try {
			HttpGet get = new HttpGet(HOST + "get_tasks/" +
					Integer.toString(state) + "/");
			try {
				get.addHeader(new BasicScheme().authenticate(credentials, get));
			} catch (AuthenticationException e) {
				Log.d(TAG, e.getMessage());
			}
			HttpResponse response = httpClient.execute(get);
			int httpStatus = response.getStatusLine().getStatusCode();
			if (httpStatus != 200) 
				throw new IOException(Integer.toString(httpStatus)); 
			InputStream stream = response.getEntity().getContent();
			try {
				result = convertStreamToString(stream);
			} catch (NoSuchElementException e) {
				Log.d(TAG, e.getMessage());
			}
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}
}

package org.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		boolean connected = !(networkInfo == null || !networkInfo.isConnected());
		
		if(connected) {
			Log.d("netreceiver", "net receiver started service");
			context.startService(new Intent(context, SynchronisationService.class));
		}
		else {
			Log.d("netreceiver", "net receiver stopped service");
			context.stopService(new Intent(context, SynchronisationService.class));
		}
		
	}

}

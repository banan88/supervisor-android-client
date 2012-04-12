package org.supervisor.Util;

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
			SupervisorApplication global_app = (SupervisorApplication) context.getApplicationContext();
			if(!global_app.getSyncPeriod().equals("5"))
				if(!global_app.wasLastSyncSuccessful())
					global_app.reloadAlarm(); //reload alarm only if last sync was unsuccessfull - probably because of down network
		}
		
	}

}

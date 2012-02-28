package org.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SyncRequestReceiver extends BroadcastReceiver {

	 // onReceive must be very quick and not block, so it just fires up a Service
	   @Override
	   public void onReceive(Context context, Intent intent) {
		  WakeLocker.acquire(context);
	      Log.i("syncrequestreceiver", "SyncRecuestReceiver invoked, starting SynchronisationService in background");
	      context.startService(new Intent(context, SynchronisationService.class));
	   }

}

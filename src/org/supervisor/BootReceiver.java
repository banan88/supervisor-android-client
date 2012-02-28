package org.supervisor;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		
			Log.d("bootreceiver", "phone booted - boot signal");
			SupervisorApplication global_app = (SupervisorApplication) context.getApplicationContext();
			global_app.reloadAlarm();
	}
	
	
}

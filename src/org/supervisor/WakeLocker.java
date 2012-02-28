package org.supervisor;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public abstract class WakeLocker {
private static PowerManager.WakeLock wakeLock;


	public static void acquire(Context ctx) {
		Log.d("WakeLocker", "acquire() called");
	    if (wakeLock != null) wakeLock.release();
	    PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | 
	        		PowerManager.ACQUIRE_CAUSES_WAKEUP |
	                PowerManager.ON_AFTER_RELEASE, "Supervisor app");
	        wakeLock.acquire();
	    }

	
    public static void release() {
    	Log.d("WakeLocker", "release() called");
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }
}
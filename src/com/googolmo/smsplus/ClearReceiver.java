/**
 * 
 */
package com.googolmo.smsplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author GoogolMo
 * 
 */
public class ClearReceiver extends BroadcastReceiver {

	/**
	 * 
	 */
	public ClearReceiver() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("ClearReceiver-------->onReceive()");
		clearAll(true);

	}

	public static synchronized void clearAll(boolean reenableKeyguard) {
		Log.i("ClearReceiver-------->clearAll(" + reenableKeyguard + ")");
		WakeLockManager.releaseAll();
		if (reenableKeyguard == true) {
			ManageKeyguard.reenableKeyguard();
		}

	}

	private static PendingIntent getPendingIntent(Context context) {
		return PendingIntent.getBroadcast(context, 0, new Intent(context,
				ClearReceiver.class), 0);
	}

	public static synchronized void removeCancel(Context context) {
		Log.i("ClearReceiver------->removeCancel");
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(getPendingIntent(context));
	}

	public static synchronized void setCancel(Context context, int timeout) {
		removeCancel(context);
		Log.i("ClearReceiver------->setCancel,setAlarm=" + timeout + "seconds");
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		// alarmManager.cancel(getPendingIntent(context));
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ (timeout * 1000), getPendingIntent(context));
	}

}

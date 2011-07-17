/**
 * 
 */
package com.googolmo.smsplus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * @author GoogolMo
 * 
 */
public class SMSPlusUtilsService extends Service {
	private static final Object mStartingServiceSync = new Object();
	private static PowerManager.WakeLock mStartingService;
	public static final String ACTION_DELETE_MESSAGE = "com.googolmo.smsplus.ACTION_DELETE_MESSAGE";

	/**
	 * 
	 */
	public SMSPlusUtilsService() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void startingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			Log.i("SMSPlusUtilsService--------->startingService");
			if (mStartingService == null) {
				PowerManager pm = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, Log.LOGTAG
								+ "SMSPlusUtilsService");
				mStartingService.setReferenceCounted(false);
			}
			mStartingService.acquire();
			context.startService(intent);
		}
	}

	public static void finishService(Service service, int startID) {
		synchronized (mStartingServiceSync) {
			Log.i("SMSPlusUtilsService-------->finishService");
			if (mStartingService != null) {
				if (service.stopSelfResult(startID)) {
					mStartingService.release();
				}
			}
		}
	}

}

/**
 * 
 */
package com.googolmo.smsplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

/**
 * @author GoogolMo
 * 
 */
public class WakeLockManager {

	private static PowerManager.WakeLock mWakeLock = null;
	private static PowerManager.WakeLock mPartialWakeLock = null;
	private static final boolean PREFS_SCREENON_DEFAULT = true;
	private static final String PREFS_TIMEOUT_DEFAULT = "30";

	public static synchronized void acquireFull(Context _context) {
		if (mWakeLock != null) {
			if (Log.ISDEBUG)
				Log.d("wakelock is held");
			return;
		}
		PowerManager powerManager = (PowerManager) _context
				.getSystemService(Context.POWER_SERVICE);
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(_context);

		int flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK;

		if (sharedPreferences.getBoolean(
				_context.getString(R.string.pref_screen_on_key),
				PREFS_SCREENON_DEFAULT)) {
			flags |= PowerManager.ACQUIRE_CAUSES_WAKEUP;
			ManageKeyguard.disableKeyguard(_context);
		}
		mWakeLock = powerManager.newWakeLock(flags, Log.LOGTAG + ".full");
		if (Log.ISDEBUG)
			Log.d("flags=" + flags);
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();
		Log.i("WakeLockManage------------>acquireFull:wakelocked");

		int timeout = Integer.valueOf(sharedPreferences.getString(
				_context.getString(R.string.pref_timeout_key),
				PREFS_TIMEOUT_DEFAULT));

		ClearReceiver.setCancel(_context, timeout);

	}

	public static synchronized void acquirePartial(Context mContext) {
		if (mPartialWakeLock != null)
			return;
		PowerManager powerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mPartialWakeLock = powerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.FULL_WAKE_LOCK, Log.LOGTAG + "partial");
		Log.i("WakeLockManager----->acquirePartial");
		mPartialWakeLock.setReferenceCounted(false);
		mPartialWakeLock.acquire();

	}

	public static synchronized void releaseFull() {
		if (mWakeLock != null) {
			Log.i("WakeLockManager-------->releaseFull");
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	public static synchronized void releaseAll() {
		releaseFull();
		releasePartial();
	}

	public static synchronized void releasePartial() {
		if (mPartialWakeLock != null) {
			Log.i("WakeLockManager------>releasePartial");
			mPartialWakeLock.release();
			mPartialWakeLock = null;
		}
	}

	/**
	 * 
	 */
	public WakeLockManager() {
		// TODO Auto-generated constructor stub
	}

}

/**
 * 
 */
package com.googolmo.smsplus;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager.OnKeyguardExitResult;
import android.content.Context;

/**
 * @author GoogolMo
 * 
 */
public class ManageKeyguard {

	private static KeyguardManager kManager = null;
	private static KeyguardLock kLock = null;

	public static synchronized void initialize(Context context) {
		if (kManager == null) {
			kManager = (KeyguardManager) context
					.getSystemService(Context.KEYGUARD_SERVICE);
		}
	}

	public static synchronized void disableKeyguard(Context context) {
		// 解锁
		initialize(context);
		if (kManager.inKeyguardRestrictedInputMode()) {
			kLock = kManager.newKeyguardLock(Log.LOGTAG);
			kLock.disableKeyguard();
			Log.i("KeyGuard disable");
		} else {
			kLock = null;
		}
	}

	public static synchronized void reenableKeyguard() {
		if (kManager != null) {
			if (kLock != null) {
				kLock.reenableKeyguard();
				kLock = null;
				Log.i("Keyguard reenabled");
			}
		}
	}

	public static synchronized boolean inKeyguardRestrictedInputMode() {
		if (kManager != null) {
			if (Log.ISDEBUG)
				Log.d("--inKeyguardRestrictedInputMode = "
						+ kManager.inKeyguardRestrictedInputMode());
			return kManager.inKeyguardRestrictedInputMode();
		}
		return false;
	}

	public static synchronized void exitKeyguardSecurely(
			final LaunchOnKeyguardExit callback) {
		if (inKeyguardRestrictedInputMode()) {
			if (Log.ISDEBUG)
				Log.d("--Trying to exit keyguard securely");
			kManager.exitKeyguardSecurely(new OnKeyguardExitResult() {
				public void onKeyguardExitResult(boolean success) {
					reenableKeyguard();
					if (success) {
						if (Log.ISDEBUG)
							Log.d("--Keyguard exited securely");
						callback.LaunchOnKeyguardExitSuccess();
					} else {
						if (Log.ISDEBUG)
							Log.d("--Keyguard exit failed");
					}
				}
			});
		} else {
			callback.LaunchOnKeyguardExitSuccess();
		}
	}

	public interface LaunchOnKeyguardExit {
		public void LaunchOnKeyguardExitSuccess();
	}

	/**
	 * 
	 */
	public ManageKeyguard() {
		// TODO Auto-generated constructor stub
	}

}

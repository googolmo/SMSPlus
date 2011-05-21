/**
 * 
 */
package com.googolmo.smsplus;

/**
 * @author GoogolMo
 * 
 */
public class Log {

	public final static String LOGTAG = "SMSPlus";
	public final static Boolean ISDEBUG = true;

	public static void i(String msg) {
		android.util.Log.i(LOGTAG, msg);
	}

	public static void d(String msg) {
		android.util.Log.d(LOGTAG, msg);
	}

	public static void e(String msg) {
		android.util.Log.e(LOGTAG, msg);
	}

}

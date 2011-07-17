/**
 * 
 */
package com.googolmo.smsplus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import android.os.Process;

/**
 * @author GoogolMo
 * 
 */
public class SMSReceiverService extends Service {

	private static final Object mStartingServiceSync = new Object();
	private static PowerManager.WakeLock mStartingService;

	private Context context;
	private ServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	private int mResultCode;

	/**
	 * 
	 */
	public SMSReceiverService() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.i("SMSReceiverService--------->onCreate()");
		// Toast.makeText(getApplicationContext(), "创建服务", Toast.LENGTH_LONG)
		// .show();
		HandlerThread thread = new HandlerThread(Log.LOGTAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public void onStart(Intent intent, int startID) {
		Log.i("SMSReceiverService--------->onStart()");
		// Toast.makeText(getApplicationContext(), "启动服务", Toast.LENGTH_LONG)
		// .show();
		if (context != null) {
			mResultCode = intent.getIntExtra("result", 0);
		} else {
			mResultCode = 0;
		}
		Log.d("SMSReceiverService--------->mResultCode==" + mResultCode);
		Message message = mServiceHandler.obtainMessage();
		message.arg1 = startID;
		message.obj = intent;
		mServiceHandler.sendMessage(message);
	}

	@Override
	public void onDestroy() {
		Log.i("SMSReceiverService------------>onDestroy()");
		mServiceLooper.quit();
	}

	public static void startingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			Log.i("startingService()");
			if (mStartingService == null) {
				PowerManager pManager = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				mStartingService = pManager.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, Log.LOGTAG
								+ "SMSReceiverService");
				mStartingService.setReferenceCounted(false);
				Log.i("if mstartingservice==null");
			}
			mStartingService.acquire();
			context.startService(intent);
		}
	}

	public static void finalService(Service service, int startId) {
		Log.i("SMSReceiverService------->finalService");
		if (mStartingService != null) {
			if (service.stopSelfResult(startId)) {
				mStartingService.release();
			}
		}
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i("SMSReceiverService----------->handleMessage()");

			int serviceID = msg.arg1;
			Intent intent = (Intent) msg.obj;
			// handleSMSReceived(intent);
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				SmsMessage[] message = SMSPlusUtils
						.getMessagesFromIntent(intent);
				handleSMS(message);
			}
			finalService(SMSReceiverService.this, serviceID);

		}
	}

	// private void handleSMSReceived(Intent intent) {
	// Bundle bundle = intent.getExtras();
	// if (bundle != null) {
	// SmsMessage[] message = SMSPlusUtils.getMessagesFromIntent(intent);
	// handleSMS(message);
	// }
	//
	// }

	private void handleSMS(SmsMessage[] messages) {

		TelephonyManager tManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		boolean callState = tManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;

		if (callState == false) {
			// 正在打电话
			Log.i("SMSReceiverService---------->handleSMS:isCalling Do not show popup");
		} else {
			SMSMessage m = new SMSMessage(context, messages,
					System.currentTimeMillis());
			Log.i("SMSReceiverService---------->handleSMS: show popup");
			WakeLockManager.acquireFull(context);// 唤醒屏幕
			// String msg = "SMS From:" + messages[0].getOriginatingAddress()
			// + "/n SMS Body:" + messages[0].getDisplayMessageBody()
			// + "/n Time:" + messages[0].getTimestampMillis();
			// Log.i(msg);
			// Toast.makeText(context, msg, 1500000).show();
			context.startActivity(m.getPopupIntent());
		}

	}

}

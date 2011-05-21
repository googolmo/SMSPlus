/**
 * 
 */
package com.googolmo.smsplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author GoogolMo
 * 
 */
public class SMSReceiver extends BroadcastReceiver {

	/**
	 * 
	 */
	public SMSReceiver() {
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
		Log.i("SMSReceiver------->onReceiver");
		intent.setClass(context, SMSReceiverService.class);
		Log.i("SMSReceiver------->getResultCode=" + getResultCode());
		intent.putExtra("result", getResultCode());

		// 启动服务
		SMSReceiverService.startingService(context, intent);

	}

}

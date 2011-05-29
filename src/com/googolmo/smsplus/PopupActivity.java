/**
 * 
 */
package com.googolmo.smsplus;

import com.googolmo.smsplus.ManageKeyguard.LaunchOnKeyguardExit;
import com.googolmo.smsplus.views.PopupViewFlipper;
import com.googolmo.smsplus.views.PopupViewFlipper.MessageCountChanged;

import android.app.Activity;
import android.app.NotificationManager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.Display;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * @author GoogolMo
 * 
 */
public class PopupActivity extends Activity {

	private LinearLayout mainLayout = null;
	private ViewSwitcher buttonSwitcher = null;
	private PopupViewFlipper popupViewFlipper = null;

	private static final int MAX_WIDTH = 640;
	private static final double WIDTH = 0.9;
	public static final String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
	private static final int NOTIFICATION_ID = 123;

	private boolean wasVisible = false;
	private boolean exitingKeyguard = false;

	private Bundle bundle = null;

	/**
	 * 
	 */
	public PopupActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("PopupActivity----->onCreate");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup);

		initPreferences();
		initViews();

		if (savedInstanceState == null) {
			setMessages(getIntent().getExtras(), false);
		} else {
			setMessages(savedInstanceState, false);
		}
		wakeUp();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.i("PopupActivity------------->onConfigurationChanged");
		resizeLayout();
	}

	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// // TODO Auto-generated method stub
	// return super.onContextItemSelected(item);
	// }
	//
	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v,
	// ContextMenuInfo menuInfo) {
	// // TODO Auto-generated method stub
	// super.onCreateContextMenu(menu, v, menuInfo);
	// // 创建右键菜单..
	// }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("PopupActivity------->onDestroy");
		// 删除信息
		popupViewFlipper.removeActiveMessage();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.i("PopupActivity------->onNewIntent");

		// 更新intent
		setIntent(intent);
		// 设置信息
		setMessages(intent.getExtras(), true);
		wakeUp();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i("PopupActivity--------->onPause");
		if (wasVisible) {
			ClearReceiver.removeCancel(getApplicationContext());
			ClearReceiver.clearAll(!exitingKeyguard);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("PopupActivity-------->onResume");
		wasVisible = false;
		exitingKeyguard = false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i("PopupActivity--------->onStop");

		ClearReceiver.removeCancel(getApplicationContext());
		ClearReceiver.clearAll(!exitingKeyguard);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		Log.i("PopupActivity------------>onWindowFocusChanged(" + hasFocus
				+ ")");
		if (hasFocus) {
			wasVisible = true;
			refreshViews();
		}

	}

	// 初始化配置
	private void initPreferences() {
		// 设置屏幕旋转
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	// 初始化视图
	private void initViews() {
		popupViewFlipper = (PopupViewFlipper) findViewById(R.id.SmsPopupsLayout);
		mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
		buttonSwitcher = (ViewSwitcher) findViewById(R.id.ButtonViewSwitcher);

		final Button previousButton = (Button) findViewById(R.id.PreviousButton);
		final Button nextButton = (Button) findViewById(R.id.NextButton);
		final Button inboxButton = (Button) findViewById(R.id.InboxButton);
		// exitingKeyguard = false;

		previousButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popupViewFlipper.showPrevious();
			}
		});

		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popupViewFlipper.showNext();
			}
		});

		inboxButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setType(SMS_MIME_TYPE);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				// 解开键盘锁
				unlock();
				getApplicationContext().startActivity(intent);
				finish();
			}
		});

		// TODO
		popupViewFlipper.setOnMessageCountChanged(new MessageCountChanged() {

			@Override
			public void onChanged(int current, int total) {
				// TODO Auto-generated method stub
				inboxButton.setText((current + 1) + "/" + (total));
				boolean previous = true;
				boolean next = true;
				if (current == 0)
					previous = false;
				if (current == (total - 1)) {
					next = false;
				}
				previousButton.setEnabled(previous);
				nextButton.setEnabled(next);
			}
		});

		// Button1
		final Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wasVisible = true;
				exitingKeyguard = false;
				closeMessages();
			}
		});

		// 标记为已读
		final Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				markAsRead();
			}
		});
		// button2.setVisibility(View.GONE);

		// 回复操作按钮
		final Button button3 = (Button) findViewById(R.id.button3);
		button3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wasVisible = false;
				exitingKeyguard = true;
				unlock();
				Intent i = SMSPlusUtils.getReplyIntent(getApplicationContext(),
						popupViewFlipper.getActiveMessage());
				getApplicationContext().startActivity(i);
				// finish();
			}
		});
		refreshViews();
		resizeLayout();
	}

	/*
	 * 
	 */

	private void refreshViews() {
		buttonSwitcher.setDisplayedChild(R.id.ButtonViewSwitcher);
	}

	private void resizeLayout() {
		Display d = getWindowManager().getDefaultDisplay();
		int width;
		if (d.getWidth() > MAX_WIDTH) {
			width = MAX_WIDTH;
		} else {
			width = (int) (d.getWidth() * WIDTH);
		}
		if (Log.ISDEBUG)
			Log.d("width=" + width);
		mainLayout.setMinimumWidth(width);
		mainLayout.invalidate();
	}

	private void setMessages(Bundle b, boolean newIntent) {
		bundle = b;
		SMSMessage smsMessage = new SMSMessage(getApplicationContext(), bundle);
		popupViewFlipper.addMessage(smsMessage);
		if (!newIntent) {
			popupViewFlipper.addMessages(SMSPlusUtils.getUnreadMessages(this,
					smsMessage.getMessageID()));
		}
	}

	private void wakeUp() {
		WakeLockManager.acquireFull(getApplicationContext());
		WakeLockManager.releasePartial();
	}

	// private void removeActiveMessage() {
	// if (popupViewFlipper.removeActiveMessage()) {
	// finish();
	// } else {
	// // 提醒
	// Toast.makeText(getApplicationContext(),
	// getString(R.string.delete_message_failed_text),
	// Toast.LENGTH_LONG).show();
	// }
	// }

	private void closeMessages() {
		// removeActiveMessage();
		ClearReceiver.clearAll(true);
		finish();
	}

	private void unlock() {
		exitingKeyguard = true;

		ManageKeyguard.exitKeyguardSecurely(new LaunchOnKeyguardExit() {
			@Override
			public void LaunchOnKeyguardExitSuccess() {
			}
		});
	}

	/*
	 * 标记为已读
	 */
	private void markAsRead() {
		// TODO
		if (SMSPlusUtils.markAsReadBySMSMessage(getApplicationContext(),
				popupViewFlipper.getActiveMessage()) == 0) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.error_markasreadfailed),
					Toast.LENGTH_LONG).show();
		} else {
			// TODO
			// 删除当前的显示信息
			if (popupViewFlipper.removeActiveMessage() == true) {
				NotificationManager notificationManager = (NotificationManager) getApplicationContext()
						.getSystemService(NOTIFICATION_SERVICE);
				// notificationManager.cancel(NOTIFICATION_ID);
				notificationManager.cancelAll();

				finish();
			}
			if (Log.ISDEBUG)
				Log.d("totalMessage=" + popupViewFlipper.getTotalMessage());

			// popupViewFlipper.showNext();

		}
		// SMSMessage message = popupViewFlipper.getActiveMessage();
	}

}

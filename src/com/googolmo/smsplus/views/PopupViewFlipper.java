/**
 * 
 */
package com.googolmo.smsplus.views;

import java.util.ArrayList;

import com.googolmo.smsplus.Log;
import com.googolmo.smsplus.SMSMessage;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ViewFlipper;

/**
 * @author GoogolMo
 * 
 */
public class PopupViewFlipper extends ViewFlipper {

	private Context context;

	private ArrayList<SMSMessage> messages;
	private int currunMessage;
	private int totalMessage;

	private MessageCountChanged messageCountChanged;
	float firstTouchValue = 0;

	private GestureDetector gestureDetector;

	public PopupViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public PopupViewFlipper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	@Override
	public void showNext() {
		// TODO Auto-generated method stub

		if (currunMessage < totalMessage - 1) {
			currunMessage += 1;
			setInAnimation(AnimationHelper.inFromRightAnimation());
			setOutAnimation(AnimationHelper.outToLeftAnimation());
			super.showNext();
			UpdateMessageCount();
			if (Log.ISDEBUG)
				Log.d("PopupViewFlipper--->showNext: currentMessage="
						+ getActiveMessage().getContactName());
		}
	}

	/**
	 * @return the totalMessage
	 */
	public int getTotalMessage() {
		return totalMessage;
	}

	/**
	 * @param totalMessage
	 *            the totalMessage to set
	 */
	public void setTotalMessage(int totalMessage) {
		this.totalMessage = totalMessage;
	}

	@Override
	public void showPrevious() {
		// TODO Auto-generated method stub
		if (currunMessage > 0) {
			currunMessage -= 1;
			setInAnimation(AnimationHelper.inFromLeftAnimation());
			setOutAnimation(AnimationHelper.outToRightAnimation());
			super.showPrevious();
			UpdateMessageCount();
			if (Log.ISDEBUG)
				Log.d("PopupViewFlipper----->showPrevious: currentMessage="
						+ getActiveMessage().getContactName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ViewAnimator#removeView(android.view.View)
	 */
	@Override
	public void removeView(View view) {
		// TODO Auto-generated method stub
		super.removeView(view);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		// switch (event.getAction()) {
		// case MotionEvent.ACTION_MOVE:
		// if (Log.ISDEBUG)
		// Log.d("ACTION_MOVE");
		// // if (firstTouchValue == 0) {
		// // firstTouchValue = event.getX();
		// // }
		//
		// // if (Log.ISDEBUG)
		// Log.d("enevet.getx()=" + event.getX() + ";firstTouchValue="
		// + firstTouchValue);
		// if ((event.getX() - firstTouchValue) > (this.getWidth() / 2)) {
		// showPrevious();
		// firstTouchValue = event.getX();
		// } else if ((event.getX() - firstTouchValue) < -(this.getWidth() / 2))
		// {
		// showNext();
		// firstTouchValue = event.getX();
		//
		// }
		// // if(event.getX()>oldTouchValue){
		// // showPrevious();
		// // }else if(event.getX()<oldTouchValue){
		// // showNext();
		// // }
		// // oldTouchValue = event.getX();
		// break;
		// case MotionEvent.ACTION_DOWN:
		// if (Log.ISDEBUG)
		// Log.d("action_down");
		// firstTouchValue = event.getX();
		// break;
		// case MotionEvent.ACTION_UP:
		// if (Log.ISDEBUG)
		// Log.d("ACTION_UP");
		// firstTouchValue = 0;
		// break;
		// }
		// return super.onTouchEvent(event);
		return onTouchListener.onTouch(this, event);
	}

	private void init(Context c) {
		context = c;
		messages = new ArrayList<SMSMessage>(5);
		totalMessage = 0;
		currunMessage = 0;
		gestureDetector = new GestureDetector(new CommanGestureListener());
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event){
	// if (Log.ISDEBUG)
	// Log.d("onTouchEvent");
	// return gestureDetector.onTouchEvent(event);
	// }

	private View.OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (Log.ISDEBUG)
				Log.d("OnTouchListener--->onTouch");
			return gestureDetector.onTouchEvent(event);
		}
	};

	public void setOnMessageCountChanged(MessageCountChanged m) {
		messageCountChanged = m;
	}

	public static interface MessageCountChanged {
		abstract void onChanged(int current, int total);
	}

	public void addMessage(SMSMessage newMessage) {

		messages.add(newMessage);
		SMSPopupView spView = new SMSPopupView(context, newMessage,
				onTouchListener, gestureDetector);
		// spView.setOnTouchListener(onTouchListener);
		// spView.setGestureDetector(gestureDetector);
		addView(spView);

		totalMessage = messages.size();

		UpdateMessageCount();
	}

	private void UpdateMessageCount() {
		if (messageCountChanged != null) {
			messageCountChanged.onChanged(currunMessage, totalMessage);
		}
	}

	public void addMessages(ArrayList<SMSMessage> unreadMessages) {
		// TODO Auto-generated method stub
		if (unreadMessages != null) {
			for (int i = 0; i < unreadMessages.size(); i++) {
				SMSPopupView spView = new SMSPopupView(context,
						unreadMessages.get(i), onTouchListener, gestureDetector);
				// spView.setOnTouchListener(onTouchListener);
				// spView.setGestureDetector(gestureDetector);
				addView(spView);
			}
			messages.addAll(unreadMessages);
			totalMessage = messages.size();
			UpdateMessageCount();
		}

	}

	public SMSMessage getActiveMessage() {
		return messages.get(currunMessage);
	}

	public boolean removeActiveMessage() {
		boolean flag = removeMessage(currunMessage);
		// UpdateMessageCount();
		Log.i("PopupViewFlipper---->removeActiveMessage");
		return flag;
	}

	public boolean removeMessage(int _messageID) {
		// Log.d("messageID=" + _messageID + ";totalMessage=" + totalMessage);
		if (_messageID < totalMessage && _messageID >= 0 && totalMessage > 0) {
			setOutAnimation(context, android.R.anim.fade_out);

			if (_messageID == (totalMessage - 1)) {
				setInAnimation(AnimationHelper.inFromLeftAnimation());
			} else {
				setInAnimation(AnimationHelper.inFromRightAnimation());
			}

			removeViewAt(_messageID);
			messages.remove(_messageID);
			totalMessage = messages.size();
			if (currunMessage >= totalMessage) {
				currunMessage = totalMessage - 1;
			}
			// if (Log.ISDEBUG)
			// Log.d("totalMessage=" + totalMessage);
			UpdateMessageCount();
			if (totalMessage > 0) {
				if (Log.ISDEBUG)
					// Log.d("totalMessage=" + totalMessage);
					return false;
			}
		}
		return true;
	}

	public class CommanGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Log.ISDEBUG)
				Log.d("onFling");
			if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 50) {
				showNext();
			} else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 50) {
				showPrevious();
			}
			return true;
		}
	}

	private static class AnimationHelper {
		public static Animation inFromRightAnimation() {
			Animation inFromRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromRight.setDuration(350);
			inFromRight.setInterpolator(new AccelerateInterpolator());
			return inFromRight;
		}

		public static Animation outToLeftAnimation() {
			Animation outtoLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoLeft.setDuration(350);
			outtoLeft.setInterpolator(new AccelerateInterpolator());
			return outtoLeft;
		}

		public static Animation inFromLeftAnimation() {
			Animation inFromLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromLeft.setDuration(350);
			inFromLeft.setInterpolator(new AccelerateInterpolator());
			return inFromLeft;
		}

		public static Animation outToRightAnimation() {
			Animation outtoRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoRight.setDuration(350);
			outtoRight.setInterpolator(new AccelerateInterpolator());
			return outtoRight;
		}
	}

}

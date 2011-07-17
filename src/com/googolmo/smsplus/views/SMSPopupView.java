/**
 * 
 */
package com.googolmo.smsplus.views;

import com.googolmo.smsplus.Log;
import com.googolmo.smsplus.R;
import com.googolmo.smsplus.SMSMessage;
import com.googolmo.smsplus.SMSPlusUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author GoogolMo
 * 
 */
public class SMSPopupView extends LinearLayout {

	private SMSMessage message;
	private Context context;
	// private boolean messageViewed = false;

	private TextView fromTextView;
	private TextView messageTextView;

	private TextView headerTextView;

	private FirendlyScrollView messageScrollView;

	private ImageView fromImageView;

	private Drawable contactPhotoPlaceholderDrawable = null;
	private Bitmap contactPhoto = null;
	private static int contactPhotoMargin = 3;
	private static int contactPhotoDefaultMargin = 10;

	private View.OnTouchListener onTouchListener;

	private GestureDetector gestureDetector;

	/**
	 * @return the gestureDetector
	 */
	public GestureDetector getGestureDetector() {
		return gestureDetector;
	}

	/**
	 * @param gestureDetector
	 *            the gestureDetector to set
	 */
	public void setGestureDetector(GestureDetector gestureDetector) {
		this.gestureDetector = gestureDetector;
	}

	/**
	 * @return the onTouchListener
	 */
	public View.OnTouchListener getOnTouchListener() {
		return onTouchListener;
	}

	/**
	 * @param onTouchListener
	 *            the onTouchListener to set
	 */
	public void setOnTouchListener(View.OnTouchListener onTouchListener) {
		this.onTouchListener = onTouchListener;
	}

	/**
	 * @param context
	 */
	public SMSPopupView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public SMSPopupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		setLayout(context);
	}

	public SMSPopupView(Context context, SMSMessage message) {
		super(context);
		// TODO
		this.context = context;
		this.message = message;
		setLayout(this.context);
		setMessages(this.message);
	}

	public SMSPopupView(Context context, SMSMessage message,
			OnTouchListener onTouchListener, GestureDetector gestureDetector) {
		super(context);
		// TODO
		this.context = context;
		this.message = message;
		this.onTouchListener = onTouchListener;
		this.gestureDetector = gestureDetector;
		setLayout(this.context);
		setMessages(this.message);
	}

	private void setLayout(Context context) {
		// TODO
		View.inflate(context, R.layout.message, this);

		fromTextView = (TextView) findViewById(R.id.FromTextView);
		messageTextView = (TextView) findViewById(R.id.MessageTextView);
		headerTextView = (TextView) findViewById(R.id.HeaderTextView);
		messageScrollView = (FirendlyScrollView) findViewById(R.id.MessageScrollView);
		fromImageView = (ImageView) findViewById(R.id.FromImageView);
		// messageTextView.setText("New Text");
		messageScrollView.setVisibility(View.VISIBLE);
		messageTextView.setVisibility(View.VISIBLE);
		loadContactPhoto();
		messageScrollView.setOnTouchListener(onTouchListener);
		messageScrollView.setGestureDetector(gestureDetector);
	}

	private void setMessages(SMSMessage message) {
		// TODO
		if (message != null && this.message != null) {
			if (!TextUtils.equals(this.message.getFromAddress(),
					message.getFromAddress())) {
				contactPhoto = null;
			}
		}
		// messageViewed = false;
		this.message = message;
		String headerText = context.getString(R.string.new_text_at,
				this.message.getFormatTimeStamp());

		fromTextView.setText(this.message.getContactName());
		messageTextView.setText(this.message.getMessageBody());
		headerTextView.setText(headerText);
	}

	private void loadContactPhoto() {
		if (contactPhoto == null) {
			// TODO
			// setContactPhotoDefault(fromImageView);
			new FetchContactPhotoTast().execute(message.getContactID());
		}
	}

	private void setContactPhotoDefault(ImageView contactPhoto) {
		contactPhoto.setBackgroundResource(0);
		contactPhoto.setPadding(0, 0, 0, 0);

		MarginLayoutParams mlp = (MarginLayoutParams) contactPhoto
				.getLayoutParams();
		final int scaleMargin = (int) (contactPhotoDefaultMargin * this
				.getResources().getDisplayMetrics().density);

		mlp.setMargins(scaleMargin, scaleMargin, scaleMargin, scaleMargin);

		contactPhoto.setLayoutParams(mlp);
		contactPhoto.setImageDrawable(contactPhotoPlaceholderDrawable);
	}

	private void setContactPhoto(ImageView photoImageView, Bitmap contactPhoto) {
		if (contactPhoto == null) {
			setContactPhotoDefault(photoImageView);
			return;
		}
		photoImageView
				.setBackgroundResource(R.drawable.quickcontact_badge_small);
		// Set margins for image
		MarginLayoutParams mLP = (MarginLayoutParams) photoImageView
				.getLayoutParams();
		final int scaledMargin = (int) (contactPhotoMargin * this
				.getResources().getDisplayMetrics().density);
		mLP.setMargins(scaledMargin, scaledMargin, scaledMargin, scaledMargin);
		photoImageView.setLayoutParams(mLP);

		// Set contact photo image
		photoImageView.setImageBitmap(contactPhoto);
	}

	private class FetchContactPhotoTast extends
			AsyncTask<String, Integer, Bitmap> {

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (Log.ISDEBUG)
				Log.d("SMSPopupView------>load contactPhoto down");
			contactPhoto = result;
			if (contactPhoto != null) {
				// TODO
				setContactPhoto(fromImageView, contactPhoto);
			}
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			if (Log.ISDEBUG)
				Log.d("SMSPopupView------>loading contactPhoto in back");
			return SMSPlusUtils.getContactPhoto(context, params[0]);

		}

	}

}

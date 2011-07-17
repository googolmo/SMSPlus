/**
 * 
 */
package com.googolmo.smsplus;

import com.googolmo.smsplus.SMSPlusUtils.ContactInfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.SmsMessage.MessageClass;
import android.text.format.DateUtils;

/**
 * @author GoogolMo
 * 
 */
public class SMSMessage {

	private static final String PREFIX = "com.googolmo.smsplus";
	private static final String FROM_ADDRESS = PREFIX + "FROM_ADDRESS";
	private static final String MESSAGE_BODY = PREFIX + "MESSAGE_BODY";
	private static final String TIME_STAMP = PREFIX + "TIME_STAMP";
	private static final String UNREAD_COUNT = PREFIX + "UNREAD_COUNT";
	private static final String THREAD_ID = PREFIX + "THREAD_ID";
	private static final String CONTACT_ID = PREFIX + "CONTACT_ID";
	private static final String CONTACT_NAME = PREFIX + "CONTACT_NAME";
	private static final String CONTACT_LOOKUP = PREFIX + "CONTACT_LOOKUP";
	private static final String MESSAGE_ID = PREFIX + "MESSAGE_ID";
	private static final String MESSAGE_TYPE = PREFIX + "MESSAGE_TYPE";

	// vars
	private Context context;
	private String fromAddress = null;
	private String messageBody = null;
	private long timeStamp = 0;
	private int unreadCount = 0;
	private long threadID = 0;
	private String contactID = null;
	private String contactName = null;
	private String contactLookup = null;
	private long messageID = 0;
	private int messageType = 0;
	private MessageClass messageClass = null;

	public static final int MESSAGE_TYPE_SMS = 0;

	public SMSMessage(Context _context, SmsMessage[] smsMessages,
			long _timestamp) {
		SmsMessage sms = smsMessages[0];

		context = _context;
		fromAddress = sms.getOriginatingAddress();
		timeStamp = _timestamp;
		messageType = MESSAGE_TYPE_SMS;
		messageClass = sms.getMessageClass();

		String body = "";

		try {
			if (smsMessages.length == 1 || sms.isReplace()) {
				body = sms.getDisplayMessageBody();
			} else {
				StringBuilder smsText = new StringBuilder();
				for (int i = 0; i < smsMessages.length; i++) {
					smsText.append(smsMessages[i].getMessageBody());
				}
				body = smsText.toString();
			}
		} catch (Exception e) {
			Log.e("SMSMessage exception:" + e.toString());
		}
		messageBody = body;

		ContactInfo contactInfo = null;
		contactInfo = SMSPlusUtils.getContactFromPhoneNumber(context,
				fromAddress);
		contactName = PhoneNumberUtils.formatNumber(fromAddress);

		if (contactInfo != null) {
			contactID = contactInfo.ContactID;
			contactName = contactInfo.ContactName;
			contactLookup = contactInfo.ContactLookup;
		}

		unreadCount = SMSPlusUtils.getSMSUnreadCount(context, timeStamp,
				messageBody);
		if (contactName == null) {
			contactName = context.getString(android.R.string.unknownName);
		}
	}

	public SMSMessage(Context context, String fromAddress, String messageBody,
			long timeStamp, long threadID, int unreadCount, long messageID,
			int messageType) {
		super();
		this.context = context;
		this.fromAddress = fromAddress;
		this.messageBody = messageBody;
		this.timeStamp = timeStamp;
		this.threadID = threadID;
		this.unreadCount = unreadCount;
		this.messageID = messageID;
		this.messageType = messageType;

		ContactInfo contactInfo = null;

		contactInfo = SMSPlusUtils.getContactFromPhoneNumber(context,
				fromAddress);
		contactName = PhoneNumberUtils.formatNumber(fromAddress);
		if (contactInfo != null) {
			contactID = contactInfo.ContactID;
			contactLookup = contactInfo.ContactLookup;
			contactName = contactInfo.ContactName;
		}
		if (contactName == null) {
			contactName = context.getString(android.R.string.unknownName);
		}

	}

	public SMSMessage(Context _context, Bundle b) {
		context = _context;
		fromAddress = b.getString(FROM_ADDRESS);
		messageBody = b.getString(MESSAGE_BODY);
		timeStamp = b.getLong(TIME_STAMP);
		unreadCount = b.getInt(UNREAD_COUNT, 1);
		threadID = b.getLong(THREAD_ID, 0);
		contactID = b.getString(CONTACT_ID);
		contactName = b.getString(CONTACT_NAME);
		contactLookup = b.getString(CONTACT_LOOKUP);
		messageID = b.getLong(MESSAGE_ID, 0);
		messageType = b.getInt(MESSAGE_TYPE, MESSAGE_TYPE_SMS);
	}

	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		bundle.putString(FROM_ADDRESS, fromAddress);
		bundle.putString(MESSAGE_BODY, messageBody);
		bundle.putLong(TIME_STAMP, timeStamp);
		bundle.putInt(UNREAD_COUNT, unreadCount);
		bundle.putLong(THREAD_ID, threadID);
		bundle.putString(CONTACT_ID, contactID);
		bundle.putString(CONTACT_NAME, contactName);
		bundle.putString(CONTACT_LOOKUP, contactLookup);
		bundle.putLong(MESSAGE_ID, messageID);
		bundle.putInt(MESSAGE_TYPE, messageType);
		return bundle;
	}

	public Intent getPopupIntent() {
		Intent intent = new Intent(context, PopupActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtras(toBundle());
		return intent;
	}

	public void localThreadID() {
		if (threadID == 0) {
			threadID = SMSPlusUtils.findThreadIDFromAddress(context,
					fromAddress);
		}
	}

	public void localMessageID() {
		if (messageID == 0) {
			if (threadID == 0) {
				localThreadID();
			}
			messageID = SMSPlusUtils.findMessageID(context, threadID,
					timeStamp, messageBody, messageType);
		}
	}

	public long getMessageID() {
		localMessageID();
		return messageID;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public CharSequence getFormatTimeStamp() {
		return DateUtils.formatDateTime(context, timeStamp,
				DateUtils.FORMAT_SHOW_TIME);
	}

	public String getContactName() {
		if (contactName == null) {
			contactName = context.getString(android.R.string.unknownName);
		}
		return contactName;
	}

	public String getMessageBody() {
		if (messageBody == null) {
			messageBody = "";
		}
		return messageBody;
	}

	public String getContactID() {
		return contactID;
	}

	public long getThreadID() {
		localThreadID();
		return threadID;
	}
}

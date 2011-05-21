/**
 * 
 */
package com.googolmo.smsplus;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.telephony.SmsMessage;

/**
 * @author GoogolMo
 * 
 */
@SuppressWarnings("deprecation")
public class SMSPlusUtils {
	static String contactLookupUri = "content://com.android.contacts/phone_lookup";
	private static final String UNREAD_CONDITION = "read=0";

	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(
			SMS_CONTENT_URI, "inbox");
	public static final Uri MMS_SMS_CONTENT_URI = Uri
			.parse("content://mms-sms/");
	public static final Uri THREAD_ID_CONTENT_URI = Uri.withAppendedPath(
			MMS_SMS_CONTENT_URI, "threadID");
	public static final Uri CONVERSATION_CONTENT_URI = Uri.withAppendedPath(
			MMS_SMS_CONTENT_URI, "conversations");

	// The size of the contact photo thumbnail on the popup
	public static final int CONTACT_PHOTO_THUMBSIZE = 60;

	// The max size of either the width or height of the contact photo
	public static final int CONTACT_PHOTO_MAXSIZE = 1024;

	/**
	 * 
	 */
	public SMSPlusUtils() {
		// TODO Auto-generated constructor stub
	}

	// 短信设置
	public static void enableSMSPlus(Context context, boolean enable) {
		PackageManager pManager = context.getPackageManager();
		ComponentName cName = new ComponentName(context, SMSReceiver.class);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor settings = sharedPreferences.edit();
		settings.putBoolean(context.getString(R.string.pref_enabled_key),
				enable);
		settings.commit();

		if (enable) {
			Log.i("SMSPlus SMSReceiver is enabled");
			pManager.setComponentEnabledSetting(cName,
					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
					PackageManager.DONT_KILL_APP);
		} else {
			Log.i("SMSPlus SMSReceiver is disabled");
			pManager.setComponentEnabledSetting(cName,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
	}

	// 获得短信
	public static final SmsMessage[] getMessagesFromIntent(Intent intent) {
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
		if (messages == null || messages.length == 0) {
			return null;
		}
		int len = messages.length;
		byte[][] pduObject = new byte[len][];
		for (int i = 0; i < len; i++) {
			pduObject[i] = (byte[]) messages[i];
		}
		byte[][] pdus = new byte[pduObject.length][];
		int pdusCount = pduObject.length;
		SmsMessage[] smsMessages = new SmsMessage[pdusCount];
		for (int i = 0; i < pdusCount; i++) {
			pdus[i] = pduObject[i];
			smsMessages[i] = SmsMessage.createFromPdu(pdus[i]);
		}

		return smsMessages;
	}

	public static class ContactInfo {
		String ContactID = null;
		String ContactName = null;
		String ContactLookup = null;

		public ContactInfo(String _contactID, String _contactName) {
			ContactID = _contactID;
			ContactName = _contactName;
		}

		public ContactInfo(String _contactID, String _contactName,
				String _contactLookup) {
			ContactID = _contactID;
			ContactName = _contactName;
			ContactLookup = _contactLookup;
		}

	}

	public static synchronized ContactInfo getContactFromPhoneNumber(
			Context context, String phoneAddress) {
		if (phoneAddress == null)
			return null;
		Uri uri = Uri.withAppendedPath(Uri.parse(contactLookupUri),
				Uri.encode(phoneAddress));
		String[] projection = { "_id", "display_name", "lookup" };
		Cursor cursor = context.getContentResolver().query(uri, projection,
				null, null, null);
		if (cursor != null) {
			try {
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					String contactID = String.valueOf(cursor.getLong(0));
					String contactName = cursor.getString(1);
					String contactLookup = cursor.getString(2);
					if (Log.ISDEBUG) {
						Log.d("SMSPlusUtils--------->getContactFromPhoneNumber:Found Contact="
								+ contactID
								+ ","
								+ contactName
								+ ","
								+ contactLookup);
					}
					return new ContactInfo(contactID, contactName,
							contactLookup);
				}
			} catch (Exception e) {
				Log.e(e.getMessage());
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	synchronized public static int getSMSUnreadCount(Context context,
			long timestamp, String messageBody) {
		if (Log.ISDEBUG)
			Log.d("SMSPlusUtils------>getSMSUnreadCount");
		final String[] projection = new String[] { "_id", "body" };
		final String selection = UNREAD_CONDITION;
		final String[] selectionArgs = null;
		final String sortOrder = "date DESC";

		int count = 0;

		Cursor cursor = context.getContentResolver().query(
				SMS_INBOX_CONTENT_URI, projection, selection, selectionArgs,
				sortOrder);
		if (cursor != null) {
			try {
				count = cursor.getCount();
				if (messageBody != null && count > 0) {
					if (cursor.moveToFirst()) {
						if (!messageBody.equals(cursor.getString(1))) {
							count++;
						}
					}
				}
			} finally {
				cursor.close();
			}
		}

		if (count == 0 && timestamp > 0) {
			count = 1;
		}

		Log.i("SMSPlusUtils----->getUnreadCount=" + count);

		return count;
	}

	synchronized public static long findThreadIDFromAddress(Context context,
			String address) {
		if (address == null)
			return 0;
		String THREAD_RECIPIENT_QUERY = "recipient";
		Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
		uriBuilder.appendQueryParameter(THREAD_RECIPIENT_QUERY, address);
		long threadID = 0;
		Cursor cursor = context.getContentResolver().query(uriBuilder.build(),
				new String[] { Contacts.People._ID }, null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					threadID = cursor.getLong(0);
				}
			} finally {
				cursor.close();
			}
		}
		return threadID;
	}

	synchronized public static long findMessageID(Context context,
			long threadID, long timeStamp, String body, int messageType) {
		long id = 0;
		String selection = "body=" + DatabaseUtils.sqlEscapeString(body);
		final String sortOrder = "date DESC";
		final String[] projection = new String[] { "_id", "date", "thread_ID",
				"body" };

		if (threadID > 0) {
			if (Log.ISDEBUG)
				Log.d("find MessageID");
			Cursor cursor = context.getContentResolver().query(
					ContentUris.withAppendedId(CONVERSATION_CONTENT_URI,
							threadID), projection, selection, null, sortOrder);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						id = cursor.getLong(0);
						if (Log.ISDEBUG)
							Log.d("MessageID=" + id);
					}
				} finally {
					cursor.close();
				}
			}
		}
		return id;
	}

	// 获得未读的短信
	public static ArrayList<SMSMessage> getUnreadMessages(Context context,
			long ignoreMessageId) {
		Log.i("getUnreadMessages:ignoreMessageID=" + ignoreMessageId);
		ArrayList<SMSMessage> messages = null;
		final String[] projection = new String[] { "_id", "thread_id",
				"address", "date", "body" };
		String selection = "read=0 ";
		String[] selectionArgs = null;
		final String sortOrder = "date DESC";

		if (ignoreMessageId > 0) {
			selection += "and _id !=?";
			selectionArgs = new String[] { String.valueOf(ignoreMessageId) };
		}
		Cursor cursor = context.getContentResolver().query(SMS_CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);
		long messageID;
		long threadID;
		String address;
		long timeStamp;
		String body;

		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count > 0) {
					messages = new ArrayList<SMSMessage>(count);
					while (cursor.moveToNext()) {
						messageID = cursor.getLong(0);
						threadID = cursor.getLong(1);
						address = cursor.getString(2);
						timeStamp = cursor.getLong(3);
						body = cursor.getString(4);

						messages.add(new SMSMessage(context, address, body,
								timeStamp, threadID, count, messageID,
								SMSMessage.MESSAGE_TYPE_SMS));
					}
				}
			} finally {
				cursor.close();
			}
		}
		return messages;
	}

	public static Bitmap getContactPhoto(Context context, String id) {
		if (id == null || id.equals("0"))
			return null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		loadContactPhoto(context, id, 0, options);

		int width = options.outWidth;
		int height = options.outHeight;

		if (Log.ISDEBUG)
			Log.d("ContactPhoto size is width=" + width + " height=" + height);
		if (width > CONTACT_PHOTO_MAXSIZE || height > CONTACT_PHOTO_MAXSIZE
				|| width == 0 || height == 0) {
			return null;
		}
		options.inJustDecodeBounds = false;
		final float scale = context.getResources().getDisplayMetrics().density;
		int thumb = CONTACT_PHOTO_THUMBSIZE;
		if (scale != 1) {
			if (Log.ISDEBUG)
				Log.d("scale=" + scale);
			thumb = Math.round(thumb * scale);
		}
		int newWidth = thumb;
		int newHeight = thumb;

		boolean sampleDown = false;

		if (width > thumb || height > thumb) {
			sampleDown = true;
		}
		if (height < width) {
			if (sampleDown) {
				options.inSampleSize = Math.round(height / thumb);
			}
			newHeight = Math.round(thumb * height / width);
		} else {
			if (sampleDown) {
				options.inSampleSize = Math.round(width / thumb);
			}
			newWidth = Math.round(thumb * width / height);
		}
		Bitmap contactPhoto = null;
		try {
			contactPhoto = loadContactPhoto(context, id, 0, options);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(e.getMessage());
		}
		if (contactPhoto == null)
			return null;
		return Bitmap.createScaledBitmap(contactPhoto, newWidth, newHeight,
				true);

	}

	public static Bitmap loadContactPhoto(Context context, String id,
			int placeholderImageResource, BitmapFactory.Options options) {
		if (id == null) {
			return loadPlaceholderPhoto(placeholderImageResource, context,
					options);
		}

		Uri uri = Uri.withAppendedPath(
				Uri.parse("content://com.android.contacts/contacts"), id);

		Class<?> contactClass;
		Bitmap bm = null;
		try {
			contactClass = Class
					.forName("android.provider.ContactsContract$Contacts");
			Method contactsOpenPhotoStreamMethod = contactClass.getMethod(
					"openContactPhotoInputStream", new Class[] {
							ContentResolver.class, Uri.class });

			InputStream stream = (InputStream) contactsOpenPhotoStreamMethod
					.invoke(contactClass, context.getContentResolver(), uri);

			bm = stream != null ? BitmapFactory.decodeStream(stream, null,
					options) : null;
			if (bm == null) {
				bm = loadPlaceholderPhoto(placeholderImageResource, context,
						options);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bm;

	}

	private static Bitmap loadPlaceholderPhoto(int placeholderImageResource,
			Context context, BitmapFactory.Options options) {
		if (placeholderImageResource == 0) {
			return null;
		}
		return BitmapFactory.decodeResource(context.getResources(),
				placeholderImageResource, options);
	}

	public static Intent getReplyIntent(Context context, SMSMessage message) {
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP;
		long threadID = message.getThreadID();
		String fromAddress = message.getFromAddress();
		if (Log.ISDEBUG)
			Log.d("ThreadID=" + threadID + " fromaddress=" + fromAddress);
		Uri uri;
		Intent replyIntent = null;
		if (threadID > 0) {
			replyIntent = new Intent(Intent.ACTION_VIEW);
			uri = Uri.withAppendedPath(THREAD_ID_CONTENT_URI,
					String.valueOf(threadID));
			replyIntent.setData(uri);
		} else if (!fromAddress.equals("")) {
			replyIntent = new Intent(Intent.ACTION_SENDTO);
			uri = Uri.parse("smsto:" + Uri.encode(fromAddress));
			replyIntent.setData(uri);
		} else {
			replyIntent = new Intent(Intent.ACTION_MAIN);
		}
		replyIntent.setFlags(flags);
		return replyIntent;

	}
}

/**
 * 
 */
package com.googolmo.smsplus.preferences;

import com.googolmo.smsplus.Log;
import com.googolmo.smsplus.SMSPlusUtils;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

/**
 * @author GoogolMo 用于设置是否启用SMSPlus
 * 
 */
public class SPEnabledCheckBox extends CheckBoxPreference {

	private Context context;

	public SPEnabledCheckBox(Context c, AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		// TODO Auto-generated constructor stub
		context = c;
	}

	public SPEnabledCheckBox(Context c, AttributeSet attrs) {
		super(c, attrs);
		// TODO Auto-generated constructor stub
		context = c;
	}

	public SPEnabledCheckBox(Context c) {
		super(c);
		// TODO Auto-generated constructor stub
		context = c;
	}

	@Override
	protected void onClick() {
		super.onClick();
		Log.i(isChecked() + "");
		SMSPlusUtils.enableSMSPlus(context, isChecked());
	}

}

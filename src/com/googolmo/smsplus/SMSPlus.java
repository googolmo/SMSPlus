package com.googolmo.smsplus;

import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;

public class SMSPlus extends PreferenceActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// final RingtonePreference ringtone = (RingtonePreference)
		// findPreference(getString(R.string.pref_notification_ringtone_key));
		// // ringtone.setDefaultValue(android.provider.MediaStore.)

		final DialogPreference aboutPre = (DialogPreference) findPreference(getString(R.string.pref_about_key));
		aboutPre.setDialogTitle(R.string.app_name);
		aboutPre.setDialogLayoutResource(R.layout.about);

	}
}
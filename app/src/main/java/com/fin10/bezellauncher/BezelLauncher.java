package com.fin10.bezellauncher;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class BezelLauncher extends Application {

	private static final String TAG = "BelzelLauncher";
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate:");
		super.onCreate();
		boolean enabled = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE).getBoolean(SettingsActivity.PREF_KEY_ENABLED, false);
		if (enabled) {
			startService(new Intent(this, LaunchService.class));
		}
	}
}

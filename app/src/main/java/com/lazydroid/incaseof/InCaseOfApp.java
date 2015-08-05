package com.lazydroid.incaseof;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;

public class InCaseOfApp extends Application {
	public static final boolean DEBUG = false;	// enables debug output in logs

	private static InCaseOfApp instance;

	public static SharedPreferences preferences;

	public static String android_id;

	// used to prevent erroneous camera restarts
	public static long camera_done = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		preferences = getSharedPreferences( getPackageName() + "_preferences", MODE_PRIVATE);
		android_id = Secure.getString( this.getContentResolver(), Secure.ANDROID_ID);
	}

	public static Context getContext() {
		return instance;
	}
}

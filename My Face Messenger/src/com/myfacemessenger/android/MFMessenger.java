package com.myfacemessenger.android;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MFMessenger extends Application
{
	public static final String	PACKAGE			= "com.myfacemessenger.android";
	public static final String	LOG_TAG			= PACKAGE + ".log";
	public static final String	ACTION_UPDATE	= PACKAGE + ".UPDATE_ACTION";
	private String				DEVICE_ID		= MFMessenger.PACKAGE + ".IDENTITY";

	private String getDevicePhoneId()
	{
		SharedPreferences prefs = getSharedPreferences(MFMessenger.PACKAGE, 0);
		String id = prefs.getString(DEVICE_ID, null);
		if( id != null ) {
			return id;
		}
		TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if( id == null ) {
			id = manager.getLine1Number();
		}
		if( id == null ) {
			id = manager.getVoiceMailNumber();
		}
		if( id == null ) {
			id = UUID.randomUUID().toString();
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(DEVICE_ID, id);
		editor.commit();
		return id;
	}
	public static void log(String message)
	{
		log( Log.DEBUG, message );
	}

	public static void log(int level, String message)
	{
		switch( level ) {
			default:
			case Log.DEBUG:
				Log.d(LOG_TAG, message);
				break;
			case Log.ERROR:
				Log.e(LOG_TAG, message);
				break;
			case Log.INFO:
				Log.i(LOG_TAG, message);
				break;
			case Log.WARN:
				Log.w(LOG_TAG, message);
				break;
			case Log.VERBOSE:
				Log.v(LOG_TAG, message);
				break;
		}
	}
}
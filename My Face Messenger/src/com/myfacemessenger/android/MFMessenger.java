package com.myfacemessenger.android;

import android.app.Application;
import android.util.Log;

public class MFMessenger extends Application
{
	public static final String	PACKAGE			= "com.myfacemessenger.android";
	public static final String	LOG_TAG			= PACKAGE + ".log";
	public static final String	ACTION_UPDATE	= PACKAGE + ".UPDATE_ACTION";

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
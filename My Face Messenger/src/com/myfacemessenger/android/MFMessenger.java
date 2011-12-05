package com.myfacemessenger.android;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MFMessenger extends Application
{
	public static final String		PACKAGE			= "com.myfacemessenger.android";
	public static final String		UPDATE_ACTION	= PACKAGE + ".ACTION_NEW_MESSAGE";
	public static final String		LOG_TAG			= PACKAGE + ".log";
	public static final String		ACTION_UPDATE	= PACKAGE + ".UPDATE_ACTION";
	public static SharedPreferences	preferences		= null;

	private String					PREF_DEVICE_ID	= PACKAGE + ".IDENTITY";
	private String					PREF_FIRST_RUN	= PACKAGE + ".FIRST_RUN";

	private static final String		ICON_DIRECTORY	= "MyFaceMessenger";

	@Override
	public void onCreate()
	{
		super.onCreate();
		preferences = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
	}

	private String getDevicePhoneId()
	{
		String id = preferences.getString(PREF_DEVICE_ID, null);
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
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREF_DEVICE_ID, id);
		editor.commit();
		return id;
	}

	public static String getContactName(ContentResolver cr, String phone)
	{
		String name = null;
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
		String[] projection = new String[] {
			PhoneLookup.DISPLAY_NAME,
			PhoneLookup.LABEL,
			PhoneLookup.PHOTO_ID,
			PhoneLookup.TYPE
		};
		Cursor cursor = cr.query(uri, projection, null, null, null);
		if( cursor.moveToFirst() ) {
			name = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			String label = cursor.getString(cursor.getColumnIndex(PhoneLookup.LABEL));
			if( label != null ) {
				name += " "+label;
			}
		} else {
			MFMessenger.log("Unable to match existing contact");
			name = phone;
		}
		return name;
	}

	public static Bitmap getContactPhoto(ContentResolver cr, String phone)
	{
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
		String[] projection = new String[] {
			PhoneLookup._ID,
			PhoneLookup.DISPLAY_NAME,
			PhoneLookup.LABEL,
			PhoneLookup.PHOTO_ID,
			PhoneLookup.TYPE
		};
		Cursor cursor = cr.query(uri, projection, null, null, null);
		if( cursor.moveToFirst() ) {
			long id = cursor.getLong(cursor.getColumnIndex(PhoneLookup._ID));
			Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
			InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
			if( is != null ) {
				return BitmapFactory.decodeStream(is);
			}
			MFMessenger.log("Unable to retrieve a contact photo.");
		}
		MFMessenger.log("Unable to match existing contact");
		return (null);
	}

	private static File getIconDirectory()
	{
		File targetDir = new File(Environment.getExternalStorageDirectory(), ICON_DIRECTORY);
		if( !targetDir.exists() ) {
			targetDir.mkdirs();
		}
		return targetDir;
	}

	public static File getEmoticonFile(String emotion)
	{
		return new File(getIconDirectory(), emotion + ".jpg");
	}

	public static File getEmoteIcon(String address, String emote)
	{
		File contactDir = new File(getIconDirectory(), address);
		if( contactDir.exists() ) {
			File icon = new File(contactDir, emote + ".jpg");
			if( icon.exists() ) {
				return icon;
//				InputStream is;
//				try {
//					is = new FileInputStream(icon);
//					return BitmapFactory.decodeStream(is);
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}
			}
		}
		return null;
	}

	private static HashMap<String,String> loadEmoticonMap(Context context)
	{
		String[] keyValues = context.getResources().getStringArray(R.array.emoticons);
		String[] valuesData = context.getResources().getStringArray(R.array.emoticon_names);
		HashMap<String,String> map = new HashMap<String, String>();
		for( int i=0; i<keyValues.length; i++ ) {
			map.put(keyValues[i], valuesData[i]);
		}
		return map;
	}

	public static String identifyEmote(Context context, String source)
	{
		String emote = "happy";
		if( source != null ) {
			HashMap<String,String> emoticonMap = loadEmoticonMap(context);
			Pattern p = Pattern.compile(
				"(?:" +
				":[-o]?\\)" +
				"|" +
				";[-o]?\\)" +
				"|" +
				":[-o]?\\(" +
				"|" +
				":[-o]?P" +
				"|" +
				"=[-o]?[0O]" +
				"|" +
				":[-o]?[0O]" +
				"|" +
				":[-o]?\\*" +
				"|" +
				"B[-o]?\\)" +
				"|" +
				":[-o]\\$" +
				"|" +
				":[-o]?\\[" +
				"|" +
				":[-o]?\\!" +
				"|" +
				"[0O]:[-o]?\\)" +
				"|" +
				":[-o]?\\\\" +
				"|" +
				":[-o]?D" +
				"|" +
				":\'[-o]?\\(" +
				"|" +
				":[-o]?X" +
				"|" +
				"o_O" +
				")"
			);
			Matcher m = p.matcher(source);
			if( m.find() ) {
				MatchResult mr = m.toMatchResult();
				emote = mr.group(0);
			} else {
				emote = ":-)";
			}
			return emoticonMap.get(emote);
//			if( emote == ":)" || emote == ":-)" ) {
//				return "happy";
//			}
//			if( emote == ";)" || emote == ";-)" ) {
//				return "winking";
//			}
//			if( emote == ":(" || emote == ":-(" ) {
//				return "sad";
//			}
//			if( emote == ":P" || emote == ":-P" ) {
//				return "tongue_sticking_out";
//			}
//			if( emote == "=O" || emote == "=-O" ) {
//				return "surprised";
//			}
//			if( emote == ":O" || emote == ":-O" ) {
//				return "yelling";
//			}
//			if( emote == ":*" || emote == ":-*" ) {
//				return "kissing";
//			}
//			if( emote == "B)" || emote == "B-)" ) {
//				return "cool";
//			}
//			if( emote == ":$" || emote == ":-$" ) {
//				return "money_mouth";
//			}
//			if( emote == ":[" || emote == ":-[" ) {
//				return "embarrassed";
//			}
//			if( emote == ":!" || emote == ":-!" ) {
//				return "foot_in_mouth";
//			}
//			if( emote == "O:)" || emote == "O:-)" ) {
//				return "angel";
//			}
//			if( emote == ":\\" || emote == ":-\\" ) {
//				return "undecided";
//			}
//			if( emote == ":D" || emote == ":-D" ) {
//				return "laughing";
//			}
//			if( emote == ":\'(" ) {
//				return "crying";
//			}
//			if( emote == ":X" || emote == ":-X" ) {
//				return "lips_are_sealed";
//			}
//			if( emote == "o_O" ) {
//				return "confused";
//			}
		}
		return "happy";
	}

	public static String hash(String source)
	{
		String hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(source.getBytes(), 0, source.length());
			hash = new BigInteger(1, digest.digest()).toString();
		} catch( NoSuchAlgorithmException e ) {
			e.printStackTrace();
		}
		return hash;
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
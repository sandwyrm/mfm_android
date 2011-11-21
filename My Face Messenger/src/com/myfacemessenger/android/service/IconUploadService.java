package com.myfacemessenger.android.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.myfacemessenger.android.MFMessenger;

public class IconUploadService extends Service
{
	private String ftpUrl		= "ftp.myfacemessenger.com";
	private String ftpUser		= "mfmessenger";
	private String ftpPass		= "textmyface1!";
	private String DEVICE_ID	= MFMessenger.PACKAGE + ".IDENTITY";

	@Override
	public void onStart(Intent intent, int startId)
	{
		String fileName = intent.getExtras().getString("file");
		MFMessenger.log("Upload service started.");
		MFMessenger.log("Uploading file: "+fileName);
		try {
			MFMessenger.log("Initializing FTP connection.");
			FTPClient client = new FTPClient();
			MFMessenger.log("Connecting FTP");
			client.connect(ftpUrl);
			if( client.isConnected() ) {
				if( !client.login(ftpUser, ftpPass) ) {
					client.logout();
					MFMessenger.log("Login failed");
				} else {
					String rawFileName = "emoticons/" + getDevicePhoneId() + "_" + fileName.substring( fileName.lastIndexOf("/")+1 );
					MFMessenger.log("Remote file destination: " + rawFileName);
					client.enterLocalPassiveMode();
					client.setFileType(FTP.BINARY_FILE_TYPE);
					InputStream input;
					input = new FileInputStream(fileName);
					client.storeFile(rawFileName, input);
					input.close();
				}
			}
			client.disconnect();
			MFMessenger.log("Closing FTP connection");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		MFMessenger.log("Upload service initialized");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		MFMessenger.log("Upload service destroyed");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

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
			id = manager.getDeviceId();
		}
		if( id == null ) {
			id = manager.getSimSerialNumber();
		}
		if( id == null ) {
			id = UUID.randomUUID().toString();
		}
		MessageDigest digest = null;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if( digest != null ) {
			digest.update(id.getBytes());
			id = digest.digest().toString();
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(DEVICE_ID, id);
		editor.commit();
		return id;
	}
}
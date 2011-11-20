package com.myfacemessenger.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class APISyncService extends Service
{
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
package com.myfacemessenger.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.service.MessageReceiverService;

public class MessageReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		MFMessenger.log("Incoming message");
		intent.setClass(context, MessageReceiverService.class);
		intent.putExtra("result", getResultCode());
		MessageReceiverService.startService(context, intent);
		MFMessenger.log("Message handled");
//		abortBroadcast();
	}
}
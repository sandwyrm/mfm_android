package com.myfacemessenger.android.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.telephony.SmsMessage;

import com.myfacemessenger.android.MFMessenger;

import custom.com.google.android.mms.pdu.GenericPdu;
import custom.com.google.android.mms.pdu.NotificationInd;
import custom.com.google.android.mms.pdu.PduBody;
import custom.com.google.android.mms.pdu.PduHeaders;
import custom.com.google.android.mms.pdu.PduParser;
import custom.com.google.android.mms.pdu.PduPart;
import custom.com.google.android.mms.pdu.RetrieveConf;

public class MessageReceiverService extends Service
{
	private static final String			SMS_RECEIVED	= "android.provider.Telephony.SMS_RECEIVED";
	private static final String			MMS_RECEIVED	= "android.provider.Telephony.WAP_PUSH_RECEIVED";
	private static final String			DATA_RECEIVED	= "android.intent.action.DATA_SMS_RECEIVED";
	private static final String			SMS_REJECTED	= "android.provider.Telephony.SMS_REJECTED";
	private static final String			DATA_TYPE_MMS	= "application/vnd.wap.mms-message";

	private Context						context;
	private Looper						looper;
	private ServiceHandler				handler;
	private int							serviceId;

	public static final Object			serviceSync		= new Object();
	public static PowerManager.WakeLock	startingService;

	@Override
	public void onCreate()
	{
		MFMessenger.log("Service created");
		HandlerThread thread = new HandlerThread(MFMessenger.PACKAGE, Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		looper = thread.getLooper();
		handler = new ServiceHandler(looper);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		MFMessenger.log("Service started");
		serviceId = startId;
		Message message = handler.obtainMessage();
		message.arg1 = startId;
		message.obj = intent;
		handler.sendMessage(message);
	}

	@Override
	public void onDestroy()
	{
		MFMessenger.log("Service destroyed");
		looper.quit();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	public static void startService(Context context, Intent intent)
	{
		synchronized (serviceSync) {
			MFMessenger.log("Service starting");
			if( startingService == null ) {
				PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				startingService = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MFMessenger.PACKAGE);
				startingService.setReferenceCounted(false);
			}
			startingService.acquire();
			context.startService(intent);
		}
	}

	public static void finalizeService(Service service, int startId)
	{
		synchronized (serviceSync) {
			MFMessenger.log("Service shutting down");
			if( startingService != null ) {
				if( service.stopSelfResult(startId) ) {
					startingService.release();
				}
			}
		}
	}

	private void handleSMSReceived(Intent intent)
	{
		MFMessenger.log("SMS Received");
		StringBuilder body = new StringBuilder();
		Bundle extras = intent.getExtras();
		if( extras != null ) {
			SmsMessage[] messages = getMessagesFromIntent(intent);
			if( messages != null ) {
				SmsMessage message = messages[0];
				if( message.getMessageClass() == SmsMessage.MessageClass.CLASS_0
					|| message.isReplace() ) {
					MFMessenger.log("Message is CLASS 0 or Replace");
				} else {
					String address = messages[0].getOriginatingAddress();
					long timestamp = messages[0].getTimestampMillis();
					for( int i=0; i< messages.length; i++ ) {
						body.append(messages[i].getMessageBody());
					}
					String content = body.toString();
//					STMDatabase db = new STMDatabase(getBaseContext());
//					ThreadIndexTable index = new ThreadIndexTable(getBaseContext(), db.getWritableDatabase());
//					String threadId = index.findOrCreateThreadId(address);
//					index.updateThreadTime(threadId, content);
//					ThreadTable table = new ThreadTable(getBaseContext(), db.getWritableDatabase(), threadId);
//					int type = ThreadTable.MESSAGE_TYPE_SMS|ThreadTable.MESSAGE_TYPE_RECEIVED;
//					table.insert(type, content, ThreadTable.STATUS_UNREAD);
					MFMessenger.log(String.format("SMS From %s @ %d: %s", address, timestamp, content));
//					db.close();
					Intent i = new Intent(MFMessenger.ACTION_UPDATE);
					sendBroadcast(i);
				}
			} else {
				MFMessenger.log("No messages processed");
			}
		} else {
			MFMessenger.log("Passed intent contained no extras");
		}
	}

	private SmsMessage[] getMessagesFromIntent(Intent intent)
	{
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
		if( messages == null ) {
			return null;
		}
		if( messages.length == 0 ) {
			return null;
		}
		byte[][] pduObjs = new byte[messages.length][];
		for( int i=0; i<messages.length; i++) {
			pduObjs[i] = (byte[]) messages[i];
		}
		byte[][] pdus = new byte[pduObjs.length][];
		int pduCount = pdus.length;
		SmsMessage[] result = new SmsMessage[pduCount];
		for( int i=0; i<pduCount; i++ ) {
			pdus[i] = pduObjs[i];
			result[i] = SmsMessage.createFromPdu(pdus[i]);
		}
		return result;
	}

	private void handleMMSReceived(Intent intent)
	{
		MFMessenger.log("MMS Received");
		Bundle extras = intent.getExtras();
		if( extras != null ) {
			handleMMSHeaders(extras.getByteArray("data"));
		}
	}

	private void handleMMSHeaders(byte[] headerData)
	{
		PduParser parser = new PduParser(headerData);
		GenericPdu headers = parser.parse();
		if( headers != null ) {
			switch( headers.getMessageType() ) {
				case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
					MFMessenger.log("Notification indicator received");
					NotificationInd notification = ((NotificationInd) headers);
					String location = byteArrayToString(notification.getContentLocation());
					MFMessenger.log("Content location: "+location);
					getMessageData(location);
					break;
				case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
//					STMDatabase db = new STMDatabase(getBaseContext());
					MFMessenger.log("Notification indicator received");
					RetrieveConf retrieval = ((RetrieveConf) headers);
//					ThreadIndexTable index = new ThreadIndexTable(getBaseContext(), db.getWritableDatabase());
//					String threadId = index.findOrCreateThreadId(retrieval.getFrom().getString());
//					ThreadTable table = new ThreadTable(getBaseContext(), db.getWritableDatabase(), threadId);
//					int type = ThreadTable.MESSAGE_TYPE_MMS|ThreadTable.MESSAGE_TYPE_RECEIVED;
//					table.insert(type, byteArrayToString(headerData), ThreadTable.STATUS_UNREAD);
					PduBody retrievalBody = retrieval.getBody();
					for( int i=0; i<retrievalBody.getPartsNum(); i++ ) {
						PduPart bodyPart = retrievalBody.getPart(i);
						String bodyPartType = byteArrayToString(bodyPart.getContentType());
						MFMessenger.log("Body part #"+i+" content-type: "+bodyPartType);
						if( bodyPartType.trim().equalsIgnoreCase("text/plain") ) {
//							index.updateThreadTime(threadId, byteArrayToString(bodyPart.getData()));
							MFMessenger.log("Content: "+byteArrayToString(bodyPart.getData()));
						}
					}
//					db.close();
					Intent i = new Intent(MFMessenger.ACTION_UPDATE);
					sendBroadcast(i);
					break;
				default:
					MFMessenger.log("Unhandled MMS header received");
					break;
			}
		} else {
			MFMessenger.log("MMS headers could not be parse successfully");
		}
	}

	private void getMessageData(String uri)
	{
		URL url;
		try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		InputStream in;
		ByteArrayOutputStream out;
		try {
			in = new BufferedInputStream(url.openStream());
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while( -1 != (n=in.read(buf)) ) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		handleMMSHeaders(out.toByteArray());
	}

	private String byteArrayToString(byte[] source)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for( int i=0; i<source.length; i++ ) {
			out.write(source[i]);
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	private final class ServiceHandler extends Handler
	{
		public ServiceHandler(Looper looper)
		{
			super(looper);
			MFMessenger.log("Message service handler initialized");
		}

		@Override
		public void handleMessage(Message message)
		{
			MFMessenger.log("Handling message: "+message);
			super.handleMessage(message);
			int serviceId = message.arg1;
			Intent intent = (Intent) message.obj;
			String action = intent.getAction();
			if( action.equals(SMS_RECEIVED) ) {
				handleSMSReceived(intent);
			} else if( action.equals(MMS_RECEIVED) ) {
				handleMMSReceived(intent);
//			} else if( action.equals(DATA_RECEIVED) ) {
//			} else if( action.equals(SMS_REJECTED) ) {	
			}
			finalizeService(MessageReceiverService.this, serviceId);
		}
	}

}
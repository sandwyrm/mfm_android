package com.myfacemessenger.android.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;

import custom.android.provider.Telephony.Sms;

public class ThreadActivity extends ListActivity
{
	private String thread_id;
	private String address;
	private String contactName;
	private SmsThreadAdapter adapter;
	private UpdateReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
		thread_id = getIntent().getStringExtra("thread_id");
		address = getIntent().getStringExtra("address");
		((Button) findViewById(R.id.sendButton))
			.setOnClickListener(sendListener);
		Bitmap photo = MFMessenger.getContactPhoto(getContentResolver(), address);
		if( photo != null ) {
			((ImageView) findViewById(R.id.threadIcon))
				.setImageBitmap(photo);
		}
		contactName = MFMessenger.getContactName(getContentResolver(), address);
		((TextView) findViewById(R.id.threadContactName))
			.setText(contactName);
		Cursor c = getContentResolver().query(Sms.CONTENT_URI,
				null, "thread_id = ?", new String[] {thread_id}, "date ASC");
		adapter = new SmsThreadAdapter(this, c);
		setListAdapter(adapter);
		getListView()
        	.setStackFromBottom(true);
        getListView()
        	.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		refreshList();
		receiver = new UpdateReceiver();
		registerReceiver(receiver, new IntentFilter(MFMessenger.ACTION_UPDATE));
	}

	@Override
	protected void onPause()
	{
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch( item.getItemId() ) {
			case R.id.settings:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.faces:
				intent = new Intent(this, FaceIconManagerActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void sendMessage()
	{
		EditText input = (EditText) findViewById(R.id.sendText);
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(address, null, input.getText().toString(), null, null);
		ContentValues cv = new ContentValues();
		cv.put("address", address);
		cv.put("body", input.getText().toString());
		cv.put("thread_id", thread_id);
		cv.put("type", 2);
		getContentResolver().insert(Sms.CONTENT_URI, cv);
		input.setText("");
		refreshList();
	}

	private OnClickListener sendListener =	//
		new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			sendMessage();
		}
	};

	private class SmsThreadAdapter extends CursorAdapter
	{
		private SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, MMM d");

		public SmsThreadAdapter(Context context, Cursor c)
		{
			super(context, c, true);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			int type = cursor.getInt(cursor.getColumnIndex("type"));
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = null;
			switch( type ) {
				case 1:
					v = inflater.inflate(R.layout.thread_send, parent, false);
					break;
				default:
				case 2:
					v = inflater.inflate(R.layout.thread_receive, parent, false);
					break;
			}
			return v;
		}

		@Override
		public int getViewTypeCount()
		{
			return 2;
		}

		@Override
		public int getItemViewType(int position)
		{
			Cursor c = getCursor();
			if( c.moveToPosition(position) ) {
				int type = c.getInt(c.getColumnIndex("type"));
				return type-1;
			} else {
				return 1;
			}
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			String name = "";
			String address = cursor.getString(cursor.getColumnIndex("address"));
			String person = cursor.getString(cursor.getColumnIndex("person"));
			String body = cursor.getString(cursor.getColumnIndex("body"));
			long date = cursor.getLong(cursor.getColumnIndex("date"));
			int type = cursor.getInt(cursor.getColumnIndex("type"));
			ImageView icon = (ImageView) view.findViewById(R.id.messageIcon);
			String emote = MFMessenger.identifyEmote(body);
			MFMessenger.log("I think you're "+emote);
			File photo = null;
			if( type == 2 ) {
				name = "Me";
				photo = MFMessenger.getEmoticonFile(emote);
				MFMessenger.log("My Photo: "+photo);
			} else {
				name = contactName;
				photo = MFMessenger.getEmoteIcon(address, emote);
				MFMessenger.log("Their Photo: "+photo);
				if( photo == null ) {
					photo = MFMessenger.getEmoticonFile(emote);
					MFMessenger.log("My Backup Photo: "+photo);
				}
			}
			if( photo == null ) {
				icon.setImageDrawable(getResources().getDrawable(android.R.drawable.sym_action_chat));
				MFMessenger.log("Default Photo");
			} else {
				icon.setImageDrawable(Drawable.createFromPath(photo.getPath()));
			}
			view.setTag(thread_id);
			((TextView) view.findViewById(R.id.messageSender))
				.setText(name);
			((TextView) view.findViewById(R.id.messageBody))
				.setText(body);
			((TextView) view.findViewById(R.id.messageTime))
				.setText("SENT "+dateFormat.format(new Date(date)));
		}
	}

	private void refreshList()
	{
		Cursor c = getContentResolver().query(Sms.CONTENT_URI,
				null, "thread_id = ?", new String[] {thread_id}, "date ASC");
		adapter = new SmsThreadAdapter(this, c);
		setListAdapter(adapter);
		getListView()
        	.setStackFromBottom(true);
        getListView()
        	.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	private class UpdateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			MFMessenger.log("Message update broadcast received!");
			refreshList();
		}
	}
}
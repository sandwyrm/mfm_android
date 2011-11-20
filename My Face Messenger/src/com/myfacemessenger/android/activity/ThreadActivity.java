package com.myfacemessenger.android.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
		thread_id = getIntent().getStringExtra("thread_id");
		address = getIntent().getStringExtra("address");
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
			MFMessenger.log("Pulling message type for message #"+position);
			Cursor c = getCursor();
			if( c.moveToPosition(position) ) {
				int type = c.getInt(c.getColumnIndex("type"));
				MFMessenger.log("Type: "+type);
				return type-1;
			} else {
				MFMessenger.log("Index did not exist");
				return 1;
			}
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
//			mutable = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//			canvas = new Canvas(mutable);
//			paint = new Paint();
//			paint.setFilterBitmap(false);
//
//			canvas.drawBitmap(bg1, 0, 0, paint);
//			canvas.drawBitmap(bg2, 0, 0, paint);
//			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
//			canvas.drawBitmap(mask, 0, 0, paint);
//			paint.setXfermode(null);
//
//			onDrawCanvas.drawBitmap(bitmap, 0, 0, paint);
			
			String name = "";
			String address = cursor.getString(cursor.getColumnIndex("address"));
			String person = cursor.getString(cursor.getColumnIndex("person"));
			String body = cursor.getString(cursor.getColumnIndex("body"));
			long date = cursor.getLong(cursor.getColumnIndex("date"));
			int type = cursor.getInt(cursor.getColumnIndex("type"));
			ImageView icon = (ImageView) view.findViewById(R.id.messageIcon);
			String emote = MFMessenger.identifyEmote(body);
			Bitmap photo = null;
			if( type == 2 ) {
				name = "Me";
				try {
					photo = BitmapFactory.decodeStream(new FileInputStream(MFMessenger.getEmoticonFile(emote)));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				name = contactName;
				photo = MFMessenger.getEmoteIcon(address, emote);
			}
			if( photo == null ) {
				icon.setImageDrawable(getResources().getDrawable(android.R.drawable.sym_action_chat));
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
}
package com.myfacemessenger.android.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;

import custom.android.provider.Telephony.Sms;

public class ThreadListActivity extends ListActivity
{
	private SmsInboxAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread_list);
		Cursor c = getContentResolver().query(Sms.CONTENT_URI, null, "type = ?) GROUP BY (address", new String[] {"1"}, "date DESC");
		adapter = new SmsInboxAdapter(this, c);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(threadClickListener);
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

	private OnItemClickListener threadClickListener =	//
		new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			String address = ((TextView) view.findViewById(R.id.threadContactName)).getTag().toString();
			Intent intent = new Intent(getBaseContext(), ThreadActivity.class);
			intent.putExtra("thread_id", view.getTag().toString());
			intent.putExtra("address", address);
			startActivity(intent);
		}
	};

	private class SmsInboxAdapter extends CursorAdapter
	{
		private SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, MMM d");

		public SmsInboxAdapter(Context context, Cursor c)
		{
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.thread_list_item, parent, false);
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			String thread_id = cursor.getString(cursor.getColumnIndex("thread_id"));
//			String person = cursor.getString(cursor.getColumnIndex("person"));
			String address = cursor.getString(cursor.getColumnIndex("address"));
			String body = cursor.getString(cursor.getColumnIndex("body"));
			long date = cursor.getLong(cursor.getColumnIndex("date"));
			String name = MFMessenger.getContactName(getContentResolver(), address);
			Bitmap photo = MFMessenger.getContactPhoto(getContentResolver(), address);
			view.setTag(thread_id);
			((TextView) view.findViewById(R.id.threadContactName))
				.setTag(address);
			((TextView) view.findViewById(R.id.threadContactName))
				.setText(name);
			((TextView) view.findViewById(R.id.threadLastMessage))
				.setText(body);
			((TextView) view.findViewById(R.id.threadLastTime))
				.setText(dateFormat.format(new Date(date)));
			if( photo != null ) {
				((ImageView) view.findViewById(R.id.threadIcon))
					.setImageBitmap(photo);
			} else {
				((ImageView) view.findViewById(R.id.threadIcon))
					.setImageDrawable(getResources().getDrawable(android.R.drawable.sym_action_chat));
			}
		}
	}
}
package com.myfacemessenger.android.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;

public class FaceManagerActivity extends Activity
{
	private static final String	ICON_DIRECTORY			= "MyFaceMessenger";

	private String[]			emoticons;
	private String[]			emoticon_names;
	private GridView			grid;
	private FaceIconAdapter		adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face_icon_manager);
		grid = (GridView) findViewById(R.id.iconGrid);
		emoticon_names = getResources().getStringArray(
			getResources().getIdentifier("emoticon_names", "array", MFMessenger.PACKAGE)
		);
		emoticons = getResources().getStringArray(
			getResources().getIdentifier("emoticons", "array", MFMessenger.PACKAGE)
		);
		adapter = new FaceIconAdapter(this);
		grid.setAdapter(adapter);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	private String getEmoticon(int position)
	{
		String emote = "";
		if( position < emoticons.length ) {
			emote = emoticons[position];
		}
		return emote;
	}

	private String getEmoticonName(int position)
	{
		String emote = "";
		if( position < emoticon_names.length ) {
			emote = emoticon_names[position];
		}
		return emote;
	}

	private File getIconDirectory()
	{
		File targetDir = new File(Environment.getExternalStorageDirectory(), ICON_DIRECTORY);
		if( !targetDir.exists() ) {
			targetDir.mkdirs();
		}
		return targetDir;
	}

	private File getEmoticonFile(String emotion)
	{
		return new File(getIconDirectory(), emotion + ".jpg");
	}

	private class FaceIconAdapter extends ArrayAdapter<String>
	{
		public FaceIconAdapter(Context context)
		{
			super(context, 0);
		}

		@Override
		public int getCount()
		{
			return emoticon_names.length;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			if( v == null ) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.face_icon_item, parent, false);
			}
			String emotion = getEmoticonName(position);
			File icon = getEmoticonFile(emotion);
			if( icon.exists() ) {
//				Uri uri = Uri.parse(icon.toURI().toString());
				((ImageView) v.findViewById(R.id.image_Face))
					.setImageDrawable(Drawable.createFromPath(icon.getPath()));
//					.setCompoundDrawablesWithIntrinsicBounds(null, Drawable.createFromPath(icon.getPath()), null, null);
//					.setImageURI(uri);
			} else {
				((ImageView) v.findViewById(R.id.image_Face))
//					.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(android.R.drawable.ic_menu_help), null, null);
					.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_help));
			}
			((TextView) v.findViewById(R.id.text_Emoticon))
				.setText(getEmoticon(position));
			v.setTag(emotion);
			v.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(getBaseContext(), FaceEditorActivity.class);
					intent.putExtra("emote", String.valueOf(v.getTag()));
					startActivity(intent);
//					showDialog(DIALOG_IMAGE_OPTIONS);
				}
			});
			return v;
		}
	}
}
package com.myfacemessenger.android.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;

public class FaceIconManagerActivity extends Activity
{
	private static final int	DIALOG_IMAGE_OPTIONS	= 100;

	private static final int	REQUEST_CODE_CAPTURE	= 100;
	private static final int	REQUEST_CODE_SELECT		= 101;

	private String[]			emoticons;
	private String[]			emoticon_names;
	private GridView			grid;
	private FaceIconAdapter		adapter;
	private String				currentEmote;

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
	protected Dialog onCreateDialog(int id)
	{
		switch( id ) {
			case DIALOG_IMAGE_OPTIONS:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder
					.setTitle("Set Face Icon")
					.setItems(R.array.image_source, sourceListener);
				return builder.create();
		}
		return super.onCreateDialog(id);
	}

	private android.content.DialogInterface.OnClickListener sourceListener =	//
		new android.content.DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			Intent intent = null;
			switch( which ) {
				case 0:
					intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					Uri captureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_face_icon_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, captureUri);
					try {
						intent.putExtra("return-data", true);
						startActivityForResult(intent, REQUEST_CODE_CAPTURE);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
			        }
					break;
				case 1:
					intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Complete action using"), REQUEST_CODE_SELECT);
					break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if( resultCode == RESULT_OK ) {
			switch( requestCode ) {
				default:
					MFMessenger.log("Result");
			}
		}
	}

	private String getEmoticon(int position)
	{
		String emote = "";
		if( position < emoticons.length ) {
			emote = emoticons[position];
		}
		return emote;
	}

//	private void addEmoticonButton(String emotion, String text)
//	{
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View v = inflater.inflate(R.layout.face_icon_item, null);
//		((Button) v.findViewById(R.id.bttn_setImage))
//			.setText(text + " ["+emotion+"]");
//		grid.addView(v);
//	}

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
			((Button) v.findViewById(R.id.bttn_setImage))
				.setText(getEmoticon(position));
			((Button) v.findViewById(R.id.bttn_setImage))
				.setTag(getEmoticon(position));
			((Button) v.findViewById(R.id.bttn_setImage))
				.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					currentEmote = String.valueOf(v.getTag());
					showDialog(DIALOG_IMAGE_OPTIONS);
				}
			});
			return v;
//			return super.getView(position, convertView, parent);
		}
	}
}
package com.myfacemessenger.android.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;
import com.myfacemessenger.android.service.IconUploadService;

public class FaceIconManagerActivity extends Activity
{
	private static final int	DIALOG_IMAGE_OPTIONS	= 100;
	private static final int	PICK_FROM_CAMERA		= 1;
	private static final int	CROP_FROM_CAMERA		= 2;
	private static final int	PICK_FROM_FILE			= 3;

	private static final String	ICON_DIRECTORY			= "MyFaceMessenger";

	private String[]			emoticons;
	private String[]			emoticon_names;
	private GridView			grid;
	private FaceIconAdapter		adapter;
	private String				currentEmote;
	private Uri					captureUri;

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
					captureUri = Uri.fromFile(getEmoticonFile(currentEmote));
					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, captureUri);
					try {
						intent.putExtra("return-data", true);
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
			        }
					break;
				case 1:
					intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
					break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if( resultCode == RESULT_OK ) {
			switch( requestCode ) {
				case PICK_FROM_CAMERA:
					cropSelection();
					break;
				case PICK_FROM_FILE:
					captureUri = data.getData();
					cropSelection();
					break;
				case CROP_FROM_CAMERA:
					Bundle extras = data.getExtras();
					if( extras != null ) {
						Bitmap temp = extras.getParcelable("data");
						int height = 100; //(temp.getHeight() / temp.getWidth()) * 48;
						int width = 100; //(temp.getWidth() / temp.getHeight()) * 48;
						Bitmap photo = Bitmap.createScaledBitmap(temp, width, height, true);
						MFMessenger.log("Acquired image data: "+photo.toString());
						try {
							FileOutputStream out = new FileOutputStream(new File(captureUri.getPath()));
							photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
						} catch( Exception e ) {
							e.printStackTrace();
						}
					}
					Intent intent = new Intent(this, IconUploadService.class);
					if( captureUri.getPath().matches(".*external.*") ) {
						intent.putExtra("file", currentEmote + ".jpg");
					} else {
						intent.putExtra("file", captureUri.getPath());
					}
					startService(intent);
					adapter.notifyDataSetChanged();
					break;
				default:
					MFMessenger.log("Unplanned result");
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

	private String getEmoticonName(int position)
	{
		String emote = "";
		if( position < emoticon_names.length ) {
			emote = emoticon_names[position];
		}
		return emote;
	}

	private void cropSelection()
	{
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
		int size = list.size();
		if( size == 0 ) {
			Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
			return;
		} else {
			intent.setData(captureUri);
			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);
			if( size == 1 ) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);
				i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for( ResolveInfo res : list ) {
					final CropOption co = new CropOption();
					co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);
					co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
					cropOptions.add(co);
				}
				CropOptionAdapter adapter = new CropOptionAdapter(this, cropOptions);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder
					.setTitle("Choose application to crop")
					.setAdapter(adapter, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							startActivityForResult(cropOptions.get(which).appIntent, CROP_FROM_CAMERA);
						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener()
					{
						@Override
						public void onCancel(DialogInterface dialog)
						{
							if( captureUri != null ) {
								getContentResolver().delete(captureUri, null, null);
								captureUri = null;
							}
						}
					});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
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
				Uri uri = Uri.parse(icon.toURI().toString());
				((ImageView) v.findViewById(R.id.image))
					.setImageURI(uri);
			} else {
				((ImageView) v.findViewById(R.id.image))
					.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_help));
			}
			((Button) v.findViewById(R.id.bttn_setImage))
				.setText(getEmoticon(position));
			((Button) v.findViewById(R.id.bttn_setImage))
				.setTag(emotion);
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
		}
	}

	private class CropOption
	{
		public CharSequence title;
		public Drawable icon;
		public Intent appIntent;
	}

	public class CropOptionAdapter extends ArrayAdapter<CropOption>
	{
		private ArrayList<CropOption>	mOptions;
		private LayoutInflater			mInflater;

		public CropOptionAdapter(Context context, ArrayList<CropOption> options)
		{
			super(context, R.layout.crop_selector, options);
			mOptions = options;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group)
		{
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.crop_selector, null);
			CropOption item = mOptions.get(position);
			if (item != null) {
				((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
				((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);
				return convertView;
			}
			return null;
		}
	}
}
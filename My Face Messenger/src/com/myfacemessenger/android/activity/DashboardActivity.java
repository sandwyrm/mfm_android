package com.myfacemessenger.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.myfacemessenger.android.R;
import com.myfacemessenger.android.service.IconUploadService;

public class DashboardActivity extends Activity
{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		((Button) findViewById(R.id.getImage))
			.setOnClickListener(imageSelectionListener);
	}

	private OnClickListener imageSelectionListener =	//
		new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(getBaseContext(), FaceIconManagerActivity.class);
			startActivity(intent);
		}
	};
}
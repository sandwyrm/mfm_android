package com.myfacemessenger.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;
import com.myfacemessenger.android.service.IconUploadService;

public class DashboardActivity extends Activity
{
	private static final int	REQUEST_CODE_REGISTER	= 0x001;
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
		if( MFMessenger.preferences.getBoolean(MFMessenger.PREF_FIRST_RUN, true) ) {
			startRegistration();
		} else {
			startThreadList();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch( requestCode ) {
			case REQUEST_CODE_REGISTER:
				if( resultCode == RESULT_OK ) {
					startThreadList();
				}
				break;
			default:
				finish();
//				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void startRegistration()
	{
		Intent intent = new Intent(getBaseContext(), RegistrationActivity.class);
		startActivityForResult(intent, REQUEST_CODE_REGISTER);
	}

	private void startThreadList()
	{
		Intent intent = new Intent(getBaseContext(), ThreadListActivity.class);
		startActivity(intent);
	}
}
package com.myfacemessenger.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.myfacemessenger.android.MFMessenger;
import com.myfacemessenger.android.R;
import com.myfacemessenger.android.api.APIHandler;

public class RegistrationActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		((EditText) findViewById(R.id.input_Phone))
			.setText(PhoneNumberUtils.formatNumber(MFMessenger.getDevicePhoneId(this)));
		((EditText) findViewById(R.id.input_Phone))
			.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		((Button) findViewById(R.id.button_Register))
			.setOnClickListener(clickListener_Registration);
	}

	private void register()
	{
		String phone = ((EditText) findViewById(R.id.input_Phone)).getText().toString();
		String email = ((EditText) findViewById(R.id.input_Email)).getText().toString();
		phone = PhoneNumberUtils.formatNumber(phone);
		new RegistrationTask(phone, email).execute((Void) null);
	}

	private void registrationComplete()
	{
		String phone = ((EditText) findViewById(R.id.input_Phone)).getText().toString();
		SharedPreferences.Editor editor = MFMessenger.preferences.edit();
		editor.putBoolean(MFMessenger.PREF_FIRST_RUN, false);
		editor.putString(MFMessenger.PREF_DEVICE_ID, PhoneNumberUtils.formatNumber(phone));
		editor.commit();
		finish();
	}

	private OnClickListener clickListener_Registration	= //
		new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			register();
		}
	};

	private class RegistrationTask extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog	dialog_Progress	= new ProgressDialog(RegistrationActivity.this);
		private String			phone;
		private String			email;

		public RegistrationTask(String phone, String email)
		{
			this.phone = phone;
			this.email = email;
		}

		@Override
		protected void onPreExecute()
		{
			dialog_Progress.setMessage("Registering...");
			dialog_Progress.show();
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			APIHandler.registerUser(phone, email);
			return null;
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
			if( dialog_Progress.isShowing() ) {
				dialog_Progress.dismiss();
			}
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			if( dialog_Progress.isShowing() ) {
				dialog_Progress.dismiss();
			}
			registrationComplete();
		}
	}
}
package com.myfacemessenger.android.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.myfacemessenger.android.R;

public class SettingsActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.default_preferences);
	}
}
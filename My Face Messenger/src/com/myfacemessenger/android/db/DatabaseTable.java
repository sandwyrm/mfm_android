package com.myfacemessenger.android.db;

import com.myfacemessenger.android.MFMessenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseTable
{
	protected SQLiteDatabase	db			= null;

	public Context			mContext	= null;

	public DatabaseTable(Context context, SQLiteDatabase database)
	{
		mContext = context;
		db = database;
	}

	protected void log(String message)
	{
		MFMessenger.log("[DB] "+message);
	}
}
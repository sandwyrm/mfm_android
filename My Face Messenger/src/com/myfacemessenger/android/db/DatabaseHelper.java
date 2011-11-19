package com.myfacemessenger.android.db;

import com.myfacemessenger.android.MFMessenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
	public static final String	DATABASE_FILENAME	= "MyFaceMessenger.db";
	public static final int		DATABASE_VERSION	= 1;

	private Context				mContext			= null;

	public DatabaseHelper(Context context, CursorFactory factory)
	{
		super(context, DATABASE_FILENAME, factory, DATABASE_VERSION);
		MFMessenger.log(Log.INFO, "DatabaseHelper initializing");
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		MFMessenger.log(Log.INFO, "Creating database");
		db.execSQL(getSqlString("CREATE_icons"));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		MFMessenger.log(Log.INFO, "Upgrading database");
		db.execSQL(String.format("DROP", "icons"));
		onCreate(db);
	}

	private String getSqlString(String name)
	{
		return
			mContext.getResources().getString(
				mContext.getResources().getIdentifier("sql_"+name, "string", MFMessenger.PACKAGE)
			);
	}
}
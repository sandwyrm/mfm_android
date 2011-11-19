package com.myfacemessenger.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class IconTable extends DatabaseTable
{
	public static final String	TABLE_NAME		= "icons";
	public static final String	COLUMN_ID		= "_id";
	public static final String	COLUMN_CONTACT	= "contact";
	public static final String	COLUMN_EMOTION	= "emotion";
	public static final String	COLUMN_FILE		= "file";
	public static final String	COLUMN_UPDATED	= "updated";

	public IconTable(Context context, SQLiteDatabase database)
	{
		super(context, database);
	}

	public int find(String contact, String emotion)
	{
		String[] columns = new String[] {
			COLUMN_ID
		};
		String selection = COLUMN_CONTACT + " = ? AND " + COLUMN_EMOTION + " = ?";
		String[] selectionArgs = new String[] {
			contact,
			emotion
		};
		Cursor c = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		if( c.moveToFirst() ) {
			return c.getInt(c.getColumnIndex(COLUMN_ID));
		} else {
			return -1;
		}
	}

	public void insert(String contact, String emotion, String file)
	{
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_CONTACT, contact);
		cv.put(COLUMN_EMOTION, emotion);
		cv.put(COLUMN_FILE, file);
		cv.put(COLUMN_UPDATED, System.currentTimeMillis());
		db.insert(TABLE_NAME, COLUMN_ID, cv);
	}

	public void update(int id, String file)
	{
		String where = COLUMN_ID + " = ?";
		String[] whereArgs = new String[] {
			String.valueOf(id)
		};
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_FILE, file);
		db.update(TABLE_NAME, cv, where, whereArgs);
	}
}
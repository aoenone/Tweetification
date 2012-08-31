package com.owentech.tweetification;

import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database
{

	SQLiteDatabase db;
	Context ctx;
	private static final String TAG = "Tweetification";

	public Database(Context ctx)
	{
		this.ctx = ctx;
	}

	/*********************************/
	/* Method to connect to database */
	/*********************************/
	public void connectDB()
	{
		db = ctx.openOrCreateDatabase("Tweetification.db",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
	}

	/***************************************/
	/* Method to close database connection */
	/***************************************/
	public void closeDB()
	{
		db.close();
	}

	/**************************************/
	/* Method to create table in database */
	/**************************************/
	public void createTable()
	{
		Log.i(TAG, "Creating SQLITE tables");
		db.setVersion(1);
		db.setLocale(Locale.getDefault());
		db.setLockingEnabled(true);

		final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS tbl_tweetification ("
				+ "username TEXT PRIMARY KEY," + "name TEXT);";

		db.execSQL(CREATE_USER_TABLE);

		final String CREATE_NOTIF_TABLE = "CREATE TABLE IF NOT EXISTS tbl_notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, message TEST);";
		
		db.execSQL(CREATE_NOTIF_TABLE);
		
	}

	/*************************************************/
	/* Method to insert new Twitter user in database */
	/*************************************************/
	public void insertUser(String username, String name)
	{
		Log.i(TAG, "Inserting user into SQLITE");
		String CREATE_USER = "INSERT INTO 'tbl_tweetification' "
				+ "(username, name)" + "VALUES" + "('" + username + "','"
				+ name + "');";

		db.execSQL(CREATE_USER);
	}
	
	/*************************************************/
	/* Method to insert new Notification in database */
	/*************************************************/
	public void insertNotification(String message)
	{
		Log.i(TAG, "Inserting user into SQLITE");
		
		ContentValues values = new ContentValues();
	    values.put("message", message); 

	    db.insert("tbl_notifications", null, values);
	
	}
	
	/******************************************/
	/* Method to count notifications in table */
	/******************************************/
	public int countNotifications()
	{
		Cursor mCount = db.rawQuery(
				"SELECT count(*) FROM tbl_notifications", null);
		mCount.moveToFirst();
		int total = mCount.getInt(0);
		mCount.close();
		
		return total;
	}
	
	
	/******************************************/
	/* Method to get all notification records */
	/******************************************/
	public Cursor getAllNotifications()
	{
		Cursor notifications = db.query("tbl_notifications", null, null, null, null,
                null, "id ASC", null);
		
		return notifications;
	}
	

	/***************************************/
	/* Method to delete user from database */
	/***************************************/
	public void deleteUser(String username)
	{
		Log.i(TAG, "Deleting user from SQLITE");
		String DELETE_USER = "DELETE FROM 'tbl_tweetification' WHERE username = '"
				+ username + "';";

		db.execSQL(DELETE_USER);
	}

	/***************************************************/
	/* Method to check whether user exists in database */
	/***************************************************/
	public boolean userExists(String username)
	{

		boolean exists;

		Cursor mCount = db.rawQuery(
				"SELECT count(*) FROM tbl_tweetification WHERE username = '"
						+ username + "'", null);
		mCount.moveToFirst();
		int total = mCount.getInt(0);
		mCount.close();

		if (total == 0)
		{
			exists = false;
		}
		else
		{
			exists = true;
		}

		return exists;
	}

	/************************************************/
	/* Method to get name of username from database */
	/************************************************/
	public String getName(String username)
	{

		String name;

		Cursor PopulateCursor = db.query("tbl_tweetification", null,
				"username = '" + username + "'", null, null, null, null, null);
		PopulateCursor.moveToFirst();

		name = PopulateCursor.getString(1);

		return name;

	}

	/**********************************/
	/* Method to clear database table */
	/**********************************/
	public void clearDB()
	{
		Log.i(TAG, "Clearing SQLITE database");
		db.execSQL("DELETE FROM tbl_tweetification");
		db.execSQL("DELETE FROM tbl_notifications");
	}
	
	
	/***************************************/
	/* Method to clear notifications table */
	/***************************************/
	public void clearNotificationsTable()
	{
		Log.i(TAG, "Clearing notifications table");
		db.execSQL("DELETE FROM tbl_notifications");
	}

}

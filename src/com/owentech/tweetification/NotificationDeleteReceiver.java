package com.owentech.tweetification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationDeleteReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{

		Log.i("Tweetififcation", "DeleteIntent called");
		Database db = new Database(context);
		db.connectDB();
		db.clearNotificationsTable();
		db.closeDB();
		
	}

}

package com.owentech.tweetification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotificationClickReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{

		Log.i("Tweetififcation", "Click Intent called");

		Database db = new Database(context);
		db.connectDB();
		db.clearNotificationsTable();
		db.closeDB();
		
		// setup shared preferences
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);

		String app = sp.getString("application", "none");

		Intent LaunchIntent = context.getPackageManager()
				.getLaunchIntentForPackage(app);
		
		context.startActivity(LaunchIntent);
		
	}

}

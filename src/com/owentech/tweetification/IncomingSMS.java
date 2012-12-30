package com.owentech.tweetification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class IncomingSMS extends BroadcastReceiver
{

	SharedPreferences sp;
	NotificationManager nm;

	String messageReceived;

	//private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String TAG = "Tweetification";

	@Override
	public void onReceive(Context context, Intent intent)
	{

		// setup shared preferences
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);

		Log.i(TAG, "SMS received.");

		// if (sp.getBoolean("enabled", false) == true)
		if (!sp.getString("number", "notset").equals("notset")
				&& !sp.getString("application", "notset").equals("notset"))
		{

			Bundle bundle = intent.getExtras();
			if (bundle != null)
			{
				Object[] pdus = (Object[]) bundle.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];

				for (int i = 0; i < pdus.length; i++)
				{
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				if (messages.length > -1)
				{
					if (messages[0].getOriginatingAddress().equals(
							sp.getString("number", "none")))
					{
						Log.i(TAG, "Aborting Broadcast");
						abortBroadcast();
						Log.i(TAG,
								"Message recieved from: "
										+ messages[0].getOriginatingAddress());

						messageReceived = messages[0].getMessageBody();

						int notifID = sp.getInt("lastNotification", 0) + 1;
						SharedPreferences.Editor editor = sp.edit();
						editor.putInt("lastNotification", notifID);
						editor.commit();

						Notify notify = new Notify(context, messageReceived,
								notifID, sp.getBoolean("networkenabled", true));
						notify.execute();

					}

				}
			}
		}
		else
		{
			Toast.makeText(
					context,
					"Tweetification received a message, but isn't configured to handle it yet. Please setup the application to launch and incoming number",
					Toast.LENGTH_LONG).show();
		}

	}
}

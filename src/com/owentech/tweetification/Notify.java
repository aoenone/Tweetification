package com.owentech.tweetification;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.support.v4.app.NotificationCompat;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressLint("NewApi")
public class Notify extends AsyncTask<Void, Long, Boolean>
{

	private static final String TAG = "Tweetification";

	Context ctx;
	String username = "notset";
	String messageReceived = "notset";
	String messageTitle = "notset";
	String imgURL = "notset";
	String name = "notset";
	boolean imageReceived;
	boolean networkEnabled;
	int notifID;
	int firstAt;
	int endAt;

	Database db;
	ImageHelper ih;

	Bitmap largeIcon;
	Bitmap myBitmap;

	SharedPreferences sp;
	NotificationManager nm;

	public Notify(Context ctx, String messageReceived, int notifID,
			boolean networkEnabled)
	{
		this.ctx = ctx;
		this.messageReceived = messageReceived;
		this.notifID = notifID;
		this.networkEnabled = networkEnabled;
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{

		// Toast.makeText(ctx, "Number in List: " +
		// Integer.toString(makeFilterList().size()),
		// Toast.LENGTH_SHORT).show();
		Log.d(TAG,
				"Number in List: " + Integer.toString(makeFilterList().size()));
		if (matchesFilterList())
		{
			Log.d(TAG, "Matches Filter List");
			return false;
		}
		else
		{
			Log.d(TAG, "Does Not Match Filter List");

			// parse through the message for the first username
			username = getUsername(messageReceived);

			// if network not enabled make username the name
			if (!networkEnabled)
			{
				name = "@" + username;
			}

			// if network is enabled download and store avatar & name
			if (networkEnabled)
			{

				db = new Database(ctx);
				ih = new ImageHelper();

				db.connectDB();
				db.createTable();

				// check if the user has already been saved locally
				if (db.userExists(username))
				{

					Log.i(TAG, "Using locally stored image");

					// check avatar exists locally and set bitmap
					if (ih.avatarExists(username))
					{
						// get the locally stored avatar
						largeIcon = ih.getLocal(username, true);
						imageReceived = true;
						// get the locally stored name
						name = db.getName(username);
					}
					else
					{
						// user file doesn't exist, delete user from db
						db.deleteUser(username);
						imageReceived = false;
						// set the username as name
						name = "@" + username;
					}

				}

				// username does not exist get from Twitter REST API and add to
				// db
				else
				{

					Log.i(TAG, "Using REST API");

					URL url;
					try
					{
						url = new URL(
								"https://api.twitter.com/1/users/show.xml?screen_name="
										+ username + "&include_entities=false");
						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();

						Log.i(TAG, "REST URL: " + url.toString());

						if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
						{
							DocumentBuilderFactory dbf = DocumentBuilderFactory
									.newInstance();
							DocumentBuilder db = dbf.newDocumentBuilder();
							Document doc;
							doc = db.parse(url.openStream());
							doc.getDocumentElement().normalize();
							NodeList itemLst = doc.getElementsByTagName("user");

							for (int a = 0; a < itemLst.getLength(); a++)
							{

								Node item = itemLst.item(a);
								if (item.getNodeType() == Node.ELEMENT_NODE)
								{
									Element ielem = (Element) item;
									NodeList imageURL = ielem
											.getElementsByTagName("profile_image_url");
									NodeList mName = ielem
											.getElementsByTagName("name");

									imgURL = imageURL.item(0).getChildNodes()
											.item(0).getNodeValue();

									name = mName.item(0).getChildNodes()
											.item(0).getNodeValue();

									Log.i("Tweetification", imgURL);

								}

							}

						}

					}
					catch (MalformedURLException e)
					{
						Log.i(TAG, "Error: Malformed URL");
						e.printStackTrace();
					}
					catch (DOMException e)
					{
						Log.i(TAG, "Error: DOM Exception");
						e.printStackTrace();
					}
					catch (IOException e)
					{
						Log.i(TAG, "Error: IO Exception");
						e.printStackTrace();
					}
					catch (ParserConfigurationException e)
					{
						Log.i(TAG, "Error: Parser Configuration Exception");
						e.printStackTrace();
					}
					catch (SAXException e)
					{
						Log.i(TAG, "Error: SAX Exception");
						e.printStackTrace();
					}
					catch (Exception e)
					{
						Log.i(TAG, "Error: Unknown Exception");
						e.printStackTrace();
					}
					finally
					{

						if (name.equals("notset"))
						{
							name = "@" + username;
						}

						if (imgURL.equals("notset"))
						{
							imageReceived = false;
						}
						else
						{
							imageReceived = true;

							try
							{
								largeIcon = ih.downloadImageFromUrl(imgURL,
										username + ".jpg");
								largeIcon = ih.resizeBitmap(largeIcon,
										largeIcon.getHeight() * 2,
										largeIcon.getWidth() * 2);

							}
							catch (Exception e)
							{
								e.printStackTrace();
								largeIcon = null;
								imageReceived = false;
							}

						}

						db.insertUser(username, name);

					}
				}
			}

			return true;
		}
	}

	@Override
	protected void onPostExecute(Boolean result)
	{

		super.onPostExecute(result);

		if (result)
		{

			db.closeDB();

			// setup shared preferences
			final SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);

			String app = sp.getString("application", "none");

			Intent LaunchIntent = ctx.getPackageManager()
					.getLaunchIntentForPackage(app);

			PendingIntent contentIntent = null;

			contentIntent = PendingIntent.getActivity(ctx, 0, LaunchIntent,
					PendingIntent.FLAG_ONE_SHOT);

			showInboxNotification(contentIntent, sp);
		}

	}

	/*******************************/
	/* Method to show notification */
	/*******************************/
	public void showNotification(PendingIntent contentIntent,
			SharedPreferences sp)
	{

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) ctx
				.getSystemService(ns);

		PendingIntent pi = contentIntent;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
		builder.setContentTitle(name).setContentText(messageReceived);
		builder.setSmallIcon(R.drawable.small2);

		if (imageReceived != false && networkEnabled == true)
		{
			if (largeIcon != null)
			{
				builder.setLargeIcon(largeIcon);
			}
		}

		builder.setAutoCancel(true);
		builder.setSound(Uri.parse(sp.getString("notUri", "notset")));
		builder.setContentIntent(pi);


		Notification notification = new NotificationCompat.BigTextStyle(builder)
				.bigText(messageReceived).build();

		mNotificationManager.notify(notifID, notification);

	}

	/*************************************/
	/* Method to show inbox notification */
	/*************************************/
	public void showInboxNotification(PendingIntent contentIntent,
			SharedPreferences sp)
	{

		String ns = Context.NOTIFICATION_SERVICE;

		NotificationManager mNotificationManager = (NotificationManager) ctx
				.getSystemService(ns);

		PendingIntent pi = contentIntent;
		Notification noti = null;
		NotificationCompat.Builder inboxBuilder = null;

		db.connectDB();
		db.createTable();
		db.insertNotification(messageReceived);

		int numberOfNotifications = db.countNotifications();
		db.closeDB();

		if (numberOfNotifications == 1)
		{
			// add new notification and display notification

			/*
			 * Bitmap testBitmap1 = ih.getLocal("marcoarment", false); Bitmap
			 * testBitmap2 = ih.getLocal("parislemon", false); Bitmap
			 * testBitmap3 = ih.getLocal("tunitowen", false); Bitmap testBitmap4
			 * = ih.getLocal("jp_hero", false); Bitmap collage =
			 * ih.makeCollage(new Bitmap[] {testBitmap1, testBitmap2,
			 * testBitmap3, testBitmap4});
			 */

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					ctx)
					.setContentTitle(name)
					.setContentText(messageReceived)
					.setSmallIcon(R.drawable.ic_stat_bird)
					.setAutoCancel(true)
					.setContentIntent(
							PendingIntent.getBroadcast(ctx, 0, new Intent(ctx,
									NotificationClickReceiver.class), 0))
					.setDeleteIntent(
							PendingIntent.getBroadcast(ctx, 0, new Intent(ctx,
									NotificationDeleteReceiver.class), 0));

			if (!sp.getBoolean("silent", false))
			{
				builder.setSound(Uri.parse(sp.getString("notUri", "notset")));
			}

			if (sp.getBoolean("vibrate", false))
			{
				builder.setDefaults(Notification.DEFAULT_VIBRATE);
			}

			if (imageReceived != false && networkEnabled == true)
			{
				if (largeIcon != null)
				{
					builder.setLargeIcon(largeIcon);
				}
			}

			noti = new NotificationCompat.BigTextStyle(builder).bigText(
					messageReceived).build();

			mNotificationManager.notify(1024, noti);

		}
		else
		{
			// how many notifications and display notification
			inboxBuilder = new NotificationCompat.Builder(ctx)
					.setContentTitle(
							String.valueOf(numberOfNotifications)
									+ " Tweetifications")
					.setContentText(messageReceived)
					.setSmallIcon(R.drawable.ic_stat_bird)
					.setAutoCancel(true)
					.setContentIntent(
							PendingIntent.getBroadcast(ctx, 0, new Intent(ctx,
									NotificationClickReceiver.class), 0))
					.setDeleteIntent(
							PendingIntent.getBroadcast(ctx, 0, new Intent(ctx,
									NotificationDeleteReceiver.class), 0))
					.setSound(Uri.parse(sp.getString("notUri", "notset")));

			NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle(
					inboxBuilder);

			db.connectDB();
			Cursor notificationRecords = db.getAllNotifications();
			notificationRecords.moveToFirst();

			String[] users = new String[notificationRecords.getCount()];
			String[] messages = new String[notificationRecords.getCount()];

			int a = 0;

			// get all notifications stored in db and assign username and
			// messages to arrays
			while (notificationRecords.isAfterLast() == false)
			{
				users[a] = getUsername(notificationRecords.getString(1));
				messages[a] = notificationRecords.getString(1);
				a++;
				notificationRecords.moveToNext();
			}

			Log.i(TAG, "Array length: " + String.valueOf(users.length));
			Log.i(TAG, Arrays.toString(users));

			notificationRecords.close();

			// count number of bitmaps that exist locally
			int localBitmaps = 0;

			for (int i = 0; i < users.length; i++)
			{
				if (ih.avatarExists(users[i]))
				{
					localBitmaps++;
				}
			}

			Log.i(TAG, "local Bitmaps: " + String.valueOf(localBitmaps));

			// create bitmap array to correct size
			Bitmap[] bitmapArray;

			if (localBitmaps <= 4)
			{
				bitmapArray = new Bitmap[localBitmaps];
			}
			else
			{
				bitmapArray = new Bitmap[4];
			}

			int more = 0;
			int current;

			for (current = 1; current < users.length + 1; current++)
			{
				if (current > 4)
				{
					more++;
				}
				else
				{

					if (users.length == localBitmaps)
					{
						bitmapArray[current - 1] = ih.getLocal(
								users[current - 1], false);
					}
					Log.i(TAG, users[current - 1]);

					inbox.addLine(messages[current - 1]);
				}

			}

			Log.i(TAG,
					"BitmapArray length: " + String.valueOf(bitmapArray.length));

			if (users.length == localBitmaps)
			{
				inboxBuilder.setLargeIcon(ih.makeCollage(bitmapArray));
			}

			if (more != 0)
			{
				inbox.setSummaryText("+ " + String.valueOf(more) + " more");
			}
			db.closeDB();

			mNotificationManager.notify(1024, inbox.build());

		}

	}

	/*****************************************/
	/* Method to parse message for @username */
	/*****************************************/
	public String getUsername(String messageReceived)
	{

		firstAt = messageReceived.indexOf("@");
		endAt = 0;

		int i = 0;

		do
		{
			i++;
			endAt = firstAt + i;
		}
		while (messageReceived.charAt(firstAt + i) != ' '
				&& messageReceived.charAt(firstAt + i) != ':');

		return messageReceived.substring(firstAt + 1, endAt);

	}

	// //////////////////////////////////////////
	// Method to make a list from filter words
	// //////////////////////////////////////////
	List<String> makeFilterList()
	{
		List<String> list = new LinkedList<String>();

		// setup shared preferences
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);

		if (!sp.getString("filter1", "").equals(""))
		{
			list.add(sp.getString("filter1", ""));
		}
		if (!sp.getString("filter2", "").equals(""))
		{
			list.add(sp.getString("filter2", ""));
		}
		if (!sp.getString("filter3", "").equals(""))
		{
			list.add(sp.getString("filter3", ""));
		}
		if (!sp.getString("filter4", "").equals(""))
		{
			list.add(sp.getString("filter4", ""));
		}
		if (!sp.getString("filter5", "").equals(""))
		{
			list.add(sp.getString("filter5", ""));
		}

		return list;

	}

	// ////////////////////////////////////////////
	// Method to match filter and return boolean
	// ////////////////////////////////////////////
	boolean matchesFilterList()
	{

		List<String> list = makeFilterList();

		if (list.size() > 0)
		{
			for (int i = 0; i < list.size(); i++)
			{
				// check word with a space after, and before & after
				if (messageReceived.toLowerCase().contains(
						list.get(i).toLowerCase() + " ")
						|| messageReceived.toLowerCase().contains(
								" " + list.get(i).toLowerCase() + " ")
						|| messageReceived.toLowerCase().contains(
								" " + list.get(i).toLowerCase()))
				{
					return true;
				}
			}
		}

		return false;
	}

}

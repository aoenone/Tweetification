package com.owentech.tweetification;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity
{

	Button btnApp;
	Button btnNotification;
	EditText txtNumber;
	ToggleButton togEnable;
	TextView txtChange;
	CheckBox chkEnable;

	SharedPreferences sp;

	private static final String TAG = "Tweetification";

	static final int RINGTONE_DIALOG_ID = 1001;
	static final int APPSELECTED = 1002;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setup shared preferences
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		btnApp = (Button) findViewById(R.id.button1);
		btnNotification = (Button) findViewById(R.id.button2);
		txtNumber = (EditText) findViewById(R.id.editText1);
		togEnable = (ToggleButton) findViewById(R.id.toggleButton1);
		txtChange = (TextView) findViewById(R.id.textView3);
		chkEnable = (CheckBox) findViewById(R.id.checkBox1);

		// set up values if set
		if (sp.getString("number", "none") == "none")
		{
			Log.i(TAG, "Nothing set yet");
			txtChange.setVisibility(TextView.INVISIBLE);
			chkEnable.setChecked(true);
		}
		else
		{

			txtNumber.setText(sp.getString("number", "none"));

			Variables.appName = sp.getString("appname", "none");
			Variables.appPackage = sp.getString("apppackage", "none");

			btnApp.setText(Variables.appName);

			Variables.ringtoneTitle = sp.getString("notName", "nothing");
			Variables.ringtoneUri = Uri
					.parse(sp.getString("notUri", "nothing"));

			btnNotification.setText(Variables.ringtoneTitle);

			togEnable.setChecked(sp.getBoolean("enabled", false));
			
			chkEnable.setChecked(sp.getBoolean("networkenabled", true));

			if (togEnable.isChecked())
			{
				enableDisable(false);
				txtChange.setVisibility(TextView.VISIBLE);
			}
			else
			{
				enableDisable(true);
				txtChange.setVisibility(TextView.INVISIBLE);
			}

		}

		btnApp.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{

				startActivityForResult(new Intent(
						"com.owentech.tweetification.SELECT"), APPSELECTED);

			}

		});

		btnNotification.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{

				Intent ringtoneIntent = new Intent(
						RingtoneManager.ACTION_RINGTONE_PICKER);
				ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
						RingtoneManager.TYPE_NOTIFICATION);
				ringtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
						"Select Sound");
				ringtoneIntent.putExtra(
						RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
				ringtoneIntent.putExtra(
						RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
				startActivityForResult(ringtoneIntent, RINGTONE_DIALOG_ID);

			}

		});

		togEnable.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1)
			{

				boolean on = togEnable.isChecked();

				SharedPreferences.Editor editor = sp.edit();

				if (on)
				{
					if (Variables.appName == null
							|| Variables.ringtoneTitle == null
							|| txtNumber.getText().length() == 0)
					{
						togEnable.setChecked(false);
						Toast.makeText(getApplicationContext(),
								"You have not completed all fields",
								Toast.LENGTH_LONG).show();
					}
					else
					{
						enableDisable(false);
						txtChange.setVisibility(TextView.VISIBLE);

						editor.putString("number", txtNumber.getText()
								.toString());
						editor.putString("notName", Variables.ringtoneTitle);
						editor.putString("notUri",
								Variables.ringtoneUri.toString());
						editor.putString("appname", Variables.appName);
						editor.putString("apppackage", Variables.appPackage);

						editor.putBoolean("enabled", true);
						editor.putBoolean("networkenabled", chkEnable.isChecked());
						editor.commit();
					}
				}
				else
				{
					enableDisable(true);
					txtChange.setVisibility(TextView.INVISIBLE);

					editor.putBoolean("enabled", false);
					editor.commit();
				}

			}

		});

	}

	public void enableDisable(boolean b)
	{
		txtNumber.setEnabled(b);
		btnApp.setEnabled(b);
		btnNotification.setEnabled(b);
		chkEnable.setEnabled(b);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{

		case RINGTONE_DIALOG_ID:
		{

			if (resultCode == RESULT_OK)
			{
				Variables.ringtoneUri = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				Ringtone ringtone = RingtoneManager.getRingtone(
						getApplicationContext(), Variables.ringtoneUri);
				Variables.ringtoneTitle = ringtone
						.getTitle(getApplicationContext());

				btnNotification.setText(Variables.ringtoneTitle);
			}

			break;
		}

		case APPSELECTED:
		{

			Log.i(TAG, Variables.appName);

			if (resultCode == RESULT_OK)
			{

				btnApp.setText(Variables.appName);
			}

		}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Clear Cache").setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		menu.add(0, 1, 0, "Help").setIcon(R.drawable.ic_menu_help)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	/*********************/
	/* Menu click action */
	/*********************/

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);

		switch (item.getItemId())
		{
		
		case 0:
			Database db = new Database(this);
			db.connectDB();
			db.clearDB();
			db.closeDB();
			
			File dir = new File(Environment.getExternalStorageDirectory().toString()
					+ "/data/com.owentech.tweetification/");
			
			if (dir.isDirectory()) {
		        String[] children = dir.list();
		        for (int i = 0; i < children.length; i++) {
		            new File(dir, children[i]).delete();
		        }
		    }
			
			
			return true;

		case 1:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setMessage(
					"To use Tweetification you MUST setup SMS notifications in your Twitter account. Send yourself a tweet and make a note of the number the SMS is sent from, then add this number inside this app.")
					.setTitle("Help")
					.setIcon(R.drawable.ic_menu_help)
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog,
										int id)
								{

									dialog.cancel();

								}
							});

			AlertDialog alert = builder.create();
			alert.show();

			return true;

		default:
			break;
		}

		return false;
	}

	
}

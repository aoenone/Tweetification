package com.owentech.tweetification;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class Preferences extends SherlockPreferenceActivity
{

	public String[] appNames;
	public String[] appPackages;
	
	ListPreference listPref;
	RingtonePreference ringPref;
	EditTextPreference numberPref;
	
	EditTextPreference filter1Pref;
	EditTextPreference filter2Pref;
	EditTextPreference filter3Pref;
	EditTextPreference filter4Pref;
	EditTextPreference filter5Pref;
	
	PackageManager pm;
	List<ResolveInfo> list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		
		// setup components
		listPref = (ListPreference) findPreference("application");
		ringPref = (RingtonePreference) findPreference("notUri");
		numberPref = (EditTextPreference) findPreference("number");
		
		filter1Pref = (EditTextPreference) findPreference("filter1");
		filter2Pref = (EditTextPreference) findPreference("filter2");
		filter3Pref = (EditTextPreference) findPreference("filter3");
		filter4Pref = (EditTextPreference) findPreference("filter4");
		filter5Pref = (EditTextPreference) findPreference("filter5");

		// get installed applications
		pm = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		list = pm.queryIntentActivities(intent,
				PackageManager.PERMISSION_GRANTED);

		// sort the list alphabetically
		Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));
		
		// create the arrays
		appNames = new String[list.size()];
		appPackages = new String[list.size()];
		
		int i=0;
		
		for (ResolveInfo rInfo : list)
		{
			
			// populate arrays
			appNames[i] = rInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
			appPackages[i] = rInfo.activityInfo.applicationInfo.packageName.toString();
			
			i++;

		}

		// assign arrays to the application list preference
		listPref.setEntryValues(appPackages);
		listPref.setEntries(appNames);
		
		
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (!sp.getString("number", "notset").equals("notset"))
		{
			numberPref.setSummary(sp.getString("number", "notset"));
		}
		
		if(!sp.getString("notUri", "notset").equals("notset"))
		{
			ringPref.setSummary(getTitleFromRingtoneUri(sp.getString("notUri", "notset")));
		}
		
		if(!sp.getString("application", "notset").equals("notset"))
		{
			listPref.setSummary(getNameFromPackage(sp.getString("application", "notset")));
		}
		
		if(!sp.getString("filter1", "").equals(""))
		{
			filter1Pref.setSummary(sp.getString("filter1", ""));
		}
		if(!sp.getString("filter2", "").equals(""))
		{
			filter2Pref.setSummary(sp.getString("filter2", ""));
		}
		if(!sp.getString("filter3", "").equals(""))
		{
			filter3Pref.setSummary(sp.getString("filter3", ""));
		}
		if(!sp.getString("filter4", "").equals(""))
		{
			filter4Pref.setSummary(sp.getString("filter4", ""));
		}
		if(!sp.getString("filter5", "").equals(""))
		{
			filter5Pref.setSummary(sp.getString("filter5", ""));
		}

		
		OnPreferenceChangeListener filterChange = new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue)
			{
				preference.setSummary(newValue.toString());
				
				SharedPreferences.Editor editor = sp.edit();
				
				if(newValue.toString().length() == 0)
				{
					editor.putString(preference.getKey(), "");
					editor.commit();
				}
				else
				{
					editor.putString(preference.getKey(), newValue.toString());
					editor.commit();
				}
				
				return false;
			}
			
		};
		
		filter1Pref.setOnPreferenceChangeListener(filterChange);
		filter2Pref.setOnPreferenceChangeListener(filterChange);
		filter3Pref.setOnPreferenceChangeListener(filterChange);
		filter4Pref.setOnPreferenceChangeListener(filterChange);
		filter5Pref.setOnPreferenceChangeListener(filterChange);
		
		// on change listener for Application List
		listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue)
			{			
				listPref.setSummary(getNameFromPackage(newValue.toString()));
				
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("application", newValue.toString());
				editor.commit();
				
				return false;
			}

		});
		
		// on change listener for ringtone preference
		ringPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue)
			{
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("notUri", newValue.toString());
				editor.commit();
				
				ringPref.setSummary(getTitleFromRingtoneUri(newValue.toString()));	
				return false;
			}
			
		});
		
		// on change listener for the number preference
		numberPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue)
			{

				SharedPreferences.Editor editor = sp.edit();
				editor.putString("number", newValue.toString());
				editor.commit();
				
				numberPref.setSummary(newValue.toString());
				
				return false;
			}
			
		});

	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Clear Cache").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		menu.add(0, 1, 0, "Help").setIcon(R.drawable.ic_menu_help)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

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

			File dir = new File(Environment.getExternalStorageDirectory()
					.toString() + "/data/com.owentech.tweetification/");

			if (dir.isDirectory())
			{
				String[] children = dir.list();
				for (int i = 0; i < children.length; i++)
				{
					new File(dir, children[i]).delete();
				}
			}

			return true;

		case 1:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
			
			//startActivity(new Intent("com.owentech.tweetification.PREFERENCES"));

			return true;

		default:
			break;
		}

		return false;
	}
	
	// method to get app name from package
	String getNameFromPackage(String packageName)
	{
		
		for (ResolveInfo rInfo : list)
		{
			if (rInfo.activityInfo.applicationInfo.packageName.equals(packageName.toString()))
			{
				return rInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
			}
		}
		
		return null;
	}
	
	// method to get Ringtone title from ringtone uri
	String getTitleFromRingtoneUri(String r)
	{
		Uri ringtoneUri = Uri.parse(r);
		Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
		return ringtone.getTitle(getApplicationContext());
	}

}

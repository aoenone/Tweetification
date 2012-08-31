package com.owentech.tweetification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class SelectApp extends SherlockActivity
{

	ListView lv;
	Button btnConfirm;
	SharedPreferences sp;

	private ArrayList results = new ArrayList();
	private ArrayList resultsUnsorted = new ArrayList();
	private ArrayList resultsPackages = new ArrayList();

	private static final String TAG = "Tweetification";
	static int APPSELECTED = 1002;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);

		// setup shared preferences
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		lv = (ListView) findViewById(R.id.listView1);
		btnConfirm = (Button) findViewById(R.id.button2);

		PackageManager pm = this.getPackageManager();

		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> list = pm.queryIntentActivities(intent,
				PackageManager.PERMISSION_GRANTED);
		for (ResolveInfo rInfo : list)
		{
			results.add(rInfo.activityInfo.applicationInfo.loadLabel(pm)
					.toString());
			resultsUnsorted.add(rInfo.activityInfo.applicationInfo.loadLabel(pm)
					.toString());
			resultsPackages.add(rInfo.activityInfo.applicationInfo.packageName
					.toString());
			Log.w("Installed Applications", rInfo.activityInfo.applicationInfo
					.loadLabel(pm).toString());
		}
		
		Collections.sort(results);
		
		lv.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_list_item_single_choice, results));

		btnConfirm.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{

				if (lv.getCheckedItemPosition() < 0)
				{
					Toast.makeText(getApplicationContext(),
							"You must select an application",
							Toast.LENGTH_SHORT).show();
				}
				else
				{

					int i = lv.getCheckedItemPosition();
					Log.i(TAG, String.valueOf(i));
					
					String selectedItem = lv.getItemAtPosition(i).toString();
					int originalInt = resultsUnsorted.indexOf(selectedItem);
					String selectedPackage = resultsPackages.get(originalInt).toString();

					Variables.appName = results.get(i).toString();
					Variables.appPackage = resultsPackages.get(i).toString();
					
					Toast.makeText(getApplicationContext(),
							selectedPackage,
							Toast.LENGTH_LONG).show();

					SharedPreferences.Editor editor = sp.edit();
					editor.putString("application", selectedPackage);
					editor.commit();
					
					Intent in = new Intent();
					setResult(RESULT_OK, in);

					finish();
				}

			}

		});

	}

}

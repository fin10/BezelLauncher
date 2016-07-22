package com.fin10.bezellauncher;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity implements OnClickListener, android.content.DialogInterface.OnClickListener {

	private static final String TAG = "SettingsActivity";
	
	public static final String PREF_NAME = "PREF_NAME";
	public static final String PREF_KEY_ENABLED = "PREF_KEY_ENABLED";

	public static final String PREF_KEY_LEFT_PACKAGE_NAME = "PREF_KEY_LEFT_PACKAGE_NAME";
	public static final String PREF_KEY_LEFT_ACTIVITY_NAME = "PREF_KEY_LEFT_ACTIVITY_NAME";
	
	public static final String PREF_KEY_RIGHT_PACKAGE_NAME = "PREF_KEY_RIGHT_PACKAGE_NAME";
	public static final String PREF_KEY_RIGHT_ACTIVITY_NAME = "PREF_KEY_RIGHT_ACTIVITY_NAME";
	
	public static final String PREF_KEY_BOTTOM_PACKAGE_NAME = "PREF_KEY_BOTTOM_PACKAGE_NAME";
	public static final String PREF_KEY_BOTTOM_ACTIVITY_NAME = "PREF_KEY_BOTTOM_ACTIVITY_NAME";
	
	private List<ResolveInfo> mAppList;
	private AlertDialog mAppListDialog;
	
	private int mSelectedOption = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate:");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		boolean enabled = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(PREF_KEY_ENABLED, false);
		ToggleButton toogle = (ToggleButton) findViewById(R.id.toggle_button);
		toogle.setChecked(enabled);
		toogle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.d(TAG, "[onCheckedChanged] isChecked: " + Boolean.toString(isChecked));
				if (isChecked) {
					getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
					.putBoolean(PREF_KEY_ENABLED, true)
					.apply();
					startService(new Intent(SettingsActivity.this, LaunchService.class));
				} else {
					getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
					.putBoolean(PREF_KEY_ENABLED, false)
					.apply();
					stopService(new Intent(SettingsActivity.this, LaunchService.class));
				}
			}
		});
		
		View leftContainer = findViewById(R.id.left_option_container);
		leftContainer.setTag(R.id.left_option_container);
		leftContainer.setOnClickListener(this);

		View rightContainer = findViewById(R.id.right_option_container);
		rightContainer.setTag(R.id.right_option_container);
		rightContainer.setOnClickListener(this);

		View bottomContainer = findViewById(R.id.bottom_option_container);
		bottomContainer.setTag(R.id.bottom_option_container);
		bottomContainer.setOnClickListener(this);
		
		Intent baseIntent = new Intent(Intent.ACTION_MAIN);
		baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mAppList = getPackageManager().queryIntentActivities(baseIntent, 0);
		for (ResolveInfo info : mAppList) {
			Log.d(TAG, "activity info: " + info.activityInfo);
		}
		
		mAppListDialog = new AlertDialog.Builder(this)
		.setAdapter(new ApplicationAdapter(), this)
		.setTitle("Select an application")
		.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "clear, selected option: " + mSelectedOption);
				switch (mSelectedOption) {
				case R.id.left_option_container:
					getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
					.remove(PREF_KEY_LEFT_PACKAGE_NAME)
					.remove(PREF_KEY_LEFT_ACTIVITY_NAME)
					.apply();
					break;
				case R.id.right_option_container:
					getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
					.remove(PREF_KEY_RIGHT_PACKAGE_NAME)
					.remove(PREF_KEY_RIGHT_ACTIVITY_NAME)
					.apply();
					break;
				case R.id.bottom_option_container:
					getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
					.remove(PREF_KEY_BOTTOM_PACKAGE_NAME)
					.remove(PREF_KEY_BOTTOM_ACTIVITY_NAME)
					.apply();
					break;
				}
				update();
			}
		})
		.create();
		
		update();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy:");
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		mSelectedOption = (int) v.getTag();
		mAppListDialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Log.d(TAG, "[onClick] which: " + which + ", selected option: " + mSelectedOption);
		Log.d(TAG, "package: " + mAppList.get(which).activityInfo.name);
		switch (mSelectedOption) {
		case R.id.left_option_container:
			getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
			.putString(PREF_KEY_LEFT_PACKAGE_NAME, mAppList.get(which).activityInfo.packageName)
			.putString(PREF_KEY_LEFT_ACTIVITY_NAME, mAppList.get(which).activityInfo.name)
			.apply();
			break;
		case R.id.right_option_container:
			getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
			.putString(PREF_KEY_RIGHT_PACKAGE_NAME, mAppList.get(which).activityInfo.packageName)
			.putString(PREF_KEY_RIGHT_ACTIVITY_NAME, mAppList.get(which).activityInfo.name)
			.apply();
			break;
		case R.id.bottom_option_container:
			getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
			.putString(PREF_KEY_BOTTOM_PACKAGE_NAME, mAppList.get(which).activityInfo.packageName)
			.putString(PREF_KEY_BOTTOM_ACTIVITY_NAME, mAppList.get(which).activityInfo.name)
			.apply();
			break;
		}
		update();
	}
	
	private void update() {
		Log.d(TAG, "[update]");
		SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		String packageName = pref.getString(PREF_KEY_LEFT_PACKAGE_NAME, "");
		String activityName = pref.getString(PREF_KEY_LEFT_ACTIVITY_NAME, "");
		if (!packageName.isEmpty() && !activityName.isEmpty()) {
			Log.d(TAG, "left package: " + packageName + ", activity: " + activityName);
			try {
				ComponentName component = new ComponentName(packageName, activityName);
				ActivityInfo info = getPackageManager().getActivityInfo(component, 0);
				ImageView icon = (ImageView) findViewById(R.id.left_app_icon);
				TextView label = (TextView) findViewById(R.id.left_app_label);
				
				icon.setVisibility(View.VISIBLE);
				icon.setImageDrawable(info.loadIcon(getPackageManager()));
				icon.invalidate();
				label.setText(info.loadLabel(getPackageManager()));
				label.invalidate();
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.toString());
			}
		} else {
			ImageView icon = (ImageView) findViewById(R.id.left_app_icon);
			TextView label = (TextView) findViewById(R.id.left_app_label);

			icon.setImageDrawable(null);
			icon.setVisibility(View.GONE);
			icon.invalidate();
			label.setText(R.string.register_applicaion);
			label.invalidate();
		}

		packageName = pref.getString(PREF_KEY_RIGHT_PACKAGE_NAME, "");
		activityName = pref.getString(PREF_KEY_RIGHT_ACTIVITY_NAME, "");
		if (!packageName.isEmpty() && !activityName.isEmpty()) {
			Log.d(TAG, "right package: " + packageName + ", activity: " + activityName);
			try {
				ComponentName component = new ComponentName(packageName, activityName);
				ActivityInfo info = getPackageManager().getActivityInfo(component, 0);
				ImageView icon = (ImageView) findViewById(R.id.right_app_icon);
				TextView label = (TextView) findViewById(R.id.right_app_label);
				
				icon.setVisibility(View.VISIBLE);
				icon.setImageDrawable(info.loadIcon(getPackageManager()));
				icon.invalidate();
				label.setText(info.loadLabel(getPackageManager()));
				label.invalidate();
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.toString());
			}
		} else {
			ImageView icon = (ImageView) findViewById(R.id.right_app_icon);
			TextView label = (TextView) findViewById(R.id.right_app_label);

			icon.setImageDrawable(null);
			icon.setVisibility(View.GONE);
			icon.invalidate();
			label.setText(R.string.register_applicaion);
			label.invalidate();
		}

		packageName = pref.getString(PREF_KEY_BOTTOM_PACKAGE_NAME, "");
		activityName = pref.getString(PREF_KEY_BOTTOM_ACTIVITY_NAME, "");
		if (!packageName.isEmpty() && !activityName.isEmpty()) {
			Log.d(TAG, "bottom package: " + packageName + ", activity: " + activityName);
			try {
				ComponentName component = new ComponentName(packageName, activityName);
				ActivityInfo info = getPackageManager().getActivityInfo(component, 0);
				ImageView icon = (ImageView) findViewById(R.id.bottom_app_icon);
				TextView label = (TextView) findViewById(R.id.bottom_app_label);
				
				icon.setVisibility(View.VISIBLE);
				icon.setImageDrawable(info.loadIcon(getPackageManager()));
				icon.invalidate();
				label.setText(info.loadLabel(getPackageManager()));
				label.invalidate();
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.toString());
			}
		} else {
			ImageView icon = (ImageView) findViewById(R.id.bottom_app_icon);
			TextView label = (TextView) findViewById(R.id.bottom_app_label);

			icon.setImageDrawable(null);
			icon.setVisibility(View.GONE);
			icon.invalidate();
			label.setText(R.string.register_applicaion);
			label.invalidate();
		}
	}
	
	private class ApplicationAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mAppList != null ? mAppList.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mAppList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mAppList.get(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView appIcon;
			TextView appLabel;
			
			if (convertView == null) {
				convertView = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.app_list_item, parent, false);
				appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
				convertView.setTag(R.id.app_icon, appIcon);
				
				appLabel = (TextView) convertView.findViewById(R.id.app_label);
				convertView.setTag(R.id.app_label, appLabel);
			} else {
				appIcon = (ImageView) convertView.getTag(R.id.app_icon);
				appLabel = (TextView) convertView.getTag(R.id.app_label);
			}
			
			ResolveInfo info = mAppList.get(position);
			appIcon.setImageDrawable(info.loadIcon(getPackageManager()));
			appLabel.setText(info.loadLabel(getPackageManager()));
			
			return convertView;
		}
	}
}

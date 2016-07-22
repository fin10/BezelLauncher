package com.fin10.bezellauncher;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class LaunchService extends Service implements View.OnTouchListener {

	private static final String TAG = "LaunchService";
	private static final int SWIPE_MIN_DISTANCE = 15;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 60;

	private GestureDetector mLeftFlingDetector;
	private GestureDetector mRightFlingDetector;
	private View mLeftView;
	private View mRightView;
	private View mBottomView;
	private GestureDetector mBottomFlingDetector;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind: " + intent);
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate:");
		super.onCreate();
		Toast.makeText(this, R.string.ready_service, Toast.LENGTH_SHORT).show();
		unregisterRestartAlarm();

		mLeftFlingDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1 != null && e2 != null) {
					if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
						return false;

					Log.d(TAG, "e1.getX() - e2.getX(): " + (e1.getX() - e2.getX()));
					Log.d(TAG, "Math.abs(velocityX): " + Math.abs(velocityX));
					if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						Log.d(TAG, "left onFling:");
						SharedPreferences pref = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE);
						String packageName = pref.getString(SettingsActivity.PREF_KEY_LEFT_PACKAGE_NAME, "");
						String activityName = pref.getString(SettingsActivity.PREF_KEY_LEFT_ACTIVITY_NAME, "");
						launchApplication(packageName, activityName, R.anim.left_to_right);
						return true;
					}					
				}

				return false;
			}

		});

		mRightFlingDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1 != null && e2 != null) {
					if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
						return false;

					if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						Log.d(TAG, "right onFling:");
						SharedPreferences pref = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE);
						String packageName = pref.getString(SettingsActivity.PREF_KEY_RIGHT_PACKAGE_NAME, "");
						String activityName = pref.getString(SettingsActivity.PREF_KEY_RIGHT_ACTIVITY_NAME, "");
						launchApplication(packageName, activityName, R.anim.right_to_left);
						return true;
					}					
				}

				return false;
			}

		});

		mBottomFlingDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1 != null && e2 != null) {
					if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
						return false;

					if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
						Log.d(TAG, "bottom onFling:");
						SharedPreferences pref = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE);
						String packageName = pref.getString(SettingsActivity.PREF_KEY_BOTTOM_PACKAGE_NAME, "");
						String activityName = pref.getString(SettingsActivity.PREF_KEY_BOTTOM_ACTIVITY_NAME, "");
						launchApplication(packageName, activityName, R.anim.bottom_to_top);
						return true;
					}					
				}

				return false;
			}

		});

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				SWIPE_MIN_DISTANCE, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		mLeftView = new View(this);
		mLeftView.setTag(0);
		mLeftView.setOnTouchListener(this);
//		mLeftView.setBackgroundColor(Color.parseColor("#7fFF7F7F"));
		params.gravity = Gravity.START;
		wm.addView(mLeftView, params);

		mRightView = new View(this);
		mRightView.setTag(1);
		mRightView.setOnTouchListener(this);
//		mRightView.setBackgroundColor(Color.parseColor("#7fFF7F7F"));
		params.gravity = Gravity.END;
		wm.addView(mRightView, params);

		mBottomView = new View(this);
		mBottomView.setTag(2);
		mBottomView.setOnTouchListener(this);
//		mBottomView.setBackgroundColor(Color.parseColor("#7fFF7F7F"));
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = SWIPE_MIN_DISTANCE;
		params.gravity = Gravity.BOTTOM;
		wm.addView(mBottomView, params);

	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy:");
		super.onDestroy();
		boolean enabled = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE)
				.getBoolean(SettingsActivity.PREF_KEY_ENABLED, false);
		if (enabled) {
			registerRestartAlarm();
		} else {
			Toast.makeText(this, R.string.terminate_service, Toast.LENGTH_SHORT).show();
		}

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.removeView(mLeftView);
		wm.removeView(mRightView);
		wm.removeView(mBottomView);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "event: " + event);
		int tag = (int) v.getTag();
		switch (tag) {
		case 0:
			return mLeftFlingDetector.onTouchEvent(event);
		case 1:
			return mRightFlingDetector.onTouchEvent(event);
		case 2:
			return mBottomFlingDetector.onTouchEvent(event);
		}

		return false;
	}

	private void launchApplication(String packageName, String activityName, int enterAnimation) {
		if (!packageName.isEmpty() && !activityName.isEmpty()) {
			Log.d(TAG, "bottom package: " + packageName + ", activity: " + activityName);
			ComponentName component = new ComponentName(packageName, activityName);
			Intent intent = new Intent();
			intent.setComponent(component);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			ActivityOptions ops = ActivityOptions.makeCustomAnimation(LaunchService.this,
					enterAnimation, R.anim.fade_out);

			try {
				startActivity(intent, ops.toBundle());
			} catch (Exception e) {
				Toast.makeText(this, R.string.launch_error_message, Toast.LENGTH_SHORT).show();
				Log.e(TAG, e.toString());
			}

			try {
				ActivityInfo info = getPackageManager().getActivityInfo(component, 0);
				String msg = getResources().getString(R.string.launch_message, info.loadLabel(getPackageManager()));
				Toast.makeText(LaunchService.this, msg, Toast.LENGTH_SHORT).show();
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.toString());
			}
		}		
	}

	private void registerRestartAlarm() {
		Log.d(TAG, "registerRestartAlarm");
		Intent intent = new Intent(this, RestartReceiver.class);
		intent.setAction("ACTION.RESTART.LaunchService");
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += 10*1000;
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10*1000, sender);
	}

	private void unregisterRestartAlarm() {
		Log.d(TAG, "unregisterRestartAlarm");
		Intent intent = new Intent(this, RestartReceiver.class);
		intent.setAction("ACTION.RESTART.LaunchService");
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}

	public static class RestartReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("RestartReceiver", "RestartService called! :" + intent.getAction());

			if(intent.getAction().equals("ACTION.RESTART.LaunchService")
					|| intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){ 
				Log.d("RestartReceiver", "ACTION.RESTART.LaunchService");
				Intent i = new Intent(context,LaunchService.class);
				context.startService(i);
			} 
		}
	}
}

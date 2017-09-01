package com.example.radbeacontestingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WelcomeActivity extends Activity {
	/** Log for the WelcomeActivity. */
	private final static String WELCOME_ACTIVITY_LOG = "WELCOME_ACT_LOG";

	/** The UI component of the WelcomneActivity. */
	private Button mStartButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		final String checkCompatibleInfo = getCompatibility(this);
		// If not compatible, pop out a dialog and then close the app.
		if (checkCompatibleInfo != null) {
			Log.e(WELCOME_ACTIVITY_LOG, checkCompatibleInfo);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(checkCompatibleInfo).setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();					
				}
			});
		}
		
		mStartButton = (Button) findViewById(R.id.btn_welcome);
		mStartButton.setOnClickListener(startButtonOnClickListener);
		
		
	}

	/**
	 * OnClick Listener for the welcome button. When clicked, the App will go to
	 * the scan device activity.
	 */
	private OnClickListener startButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent tagSearchingActivityIntent = new Intent(
					getApplicationContext(), TagSearchingActivity.class);

			startActivity(tagSearchingActivityIntent);
		}
	};

	/**
	 * Check whether the device is compatible with BLE.
	 * 
	 * @param context
	 *            The activity context
	 * @return The incompatible reason or null if compatible
	 */
	public String getCompatibility(Context context) {
		if (android.os.Build.VERSION.SDK_INT < 18) {
			return "requires Android 4.3";
		}
		if (!context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return "requires Bluetooth LE";
		}
		return null;
	}
}

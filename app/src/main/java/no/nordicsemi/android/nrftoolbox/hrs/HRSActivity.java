/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.hrs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.achartengine.GraphicalView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;

/**
 * HRSActivity is the main Heart rate activity. It implements HRSManagerCallbacks to receive callbacks from HRSManager class. The activity supports portrait and landscape orientations. The activity
 * uses external library AChartEngine to show real time graph of HR values.
 */
// TODO The HRSActivity should be rewritten to use the service approach, like other do.
public class HRSActivity extends BleProfileActivity implements HRSManagerCallbacks {
	@SuppressWarnings("unused")
	private final String TAG = "HRSActivity";

	private final static String GRAPH_STATUS = "graph_status";
	private final static String GRAPH_COUNTER = "graph_counter";
	private final static String FINGER_VALUE = "hr_value";
	private final static String ENV_VALUE = "hr_value";
	private final static String OBJ_VALUE = "hr_value";

	private final static int MAX_HR_VALUE = 65535;
	private final static int MIN_POSITIVE_VALUE = 0;
	private final static int REFRESH_INTERVAL = 1000; // 1 second interval

	private Handler mHandler = new Handler();

	private boolean isGraphInProgress = false;

	private GraphicalView mGraphView;
	private LineGraphView mLineGraph;
	private TextView mFingerTextView, mEnvTextView, mObjTextView, mHRSPosition;

	private int mFingerValue = 0;
    private int mEnvValue = 0;
    private int mObjValue = 0;
	private int mCounter = 0;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hrs);
		setGUI();
	}

	private void setGUI() {
		mLineGraph = LineGraphView.getLineGraphView();
		mFingerTextView = (TextView) findViewById(R.id.text_hrs_value);
		mEnvTextView = (TextView) findViewById(R.id.text_env_value);
		mObjTextView = (TextView) findViewById(R.id.text_obj_value);
		mHRSPosition = (TextView) findViewById(R.id.text_hrs_position);
		showGraph();
	}

	private void showGraph() {
		mGraphView = mLineGraph.getView(this);
		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_hrs);
		layout.addView(mGraphView);
	}

//	private void toggleGraph() {
//		mGraphView = mLineGraph.getView(this);
//		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_hrs);
//		if(layout.getVisibility()==View.INVISIBLE) {
//			layout.setVisibility(View.VISIBLE);
//
//		}else layout.setVisibility(View.INVISIBLE);
//	}

	@Override
	protected void onStart() {
		super.onStart();

		final Intent intent = getIntent();
		if (!isDeviceConnected() && intent.hasExtra(FeaturesActivity.EXTRA_ADDRESS)) {
			final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(getIntent().getByteArrayExtra(FeaturesActivity.EXTRA_ADDRESS));
			onDeviceSelected(device, device.getName());

			intent.removeExtra(FeaturesActivity.EXTRA_APP);
			intent.removeExtra(FeaturesActivity.EXTRA_ADDRESS);
		}
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
		mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
		mFingerValue = savedInstanceState.getInt(FINGER_VALUE);
		mEnvValue = savedInstanceState.getInt(FINGER_VALUE);
		mObjValue = savedInstanceState.getInt(FINGER_VALUE);

		if (isGraphInProgress)
			startShowGraph();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
		outState.putInt(GRAPH_COUNTER, mCounter);
		outState.putInt(FINGER_VALUE, mFingerValue);
		outState.putInt(FINGER_VALUE, mEnvValue);
		outState.putInt(FINGER_VALUE, mObjValue);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopShowGraph();
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.hrs_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hrs_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.hrs_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HRSManager.HR_SERVICE_UUID;
	}

	private void updateGraph(final int value1, final int value2, final int value3) {
		mCounter++;
		mLineGraph.addValue(new Point(mCounter, value2), new Point(mCounter, value1), new Point(mCounter, value3));
		mGraphView.repaint();
	}

	private Runnable mRepeatTask = new Runnable() {
		@Override
		public void run() {
			if (mFingerValue > 0 && mEnvValue > 0 && mObjValue > 0)
				updateGraph(mFingerValue, mEnvValue, mObjValue);
			if (isGraphInProgress)
				mHandler.postDelayed(mRepeatTask, REFRESH_INTERVAL);
		}
	};

	void startShowGraph() {
		isGraphInProgress = true;
		mRepeatTask.run();
	}

	void stopShowGraph() {
		isGraphInProgress = false;
		mHandler.removeCallbacks(mRepeatTask);
	}

	@Override
	protected BleManager<HRSManagerCallbacks> initializeManager() {
		final HRSManager manager = HRSManager.getInstance(getApplicationContext());
		manager.setGattCallbacks(this);
		return manager;
	}

	private void setHRSValueOnView(final int value1,final int value2,final int value3 ) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (value1 >= MIN_POSITIVE_VALUE && value1 <= MAX_HR_VALUE 
                        && value2 >= MIN_POSITIVE_VALUE && value2 <= MAX_HR_VALUE  
                        && value3 >= MIN_POSITIVE_VALUE && value3 <= MAX_HR_VALUE) {
					mFingerTextView.setText(Integer.toString(value2));
					mEnvTextView.setText(Integer.toString(value1));
					mObjTextView.setText(Integer.toString(value3));
					setInsight(value2,value1,value3); //finger,env,obj
				} else {
					mFingerTextView.setText(R.string.not_available_value);
					mEnvTextView.setText(R.string.not_available_value);
					mObjTextView.setText(R.string.not_available_value);

				}

			}
		});
	}

	private void setInsight(int value2, int value1, int value3) {
		int finger = value2;
		int env = value1;
		int obj = value3;
		TextView insightText=(TextView) findViewById(R.id.insight);

		if(finger>28) insightText.setText("Hot fingers babeee! Stay like this!");
		else if(obj<16) insightText.setText("It's getting risky, why don't you let that cold thing?");
		else if(obj<11 && finger<22 ) insightText.setText("You are soon losing touch!");
		else if(obj<10 && finger<17 ) insightText.setText("Too numb, let it go! Let it go!");
		else if(finger<19 && obj>22 && env>20) insightText.setText("Getting stressed? Does it worth it?");
		else if(finger<18 && obj>24 && env>23) insightText.setText("You look way stressed... Ready for some meditating activities?");
		else if(finger<18 && obj>24 && env>23) insightText.setText("You look way stressed... Ready for some meditating activities?");
		else if(finger>26 && obj>24 && env>23) insightText.setText("Lucky you! So warm!");
		else if(finger>28 && obj>28 && env>26) insightText.setText("Soooo warm!");



	}

	private void setHRSPositionOnView(final String position) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (position != null) {
					mHRSPosition.setText(position);
				} else {
					mHRSPosition.setText(R.string.not_available);
				}
			}
		});
	}

	@Override
	public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceReady(final BluetoothDevice device) {
		startShowGraph();
	}

	@Override
	public void onHRSensorPositionFound(final BluetoothDevice device, final String position) {
		setHRSPositionOnView(position);
	}

	@Override
	public void onHRValueReceived(final BluetoothDevice device, int value1, int value2, int value3) {
		mFingerValue = value1;
        mEnvValue = value2;
        mObjValue = value3;
		setHRSValueOnView(mFingerValue, mEnvValue, mObjValue);
	}

	@Override
	public void onDeviceDisconnected(final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mFingerTextView.setText(R.string.not_available_value);
				mEnvTextView.setText(R.string.not_available_value);
				mObjTextView.setText(R.string.not_available_value);

				mHRSPosition.setText(R.string.not_available);
				stopShowGraph();
			}
		});
	}

	@Override
	protected void setDefaultUI() {
		mFingerTextView.setText(R.string.not_available_value);
		mEnvTextView.setText(R.string.not_available_value);
		mObjTextView.setText(R.string.not_available_value);

		mHRSPosition.setText(R.string.not_available);
		clearGraph();
	}

	private void clearGraph() {
		mLineGraph.clearGraph();
		mGraphView.repaint();
		mCounter = 0;
		mFingerValue = 0;
		mEnvValue = 0;
		mObjValue = 0;
	}
}

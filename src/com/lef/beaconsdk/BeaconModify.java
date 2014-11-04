package com.lef.beaconsdk;

import com.lef.beaconconnection.BeaconConnection;
import com.lef.beaconconnection.BeaconConnectionCallback;
import com.lef.ibeacon.IBeacon;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class BeaconModify extends Activity implements BeaconConnectionCallback {

	private IBeacon currentBeacon;
	private TextView uuidTextView;
	private TextView majorTextView;
	private TextView minorTextView;
	private TextView rssiTextView;

	private BeaconConnection beaconConnection;
	private static final int EMPTYVALUE = 1;
	private static final int INVALIDVALUE = 6;
	private static final int SETSUCCEED = 2;
	private static final int SETFAILURE = 3;
	private static final int CONNECTION_S = 4;
	private static final int CONNECTION_F = 5;

	// private static final int EmptyValue = 1;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case EMPTYVALUE:
				Toast.makeText(BeaconModify.this, "不能为空", Toast.LENGTH_SHORT)
						.show();
				break;
			case SETFAILURE:
				Toast.makeText(BeaconModify.this, "设置失败，请重新连接",
						Toast.LENGTH_SHORT).show();
				break;
			case SETSUCCEED:
				Toast.makeText(BeaconModify.this, "设置成功", Toast.LENGTH_SHORT)
						.show();
				break;
			case CONNECTION_S:
				Toast.makeText(BeaconModify.this, "连接成功", Toast.LENGTH_SHORT)
						.show();
				break;
			case CONNECTION_F:
				Toast.makeText(BeaconModify.this, "连接失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case INVALIDVALUE:
				Toast.makeText(BeaconModify.this, "不合理值", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beacon_modify);

		Intent intent = getIntent();
		currentBeacon = intent.getParcelableExtra("beacon");

		beaconConnection = new BeaconConnection(this, currentBeacon, this);

		uuidTextView = (TextView) findViewById(R.id.uuid_modify);
		majorTextView = (TextView) findViewById(R.id.major_modify);
		minorTextView = (TextView) findViewById(R.id.minor_modify);
		rssiTextView = (TextView) findViewById(R.id.rssi_modify);
		Button disconnectBtn = (Button) findViewById(R.id.disconnect);
		disconnectBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (beaconConnection != null && beaconConnection.isConnection()) {
					beaconConnection.disConnect();
				}
			}
		});
		uuidTextView.setOnClickListener(new OnClickListener() {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.attr_modify_dialog, null);
			final EditText attrValue = (EditText) ll
					.findViewById(R.id.attr_value);

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(BeaconModify.this)
						.setIcon(R.drawable.title_bar_menu)
						.setTitle("设置UUID")
						.setView(ll)
						.setPositiveButton("保存",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										if (!attrValue.getText().toString()
												.equals("")) {
											beaconConnection.setUUID(attrValue
													.getText().toString());
										} else {
											handler.sendEmptyMessage(EMPTYVALUE);
										}

									}
								}).setNegativeButton("取消", null).create()
						.show();
			}
		});
		majorTextView.setOnClickListener(new OnClickListener() {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.attr_modify_dialog, null);
			final EditText attrValue = (EditText) ll
					.findViewById(R.id.attr_value);

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(BeaconModify.this)
						.setIcon(R.drawable.title_bar_menu)
						.setTitle("设置MAJOR")
						.setView(ll)
						.setPositiveButton("保存",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										if (!attrValue.getText().toString()
												.equals("")) {

											try {
												beaconConnection.setMajorMinor(
														Integer.valueOf(attrValue
																.getText()
																.toString()),
														Integer.valueOf(attrValue
																.getText()
																.toString()));
											} catch (Exception e) {
												handler.sendEmptyMessage(EMPTYVALUE);
											}

										} else {
											handler.sendEmptyMessage(EMPTYVALUE);
										}

									}
								}).setNegativeButton("取消", null).create()
						.show();
			}
		});
		minorTextView.setOnClickListener(new OnClickListener() {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.attr_modify_dialog, null);
			final EditText attrValue = (EditText) ll
					.findViewById(R.id.attr_value);

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(BeaconModify.this)
						.setIcon(R.drawable.title_bar_menu)
						.setTitle("设置MINOR")
						.setView(ll)
						.setPositiveButton("保存",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										if (!attrValue.getText().toString()
												.equals("")) {
											try {
												beaconConnection.setMajorMinor(
														Integer.valueOf(attrValue
																.getText()
																.toString()),
														Integer.valueOf(attrValue
																.getText()
																.toString()));
											} catch (Exception e) {
												handler.sendEmptyMessage(EMPTYVALUE);
											}
										} else {
											handler.sendEmptyMessage(EMPTYVALUE);
										}

									}
								}).setNegativeButton("取消", null).create()
						.show();
			}
		});
		rssiTextView.setOnClickListener(new OnClickListener() {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(
					R.layout.attr_modify_dialog, null);
			final EditText attrValue = (EditText) ll
					.findViewById(R.id.attr_value);

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(BeaconModify.this)
						.setIcon(R.drawable.title_bar_menu)
						.setTitle("设置计算RSSI")
						.setView(ll)
						.setPositiveButton("保存",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										if (!attrValue.getText().toString()
												.equals("")) {
											try {
												beaconConnection.setCalRssi(Integer
														.valueOf(attrValue
																.getText()
																.toString()));
											} catch (Exception e) {
												handler.sendEmptyMessage(EMPTYVALUE);
											}
										} else {
											handler.sendEmptyMessage(EMPTYVALUE);
										}

									}
								}).setNegativeButton("取消", null).create()
						.show();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		beaconConnection.connect();
	}

//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//		if (beaconConnection != null && beaconConnection.isConnection()) {
//			beaconConnection.disConnect();
//		}
//	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (beaconConnection != null && beaconConnection.isConnection()) {
			beaconConnection.disConnect();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.beacon_modify, menu);
		return true;
	}

	@Override
	public void onConnectedState(IBeacon beacon, int status) {
		// TODO Auto-generated method stub
		switch (status) {
		case BeaconConnection.CONNECTED:
			handler.sendEmptyMessage(CONNECTION_S);
			break;
		case BeaconConnection.DISCONNECTED:
			handler.sendEmptyMessage(CONNECTION_F);
			break;
		case BeaconConnection.CONNECTING:
		case BeaconConnection.DISCONNECTTING:
		default:
			break;
		}
	}

	@Override
	public void onSetMajoMinor(IBeacon beacon, int status) {
		if (status == BeaconConnection.SUCCESS) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(SETSUCCEED);
			majorTextView.setText(beacon.getMajor());
			minorTextView.setText(beacon.getMinor());
		} else if (status == BeaconConnection.FAILURE) {
			handler.sendEmptyMessage(SETFAILURE);
		} else {
			handler.sendEmptyMessage(INVALIDVALUE);
		}
	}

	@Override
	public void onSetBaseSetting(IBeacon beacon, int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSetCalRssi(IBeacon beacon, int status) {
		// TODO Auto-generated method stub
		if (status == BeaconConnection.SUCCESS) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(SETSUCCEED);
			rssiTextView.setText(beacon.getTxPower());
		} else if (status == BeaconConnection.FAILURE) {
			handler.sendEmptyMessage(SETFAILURE);
		} else {
			handler.sendEmptyMessage(INVALIDVALUE);
		}
	}

	@Override
	public void onSetProximityUUID(IBeacon beacon, int status) {
		// TODO Auto-generated method stub
		if (status == BeaconConnection.SUCCESS) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(SETSUCCEED);
			uuidTextView.setText(beacon.getProximity());
		} else if (status == BeaconConnection.FAILURE) {
			handler.sendEmptyMessage(SETFAILURE);
		} else {
			handler.sendEmptyMessage(INVALIDVALUE);
		}
	}

	@Override
	public void onGetUUID(String proximityUuid) {
		// TODO Auto-generated method stub
		uuidTextView.setText(proximityUuid);
	}

	@Override
	public void onGetMajor(int major) {
		// TODO Auto-generated method stub
		majorTextView.setText(major + "");
	}

	@Override
	public void onGetMinor(int minor) {
		// TODO Auto-generated method stub
		minorTextView.setText(minor + "");
	}

	@Override
	public void onGetRssi(int rssi) {
		// TODO Auto-generated method stub
		rssiTextView.setText(rssi + "");
	}

}

package com.lef.beaconconnection;

import java.util.UUID;

import com.radiusnetworks.ibeacon.IBeacon;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

public class BeaconConnection implements ScannerListener {

	public static final int SUCCESS = 1;
	public static final int FAILURE = 2;
	public static final int INVALIDVALUE = 3;
	public static final int CONNECTING = 4;
	public static final int CONNECTED = 5;
	public static final int DISCONNECTED = 6;
	public static final int DISCONNECTTING = 7;

	private UpdateService.ServiceBinder mBinder;
	private boolean mBinded;
	private Activity mContext;
	private IBeacon mcurrentBeacon;
	private BeaconConnectionCallback mConnectionCallback;
	private boolean isConnection;
	private BeaconScanner bs;

	/**
	 * 构造函数，需要实现监听程序BeaconConnectionCallback
	 * 
	 * @param context
	 * @param beacon
	 * @param callback
	 */
	public BeaconConnection(Activity context, IBeacon beacon,
			BeaconConnectionCallback callback) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mcurrentBeacon = beacon;
		this.mConnectionCallback = callback;
		IntentFilter filter = new IntentFilter();
		filter.addAction(UpdateService.ACTION_STATE_CHANGED);
		filter.addAction(UpdateService.ACTION_DONE);
		filter.addAction(UpdateService.ACTION_UUID_READY);
		filter.addAction(UpdateService.ACTION_MAJOR_MINOR_READY);
		filter.addAction(UpdateService.ACTION_RSSI_READY);
		filter.addAction(UpdateService.ACTION_GATT_ERROR);
		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				mServiceBroadcastReceiver, filter);
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name,
				final IBinder service) {
			final UpdateService.ServiceBinder binder = mBinder = (UpdateService.ServiceBinder) service;
			final int state = binder.getState();
			switch (state) {
			case UpdateService.STATE_DISCONNECTED:
				binder.connect();
				break;
			case UpdateService.STATE_CONNECTED:
				isConnection = true;
				break;
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			mBinder = null;
		}
	};

	/**
	 * 设置beacon的Major和Minor
	 * 
	 * @param major
	 *            (范围:0~65535)
	 * @param minor
	 *            (范围:0~65535)
	 */
	public void setMajorMinor(int major, int minor) {
		// TODO Auto-generated method stub
		if (major < 0 || major > 0xFFF || minor < 0 || minor > 0xFFFF) {
			mConnectionCallback.onSetMajoMinor(mcurrentBeacon, INVALIDVALUE);
		} else {
			if (mBinder.setMajorAndMinor(major, minor)) {
				mcurrentBeacon.setMajor(major);
				mcurrentBeacon.setMajor(minor);
				mConnectionCallback.onSetMajoMinor(mcurrentBeacon, SUCCESS);
			} else {
				mConnectionCallback.onSetMajoMinor(mcurrentBeacon, FAILURE);
			}
		}
	}

	/**
	 * 设置beacon的UUID
	 * 
	 * @param UUID
	 *            (格式:"00000000-0000-0000-0000-000000000000")
	 */
	public void setUUID(String proximityUuid) {
		if (TextUtils.isEmpty(proximityUuid) || proximityUuid.length() != 36) {
			mConnectionCallback
					.onSetProximityUUID(mcurrentBeacon, INVALIDVALUE);
		} else {
			UUID uuid = UUID.fromString(proximityUuid);
			if (mBinder.setBeaconUuid(uuid)) {
				mcurrentBeacon.setProximityUuid(proximityUuid);
				mConnectionCallback.onSetProximityUUID(mcurrentBeacon, SUCCESS);
			} else {
				mConnectionCallback.onSetProximityUUID(mcurrentBeacon, FAILURE);
			}
		}
	}

	public void setCalRssi(int rssi) {
		if (rssi > 0 || rssi < -150) {
			mConnectionCallback.onSetCalRssi(mcurrentBeacon, INVALIDVALUE);
		} else {
			if (mBinder.setCalibratedRssi(rssi)) {
				mcurrentBeacon.setTxPower(rssi);
				mConnectionCallback.onSetCalRssi(mcurrentBeacon, SUCCESS);
			} else {
				mConnectionCallback.onSetCalRssi(mcurrentBeacon, FAILURE);
			}
		}
	}

	/**
	 * 连接蓝牙设备Beacon
	 * 
	 * @param device
	 */
	public void connect() {
		bs = new BeaconScanner(mContext, this,
				mcurrentBeacon.getBluetoothAddress());
		bs.startScan();
	}

	/**
	 * 断开连接beacon
	 */
	public void disConnect() {

		if (mBinded)
			mContext.unbindService(mServiceConnection);
	}

	/**
	 * 判断是否连接beacon成功
	 * 
	 * @return isConnection
	 */
	public boolean isConnection() {
		return isConnection;
	}

	private BroadcastReceiver mServiceBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// if (mContext == null )
			// return;

			final String action = intent.getAction();

			if (UpdateService.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(UpdateService.EXTRA_DATA,
						UpdateService.STATE_DISCONNECTED);

				switch (state) {
				case UpdateService.STATE_DISCONNECTED:

					Intent service = new Intent(mContext, UpdateService.class);
					mContext.unbindService(mServiceConnection);
					mContext.stopService(service);
					mBinder = null;
					mBinded = false;
					break;
				case UpdateService.STATE_CONNECTED:
					mConnectionCallback.onConnectedState(mcurrentBeacon,
							CONNECTED);
					break;
				case UpdateService.STATE_DISCONNECTING:
					mConnectionCallback.onConnectedState(mcurrentBeacon,
							DISCONNECTTING);
					break;
				case UpdateService.STATE_CONNECTING:
					mConnectionCallback.onConnectedState(mcurrentBeacon,
							CONNECTING);
					break;
				}
			} else if (UpdateService.ACTION_UUID_READY.equals(action)) {
				UUID uuid = ((ParcelUuid) intent
						.getParcelableExtra(UpdateService.EXTRA_DATA))
						.getUuid();
				mConnectionCallback.onGetUUID(uuid.toString());
				
			} else if (UpdateService.ACTION_MAJOR_MINOR_READY.equals(action)) {
				int major = intent.getIntExtra(UpdateService.EXTRA_MAJOR, 0);
				int minor = intent.getIntExtra(UpdateService.EXTRA_MINOR, 0);
				mConnectionCallback.onGetMajor(major);
				mConnectionCallback.onGetMinor(minor);
			} else if (UpdateService.ACTION_RSSI_READY.equals(action)) {
				int rssi = intent.getIntExtra(UpdateService.EXTRA_DATA, 0);
				mConnectionCallback.onGetRssi(rssi);
			} else if (UpdateService.ACTION_DONE.equals(action)) {
				mBinder.read();
			} else if (UpdateService.ACTION_GATT_ERROR.equals(action)) {
				final int error = intent.getIntExtra(UpdateService.EXTRA_DATA,
						0);

				switch (error) {
				case UpdateService.ERROR_UNSUPPORTED_DEVICE:
					Toast.makeText(mContext, "设备不支持BLE", Toast.LENGTH_SHORT)
							.show();
					break;
				default:
					Toast.makeText(mContext, "服务出现错误", Toast.LENGTH_SHORT)
							.show();
					break;
				}
				mBinder.disconnectAndClose();
			}
		};
	};

	@Override
	public void onDeviceSelected(BluetoothDevice device, String name) {
		// TODO Auto-generated method stub
		// This will connect to the service only if it's already running
		bs.stopScan();
		final Activity activity = mContext;
		final Intent service = new Intent(activity, UpdateService.class);
		service.putExtra(UpdateService.EXTRA_DATA, device);
		activity.startService(service);
		mBinded = true;
		activity.bindService(service, mServiceConnection, 0);
	}
}

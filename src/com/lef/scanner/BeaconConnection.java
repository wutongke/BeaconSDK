package com.lef.scanner;

import java.util.UUID;

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

import com.lef.ibeacon.service.BeaconScanner;
import com.lef.ibeacon.service.ScannerListener;
import com.lef.ibeacon.service.UpdateService;

/**
 * * An class for an Android <code>Activity</code> that wants to interact with
 * iBeacons.<br>
 * 连接beacon是用到，连接成功后可以设置beacon的uuid{@link BeaconConnection#setUUID(String)}、major、minor、发射功率和频率
 */
public class BeaconConnection implements ScannerListener {
	/**
	 * 默认密码为666666<br>
	 * 如果修改过密码，则需要在连接之前，先{@link BeaconConnection@#setPASSWORD(String)};返回再连接
	 */
	public static String PASSWORD  = "666666";
	/**
	 * 属性设置成功
	 */
	public static final int SUCCESS = 1;
	/**
	 * 属性设置失败
	 */
	public static final int FAILURE = 2;
	/**
	 * 设置时采用了不合理的值
	 */
	public static final int INVALIDVALUE = 3;
	/**
	 * 正在连接beacon
	 */
	public static final int CONNECTING = 4;
	/**
	 * 连接beacon成功
	 */
	public static final int CONNECTED = 5;
	/**
	 * 断开beacon连接
	 */
	public static final int DISCONNECTED = 6;
	/**
	 * 正在断开beacon连接
	 */
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
	 *            -连接beacon的Activity
	 * @param beacon
	 *            -需要连接的beacon
	 * @param callback
	 *            -实现BeaconConnectionCallback接口，用于连接成功后对beacon进行设置
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
		filter.addAction(UpdateService.ACTION_ADVERTISINGINTERVAL_READY);
		filter.addAction(UpdateService.ACTION_TRANSMITPOWER_READY);
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
	 * 设置beacon的Major和Minor，回调onSetMajoMinor
	 * 
	 * @param major
	 *            (范围:0~65535)
	 * @param minor
	 *            (范围:0~65535)
	 */
	public void setMajorMinor(int major, int minor) {
		// TODO Auto-generated method stub
		if (major < 0 || major > 0xFFFF || minor < 0 || minor > 0xFFFF) {
			mConnectionCallback.onSetMajoMinor(mcurrentBeacon, INVALIDVALUE);
		} else {
			if (mBinder.setMajorAndMinor(major, minor)) {
				mcurrentBeacon.setMajor(major);
				mcurrentBeacon.setMinor(minor);
				mConnectionCallback.onSetMajoMinor(mcurrentBeacon, SUCCESS);
			} else {
				mConnectionCallback.onSetMajoMinor(mcurrentBeacon, FAILURE);
			}
		}
	}

	/**
	 * 设置beacon的UUID，回调onSetProximityUUID
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

	/**
	 * 设置距离一米是beacon的信号强度，用于根据信号强度计算智能终端与beacon之间的距离
	 * 
	 * @param txPower
	 */
	public void setCalRssi(int txPower) {
		if (txPower > 0 || txPower < -150) {
			mConnectionCallback.onSetCalRssi(mcurrentBeacon, INVALIDVALUE);
		} else {
			if (mBinder.setCalibratedRssi(txPower)) {
				mcurrentBeacon.setTxPower(txPower);
				mConnectionCallback.onSetCalRssi(mcurrentBeacon, SUCCESS);
			} else {
				mConnectionCallback.onSetCalRssi(mcurrentBeacon, FAILURE);
			}
		}
	}

	/**
	 * 设置基本配置中的发射频率，回调onSetBaseSetting
	 * @param advertisingInterval
	 */
	public void setAdvertisingInterval(
			BaseSettings.AdvertisingInterval advertisingInterval) {
		if(advertisingInterval.aValue<0||advertisingInterval.aValue>6){
			mConnectionCallback.onSetBaseSetting(mcurrentBeacon, INVALIDVALUE);
			return;
		}
		if (mBinder.setAdvertisingInterval(advertisingInterval.aValue)) {
			mcurrentBeacon.setAdvertisingInterval(advertisingInterval.aValue);
			mConnectionCallback.onSetBaseSetting(mcurrentBeacon, SUCCESS);
		} else {
			mConnectionCallback.onSetBaseSetting(mcurrentBeacon, FAILURE);
		}
	}
	/**
	 * 设置基本配置中的发射功率，回调onSetBaseSetting
	 * @param advertisingInterval
	 */
	public void setTransmitPower(
			BaseSettings.TransmitPower ransmitPower) {
		if(ransmitPower.aValue<0||ransmitPower.aValue>6){
			mConnectionCallback.onSetBaseSetting(mcurrentBeacon, INVALIDVALUE);
			return;
		}
		if (mBinder.setTransmitPower(ransmitPower.aValue)) {
			mcurrentBeacon.setTransmitPower(ransmitPower.aValue);
			mConnectionCallback.onSetBaseSetting(mcurrentBeacon, SUCCESS);
		} else {
			mConnectionCallback.onSetBaseSetting(mcurrentBeacon, FAILURE);
		}
	}

	/**
	 * 启动连接Beacon
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

		if (mBinded) {
			isConnection = false;
			mBinded = false;
			mBinder.disconnectAndClose();
		}
		
		mContext.unbindService(mServiceConnection);
		final Intent service = new Intent(mContext, UpdateService.class);
		mContext.stopService(service);
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mServiceBroadcastReceiver);
	}

	/**
	 * 判断beacon是否已经连接
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
					if (isConnection) {
						Intent service = new Intent(mContext,
								UpdateService.class);
						mContext.unbindService(mServiceConnection);
						mContext.stopService(service);
					}
					mBinder = null;
					mBinded = false;
					mConnectionCallback.onConnectedState(mcurrentBeacon,
							DISCONNECTED);
					break;
				case UpdateService.STATE_CONNECTED:
					isConnection = true;
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
				mcurrentBeacon.setProximityUuid(uuid.toString());
//				mConnectionCallback.onGetUUID(uuid.toString());

			} else if (UpdateService.ACTION_MAJOR_MINOR_READY.equals(action)) {
				int major = intent.getIntExtra(UpdateService.EXTRA_MAJOR, 0);
				int minor = intent.getIntExtra(UpdateService.EXTRA_MINOR, 0);
				mcurrentBeacon.setMajor(major);
				mcurrentBeacon.setMinor(minor);
//				mConnectionCallback.onGetMajor(major);
//				mConnectionCallback.onGetMinor(minor);
			} else if (UpdateService.ACTION_RSSI_READY.equals(action)) {
				int rssi = intent.getIntExtra(UpdateService.EXTRA_DATA, 0);
				mcurrentBeacon.setTxPower(rssi);
//				mConnectionCallback.onGetRssi(rssi);
			} else if (UpdateService.ACTION_ADVERTISINGINTERVAL_READY
					.equals(action)) {
				int advertisinginterval = intent.getIntExtra(
						UpdateService.EXTRA_DATA, 0);
				mcurrentBeacon.setAdvertisingInterval(advertisinginterval);
				// mConnectionCallback.onGetRssi(advertisinginterval);
			} else if (UpdateService.ACTION_TRANSMITPOWER_READY.equals(action)) {
				int transmitpower = intent.getIntExtra(
						UpdateService.EXTRA_DATA, 0);
				mcurrentBeacon.setTransmitPower(transmitpower);
				/**
				 * 发送密码，暂时放置在这里，放置在其他地方目前测试不太好用
				 */
				mBinder.sendPassword(PASSWORD);
				// mConnectionCallback.onGetRssi(advertisinginterval);
			} else if (UpdateService.ACTION_DONE.equals(action)) {
				mBinder.read();
				
				// 注释掉的内容错误是空指针
				// mcurrentBeacon.setProximityUuid(mBinder.getBeaconUuid().toString());
				// mcurrentBeacon.setTxPower(mBinder.getCalibratedRssi());
				// mcurrentBeacon.setMajor(mBinder.getMajorAndMinor().first);
				// mcurrentBeacon.setMinor(mBinder.getMajorAndMinor().second);
			} else if (UpdateService.ACTION_GATT_ERROR.equals(action)) {
				final int error = intent.getIntExtra(UpdateService.EXTRA_DATA,
						0);

				switch (error) {
				case UpdateService.ERROR_UNSUPPORTED_DEVICE:
					Toast.makeText(mContext, "设备不支持BLE", Toast.LENGTH_SHORT)
							.show();
					break;
				default:
					Toast.makeText(mContext, "蓝牙服务出现错误，请重新连接", Toast.LENGTH_SHORT)
							.show();
					break;
				}
				// 防止提前点击了断开连接
				if (mBinded) {
					mBinder.disconnectAndClose();
				}
			}
		};
	};

	/**
	 * SDK自动回调，开发者不需要调用
	 * @param BluetoothDevice
	 * @param BluetoothDeviceName
	 */
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

	public static String getPASSWORD() {
		return PASSWORD;
	}
	
	public static void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}
}

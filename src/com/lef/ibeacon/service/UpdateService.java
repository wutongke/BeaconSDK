/*******************************************************************************
 * Copyright (c) 2014 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.lef.ibeacon.service;

import java.lang.reflect.Method;
import java.util.UUID;

import com.lef.beaconsdk.BuildConfig;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

public class UpdateService extends Service {
	private static final String TAG = "UpdateService";

	public final static String ACTION_STATE_CHANGED = "no.nordicsemi.android.nrfbeacon.ACTION_STATE_CHANGED";
	public final static String ACTION_GATT_ERROR = "no.nordicsemi.android.nrfbeacon.ACTION_GATT_ERROR";
	public final static String ACTION_DONE = "no.nordicsemi.android.nrfbeacon.ACTION_DONE";
	public final static String ACTION_UUID_READY = "no.nordicsemi.android.nrfbeacon.ACTION_UUID_READY";
	public final static String ACTION_MAJOR_MINOR_READY = "no.nordicsemi.android.nrfbeacon.ACTION_MAJOR_MINOR_READY";
	public final static String ACTION_RSSI_READY = "no.nordicsemi.android.nrfbeacon.ACTION_RSSI_READY";
	public final static String ACTION_ADVERTISINGINTERVAL_READY = "no.nordicsemi.android.nrfbeacon.ACTION_ADVERTISINGINTERVAL_READY";
	public final static String ACTION_TRANSMITPOWER_READY = "no.nordicsemi.android.nrfbeacon.ACTION_TRANSMITPOWER_READY";

	public final static String EXTRA_DATA = "no.nordicsemi.android.nrfbeacon.EXTRA_DATA";
	public final static String EXTRA_MAJOR = "no.nordicsemi.android.nrfbeacon.EXTRA_MAJOR";
	public final static String EXTRA_MINOR = "no.nordicsemi.android.nrfbeacon.EXTRA_MINOR";

	public final static int ERROR_UNSUPPORTED_DEVICE = -1;

	private int mConnectionState;
	public final static int STATE_DISCONNECTED = 0;
	public final static int STATE_CONNECTING = 1;
	public final static int STATE_DISCOVERING_SERVICES = 2;
	public final static int STATE_CONNECTED = 3;
	public final static int STATE_DISCONNECTING = 4;

	public final static int SERVICE_UUID = 1;
	public final static int SERVICE_MAJOR_MINOR = 2;
	public final static int SERVICE_CALIBRATION = 3;

	private static final UUID CONFIG_SERVICE_UUID = new UUID(
			0x955A15230FE2F5AAl, 0x0A09484B8D4F3E8ADl);
	private static final UUID CONFIG_UUID_CHARACTERISTIC_UUID = new UUID(
			0x955A15240FE2F5AAl, 0x0A09484B8D4F3E8ADl);
	private static final UUID CONFIG_RSSI_CHARACTERISTIC_UUID = new UUID(
			0x955A15250FE2F5AAl, 0x0A09484B8D4F3E8ADl);
	private static final UUID CONFIG_MAJOR_MINOR_CHARACTERISTIC_UUID = new UUID(
			0x955A15260FE2F5AAl, 0x0A09484B8D4F3E8ADl);
	private static final UUID CONFIG_TRANSMITPOWER_CHARACTERISTIC_UUID = new UUID(
			0x955A15280FE2F5AAl, 0x0A09484B8D4F3E8ADl);
	private static final UUID CONFIG_ADVERTISINGINTERVAL_CHARACTERISTIC_UUID = new UUID(
			0x955A15270FE2F5AAl, 0x0A09484B8D4F3E8ADl);

	private BluetoothAdapter mAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCharacteristic mUuidCharacteristic;
	private BluetoothGattCharacteristic mMajorMinorCharacteristic;
	private BluetoothGattCharacteristic mRssiCharacteristic;
	private BluetoothGattCharacteristic mAdvertisingintervalCharacteristic;
	private BluetoothGattCharacteristic mTransmitpowerCharacteristic;

	private Handler mHandler;

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt,
				final int status, final int newState) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				logw("Connection state change error: " + status);
				broadcastError(status);
				return;
			}

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				setState(STATE_DISCOVERING_SERVICES);
				// Attempts to discover services after successful connection.
				gatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				setState(STATE_DISCONNECTED);
				refreshDeviceCache(gatt);
				gatt.close();
				mBluetoothGatt = null;
				stopSelf();
			}
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt,
				final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				logw("Service discovery error: " + status);
				broadcastError(status);
				return;
			}

			// We have successfully connected 欲设置为所有数据读取完成后再setState
//			setState(STATE_CONNECTED);

			// Search for config service
			final BluetoothGattService configService = gatt
					.getService(CONFIG_SERVICE_UUID);
			if (configService == null) {
				// Config service is not present
				broadcastError(ERROR_UNSUPPORTED_DEVICE);
				setState(STATE_DISCONNECTING);
				gatt.disconnect();
				return;
			}

			mUuidCharacteristic = configService
					.getCharacteristic(CONFIG_UUID_CHARACTERISTIC_UUID);
			mMajorMinorCharacteristic = configService
					.getCharacteristic(CONFIG_MAJOR_MINOR_CHARACTERISTIC_UUID);
			mRssiCharacteristic = configService
					.getCharacteristic(CONFIG_RSSI_CHARACTERISTIC_UUID);
			mTransmitpowerCharacteristic = configService
					.getCharacteristic(CONFIG_TRANSMITPOWER_CHARACTERISTIC_UUID);
			mAdvertisingintervalCharacteristic = configService
					.getCharacteristic(CONFIG_ADVERTISINGINTERVAL_CHARACTERISTIC_UUID);

			if (mUuidCharacteristic != null
					|| mMajorMinorCharacteristic != null
					|| mRssiCharacteristic != null
					|| mTransmitpowerCharacteristic != null
					|| mAdvertisingintervalCharacteristic != null)
				broadcastOperationCompleted();

			if (mUuidCharacteristic == null
					&& mMajorMinorCharacteristic == null
					&& mRssiCharacteristic == null
					&& mTransmitpowerCharacteristic == null
					&& mAdvertisingintervalCharacteristic == null) {
				// Config characteristics is not present
				broadcastError(ERROR_UNSUPPORTED_DEVICE);
				setState(STATE_DISCONNECTING);
				gatt.disconnect();
				return;
			}
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt,
				final BluetoothGattCharacteristic characteristic,
				final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				logw("Characteristic write error: " + status);
				broadcastError(status);
				return;
			}

			if (CONFIG_UUID_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final UUID uuid = decodeBeaconUUID(characteristic);
				broadcastUuid(uuid);
			} else if (CONFIG_MAJOR_MINOR_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final int major = decodeUInt16(characteristic, 0);
				final int minor = decodeUInt16(characteristic, 2);
				broadcastMajorAndMinor(major, minor);
			} else if (CONFIG_RSSI_CHARACTERISTIC_UUID.equals(characteristic
					.getUuid())) {
				final int rssi = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
				broadcastRssi(rssi);
			} else if (CONFIG_ADVERTISINGINTERVAL_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final int advertisinginterval = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
				broadcastAdvertisinginterval(advertisinginterval);
			} else if (CONFIG_TRANSMITPOWER_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final int advertisinginterval = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
				broadcastTransmitpower(advertisinginterval);
				//所有数据读取完成后设置成功，这样返回的mcurrnentBeacon所有配置就被设置了
				setState(STATE_CONNECTED);
			}
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt,
				final BluetoothGattCharacteristic characteristic,
				final int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				logw("Characteristic read error: " + status);
				broadcastError(status);
				return;
			}

			if (CONFIG_UUID_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final UUID uuid = decodeBeaconUUID(characteristic);
				broadcastUuid(uuid);
				if (mMajorMinorCharacteristic.getValue() == null)
					gatt.readCharacteristic(mMajorMinorCharacteristic);
			} else if (CONFIG_MAJOR_MINOR_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final int major = decodeUInt16(characteristic, 0);
				final int minor = decodeUInt16(characteristic, 2);
				broadcastMajorAndMinor(major, minor);
				if (mRssiCharacteristic.getValue() == null)
					gatt.readCharacteristic(mRssiCharacteristic);
			} else if (CONFIG_RSSI_CHARACTERISTIC_UUID.equals(characteristic
					.getUuid())) {
				final int rssi = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
				broadcastRssi(rssi);
				if (mAdvertisingintervalCharacteristic.getValue()==null){
					gatt.readCharacteristic(mAdvertisingintervalCharacteristic);
				}
			}
			else if (CONFIG_ADVERTISINGINTERVAL_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final int advertisinginterval = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
				broadcastAdvertisinginterval(advertisinginterval);
				if(mTransmitpowerCharacteristic.getValue()==null){
					gatt.readCharacteristic(mTransmitpowerCharacteristic);
				}
			} else if (CONFIG_TRANSMITPOWER_CHARACTERISTIC_UUID
					.equals(characteristic.getUuid())) {
				final int advertisinginterval = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
				broadcastTransmitpower(advertisinginterval);
				//所有数据读取完成后设置成功，这样返回的mcurrnentBeacon所有配置就被设置了
				setState(STATE_CONNECTED);
			}
		}
	};

	public class ServiceBinder extends Binder {
		/**
		 * Connects to the service. The bluetooth device must have been passed
		 * during binding to the service in {@link UpdateService#EXTRA_DATA}
		 * field.
		 * 
		 * @return <code>true</code> if connection process has been initiated
		 */
		public boolean connect() {
			if (mAdapter == null) {
				logw("BluetoothAdapter not initialized or unspecified address.");
				return false;
			}

			if (mBluetoothDevice == null) {
				logw("Target device not specified. Start service with the BluetoothDevice set in EXTRA_DATA field.");
				return false;
			}

			// the device may be already connected
			if (mConnectionState == STATE_CONNECTED) {
				return true;
			}

			setState(STATE_CONNECTING);
			mBluetoothGatt = mBluetoothDevice.connectGatt(UpdateService.this,
					false, mGattCallback);
			return true;
		}

		/**
		 * Disconnects from the device and closes the Bluetooth GATT object
		 * afterwards.
		 */
		public void disconnectAndClose() {
			// This sometimes happen when called from
			// UpdateService.ACTION_GATT_ERROR event receiver in UpdateFragment.
			if (mBluetoothGatt == null)
				return;

			setState(STATE_DISCONNECTING);
			mBluetoothGatt.disconnect();

			// Sometimes the connection gets error 129 or 133. Calling
			// disconnect() method does not really disconnect... sometimes the
			// connection is already broken.
			// Here we have a security check that notifies UI about
			// disconnection even if onConnectionStateChange(...) has not been
			// called.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mConnectionState == STATE_DISCONNECTING)
						mGattCallback.onConnectionStateChange(mBluetoothGatt,
								BluetoothGatt.GATT_SUCCESS,
								BluetoothProfile.STATE_DISCONNECTED);
				}
			}, 1500);
		}

		/**
		 * Reads all the values from the device, one by one.
		 * 
		 * @return <code>true</code> if at least one required characteristic has
		 *         been found on the beacon.
		 */
		public boolean read() {
			if (mBluetoothGatt == null)
				return false;

			if (mUuidCharacteristic != null) {
				mBluetoothGatt.readCharacteristic(mUuidCharacteristic);
				return true;
			} else if (mMajorMinorCharacteristic != null) {
				mBluetoothGatt.readCharacteristic(mMajorMinorCharacteristic);
				return true;
			} else if (mRssiCharacteristic != null) {
				mBluetoothGatt.readCharacteristic(mRssiCharacteristic);
				return true;
			} else if (mAdvertisingintervalCharacteristic != null) {
				mBluetoothGatt.readCharacteristic(mAdvertisingintervalCharacteristic);
				return true;
			} else if (mTransmitpowerCharacteristic != null) {
				mBluetoothGatt.readCharacteristic(mTransmitpowerCharacteristic);
				return true;
			}
			return false;
		}

		/**
		 * Overwrites the beacon service UUID.
		 * 
		 * @param uuid
		 *            the new UUID
		 * @return <code>true</code> if altering UUID is supported (required
		 *         characteristic exists)
		 */
		public boolean setBeaconUuid(final UUID uuid) {
			if (mUuidCharacteristic == null || uuid == null)
				return false;

			final byte[] data = new byte[16];
			for (int i = 0; i < 8; ++i)
				data[i] = (byte) ((uuid.getMostSignificantBits() >>> (56 - i * 8)) & 0xFF);
			for (int i = 8; i < 16; ++i)
				data[i] = (byte) ((uuid.getLeastSignificantBits() >>> (56 - i * 8)) & 0xFF);
			mUuidCharacteristic.setValue(data);
			mBluetoothGatt.writeCharacteristic(mUuidCharacteristic);
			return true;
		}

		/**
		 * Returns the current UUID value. This reads the value from the local
		 * cache. The {@link #read()} method must be invoked before to read the
		 * current value from the device.
		 * 
		 * @return the beacon service UUID
		 */
		public UUID getBeaconUuid() {
			final BluetoothGattCharacteristic characteristic = mUuidCharacteristic;
			if (characteristic != null) {
				final byte[] data = characteristic.getValue();
				if (data == null || data.length != 16)
					return null;
				return decodeBeaconUUID(characteristic);
			}
			return null;
		}

		/**
		 * Overwrites the beacon major and minor numbers.
		 * 
		 * @param major
		 *            the major number (0-65535)
		 * @param minor
		 *            the minor number (0-65535)
		 * @return <code>true</code> if altering major and minor is supported
		 *         (required characteristic exists)
		 */
		public boolean setMajorAndMinor(final int major, final int minor) {
			if (mMajorMinorCharacteristic == null)
				return false;

			if (major < 0 || major > 0xFFFF)
				return false;

			if (minor < 0 || minor > 0xFFFF)
				return false;

			final int majorInverted = (major & 0xFF) << 8
					| ((major >> 8) & 0xFF);
			final int minorInverted = (minor & 0xFF) << 8
					| ((minor >> 8) & 0xFF);
			mMajorMinorCharacteristic.setValue(majorInverted,
					BluetoothGattCharacteristic.FORMAT_UINT16, 0);
			mMajorMinorCharacteristic.setValue(minorInverted,
					BluetoothGattCharacteristic.FORMAT_UINT16, 2);
			mBluetoothGatt.writeCharacteristic(mMajorMinorCharacteristic);
			return true;
		}

		/**
		 * Returns the pair of current Major and Minor values. This reads the
		 * value from the local cache. The {@link #read()} method must be
		 * invoked before to read the current value from the device.
		 * 
		 * @return the pair where the first value is the major number and the
		 *         second is the minor value
		 */
		public Pair<Integer, Integer> getMajorAndMinor() {
			final BluetoothGattCharacteristic characteristic = mMajorMinorCharacteristic;
			if (characteristic != null) {
				final byte[] data = characteristic.getValue();
				if (data == null || data.length != 4)
					return null;
				final int major = decodeUInt16(characteristic, 0);
				final int minor = decodeUInt16(characteristic, 2);
				return new Pair<Integer, Integer>(major, minor);
			}
			return null;
		}
		

		/**
		 * Overwrites the beacon calibration RSSI value.
		 * 
		 * @param rssi
		 *            the RSSI value calculated at 1m distance from the beacon
		 *            using iPhone 5S.
		 * @return <code>true</code> if altering major and minor is supported
		 *         (required characteristic exists)
		 */
		public boolean setCalibratedRssi(final int rssi) {
			if (mRssiCharacteristic == null)
				return false;

			mRssiCharacteristic.setValue(rssi,
					BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			mBluetoothGatt.writeCharacteristic(mRssiCharacteristic);
			return true;
		}
		/**
		 * 重写beacon的发射频率
		 * @param advertisingInterval
		 * @return
		 */
		public boolean setAdvertisingInterval(final int advertisingInterval) {
			if (mAdvertisingintervalCharacteristic == null)
				return false;

			mAdvertisingintervalCharacteristic.setValue(advertisingInterval,
					BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			mBluetoothGatt.writeCharacteristic(mAdvertisingintervalCharacteristic);
			return true;
		}
		/**
		 * 重写beacon的发射功率
		 * @param transmitPower
		 * @return
		 */
		public boolean setTransmitPower(final int transmitPower) {
			if (mTransmitpowerCharacteristic == null)
				return false;

			mTransmitpowerCharacteristic.setValue(transmitPower,
					BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			mBluetoothGatt.writeCharacteristic(mTransmitpowerCharacteristic);
			return true;
		}
		/**
		 * Obtains the cached value of the AdvertisingInterval characteristic. If the value has
		 * not been obtained yet using {@link #read()}, or the characteristic
		 * has not been found on the beacon, <code>null</code> is returned.
		 * 
		 * @return the RSSI value or <code>null</code>
		 */
		public Integer getAdvertisingInterval() {
			final BluetoothGattCharacteristic characteristic = mAdvertisingintervalCharacteristic;
			if (characteristic != null) {
				final byte[] data = characteristic.getValue();
				if (data == null || data.length < 1)
					return null;
				return characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			}
			return null;
		}
		/**
		 * Obtains the cached value of the AdvertisingInterval characteristic. If the value has
		 * not been obtained yet using {@link #read()}, or the characteristic
		 * has not been found on the beacon, <code>null</code> is returned.
		 * 
		 * @return the RSSI value or <code>null</code>
		 */
		public Integer getTransmitPower() {
			final BluetoothGattCharacteristic characteristic = mTransmitpowerCharacteristic;
			if (characteristic != null) {
				final byte[] data = characteristic.getValue();
				if (data == null || data.length < 1)
					return null;
				return characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			}
			return null;
		}
		/**
		 * Obtains the cached value of the RSSI characteristic. If the value has
		 * not been obtained yet using {@link #read()}, or the characteristic
		 * has not been found on the beacon, <code>null</code> is returned.
		 * 
		 * @return the RSSI value or <code>null</code>
		 */
		public Integer getCalibratedRssi() {
			final BluetoothGattCharacteristic characteristic = mRssiCharacteristic;
			if (characteristic != null) {
				final byte[] data = characteristic.getValue();
				if (data == null || data.length < 1)
					return null;
				return characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			}
			return null;
		}
		public int getState() {
			return mConnectionState;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		initialize();
		mHandler = new Handler();
		mConnectionState = STATE_DISCONNECTED;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mBluetoothGatt != null)
			mBluetoothGatt.disconnect();
		mHandler = null;
		mBluetoothDevice = null;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return new ServiceBinder();
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		// We want to allow rebinding
		return true;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		mBluetoothDevice = intent.getParcelableExtra(EXTRA_DATA);
		return START_NOT_STICKY;
	}

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 */
	private void initialize() {
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		mAdapter = bluetoothManager.getAdapter();
	}

	private void setState(final int state) {
		mConnectionState = state;
		final Intent intent = new Intent(ACTION_STATE_CHANGED);
		intent.putExtra(EXTRA_DATA, state);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastUuid(final UUID uuid) {
		final Intent intent = new Intent(ACTION_UUID_READY);
		intent.putExtra(EXTRA_DATA, new ParcelUuid(uuid));
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastMajorAndMinor(final int major, final int minor) {
		final Intent intent = new Intent(ACTION_MAJOR_MINOR_READY);
		intent.putExtra(EXTRA_MAJOR, major);
		intent.putExtra(EXTRA_MINOR, minor);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastRssi(final int rssi) {
		final Intent intent = new Intent(ACTION_RSSI_READY);
		intent.putExtra(EXTRA_DATA, rssi);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastAdvertisinginterval(final int advertisinginterval) {
		final Intent intent = new Intent(ACTION_ADVERTISINGINTERVAL_READY);
		intent.putExtra(EXTRA_DATA, advertisinginterval);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastTransmitpower(final int transmitpower) {
		final Intent intent = new Intent(ACTION_TRANSMITPOWER_READY);
		intent.putExtra(EXTRA_DATA, transmitpower);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastOperationCompleted() {
		final Intent intent = new Intent(ACTION_DONE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastError(final int error) {
		final Intent intent = new Intent(ACTION_GATT_ERROR);
		intent.putExtra(EXTRA_DATA, error);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	/**
	 * Clears the device cache.
	 * <p>
	 * CAUTION:<br />
	 * It is very unsafe to call the refresh() method. First of all it's hidden
	 * so it may be removed in the future release of Android. We do it because
	 * Nordic Beacon may advertise as a beacon, as Beacon Config or DFU. Android
	 * does not clear cache then device is disconnected unless manually
	 * restarted Bluetooth Adapter. To do this in the code we need to call
	 * {@link BluetoothGatt#refresh()} method. However is may cause a lot of
	 * troubles. Ideally it should be called before connection attempt but we
	 * get 'gatt' object by calling connectGatt method so when the connection
	 * already has been started. Calling refresh() afterwards causes errors 129
	 * and 133 to pop up from time to time when refresh takes place actually
	 * during service discovery. It seems to be asynchronous method. Therefore
	 * we are refreshing the device after disconnecting from it, before closing
	 * gatt. Sometimes you may obtain services from cache, not the actual values
	 * so reconnection is required.
	 * 
	 * @param gatt
	 *            the Bluetooth GATT object to refresh.
	 */
	private boolean refreshDeviceCache(final BluetoothGatt gatt) {
		/*
		 * There is a refresh() method in BluetoothGatt class but for now it's
		 * hidden. We will call it using reflections.
		 */
		try {
			final Method refresh = gatt.getClass().getMethod("refresh");
			if (refresh != null) {
				return (Boolean) refresh.invoke(gatt);
			}
		} catch (final Exception e) {
			loge("An exception occured while refreshing device");
		}
		return false;
	}

	private void loge(final String message) {
		if (BuildConfig.DEBUG)
			Log.e(TAG, message);
	}

	private void logw(final String message) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, message);
	}

	public static int decodeUInt16(
			final BluetoothGattCharacteristic characteristic, final int offset) {
		final byte[] data = characteristic.getValue();
		return (unsignedByteToInt(data[offset]) << 8)
				| unsignedByteToInt(data[offset + 1]);
	}

	public static UUID decodeBeaconUUID(
			final BluetoothGattCharacteristic characteristic) {
		final byte[] data = characteristic.getValue();
		final long mostSigBits = (unsignedByteToLong(data[0]) << 56)
				+ (unsignedByteToLong(data[1]) << 48)
				+ (unsignedByteToLong(data[2]) << 40)
				+ (unsignedByteToLong(data[3]) << 32)
				+ (unsignedByteToLong(data[4]) << 24)
				+ (unsignedByteToLong(data[5]) << 16)
				+ (unsignedByteToLong(data[6]) << 8)
				+ unsignedByteToLong(data[7]);
		final long leastSigBits = (unsignedByteToLong(data[8]) << 56)
				+ (unsignedByteToLong(data[9]) << 48)
				+ (unsignedByteToLong(data[10]) << 40)
				+ (unsignedByteToLong(data[11]) << 32)
				+ (unsignedByteToLong(data[12]) << 24)
				+ (unsignedByteToLong(data[13]) << 16)
				+ (unsignedByteToLong(data[14]) << 8)
				+ unsignedByteToLong(data[15]);
		return new UUID(mostSigBits, leastSigBits);
	}

	/**
	 * Convert a signed byte to an unsigned long.
	 */
	public static long unsignedByteToLong(byte b) {
		return b & 0xFF;
	}

	/**
	 * Convert a signed byte to an unsigned int.
	 */
	public static int unsignedByteToInt(int b) {
		return b & 0xFF;
	}
}

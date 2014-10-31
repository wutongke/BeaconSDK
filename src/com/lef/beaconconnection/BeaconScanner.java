package com.lef.beaconconnection;

import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;


public class BeaconScanner {
	
	private final static String TAG = "BeaconScanner";
	private BluetoothAdapter mBluetoothAdapter;
	private Context mContext;
	private static final UUID BEACON_CONFIG_UUID = UUID.fromString("955A1523-0FE2-F5AA-0A094-84B8D4F3E8AD");
	private final static String PARAM_UUID = "param_uuid";
	private ScannerListener mListener;

	private UUID mUuid;
	private boolean mIsScanning = false;
	
	public BeaconScanner(Context context,ScannerListener listener) {
		super();
		this.mContext = context;
		this.mListener = listener;
		BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = manager.getAdapter();
	}

	/**
	 * 开始扫描设备
	 */
	public void startScan() {
		if (!mIsScanning) {
			mBluetoothAdapter.startLeScan(mLEScanCallback);
			mIsScanning = true;
		}
	}

	/**
	 * 停止蓝牙扫描
	 */
	public void stopScan() {
		if (mIsScanning) {
			mBluetoothAdapter.stopLeScan(mLEScanCallback);
			mIsScanning = false;
		}
	}
	/**
	 * Callback for scanned devices class {@link ScannerServiceParser} will be used to filter devices with custom BLE service UUID then the device will be added in a list
	 */
	private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device != null) {
				try {
					if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, mUuid)) {
						// On some devices device.getName() is always null. We have to parse the name manually :(
						// This bug has been found on Sony Xperia Z1 (C6903) with Android 4.3.
						// https://devzone.nordicsemi.com/index.php/cannot-see-device-name-in-sony-z1
						mListener.ScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi);
					}
				} catch (Exception e) {
					Log.w(TAG, "Invalid data in Advertisement packet " + e.toString());
				}
			}
		}
	};
}

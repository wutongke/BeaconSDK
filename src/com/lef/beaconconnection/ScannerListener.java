/*******************************************************************************
 * Copyright (c) 2014 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.lef.beaconconnection;

import android.bluetooth.BluetoothDevice;

public interface ScannerListener {

	/**
	 * 
	 * @param device 蓝牙设备
	 * @param DeviceName 设备名称
	 * @param rssi 设备的rssi
	 */
	public void ScannedDevice(BluetoothDevice device,String DeviceName,int rssi);
}

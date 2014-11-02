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
	 * Called when user has selected the device.
	 * 
	 * @param device
	 *            the selected device. May not be null.
	 * @param the
	 *            device name.
	 */
	void onDeviceSelected(final BluetoothDevice device, final String name);

}

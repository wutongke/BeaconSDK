/*******************************************************************************
 * Copyright (c) 2014 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.lef.beaconconnection;


public interface ScannerListener {
	/**
	 * 扫描到新的beacon设备
	 * @param beaconDevice
	 */
	public void onNewBeacon(BeaconDevice beaconDevice);
	/**
	 * 离开之前扫描到beacon设备
	 * @param beaconDevice
	 */
	public void onGoneBeacon(BeaconDevice beaconDevice);
	/**
	 * 更新扫描到beacon设备的信息
	 * @param beaconDevice
	 */
	public void onUpdateBeacon(BeaconDevice beaconDevice);
}

package com.lef.scanner;


/**
 *  An interface for an Android <code>Activity</code> 
 * that wants to interact with iBeacons.  
 * @author lief
 *
 */
public interface BeaconConnectionCallback {
	/**
	 * 返回连接状态
	 * 
	 * @param beacon
	 *            当前连接的beacon
	 * @param status
	 *            返回连接状态 {@link}UpdateService.STATE_CONNECTED 和 {@link}
	 *            UpdateService.STATE_DISCONNECTED
	 */
	public void onConnectedState(IBeacon beacon, int status);

	/**
	 * @exception 返回MAJORE和MINOR的设置结果
	 *                返回设置结果
	 * @param beacon
	 *            当前连接的beacon
	 * @param status
	 *            返回设置结果 {@link BeaconConnection#SUCCESS} 、
	 *            {@link BeaconConnection#INVALIDVALUE}和
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetMajoMinor(IBeacon beacon, int status);

	/**
	 * @exception 返回基本设置的设置结果，目前还没有启用该功能，将用于设置发射功率和发射频率
	 *                返回设置结果
	 * @param beacon
	 *            当前连接的beacon
	 * @param status
	 *            返回设置结果 {@link BeaconConnection#SUCCESS}、
	 *            {@link BeaconConnection#INVALIDVALUE} 和
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetBaseSetting(IBeacon beacon, int status);

	/**
	 * @exception 返回计算RSSI的设置结果
	 *                返回设置结果
	 * @param beacon
	 *            当前连接的beacon
	 * @param status
	 *            返回设置结果 {@link BeaconConnection#SUCCESS}、
	 *            {@link BeaconConnection#INVALIDVALUE} 和
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetCalRssi(IBeacon beacon, int status);

	/**
	 * @exception 返回UUID的设置结果
	 *                返回设置结果
	 * @param beacon
	 *            当前连接的beacon
	 * @param status
	 *            返回设置结果 {@link BeaconConnection#SUCCESS} 、
	 *            {@link BeaconConnection#INVALIDVALUE}和
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetProximityUUID(IBeacon beacon, int status);

	/**
	 * 返回已连接设备的uuid
	 * 
	 * @param proximityUuid
	 */
	public void onGetUUID(String proximityUuid);

	/**
	 * 返回已连接设备的major
	 * 
	 * @param major
	 */
	public void onGetMajor(int major);

	/**
	 * 返回已连接设备的minor
	 * 
	 * @param minor
	 */
	public void onGetMinor(int minor);

	/**
	 * 返回已连接设备的计算Rssi
	 * 
	 * @param rssi
	 */
	public void onGetRssi(int rssi);
}

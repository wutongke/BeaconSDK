package com.lef.beaconconnection;

import com.lef.scanner.IBeacon;


public interface BeaconConnectionCallback {
	/**
	 * ��������״̬
	 * 
	 * @param beacon
	 *            ��ǰ���ӵ�beacon
	 * @param status
	 *            ��������״̬ {@link}UpdateService.STATE_CONNECTED �� {@link}
	 *            UpdateService.STATE_DISCONNECTED
	 */
	public void onConnectedState(IBeacon beacon, int status);

	/**
	 * @exception ����MAJORE��MINOR�����ý��
	 *                �������ý��
	 * @param beacon
	 *            ��ǰ���ӵ�beacon
	 * @param status
	 *            �������ý�� {@link BeaconConnection#SUCCESS} ��
	 *            {@link BeaconConnection#INVALIDVALUE}��
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetMajoMinor(IBeacon beacon, int status);

	/**
	 * @exception ���ػ������õ����ý��
	 *                �������ý��
	 * @param beacon
	 *            ��ǰ���ӵ�beacon
	 * @param status
	 *            �������ý�� {@link BeaconConnection#SUCCESS}��
	 *            {@link BeaconConnection#INVALIDVALUE} ��
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetBaseSetting(IBeacon beacon, int status);

	/**
	 * @exception ���ؼ���RSSI�����ý��
	 *                �������ý��
	 * @param beacon
	 *            ��ǰ���ӵ�beacon
	 * @param status
	 *            �������ý�� {@link BeaconConnection#SUCCESS}��
	 *            {@link BeaconConnection#INVALIDVALUE} ��
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetCalRssi(IBeacon beacon, int status);

	/**
	 * @exception ����UUID�����ý��
	 *                �������ý��
	 * @param beacon
	 *            ��ǰ���ӵ�beacon
	 * @param status
	 *            �������ý�� {@link BeaconConnection#SUCCESS} ��
	 *            {@link BeaconConnection#INVALIDVALUE}��
	 *            {@link BeaconConnection#FAILURE}
	 */
	public void onSetProximityUUID(IBeacon beacon, int status);

	/**
	 * �����������豸��uuid
	 * 
	 * @param proximityUuid
	 */
	public void onGetUUID(String proximityUuid);

	/**
	 * �����������豸��major
	 * 
	 * @param major
	 */
	public void onGetMajor(int major);

	/**
	 * �����������豸��minor
	 * 
	 * @param minor
	 */
	public void onGetMinor(int minor);

	/**
	 * �����������豸�ļ���Rssi
	 * 
	 * @param rssi
	 */
	public void onGetRssi(int rssi);
}

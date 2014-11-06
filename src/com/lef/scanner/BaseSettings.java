package com.lef.scanner;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseSettings {
	public AdvertisingInterval advertisingInterval;
	public TransmitPower transmitPower;
	public BaseSettings() {
		// TODO Auto-generated constructor stub
		this.advertisingInterval = AdvertisingInterval.UNKNOWN;
		this.transmitPower = TransmitPower.UNKNOWN;
	}
	public BaseSettings(int advertisingInterval,int transmitPower) {
		// TODO Auto-generated constructor stub
		this.advertisingInterval = getAdvertisingIntervalByInt(advertisingInterval);
		this.transmitPower = getTransmitPowerByInt(transmitPower);
	}
	public enum AdvertisingInterval {
		VERYHIGH(0),HIGH(1),MEDIUM(2),LOW(3),VERYLOW(4),MIN(5),UNKNOWN(6);
		int aValue;
		private AdvertisingInterval(int aValue){
			this.aValue = aValue;
		}
		@Override
        public String toString() {
            return String.valueOf(this.aValue);
        }
		
	}
	public enum TransmitPower{
		MAX(0),VERYHIGH(1),HIGH(2),MEDIUM(3),LOW(4),VERYLOW(5),MIN(6),UNKNOWN(7);
		int aValue;
		private TransmitPower(int aValue){
			this.aValue = aValue;
		}
		@Override
        public String toString() {
            return String.valueOf(this.aValue);
        }
	}
	/**
	 * 获取基本配置，开发者不需要使用，最好设置成选择式的方式
	 * @param aValue
	 * @return
	 */
	public static AdvertisingInterval getAdvertisingIntervalByInt(int aValue){
		AdvertisingInterval[] ads = AdvertisingInterval.values();
		for(AdvertisingInterval ad :ads){
			if(ad.aValue == aValue){
				return ad;
			}
		}
		return AdvertisingInterval.UNKNOWN;
	}
	/**
	 * 获取基本配置，开发者不需要使用，最好设置成选择式的方式
	 * @param aValue
	 * @return
	 */
	public static TransmitPower getTransmitPowerByInt(int aValue){
		TransmitPower[] ads = TransmitPower.values();
		for(TransmitPower ad :ads){
			if(ad.aValue == aValue){
				return ad;
			}
		}
		return TransmitPower.UNKNOWN;
	}
	/**
	 * 获取广播频率
	 * @return
	 */
	public  AdvertisingInterval getAdvertisingInterval() {
		return advertisingInterval;
	}
	/**
	 * 获取发射功率
	 * @return
	 */
	public TransmitPower getTransmitPower() {
		return transmitPower;
	}
	protected void setaInterval(AdvertisingInterval aInterval) {
		this.advertisingInterval = aInterval;
	}
	protected void settPower(TransmitPower tPower) {
		this.transmitPower = tPower;
	}
}

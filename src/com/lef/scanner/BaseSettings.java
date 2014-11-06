package com.lef.scanner;


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
	/**
	 * 广播频率<br>
	 * VERYHIGH(0),HIGH(1),MEDIUM(2),LOW(3),VERYLOW(4),MIN(5),UNKNOWN(6);<br>
	 * 0:100ms<br>
	 * 1:300ms<br>
	 * 2:500ms<br>
	 * 3:1s<br>
	 * 4:2s<br>
	 * 5:5s<br>
	 * @author lief
	 *
	 */
	public enum AdvertisingInterval {
		/**
		 * 0:100ms
		 */
		VERYHIGH(0),
		/**
		 * 1:300ms
		 */
		HIGH(1),
		/**
		 * 2:500ms
		 */
		MEDIUM(2),
		/**
		 * 3:1s
		 */
		LOW(3),
		/**
		 * 4:2s
		 */
		VERYLOW(4),
		/**
		 * 5:5s
		 */
		MIN(5),
		UNKNOWN(6);
		int aValue;
		private AdvertisingInterval(int aValue){
			this.aValue = aValue;
		}
		@Override
        public String toString() {
            return String.valueOf(this.aValue);
        }
		
	}
	/**
	 * 发射功率<br>
	 * MAX(0),VERYHIGH(1),HIGH(2),MEDIUM(3),LOW(4),VERYLOW(5),MIN(6),UNKNOWN(7);<br>
	 * 0:4dbm<br>
	 * 1:0dbm<br>
	 * 2:-4dbm<br>
	 * 3:-8dbm<br>
	 * 4:-12dbm<br>
	 * 5:-16dbm<br>
	 * 6:-20dbm<br>
	 * @author lief
	 *
	 */
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
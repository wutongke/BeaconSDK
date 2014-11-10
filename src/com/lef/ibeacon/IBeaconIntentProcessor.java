/**
 * Radius Networks, Inc.
 * http://www.radiusnetworks.com
 * 
 * @author David G. Young
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.lef.ibeacon;

import com.lef.ibeacon.service.MonitoringData;
import com.lef.ibeacon.service.RangingData;
import com.lef.scanner.IBeacon;
import com.lef.scanner.IBeaconData;
import com.lef.scanner.IBeaconManager;
import com.lef.scanner.MonitorNotifier;
import com.lef.scanner.RangeNotifier;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

@TargetApi(3)
public class IBeaconIntentProcessor extends IntentService {
	private static final String TAG = "IBeaconIntentProcessor";
	private boolean initialized = false;

	public IBeaconIntentProcessor() {
		super("IBeaconIntentProcessor");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (IBeaconManager.debug) Log.d(TAG, "got an intent to process");
		
		MonitoringData monitoringData = null;
		RangingData rangingData = null;
		RangingData rangingNewBeaocn = null;
		RangingData rangingUpdateBeacons = null;
		RangingData rangingGoneBeaocn = null;
		
		
		if (intent != null && intent.getExtras() != null) {
			monitoringData = (MonitoringData) intent.getExtras().get("monitoringData");
			rangingData = (RangingData) intent.getExtras().get("rangingData");	
			rangingNewBeaocn = (RangingData) intent.getExtras().get("rangingDataNewBeacon");
			rangingUpdateBeacons = (RangingData) intent.getExtras().get("rangingDataUpdateBeacons");
			rangingGoneBeaocn = (RangingData) intent.getExtras().get("rangingDataGoneBeacon");
		}
		
		if (rangingData != null) {
			if (IBeaconManager.debug) Log.d(TAG, "got ranging data");
            if (rangingData.getIBeacons() == null) {
                Log.w(TAG, "Ranging data has a null iBeacons collection");
            }
			RangeNotifier notifier = IBeaconManager.getInstanceForApplication(this).getRangingNotifier();
//            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingData.getIBeacons());
            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingData.getIBeacons());
			if (notifier != null) {
				notifier.didRangeBeaconsInRegion(iBeacons, rangingData.getRegion());
			}
            else {
                if (IBeaconManager.debug) Log.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
            RangeNotifier dataNotifier = IBeaconManager.getInstanceForApplication(this).getDataRequestNotifier();
            if (dataNotifier != null) {
                dataNotifier.didRangeBeaconsInRegion(iBeacons, rangingData.getRegion());
            }

		}
		if(rangingNewBeaocn!=null){
			if (IBeaconManager.debug) Log.d(TAG, "got ranging data");
            if (rangingNewBeaocn.getIBeacons() == null) {
                Log.w(TAG, "Ranging data has a null iBeacons collection");
            }
			RangeNotifier notifier = IBeaconManager.getInstanceForApplication(this).getRangingNotifier();
//            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingData.getIBeacons());
            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingNewBeaocn.getIBeacons());
			if (notifier != null) {
				notifier.onNewBeacons(iBeacons, rangingNewBeaocn.getRegion());
			}
            else {
                if (IBeaconManager.debug) Log.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
            RangeNotifier dataNotifier = IBeaconManager.getInstanceForApplication(this).getDataRequestNotifier();
            if (dataNotifier != null) {
                dataNotifier.onNewBeacons(iBeacons, rangingNewBeaocn.getRegion());
            }
		}
		if(rangingUpdateBeacons!=null){
			if (IBeaconManager.debug) Log.d(TAG, "got ranging data");
            if (rangingUpdateBeacons.getIBeacons() == null) {
                Log.w(TAG, "Ranging data has a null iBeacons collection");
            }
			RangeNotifier notifier = IBeaconManager.getInstanceForApplication(this).getRangingNotifier();
//            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingData.getIBeacons());
            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingUpdateBeacons.getIBeacons());
			if (notifier != null) {
				notifier.onUpdateBeacon(iBeacons, rangingUpdateBeacons.getRegion());
			}
            else {
                if (IBeaconManager.debug) Log.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
            RangeNotifier dataNotifier = IBeaconManager.getInstanceForApplication(this).getDataRequestNotifier();
            if (dataNotifier != null) {
                dataNotifier.onUpdateBeacon(iBeacons, rangingUpdateBeacons.getRegion());
            }
		}
		if(rangingGoneBeaocn!=null){
			if (IBeaconManager.debug) Log.d(TAG, "got ranging data");
            if (rangingGoneBeaocn.getIBeacons() == null) {
                Log.w(TAG, "Ranging data has a null iBeacons collection");
            }
			RangeNotifier notifier = IBeaconManager.getInstanceForApplication(this).getRangingNotifier();
            java.util.Collection<IBeacon> iBeacons = IBeaconData.fromIBeaconDatas(rangingGoneBeaocn.getIBeacons());
			if (notifier != null) {
				notifier.onGoneBeacons(iBeacons, rangingGoneBeaocn.getRegion());
			}
            else {
                if (IBeaconManager.debug) Log.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
            RangeNotifier dataNotifier = IBeaconManager.getInstanceForApplication(this).getDataRequestNotifier();
            if (dataNotifier != null) {
                dataNotifier.onGoneBeacons(iBeacons, rangingGoneBeaocn.getRegion());
            }
		}
		if (monitoringData != null) {
			if (IBeaconManager.debug) Log.d(TAG, "got monitoring data");
			MonitorNotifier notifier = IBeaconManager.getInstanceForApplication(this).getMonitoringNotifier();
			if (notifier != null) {
				if (IBeaconManager.debug) Log.d(TAG, "Calling monitoring notifier:"+notifier);
				notifier.didDetermineStateForRegion(monitoringData.isInside() ? MonitorNotifier.INSIDE : MonitorNotifier.OUTSIDE, monitoringData.getRegion());
				if (monitoringData.isInside()) {
					notifier.didEnterRegion(monitoringData.getRegion());
				}
				else {
					notifier.didExitRegion(monitoringData.getRegion());					
				}
					
			}
		}
				
	}

}

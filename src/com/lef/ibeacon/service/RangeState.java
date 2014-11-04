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
 * 包含range中的ibeacon
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.lef.ibeacon.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;

import com.lef.ibeacon.IBeacon;

public class RangeState {
	private static final String TAG = "RangeState";
	private Callback callback;
	private Set<IBeacon> iBeacons = new HashSet<IBeacon>();
	private Set<IBeacon> allIBeacons = new HashSet<IBeacon>();
	private long inside_expiration_millis;

	public RangeState(Callback c, long inside_expiration_millis) {
		callback = c;
		this.inside_expiration_millis = inside_expiration_millis;
	}

	public Callback getCallback() {
		return callback;
	}

	public void clearIBeacons() {
		synchronized (iBeacons) {
			iBeacons.clear();
		}
	}

	public Set<IBeacon> getIBeacons() {
		synchronized (iBeacons) {
			return iBeacons;
		}
	}

	public Set<IBeacon> getAllIBeacons() {
		return allIBeacons;
	}

	public long getInside_expiration_millis() {
		return inside_expiration_millis;
	}

	public void addIBeacon(IBeacon iBeacon) {
		synchronized (iBeacons) {
			iBeacons.add(iBeacon);
		}
	}

	public boolean isOutofRange(IBeacon iBeacon) {
		if (iBeacon.getUpdateTime() > 0
				&& (new Date()).getTime() - iBeacon.getUpdateTime() > inside_expiration_millis) {
			Log.d(TAG,
					"有个Beacon从区域中消失 "
							+ iBeacon.getUpdateTime()
							+ " was "
							+ ((new Date()).getTime() - iBeacon.getUpdateTime())
							+ " seconds ago, and that is over the expiration duration of  "
							+ inside_expiration_millis);
			return true;
		}
		return false;
	}

}
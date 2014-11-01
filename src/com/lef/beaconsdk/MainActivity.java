package com.lef.beaconsdk;

import com.lef.beaconconnection.BeaconConnection;
import com.lef.beaconconnection.BeaconConnectionCallback;
import com.lef.beaconconnection.BeaconDevice;
import com.lef.beaconconnection.BeaconScanner;
import com.lef.beaconconnection.ScannerListener;
import com.lef.beaconsdk.R;
import com.lef.ibeacon.IBeacon;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView uuid;
	private TextView major;
	private TextView minor;
	private TextView rssi;
	private Button connect;
	private BeaconScanner scanner;
	private BeaconConnection connection;
	private IBeacon mDevice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		uuid = (TextView)findViewById(R.id.uuid);
		major = (TextView)findViewById(R.id.major);
		minor = (TextView)findViewById(R.id.minor);
		rssi = (TextView)findViewById(R.id.rssi);
		connect = (Button)findViewById(R.id.connect);
		
		uuid.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(connection.isConnection()){
				}
			}
			
		});
		
		scanner = new BeaconScanner(this, new ScannerListener() {
			

			@Override
			public void onNewBeacon(BeaconDevice beaconDevice) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGoneBeacon(BeaconDevice beaconDevice) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onUpdateBeacon(BeaconDevice beaconDevice) {
				// TODO Auto-generated method stub
				
			}
		});
		connect.setOnClickListener(new  OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				scanner.startScan();
			}
		});
		
	}
	protected void initConn() {
		// TODO Auto-generated method stub
		connection = new BeaconConnection(MainActivity.this, mDevice, new BeaconConnectionCallback() {
			
			@Override
			public void onSetProximityUUID(IBeacon beacon, int status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSetMajoMinor(IBeacon beacon, int status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSetCalRssi(IBeacon beacon, int status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSetBaseSetting(IBeacon beacon, int status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGetUUID(String proximityUuid) {
				// TODO Auto-generated method stub
				uuid.setText(proximityUuid+"");
				
			}
			
			@Override
			public void onGetRssi(int rssi) {
				// TODO Auto-generated method stub
				MainActivity.this.rssi .setText(rssi+"");
			}
			
			@Override
			public void onGetMinor(int minor) {
				// TODO Auto-generated method stub
				MainActivity.this.minor.setText(minor+"");
			}
			
			@Override
			public void onGetMajor(int major) {
				// TODO Auto-generated method stub
				MainActivity.this.major.setText(major+"");
			}
			
			@Override
			public void onConnectedState(IBeacon beacon, int status) {
				// TODO Auto-generated method stub
				System.out.print("");
			}
		});
		connection.connect(mDevice.getBluetoothDevice());
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

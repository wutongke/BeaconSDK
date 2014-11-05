package com.lef.beaconsdk;

import java.util.ArrayList;
import java.util.Collection;

import com.lef.scanner.IBeacon;
import com.lef.scanner.IBeaconData;
import com.lef.scanner.IBeaconManager;
import com.lef.scanner.MonitorNotifier;
import com.lef.scanner.RangeNotifier;
import com.lef.scanner.Region;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements
		com.lef.scanner.IBeaconConsumer {
	// 启动蓝牙请求码
	protected static final int BLUTETOOTH = 1;
	private IBeaconManager iBeaconManager;
	private ListView beaconListView;
	/**
	 * 数据更新
	 */
	private ArrayList<IBeacon> beaconDataListA = new ArrayList<IBeacon>();
	/**
	 * UI数据
	 */
	private ArrayList<IBeacon> beaconDataListB = new ArrayList<IBeacon>();

	// 云子数据Adapter
	private BeaconAdapter beaconListAdapter;
	// 数据常量
	private static final int UPDATEUI = 1;
	private static final int PROGRESSBARGONE = 2;
	private static final int CLICKTOAST = 3;
	//
	Handler UIHandler = new Handler();
	// 开始扫描时使用ProgressBar
	private ProgressBar progressScan;
	private TextView progressScanTextView;
	private boolean ProgressBarVisibile = true;

	/**
	 * 侧键menu
	 */
	private ResideMenu resideMenu;
	private ResideMenuItem school;
	private ResideMenuItem eMail;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATEUI:
				beaconDataListB.clear();
				beaconDataListB.addAll(beaconDataListA);
				beaconListAdapter.notifyDataSetChanged();
				break;
			case PROGRESSBARGONE:
				progressScan.setVisibility(TextView.GONE);
				progressScanTextView.setVisibility(TextView.GONE);
				ProgressBarVisibile = false;
				break;
			case CLICKTOAST:
				Toast.makeText(MainActivity.this, "请设置beacon部署模式",
						Toast.LENGTH_SHORT).show();
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 设置名称
		ActionBar mainBar = getActionBar();
		mainBar.setLogo(R.drawable.ic_list);
		mainBar.setTitle(R.string.ibeacon_list);

		// attach to current activity;
		resideMenu = new ResideMenu(this);
		resideMenu.setBackground(R.drawable.menu_background);
		resideMenu.attachToActivity(this);
		// valid scale factor is between 0.0f and 1.0f. leftmenu'width is
		// 150dip.
		resideMenu.setScaleValue(0.6f);
		// create menu items;
		school = new ResideMenuItem(this, R.drawable.icon_home, "School:BUPT");
		eMail = new ResideMenuItem(this, R.drawable.icon_profile,
				"Email:butp_lef@163.com");

		resideMenu.addMenuItem(school, 0);
		resideMenu.addMenuItem(eMail, 0);

		iBeaconManager = IBeaconManager.getInstanceForApplication(this);

		// 获取View
		beaconListView = (ListView) findViewById(R.id.yunzilist);
		progressScan = (ProgressBar) findViewById(R.id.progressScan);
		progressScanTextView = (TextView) findViewById(R.id.progressScantext);

		beaconListAdapter = new BeaconAdapter(this, beaconDataListB);
		beaconListView.setAdapter(beaconListAdapter);
		beaconListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (beaconDataListB.get(position).isCanBeConnected()) {
					Intent mintent = new Intent(MainActivity.this,
							BeaconModify.class);
					// mintent.putExtra("address",
					// beaconDataListB.get(position).getBluetoothAddress());
					mintent.putExtra("beacon",
							new IBeaconData(beaconDataListB.get(position)));
					startActivity(mintent);
					beaconDataListA.remove(position);
				} else {
					handler.sendEmptyMessage(CLICKTOAST);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (iBeaconManager!=null&&!iBeaconManager.isBound(this)) {
			// 蓝牙dialog
			initBluetooth();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (iBeaconManager.isBound(this)) {
			iBeaconManager.unBind(this);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (iBeaconManager != null && iBeaconManager.isBound(this)) {
			iBeaconManager.unBind(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 请求打开蓝牙，如果不打开，将退出程序
	 */
	private void initBluetooth() {
		// TODO Auto-generated method stub
		final BluetoothAdapter blueToothEable = BluetoothAdapter
				.getDefaultAdapter();
		if (!blueToothEable.isEnabled()) {
			new AlertDialog.Builder(MainActivity.this)
					.setIcon(R.drawable.title_bar_menu).setTitle("蓝牙开启")
					.setMessage("云子配置需要开启蓝牙").setCancelable(false)
					.setPositiveButton("开启", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							blueToothEable.enable();
							iBeaconManager.bind(MainActivity.this);
						}
					}).setNegativeButton("退出", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							MainActivity.this.finish();
						}
					}).create().show();
		} else {
			iBeaconManager.setForegroundScanPeriod(800);
			iBeaconManager.bind(this);
		}
	}

	/**
	 * 手势检测
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return resideMenu.dispatchTouchEvent(ev);
	}

	@Override
	public void onIBeaconServiceConnect() {
		// TODO Auto-generated method stub
		// 启动Range服务
		iBeaconManager.setRangeNotifier(new RangeNotifier() {

			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				if (ProgressBarVisibile) {
					handler.sendEmptyMessage(PROGRESSBARGONE);
				}
//				java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
//				while (iterator.hasNext()) {
//					IBeacon temp = iterator.next();
//					if (beaconDataListA.contains(temp)) {
//						beaconDataListA.set(beaconDataListA.indexOf(temp), temp);
//						handler.sendEmptyMessage(UPDATEUI);
//					} else {
//						beaconDataListA.add(temp);
//						handler.sendEmptyMessage(UPDATEUI);
//					}
//
//				}

			}

			@Override
			public void onNewBeacons(Collection<IBeacon> iBeacons, Region region) {
				// TODO Auto-generated method stub
				beaconDataListA.addAll(iBeacons);
				handler.sendEmptyMessage(UPDATEUI);
			}

			@Override
			public void onGoneBeacons(Collection<IBeacon> iBeacons,
					Region region) {
				// TODO Auto-generated method stub
				java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
				while (iterator.hasNext()) {
					IBeacon temp = iterator.next();
					if (beaconDataListA.contains(temp)) {
						beaconDataListA.remove(temp);
					}
					handler.sendEmptyMessage(UPDATEUI);
				}
			}

			@Override
			public void onUpdateBeacon(Collection<IBeacon> iBeacons,
					Region region) {
				// TODO Auto-generated method stub
				java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
				while (iterator.hasNext()) {
					IBeacon temp = iterator.next();
					if (beaconDataListA.contains(temp)) {
						beaconDataListA
								.set(beaconDataListA.indexOf(temp), temp);
					}
					handler.sendEmptyMessage(UPDATEUI);
				}
			}

			
		});
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

			@Override
			public void didExitRegion(Region region) {
				// TODO Auto-generated method stub
			}

			@Override
			public void didEnterRegion(Region region) {
				// TODO Auto-generated method stub

			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				// TODO Auto-generated method stub

			}
		});
		try {
			Region myRegion = new Region("myRangingUniqueId", null, null, null);
//			iBeaconManager.startMonitoringBeaconsInRegion(myRegion);
			iBeaconManager.startRangingBeaconsInRegion(myRegion);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}

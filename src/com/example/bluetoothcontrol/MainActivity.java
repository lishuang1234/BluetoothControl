package com.example.bluetoothcontrol;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public BluetoothAdapter adapter;
	public static final int REQUEST_DISCOVERABLE = 1;
	// public Discovery discovery;
//	public List<BluetoothDevice> deviceList;
	// public List<String> deviceInfor;
	public String infor;
	public ProgressDialog pd;
	public Button open;
	public TextView state;
	public TextView information;
	public Receiver receiver;
	public Intent sendToService;
	public static final String SERVICE = "com.example.server.start";
	public static final String SEND_ONE = "com.example.action.send_one";
	public static final String OPEN = "com.example.action.open";
	public static final String CONNECT_STATE = "com.example.action.connect-state";
	public static final String CONNECT_ING = "com.example.action.connect-ing";
	public static final String CUT_CONNECT = "com.example.action.cut-connect";
	public static final String WORK_STATE = "com.example.action.work-state";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		adapter = BluetoothAdapter.getDefaultAdapter();
		//deviceList = new ArrayList<BluetoothDevice>();
		// deviceInfor = new ArrayList<String>();
		pd = new ProgressDialog(MainActivity.this);
		state = (TextView) findViewById(R.id.state);//
		information = (TextView) findViewById(R.id.information);
		open = (Button) findViewById(R.id.open);
		open.setOnClickListener(new ClickListener());
		sendToService = new Intent();

		receiver = new Receiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECT_STATE);
		registerReceiver(receiver, filter);
		filter.addAction(CONNECT_ING);
		registerReceiver(receiver, filter);
		filter.addAction(WORK_STATE);
		registerReceiver(receiver, filter);
	}

	/**
	 * 打开蓝牙
	 * 
	 * @param v
	 */
	public void creat(View v) {

		Intent enabler = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivityForResult(enabler, REQUEST_DISCOVERABLE);
		state.setText("开启蓝牙！");

	}

	/**
	 * 搜索蓝牙设备
	 * 
	 * @param v
	 */
	public void search(View v) {
		state.setText("正在搜索！");
		adapter.startDiscovery();
		Intent intent2 = new Intent();
		intent2.setAction(SERVICE);
		startService(intent2);
		Intent intent3 = new Intent();
		intent3.setClass(MainActivity.this, OtherActivity.class);
		startActivity(intent3);
	}

	public void close(View v) {
		adapter.disable();
		state.setText("关闭蓝牙！");
	}

	public void cut(View v) {
		sendToService.setAction(CUT_CONNECT);
		sendBroadcast(sendToService);
		state.setText("断开连接！");
	}

	/**
	 * 控制区按钮监听
	 * 
	 * @author Administrator
	 * 
	 */
	class ClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch (v.getId()) {
			case R.id.open:
				sendToService.setAction(OPEN);
				sendBroadcast(sendToService);
				state.setText("打开/关闭设备！");
			}
		}
	}

	/**
	 * 接受反馈消息
	 * 
	 * @author Administrator
	 * 
	 */
	class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			if (action.equals(CONNECT_STATE)) {// 蓝牙连接反馈
				if (arg1.getBooleanExtra("connect-state", false)) {
					information.setText("连接成功！");
				} else {
					information.setText("连接失败，请重试！");
				}
			} else if (action.equals(CONNECT_ING)) {// 连接状态反馈
				if (arg1.getBooleanExtra("connecting", false)) {
					state.setText("正在连接蓝牙！");
				} else {
					state.setText("未连接蓝牙！");
				}
			} else if (action.equals(WORK_STATE)) {// 工作状态反馈
				String workState = arg1.getStringExtra("workState");
				System.out.println(workState);
				if (workState.equals("open")) {
					information.setText("设备已打开!");
				} else if (workState.equals("close")) {
					information.setText("设备已关闭!");
				}

			}
		}
	}
}

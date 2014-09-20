package com.example.bluetoothcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class OtherActivity extends Activity {

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(discovery);
	}

	public List<BluetoothDevice> deviceList;
	public boolean DISCOVERY_OVER = false;
	public ProgressDialog pd;
	public ArrayAdapter<String> adapter;
	public ListView list1;
	public List<String> infor;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String DEVICE_LIST = "com.example.action.device_list";
	public static final String SERVICE = "com.example.server.start";
	public static final String CONNECT = "com.example.action.connect";
	public static final String CONNECT_ING = "com.example.action.connect-ing";
	public Intent sendToMain;
	public BluetoothDevice device;
	public BluetoothSocket socket;
	public BluetoothAdapter btAdapter;
	public Discovery discovery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.other_activity);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		deviceList = new ArrayList<BluetoothDevice>();
		pd = new ProgressDialog(OtherActivity.this);
		list1 = (ListView) findViewById(R.id.list1);
		infor = new ArrayList<String>();
		sendToMain = new Intent();
		sendToMain.setAction(CONNECT_ING);

		discovery = new Discovery();
		IntentFilter filter = new IntentFilter();
		filter = new IntentFilter(DEVICE_LIST);
		registerReceiver(discovery, filter);

		new Work().execute(DISCOVERY_OVER);
	}

	/**
	 * 接受搜索到的蓝牙信息
	 * 
	 * @author Administrator
	 * 
	 */
	class Discovery extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();

			if (DEVICE_LIST.equals(action))
				;
			{
				infor = intent.getStringArrayListExtra("device");
				DISCOVERY_OVER = true;
			}
		}
	}

	/**
	 * 点击列表连接蓝牙设备
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("Discovery__over");
		list1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(CONNECT);
				intent.putExtra("position", position);
				sendBroadcast(intent);
				sendToMain.putExtra("connecting", true);
				sendBroadcast(sendToMain);
				intent.setClass(OtherActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 监听搜素结束后生成List
	 * 
	 * @author Administrator
	 * 
	 */
	protected class Work extends AsyncTask<Boolean, String, String> {
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			adapter = new ArrayAdapter<String>(OtherActivity.this,
					R.layout.information, infor);
			list1.setAdapter(adapter);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pd.setMessage("正在搜索，请稍后……");
			pd.setIndeterminate(false);// 在最大值最小值中移动
			pd.setCancelable(false);// 可以取消
			pd.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected String doInBackground(Boolean... params) {
			// TODO Auto-generated method stub
			while (true) {
				if (DISCOVERY_OVER) {
					pd.dismiss();
					break;
				}
			}
			return null;
		}
	}

}

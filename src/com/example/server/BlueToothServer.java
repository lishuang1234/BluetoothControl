package com.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;

public class BlueToothServer extends Service {
	public List<BluetoothDevice> deviceList;
	public boolean DISCOVERY_OVER = false;
	// public ProgressDialog pd;
	// public ArrayAdapter<String> adapter;
	// public ListView list1;
	public ArrayList<String> infor;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String DEVICE_LIST = "com.example.action.device_list";// 反馈搜到的蓝牙设备
	public static final String SEND_ONE = "com.example.action.send_one";// 风速1
	public static final String OPEN = "com.example.action.open";// 想蓝牙发送打开消息
	public static final String CONNECT = "com.example.action.connect";// 连接蓝牙消息
	public static final String CONNECT_STATE = "com.example.action.connect-state";// 连接状态反馈
	public static final String WORK_STATE = "com.example.action.work-state";
	public static final String CUT_CONNECT = "com.example.action.cut-connect";
	public BluetoothDevice device;
	public BluetoothSocket socket;
	public BluetoothAdapter btAdapter;
	public Intent sendToOther;
	public Intent sendToMain;
	public int position;
	public OutputStream outStream;
	public byte[] myByte;
	public InputStream inPutStream;
	public Discovery discovery;
	public StringBuffer buffer;
	public SendRe sr;
	public String sendInfor;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		deviceList = new ArrayList<BluetoothDevice>();
		// pd = new ProgressDialog(BlueToothServer.this);
		// list1 = (ListView) findViewById(R.id.list1);
		infor = new ArrayList<String>();
		myByte = new byte[1024];
		sendToMain = new Intent();

		sendToOther = new Intent();
		sendToOther.setAction(DEVICE_LIST);
		position = -1;

		// 注册狂波接受蓝牙设备
		discovery = new Discovery();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(discovery, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(discovery, filter);
		super.onCreate();

		sr = new SendRe();
		IntentFilter filter2 = new IntentFilter();
		filter2.addAction(SEND_ONE);
		filter2.addAction(OPEN);
		filter2.addAction(CONNECT);
		filter2.addAction(CUT_CONNECT);
		registerReceiver(sr, filter2);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			cutConnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unregisterReceiver(discovery);
		unregisterReceiver(sr);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		System.out.println("SERvice ====Start");

		new Work().execute(DISCOVERY_OVER);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 异步任务发送所得蓝牙设备信息
	 * 
	 * @author Administrator
	 * 
	 */
	protected class Work extends AsyncTask<Boolean, String, String> {
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

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
					System.out.println("stop ---Server");
					sendToOther.putStringArrayListExtra("device",
							turnString(deviceList));
					sendBroadcast(sendToOther);
					break;
				}
			}
			return null;
		}
	}

	/**
	 * 接受蓝牙设备信息
	 * 
	 * @author Administrator
	 * 
	 */
	class Discovery extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				deviceList.add(device);
				System.out.println("Discovery__ing" + device.getName() + "/n"
						+ device.getAddress());

				// 发送Device名称

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				DISCOVERY_OVER = true;

			}
		}
	}

	public ArrayList<String> turnString(List<BluetoothDevice> deviceList2) {
		// TODO Auto-generated method stub
		String[] infor1 = new String[deviceList2.size()];
		for (int i = 0; i < deviceList2.size(); i++) {
			infor1[i] = deviceList2.get(i).getName();
			if (infor.indexOf(infor1[i]) == -1) {
				infor.add(infor1[i]);
			}
		}
		return infor;
	}

	/**
	 * 获取Device连接
	 * 
	 * @param position
	 */
	protected void connectBlue(int position) {
		// TODO Auto-generated method stub
		// device = deviceList.get(position);
		device = btAdapter.getRemoteDevice(deviceList.get(position)
				.getAddress());
		System.out.println("BeforeContect~~~"
				+ deviceList.get(position).getAddress());

		new Connect().start();
	}

	/**
	 * 连接蓝牙操作线程
	 * 
	 * @author Administrator
	 * 
	 */
	class Connect extends Thread {
		// private BluetoothSocket tmp;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("BeforeContect~~~");
			sendToMain.setAction(CONNECT_STATE);
			try {
				socket = device.createRfcommSocketToServiceRecord(MY_UUID);
				btAdapter.cancelDiscovery();
				socket.connect();
				outStream = socket.getOutputStream();
				inPutStream = socket.getInputStream();
				System.out.println("Contect~~~");
				sendToMain.putExtra("connect-state", true);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ContectError~~~");
				sendToMain.putExtra("connect-state", false);
				try {
					socket.close();
					outStream.flush();
					outStream.close();
					inPutStream.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			sendBroadcast(sendToMain);
			super.run();

		}

	}

	/**
	 * 连接蓝牙后读取操作
	 * 
	 * @author Administrator
	 * 
	 */
	class Oper extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				outStream.write(sendInfor.getBytes());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						inPutStream));
				buffer = new StringBuffer();
				String line = "";
				String backInfor = "";
				while ((line = in.readLine()) != null) {
					buffer.append(line);
					System.out.println("----------------------->>>>"
							+ buffer.toString());
					backInfor = buffer.toString();
					sendState(backInfor);

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.run();
		}
	}

	/**
	 * 响应用户操作
	 * 
	 * @author Administrator
	 * 
	 */
	class SendRe extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			if (action.equals(OPEN)) {// 打开关闭设备
				sendInfor = "O";
				new Oper().start();
			} else if (action.equals(CONNECT)) {// 连接蓝牙设备
				position = arg1.getIntExtra("position", -1);
				if (position > -1) {
					connectBlue(position);
				}
			} else if (action.equals(CUT_CONNECT)) {
				System.out.println(CUT_CONNECT + "asdasd");
				try {
					cutConnect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void sendState(String string) {
		// TODO Auto-generated method stub
		sendToMain.setAction(WORK_STATE);
		if (string.equals("open")) {
			sendToMain.putExtra("workState", "open");
		} else if (string.equals("close")) {
			sendToMain.putExtra("workState", "close");
		}
		sendBroadcast(sendToMain);

	}

	public void cutConnect() throws IOException {
		// TODO Auto-generated method stub
		if (socket != null && outStream != null && inPutStream != null) {
			socket.close();
			outStream.flush();
			outStream.close();
			inPutStream.close();
		}
	}
}

package com.atet.jmdns.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jmdns.ServiceInfo;

import org.json.JSONObject;

import com.atet.jmdns.adapter.DeviceAdapter;
import com.atet.jmdns.app.JmdnsAPP;
import com.atet.jmdns.com.Param;
import com.atet.jmdns.connect.Communication;
import com.atet.jmdns.connect.Connection;
import com.atet.jmdns.connect.ConnectionWrapper;
import com.atet.jmdns.connect.MessageHandler;
import com.jmdns.multicast.device.TCPSocketCallback;
import com.jmdns.multicast.device.TCPSocketConnect;
import com.jmdns.multicast.device.TCPSocketFactory;
import com.jmdns.multicast.device.UDPSocket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	private String Tag = "MainActivity";
	private ListView lv_device = null;
	private List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
	private DeviceAdapter deviceAdapter = null;
	private Map<String, String> message = new HashMap<String, String>();
	private EditText etMsg = null;
	private Button btnSend = null;
	private JmdnsAPP jmdnsAPP = null;
	private UDPSocket udpSocket = null;
	private TCPSocketConnect tcpSocketConnect = null;
	private boolean isRunning = false;
	private String devName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		jmdnsAPP = (JmdnsAPP) getApplication();

		widget_init();
		message.put(Communication.MESSAGE, "1");
		message.put(Communication.MESSAGE_TYPE, "2");
		devName = android.os.Build.MODEL;
		// ((JmdnsAPP) getApplication())
		// .createConnectionWrapper(new ConnectionWrapper.OnCreatedListener() {
		// @Override
		// public void onCreated() {
		// Log.e(Tag, "OnCreatedListener");
		// }
		// });
		// JmdnsAPP.multiSocket.startSocket();

	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
			case Param.GET_A_DEVICE: {
				ServiceInfo serviceInfo = (ServiceInfo) msg.obj;
				Log.e(Tag, "get device:" + serviceInfo.getName());
				serviceInfos.add(serviceInfo);

				deviceAdapter.notifyDataSetChanged();

			}
				break;

			default:
				break;
			}
		};
	};

	private void widget_init() {
		lv_device = (ListView) findViewById(R.id.lv_device_name);
		deviceAdapter = new DeviceAdapter(MainActivity.this, serviceInfos);
		lv_device.setAdapter(deviceAdapter);
		lv_device.setOnItemClickListener(new ItemClick());
		etMsg = (EditText) findViewById(R.id.etMsg);
		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String msg = etMsg.getText().toString();
				// if (!msg.isEmpty()) {
				// Map<String, String> sendMsg = new HashMap<String, String>();
				// SimpleDateFormat format = new SimpleDateFormat(
				// "MM-dd   HH:mm:ss.SSS");
				// long time = System.currentTimeMillis();
				// String strTime = format.format(time);
				// sendMsg.put(Communication.MESSAGE_TYPE, strTime + "  "
				// + msg);
				// sendMsg.put(Communication.MESSAGE, "hahaha!");
				// startSendMsg(sendMsg);
				// // startSendMsg();
				// }
			}
		});
	}

	private class ItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			ServiceInfo serviceInfo = serviceInfos.get(arg2);
			ServiceInfo serviceInfo2 = JmdnsAPP.mJmdns
					.getServiceInfo(serviceInfo);
			if (serviceInfo2 != null) {
				InetAddress inetAddress = serviceInfo2.getInetAddress();
				if (tcpSocketConnect == null) {
					tcpSocketConnect = new TCPSocketConnect(
							new TCPSocketCallback() {
								@Override
								public void tcp_receive(byte[] buffer) {
									// TODO Auto-generated method stub

								}

								@Override
								public void tcp_disconnect() {
									// TODO Auto-generated method stub
									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub
											Toast.makeText(MainActivity.this,
													"连接失败!", 1000).show();
											tcpSocketConnect.resetConnect();
										}
									});
								}

								@Override
								public void tcp_connected() {
									// TODO Auto-generated method stub
									isRunning = true;
									tcpSocketConnect.setWriteable();
									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											Toast.makeText(MainActivity.this,
													"连接成功!", 1000).show();

										}
									});
									while (isRunning) {
										String msg = "连接成功，开始发送数据!    "
												+ devName + "\n";
										tcpSocketConnect.write(msg.getBytes());

										try {
											Thread.sleep(30);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							});
					String address = inetAddress.getHostAddress();
					Log.e(Tag, "Address:" + address);
					tcpSocketConnect.setAddress(address, 60034);
					new Thread(tcpSocketConnect).start();
				}
			}

		}
	}

	private Connection.ConnectionListener mConnectionListener = new Connection.ConnectionListener() {
		@Override
		public void onConnection() {
			mHandler.postDelayed(msgRunnable, 50);
			Log.e(Tag, "Connect success");
		}
	};

	private void startSendMsg(Map<String, String> msg) {

		getConnectionWrapper().send(msg);
	}

	Runnable msgRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			// startSendMsg();
			mHandler.postDelayed(msgRunnable, 50);
		}
	};

	private Handler mClientHandler = new MessageHandler(MainActivity.this) {
		@Override
		public void onMessage(String type, JSONObject message) {
			if (type.equals(Communication.ConnectSuccess.TYPE)) {
				Toast.makeText(getApplicationContext(),
						"Connection succesfully performed!", Toast.LENGTH_SHORT)
						.show();
			}
		}
	};

	private ConnectionWrapper getConnectionWrapper() {
		return ((JmdnsAPP) getApplication()).getConnectionWrapper();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		JmdnsAPP.mJmdns.setHandler(mHandler);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		JmdnsAPP.mJmdns.clear();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		isRunning = false;
		if (udpSocket != null)
			udpSocket.stopSocket();
		if (tcpSocketConnect != null)
			tcpSocketConnect.resetConnect();
		JmdnsAPP.mJmdns.exit();
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}

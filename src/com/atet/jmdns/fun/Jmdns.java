package com.atet.jmdns.fun;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import com.atet.jmdns.com.LOG;
import com.atet.jmdns.com.Param;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Jmdns implements ServiceListener, ServiceTypeListener {
	private String searchType = "";
	private JmDNS jmdns = null;
	private LOG log = null;
	private WifiManager wifiManager = null;
	private String defaultType = "_teleplus._tcp.local.";
	/**
	 * Allows an application to receive Wifi Multicast packets.
	 */
	private static MulticastLock multicastLock = null;
	private static final String HOSTNAME = "melloware";
	private Handler mHandler = null;

	public Jmdns(final Context context) {
		// TODO Auto-generated constructor stub
		log = new LOG();
		new Thread() {
			public void run() {
				wifiManager = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				InetAddress inetAddress = getDeviceIpAddress(wifiManager);
				multicastLock = wifiManager.createMulticastLock(getClass()
						.getName());
				multicastLock.setReferenceCounted(true);
				multicastLock.acquire();

				Create(inetAddress);
				log.e("jmdns start");
			};
		}.start();

	}

	public void setHandler(Handler mHandler) {
		// TODO Auto-generated method stub
		this.mHandler = mHandler;
	}

	public void clear() {
		mHandler = null;
	}

	private void sendMessage(int action, Object obj) {
		if (mHandler != null) {
			Message msg = Message.obtain();
			msg.arg1 = action;
			msg.obj = obj;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * @description: 
	 *
	 * @param service
	 * @return 
	 * @throws: 
	 * @author: LiuQin
	 * @date: 2015年5月7日 下午12:06:05
	 */
	public ServiceInfo getServiceInfo(ServiceInfo service) {
		ServiceInfo resolvedService = jmdns.getServiceInfo(service.getType(),
				service.getName());

		if (resolvedService == null) {
			return null;
		}
		return resolvedService;
	}

	/**
	 * ��ȡ����IP��ַ
	 * 
	 * @param wifi
	 * @return
	 */
	private InetAddress getDeviceIpAddress(WifiManager wifi) {
		InetAddress result = null;
		try {
			// default to Android localhost
			result = InetAddress.getByName("10.0.0.2");
			// figure out our wifi address, otherwise bail
			WifiInfo wifiinfo = wifi.getConnectionInfo();
			int intaddr = wifiinfo.getIpAddress();
			byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff),
					(byte) (intaddr >> 8 & 0xff),
					(byte) (intaddr >> 16 & 0xff),
					(byte) (intaddr >> 24 & 0xff) };
			result = InetAddress.getByAddress(byteaddr);
		} catch (UnknownHostException ex) {
			log.w(String.format("getDeviceIpAddress Error: %s", ex.getMessage()));
		}

		return result;
	}

	/**
	 * ����Jmdns����
	 */
	public void Create(final InetAddress address) {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					jmdns = JmDNS.create(address, HOSTNAME);
					jmdns.addServiceTypeListener(Jmdns.this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void serviceResolved(ServiceInfo serviceInfo) {
		String[] hostAddresses = serviceInfo.getHostAddresses();
		if (hostAddresses.length > 0) {
			log.e(hostAddresses[0]);
		} else {
			log.e("no host address");
		}

	}

	/**
	 * 
	 */
	public void startDiscover(String serviceType) {

	}

	@Override
	public void serviceTypeAdded(ServiceEvent arg0) {
		// TODO Auto-generated method stub
		String type = arg0.getType();
		log.e("jmdns type:" + arg0.getType());
		if (defaultType.equals(type)) {
			jmdns.addServiceListener(arg0.getType(), Jmdns.this);
			
		}
	}

	@Override
	public void subTypeForServiceTypeAdded(ServiceEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void serviceAdded(ServiceEvent arg0) {
		// TODO Auto-generated method stub

		ServiceInfo serviceInfo = arg0.getInfo();
		String name = serviceInfo.getName();
		sendMessage(Param.GET_A_DEVICE, serviceInfo);
		jmdns.requestServiceInfo(arg0.getType(), arg0.getName(), 1);
		if (name != null) {
			log.e(name);
			log.e("地址为:" + serviceInfo.getHostAddress());
		}
	}

	public void connect(ServiceInfo serviceInfo) {
		String hostAddress = serviceInfo.getHostAddress();
		log.e(hostAddress);

	}

	@Override
	public void serviceRemoved(ServiceEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void serviceResolved(ServiceEvent arg0) {
		// TODO Auto-generated method stub
		log.e("serviceResolved");
	}

	public void exit() {
		if (jmdns != null) {
			try {
				jmdns.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

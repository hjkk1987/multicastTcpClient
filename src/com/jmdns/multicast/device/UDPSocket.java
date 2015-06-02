package com.jmdns.multicast.device;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

/*
 * File：UDPSocket.java
 *
 * Copyright (C) 2015 JmdnsClient Project
 * Date：2015年6月1日 上午9:34:38
 * All Rights SXHL(New Space) Corporation Reserved.
 * http://www.at-et.cn
 *
 */

/**
 * @description:
 * 
 * @author: HuJun
 * @date: 2015年6月1日 上午9:34:38
 */

public class UDPSocket implements Runnable {
	private Context context = null;
	private static int udpPort = 0;
	private DatagramSocket socket = null;
	private InetAddress inetAddress = null;
	private boolean isRunning = false;
	private Thread socketThread = null;
	private String Tag = "Udpsocket";
	private String devName = "";

	/**
	 * Constructors： UDPSocket.
	 * 
	 * @param context
	 * @param inetAddress
	 *            服务端对应的IP地址
	 */
	public UDPSocket(Context context, InetAddress inetAddress) {
		this.context = context;
		this.inetAddress = inetAddress;
		initSocket();
		devName = android.os.Build.MODEL;
	}

	/**
	 * @description: 初始化Socket
	 * 
	 * @throws:
	 * @author: HuJun
	 * @date: 2015年6月1日 上午9:36:32
	 */
	private void initSocket() {
		udpPort = (int) (Math.random() * 55535 + 10000);// 随机一个本地的socket
		try {
			socket = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// new 一个本地的socket
	}

	/**
	 * @description:
	 * 
	 * @return
	 * @throws:
	 * @author: HuJun
	 * @date: 2015年6月1日 下午3:20:34
	 */
	public boolean isAlive() {
		if (socketThread == null) {
			return false;
		}

		return socketThread.isAlive();
	}

	/**
	 * @description:开始发送数据
	 * 
	 * @throws:
	 * @author: HuJun
	 * @date: 2015年6月1日 下午1:02:29
	 */
	public void startSocket() {
		if (socketThread == null || !socketThread.isAlive()) {
			isRunning = true;
			socketThread = new Thread(UDPSocket.this);
			socketThread.start();
		}
	}

	/**
	 * @description:
	 * 
	 * @throws:
	 * @author: HuJun
	 * @date: 2015年6月1日 下午1:04:38
	 */
	public void stopSocket() {
		isRunning = false;
	}

	public void sendMsg(byte[] data) {
		if (socket == null)
			return;
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length,
					inetAddress, 60034);
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (isRunning) {
			String time = devName
					+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date());
			byte[] buf = time.getBytes();
			Log.e(Tag, "发送数据:" + time);
			sendMsg(buf);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

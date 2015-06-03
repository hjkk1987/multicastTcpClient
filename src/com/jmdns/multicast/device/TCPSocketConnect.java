package com.jmdns.multicast.device;

import java.io.IOException;
import java.util.Vector;

import android.util.Log;

/**
 * 连接服务器线程类
 * 
 * @author Esa
 */
public class TCPSocketConnect implements Runnable {

	private static final String TAG = "TCPSocketConnect";
	private boolean isConnect = false;// 是否连接服务器
	private boolean isWrite = false;// 是否发送数据
	private static Vector<byte[]> datas = new Vector<byte[]>();// 待发送数据队列
	private Object lock = new Object();// 连接锁对象
	private TCPSocketFactory mSocket;// socket连接
	private WriteRunnable writeRunnable;// 发送数据线程
	private String ip = null;
	private int port = -1;
	private String Tag = TCPSocketConnect.class.getName();

	/**
	 * 创建连接
	 * 
	 * @param callback
	 *            回调接口
	 * @param executor
	 *            线程池对象
	 */

	public TCPSocketConnect(TCPSocketCallback callback) {
		mSocket = new TCPSocketFactory(callback);// 创建socket连接
		writeRunnable = new WriteRunnable();// 创建发送线程
	}

	public void Connect() {

		if (ip == null || port == -1) {
			return;
		}
		isConnect = true;
		while (isConnect) {
			synchronized (lock) {
				try {
					Log.e(TAG, ">TCP连接服务器<");
					mSocket.connect(ip, port);// 连接服务器
					Log.e(TAG, ">TCP连接服务器成功11111<");
				} catch (Exception e) {
					try {
						Log.e(TAG, ">TCP连接服务器失败<");
						// resetConnect();// 断开连接
						// Connect();
						lock.wait(2000);
						continue;
					} catch (InterruptedException e1) {
						Log.e(TAG, ">TCP连接服务器失败，重新连接1111<");
						continue;
					}
				}
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run() {
		Log.e(TAG, ">=TCP结束连接线程=<");
		Connect();
	}

	/**
	 * @description:
	 * 
	 * @throws:
	 * @author: HuJun
	 * @date: 2015年6月2日 上午11:54:05
	 */
	public void setWriteable() {
		isConnect = false;
		isWrite = true;// 设置可发送数据
		new Thread(writeRunnable).start();// 启动发送线程
	}

	/**
	 * 关闭服务器连接
	 */
	public void disconnect() {
		synchronized (lock) {
			isConnect = false;
			lock.notify();
			resetConnect();
		}
	}

	/**
	 * 重置连接
	 */
	public void resetConnect() {
		// Log.e(TAG, ">TCP重置连接<");
		writeRunnable.stop();// 发送停止信息
		mSocket.disconnect();
	}

	/**
	 * 向发送线程写入发送数据
	 */
	public void write(byte[] buffer) {
		if (writeRunnable != null) {

			writeRunnable.write(buffer);
		}
	}

	public int read(byte[] bytes, int readSize) {
		int ret = -1;
		try {
			ret = mSocket.read(bytes, readSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// Log.e(TAG, ">TCP连接中断<");
			resetConnect();// 断开连接
		}
		return ret;
	}

	/**
	 * 设置IP和端口
	 * 
	 * @param ip
	 * @param port
	 */
	public void setAddress(String host, int port) {
		this.ip = host;
		this.port = port;
	}

	/**
	 * 发送数据
	 */
	private boolean writes(byte[] buffer) {
		try {
			mSocket.write(buffer);
			Thread.sleep(1); // 1ms
			return true;
		} catch (Exception e) {
			Log.e("writes", "socket writes error.  check wifi " + isConnect());
			// resetConnect();

			return false;
		}
	}

	private boolean isConnect() {
		boolean success = false;
		Process p = null;

		try {
			p = Runtime.getRuntime().exec("ping -c 1 -i 0.2 -W 1 " + ip);
			int status = p.waitFor();
			if (status == 0) {
				success = true;
			} else {
				success = false;
			}
		} catch (IOException e) {
			success = false;
		} catch (InterruptedException e) {
			success = false;
		} finally {
			p.destroy();
		}

		return success;
	}

	/**
	 * 发送线程
	 * 
	 * @author Esa
	 */
	private class WriteRunnable implements Runnable {

		private Object wlock = new Object();// 发送线程锁对象

		@Override
		public void run() {

			isWrite = true;// 设置可发送数据
			while (isWrite) {
				synchronized (wlock) {
					if (datas.size() <= 0) {
						try {
							wlock.wait();// 等待发送数据
						} catch (InterruptedException e) {
							continue;
						}
					}
					while (datas.size() > 0) {
						byte[] buffer = datas.remove(0);// 获取一条发送数据
						if (isWrite) {
							Log.e(TAG, ">发送数据<");
							writes(buffer);// 发送数据
						} else {
							wlock.notify();
						}
					}
				}
			}
			// Log.e(TAG, ">TCP发送线程结束<");
		}

		/**
		 * 添加数据到发送队列
		 * 
		 * @param buffer
		 *            数据字节
		 */
		public void write(byte[] buffer) {
			synchronized (wlock) {
				datas.add(buffer);// 将发送数据添加到发送队列
				wlock.notify();// 取消等待
			}
		}

		public void stop() {
			synchronized (wlock) {
				isWrite = false;
				wlock.notify();
			}
		}
	}
}

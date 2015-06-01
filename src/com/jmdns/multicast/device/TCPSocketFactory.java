package com.jmdns.multicast.device;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.util.Log;

public class TCPSocketFactory {
	private Socket mSocket; // socket连接对象
	private DataOutputStream outStream; // 输出流
	private DataInputStream inStream; // 输入流
	private byte[] buffer = new byte[1024]; // 缓冲区字节数组，信息不能大于此缓冲区
	private byte[] tmpBuffer; // 临时缓冲区
	private TCPSocketCallback callback; // 信息回调接口
	private int timeOut = 2000;
	private String Tag = "TcpSocketFactory";

	/**
	 * 构造方法传入信息回调接口对象
	 * 
	 * @param sdi
	 *            回调接口
	 */
	public TCPSocketFactory(TCPSocketCallback callback) {
		this.callback = callback;
	}

	public void connect(String ip, int port) throws Exception {
		mSocket = new Socket();
		SocketAddress mAddress = new InetSocketAddress(ip, port);
		mSocket.connect(mAddress, timeOut);

		if (isConnected()) {
			outStream = new DataOutputStream(mSocket.getOutputStream());
			inStream = new DataInputStream(mSocket.getInputStream());
			if (isConnected()) {
				Log.e(Tag, "Connect");
				callback.tcp_connected();
			}
		}
	}

	public void setTimeOut(int timeout) {
		this.timeOut = timeout;
	}

	/**
	 * 返回Socket是否连接成功
	 * */
	public boolean isConnected() {
		if (mSocket != null && mSocket.isConnected())
			return true;
		else
			return false;
	}

	/**
	 * 往socket中写数据
	 * 
	 * @param bytes
	 * @throws Exception
	 */
	public void write(byte[] bytes) throws IOException {
		if (outStream != null) {
			Log.e("TcpSocketFactory", "发送数据");
			outStream.write(bytes);
			outStream.flush();
		}
	}

	/**
	 * 从socket中读取数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public int read(byte[] bytes, int readSize) throws IOException {
		int recvSize = -1;
		if (inStream != null) {
			recvSize = inStream.read(buffer, 0, readSize);
			if (recvSize > 0) {
				// bytes = buffer;
				tmpBuffer = new byte[recvSize];
				System.arraycopy(buffer, 0, tmpBuffer, 0, recvSize);
				System.arraycopy(buffer, 0, bytes, 0, recvSize);
				callback.tcp_receive(tmpBuffer);// 调用回调接口传入得到的数据
				tmpBuffer = null;
			}
		} else {
			return -1;
		}
		return recvSize;
	}

	/**
	 * 取消socket连接
	 */
	public void disconnect() {
		try {
			if (mSocket != null && mSocket.isConnected()) {
				if (!mSocket.isInputShutdown())
					mSocket.shutdownInput();
				if (!mSocket.isOutputShutdown())
					mSocket.shutdownOutput();
			}
			if (outStream != null)
				outStream.close();
			if (inStream != null)
				inStream.close();
			if (mSocket != null && !mSocket.isClosed())
				mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			callback.tcp_disconnect();
			outStream = null;
			inStream = null;
			mSocket = null;// 置空socket对象
		}
	}

}

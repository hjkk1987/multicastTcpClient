package com.atet.jmdns.app;

import com.atet.jmdns.connect.ConnectionWrapper;
import com.atet.jmdns.fun.Jmdns;
import com.jmdns.multicast.device.MultiSocket;

//
import android.app.Application;
import android.util.Log;

public class JmdnsAPP extends Application {
	public static Jmdns mJmdns = null;
	private ConnectionWrapper mConnectionWrapper = null;
	public static MultiSocket multiSocket = null;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("Applications", "start!");
		multiSocket = new MultiSocket(getApplicationContext());
		new Thread() {
			public void run() {
				mJmdns = new Jmdns(getApplicationContext());
				mJmdns.startDiscover("haha");
			};
		}.start();
	}

	public void createConnectionWrapper(
			ConnectionWrapper.OnCreatedListener listener) {
		mConnectionWrapper = new ConnectionWrapper(getApplicationContext(),
				listener);
	}

	public ConnectionWrapper getConnectionWrapper() {
		return mConnectionWrapper;
	}

}

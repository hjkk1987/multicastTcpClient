package com.atet.jmdns.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/*
 * File：Jmdns.java
 *
 * Copyright (C) 2015 JmdnsClient Project
 * Date：2015年5月12日 下午4:54:39
 * All Rights SXHL(New Space) Corporation Reserved.
 * http://www.at-et.cn
 *
 */

/**
 * @description:
 * 
 * @author: HuJun
 * @date: 2015年5月12日 下午4:54:39
 */

public class JmdnsService extends Service {

	private ServiceBinder mBinder = new ServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class ServiceBinder extends Binder {
		public JmdnsService getService() {
			return JmdnsService.this;
		}
	}

}

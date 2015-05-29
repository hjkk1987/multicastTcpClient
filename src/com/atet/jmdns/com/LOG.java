package com.atet.jmdns.com;

import android.util.Log;

public class LOG {
	private String Tag = "Log";

	public void e(String message) {
		Log.e(Tag, message);
	}

	public void i(String message) {
		Log.i(Tag, message);
	}

	public void d(String message) {
		Log.d(Tag, message);
	}

	public void w(String message) {
		Log.w(Tag, message);
	}
}

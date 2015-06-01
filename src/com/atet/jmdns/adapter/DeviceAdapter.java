package com.atet.jmdns.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import com.atet.jmdns.activity.R;
import com.atet.jmdns.app.JmdnsAPP;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.GridView;
import android.widget.TextView;

public class DeviceAdapter extends BaseAdapter {
	private List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
	private ViewHolder viewHolder;
	private LayoutInflater layoutInflater = null;
	private int screenHeight = 0;

	public DeviceAdapter(Context context, List<ServiceInfo> serviceInfos) {
		layoutInflater = LayoutInflater.from(context);
		this.serviceInfos = serviceInfos;
		DisplayMetrics dmMetrics = context.getResources().getDisplayMetrics();
		screenHeight = dmMetrics.heightPixels;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return serviceInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return serviceInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (arg1 == null) {
			arg1 = layoutInflater.inflate(R.layout.servicedevice, null);
			viewHolder = new ViewHolder();
			viewHolder.tv_name = (TextView) arg1
					.findViewById(R.id.tv_device_name);
			arg1.setLayoutParams(new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, 80 * screenHeight / 960));
			arg1.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}
		ServiceInfo serviceInfo = serviceInfos.get(arg0);
		ServiceInfo serviceInfo2 = JmdnsAPP.mJmdns.getServiceInfo(serviceInfo);
		viewHolder.tv_name.setText(serviceInfo2.getName() + "    "
				+ serviceInfo2.getInetAddress().getHostAddress());

		return arg1;
	}

	private class ViewHolder {
		public TextView tv_name = null;
	}

}

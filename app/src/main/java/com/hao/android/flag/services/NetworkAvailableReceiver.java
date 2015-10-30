package com.hao.android.flag.services;

import java.util.Enumeration;

import com.hao.android.flag.AppTools;
import com.hao.android.flag.MyApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkAvailableReceiver extends BroadcastReceiver {
	private String lastNetworkName;
	public static final String ACTION_AVAILABLE = NetworkAvailableReceiver.class.getSimpleName()+"_ACTION_AVAILABLE";
	private MyApp myApp;
	public NetworkAvailableReceiver() {
		AppTools.tableUnfinishedServices.put(MyAdsService.class.getName(), MyAdsService.class);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
        	if(myApp == null) myApp = (MyApp)context.getApplicationContext();
            String currentNetworkName = myApp.getConnectivityNetworkName();
            if(currentNetworkName != null && !currentNetworkName.equals(lastNetworkName)) {
               	Enumeration<String> enumKeys = AppTools.tableUnfinishedServices.keys();
               	while(enumKeys.hasMoreElements()) {
               		String key = enumKeys.nextElement();
               		Class tempClass = AppTools.tableUnfinishedServices.remove(key);
               		context.startService(new Intent(context, tempClass));
               	}
            }
            lastNetworkName = currentNetworkName;
        }
	}
}

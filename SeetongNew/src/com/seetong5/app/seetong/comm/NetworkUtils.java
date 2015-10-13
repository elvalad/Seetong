package com.seetong5.app.seetong.comm;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * @declaration 网络检测工具类
 * @author nilbounds@gmail.com
 *2012-10-14 下午10:13:06
 */
public class NetworkUtils {
	public final static int NONE = 0;// 无网络
	public final static int WIFI = 1;// Wi-Fi
	public final static int MOBILE = 2;// 3G,GPRS
	public final static int ETHERNET = 3;// ethernet

	/**
	 * 
	 * 获取当前网络状态
	 * 
	 * @param context
	 * @return
	 */
	public static int getNetworkState(Context context) {
		boolean isOk = isNetworkAvailable(context);
		if(isOk){//有可用网络
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			if (connManager == null){
				return NONE;
			}
			
			// Wifi网络判断
			NetworkInfo networkInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return WIFI;
				}
			}

			// 手机网络判断
			networkInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return MOBILE;
				}
			}

			// 以太网连接判断
			networkInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return MOBILE;
				}
			}
		}
		return NONE;
	}
	
	/**
	 * 是否有可用网络<br>
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager == null){
			return false;
		}
		
		NetworkInfo networkInfo= connManager.getActiveNetworkInfo();
		if(networkInfo == null){
			return false;
		}
		boolean isOK = networkInfo.isAvailable();
		return isOK;
	}
	
	/**
	 * 跳转到网络设置界面(wifi or 3g)
	 * @param context
	 */
	public void gotoNetworkSetUI(Context context, int types){
		String action = android.provider.Settings.ACTION_WIFI_SETTINGS;
		if(types == MOBILE){
			 // 跳转到无线网络设置界面  
			action = android.provider.Settings.ACTION_WIRELESS_SETTINGS;
		}else if(types == WIFI){
			  // 跳转到无限wifi网络设置界面  
			action = android.provider.Settings.ACTION_WIFI_SETTINGS;
		}
		context.startActivity(new Intent(action));
	}
}


package com.android.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import com.umeng.analytics.MobclickAgent;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
				MobclickAgent.reportError(context, "connManager is null.");
				return NONE;
			}
			
			// Wifi网络判断
			NetworkInfo networkInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return WIFI;
				}
			}else {
				MobclickAgent.reportError(context, "wifi networkInfo is null.");
			}
			
			// 手机网络判断
			networkInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return MOBILE;
				}
			}else {
				MobclickAgent.reportError(context, "3G networkInfo is null.");
			}
			
			// 以太网连接判断
			networkInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return MOBILE;
				}
			}else {
				MobclickAgent.reportError(context, "ethernet networkInfo is null.");
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
		boolean isOK = false;
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connManager == null){
			MobclickAgent.reportError(context, "connManager is null.");
			return isOK;
		}
		
		NetworkInfo networkInfo= connManager.getActiveNetworkInfo();
		if(networkInfo == null){
			return isOK;
		}
		isOK = networkInfo.isAvailable();
		return isOK;
	}

	/**
	 * 判断是否可以访问互联网
	 */
	public static boolean isConnectInternet() {
		String myString = "";
		try {
			URL url = new URL("HTTP://www.baidu.com/index.html");
			URLConnection urlCon = url.openConnection();
			urlCon.setConnectTimeout(1500);
			InputStream is = urlCon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			// 用ByteArrayBuffer缓存
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			myString = EncodingUtils.getString(baf.toByteArray(), "UTF-8");
			bis.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (myString.indexOf("www.baidu.com") > -1) {
			return true;
		} else {
			return false;
		}
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


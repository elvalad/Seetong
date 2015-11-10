package com.seetong5.app.seetong.comm;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

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
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			if (cm == null){
				return NONE;
			}
			
			// Wifi网络判断
			NetworkInfo networkInfo= cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return WIFI;
				}
			}

			// 手机网络判断
			networkInfo= cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return MOBILE;
				}
			}

			// 以太网连接判断
			networkInfo= cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			if (networkInfo != null){
				State state = networkInfo.getState();
				if (state == State.CONNECTED || state == State.CONNECTING) {
					return MOBILE;
				}
			}
		}
		return NONE;
	}

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null == info) return false;
        return info.isConnected();
    }

    public static boolean isEthernet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (null == info) return false;
        return info.isConnected();
    }

    public static boolean isMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return false;
        return info.isConnected();
    }

    public static boolean is2G(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return false;

        switch (info.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return true;
        }

        return false;
    }

    public static boolean is3G(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return false;

        switch (info.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true;
        }

        return false;
    }

    public static boolean is4G(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return false;

        switch (info.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
        }

        return false;
    }

    public static boolean isCMCC(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return false;

        switch (info.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
        }

        return false;
    }

    public static int getNetSubType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return TelephonyManager.NETWORK_TYPE_UNKNOWN;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return TelephonyManager.NETWORK_TYPE_UNKNOWN;

        return info.getSubtype();
    }

    public String getNativePhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null == tm) return "";
        return tm.getLine1Number();
    }

    public static String getProvidersName(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "";

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == info) return "";

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null == tm) return "";

        String ProvidersName = "";
        // 返回唯一的用户ID;就是这张卡的编号神马的
        String imsi = tm.getSubscriberId();
        if (TextUtils.isEmpty(imsi)) return "";

        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
            ProvidersName = "YD";
        } else if (imsi.startsWith("46001")) {
            ProvidersName = "LT";
        } else if (imsi.startsWith("46003") || imsi.startsWith("46011") || imsi.startsWith("20404") || imsi.startsWith("45404")) {
            ProvidersName = "DX";
        } else {
            ProvidersName = "UN";
        }

        return ProvidersName;
    }
	
	/**
	 * 是否有可用网络<br>
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null){
			return false;
		}
		
		NetworkInfo networkInfo= cm.getActiveNetworkInfo();
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


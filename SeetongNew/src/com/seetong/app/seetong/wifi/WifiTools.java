package com.seetong.app.seetong.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.util.List;

/**
 * @declaration Wifi配置<br>
 * @author qinglei.yin@192.168.88.9<br>
 *2014-3-12 下午5:17:12<br>
 */
public class WifiTools {
	WifiReceiver mWifiReceiver;
	public WifiTools(WifiReceiver wifiReceiver) {
		mWifiReceiver = wifiReceiver;
	}
	
	public static final int  WIFI_AUTH_NONE = 0;
	public static final int  WIFI_AUTH_WEP_SHARED =1;
	public static final int  WIFI_AUTH_WEP_NONE =2;
	public static final int  WIFI_AUTH_WPA_PSK_TKIP =3;
	public static final int  WIFI_AUTH_WPA_PSK_AES =4;
	public static final int  WIFI_AUTH_WPA2_PSK_TKIP =5;
	public static final int  WIFI_AUTH_WPA2_PSK_AES =6;
	public static final String WIFI_URL_GET = "http://192.168.169.1/wireless_sta?";
	
	/**
	 * 测试链接：http://192.168.169.1/wireless_sta?SSID=TP_LINK&SecurityMode=&passwd=888888<br>
	 */
	public void tellWifi(String ssid, String pwd){
		AjaxParams params = new AjaxParams();
		params.put("SSID", ssid);
		params.put("SecurityMode", WIFI_AUTH_WPA2_PSK_AES+"");
		params.put("passwd", pwd);
		
		FinalHttp fh = new FinalHttp();
		fh.get(WIFI_URL_GET, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				Log.e("MSG", (t == null)? "null":"onSuccess..."+t.toString());
			}
			
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Log.e("MSG", "onFailure...errorNO="+errorNo+",strMsg="+strMsg);
			}
			
			@Override
			public void onStart() {
				Log.e("MSG", "onStart...");
			}
			
			@Override
			public void onLoading(long count, long current) {
				Log.e("MSG", "onLoading...count="+count+",current="+current);
			}			
		});	
	}
	
    static WifiManager mWifiManager;
    static AccessPoint mAccessPoint;
    static int mCurNetid;
    static List<ScanResult> mScanResults;
    static ScanResult mCurScanResult;
    
    public void wifiConnect(Context ctx){
        mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);    //开启WIFI
        }
//        deleteSavedConfigs(mWifiManager);
        
        mCurNetid = -1;
        mWifiManager.startScan(); //开始扫描网络 
    }
    
    public void deleteSavedConfigs(WifiManager wm){
    	if(wm != null){
	        List<WifiConfiguration> configs = wm.getConfiguredNetworks();
	        int size = (configs == null)? 0 : configs.size();
	        for (int i = 0; i < size; i++) {
	        	WifiConfiguration config = configs.get(i);
	            config.priority = i + 2;    //将优先级排后
	            wm.removeNetwork(config.networkId); 
	        }
	        wm.saveConfiguration();
    	}
    }
    
    public void connect(AccessPoint ap, String ssid, String pwd){
    	if(ap != null){
    		mAccessPoint = ap;
	    	mAccessPoint.mConfig.priority = 1;
	        mAccessPoint.mConfig.status = WifiConfiguration.Status.ENABLED;
	        mAccessPoint.mConfig.SSID = ssid;
	        mAccessPoint.mConfig.preSharedKey = pwd;     //设置密码
	        
	        mWifiManager.updateNetwork(mAccessPoint.mConfig);
	        if(mWifiManager.enableNetwork(mCurNetid, false))
	        	Log.i("MSG","启用网络失败");
	        mWifiManager.saveConfiguration();
	        mWifiManager.reconnect(); //连接AP
    	}
    }
    
    public void registerReceiver(){
    	if(mWifiReceiver != null){
    		mWifiReceiver.registerReceiver();
    	}
    }
    public void unRegisterReceiver(){
    	if(mWifiReceiver != null){
    		mWifiReceiver.unRegisterReceiver();
    	}
    }
    
    public static interface IWifiRecv{
    	public void recvBC(WifiManager wifiManager, Intent intent);
    }
    public static class WifiReceiver extends BroadcastReceiver {
    	IWifiRecv mIWifiRecv;
    	WifiManager mWifiManager2;
    	Context mCtx;
        boolean m_registered = false;
    	public WifiReceiver(Context ctx, IWifiRecv wifiRecv, WifiManager wifiManager2) {
			mCtx = ctx;
			mIWifiRecv = wifiRecv;
			mWifiManager2 = wifiManager2;
		}
    	public void registerReceiver(){
    		if(mCtx != null){
                if (m_registered) return;
    			IntentFilter itf = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    			itf.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                itf.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    			mCtx.registerReceiver(this, itf);
                m_registered = true;
    		}
    	}
    	public void unRegisterReceiver(){
    		if(mCtx != null){
                if (!m_registered) return;
                mCtx.unregisterReceiver(this);
                m_registered = false;
    		}
    	}
    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(mIWifiRecv != null){
        		mIWifiRecv.recvBC(mWifiManager2, intent);
        	}
        }           
    } 
}

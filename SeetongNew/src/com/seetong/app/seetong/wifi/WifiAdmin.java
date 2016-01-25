package com.seetong.app.seetong.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import java.util.List;

/**
 * @author qinglei.yin@192.168.88.9<br>
 *         2014-3-19 下午4:18:25<br>
 * @declaration Wifi工具类<br>
 * http://blog.csdn.net/yuanbohx/article/details/8109042<br>
 */
@SuppressLint("UseValueOf")
public class WifiAdmin {
    // 定义WifiManager对象  
    private WifiManager mWifiManager;
    // 定义WifiInfo对象  
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表  
    private List<ScanResult> mWifiList;
    // 网络连接列表  
    private List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock  
    WifiLock mWifiLock;

    // 构造器  
    public WifiAdmin(Context context) {
        // 取得WifiManager对象  
        setWifiManager((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        // 取得WifiInfo对象  
        mWifiInfo = getWifiManager().getConnectionInfo();
    }

    public WifiInfo getConnectionInfo() {
        mWifiInfo = getWifiManager().getConnectionInfo();
        return mWifiInfo;
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    public void setWifiManager(WifiManager mWifiManager) {
        this.mWifiManager = mWifiManager;
    }

    // 打开WIFI
    public void openWifi() {
        if (!getWifiManager().isWifiEnabled()) {
            boolean isOK = getWifiManager().setWifiEnabled(true);
            Log.d("MSG", "Wifi open state=" + isOK + ".");
        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (getWifiManager().isWifiEnabled()) {
            boolean isOK = getWifiManager().setWifiEnabled(false);
            Log.d("MSG", "Wifi close state=" + isOK + ".");
        }
    }

    // 检查当前WIFI状态  
    public int checkState() {
        return getWifiManager().getWifiState();
    }

    // 锁定WifiLock  
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock  
    public void releaseWifiLock() {
        // 判断时候锁定  
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock  
    public void creatWifiLock() {
        mWifiLock = getWifiManager().createWifiLock("Test");
    }

    // 得到配置好的网络  
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接  
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回  
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络  
        getWifiManager().enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    public void connectConfiguration(AccessPoint ap, String ssid, String pwd) {
        if (ap != null) {
            addNetwork(createWifiInfo(ssid, pwd, ap.security));
        }
    }

    public void connectConfiguration(final String ssid, final String pwd, final int encrypType) {
        addNetwork(createWifiInfo(ssid, pwd, encrypType));
    }

    public void startScan() {
        getWifiManager().startScan();
        // 得到扫描结果  
        mWifiList = getWifiManager().getScanResults();
        // 得到配置好的网络连接  
        mWifiConfiguration = getWifiManager().getConfiguredNetworks();
    }

    // 得到网络列表  
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    // 查看扫描结果  
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包  
            // 其中把包括：BSSID、SSID、capabilities、frequency、level  
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    // 得到MAC地址  
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID  
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 得到IP地址  
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID  
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 得到WifiInfo的所有信息包  
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 添加一个网络并连接  
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = getWifiManager().addNetwork(wcg);
        boolean b = getWifiManager().enableNetwork(wcgID, true);
        System.out.println("a--" + wcgID);
        System.out.println("b--" + b);
    }

    public boolean enableNetwork(int netId, boolean disableOthers) {
        return getWifiManager().enableNetwork(netId, disableOthers);
    }

    // 断开指定ID的网络  
    public void disconnectWifi(int netId) {
        getWifiManager().disableNetwork(netId);
        getWifiManager().disconnect();
    }

    //然后是一个实际应用方法，只验证过没有密码的情况： 
    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = IsExsits(SSID);
        if (tempConfig != null) {
            getWifiManager().removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFICIPHER_NOPASS
        {
            // 下面两行注释掉，否则连接会失败
            config.hiddenSSID = true;
            //config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = getWifiManager().getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (!existingConfig.SSID.equals("\"" + SSID + "\"")) continue;
            return existingConfig;
        }

        return null;
    }

}

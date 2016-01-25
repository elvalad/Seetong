package com.seetong.app.seetong.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.utils.NetworkUtils;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.ui.aid.WifiListAdapter;
import com.seetong.app.seetong.wifi.AccessPoint;
import com.seetong.app.seetong.wifi.WifiAdmin;
import com.seetong.app.seetong.wifi.WifiTools;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WifiEtcUI extends BaseActivity implements View.OnClickListener {
    TextView mtvDeviceWifi;
    TextView mtvWifiInfo;
    TextView mtvEtcFinish;

    LinearLayout mDeviceWifiInfo;
    LinearLayout mMyWifiInfo;
    LinearLayout mResult;

    ListView mlvDevicewifiList;
    ListView mlvMyWifiList;

    Button mbtnPreTo1;
    Button mbtnNextTo3;
    Button mbtnMoreWifi;
    EditText metWifiName;
    EditText metWifiPwd;
    Spinner mspEncrypedTypes;

    Button mbtnEtcFinish;
    Button mbtnbtnReEtc;

    WifiAdmin mWifiAdmin;
    List<ScanResult> mScanResults;
    WifiListAdapter mWifiListAdapter;
    private ProgressDialog mTipDlg;
    WifiTools.WifiReceiver mWifiReceiver;
    String mCurConnectedSSID;
    WifiListAdapter.WifiFlagInfo mCurWifiInfo;
    WifiInfo mConnectedWifiInfo;
    WifiRecvImpl mWifiRecvImpl;

    boolean m_success = false;
    boolean m_connect_device = false;
    boolean m_scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_etc_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(R.string.form_wifi_tv_etc_wifi_title);
        initWidget();
    }

    protected void initWidget() {
        mtvDeviceWifi = (TextView) findViewById(R.id.tvDeviceWifi);
        mtvWifiInfo = (TextView) findViewById(R.id.tvWifiInfo);
        mtvEtcFinish = (TextView) findViewById(R.id.tvEtcFinish);

        mDeviceWifiInfo = (LinearLayout) findViewById(R.id.llDeviceWifiInfo);
        mMyWifiInfo = (LinearLayout) findViewById(R.id.llMyWifiInfo);
        mResult = (LinearLayout) findViewById(R.id.llResult);

        mlvDevicewifiList = (ListView) findViewById(R.id.lvDeviceWifiList);
        mlvMyWifiList = (ListView) findViewById(R.id.lvMyWifiList);

        mbtnPreTo1 = (Button) findViewById(R.id.btnPreStepTo1);
        mbtnPreTo1.setOnClickListener(this);
        mbtnNextTo3 = (Button) findViewById(R.id.btnNextStepTo3);
        mbtnNextTo3.setOnClickListener(this);
        mbtnMoreWifi = (Button) findViewById(R.id.btnMoreWifi);
        mbtnMoreWifi.setOnClickListener(this);
        metWifiName = (EditText) findViewById(R.id.etWifiName);
        metWifiPwd = (EditText) findViewById(R.id.etWifiPwd);
        mspEncrypedTypes = (Spinner) findViewById(R.id.spEncryptedTypes);

        mbtnEtcFinish = (Button) findViewById(R.id.btnEtcFinish);
        mbtnEtcFinish.setOnClickListener(this);
        mbtnbtnReEtc = (Button) findViewById(R.id.btnReEtc);
        mbtnbtnReEtc.setOnClickListener(this);

        initUIByStep(mCurStep);
        mWifiAdmin = new WifiAdmin(this);
        mWifiRecvImpl = new WifiRecvImpl();
        mWifiReceiver = new WifiTools.WifiReceiver(this, mWifiRecvImpl, mWifiAdmin.getWifiManager());

        startScan();
    }

    public List<ScanResult> filterWifi(List<ScanResult> scanResults) {
        List<ScanResult> result = new ArrayList<ScanResult>();
        if (scanResults != null && scanResults.size() > 0) {
            for (ScanResult tmp : scanResults) {
                if (WifiListAdapter.isRightSSID(tmp.SSID)) {
                    result.add(tmp);
                }
            }
        }
        return result;
    }

    private String reviseSSID(String ssid) {
        if (TextUtils.isEmpty(ssid)) return ssid;
        if (ssid.startsWith("\"")) ssid = ssid.substring(1);
        if (ssid.endsWith("\"")) ssid = ssid.substring(0, ssid.length() - 1);
        return ssid;
    }

    public void startScan() {
        m_connect_device = false;
        if (mTipDlg == null) {
            mTipDlg = new ProgressDialog(this, R.string.tip_wifi_wifilist_scanning);
            mTipDlg.setCancelable(true);
        } else {
            mTipDlg.dismiss();
        }

        mTipDlg.setTitle(R.string.tip_wifi_wifilist_scanning);
        mTipDlg.show();

        if (null != mWifiListAdapter) mWifiListAdapter.resetContent();

//		mWifiAdmin.closeWifi();
        mWifiAdmin.openWifi();
        mConnectedWifiInfo = mWifiAdmin.getConnectionInfo();
        mWifiAdmin.startScan();
//		SystemClock.sleep(200);//200毫秒后获取Wifi列表
//		mScanResults = mWifiAdmin.getWifiList();
    }

    @Override
    public void onClick(View v) {
        btnClick(v);
    }

    class WifiRecvImpl implements WifiTools.IWifiRecv {
        @Override
        public void recvBC(WifiManager wifiManager, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {// 扫描状态
                m_scanning = true;
                if (mCurStep == STEP_DEVICE_WIFI) {//只初始化一次
                    //更新列表数据
                    mScanResults = wifiManager.getScanResults();
                    // 过滤出设备Wifi
                    List<ScanResult> _result = /*mScanResults;*/filterWifi(mScanResults);
                    if (_result != null && _result.size() > 0) {
                        mWifiListAdapter = new WifiListAdapter(WifiEtcUI.this, _result);
                        mlvDevicewifiList.setAdapter(mWifiListAdapter);
                        mlvDevicewifiList.setOnItemClickListener(mWifiListAdapter);
                        if (mCurWifiInfo != null && !isNullStr(mCurConnectedSSID)) {
                            mWifiListAdapter.mWifHashMap.put(mCurConnectedSSID, mCurWifiInfo);
                        }
                        mWifiListAdapter.notifyDataSetChanged();
                    } else {
                        toast(R.string.tip_wifi_scanning_fail);
                    }
                }

                if (!m_connect_device) mTipDlg.dismiss();
                Log.d("MSG", "扫描状态...mScanResults=" + mScanResults);
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {// 连接状态
                if (!m_scanning) return;
                WifiInfo info = wifiManager.getConnectionInfo();
                SupplicantState state = info.getSupplicantState();

                WifiListAdapter.WifiFlagInfo wifiInfo = new WifiListAdapter.WifiFlagInfo();
                wifiInfo.mSupplicantState = state;
                wifiInfo.mIsConnected = true;
                String str = null;
                if (state == SupplicantState.ASSOCIATED) {
                    str = T(R.string.tip_wifi_connect_state_associated);
                } else if (state.toString().equals("AUTHENTICATING")/*SupplicantState.AUTHENTICATING*/) {
                    str = T(R.string.tip_wifi_connect_state_authenticating);
                } else if (state == SupplicantState.ASSOCIATING) {
                    str = T(R.string.tip_wifi_connect_state_associating);
                } else if (state == SupplicantState.COMPLETED) {
                    str = T(R.string.tip_wifi_connect_state_completed);
                } else if (state == SupplicantState.DISCONNECTED) {
                    str = T(R.string.tip_wifi_connect_state_disconnected);
                } else if (state == SupplicantState.DORMANT) {
                    str = T(R.string.tip_wifi_connect_state_dormant);
                } else if (state == SupplicantState.FOUR_WAY_HANDSHAKE) {
                    str = T(R.string.tip_wifi_connect_state_four_way_handshake);
                } else if (state == SupplicantState.GROUP_HANDSHAKE) {
                    str = T(R.string.tip_wifi_connect_state_group_handshake);
                } else if (state == SupplicantState.INACTIVE) {
                    str = T(R.string.tip_wifi_connect_state_inactive);
                } else if (state == SupplicantState.INVALID) {
                    str = T(R.string.tip_wifi_connect_state_invalid);
                } else if (state == SupplicantState.SCANNING) {
                    str = T(R.string.tip_wifi_connect_state_scanning);
                } else if (state == SupplicantState.UNINITIALIZED) {
                    str = T(R.string.tip_wifi_connect_state_uninitialized);
                }
                Log.d("MSG", "连接状态...str=" + str);
                //((TextView)findViewById(R.id.tvTitle)).setText(info.getSSID()+":"+str);
                wifiInfo.mStateText = str;
                if (mWifiListAdapter != null) {
                    mWifiListAdapter.mWifHashMap.put(reviseSSID(info.getSSID()), wifiInfo);
                    mWifiListAdapter.notifyDataSetChanged();
                }
                final int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (errorCode == WifiManager.ERROR_AUTHENTICATING) {
                    Log.d("MSG", "连接状态...WIFI验证失败！");
                    if (mTipDlg.isShowing()) mTipDlg.dismiss();
                    toast(R.string.form_wifi_tv_verify_fail);
                }

                //记录当前连接信息
                mCurWifiInfo = wifiInfo;
                mCurConnectedSSID = reviseSSID(info.getSSID());
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                if (!m_scanning) return;
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null == parcelableExtra) return;
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                if (state != NetworkInfo.State.CONNECTED) return;

                WifiInfo info = wifiManager.getConnectionInfo();
                if (mCurStep == STEP_DEVICE_WIFI) {
                    final String _ssid = reviseSSID(info.getSSID());
                    //SSID合法性检测
                    if (WifiListAdapter.isRightSSID(_ssid)) {
                        if (mTipDlg.isShowing()) mTipDlg.dismiss();
                        mCurConnectedSSID = _ssid;
                        autoSwitchNextStep();
                    } else {
                        // 只记录初始进入的首个已连接的家庭Wifi
                        if (mConnectedWifiInfo == null) mConnectedWifiInfo = info;
//		                    	if(mWifiListAdapter != null){
//			                    	if(mAlertDialog != null)mAlertDialog.dismiss();
//									mAlertDialog = MyTipDialog
//											.getMyDialog(
//													mContext,
//													gRstr(R.string.dlg_tip),
//													mResources.getString(R.string.tip_wifi_connect_success_info, _ssid),
//													gRstr(R.string.sure),
//													gRstr(R.string.cancel),
//													new IDialogMethod() {
//														@Override
//														public void sure() {
//															mCurConnectedSSID = _ssid;
//															initUIByStep(STEP_WIFI_INFO);
//														}
//													});
//			                    	mAlertDialog.show();
//		                    	}
                    }
                }
            }
        }
    }

    public void autoSwitchNextStep() {
        if (null == mConnectedWifiInfo) return;
        String routeSsid = reviseSSID(mConnectedWifiInfo.getSSID());
        if (TextUtils.isEmpty(routeSsid)) return;
        int encrypType = findSecurity(routeSsid);
        if (SECURITY_NONE == encrypType) {
            tellWifi(routeSsid, "", encrypType);
        } else {
            initUIByStep(STEP_WIFI_INFO);
        }
    }

    public void switchOldWifi() {
        mWifiAdmin.enableNetwork(mConnectedWifiInfo.getNetworkId(), true);
        /*String routeSsid = reviseSSID(mConnectedWifiInfo.getSSID());
        if (TextUtils.isEmpty(routeSsid)) return;
        int encrypType = findSecurity(routeSsid);
        String pwd = (metWifiPwd == null) ? "" : metWifiPwd.getText().toString();
        connectAp(routeSsid, pwd, getSp2WifiType(encrypType));*/
    }

    public void connectAp(AccessPoint ap, String ssid, String pwd) {
        mWifiAdmin.connectConfiguration(ap, ssid, pwd);
    }

    public void connectAp(final String ssid, final String pwd, final int encrypType) {
        mWifiAdmin.connectConfiguration(ssid, pwd, encrypType);
    }

    public void connectDeviceAp(AccessPoint ap, String ssid, String pwd) {
        m_connect_device = true;
        if (mTipDlg.isShowing()) mTipDlg.dismiss();
        mTipDlg = new ProgressDialog(this, R.string.tip_wifi_connect_state_associating);
        mTipDlg.setCancelable(false);
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                mTipDlg.dismiss();
                m_connect_device = false;
                toast(R.string.form_wifi_tv_etc_fail);
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        mTipDlg.show(30000);
        connectAp(ap, ssid, pwd);
    }

    public void setTextViewState(TextView tv, boolean isActive) {
        if (tv != null) {
            int tvcolor = (isActive) ? mResources.getColor(R.color.st_item_font) : Color.BLACK;
            int bgColor = (isActive) ? mResources.getColor(R.color.st_bar_bg) : Color.TRANSPARENT;
            tv.setTextColor(tvcolor);
            tv.setBackgroundColor(bgColor);
        }
    }

    public static final int STEP_DEVICE_WIFI = 0;
    public static final int STEP_WIFI_INFO = 1;
    public static final int STEP_RESULT_Fail = 2;
    public int mCurStep = STEP_DEVICE_WIFI;

    public void initUIByStep(int step) {
        mCurStep = step;
        findViewById(R.id.btnRight).setVisibility(View.GONE);
        if (step == STEP_DEVICE_WIFI) {
            mDeviceWifiInfo.setVisibility(View.VISIBLE);
            mMyWifiInfo.setVisibility(View.GONE);
            mResult.setVisibility(View.GONE);

            setTextViewState(mtvDeviceWifi, true);
            setTextViewState(mtvWifiInfo, false);
            setTextViewState(mtvEtcFinish, false);

            Button btnScan = (Button) findViewById(R.id.btnRight);
            btnScan.setVisibility(View.VISIBLE);
            btnScan.setText(R.string.flush);
            btnScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startScan();
                }
            });
        } else if (step == STEP_WIFI_INFO) {
            mDeviceWifiInfo.setVisibility(View.GONE);
            mMyWifiInfo.setVisibility(View.VISIBLE);
            mResult.setVisibility(View.GONE);

            setTextViewState(mtvDeviceWifi, false);
            setTextViewState(mtvWifiInfo, true);
            setTextViewState(mtvEtcFinish, false);

            String ssid = reviseSSID(mConnectedWifiInfo.getSSID());
            metWifiName.setText(ssid);
            mspEncrypedTypes.setSelection(findSecurity(ssid));
            mspEncrypedTypes.setEnabled(false);//xml中配置失效，所以只能用代码强设置
        } else if (step == STEP_RESULT_Fail) {
            mDeviceWifiInfo.setVisibility(View.GONE);
            mMyWifiInfo.setVisibility(View.GONE);
            mResult.setVisibility(View.VISIBLE);

            setTextViewState(mtvDeviceWifi, false);
            setTextViewState(mtvWifiInfo, false);
            setTextViewState(mtvEtcFinish, true);
        }
    }

    public void btnClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreStepTo1://上一步
                initUIByStep(STEP_DEVICE_WIFI);
                break;

            case R.id.btnNextStepTo3://下一步
                if (isRightForm(true)) {
                    String name = (metWifiName == null) ? null : metWifiName.getText().toString();
                    String pwd = (metWifiPwd == null) ? "" : metWifiPwd.getText().toString();
                    int encrypType = mspEncrypedTypes.getSelectedItemPosition();
                    if (SECURITY_NONE != encrypType && "".equals(pwd)) {
                        toast(R.string.tip_wifi_pwd_is_null);
                        return;
                    }

                    hideInputPanel(null);
                    tellWifi(name, pwd, encrypType);
                }

                break;

            case R.id.btnMoreWifi: {//Wifi List
                int size = (mScanResults == null) ? 0 : mScanResults.size();
                final String[] ssids = (size > 0) ? new String[size] : null;
                for (int i = 0; i < size; i++) {
                    String ssid = (mScanResults.get(i) != null) ? mScanResults.get(i).SSID : null;
                    if (ssid != null && !"".equals(ssid)) {
                        ssids[i] = mScanResults.get(i).SSID;
                    }
                }
                if (ssids != null && ssids.length > 0) {
                    new AlertDialog.Builder(this)
                            .setItems(ssids,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            metWifiName.setText(ssids[which]);
                                            mspEncrypedTypes.setSelection(getSecurity(mScanResults.get(which)));
                                            dialog.dismiss();
                                        }
                                    }
                            ).create().show();
                } else {
                    toast(R.string.tip_wifi_wifilist_is_null);
                }
            }
            break;

            case R.id.btnReEtc: {
                initUIByStep(STEP_DEVICE_WIFI);
            }
            break;

            case R.id.btnEtcFinish: {
                finish();
            }
            break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        if (mWifiReceiver != null) {
            mWifiReceiver.registerReceiver();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (mWifiReceiver != null) {
            mWifiReceiver.unRegisterReceiver();
            mWifiReceiver = null;
        }

        switchOldWifi();
        super.onStop();
    }

    public boolean isRightForm(boolean isTip) {
        boolean isOK = false;
        String msg = "";
        String name = (metWifiName == null) ? null : metWifiName.getText().toString();
        String pwd = (metWifiPwd == null) ? null : metWifiPwd.getText().toString();
        if (isNullStr(name)) {
            msg = T(R.string.tip_wifi_name_is_null);
        } else if (isNullStr(pwd)) {
            //无线支持空密码
            //msg = gRstr(R.string.tip_wifi_pwd_is_null);
            isOK = true;
        } else {
            //合法性检测
            if (pwd.length() >= 8 && pwd.length() <= 32) {
                isOK = true;
            } else {
                msg = T(R.string.tip_wifi_pwd_is_illegal);
            }
        }
        if (isTip && !isOK) {
            toast(msg);
        }
        return isOK;
    }

    /**
     * 设备无线Wifi的加密类型与摄像机类型相互转换<br>
     *
     * @param encrypType 加密类型<br>
     * @return
     */
    public int getDevEncryType(int encrypType) {
        //NONE/WEP/WPA PSK/WPA2 PSK/(WPA/WPA2 PSK)
        int type = WIFI_AUTH_NONE;
        switch (encrypType) {
            case SECURITY_NONE:
                type = WIFI_AUTH_NONE;
                break;

            case SECURITY_WEP:
                type = WIFI_AUTH_WEP_SHARED;
                break;

            case SECURITY_WPA_TKIP:
                type = WIFI_AUTH_WPA_PSK_TKIP;
                break;
            case SECURITY_WPA_AES:
                type = WIFI_AUTH_WPA_PSK_AES;
                break;

            case SECURITY_WPA2_TKIP:
            case SECURITY_WPA_WPA2_TKIP:
                type = WIFI_AUTH_WPA2_PSK_TKIP;
                break;
            case SECURITY_WPA2_AES:
            case SECURITY_WPA_WPA2_AES:
                type = WIFI_AUTH_WPA2_PSK_AES;
                break;

            case SECURITY_802_1x_EAP: //暂定
                type = WIFI_AUTH_WPA2_PSK_AES;
                break;

            default:
                break;
        }
        return type;
    }

    public int getSecurity(ScanResult result) {
        if (result != null) {
            if (result.capabilities.contains("WEP")) {
                return SECURITY_WEP;
            } else if (result.capabilities.contains("PSK")) {
                boolean wpa = result.capabilities.contains("WPA-PSK");
                boolean wpa2 = result.capabilities.contains("WPA2-PSK");
                boolean tkip = result.capabilities.contains("TKIP");
                if (tkip) {
                    if (wpa2 && wpa) {
                        return SECURITY_WPA_WPA2_TKIP;
                    } else if (wpa2) {
                        return SECURITY_WPA2_TKIP;
                    } else if (wpa) {
                        return SECURITY_WPA_TKIP;
                    } else {
                        return SECURITY_NONE;
                    }
                } else {//aes
                    if (wpa2 && wpa) {
                        return SECURITY_WPA_WPA2_AES;
                    } else if (wpa2) {
                        return SECURITY_WPA2_AES;
                    } else if (wpa) {
                        return SECURITY_WPA_AES;
                    } else {
                        return SECURITY_NONE;
                    }
                }
            } else if (result.capabilities.contains("EAP")) {
                return SECURITY_802_1x_EAP;
            }
        }
        return SECURITY_NONE;
    }

    public int getSp2WifiType(int type) {
        int _type = SECURITY_NONE;
        if (type == SECURITY_NONE) {
            _type = AccessPoint.SECURITY_NONE;
        } else if (type == SECURITY_WEP) {
            _type = AccessPoint.SECURITY_WEP;
        } else if (type == SECURITY_802_1x_EAP) {
            _type = AccessPoint.SECURITY_EAP;
        } else {
            _type = AccessPoint.SECURITY_PSK;
        }
        return _type;
    }

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_WPA_TKIP = 2;
    public static final int SECURITY_WPA_AES = 3;
    public static final int SECURITY_WPA2_TKIP = 4;
    public static final int SECURITY_WPA2_AES = 5;
    public static final int SECURITY_WPA_WPA2_TKIP = 6;
    public static final int SECURITY_WPA_WPA2_AES = 7;
    public static final int SECURITY_802_1x_EAP = 8;

    public static final int WIFI_AUTH_NONE = 0;
    public static final int WIFI_AUTH_WEP_SHARED = 1;
    public static final int WIFI_AUTH_WEP_NONE = 2;
    public static final int WIFI_AUTH_WPA_PSK_TKIP = 3;
    public static final int WIFI_AUTH_WPA_PSK_AES = 4;
    public static final int WIFI_AUTH_WPA2_PSK_TKIP = 5;
    public static final int WIFI_AUTH_WPA2_PSK_AES = 6;
    public static final String WIFI_URL_GET = "http://192.168.169.1/wireless_sta?";

    /**
     * 测试链接：http://192.168.169.1/wireless_sta?SSID=TP_LINK&SecurityMode=&passwd=888888<br>
     */
    public void tellWifi(final String ssid, final String pwd, final int encrypType) {
        mTipDlg.dismiss();
        //检测网络是否已打开
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(R.string.dlg_network_check_tip);
            return;
        }

        final ProgressDialog tipDlg = new ProgressDialog(this, R.string.tip_wifi_info_inform_device);
        tipDlg.setCancelable(false);
//		AjaxParams params = new AjaxParams();
//		params.put("SSID", ssid);
//		params.put("SecurityMode", getDevEncryType(encrypType)+"");
//		params.put("passwd", pwd);

        /*int ret = LibImpl.getInstance().getFuncLib().SettingDevWIFI(ssid, pwd);
        if (true) return;*/

        String _url = WIFI_URL_GET;
        StringBuffer buf = new StringBuffer();
        try {
            String _ssid = URLEncoder.encode(ssid, "gb2312");
            String _pwd = URLEncoder.encode(pwd, "gb2312");
            buf.append("SSID=").append(_ssid).append("&")
                    .append("SecurityMode=").append(getDevEncryType(encrypType)).append("&")
                    .append("passwd=").append(_pwd);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        _url += buf.toString();
        FinalHttp fh = new FinalHttp();
        fh.configCharset("gb2312");//gb2312==UTF-8
        fh.configTimeout(15 * 1000);
        Log.i("MSG", "WifiEtcUI@_url=[" + _url + "]");
        fh.get(/*WIFI_URL_GET*/_url, /*params*/null, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object t) {
                m_success = true;
                Log.v("MSG", (t == null) ? "null" : "onSuccess..." + t.toString());
                toast(R.string.tip_wifi_etc_success);
                //connectAp(ssid, pwd, getSp2WifiType(encrypType));//切回之前网络
                if (tipDlg.isShowing()) tipDlg.dismiss();
                Intent intent = new Intent(WifiEtcUI.this, LoginUI2.class);
                intent.putExtra(Constant.EXTRA_WIFI_SSID, mCurConnectedSSID);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                if (tipDlg.isShowing()) tipDlg.dismiss();
                Log.v("MSG", "onFailure...errorNO=" + errorNo + ",strMsg=" + strMsg);
                initUIByStep(STEP_RESULT_Fail);
                //connectAp(ssid, pwd, getSp2WifiType(encrypType));//切回之前网络
                switchOldWifi();
                super.onFailure(t, errorNo, strMsg);
            }

            @Override
            public void onStart() {
                Log.v("MSG", "onStart...");
                tipDlg.show();
            }

            @Override
            public void onLoading(long count, long current) {
                Log.v("MSG", "onLoading...count=" + count + ",current=" + current);
            }
        });
    }

    public int findSecurity(String ssid) {
        int type = SECURITY_WPA_WPA2_TKIP;
        for (ScanResult result : mScanResults) {
            if (ssid.compareToIgnoreCase(result.SSID) == 0) {
                type = getSecurity(result);
                break;
            }
        }
        return type;
    }
}

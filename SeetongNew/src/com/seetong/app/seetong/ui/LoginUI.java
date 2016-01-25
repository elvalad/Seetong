package com.seetong.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.android.zxing.ui.CaptureTDCodeUI;
import com.custom.etc.EtcInfo;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.NetworkUtils;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.ui.aid.TDCodeOnClickListener;
import com.seetong.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

/**
 * @author qinglei.yin@192.168.88.9<br>
 *         2014-3-26 下午5:37:29<br>
 * @declaration 登录界面<br>
 */
public class LoginUI extends BaseActivity implements OnClickListener {
    public static final String DEFAULT_DEV_NAME = EtcInfo.DEFAULT_DEV_NAME;
    public static final String DEFAULT_DEV_PWD = EtcInfo.DEFAULT_DEV_PWD;
    public static final String DEFAULT_SERVER_URL = EtcInfo.DEFAULT_P2P_URL;
    public static final int DEFAULT_SERVER_PORT = EtcInfo.DEFAULT_SERVER_PORT;

    private int mCurLoginTypes = Define.LOGIN_TYPE_DEVICE; //0:device 1:user
    private ProgressDialog mTipDlg;
    private DeviceInfo mDevInfo;
    private int mLoginState = 0;//0:success other:SDK_CONSTANT@TPS_ERR_NUM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_login_title));
        initWidget();
    }

    public void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        initUIByLoginTypes();

        findViewById(R.id.btnEtcFinish).setOnClickListener(this);
        findViewById(R.id.btnEtcFinish).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //test:topseetest--yinql:123456
                sStr(R.id.etUserName, "yinql");
                sStr(R.id.etUserPassword, "123456");
                ((CheckBox) findViewById(R.id.cbSavePassword)).setChecked(true);
                return true;
            }
        });
        findViewById(R.id.btnRegister).setOnClickListener(this);

        findViewById(R.id.tvDeviceWifi).setOnClickListener(this);
        findViewById(R.id.tvWifiInfo).setOnClickListener(this);

        findViewById(R.id.btnPreStepTo1).setOnClickListener(this);
        findViewById(R.id.btnNextStep).setOnClickListener(this);
        findViewById(R.id.btnNextStep).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sStr(R.id.etWifiName, "100134");
                sStr(R.id.etSerPort, DEFAULT_SERVER_PORT + "");
                sStr(R.id.etDevName, DEFAULT_DEV_NAME);
                sStr(R.id.etWifiPwd, DEFAULT_DEV_PWD);
                return true;
            }
        });

        Button btnMore = (Button) findViewById(R.id.btnMoreWifi);
        btnMore.setOnClickListener(this);
        findViewById(R.id.llMoreDeviceInfo).setVisibility(View.GONE);
        btnMore.setText(T(R.string.more));

        loadData();
        addContextAgent();

        mTdCodeOnClickListener = new TDCodeOnClickListener(new TDCodeOnClickListener.TDCodeInterface() {
            @Override
            public void handleData(String codeText) {
                if (!mTdCodeOnClickListener.isRightCode(codeText)) {
                    toast(R.string.td_tip_error_code);
                    return;
                }

                DeviceInfo devInfo = mTdCodeOnClickListener.getDevInfoByCode(codeText);
                if (devInfo == null) {
                    toast(R.string.td_tip_code_parse_fail);
                    return;
                }

                //mActivity1.sendMyToast(R.string.td_tip_code_parse_success);
                sStr(R.id.etWifiName, devInfo.getDevId());
                sStr(R.id.etSerPort, devInfo.getDevPort() + "");
                sStr(R.id.etDevName, devInfo.getUserName());
                sStr(R.id.etWifiPwd, devInfo.getUserPassword());

                loginCallback();
            }
        });
        findViewById(R.id.btnPreStepTo1).setOnClickListener(this);

        findViewById(R.id.btnWifiEtc).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternet(LoginUI.this);
//				Intent it = new Intent(mContext, WifiEtcUI.class);
//				startActivityForResult(it, 0x1010);
            }
        });

        findViewById(R.id.btnWifiEtcByDevice).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternet(LoginUI.this);
//				Intent it = new Intent(mContext, WifiEtcUI.class);
//				startActivityForResult(it, 0x1010);
            }
        });
    }

    protected void addContextAgent() {
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onResume() {
        LibImpl.getInstance().addHandler(m_handler);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LibImpl.getInstance().removeHandler(m_handler);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    public boolean getFormData() {
        boolean isOK = false;
        if (mCurLoginTypes == Define.LOGIN_TYPE_DEVICE) {
            if (isNullStr(gStr(R.id.etWifiName))) {
                toast(T(R.string.dlg_input_ok_server_address_tip));
            } else if (isNullStr(gStr(R.id.etSerPort))) {
                toast(T(R.string.dlg_input_ok_server_port_tip));
            } else if (isNullStr(gStr(R.id.etDevName))) {
                toast(T(R.string.dlg_input_ok_dev_user_tip));
            } else if (isNullStr(gStr(R.id.etWifiPwd))) {
                toast(T(R.string.dlg_input_ok_dev_password_tip));
            } else {
                if (mDevInfo == null) mDevInfo = new DeviceInfo();
                mDevInfo.setDevId(gStr(R.id.etWifiName));
                mDevInfo.setDevIP(gStr(R.id.etWifiName));
                mDevInfo.setDevPort(Integer.parseInt(gStr(R.id.etSerPort)));
                mDevInfo.setUserName(gStr(R.id.etDevName));
                mDevInfo.setUserPassword(gStr(R.id.etWifiPwd));
                isOK = true;
            }
        } else {
            if (isNullStr(gStr(R.id.etUserName))) {
                toast(T(R.string.dlg_input_ok_user_tip));
            } else if (isNullStr(gStr(R.id.etUserPassword))) {
                toast(T(R.string.dlg_input_ok_password_tip));
            } else {
                if (mDevInfo == null) mDevInfo = new DeviceInfo();
                mDevInfo.setDevId(EtcInfo.DEFAULT_P2P_URL);
                mDevInfo.setDevIP(EtcInfo.DEFAULT_P2P_URL);
                mDevInfo.setDevPort(80);
                mDevInfo.setUserName(gStr(R.id.etUserName));
                mDevInfo.setUserPassword(gStr(R.id.etUserPassword));
                isOK = true;
            }
        }
        return isOK;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//		case R.id.btnSweep:{
//			Intent it = new Intent(this, CaptureTDCodeUI.class);
//			startActivityForResult(it, 0x1001);
//		}
//			break;
            case R.id.btnPreStepTo1:
                onBtnScanQrCode();
                break;
            case R.id.btnNextStep:
            case R.id.btnEtcFinish:
                loginCallback();
                break;

            case R.id.btnRegister:
                //检测网络是否已打开
                if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
                    toast(T(R.string.dlg_network_check_tip));
                } else {
                    Intent it = new Intent(this, UserRegisterUI.class);
                    startActivityForResult(it, USER_REGISTER_REQ_ID);
                }
                break;

            case R.id.tvDeviceWifi:
                if (mCurLoginTypes != Define.LOGIN_TYPE_USER) {
                    mCurLoginTypes = Define.LOGIN_TYPE_USER;
                    initUIByLoginTypes();
                }
                break;

            case R.id.tvWifiInfo:
                if (mCurLoginTypes != Define.LOGIN_TYPE_DEVICE) {
                    mCurLoginTypes = Define.LOGIN_TYPE_DEVICE;
                    initUIByLoginTypes();
                }
                break;

            case R.id.btnMoreWifi:
                boolean isShow = findViewById(R.id.llMoreDeviceInfo).getVisibility() == View.VISIBLE;
                if (isShow) {
                    findViewById(R.id.llMoreDeviceInfo).setVisibility(View.GONE);
                    ((Button) v).setText(mResources.getString(R.string.more));
                } else {
                    findViewById(R.id.llMoreDeviceInfo).setVisibility(View.VISIBLE);
                    ((Button) v).setText(mResources.getString(R.string.hide));
                }
                break;

            default:
                break;
        }
    }

    private void onBtnScanQrCode() {
        Intent it = new Intent(this, CaptureTDCodeUI.class);
        startActivityForResult(it, TDCodeOnClickListener.TD_CODE_REQ_ID);
    }

    public void loginCallback() {
        //检测网络是否已打开
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(T(R.string.dlg_network_check_tip));
        } else {
            if (getFormData()) {
                // 如果在按menu或back按钮的时候，以及打开了软键盘，则将软键盘隐藏
                hideInputPanel(null);
                mTipDlg.setCallback(new ProgressDialog.ICallback() {
                    @Override
                    public void onTimeout() {
                        mTipDlg.dismiss();
                        toast(R.string.dlg_login_fail_tip);
                    }

                    @Override
                    public boolean onCancel() {
                        return false;
                    }
                });

                mTipDlg.show(60000);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DeviceInfo devInfo = mDevInfo;
                        Global.m_devInfo = mDevInfo;
                        //mFunclibAgent.LogoutAll();
                        mLoginState = LibImpl.getInstance().Login(devInfo.getUserName(), devInfo
                                .getUserPassword(), devInfo.getDevIP(), (short) devInfo.getDevPort());
                        Log.w("Login", "user login state:" + mLoginState);
                        if (mLoginState != 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTipDlg.dismiss();
                                    //toast(SDK_CONSTANT.getTPSErrText(mLoginState, mContext));
                                    if (mLoginState == SDK_CONSTANT.ERR_DEV_LOCK) {
                                        String msg = "";
                                        if (mCurLoginTypes == Define.LOGIN_TYPE_DEVICE) {
                                            msg = T(R.string.ipc_err_dev_lock);
                                        } else {
                                            msg = T(R.string.ipc_err_user_lock);
                                        }

                                        MyTipDialog.popDialog(LoginUI.this, R.string.dlg_tip, msg, R.string.close);
                                    } else {
                                        MyTipDialog.popDialog(LoginUI.this, R.string.dlg_tip, ConstantImpl.getTPSErrText(mLoginState), R.string.close);
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }


    public void initUIByLoginTypes() {
        if (mCurLoginTypes == Define.LOGIN_TYPE_DEVICE) {
            ((TextView) findViewById(R.id.tvDeviceWifi)).setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.tvDeviceWifi)).setBackgroundColor(Color.TRANSPARENT);
            ((TextView) findViewById(R.id.tvWifiInfo)).setTextColor(mResources.getColor(R.color.st_item_font));
            ((TextView) findViewById(R.id.tvWifiInfo)).setBackgroundColor(mResources.getColor(R.color.st_bar_bg));

            findViewById(R.id.svDevices).setVisibility(View.GONE);
            (findViewById(R.id.llDeviceForm)).setVisibility(View.VISIBLE);
            (findViewById(R.id.llDeviceForm)).findViewById(R.id.svDevices).setVisibility(View.VISIBLE);
        } else {
            ((TextView) findViewById(R.id.tvDeviceWifi)).setTextColor(mResources.getColor(R.color.st_item_font));
            ((TextView) findViewById(R.id.tvDeviceWifi)).setBackgroundColor(mResources.getColor(R.color.st_bar_bg));
            ((TextView) findViewById(R.id.tvWifiInfo)).setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.tvWifiInfo)).setBackgroundColor(Color.TRANSPARENT);

            findViewById(R.id.svDevices).setVisibility(View.VISIBLE);
            (findViewById(R.id.llDeviceForm)).setVisibility(View.GONE);
            (findViewById(R.id.llDeviceForm)).findViewById(R.id.svDevices).setVisibility(View.GONE);
        }

        Global.m_loginType = mCurLoginTypes;
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        int msgType = msg.arg1;
        switch (msgType) {
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_OK:
                if (mTipDlg.isTimeout()) return;
                mTipDlg.setTitle(T(R.string.dlg_login_recv_list_tip));
                saveData();
                break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED: {
                mTipDlg.dismiss();
                //toast(T(R.string.dlg_login_fail_tip));
//				MyTipDialog.popDialog(mContext, R.string.dlg_tip,SDK_CONSTANT.getTPSErrText(mLoginState, mContext),R.string.close);
            }
            break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DEV_DATA: {
                if (mTipDlg.isTimeout()) return;
                mTipDlg.dismiss();
                if ((msg.obj != null) && (msg.obj instanceof LibImpl.MsgObject)) {
                    Intent it = new Intent(LoginUI.this, MainActivity.class);
                    it.putExtra(Constant.DEVICE_INFO_KEY, mDevInfo.getDevId());
                    int AddLiveID = getIntent().getIntExtra(MainActivity.ADD_LIVE_KEY, 0);
                    if (AddLiveID == MainActivity.ADD_LIVE_ID) {
                        setResult(RESULT_OK, it);
                    } else {
                        LoginUI.this.startActivity(it);
                    }
                    finish();
                } else {
                    toast(T(R.string.dlg_login_recv_list_fail_tip));
                }
            }
            break;
            case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH:

                break;
            default:
                break;
        }
    }

    public void loadData() {
        boolean isSaveData = Global.m_spu_login.loadBooleanSharedPreference(Define.IS_SAVE_DATA);
        if (!isSaveData) {
            saveData();
        }
        //user login
        CheckBox cbSavePwd = (CheckBox) findViewById(R.id.cbSavePassword);
        cbSavePwd.setChecked(Global.m_spu_login.loadBooleanSharedPreference(Define.IS_SAVE_PWD));
        sStr(R.id.etUserName, Global.m_spu_login.loadStringSharedPreference(Define.USR_NAME));
        sStr(R.id.etUserPassword, Global.m_spu_login.loadStringSharedPreference(Define.USR_PSW));
        //device login
        sStr(R.id.etWifiName, Global.m_spu_login.loadStringSharedPreference(Define.DEV_ID));
        sStr(R.id.etSerPort, Global.m_spu_login.loadIntSharedPreference(Define.SERVER_PORT) + "");
        sStr(R.id.etDevName, Global.m_spu_login.loadStringSharedPreference(Define.DEV_NAME));
        sStr(R.id.etWifiPwd, Global.m_spu_login.loadStringSharedPreference(Define.DEV_PSW));
    }

    public void saveData() {
        boolean isSaveData = Global.m_spu_login.loadBooleanSharedPreference(Define.IS_SAVE_DATA);
        if (!isSaveData) {//初始化默认数据
            Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_DATA, true);
            Global.m_spu_login.saveSharedPreferences(Define.USR_NAME, "");
            Global.m_spu_login.saveSharedPreferences(Define.USR_PSW, "");
            Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_PWD, false);

            Global.m_spu_login.saveSharedPreferences(Define.DEV_ID, "");
            Global.m_spu_login.saveSharedPreferences(Define.SERVER_PORT, DEFAULT_SERVER_PORT);
            Global.m_spu_login.saveSharedPreferences(Define.DEV_NAME, DEFAULT_DEV_NAME);
            Global.m_spu_login.saveSharedPreferences(Define.DEV_PSW, DEFAULT_DEV_PWD);
        } else {
            if (mCurLoginTypes == Define.LOGIN_TYPE_DEVICE) {//device login
                Global.m_spu_login.saveSharedPreferences(Define.DEV_ID, gStr(R.id.etWifiName));
                Global.m_spu_login.saveSharedPreferences(Define.SERVER_PORT, DEFAULT_SERVER_PORT);
                Global.m_spu_login.saveSharedPreferences(Define.DEV_NAME, gStr(R.id.etDevName));
                Global.m_spu_login.saveSharedPreferences(Define.DEV_PSW, gStr(R.id.etWifiPwd));
            } else {//user login
                Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_DATA, true);
                Global.m_spu_login.saveSharedPreferences(Define.USR_NAME, gStr(R.id.etUserName));
                CheckBox cbSavePwd = (CheckBox) findViewById(R.id.cbSavePassword);
                boolean isSavePwd = cbSavePwd.isChecked();
                Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_PWD, isSavePwd);
                Global.m_spu_login.saveSharedPreferences(Define.USR_PSW, (isSavePwd) ? gStr(R.id.etUserPassword) : "");
            }
        }
    }

    /*user register*/
    public static final int USER_REGISTER_REQ_ID = 1001;
    public static String USER_INFO_CONTENT_KEY = "user_info_content_key";
    TDCodeOnClickListener mTdCodeOnClickListener;

    public static final int WIFI_ETC_REQ_ID = 0x1010;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mTdCodeOnClickListener != null) {
            mTdCodeOnClickListener.tdCodeRecv(requestCode, resultCode, data);
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case USER_REGISTER_REQ_ID: {
                    UserRegisterUI.RegisterInfo regInfo = (UserRegisterUI.RegisterInfo) data.getSerializableExtra(USER_INFO_CONTENT_KEY);
                    if (regInfo != null) {
                        sStr(R.id.etUserName, regInfo.userName);
                        sStr(R.id.etUserPassword, regInfo.userPwd);
                        toast(regInfo.userName + T(R.string.reg_error_null));
                    } else {
                        toast(R.string.reg_register_info_is_null);
                    }
                }
                break;
                case WIFI_ETC_REQ_ID:
                    String wifiSSID = data.getStringExtra("wifiSSID");
                    ((EditText) findViewById(R.id.etWifiName)).setText(wifiSSID);
                    onClick(findViewById(R.id.tvWifiInfo));
                    break;

                default:
                    break;
            }
        }
    }

    public void checkInternet(final Context context) {
        int isOK = NetworkUtils.getNetworkState(this);
        if (isOK == NetworkUtils.WIFI) {
            final ProgressDialog tipDlg = new ProgressDialog(context, R.string.tip_wifi_connect_internet_check);
            tipDlg.setCancelable(false);

            FinalHttp fh = new FinalHttp();
            String url = "http://www.baidu.com";
            fh.configTimeout(10 * 1000);
            fh.get(url, new AjaxCallBack<Object>() {
                @Override
                public void onSuccess(Object t) {
                    tipDlg.dismiss();
                    Log.e("MSG", (t == null) ? "null" : "onSuccess..." + t.toString());
                    Intent it = new Intent(context, WifiEtcUI.class);
                    startActivityForResult(it, WIFI_ETC_REQ_ID);
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    tipDlg.dismiss();
                    Log.e("MSG", "onFailure...errorNO=" + errorNo + ",strMsg=" + strMsg);
                    toast(R.string.tip_wifi_connect_internet_error);
                    super.onFailure(t, errorNo, strMsg);
                }

                @Override
                public void onStart() {
                    Log.e("MSG", "onStart...");
                    tipDlg.show();
                }

                @Override
                public void onLoading(long count, long current) {
                    Log.e("MSG", "onLoading...count=" + count + ",current=" + current);
                }
            });
        } else if (isOK == NetworkUtils.MOBILE) {
            toast(T(R.string.dlg_network_check_WIFI_tip));
        } else {
            toast(T(R.string.dlg_network_check_WIFI_tip));
        }
    }
}
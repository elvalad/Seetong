package com.seetong5.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.android.zxing.ui.CaptureTDCodeUI;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.comm.NetworkUtils;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.aid.TDCodeOnClickListener;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import com.seetong5.app.seetong.ui.ext.RegexpEditText;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;

public class LoginUI2 extends BaseActivity implements OnClickListener {
    public static final String DEFAULT_DEV_NAME = EtcInfo.DEFAULT_DEV_NAME;
    public static final String DEFAULT_DEV_PWD = EtcInfo.DEFAULT_DEV_PWD;
    public static final String DEFAULT_SERVER_URL = EtcInfo.DEFAULT_P2P_URL;
    public static final int DEFAULT_SERVER_PORT = EtcInfo.DEFAULT_SERVER_PORT;

    private ProgressDialog mTipDlg;
    private DeviceInfo mDevInfo = new DeviceInfo();

    private RegexpEditText m_txt_user;
    private RegexpEditText m_txt_pwd;
    private RegexpEditText m_txt_dev_id;
    private RegexpEditText m_txt_dev_user;
    private RegexpEditText m_txt_dev_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_ui2);

        LibImpl.getInstance().init();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            int version = Global.m_spu.loadIntSharedPreference(Define.SAVED_VERSION);
            if (version < info.versionCode) {
                Global.m_spu.saveSharedPreferences(Define.SAVED_VERSION, info.versionCode);
                Intent it = new Intent(this, Wizard.class);
                startActivity(it);
                finish();
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initWidget();
    }

    public void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_login_by_user).setOnClickListener(this);
        findViewById(R.id.btn_login_by_device).setOnClickListener(this);
        findViewById(R.id.btn_login_by_demo).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_dev_login).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.btn_search_wifi).setOnClickListener(this);
        m_txt_user = (RegexpEditText) findViewById(R.id.txt_user_name);
        m_txt_user.setRequired(true);
        m_txt_pwd = (RegexpEditText) findViewById(R.id.txt_password);
        m_txt_pwd.setRequired(true);
        m_txt_dev_id = (RegexpEditText) findViewById(R.id.txt_dev_id);
        m_txt_dev_id.setRequired(true);
        m_txt_dev_user = (RegexpEditText) findViewById(R.id.txt_dev_user_name);
        m_txt_dev_user.setRequired(true);
        m_txt_dev_pwd = (RegexpEditText) findViewById(R.id.txt_dev_password);
        m_txt_dev_pwd.setRequired(true);

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
                sStr(R.id.txt_dev_id, devInfo.getDevId());
                //sStr(R.id.etSerPort, devInfo.getDevPort()+"");
                sStr(R.id.txt_dev_user_name, devInfo.getUserName());
                sStr(R.id.txt_dev_password, devInfo.getUserPassword());
                onLoginByDevice();
            }
        });

        findViewById(R.id.btn_scan_qrcode).setOnClickListener(this);

        loadData();
        LibImpl.getInstance().addHandler(m_handler);

        Global.m_loginType = Define.LOGIN_TYPE_USER;
        if (!"".equals(gStr(R.id.txt_user_name)) && !"".equals(gStr(R.id.txt_password))) {
            Global.m_loginType = Define.LOGIN_TYPE_USER;
            onBtnLogin();
            return;
        }

        /*findViewById(R.id.btn_login).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                m_txt_user.setText("0003");
                m_txt_pwd.setText("cswxzx123");
                return false;
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LibImpl.getInstance().removeHandler(m_handler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                Global.m_loginType = Define.LOGIN_TYPE_USER;
                findViewById(R.id.layout_login_types).setVisibility(View.GONE);
                findViewById(R.id.layout_by_user).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_by_device).setVisibility(View.GONE);
                findViewById(R.id.btn_back).setVisibility(View.GONE);
                findViewById(R.id.btn_register).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_login_by_device).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_login_by_demo).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_search_wifi).setVisibility(View.GONE);
                break;
            case R.id.btn_login_by_user:
                Global.m_loginType = Define.LOGIN_TYPE_USER;
                findViewById(R.id.layout_login_types).setVisibility(View.GONE);
                findViewById(R.id.layout_by_user).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_register).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_search_wifi).setVisibility(View.GONE);
                break;
            case R.id.btn_login_by_device:
                Global.m_loginType = Define.LOGIN_TYPE_DEVICE;
                findViewById(R.id.layout_login_types).setVisibility(View.GONE);
                findViewById(R.id.layout_by_device).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_by_user).setVisibility(View.GONE);
                findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_register).setVisibility(View.GONE);
                findViewById(R.id.btn_login_by_device).setVisibility(View.GONE);
                findViewById(R.id.btn_login_by_demo).setVisibility(View.GONE);
                findViewById(R.id.btn_search_wifi).setVisibility(View.VISIBLE);
                break;
            case R.id.btn_login_by_demo:
                onLoginByDemo();
                break;
            case R.id.btn_login:
            case R.id.btn_dev_login:
                onBtnLogin();
                break;
            case R.id.btn_scan_qrcode:
                onBtnScanQrCode();
                break;
            case R.id.btn_register:
                onBtnRegister();
                break;
            case R.id.btn_search_wifi:
                onBtnSearchWifi();
                break;
            default:
                break;
        }
    }

    private void onBtnScanQrCode() {
        Intent it = new Intent(this, CaptureTDCodeUI.class);
        startActivityForResult(it, TDCodeOnClickListener.TD_CODE_REQ_ID);
    }

    private void onBtnSearchWifi() {
        searchWifi(this);
    }

    public static final int USER_REGISTER_REQ_ID = 1001;
    public static String USER_INFO_CONTENT_KEY = "user_info_content_key";
    TDCodeOnClickListener mTdCodeOnClickListener;
    public static final int WIFI_ETC_REQ_ID = 0x1010;

    private void onBtnRegister() {
        Intent it = new Intent(LoginUI2.this, UserRegisterUI2.class);
        startActivityForResult(it, USER_REGISTER_REQ_ID);
    }

    private void onBtnLogin() {
        if (Global.m_loginType == Define.LOGIN_TYPE_USER) {
            onLoginByUser();
        } else if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {
            onLoginByDevice();
        } else {
            onLoginByDemo();
        }
    }

    private void onLoginByUser() {
        if (!m_txt_user.validate()) {
            m_txt_user.setShakeAnimation();
            return;
        }

        if (!m_txt_pwd.validate()) {
            m_txt_pwd.setShakeAnimation();
            return;
        }

        mDevInfo.setUserName(m_txt_user.getText().toString());
        mDevInfo.setUserPassword(m_txt_pwd.getText().toString());
        mDevInfo.setDevIP(DEFAULT_SERVER_URL);
        mDevInfo.setDevPort(DEFAULT_SERVER_PORT);

        onLogin();
    }

    private void onLoginByDevice() {
        if (!m_txt_dev_id.validate()) {
            m_txt_dev_id.setShakeAnimation();
            return;
        }

        if (!m_txt_dev_user.validate()) {
            m_txt_dev_user.setShakeAnimation();
            return;
        }

        if (!m_txt_dev_pwd.validate()) {
            m_txt_dev_pwd.setShakeAnimation();
            return;
        }

        mDevInfo.setDevId(m_txt_dev_id.getText().toString());
        mDevInfo.setUserName(m_txt_dev_user.getText().toString());
        mDevInfo.setUserPassword(m_txt_dev_pwd.getText().toString());
        mDevInfo.setDevIP(m_txt_dev_id.getText().toString());
        mDevInfo.setDevPort(DEFAULT_SERVER_PORT);

        onLogin();
    }

    private void testOSS() {
        String bucketName = "test12222";
        //boolean b = Global.m_oss.createBucket(bucketName);
        String key = "117221-084511-vv.mp4";
        String filename = Global.getVideoDir() + "/117221/117221-084511-vv.mp4";
        File file = new File(filename);

        /*ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.length());
        // 可以在metadata中标记文件类型
        //objectMeta.setContentType("image/jpeg");

        InputStream input = null;
        try {
            input = new FileInputStream(file);
            Global.m_oss.putObject(bucketName, key, input, objectMeta);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                String bucketName = "test1222";
                //boolean b = Global.m_oss.createBucket(bucketName);
                String key = "117221-084511-vv.mp4";
                String filename = Define.VideoDirPath + "/117221/117221-084511-vv.mp4";
                File file = new File(filename);
                OSSObject obj = Global.m_oss.getObject(bucketName, key);
                return;
            }
        }).start();*/
    }

    private void onLoginByDemo() {
        Global.m_loginType = Define.LOGIN_TYPE_DEMO;
        mDevInfo.setUserName("test007");
        mDevInfo.setUserPassword("123456");
        mDevInfo.setDevIP(DEFAULT_SERVER_URL);
        mDevInfo.setDevPort(DEFAULT_SERVER_PORT);
        onLogin();
    }

    private void onLogin() {
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(T(R.string.dlg_network_check_tip));
        } else {
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

            mTipDlg.show();
            mTipDlg.setCancelable(true);

            /*FinalHttp fh = new FinalHttp();
            fh.configCharset("gb2312");//gb2312==UTF-8
            fh.configTimeout(15 * 1000);
            String url = "http://seetong.com/comm/getrand.php";
            fh.get(url, null, new AjaxCallBack<Object>() {
                @Override
                public void onSuccess(Object t) {
                    Log.d("MSG", (t == null) ? "null" : "onSuccess..." + t.toString());
                    toast((t == null) ? "null" : "onSuccess..." + t.toString());
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    Log.d("MSG", "onFailure...errorNO=" + errorNo + ",strMsg=" + strMsg);
                    toast("onFailure...errorNO=" + errorNo + ",strMsg=" + strMsg);
                }

                @Override
                public void onStart() {
                    Log.d("MSG", "onStart...");
                }

                @Override
                public void onLoading(long count, long current) {
                    Log.d("MSG", "onLoading...count=" + count + ",current=" + current);
                    toast("onLoading...count=" + count + ",current=" + current);
                }
            });*/

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceInfo devInfo = mDevInfo;
                    Global.m_devInfo = mDevInfo;
                    final int ret = LibImpl.getInstance().Login(devInfo.getUserName(), devInfo.getUserPassword(), devInfo.getDevIP(), (short) devInfo.getDevPort());
                    Log.w("Login", "user login state:" + ret);
                    if (ret != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTipDlg.dismiss();
                                //toast(SDK_CONSTANT.getTPSErrText(mLoginState, mContext));
                                if (ret == SDK_CONSTANT.ERR_DEV_LOCK) {
                                    String msg = "";
                                    if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {
                                        msg = T(R.string.ipc_err_dev_lock);
                                    } else {
                                        msg = T(R.string.ipc_err_user_lock);
                                    }

                                    MyTipDialog.popDialog(LoginUI2.this, R.string.dlg_tip, msg, R.string.close);
                                } else {
                                    MyTipDialog.popDialog(LoginUI2.this, R.string.dlg_tip, ConstantImpl.getTPSErrText(ret), R.string.close);
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public void searchWifi(final Context context) {
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

    public void loadData() {
        boolean isSaveData = Global.m_spu_login.loadBooleanSharedPreference(Define.IS_SAVE_DATA);
        if (!isSaveData) {
            saveData();
            sStr(R.id.txt_dev_user_name, "admin");
            sStr(R.id.txt_dev_password, "123456");
            return;
        }
        //user login
        //CheckBox cbSavePwd = (CheckBox) findViewById(R.id.cbSavePassword);
        //cbSavePwd.setChecked(mMySPUtil.loadBooleanSharedPreference(Define.IS_SAVE_PWD));
        sStr(R.id.txt_user_name, Global.m_spu_login.loadStringSharedPreference(Define.USR_NAME));
        sStr(R.id.txt_password, Global.m_spu_login.loadStringSharedPreference(Define.USR_PSW));
        //device login
        sStr(R.id.txt_dev_id, Global.m_spu_login.loadStringSharedPreference(Define.DEV_ID));
        //sStr(R.id.etSerPort, mMySPUtil.loadIntSharedPreference(Define.SERVER_PORT) + "");

        String devName = Global.m_spu_login.loadStringSharedPreference(Define.DEV_NAME);
        String devPwd = Global.m_spu_login.loadStringSharedPreference(Define.DEV_PSW);
        if ("".equals(devName)) devName = "admin";
        if ("".equals(devPwd)) devPwd = "123456";
        sStr(R.id.txt_dev_user_name, devName);
        sStr(R.id.txt_dev_password, devPwd);
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
            if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {//device login
                Global.m_spu_login.saveSharedPreferences(Define.DEV_ID, gStr(R.id.txt_dev_id));
                Global.m_spu_login.saveSharedPreferences(Define.SERVER_PORT, DEFAULT_SERVER_PORT);
                Global.m_spu_login.saveSharedPreferences(Define.DEV_NAME, gStr(R.id.txt_dev_user_name));
                Global.m_spu_login.saveSharedPreferences(Define.DEV_PSW, gStr(R.id.txt_dev_password));
            } else if (Global.m_loginType == Define.LOGIN_TYPE_USER) {//user login
                Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_DATA, true);
                Global.m_spu_login.saveSharedPreferences(Define.USR_NAME, gStr(R.id.txt_user_name));
                //CheckBox cbSavePwd = (CheckBox) findViewById(R.id.cbSavePassword);
                boolean isSavePwd = true;
                //isSavePwd = cbSavePwd.isChecked();
                Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_PWD, isSavePwd);
                Global.m_spu_login.saveSharedPreferences(Define.USR_PSW, (isSavePwd) ? gStr(R.id.txt_password) : "");
            }
        }
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        if (mTipDlg.isCanceled()) return;
        int msgType = msg.arg1;
        switch (msgType) {
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_OK:
                if (mTipDlg.isTimeout()) return;
                if (Global.m_loginType != Define.LOGIN_TYPE_DEMO) saveData();
                break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED: {
                mTipDlg.dismiss();
                //toast(T(R.string.dlg_login_fail_tip));
//				MyTipDialog.popDialog(mContext, R.string.dlg_tip,SDK_CONSTANT.getTPSErrText(mLoginState, mContext),R.string.close);
            }
            break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DEV_DATA:
                LibImpl.MsgObject m = (LibImpl.MsgObject) msg.obj;
                onNotifyDevData(m.recvObj);
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH:

                break;
            default:
                break;
        }
    }

    private void onNotifyDevData(Object obj) {
        mTipDlg.dismiss();
        String xml = null == obj ? "" : (String) obj;
        Intent it = new Intent(LoginUI2.this, MainActivity.class);
        String devId = getIntent().getStringExtra(MainActivity.DEVICE_ID_KEY);
        if (!TextUtils.isEmpty(devId)) it.putExtra(MainActivity.DEVICE_ID_KEY, devId);
        it.putExtra(Constant.DEVICE_INFO_KEY, mDevInfo.getDevId());
        it.putExtra(Constant.DEVICE_LIST_CONTENT_KEY, xml);
        it.putExtra(Constant.EXTRA_LOGIN_SUCCEED, 1);
        int AddLiveID = getIntent().getIntExtra(MainActivity.ADD_LIVE_KEY, 0);
        if (AddLiveID == MainActivity.ADD_LIVE_ID) {
            setResult(RESULT_OK, it);
        } else {
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //it.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            LoginUI2.this.startActivity(it);
        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mTdCodeOnClickListener != null) {
            mTdCodeOnClickListener.tdCodeRecv(requestCode, resultCode, data);
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case USER_REGISTER_REQ_ID: {
                    UserRegisterUI2.RegisterInfo regInfo = (UserRegisterUI2.RegisterInfo) data.getSerializableExtra(USER_INFO_CONTENT_KEY);
                    if (regInfo != null) {
                        sStr(R.id.txt_user_name, regInfo.userName);
                        sStr(R.id.txt_password, regInfo.userPwd);
                        toast(regInfo.userName + T(R.string.reg_error_null));
                    } else {
                        toast(R.string.reg_register_info_is_null);
                    }
                }
                break;
                case WIFI_ETC_REQ_ID:
                    String wifiSSID = data.getStringExtra(Constant.EXTRA_WIFI_SSID);
                    if (!TextUtils.isEmpty(wifiSSID) && wifiSSID.contains("camera_")) break;
                    ((EditText) findViewById(R.id.txt_dev_id)).setText(wifiSSID);
                    findViewById(R.id.btn_login_by_device).performClick();
                    break;

                default:
                    break;
            }
        }
    }
}
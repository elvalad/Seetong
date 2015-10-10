package com.seetong.app.seetong.ui;

/**
 * Created by Administrator on 2015/9/14.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import android.widget.EditText;
import com.custom.etc.EtcInfo;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.NetworkUtils;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.ext.MyTipDialog;
import com.seetong.app.seetong.ui.ext.RegexpEditText;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;

import java.util.List;

/**
 * LoginActivity 是首次进入 Seetong 后的第一个页面，用于让用户注册登录 Seetong.
 * 此页面包含几个相关的 Button，点击之后会进入不容的 Activity.
 * 需要和服务器端交互用于登录用户信息的检查，主页新的用户，忘记密码等信息.
 *
 * Created by gmk on 2015/9/11.
 */
public class LoginActivity extends BaseActivity {
    public static String TAG = LoginActivity.class.getName();
    public static final String DEFAULT_DEV_NAME = EtcInfo.DEFAULT_DEV_NAME;
    public static final String DEFAULT_DEV_PWD = EtcInfo.DEFAULT_DEV_PWD;
    public static final String DEFAULT_SERVER_URL = EtcInfo.DEFAULT_P2P_URL;
    public static final int DEFAULT_SERVER_PORT = EtcInfo.DEFAULT_SERVER_PORT;
    private ProgressDialog mTipDlg;

    private RegexpEditText m_txt_user;
    private RegexpEditText m_txt_pwd;
    private RegexpEditText m_txt_dev_id;
    private RegexpEditText m_txt_dev_user;
    private RegexpEditText m_txt_dev_pwd;

    private DeviceInfo mDevInfo = new DeviceInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        LibImpl.getInstance().init();
        initWidget();
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

    // TODO:需要和服务器交互完成用户账号密码检查，用户注册等操作
    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        m_txt_user = (RegexpEditText) findViewById(R.id.login_account);
        m_txt_user.setRequired(true);
        m_txt_user.requestFocus();
        m_txt_pwd = (RegexpEditText) findViewById(R.id.login_password);
        m_txt_pwd.setRequired(true);

        EditText passwordText = (EditText) findViewById(R.id.login_password);
        passwordText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        Button loginButton = (Button) findViewById(R.id.login_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* TODO:调试时使用默认的账号登录 */
                Global.m_loginType = Define.LOGIN_TYPE_USER;
                //Global.m_loginType = Define.LOGIN_TYPE_DEMO;
                onBtnLogin();
            }
        });
        loginButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v = findViewById(R.id.login_login);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getBackground().setAlpha(150);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        Button registerButton = (Button) findViewById(R.id.login_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        Button forgetPwdButton = (Button) findViewById(R.id.login_forget);
        forgetPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });

        loadData();
        LibImpl.getInstance().addHandler(m_handler);
        Global.m_loginType = Define.LOGIN_TYPE_USER;
        if (!"".equals(gStr(R.id.login_account)) && !"".equals(gStr(R.id.login_password))) {
            Global.m_loginType = Define.LOGIN_TYPE_USER;
            onBtnLogin();
            return;
        }
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

    private void onLoginByDemo() {
        Global.m_loginType = Define.LOGIN_TYPE_DEMO;
        mDevInfo.setUserName("gmk1");
        mDevInfo.setUserPassword("123456");
        mDevInfo.setDevIP(DEFAULT_SERVER_URL);
        mDevInfo.setDevPort(DEFAULT_SERVER_PORT);
        onLogin();
    }

    private void onLogin() {
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(T(R.string.dlg_network_check_tip));
        } else if (!com.android.utils.NetworkUtils.isConnectInternet()) {
            toast(R.string.dlg_network_connect_internet_tip);
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

            Global.m_devInfo = mDevInfo;
            Log.e(TAG, "dev info is: " + mDevInfo.getUserName());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceInfo devInfo = mDevInfo;

                    final int ret = LibImpl.getInstance().Login(devInfo.getUserName(), devInfo.getUserPassword(), devInfo.getDevIP(), (short) devInfo.getDevPort());
                    Log.v("Login", "user login state:" + ret);
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

                                    MyTipDialog.popDialog(LoginActivity.this, R.string.dlg_tip, msg, R.string.close);
                                } else {
                                    MyTipDialog.popDialog(LoginActivity.this, R.string.dlg_tip, ConstantImpl.getTPSErrText(ret), R.string.close);
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        if (mTipDlg.isCanceled()) return;
        int msgType = msg.arg1;
        switch (msgType) {
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_OK:
                if (mTipDlg.isTimeout()) return;
                saveData();
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

    public void loadData() {
        boolean isSaveData = Global.m_spu_login.loadBooleanSharedPreference(Define.IS_SAVE_DATA);
        if (!isSaveData) {
            saveData();
            //sStr(R.id.txt_dev_user_name, "admin");
            //sStr(R.id.txt_dev_password, "123456");
            return;
        }
        //user login
        //CheckBox cbSavePwd = (CheckBox) findViewById(R.id.cbSavePassword);
        //cbSavePwd.setChecked(mMySPUtil.loadBooleanSharedPreference(Define.IS_SAVE_PWD));
        sStr(R.id.login_account, Global.m_spu_login.loadStringSharedPreference(Define.USR_NAME));
        sStr(R.id.login_password, Global.m_spu_login.loadStringSharedPreference(Define.USR_PSW));
        //device login
        //sStr(R.id.txt_dev_id, Global.m_spu_login.loadStringSharedPreference(Define.DEV_ID));
        //sStr(R.id.etSerPort, mMySPUtil.loadIntSharedPreference(Define.SERVER_PORT) + "");

        //String devName = Global.m_spu_login.loadStringSharedPreference(Define.DEV_NAME);
        //String devPwd = Global.m_spu_login.loadStringSharedPreference(Define.DEV_PSW);
        //if ("".equals(devName)) devName = "admin";
        //if ("".equals(devPwd)) devPwd = "123456";
        //sStr(R.id.txt_dev_user_name, devName);
        //sStr(R.id.txt_dev_password, devPwd);
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
                Global.m_spu_login.saveSharedPreferences(Define.USR_NAME, gStr(R.id.login_account));
                //CheckBox cbSavePwd = (CheckBox) findViewById(R.id.cbSavePassword);
                boolean isSavePwd = true;
                //isSavePwd = cbSavePwd.isChecked();
                Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_PWD, isSavePwd);
                Global.m_spu_login.saveSharedPreferences(Define.USR_PSW, (isSavePwd) ? gStr(R.id.login_password) : "");
            }
        }
    }

    private void onNotifyDevData(Object obj) {
        mTipDlg.dismiss();
        String xml = null == obj ? "" : (String) obj;
        Intent it = new Intent(LoginActivity.this, MainActivity2.class);
        String devId = getIntent().getStringExtra(MainActivity2.DEVICE_ID_KEY);
        if (!TextUtils.isEmpty(devId)) it.putExtra(MainActivity2.DEVICE_ID_KEY, devId);
        it.putExtra(Constant.DEVICE_INFO_KEY, mDevInfo.getDevId());
        it.putExtra(Constant.DEVICE_LIST_CONTENT_KEY, xml);
        it.putExtra(Constant.EXTRA_LOGIN_SUCCEED, 1);
        int AddLiveID = getIntent().getIntExtra(MainActivity2.ADD_LIVE_KEY, 0);
        if (AddLiveID == MainActivity2.ADD_LIVE_ID) {
            setResult(Activity.RESULT_OK, it);
        } else {
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //it.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            LoginActivity.this.startActivity(it);
        }

        finish();
    }
}


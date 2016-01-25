package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.text.TextUtils;
import com.custom.etc.EtcInfo;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.NetworkUtils;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;

/**
 * Created by Administrator on 2016/1/7.
 */
public class WelcomeActivity extends BaseActivity {
    private static final int GO_TO_LOGIN_ACTIVITY = 9000;
    private DeviceInfo mDevInfo = new DeviceInfo();
    private String userName = "";
    private String userPwd = "";
    private boolean bTimeOut = true;
    private Thread timeoutThread;
    private Thread loginThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        LibImpl.getInstance().init();
        initWidget();
        timeoutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (bTimeOut) {
                        if ((null !=  loginThread) && loginThread.isAlive()) {
                            loginThread.interrupt();
                        }
                        sendMessage(0, GO_TO_LOGIN_ACTIVITY, 0, null);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timeoutThread.start();
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
        if ((null !=  loginThread) && loginThread.isAlive()) {
            loginThread.interrupt();
        }

        if ((null != timeoutThread) && timeoutThread.isAlive()) {
            timeoutThread.interrupt();
        }
    }

    private void initWidget() {
        LibImpl.getInstance().addHandler(m_handler);
        Global.m_loginType = Define.LOGIN_TYPE_USER;
        userName = Global.m_spu_login.loadStringSharedPreference(Define.USR_NAME);
        userPwd = Global.m_spu_login.loadStringSharedPreference(Define.USR_PSW);
        if ((null != userName) && (null != userPwd) && (!"".equals(userName)) && (!"".equals(userPwd))) {
            onDefaultLogin();
        } else {
            Message msg = Message.obtain();
            msg.arg1 = GO_TO_LOGIN_ACTIVITY;
            m_handler.sendMessageDelayed(msg, 3000);
        }
    }

    private void onDefaultLogin() {
        mDevInfo.setUserName(userName);
        mDevInfo.setUserPassword(userPwd);
        mDevInfo.setDevIP(EtcInfo.DEFAULT_P2P_URL);
        mDevInfo.setDevPort(EtcInfo.DEFAULT_SERVER_PORT);

        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            sendMessage(0, GO_TO_LOGIN_ACTIVITY, 0, null);
        } else {
            Global.m_devInfo = mDevInfo;
            loginThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceInfo devInfo = mDevInfo;
                    final int ret = LibImpl.getInstance().Login(devInfo.getUserName(), devInfo.getUserPassword(), devInfo.getDevIP(), (short) devInfo.getDevPort());
                    bTimeOut = false;
                    if ((null != timeoutThread) && timeoutThread.isAlive()) {
                        timeoutThread.interrupt();
                    }
                    if (ret != 0) {
                        sendMessage(0, GO_TO_LOGIN_ACTIVITY, 0, null);
                    }
                }
            });
            loginThread.start();
        }
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        android.os.Message msg = m_handler.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        m_handler.sendMessage(msg);
    }

    @Override
    protected void handleMessage(android.os.Message msg) {
        int msgType = msg.arg1;
        switch (msgType) {
            case GO_TO_LOGIN_ACTIVITY:
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DEV_DATA:
                LibImpl.MsgObject m = (LibImpl.MsgObject) msg.obj;
                onNotifyDevData(m.recvObj);
                break;
            default:
                break;
        }
    }

    private void onNotifyDevData(Object obj) {
        String xml = null == obj ? "" : (String) obj;
        Intent it = new Intent(WelcomeActivity.this, MainActivity2.class);
        String devId = getIntent().getStringExtra(MainActivity2.DEVICE_ID_KEY);
        if (!TextUtils.isEmpty(devId)) it.putExtra(MainActivity2.DEVICE_ID_KEY, devId);
        it.putExtra(Constant.DEVICE_INFO_KEY, mDevInfo.getDevId());
        it.putExtra(Constant.DEVICE_LIST_CONTENT_KEY, xml);
        it.putExtra(Constant.EXTRA_LOGIN_SUCCEED, 1);
        int AddLiveID = getIntent().getIntExtra(MainActivity2.ADD_LIVE_KEY, 0);
        if (AddLiveID == MainActivity2.ADD_LIVE_ID) {
            setResult(RESULT_OK, it);
        } else {
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            WelcomeActivity.this.startActivity(it);
        }

        finish();
    }
}

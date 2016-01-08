package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.comm.NetworkUtils;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;

/**
 * Created by Administrator on 2016/1/7.
 */
public class WelcomeActivity extends BaseActivity {
    private static final int GO_TO_LOGIN_ACTIVITY = 9000;
    private DeviceInfo mDevInfo = new DeviceInfo();
    private String userName = null;
    private String userPwd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        LibImpl.getInstance().init();
        LibImpl.getInstance().addHandler(m_handler);
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

    private void initWidget() {
        Global.m_loginType = Define.LOGIN_TYPE_USER;
        userName = Global.m_spu_login.loadStringSharedPreference(Define.USR_NAME);
        userPwd = Global.m_spu_login.loadStringSharedPreference(Define.USR_PSW);
        if ((!"".equals(userName)) && (!"".equals(userPwd))) {
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceInfo devInfo = mDevInfo;
                    final int ret = LibImpl.getInstance().Login(devInfo.getUserName(), devInfo.getUserPassword(), devInfo.getDevIP(), (short) devInfo.getDevPort());
                    if (ret != 0) {
                        sendMessage(0, GO_TO_LOGIN_ACTIVITY, 0, null);
                    }
                }
            }).start();
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

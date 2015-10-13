package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.utils.NetworkUtils;
import com.android.zxing.ui.CaptureTDCodeUI;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.aid.TDCodeOnClickListener;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;

/**
 * @author qinglei.yin@192.168.88.9<br>
 *         2014-3-26 上午10:22:32<br>
 * @declaration 划分设备到用户<br>
 */
public class AddDeviceUI extends BaseActivity implements OnClickListener {
    public static final String DEFAULT_DEV_NAME = EtcInfo.DEFAULT_DEV_NAME;
    public static final String DEFAULT_DEV_PWD = EtcInfo.DEFAULT_DEV_PWD;
    public static final String DEFAULT_SERVER_URL = EtcInfo.DEFAULT_P2P_URL;
    public static final int DEFAULT_SERVER_PORT = EtcInfo.DEFAULT_SERVER_PORT;

    /**
     * enter types's key-->0:first 1:add 2:edit
     */
    public static String ENTER_TYPES = "enter_types";
    /**
     * enter types's value-->0:first 1:add 2:edit
     */
    int mEnterTypes = 0; //0:first 1:add 2:edit
    Button mbtnLoginDevice;
    private ProgressDialog mTipDlg;
    private DeviceInfo mDevInfo = new DeviceInfo();;
    private int mAddState = 0;//0:success other:SDK_CONSTANT@TPS_ERR_NUM
    private int mLoginState = 0;

    public static MyHandler mhHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mhHandler = new MyHandler(this);
        setContentView(R.layout.add_device_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_add_device_title));
        initWidget();
    }

    public void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        mEnterTypes = getIntent().getIntExtra(ENTER_TYPES, 0);
        TextView tvTitle = ((TextView) findViewById(R.id.tvTitle));
        if (mEnterTypes == 0) {//save
            tvTitle.setText(mResources.getString(R.string.tv_device_info_title));
        } else if (mEnterTypes == 1) {//add
            tvTitle.setText(mResources.getString(R.string.tv_add_device_title));
        } else {//edit
            tvTitle.setText(mResources.getString(R.string.tv_edit_device_title));
        }

        mbtnLoginDevice = (Button) findViewById(R.id.btnAddDevice);
        mbtnLoginDevice.setOnClickListener(this);
        mbtnLoginDevice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sStr(R.id.etWifiName, "100134");
                sStr(R.id.etSerPort, DEFAULT_SERVER_PORT + "");
                sStr(R.id.etDevName, DEFAULT_DEV_NAME);
                sStr(R.id.etWifiPwd, DEFAULT_DEV_PWD);
                return true;
            }
        });
        addContextAgent();
        sStr(R.id.etDevName, DEFAULT_DEV_NAME);
        sStr(R.id.etWifiPwd, DEFAULT_DEV_PWD);

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
    }

    protected void addContextAgent() {
        //super.addContextAgent();
        LibImpl.getInstance().addHandler(mhHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(mhHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LibImpl.getInstance().removeHandler(mhHandler);
    }

    public boolean getFormData() {
        boolean isOK = false;
        if (isNullStr(gStr(R.id.etWifiName))) {
            toast(T(R.string.dlg_input_ok_server_address_tip));
        } else if (isNullStr(gStr(R.id.etDevName))) {
            toast(T(R.string.dlg_input_ok_dev_user_tip));
        } else if (isNullStr(gStr(R.id.etWifiPwd))) {
            toast(T(R.string.dlg_input_ok_dev_password_tip));
        } else {
            mDevInfo.setDevId(gStr(R.id.etWifiName));
            mDevInfo.setDevIP(gStr(R.id.etWifiName));
            mDevInfo.setDevPort(DEFAULT_SERVER_PORT);
            mDevInfo.setUserName(gStr(R.id.etDevName));
            mDevInfo.setUserPassword(gStr(R.id.etWifiPwd));

            //判断设备ID是否存在
            if (LibImpl.getInstance().isExistDeviceID(mDevInfo.getDevId().trim())) {
                toast(mDevInfo.getDevId().trim() + T(R.string.ad_error_dev_exist));
                return false;
            }

            if (!Global.getDeviceByName(mDevInfo.getDevId().trim()).isEmpty()) {
                toast(mDevInfo.getDevId().trim() + T(R.string.ad_error_dev_exist));
                return false;
            }

            isOK = true;
        }
        return isOK;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreStepTo1:
                onBtnScanQrCode();
                break;
            case R.id.btnAddDevice:
                loginCallback();
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
                        toast(R.string.ad_error_timeout);
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
                        Global.m_devInfoList.add(mDevInfo);
                        if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {
                            mLoginState = LibImpl.getInstance().Login(devInfo.getUserName(), devInfo
                                    .getUserPassword(), devInfo.getDevIP(), (short) devInfo.getDevPort());
                            Log.w("Login", "device login state:" + mLoginState);
                            if (mLoginState != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTipDlg.dismiss();
                                        //sendMyToast(SDK_CONSTANT.getTPSErrText(mLoginState, mContext));
                                        MyTipDialog.popDialog(AddDeviceUI.this, R.string.dlg_tip, ConstantImpl.getTPSErrText(mLoginState), R.string.close);
                                    }
                                });
                            }
                        } else if (Global.m_loginType == Define.LOGIN_TYPE_USER) {
                            mAddState = LibImpl.getInstance().getFuncLib().AddDeviceAgent(devInfo.getDevId(), devInfo.getUserName(), devInfo.getUserPassword());
                            Log.w("AddDevice", "AddDevice state:" + mAddState);
                            if (mAddState != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTipDlg.dismiss();
                                        //sendMyToast(SDK_CONSTANT.getTPSErrText(mLoginState, mContext));
                                        MyTipDialog.popDialog(AddDeviceUI.this, R.string.dlg_tip, ConstantImpl.getAddDevErrText(mAddState, false), R.string.close);
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        }
    }

    static class MyHandler extends Handler {
        AddDeviceUI m_ui;
        public MyHandler(AddDeviceUI ui) {
            m_ui = ui;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            int msgType = msg.arg1;
            switch (msgType) {
                case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_OK:
                    if (m_ui.mTipDlg.isTimeout()) return;
                    break;
                case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED:
                    m_ui.mTipDlg.dismiss();
                    break;
                case SDK_CONSTANT.TPS_MSG_NOTIFY_DEV_DATA:
                    if (m_ui.mTipDlg.isTimeout()) return;
                    m_ui.mTipDlg.dismiss();
                    if ((msg.obj != null) && (msg.obj instanceof LibImpl.MsgObject)) {
                        String xml = (String) ((LibImpl.MsgObject) msg.obj).recvObj;
                        Intent it = new Intent(m_ui, DeviceFragment.class);
                        it.putExtra(Constant.DEVICE_INFO_KEY, m_ui.mDevInfo.getDevId());
                        it.putExtra(Constant.DEVICE_LIST_CONTENT_KEY, xml);
                        m_ui.setResult(RESULT_OK, it);
                        m_ui.finish();
                    } else {
                        m_ui.toast(T(R.string.dlg_login_recv_list_fail_tip));
                    }
                    break;
                case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH:

                    break;
                default:
                    break;
            }
        };
    };

    TDCodeOnClickListener mTdCodeOnClickListener;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mTdCodeOnClickListener != null) {
            mTdCodeOnClickListener.tdCodeRecv(requestCode, resultCode, data);
        }
    }
}

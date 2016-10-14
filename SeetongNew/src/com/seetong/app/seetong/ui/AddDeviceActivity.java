package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import com.android.zxing.ui.CaptureTDCodeUI;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.NetworkUtils;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.ui.aid.TDCodeOnClickListener;
import com.seetong.app.seetong.ui.ext.MyTipDialog;
import com.seetong.app.seetong.ui.ext.RegexpEditText;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.DeviceInfo;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

/**
 * Created by gmk on 2015/9/21.
 */
public class AddDeviceActivity extends BaseActivity {
    private static String TAG = AddDeviceActivity.class.getName();

    private RegexpEditText addDeviceId;
    private RegexpEditText addDeviceAccount;
    private RegexpEditText addDevicePassword;
    private Button scanQRCodeButton;
    private Button addDeviceButton;
    private Button wifiCfgButton;
    private Button lanSearchButton;
    private ImageButton addDeviceBackButton;

    public static MyHandler mhHandler;
    private ProgressDialog mTipDlg;
    private int mAddState = 0;
    private DeviceInfo mDevInfo = new DeviceInfo();

    public static final int WIFI_ETC_REQ_ID = 0x1010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.i(AddDeviceActivity.class.getName(), "onCreate...");
        super.onCreate(savedInstanceState);
        mhHandler = new MyHandler(this);
        setContentView(R.layout.activity_add_device);
        initWidget();
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

    protected void addContextAgent() {
        LibImpl.getInstance().addHandler(mhHandler);
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        addDeviceBackButton = (ImageButton) findViewById(R.id.add_device_back);
        addDeviceBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddDeviceActivity.this.finish();
            }
        });

        addDeviceId = (RegexpEditText) findViewById(R.id.device_add_id);
        addDeviceId.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        addDeviceId.requestFocus();
        addDeviceId.setFocusableInTouchMode(true);
        addDeviceId.setRequired(true);

        addDeviceAccount = (RegexpEditText) findViewById(R.id.device_add_account);
        addDeviceAccount.setRegexp("\\w*");
        addDeviceAccount.setRequired(true);

        addDevicePassword = (RegexpEditText) findViewById(R.id.device_add_password);
        addDevicePassword.setRegexp("\\w*");
        addDevicePassword.setRequired(true);
        addDevicePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

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
                sStr(R.id.device_add_id, devInfo.getDevId());
                sStr(R.id.device_add_account, devInfo.getUserName());
                sStr(R.id.device_add_password, devInfo.getUserPassword());

                onDeviceAdd();
            }
        });

        scanQRCodeButton = (Button) findViewById(R.id.device_add_scan);
        scanQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanQRCode();
            }
        });
        scanQRCodeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view = findViewById(R.id.device_add_scan);
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.getBackground().setAlpha(150);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        addDeviceButton = (Button) findViewById(R.id.device_add_add);
        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeviceAdd();
            }
        });
        addDeviceButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view = findViewById(R.id.device_add_add);
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.getBackground().setAlpha(150);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        wifiCfgButton = (Button) findViewById(R.id.device_wifi_cfg);
        wifiCfgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchWifi();
            }
        });

        lanSearchButton = (Button) findViewById(R.id.device_lan_search);
        lanSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanSearchDevice();
            }
        });

        addContextAgent();
    }

    private void onScanQRCode() {
        Intent it = new Intent(this, CaptureTDCodeUI.class);
        startActivityForResult(it, TDCodeOnClickListener.TD_CODE_REQ_ID);
    }

    private void onDeviceAdd() {
        if (!addDeviceId.validate()) {
            addDeviceId.setShakeAnimation();
            return;
        }

        if (!addDeviceAccount.validate()) {
            addDeviceAccount.setShakeAnimation();
            return;
        }

        if (!addDevicePassword.validate()) {
            addDevicePassword.setShakeAnimation();
            return;
        }

        mDevInfo.setDevId(addDeviceId.getText().toString());
        mDevInfo.setUserName(addDeviceAccount.getText().toString());
        mDevInfo.setUserPassword(addDevicePassword.getText().toString());

        if (LibImpl.getInstance().isExistDeviceID(mDevInfo.getDevId().trim())) {
            toast(mDevInfo.getDevId().trim() + T(R.string.ad_error_dev_exist));
            return;
        }

        if (!Global.getDeviceByName(mDevInfo.getDevId().trim()).isEmpty()) {
            toast(mDevInfo.getDevId().trim() + T(R.string.ad_error_dev_exist));
            return;
        }

        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(T(R.string.dlg_network_check_tip));
        } else {
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
                    try {
                        Thread.sleep(3000);
                        Global.m_devInfoList.add(mDevInfo);
                        if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {

                        } else if (Global.m_loginType == Define.LOGIN_TYPE_USER) {
                            mAddState = LibImpl.getInstance().getFuncLib().AddDeviceAgent(mDevInfo.getDevId(), mDevInfo.getUserName(), mDevInfo.getUserPassword());
                            if (0 != mAddState) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTipDlg.dismiss();
                                        MyTipDialog.popDialog(AddDeviceActivity.this, R.string.dlg_tip, ConstantImpl.getAddDevErrText(mAddState, false), R.string.close);
                                    }
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    static class MyHandler extends Handler {
        AddDeviceActivity m_ui;
        public MyHandler(AddDeviceActivity ui) {
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
                        m_ui.hideInputPanel(null);
                        String xml = (String) ((LibImpl.MsgObject) msg.obj).recvObj;
                        Intent it = new Intent(m_ui, DeviceFragment2.class);
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
        }
    }

    TDCodeOnClickListener mTdCodeOnClickListener;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mTdCodeOnClickListener != null) {
            mTdCodeOnClickListener.tdCodeRecv(requestCode, resultCode, data);
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WIFI_ETC_REQ_ID:
                    String wifiSSID = data.getStringExtra(Constant.EXTRA_WIFI_SSID);
                    sStr(R.id.device_add_id, wifiSSID);
                    sStr(R.id.device_add_account, "admin");
                    sStr(R.id.device_add_password, "123456");
                    onDeviceAdd();
                    break;
            }
        }
    }

    public void searchWifi() {
        int isOK = NetworkUtils.getNetworkState(this);
        if (isOK == NetworkUtils.WIFI) {
            final ProgressDialog tipDlg = new ProgressDialog(this, R.string.tip_wifi_connect_internet_check);
            tipDlg.setCancelable(false);

            FinalHttp fh = new FinalHttp();
            String url = "http://www.baidu.com";
            fh.configTimeout(10 * 1000);
            fh.get(url, new AjaxCallBack<Object>() {
                @Override
                public void onSuccess(Object t) {
                    tipDlg.dismiss();
                    Log.e("MSG", (t == null) ? "null" : "onSuccess..." + t.toString());
                    Intent it = new Intent(AddDeviceActivity.this, WifiEtcUI.class);
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

    private void lanSearchDevice() {
        Intent intent = new Intent(AddDeviceActivity.this, LanSearchActivity.class);
        startActivity(intent);
    }
}

package com.seetong.app.seetong.ui;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.LanDeviceInfo;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.NetSDK_IPC_ENTRY;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/14.
 */
public class LanSearchActivity extends BaseActivity {

    private ImageButton addLanDevBtn;
    private ProgressDialog mTipDlg;
    private ListView lanDevList;
    private LanSearchListAdapter adapter;
    private List<LanDeviceInfo> data = new ArrayList<>();
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_search);
        initWidget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        data.clear();
        Global.clearLanSearchList();
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_recv_list_tip);
        mTipDlg.setCancelable(false);
        wifiManager =(WifiManager) getSystemService(Service.WIFI_SERVICE);
        LibImpl.getInstance().getFuncLib().StartSearchDev();
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                LibImpl.getInstance().getFuncLib().StopSearchDev();
                sendMessage(Define.MSG_UPDATE_LAN_SEARCH_DEV_LIST, 0, 0, null);
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });
        mTipDlg.show(15000);

        lanDevList = (ListView) findViewById(R.id.lanSearchList);
        getData();
        adapter = new LanSearchListAdapter(LanSearchActivity.this, data);
        lanDevList.setAdapter(adapter);

        addLanDevBtn = (ImageButton) findViewById(R.id.lan_device_add);
        addLanDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTipDlg.show();
                mTipDlg.setTitle(R.string.lan_search_add_dev);
                addLanDevList();
            }
        });
    }

    private void getData() {
        data.clear();
        for (LanDeviceInfo devInfo : Global.getLanSearchList()) {
            data.add(devInfo);
        }
    }

    private void addLanDevList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for (LanDeviceInfo info : data) {
                    if (!info.getChecked()) {
                        i++;
                    }

                    if (info.getChecked() && info.getEntry().getCloudId().equals("")) {
                        // 修改云ID为空的设备
                        mTipDlg.dismiss();
                        toast(R.string.lan_choose_get_cloud_id);
                        return;
                    }
                }

                if (i == data.size()) {
                    mTipDlg.dismiss();
                    toast(R.string.lan_choose_valid_dev);
                    return;
                }

                for (LanDeviceInfo info : data) {
                    if (info.getChecked() && !info.getEntry().getCloudId().equals("")) {
                        try {
                            Thread.sleep(1000);
                            final int addDevRet = LibImpl.getInstance().getFuncLib().AddDeviceAgent(info.getEntry().getCloudId(),
                                    info.getEntry().getUserCfg().getAccounts()[0].getUserName(),
                                    info.getEntry().getUserCfg().getAccounts()[0].getPassword());
                            if (0 != addDevRet) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTipDlg.dismiss();
                                        MyTipDialog.popDialog(LanSearchActivity.this, R.string.dlg_tip, ConstantImpl.getAddDevErrText(addDevRet, false), R.string.close);
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            mTipDlg.dismiss();
                            e.printStackTrace();
                            return;
                        }
                    }
                }
                mTipDlg.dismiss();
                toast(R.string.lan_search_add_success);
                finish();
            }
        }).start();
    }

    public void showGetIdTipDialog(final NetSDK_IPC_ENTRY entry, final long index) {
        final String ipAddr = entry.getLanCfg().getIPAddress();
        String cloudId = entry.getCloudId();
        if (cloudId.equals("")) {
            toast(R.string.lan_search_cloud_id_null);
            return;
        }

        MyTipDialog.popDialog(this, R.string.lan_search_modify_dev_ip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        toast("ip : " + ipAddr);
                        mTipDlg.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int ret = LibImpl.getInstance().getFuncLib().ModifyIPC((int)index,
                                        entry.objectToByteBuffer(ByteOrder.nativeOrder()).array());
                                if (0 != ret) {
                                    toast(R.string.lan_search_modify_dev_ip_err);
                                    return;
                                }

                                try {
                                    Thread.sleep(1000);
                                    wifiManager.setWifiEnabled(false);
                                    Thread.sleep(1000);
                                    wifiManager.setWifiEnabled(true);
                                    Global.clearLanSearchList();
                                    LibImpl.getInstance().getFuncLib().StartSearchDev();
                                    Thread.sleep(30000);
                                    LibImpl.getInstance().getFuncLib().StopSearchDev();
                                    sendMessage(Define.MSG_UPDATE_LAN_SEARCH_DEV_LIST, 0, 0, null);
                                    mTipDlg.dismiss();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
        );
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
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_LAN_SEARCH_DEV_LIST:
                getData();
                adapter.notifyDataSetChanged();
                break;
        }
    }
}

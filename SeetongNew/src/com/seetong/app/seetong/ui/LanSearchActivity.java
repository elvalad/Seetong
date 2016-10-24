package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.LanDeviceInfo;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.NetSDK_IPC_ENTRY;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_search);
        initWidget();
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_recv_list_tip);
        mTipDlg.setCancelable(false);
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
                //mTipDlg.show();
                adapter.addLanDevList();
            }
        });
    }

    private void getData() {
        data.clear();

        for (NetSDK_IPC_ENTRY entry : Global.getLanSearchList()) {
            LanDeviceInfo devInfo = new LanDeviceInfo();
            devInfo.setEntry(entry);
            Log.e("DDD", "user : " + devInfo.getEntry().getUserCfg().getAccounts()[0].getUserName()
                    + " pass : " + devInfo.getEntry().getUserCfg().getAccounts()[0].getPassword());
            data.add(devInfo);
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
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_LAN_SEARCH_DEV_LIST:
                getData();
                adapter.notifyDataSetChanged();
                break;
        }
    }
}

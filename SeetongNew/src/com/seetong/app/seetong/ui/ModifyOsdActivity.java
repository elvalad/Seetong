package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.NetSDK_Media_Video_Config;
import ipc.android.sdk.impl.FunclibAgent;

/**
 * Created by Administrator on 2016/9/12.
 */
public class ModifyOsdActivity extends BaseActivity implements View.OnClickListener {
    String m_device_id;
    NetSDK_Media_Video_Config m_video_config;
    NetSDK_Media_Video_Config m_new_video_config;

    private ProgressDialog mTipDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_osd);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_modify_osd));
        initWidget();
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);

        Button btnFinish = (Button) findViewById(R.id.btnRight);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().setMediaParamHandler(m_handler);
        loadData();
    }

    public void loadData() {
        String devId = m_device_id.substring(0, m_device_id.indexOf("-CH"));
        int ret = FunclibAgent.getInstance().GetP2PDevConfig(m_device_id, 501);
        if (0 != ret) {
            toast(R.string.dlg_get_media_param_fail_tip);
            finish();
            return;
        }

        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                finish();
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });
        showTipDlg(R.string.dlg_get_media_param_tip, 20000, R.string.dlg_get_media_param_timeout_tip);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    @Override
    public void onClick(View v) {

    }
}

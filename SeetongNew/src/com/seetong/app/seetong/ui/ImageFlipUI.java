package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.NetSDK_Media_Video_Config;
import ipc.android.sdk.impl.FunclibAgent;

public class ImageFlipUI extends BaseActivity implements View.OnClickListener {
    String m_device_id;
    NetSDK_Media_Video_Config m_video_config;
    NetSDK_Media_Video_Config m_new_video_config;

    private ProgressDialog mTipDlg;
    ToggleButton m_tb_h_flip;
    ToggleButton m_tb_v_flip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_flip_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.image_flip));
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);
        m_tb_h_flip = (ToggleButton) findViewById(R.id.tb_image_h_flip);
        m_tb_v_flip = (ToggleButton) findViewById(R.id.tb_image_v_flip);

        Button btnFinish = (Button) findViewById(R.id.btnRight);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().setMediaParamHandler(m_handler);
        loadData();
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData() {
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

    public void saveData() {
        m_new_video_config = (NetSDK_Media_Video_Config) m_video_config.clone();
        m_new_video_config.capture.HFlip = m_tb_h_flip.isChecked() ? "1" : "0";
        m_new_video_config.capture.VFlip = m_tb_v_flip.isChecked() ? "1" : "0";
        m_new_video_config.addHead(false);
        String xml = m_new_video_config.getCaptureXMLString();
        int ret = FunclibAgent.getInstance().SetP2PDevConfig(m_device_id, 524, xml);
        if (0 != ret) {
            toast(R.string.dlg_set_video_capture_param_fail_tip);
            return;
        }

        mTipDlg.setCallback(null);
        showTipDlg(R.string.dlg_set_video_capture_param_tip, 20000, R.string.dlg_set_video_capture_param_timeout_tip);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                onBtnFinish();
                break;
            default: break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().setMediaParamHandler(m_handler);
    }

    private void onBtnFinish() {
        saveData();
    }

    @Override
    public void handleMessage(Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case 501: // 读取视频参数配置
                NetSDK_Media_Video_Config cfg = (NetSDK_Media_Video_Config) msg.obj;
                onGetVideoParam(flag, cfg);
                break;
            case 524: // 设置视频参数配置
                onSetVideoParam(flag);
                break;
        }
    }

    private void onGetVideoParam(int flag, NetSDK_Media_Video_Config cfg) {
        mTipDlg.dismiss();
        if (flag != 0 || null == cfg) {
            toast(R.string.dlg_get_media_param_fail_tip);
            finish();
            return;
        }

        m_video_config = cfg;
        if (cfg.encode.EncodeList.size() < 2) {
            toast(R.string.dlg_get_media_param_format_incorrect_tip);
            return;
        }

        // 根据获取到的数据设置界面
        m_tb_h_flip.setChecked(1 == Integer.parseInt(m_video_config.capture.HFlip));
        m_tb_v_flip.setChecked(1 == Integer.parseInt(m_video_config.capture.VFlip));
    }

    private void onSetVideoParam(int flag) {
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.dlg_set_video_capture_param_fail_tip);
        } else {
            m_video_config = m_new_video_config;
            toast(R.string.dlg_set_video_capture_param_succeed_tip);
            finish();
        }
    }
}
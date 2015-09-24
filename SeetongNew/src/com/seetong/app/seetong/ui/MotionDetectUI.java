package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.NetSDK_Alarm_Config;
import ipc.android.sdk.impl.FunclibAgent;

public class MotionDetectUI extends BaseActivity implements View.OnClickListener {
    String m_device_id;
    NetSDK_Alarm_Config m_alarm_config;
    NetSDK_Alarm_Config m_new_alarm_config;

    private ProgressDialog mTipDlg;
    ToggleButton m_tb_motion_detect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_detect_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.motion_detect));
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);
        m_tb_motion_detect = (ToggleButton) findViewById(R.id.tb_motion_detect);

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
        int ret = FunclibAgent.getInstance().GetP2PDevConfig(m_device_id, 802);
        if (0 != ret) {
            toast(R.string.dlg_get_motion_detect_alarm_param_fail_tip);
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
        showTipDlg(R.string.dlg_get_motion_detect_alarm_param_tip, 20000, R.string.dlg_get_motion_detect_alarm_param_timeout_tip);
    }

    public void saveData() {
        m_new_alarm_config = (NetSDK_Alarm_Config) m_alarm_config.clone();
        m_new_alarm_config.motionDetectAlarm.Enable = m_tb_motion_detect.isChecked() ? "1" : "0";
        m_new_alarm_config.addHead(false);
        String xml = m_new_alarm_config.getMotionDetectAlarmXMLString();
        int ret = FunclibAgent.getInstance().SetP2PDevConfig(m_device_id, 822, xml);
        if (0 != ret) {
            toast(R.string.dlg_set_motion_detect_alarm_param_fail_tip);
            return;
        }

        mTipDlg.setCallback(null);
        showTipDlg(R.string.dlg_set_motion_detect_alarm_param_tip, 20000, R.string.dlg_set_motion_detect_alarm_param_timeout_tip);
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
            case 802: // 读取移动侦测告警参数
                NetSDK_Alarm_Config cfg = (NetSDK_Alarm_Config) msg.obj;
                onGetMotionDetectAlarm(flag, cfg);
                break;
            case 822: // 修改移动侦测告警参数
                onSetMotionDetectAlarm(flag);
                break;
        }
    }

    private void onGetMotionDetectAlarm(int flag, NetSDK_Alarm_Config cfg) {
        mTipDlg.dismiss();
        if (flag != 0 || null == cfg) {
            toast(R.string.dlg_get_motion_detect_alarm_param_fail_tip);
            finish();
            return;
        }

        m_alarm_config = cfg;

        // 根据获取到的数据设置界面
        m_tb_motion_detect.setChecked(1 == Integer.parseInt(m_alarm_config.motionDetectAlarm.Enable));
    }

    private void onSetMotionDetectAlarm(int flag) {
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.dlg_set_motion_detect_alarm_param_fail_tip);
        } else {
            m_alarm_config = m_new_alarm_config;
            toast(R.string.dlg_set_motion_detect_alarm_param_succeed_tip);
            finish();
        }
    }
}
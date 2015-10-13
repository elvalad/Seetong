package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.model.DeviceSetting;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;

public class PlaySettingUI extends BaseActivity implements View.OnClickListener {
    String m_device_id;

    private ProgressDialog mTipDlg;
    ToggleButton m_tb_force_forward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_setting_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.title_play_setting));
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, "");
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);
        m_tb_force_forward = (ToggleButton) findViewById(R.id.tb_force_forward);

        Button btnFinish = (Button) findViewById(R.id.btnRight);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);
        loadData();
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData() {
        DeviceSetting ds = DeviceSetting.findByDeviceId(m_device_id);
        if (null == ds) return;
        m_tb_force_forward.setChecked(ds.is_force_forward());
    }

    public void saveData() {
        DeviceSetting ds = DeviceSetting.findByDeviceId(m_device_id);
        if (null == ds) {
            ds = new DeviceSetting();
            ds.setDevId(m_device_id);
            ds.setEnableAlarm(false);
            ds.set_enable_push_msg(false);
        }

        ds.set_force_forward(m_tb_force_forward.isChecked());
        ds.save();
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;
        dev.m_force_forward = ds.is_force_forward();
        finish();
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
    }
}
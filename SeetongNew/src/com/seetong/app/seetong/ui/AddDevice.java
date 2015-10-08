package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.View;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Device;
import com.seetong.app.seetong.ui.ext.IntegerEditText;
import com.seetong.app.seetong.ui.ext.RegexpEditText;

/**
 * Created by Administrator on 2014-07-18.
 */
public class AddDevice extends BaseActivity implements View.OnClickListener {
    RegexpEditText m_txtIp;
    IntegerEditText m_txtPtzPort;
    IntegerEditText m_txtVideoPort;
    RegexpEditText m_txtUserName;
    RegexpEditText m_txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device);
        findViewById(R.id.btn_title_left).setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        m_txtIp = (RegexpEditText) findViewById(R.id.txt_ip);
        m_txtIp.setRequired(true);
        m_txtIp.setRegexp("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
        m_txtPtzPort = (IntegerEditText) findViewById(R.id.txt_ptz_port);
        m_txtPtzPort.setRange(0, 65535);
        m_txtVideoPort = (IntegerEditText) findViewById(R.id.txt_video_port);
        m_txtVideoPort.setRange(0, 65535);
        m_txtUserName = (RegexpEditText) findViewById(R.id.txt_user_name);
        m_txtUserName.setRequired(true);
        m_txtUserName.setRegexp("\\w*");
        m_txtPassword = (RegexpEditText) findViewById(R.id.txt_password);
        m_txtPassword.setRequired(true);
        m_txtPassword.setRegexp("\\w*");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_left:
                onBtnBack();
                break;
            case R.id.btn_ok:
                onBtnOk();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
    }

    private void onBtnBack() {
        finish();
    }

    private void onBtnOk() {
        if (!m_txtIp.validate()) {
            m_txtIp.setShakeAnimation();
            return;
        }

        if (!m_txtPtzPort.validate()) {
            m_txtPtzPort.setShakeAnimation();
            return;
        }

        if (!m_txtVideoPort.validate()) {
            m_txtVideoPort.setShakeAnimation();
            return;
        }

        if (!m_txtUserName.validate()) {
            m_txtUserName.setShakeAnimation();
            return;
        }

        if (!m_txtPassword.validate()) {
            m_txtPassword.setShakeAnimation();
            return;
        }

        Device dev = new Device();
        dev.setIp(m_txtIp.getText().toString());
        dev.setPtzPort(Integer.parseInt(m_txtPtzPort.getText().toString()));
        dev.setVideoPort(Integer.parseInt(m_txtVideoPort.getText().toString()));
        dev.setUser(m_txtUserName.getText().toString());
        dev.setPwd(m_txtPassword.getText().toString());
        if (!dev.save()) {
            toast(R.string.add_device_failed);
            return;
        }

        toast(R.string.add_device_succeed);
        finish();
    }
}

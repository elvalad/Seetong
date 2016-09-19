package com.seetong.app.seetong.ui;

import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.ext.RegexpEditText;
import ipc.android.sdk.com.NetSDK_Media_Video_Config;
import ipc.android.sdk.impl.FunclibAgent;

/**
 * Created by Administrator on 2016/9/12.
 */
public class ModifyOsdActivity extends BaseActivity implements View.OnClickListener {
    String m_device_id;
    NetSDK_Media_Video_Config m_video_config;
    NetSDK_Media_Video_Config m_new_video_config;

    private PlayerDevice playerDevice;

    private RadioGroup mTimeGroup;
    private RadioButton mTimePositionLT;
    private RadioButton mTimePositionRT;
    private RadioButton mTimePositionLB;
    private RadioButton mTimePositionRB;
    private Spinner mTimeFormat;

    private RadioGroup mTitleGroup;
    private RadioButton mTitlePositionLT;
    private RadioButton mTitlePositionRT;
    private RadioButton mTitlePositionLB;
    private RadioButton mTitlePositionRB;
    private RegexpEditText mTitleInfo;

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
        playerDevice = Global.getDeviceById(m_device_id);

        mTimeGroup = (RadioGroup) findViewById(R.id.btn_group_time_position);
        mTimePositionLT = (RadioButton) findViewById(R.id.btn_time_lt);
        mTimePositionRT = (RadioButton) findViewById(R.id.btn_time_rt);
        mTimePositionLB = (RadioButton) findViewById(R.id.btn_time_lb);
        mTimePositionRB = (RadioButton) findViewById(R.id.btn_time_rb);
        mTimeFormat = (Spinner) findViewById(R.id.sp_time_format);

        mTitleGroup = (RadioGroup) findViewById(R.id.btn_group_title_position);
        mTitlePositionLT = (RadioButton) findViewById(R.id.btn_title_lt);
        mTitlePositionRT = (RadioButton) findViewById(R.id.btn_title_rt);
        mTitlePositionLB = (RadioButton) findViewById(R.id.btn_title_lb);
        mTitlePositionRB = (RadioButton) findViewById(R.id.btn_title_rb);
        mTitleInfo = (RegexpEditText) findViewById(R.id.ret_title_info);

        Button btnFinish = (Button) findViewById(R.id.btnRight);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().setMediaParamHandler(m_handler);
        loadData();
    }

    private void loadData() {
        String xml = "";
        if (playerDevice == null) return;
        if (playerDevice.isNVR()) {
            int channelId = Integer.parseInt(playerDevice.m_devId.substring(playerDevice.m_devId.lastIndexOf("-") + 1)) - 1;
            xml = "<REQUEST_PARAM ChannelId=\"" + channelId + "\"/>";
        }

        int ret = FunclibAgent.getInstance().GetP2PDevConfig(m_device_id, 501, xml);
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

    private void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    private void saveData() {
        if (!mTitleInfo.validate()) {
            mTitleInfo.setShakeAnimation();
            return;
        }

        m_new_video_config = (NetSDK_Media_Video_Config) m_video_config.clone();
        if (mTimePositionLT.getId() == mTimeGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.timeOverlay.PosX = "0";
            m_new_video_config.overlay.timeOverlay.PosY = "0";
        } else if (mTimePositionRT.getId() == mTimeGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.timeOverlay.PosX = "1";
            m_new_video_config.overlay.timeOverlay.PosY = "0";
        } else if (mTimePositionLB.getId() == mTimeGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.timeOverlay.PosX = "0";
            m_new_video_config.overlay.timeOverlay.PosY = "1";
        } else if (mTimePositionRB.getId() == mTimeGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.timeOverlay.PosX = "1";
            m_new_video_config.overlay.timeOverlay.PosY = "1";
        }
        m_new_video_config.overlay.timeOverlay.Format = mTimeFormat.getSelectedItem().toString();

        if (mTitlePositionLT.getId() == mTitleGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.titleOverlay.PosX = "0";
            m_new_video_config.overlay.titleOverlay.PosY = "0";
        } else if (mTitlePositionRT.getId() == mTitleGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.titleOverlay.PosX = "1";
            m_new_video_config.overlay.titleOverlay.PosY = "0";
        } else if (mTitlePositionLB.getId() == mTitleGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.titleOverlay.PosX = "0";
            m_new_video_config.overlay.titleOverlay.PosY = "1";
        } else if (mTitlePositionRB.getId() == mTitleGroup.getCheckedRadioButtonId()) {
            m_new_video_config.overlay.titleOverlay.PosX = "1";
            m_new_video_config.overlay.titleOverlay.PosY = "1";
        }
        m_new_video_config.overlay.titleOverlay.Title = mTitleInfo.getText().toString();

        String xml = m_new_video_config.getOverlayXMLString();
        Log.e("OSD", xml);
        int ret = LibImpl.getInstance().getFuncLib().SetP2PDevConfig(m_device_id, 525, xml);
        if (0 != ret) {
            Log.e("OSD", "525 cmd ret : " + ret);
            toast(R.string.dlg_set_media_param_fail_tip);
            return;
        }

        mTipDlg.setCallback(null);
        showTipDlg(R.string.dlg_set_media_param_tip, 20000, R.string.dlg_set_media_param_timeout_tip);
    }

    private void onBtnFinish() {
        saveData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                onBtnFinish();
                break;
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        if (!isTopActivity(ModifyOsdActivity.class.getName())) return;
        int flag = msg.arg1;
        switch (msg.what) {
            case 501:
                NetSDK_Media_Video_Config cfg = (NetSDK_Media_Video_Config) msg.obj;
                onGetVideoParam(flag, cfg);
                break;
            case 525:
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
        onSetOsdInfo(m_video_config);
    }

    private void onSetVideoParam(int flag) {
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.dlg_set_media_param_fail_tip);
        } else {
            m_video_config = m_new_video_config;
            toast(R.string.dlg_set_media_param_succeed_tip);
            hideInputPanel(null);
            finish();
        }
    }

    private void onSetOsdInfo(NetSDK_Media_Video_Config videoCfg) {
        String timePositionX;
        String timePositionY;
        String timeFormat;
        String titlePositionX;
        String titlePositionY;
        String titleInfo;

        String overlayXml = videoCfg.getOverlayXMLString();
        Log.e("OSD", "X : " + videoCfg.overlay.timeOverlay.PosX + " Y : " + videoCfg.overlay.timeOverlay.PosY + " format : " + videoCfg.overlay.timeOverlay.Format);
        Log.e("OSD", "X : " + videoCfg.overlay.titleOverlay.PosX + " Y : " + videoCfg.overlay.titleOverlay.PosY + " info : " + videoCfg.overlay.titleOverlay.Title);
        timePositionX = videoCfg.overlay.timeOverlay.PosX;
        timePositionY = videoCfg.overlay.timeOverlay.PosY;
        timeFormat = videoCfg.overlay.timeOverlay.Format;

        if (timePositionX.equals("0") && timePositionY.equals("0")) {
            mTimePositionLT.setChecked(true);
        } else if (timePositionX.equals("1") && timePositionY.equals("0")) {
            mTimePositionRT.setChecked(true);
        } else if (timePositionX.equals("0") && timePositionY.equals("1")) {
            mTimePositionLB.setChecked(true);
        } else if (timePositionX.equals("1") && timePositionY.equals("1")) {
            mTimePositionRB.setChecked(true);
        }

        titlePositionX = videoCfg.overlay.titleOverlay.PosX;
        titlePositionY = videoCfg.overlay.titleOverlay.PosY;
        titleInfo = videoCfg.overlay.titleOverlay.Title;

        if (titlePositionX.equals("0") && titlePositionY.equals("0")) {
            mTitlePositionLT.setChecked(true);
        } else if (titlePositionX.equals("1") && titlePositionY.equals("0")) {
            mTitlePositionRT.setChecked(true);
        } else if (titlePositionX.equals("0") && titlePositionY.equals("1")) {
            mTitlePositionLB.setChecked(true);
        } else if (titlePositionX.equals("1") && titlePositionY.equals("1")) {
            mTitlePositionRB.setChecked(true);
        }
        mTitleInfo.setText(titleInfo);
    }
}

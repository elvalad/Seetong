package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.ui.ext.IntegerEditText;
import ipc.android.sdk.com.NetSDK_Media_Capability;
import ipc.android.sdk.com.NetSDK_Media_Video_Config;
import ipc.android.sdk.impl.FunclibAgent;

import java.util.ArrayList;
import java.util.List;

public class MediaParamUI extends BaseActivity implements View.OnClickListener {
    public static String TAG = "MediaParamUI";
    private String m_device_id;
    NetSDK_Media_Capability m_media_caps;
    NetSDK_Media_Video_Config m_video_config;
    NetSDK_Media_Video_Config m_new_video_config;
    int m_current_stream_type = NetSDK_Media_Capability.VIDEO_MAIN_STREAM;

    private ProgressDialog mTipDlg;

    RadioButton m_btn_main_stream;
    RadioButton m_btn_sub_stream;
    Spinner m_sp_resolution;
    Spinner m_sp_bit_rate_control;
    Spinner m_sp_i_frame_interval;
    Spinner m_sp_bit_rate;
    Spinner m_sp_frame_rate;
    IntegerEditText m_txt_i_frame_interval;
    IntegerEditText m_txt_bit_rate;
    TextView m_lab_i_frame_interval_range;
    TextView m_lab_bit_rate_range;

    ArrayList<String> m_resolution_list = new ArrayList<>();
    ArrayAdapter<String> m_resolution_adapter;
    List<NetSDK_Media_Capability.VideoCap> m_main_stream_cap = new ArrayList<>();
    List<NetSDK_Media_Capability.VideoCap> m_sub_stream_cap = new ArrayList<>();

    ArrayList<String> m_frame_rate_list = new ArrayList<>();
    ArrayAdapter<String> m_frame_rate_adapter;

    boolean m_send_config = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_param_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_media_param));
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);

        m_btn_main_stream = (RadioButton) findViewById(R.id.btn_main_stream);
        m_btn_main_stream.setChecked(true);
        m_btn_sub_stream = (RadioButton) findViewById(R.id.btn_sub_stream);
        m_sp_resolution = (Spinner) findViewById(R.id.sp_resolution);
        //m_sp_bit_rate_control = (Spinner) findViewById(R.id.sp_bit_rate_control);
        //m_sp_i_frame_interval = (Spinner) findViewById(R.id.sp_i_frame_interval);
        //m_sp_bit_rate = (Spinner) findViewById(R.id.sp_bit_rate);
        m_sp_frame_rate = (Spinner) findViewById(R.id.sp_frame_rate);

        m_txt_i_frame_interval = (IntegerEditText) findViewById(R.id.txt_i_frame_interval);
        m_txt_i_frame_interval.setRange(1, 200);
        m_txt_bit_rate = (IntegerEditText) findViewById(R.id.txt_bit_rate);

        m_lab_i_frame_interval_range = (TextView) findViewById(R.id.tv_i_frame_interval_range);
        m_lab_bit_rate_range = (TextView) findViewById(R.id.tv_bit_rate_range);

        //m_resolution_list.add("5MP");
        m_resolution_list.add("3MP");
        m_resolution_list.add("1080P");
        m_resolution_list.add("1200P");
        m_resolution_list.add("1024P");
        m_resolution_list.add("960P");
        m_resolution_list.add("720P");

        m_resolution_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_resolution_list);
        m_resolution_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_sp_resolution.setAdapter(m_resolution_adapter);
        m_sp_resolution.setSelection(0, true);

        int min_frame_rate = 1;
        int max_frame_rate = 30;
        for (int i = min_frame_rate; i <= max_frame_rate; i++) {
            m_frame_rate_list.add(Integer.toString(i));
        }

        m_frame_rate_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_frame_rate_list);
        m_frame_rate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_sp_frame_rate.setAdapter(m_frame_rate_adapter);
        m_sp_frame_rate.setSelection(0);

        m_sp_resolution.setOnItemSelectedListener(m_onResolutionItemSelected);

        /*m_sp_bit_rate_control.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Config.m_view_num = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        m_sp_i_frame_interval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Config.m_view_num = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        m_sp_bit_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Config.m_view_num = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        m_sp_frame_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RadioGroup group = (RadioGroup) findViewById(R.id.btn_group_stream);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.btn_main_stream == checkedId) {
                    m_current_stream_type = NetSDK_Media_Capability.VIDEO_MAIN_STREAM;
                    onChangeStream(NetSDK_Media_Capability.VIDEO_MAIN_STREAM);
                } else if (R.id.btn_sub_stream == checkedId) {
                    m_current_stream_type = NetSDK_Media_Capability.VIDEO_SUB_STREAM;
                    onChangeStream(NetSDK_Media_Capability.VIDEO_SUB_STREAM);
                }
            }
        });

        Button btnFinish = (Button) findViewById(R.id.btnRight);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);

        LibImpl.getInstance().setMediaParamHandler(m_handler);
        LibImpl.m_change_media_param_dev = Global.getDeviceById(m_device_id);
        m_send_config = false;
        loadData();
    }

    AdapterView.OnItemSelectedListener m_onResolutionItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            onChangeResolution(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData() {
        int ret = FunclibAgent.getInstance().P2PDevSystemControl(m_device_id, 1031, "");
        if (0 != ret) {
            toast(R.string.dlg_get_media_capability_fail_tip);
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

        showTipDlg(R.string.dlg_get_media_capability_tip, 20000, R.string.dlg_get_media_capability_timeout_tip);
    }

    public void saveData() {
        if (!m_txt_i_frame_interval.validate()) {
            m_txt_i_frame_interval.setShakeAnimation();
            return;
        }

        if (!m_txt_bit_rate.validate()) {
            m_txt_bit_rate.setShakeAnimation();
            return;
        }

        m_new_video_config = (NetSDK_Media_Video_Config) m_video_config.clone();
        String stream = NetSDK_Media_Capability.VIDEO_MAIN_STREAM == m_current_stream_type ? "1" : "2";
        for (NetSDK_Media_Video_Config.EncodeConfig e : m_new_video_config.encode.EncodeList) {
            if (!stream.equals(e.Stream)) continue;
            e.Resolution = m_resolution_list.get(m_sp_resolution.getSelectedItemPosition());
            e.Initquant = m_txt_i_frame_interval.getText().toString();
            e.BitRate = m_txt_bit_rate.getText().toString();
            e.FrameRate = m_frame_rate_list.get(m_sp_frame_rate.getSelectedItemPosition());
        }

        m_send_config = true;
        m_new_video_config.addHead(false);
        String xml = m_new_video_config.getEncodeXMLString();
        int ret = LibImpl.getInstance().getFuncLib().SetP2PDevConfig(m_device_id, 523, xml);
        if (0 != ret) {
            toast(R.string.dlg_set_media_param_fail_tip);
            return;
        }

        mTipDlg.setCallback(null);
        showTipDlg(R.string.dlg_set_media_param_tip, 20000, R.string.dlg_set_media_param_timeout_tip);
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

    @Override
    protected void onDestroy() {
        LibImpl.getInstance().setMediaParamHandler(null);
        LibImpl.m_change_media_param_dev = null;
        super.onDestroy();
    }

    @Override
    public void handleMessage(Message msg) {
        if (!isTopActivity(MediaParamUI.class.getName())) return;
        int flag = msg.arg1;
        switch (msg.what) {
            case 1031: // 读取媒体编码能力
                NetSDK_Media_Capability cap = (NetSDK_Media_Capability) msg.obj;
                onGetMediaCapability(flag, cap);
                break;
            case 501: // 读取视频参数配置
                NetSDK_Media_Video_Config cfg = (NetSDK_Media_Video_Config) msg.obj;
                onGetVideoParam(flag, cfg);
                break;
            case 523: // 设置视频参数配置
                if (!m_send_config) return;
                onSetVideoParam(flag);
                break;
        }
    }

    private void onGetMediaCapability(int flag, NetSDK_Media_Capability cap) {
        mTipDlg.dismiss();
        if (flag != 0 || null == cap) {
            toast(R.string.dlg_get_media_capability_fail_tip);
            finish();
            return;
        }

        m_media_caps = cap;
        onChangeStream(NetSDK_Media_Capability.VIDEO_MAIN_STREAM);

        // 获取设备媒体参数
        int ret = LibImpl.getInstance().getMediaParam(Global.getDeviceById(m_device_id));
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
        onChangeStream(NetSDK_Media_Capability.VIDEO_MAIN_STREAM);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                onBtnFinish();
                break;
            default: break;
        }
    }

    private void onBtnFinish() {
        saveData();
    }

    private void changeResolutionIndex(int index) {
        m_sp_resolution.setOnItemSelectedListener(null);
        m_sp_resolution.setSelection(index, true);
        m_sp_resolution.setOnItemSelectedListener(m_onResolutionItemSelected);
    }

    /**
     * 改变码流参数
     * @param streamType 码流类型
     */
    private void onChangeStream(int streamType) {
        if (null == m_media_caps) return;
        m_resolution_list.clear();
        m_main_stream_cap.clear();
        m_sub_stream_cap.clear();
        int min_bit_rate = -1;
        int max_bit_rate = -1;
        int min_frame_rate = -1;
        int max_frame_rate = -1;
        for (NetSDK_Media_Capability.VideoCap videoCap : m_media_caps.videoCaps) {
            if (Integer.parseInt(videoCap.stream_type) == NetSDK_Media_Capability.VIDEO_MAIN_STREAM) {
                m_main_stream_cap.add(videoCap);
            } else {
                m_sub_stream_cap.add(videoCap);
            }

            if (Integer.parseInt(videoCap.stream_type) != streamType) continue;
            if (videoCap.resolution.compareTo("5MP") == 0) continue;
            if (!m_resolution_list.contains(videoCap.resolution)) {
                m_resolution_list.add(videoCap.resolution);
            }

            if (-1 == min_frame_rate) min_frame_rate = Integer.parseInt(videoCap.min_frame_rate);
            if (-1 == max_frame_rate) max_frame_rate = Integer.parseInt(videoCap.max_frame_rate);
            if (-1 == min_bit_rate) min_bit_rate = Integer.parseInt(videoCap.min_bit_rate);
            if (-1 == max_bit_rate) max_bit_rate = Integer.parseInt(videoCap.max_bit_rate);
        }


        m_resolution_adapter.notifyDataSetChanged();
        //m_sp_resolution.setSelection(0, false);
        changeResolutionIndex(0);

        m_txt_bit_rate.setRange(min_bit_rate, max_bit_rate);
        String text = "(" + min_bit_rate + "-" + max_bit_rate + ")";
        m_lab_bit_rate_range.setText(text);

        m_frame_rate_list.clear();
        for (int i = min_frame_rate; i <= max_frame_rate; i++) {
            m_frame_rate_list.add(Integer.toString(i));
        }

        m_frame_rate_adapter.notifyDataSetChanged();
        m_sp_frame_rate.setSelection(0);

        onSetCurrentDeviceMediaParam();
    }

    /**
     * 改变分辨率
     * @param index
     */
    private void onChangeResolution(int index) {
        if (null == m_media_caps) return;
        NetSDK_Media_Capability.VideoCap cap = null;
        if (NetSDK_Media_Capability.VIDEO_MAIN_STREAM == m_current_stream_type) {
            cap = m_main_stream_cap.get(index);
        } else {
            cap = m_sub_stream_cap.get(index);
        }

        if (null == cap) return;

        int min_bit_rate = Integer.parseInt(cap.min_bit_rate);
        int max_bit_rate = Integer.parseInt(cap.max_bit_rate);
        int frame_rate = Integer.parseInt(cap.frame_rate);

        m_txt_bit_rate.setRange(min_bit_rate, max_bit_rate);
        m_txt_bit_rate.setText(cap.bit_rate);

        String text = "(" + min_bit_rate + "-" + max_bit_rate + ")";
        m_lab_bit_rate_range.setText(text);

        int min_frame_rate = Integer.parseInt(cap.min_frame_rate);
        int max_frame_rate = Integer.parseInt(cap.max_frame_rate);
        m_frame_rate_list.clear();
        for (int i = min_frame_rate; i <= max_frame_rate; i++) {
            m_frame_rate_list.add(Integer.toString(i));
        }

        m_frame_rate_adapter.notifyDataSetChanged();
        m_sp_frame_rate.setSelection(frame_rate - 1);
    }

    /**
     * 设置当前设备媒体参数
     */
    private void onSetCurrentDeviceMediaParam() {
        if (null == m_video_config) return;
        if (m_video_config.encode.EncodeList.size() < 2) {
            Log.e(TAG, "onSetCurrentDeviceMediaParam, EncodeList is empty");
            return;
        }

        NetSDK_Media_Video_Config.EncodeConfig config = null;
        for (NetSDK_Media_Video_Config.EncodeConfig encodeConfig : m_video_config.encode.EncodeList) {
            if ((Integer.parseInt(encodeConfig.Stream) - 1) != m_current_stream_type) continue;
            config = encodeConfig;
        }

        if (null == config) return;

        for (int i = 0, j = 0; i < m_media_caps.videoCaps.size(); i++) {
            NetSDK_Media_Capability.VideoCap cap = m_media_caps.videoCaps.get(i);
            if (Integer.parseInt(cap.stream_type) != m_current_stream_type) continue;
            j++;
            if (cap.resolution.compareToIgnoreCase(config.Resolution) != 0) continue;
            //m_sp_resolution.setSelection(j - 1, false);
            changeResolutionIndex(j - 1);
            m_txt_i_frame_interval.setText(config.Initquant);
            m_txt_bit_rate.setText(config.BitRate);
            m_sp_frame_rate.setSelection(Integer.parseInt(config.FrameRate) - 1);
            break;
        }
    }
}
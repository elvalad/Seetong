package com.seetong.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Device;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gmk on 2015/10/8.
 */
public class PlayerSettingActivity extends BaseActivity {
    public static String TAG = PlayerSettingActivity.class.getName();
    private String deviceId = null;
    private PlayerDevice playerDevice;
    private Adapter adapter;
    private List<Integer> data = new ArrayList<>();
    private ProgressDialog mTipDlg;

    class Adapter extends BaseAdapter {
        Context m_context;
        LayoutInflater m_inflater;
        List<Integer> m_data;

        private class ViewHolder {
            public TextView deviceSettingOption;
        }

        public Adapter(Context context, List<Integer> data) {
            m_context = context;
            m_data = data;
            m_inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return m_data.size();
        }

        @Override
        public Object getItem(int position) {
            return m_data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == view) {
                viewHolder = new ViewHolder();
                view = m_inflater.inflate(R.layout.player_setting_item, parent, false);
                viewHolder.deviceSettingOption = (TextView) view.findViewById(R.id.device_setting_option);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.deviceSettingOption.setText(T(m_data.get(position)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClick(m_data.get(position));
                }
            });

            return view;
        }
    }

    private void onItemClick(Integer id) {
        switch (id) {
            case R.string.dev_list_tip_title_input_dev_alias:
                onModifyDeviceAlias();
                break;
            case R.string.dev_list_tip_title_modify_user_pwd:
                onModifyUserPwd();
                break;
            case R.string.dev_list_tip_title_modify_media_parameter:
                onModifyMediaParameter();
                break;
            case R.string.title_play_setting:
                //onPlaySetting();
                break;
            case R.string.image_flip:
                //onImageFlip();
                break;
            case R.string.motion_detect:
                //onMotionDetect();
                break;
            case R.string.tv_alarm_setting:
                //onAlarmSetting();
                break;
            case R.string.tv_storage_setting:
                //onStorageSetting();
                break;
            case R.string.tv_timezone_setting:
                //onTimezoneSetting();
                break;
            case R.string.front_end_record:
                //onFrontEndRecord();
                break;
            case R.string.cloud_record:
                //onCloudRecord();
                break;
            case R.string.restore_factory_settings:
                //onRestoreFactorySettings();
                break;
        }
    }

    private void onModifyDeviceAlias() {
        int devType = playerDevice.m_dev.getDevType();
        if (100 == devType) {
            onModifyIpcAlias();
        } else if (200 == devType) {
            onModifyNvrAlias();
        }
    }

    private void onModifyIpcAlias() {

    }

    private void onModifyNvrAlias() {

    }

    private void onModifyUserPwd() {

    }

    private void onModifyMediaParameter() {
        Intent it = new Intent(this, MediaParamUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player_setting);
        deviceId = getIntent().getStringExtra("device_setting_id");
        playerDevice = Global.getDeviceById(deviceId);
        initWidget();
    }

    private void getData() {
        int devType = playerDevice.m_dev.getDevType();
        if (devType == 100) {
            data.add(R.string.dev_list_tip_title_input_dev_alias);
            //data.add(R.string.dev_list_tip_title_modify_user_pwd);
            data.add(R.string.dev_list_tip_title_modify_media_parameter);
            data.add(R.string.image_flip);
            data.add(R.string.motion_detect);
            data.add(R.string.tv_alarm_setting);
            data.add(R.string.tv_storage_setting);
            data.add(R.string.tv_timezone_setting);
            data.add(R.string.front_end_record);
            data.add(R.string.restore_factory_settings);
        } else if (devType == 200) {
            data.add(R.string.dev_list_tip_title_input_dev_alias);
        }
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        TextView textView = (TextView) findViewById(R.id.device_setting_id);
        textView.setText(deviceId);

        ListView listView = (ListView) findViewById(R.id.device_setting_list);
        getData();
        adapter = new Adapter(PlayerSettingActivity.this, data);
        listView.setAdapter(adapter);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }
}

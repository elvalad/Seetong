package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.aid.ClearEditText;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;

import java.lang.reflect.Field;

public class DeviceConfigUI extends BaseActivity implements View.OnClickListener {
    public static String TAG = "DeviceConfigUI";
    private String m_device_id;
    private PlayerDevice m_device;

    private ProgressDialog mTipDlg;
    private ExpandableListView m_listView;
    private Adapter m_adapter;

    class Adapter extends BaseExpandableListAdapter {
        Context m_ctx;
        private LayoutInflater m_inflater;

        private int[] m_itemTextResIds;

        private class GroupViewHolder {
            public TextView tvGroupName;
            public ImageView imgGroupIco;
        }

        private class ChildViewHolder {
            public TextView tvTitle;
            public ImageView imgState;
        }

        public Adapter(Context ctx) {
            m_ctx = ctx;
            m_inflater = LayoutInflater.from(ctx);
            int devType = m_device.m_dev.getDevType();
            if (devType == 100) {
                PlayerDevice dev = Global.getDeviceById(m_device_id);
                if (null == dev || null == dev.m_dev) return;

                if (0 == dev.m_dev.getSupportOss()) {
                    m_itemTextResIds = new int[]{R.string.dev_list_tip_title_input_dev_alias
                            , R.string.dev_list_tip_title_modify_user_pwd
                            , R.string.dev_list_tip_title_modify_media_parameter
                            //, R.string.title_play_setting
                            , R.string.image_flip
                            , R.string.motion_detect
                            , R.string.tv_alarm_setting
                            , R.string.tv_storage_setting
                            , R.string.tv_timezone_setting
                            , R.string.front_end_record
                            , R.string.restore_factory_settings};
                } else {
                    m_itemTextResIds = new int[]{R.string.dev_list_tip_title_input_dev_alias
                            , R.string.dev_list_tip_title_modify_user_pwd
                            , R.string.dev_list_tip_title_modify_media_parameter
                            //, R.string.title_play_setting
                            , R.string.image_flip
                            , R.string.motion_detect
                            , R.string.tv_alarm_setting
                            , R.string.tv_storage_setting
                            , R.string.tv_timezone_setting
                            , R.string.front_end_record
                            , R.string.cloud_record
                            , R.string.restore_factory_settings};
                }
            } else if (devType == 200) {
                m_itemTextResIds = new int[]{R.string.dev_list_tip_title_input_dev_alias};
            }
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return m_itemTextResIds.length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return m_itemTextResIds[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return getGroupCount() > 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View v, ViewGroup parent) {
            GroupViewHolder viewHolder;
            if (null == v) {
                v = m_inflater.inflate(R.layout.device_config_list_group_item, parent, false);
                viewHolder = new GroupViewHolder();
                viewHolder.tvGroupName = (TextView) v.findViewById(R.id.tvGroupName);
                viewHolder.imgGroupIco = (ImageView) v.findViewById(R.id.imgGroupIco);
                PlayerDevice dev = Global.getDeviceById(m_device_id);
                if (100 == dev.m_dev.getDevType()) {
                    viewHolder.tvGroupName.setText(dev.m_dev.getDevName() + "(" + m_device_id + ")");
                } else if (200 == dev.m_dev.getDevType()) {
                    viewHolder.tvGroupName.setText(dev.m_dev.getDevGroupName() + "(" + m_device_id + ")");
                }
                v.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) v.getTag();
            }

            v.setOnClickListener(null);
            return v;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent) {
            final int pos = childPosition;
            ChildViewHolder viewHolder = null;
            if (null == v) {
                v = m_inflater.inflate(R.layout.device_config_list_item, parent, false);
                viewHolder = new ChildViewHolder();
                viewHolder.tvTitle = (TextView) v.findViewById(R.id.tvTitle);
                viewHolder.imgState = (ImageView) v.findViewById(R.id.ivState);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) v.getTag();
            }

            String title = T(m_itemTextResIds[childPosition]);
            viewHolder.tvTitle.setText(title);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(m_itemTextResIds[pos]);
                }
            });

            viewHolder.imgState.setImageResource(R.drawable.icon_link_n);

            int totle_Num = getChildrenCount(groupPosition);
            if (totle_Num == 1) {
                v.setBackgroundResource(R.drawable.default_selector);
                return v;
            }
            // 第一项
            else if (childPosition == 0) {
                v.setBackgroundResource(R.drawable.list_top_selector);
            }
            // 最后一项
            else if (childPosition == totle_Num - 1) {
                v.setBackgroundResource(R.drawable.list_bottom_selector);
            } else {
                v.setBackgroundResource(R.drawable.list_center_selector_2);
            }
            return v;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private void onItemClick(int id) {
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
                onPlaySetting();
                break;
            case R.string.image_flip:
                onImageFlip();
                break;
            case R.string.motion_detect:
                onMotionDetect();
                break;
            case R.string.tv_alarm_setting:
                onAlarmSetting();
                break;
            case R.string.tv_storage_setting:
                onStorageSetting();
                break;
            case R.string.tv_timezone_setting:
                onTimezoneSetting();
                break;
            case R.string.front_end_record:
                onFrontEndRecord();
                break;
            case R.string.cloud_record:
                onCloudRecord();
                break;
            case R.string.restore_factory_settings:
                onRestoreFactorySettings();
                break;
        }
    }

    private void onRestoreFactorySettings() {
        MyTipDialog.popDialog(this, R.string.confirm_restore_factory_tip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        restoreFactory();
                    }
                }
        );
    }

    private void restoreFactory() {
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_device_id);
                if (null == dev) return;
                //MainActivity.m_this.getVideoFragment().stopAndResetPlay(dev);
                toast(R.string.dlg_wait_device_reboot_tip);
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        showTipDlg(R.string.dlg_restore_factory_tip, 10000, R.string.dlg_restore_factory_timeout_tip);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerDevice dev = Global.getDeviceById(m_device_id);
                if (null == dev) return;
                int ret = LibImpl.getInstance().getFuncLib().P2PDevSystemControl(dev.m_devId, 1002, "");
                if (0 != ret) {
                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_TOAST;
                    msg.arg1 = R.string.dlg_restore_factory_failed_tip;
                    m_handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void onCloudRecord() {
        Intent it = new Intent(this, CloudEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onFrontEndRecord() {
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;
        if (!dev.is_p2p_replay()) {
            toast(R.string.tv_not_support_front_end_record);
            return;
        }

        Intent it = new Intent(this, FrontEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onAlarmSetting() {
        Intent it = new Intent(this, AlarmSettingUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onStorageSetting() {
        Intent it = new Intent(this, StorageSettingUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onTimezoneSetting() {
        Intent it = new Intent(this, TimeZoneUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onMotionDetect() {
        Intent it = new Intent(this, MotionDetectUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onImageFlip() {
        Intent it = new Intent(this, ImageFlipUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onModifyMediaParameter() {
        Intent it = new Intent(this, MediaParamUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onPlaySetting() {
        Intent it = new Intent(this, PlaySettingUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        this.startActivity(it);
        finish();
    }

    private void onModifyUserPwd() {
        Intent it = new Intent(this, DeviceFragment.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_USER_PWD);
        this.setResult(RESULT_OK, it);
        finish();
    }

    private void onModifyDeviceAlias() {
        int devType = m_device.m_dev.getDevType();
        if (100 == devType) {
            onModifyIpcAlias();
        } else if (200 == devType) {
            onModifyNvrAlias();
        }
    }

    private void onModifyNvrAlias() {
        final DeviceConfigUI self = this;
        final String _devName = m_device.m_dev.getDevGroupName();
        Resources mResources = self.getResources();
        final ClearEditText etAddGroup = new ClearEditText(self);
        etAddGroup.setHint(R.string.dev_list_hint_input_dev_alias);
        etAddGroup.setPadding(10, 10, 10, 10);
        etAddGroup.setSingleLine(true);
        etAddGroup.setText(_devName);
        etAddGroup.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});
        new AlertDialog.Builder(self).setTitle(R.string.dev_list_tip_title_input_dev_alias)
                .setView(etAddGroup)
                .setNegativeButton(mResources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        self.hideInputPanel(etAddGroup);
                        dialog.dismiss();
                    }
                }).setPositiveButton(mResources.getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = etAddGroup.getText().toString();
                if ("".equals(value)) {
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                self.hideInputPanel(etAddGroup);
                int enterTypes = Global.m_loginType;
                int ret = LibImpl.getInstance().saveDeviceAlias(m_device.m_dev.getDevId(), value, enterTypes);
                if (ret != 0) {
                    toast(ConstantImpl.getModifyDevNameErrText(ret));
                    return;
                }

                Intent it = new Intent(self, DeviceFragment.class);
                it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
                it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_ALIAS);
                it.putExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME, value);
                self.setResult(RESULT_OK, it);
                dialog.dismiss();
                finish();
            }
        }).create().show();
    }

    private void onModifyIpcAlias() {
        final DeviceConfigUI self = this;
        String _devName = LibImpl.getInstance().getDeviceAlias(m_device.m_dev);
        final ClearEditText etAddGroup = new ClearEditText(this);
        etAddGroup.setHint(R.string.dev_list_hint_input_dev_alias);
        etAddGroup.setPadding(10, 10, 10, 10);
        etAddGroup.setSingleLine(true);
        etAddGroup.setText(_devName);
        etAddGroup.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});
        new AlertDialog.Builder(this).setTitle(R.string.dev_list_tip_title_input_dev_alias)
                .setView(etAddGroup)
                .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        self.hideInputPanel(etAddGroup);
                        dialog.dismiss();
                    }
                }).setPositiveButton(this.getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = etAddGroup.getText().toString();
                if ("".equals(value)) {
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                self.hideInputPanel(etAddGroup);
                int enterTypes = Global.m_loginType;
                int ret = LibImpl.getInstance().saveDeviceAlias(m_device.m_dev.getDevId(), value, enterTypes);
                if (ret != 0) {
                    toast(ConstantImpl.getModifyDevNameErrText(ret));
                    return;
                }

                Intent it = new Intent(self, DeviceFragment.class);
                it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
                it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_ALIAS);
                it.putExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME, value);
                self.setResult(RESULT_OK, it);
                dialog.dismiss();
                finish();
            }
        }).create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_device_config));
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);
        m_device = Global.getDeviceById(m_device_id);

        m_adapter = new Adapter(this);
        m_listView = (ExpandableListView) findViewById(R.id.lv_config);
        m_listView.setGroupIndicator(null);
        m_listView.setAdapter(m_adapter);
        m_listView.expandGroup(0);

        LibImpl.getInstance().addHandler(m_handler);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void handleMessage(Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case Define.MSG_SHOW_TOAST:
                toast(msg.arg1);
                break;
            case 1002:
                onRestoreFactory(flag);
                break;
            case 1007:
                onRebootDevice(flag);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                break;
            default:
                break;
        }
    }

    private void onRestoreFactory(int flag) {
        if (mTipDlg.isShowing()) mTipDlg.dismiss();
        if (0 == flag) {
            int ret = LibImpl.getInstance().getFuncLib().P2PDevSystemControl(m_device_id, 1007, "");
            toast(R.string.dlg_restore_factory_succeed_tip);
        } else {
            toast(R.string.dlg_restore_factory_failed_tip);
        }
    }

    private void onRebootDevice(int flag) {
        if (0 == flag) {
            toast(R.string.dlg_wait_device_reboot_tip);
            finish();
        } else {
            toast(R.string.dlg_restore_factory_failed_tip);
        }
    }
}
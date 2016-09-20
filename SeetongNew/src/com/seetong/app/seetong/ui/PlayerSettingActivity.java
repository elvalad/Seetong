package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.Device;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.aid.ClearEditText;
import com.seetong.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.NetSDK_CMD_TYPE;
import ipc.android.sdk.com.NetSDK_Media_Video_Config;
import ipc.android.sdk.com.NetSDK_TimeZone_DST_Config;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.impl.FunclibAgent;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gmk on 2015/10/8.
 */
public class PlayerSettingActivity extends BaseActivity {
    public static String TAG = PlayerSettingActivity.class.getName();
    private String deviceId = null;
    private boolean bFirmwarePrompt = false;
    private PlayerDevice playerDevice;
    private Adapter adapter;
    private List<SettingContent> data = new ArrayList<>();
    private ProgressDialog mTipDlg;
    private android.app.ProgressDialog updateProgress;
    private int fwUpdateProgress = 0;
    private int fwUpdateState = 0;
    private boolean bStopQueryUpdateState = true;
    private boolean bShowIpcDialog = true;
    private NetSDK_Media_Video_Config m_video_config;
    private String timeMode = "";

    class SettingContent {
        Integer settingOptionR;
        Integer settingImageR;

        public SettingContent(Integer settingOptionR, Integer settingImageR) {
            this.settingOptionR = settingOptionR;
            this.settingImageR = settingImageR;
        }
    }

    class Adapter extends BaseAdapter {
        Context m_context;
        LayoutInflater m_inflater;
        List<SettingContent> m_data;

        private class ViewHolder {
            public TextView deviceSettingOption;
            public ImageView deviceSettingImage;
            public ImageView deviceSettingPrompt;
        }

        public Adapter(Context context, List<SettingContent> data) {
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
                viewHolder.deviceSettingImage = (ImageView) view.findViewById(R.id.device_setting_image);
                viewHolder.deviceSettingPrompt = (ImageView) view.findViewById(R.id.device_setting_firmware_update);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.deviceSettingOption.setText(T(m_data.get(position).settingOptionR));
            viewHolder.deviceSettingImage.setBackgroundResource(m_data.get(position).settingImageR);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClick(m_data.get(position).settingOptionR);
                }
            });

            if (playerDevice.isNVR()) {
                if ((m_data.get(position).settingOptionR == R.string.nvr_firmware_update) && bFirmwarePrompt && playerDevice.bNvrUpdate) {
                    viewHolder.deviceSettingPrompt.setVisibility(View.VISIBLE);
                } else if ((m_data.get(position).settingOptionR == R.string.nvr_firmware_update) && bFirmwarePrompt && !playerDevice.bNvrUpdate) {
                    viewHolder.deviceSettingPrompt.setVisibility(View.GONE);
                } else if ((m_data.get(position).settingOptionR == R.string.ipc_firmware_update) && bFirmwarePrompt && playerDevice.bIpcUpdate) {
                    viewHolder.deviceSettingPrompt.setVisibility(View.VISIBLE);
                } else if ((m_data.get(position).settingOptionR == R.string.ipc_firmware_update) && bFirmwarePrompt && !playerDevice.bIpcUpdate) {
                    viewHolder.deviceSettingPrompt.setVisibility(View.GONE);
                } else {
                    viewHolder.deviceSettingPrompt.setVisibility(View.GONE);
                }
            } else {
                if (bFirmwarePrompt && (m_data.get(position).settingOptionR == R.string.ipc_firmware_update)) {
                    viewHolder.deviceSettingPrompt.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.deviceSettingPrompt.setVisibility(View.GONE);
                }
            }

            return view;
        }
    }

    private void onItemClick(Integer id) {
        switch (id) {
            case R.string.dev_list_tip_title_input_dev_alias:
                onModifyDeviceAlias();
                break;
            case R.string.dev_list_tip_title_input_nvr_alias:
                onModifyDeviceAlias();
                break;
            case R.string.dev_list_tip_title_input_nvr_chn_alias:
                onModifyNvrChnAlias();
                break;
            case R.string.dev_list_tip_title_modify_user_pwd:
                onModifyUserPwd();
                break;
            case R.string.dev_list_tip_title_modify_media_parameter:
                onModifyMediaParameter();
                break;
            case R.string.tv_modify_osd:
                onModifyOsd();
                break;
            case R.string.title_play_setting:
                //onPlaySetting();
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
            case R.string.ipc_firmware_update:
                onSystemUpdate();
                break;
            case R.string.nvr_firmware_update:
                onNvrFirmwareUpdate();
                break;
        }
    }

    private void onModifyDeviceAlias() {
        int devType = playerDevice.m_dev.getDevType();
        if (100 == devType) {
            onModifyIpcAlias();
        } else if (200 == devType || 201 == devType) {
            onModifyNvrDeviceAlias();
        }
    }

    private void onModifyNvrChnAlias() {
        int devType = playerDevice.m_dev.getDevType();
        if (100 == devType) { // IPC
            onModifyIpcAlias();
        } else if (200 == devType) { // NVR
            onModifyNvrAlias();
        } else if (201 == devType) { // NVR4.0
            onModifyNvrAlias();
        }
    }

    private void onModifyIpcAlias() {
        final PlayerSettingActivity self = this;
        String _devName = LibImpl.getInstance().getDeviceAlias(playerDevice.m_dev);
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
                showTipDlg(R.string.dev_list_tip_title_input_dev_alias, 15000, R.string.dlg_dev_alias_timeout_tip);
                final String value = etAddGroup.getText().toString();
                if ("".equals(value)) {
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (mTipDlg.isShowing()) mTipDlg.dismiss();
                    toast(R.string.md_error_name_null);
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                self.hideInputPanel(etAddGroup);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int enterTypes = Global.m_loginType;
                        int ret = LibImpl.getInstance().saveDeviceAlias(playerDevice.getNvrId(), value, enterTypes);
                        if (ret != 0) {
                            toast(ConstantImpl.getModifyDevNameErrText(ret));
                            return;
                        }

                        if (mTipDlg.isShowing()) mTipDlg.dismiss();
                        Intent it = new Intent(self, PlayerActivity.class);
                        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
                        it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_ALIAS);
                        it.putExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME, value);
                        self.setResult(RESULT_OK, it);
                        finish();
                    }
                }).start();
            }
        }).create().show();
    }

    private void onModifyNvrDeviceAlias() {
        final PlayerSettingActivity self = this;
        String _devName = playerDevice.m_dev.getDevGroupName();
        final ClearEditText etAddGroup = new ClearEditText(this);
        etAddGroup.setHint(R.string.dev_list_tip_title_input_nvr_alias);
        etAddGroup.setPadding(10, 10, 10, 10);
        etAddGroup.setSingleLine(true);
        etAddGroup.setText("");
        etAddGroup.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});
        new AlertDialog.Builder(this).setTitle(R.string.dev_list_tip_title_input_nvr_alias)
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
                showTipDlg(R.string.dev_list_tip_title_input_nvr_alias, 15000, R.string.dlg_dev_alias_timeout_tip);
                final String value = etAddGroup.getText().toString();
                if ("".equals(value)) {
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (mTipDlg.isShowing()) mTipDlg.dismiss();
                    toast(R.string.md_error_name_null);
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                self.hideInputPanel(etAddGroup);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int enterTypes = Global.m_loginType;
                        PlayerDevice nvrDevice = playerDevice;
                        List<PlayerDevice> list = Global.getNvrDeviceList(playerDevice.getNvrId());
                        if (list != null) {
                            nvrDevice = list.get(0);
                        }

                        int ret = LibImpl.getInstance().saveDeviceAlias(nvrDevice.getNvrId(), value, enterTypes);
                        if (ret != 0) {
                            toast(ConstantImpl.getModifyDevNameErrText(ret));
                            return;
                        }

                        if (mTipDlg.isShowing()) mTipDlg.dismiss();
                        Intent it = new Intent(self, PlayerActivity.class);
                        it.putExtra(Constant.EXTRA_DEVICE_ID, nvrDevice.m_devId);
                        it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_ALIAS);
                        it.putExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME, value);
                        self.setResult(RESULT_OK, it);
                        finish();
                    }
                }).start();
            }
        }).create().show();
    }

    private void onModifyNvrAlias() {
        final PlayerSettingActivity self = this;
        final String _devName = playerDevice.m_dev.getDevGroupName();

        Resources mResources = self.getResources();
        final ClearEditText etAddGroup = new ClearEditText(self);
        etAddGroup.setHint(R.string.dev_list_hint_input_nvr_chn_alias);
        etAddGroup.setPadding(10, 10, 10, 10);
        etAddGroup.setSingleLine(true);
        etAddGroup.setText("");
        etAddGroup.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});
        new AlertDialog.Builder(self).setTitle(R.string.dev_list_tip_title_input_nvr_chn_alias)
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
                showTipDlg(R.string.dev_list_tip_title_input_nvr_chn_alias, 15000, R.string.dlg_dev_alias_timeout_tip);
                final String value = etAddGroup.getText().toString();
                if ("".equals(value)) {
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (mTipDlg.isShowing()) mTipDlg.dismiss();
                    toast(R.string.md_error_name_null);
                    return;
                }

                dialog.dismiss();
                self.hideInputPanel(etAddGroup);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int enterTypes = Global.m_loginType;
                        /*int ret = LibImpl.getInstance().saveDeviceAlias(playerDevice.getNvrId(), value, enterTypes);
                        if (ret != 0) {
                            toast(ConstantImpl.getModifyDevNameErrText(ret));
                            return;
                        }*/

                        Device device = new Device();
                        device.setIp(playerDevice.m_devId);
                        device.setPtzPort(0);
                        device.setVideoPort(0);
                        device.setUser(value);
                        device.setPwd("");
                        device.save();
                        Global.m_sqlList = Device.findAll();

                        try {
                            Thread.sleep(1000);
                            if (mTipDlg.isShowing()) mTipDlg.dismiss();
                            Intent it = new Intent(self, PlayerActivity.class);
                            it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
                            it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_NVR_CHN_ALIAS);
                            it.putExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME, value);
                            self.setResult(RESULT_OK, it);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).create().show();
    }

    private void onModifyUserPwd() {
        Intent it = new Intent(this, PlayMultiVideoFragment.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        it.putExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, Constant.DEVICE_CONFIG_ITEM_MODIFY_USER_PWD);
        this.setResult(RESULT_OK, it);
        finish();
    }

    private void onModifyMediaParameter() {
        Intent it = new Intent(this, MediaParamUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onModifyOsd() {
        if (!playerDevice.is_osd_setting_support()) {
            toast(R.string.player_not_support_osd_setting);
            return;
        }

        Intent it = new Intent(this, ModifyOsdActivity.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onImageFlip() {
        Intent it = new Intent(this, ImageFlipUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onMotionDetect() {
        Intent it = new Intent(this, MotionDetectUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onAlarmSetting() {
        Intent it = new Intent(this, AlarmSettingUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onStorageSetting() {
        Intent it = new Intent(this, StorageSettingUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onTimezoneSetting() {
        if (!timeMode.equals("MANUAL")) {
            toast(R.string.player_not_support_timezone_setting);
            return;
        }

        Intent it = new Intent(this, TimeZoneUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onFrontEndRecord() {
        PlayerDevice dev = Global.getDeviceById(deviceId);
        if (null == dev) return;
        if (!dev.is_p2p_replay()) {
            toast(R.string.tv_not_support_front_end_record);
            return;
        }

        Intent it = new Intent(this, FrontEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
    }

    private void onCloudRecord() {
        Intent it = new Intent(this, CloudEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, deviceId);
        this.startActivity(it);
        finish();
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
                PlayerDevice dev = Global.getDeviceById(deviceId);
                if (null == dev) return;
                int ret = LibImpl.getInstance().getFuncLib().P2PDevSystemControl(dev.m_devId, 1002, "");
                if (0 != ret) {
                    android.os.Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_TOAST;
                    msg.arg1 = R.string.dlg_restore_factory_failed_tip;
                    m_handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void showIpcTipDialog() {
        MyTipDialog.popDialog(this, R.string.dlg_system_update_tip, deviceIdentify + "\n\n" + deviceComment, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        if (bFirmwarePrompt && playerDevice.bIpcUpdate) {
                            getDevVersionInfo();
                            playerDevice.bIpcUpdate = false;
                        } else {
                            toast(R.string.firmware_can_not_update);
                        }
                    }
                }
        );
    }

    private void onSystemUpdate() {
        if (!bFirmwarePrompt) {
            toast(R.string.firmware_can_not_update);
            return;
        }

        String devIdentify = playerDevice.ipcIdentify;//"TH38C13-2.5.3.20-2016062016";
        String firmwareUpdateCap = "";
        if (playerDevice.is_fw_update_v1_support()) {
            firmwareUpdateCap = "fw_update_v1";
        }
        int ret = LibImpl.getInstance().getFuncLib().GetUpdateFWInfo(deviceId, devIdentify, firmwareUpdateCap);
        //Log.e(TAG, "ret : " + ret + " ipc identify :" + devIdentify + " dev id : " + deviceId);
        if (ret == 0) {
            bShowIpcDialog = true;
        } else {
            toast(R.string.player_fw_get_update_fail);
        }

        /*if (!bFirmwarePrompt) {
            toast(R.string.firmware_can_not_update);
            return;
        }

        MyTipDialog.popDialog(this, R.string.dlg_system_update_tip, deviceIdentify + "\n" + deviceComment  ,R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        if (bFirmwarePrompt && playerDevice.bIpcUpdate) {
                            getDevVersionInfo();
                            playerDevice.bIpcUpdate = false;
                        } else {
                            toast(R.string.firmware_can_not_update);
                        }
                    }
                }
        );*/
    }

    private void showNvrTipDialog() {
        MyTipDialog.popDialog(this, R.string.dlg_system_update_tip, deviceIdentify + "\n\n" + deviceComment, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        if (bFirmwarePrompt && playerDevice.bNvrUpdate) {
                            getNvrDevInfo();
                            playerDevice.bNvrUpdate = false;
                        } else {
                            toast(R.string.firmware_can_not_update);
                        }
                    }
                }
        );
    }

    private void onNvrFirmwareUpdate() {
        if (!bFirmwarePrompt) {
            toast(R.string.firmware_can_not_update);
            return;
        }

        String devIdentify = playerDevice.nvrIdentify;//"TH38C13-2.5.3.20-2016062016";
        String firmwareUpdateCap = "";
        if (playerDevice.is_fw_update_v1_support()) {
            firmwareUpdateCap = "fw_update_v1";
        }
        int ret = LibImpl.getInstance().getFuncLib().GetUpdateFWInfo(deviceId, devIdentify, firmwareUpdateCap);
        //Log.e(TAG, "ret : " + ret + " nvr identify :" + devIdentify + " dev id : " + deviceId);
        if (ret == 0) {
            bShowIpcDialog = false;
        } else {
            toast(R.string.player_fw_get_update_fail);
        }

        /*if (!bFirmwarePrompt) {
            toast(R.string.firmware_can_not_update);
            return;
        }

        MyTipDialog.popDialog(this, R.string.dlg_system_update_tip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        if (bFirmwarePrompt && playerDevice.bNvrUpdate) {
                            getNvrDevInfo();
                            playerDevice.bNvrUpdate = false;
                        } else {
                            toast(R.string.firmware_can_not_update);
                        }
                    }
                }
        );*/
    }

    private void getDevVersionInfo() {
        Global.m_firmware_version_detect = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerDevice dev = Global.getDeviceById(deviceId);
                if (null == dev) return;
                String xml = "<REQUEST_PARAM ChannelId=\"\"/>";
                if (dev.isNVR()) {
                    int channelId = Integer.parseInt(dev.m_devId.substring(dev.m_devId.lastIndexOf("-") + 1)) - 1;
                    xml = "<REQUEST_PARAM ChannelId=\"" + channelId + "\"/>";
                }
                LibImpl.getInstance().getFuncLib().P2PDevSystemControl(dev.m_devId, 1012, xml);
            }
        }).start();
    }

    private void getNvrDevInfo() {
        Global.m_firmware_version_detect = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerDevice dev = Global.getDeviceById(deviceId);
                if (null == dev) return;
                String xml = "<REQUEST_PARAM ChannelId=\"\"/>";
                LibImpl.getInstance().getFuncLib().P2PDevSystemControl(dev.m_devId, 1012, xml);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player_setting);
        deviceId = getIntent().getStringExtra("device_setting_id");
        bFirmwarePrompt = getIntent().getBooleanExtra("device_setting_firmware_prompt", false);
        playerDevice = Global.getDeviceById(deviceId);
        FunclibAgent.getInstance().GetP2PDevConfig(deviceId, NetSDK_CMD_TYPE.CMD_GET_SYSTEM_TIME_CONFIG, "");
        initWidget();
    }

    @Override
    protected void onDestroy() {
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    private void getData() {
        int devType = playerDevice.m_dev.getDevType();
        if (100 == devType) { // IPC
            SettingContent[] settingContents = new SettingContent[11];
            settingContents[0] = new SettingContent(R.string.dev_list_tip_title_input_dev_alias, R.drawable.tps_device_setting_alais);
            data.add(settingContents[0]);
            settingContents[1] = new SettingContent(R.string.dev_list_tip_title_modify_user_pwd, R.drawable.tps_device_setting_password);
            data.add(settingContents[1]);
            settingContents[2] = new SettingContent(R.string.dev_list_tip_title_modify_media_parameter, R.drawable.tps_device_setting_alais);
            data.add(settingContents[2]);
            settingContents[3] = new SettingContent(R.string.image_flip, R.drawable.tps_device_setting_flip);
            data.add(settingContents[3]);
            settingContents[4] = new SettingContent(R.string.motion_detect, R.drawable.tps_device_setting_motion);
            data.add(settingContents[4]);
            //settingContents[5] = new SettingContent(R.string.tv_modify_osd, R.drawable.tps_device_setting_alais);
            //data.add(settingContents[5]);
            settingContents[6] = new SettingContent(R.string.tv_storage_setting, R.drawable.tps_device_setting_storage);
            data.add(settingContents[6]);
            settingContents[7] = new SettingContent(R.string.tv_timezone_setting, R.drawable.tps_device_setting_time);
            data.add(settingContents[7]);
            settingContents[8] = new SettingContent(R.string.front_end_record, R.drawable.tps_device_setting_front_end_record);
            data.add(settingContents[8]);
            settingContents[9] = new SettingContent(R.string.restore_factory_settings, R.drawable.tps_device_setting_factory);
            data.add(settingContents[9]);
            settingContents[10] = new SettingContent(R.string.ipc_firmware_update, R.drawable.tps_device_setting_factory);
            data.add(settingContents[10]);
        } else if (200 == devType) { // NVR
            SettingContent[] settingContents = new SettingContent[5];
            settingContents[0] = new SettingContent(R.string.dev_list_tip_title_input_nvr_alias, R.drawable.tps_device_setting_alais);
            data.add(settingContents[0]);
            settingContents[1] = new SettingContent(R.string.dev_list_tip_title_input_nvr_chn_alias, R.drawable.tps_device_setting_alais);
            data.add(settingContents[1]);
            settingContents[2] = new SettingContent(R.string.ipc_firmware_update, R.drawable.tps_device_setting_factory);
            data.add(settingContents[2]);
            settingContents[3] = new SettingContent(R.string.nvr_firmware_update, R.drawable.tps_device_setting_factory);
            data.add(settingContents[3]);
            settingContents[4] = new SettingContent(R.string.tv_modify_osd, R.drawable.tps_device_setting_alais);
            data.add(settingContents[4]);
        } else if (201 == devType) { // NVR4.0
            SettingContent[] settingContents = new SettingContent[5];
            settingContents[0] = new SettingContent(R.string.dev_list_tip_title_input_nvr_alias, R.drawable.tps_device_setting_alais);
            data.add(settingContents[0]);
            settingContents[1] = new SettingContent(R.string.dev_list_tip_title_input_nvr_chn_alias, R.drawable.tps_device_setting_alais);
            data.add(settingContents[1]);
            settingContents[2] = new SettingContent(R.string.ipc_firmware_update, R.drawable.tps_device_setting_factory);
            data.add(settingContents[2]);
            settingContents[3] = new SettingContent(R.string.nvr_firmware_update, R.drawable.tps_device_setting_factory);
            data.add(settingContents[3]);
            settingContents[4] = new SettingContent(R.string.tv_modify_osd, R.drawable.tps_device_setting_alais);
            data.add(settingContents[4]);
        }
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        updateProgress = new android.app.ProgressDialog(this);
        updateProgress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        updateProgress.setTitle(R.string.player_fw_update_progress);
        updateProgress.setMessage(getResources().getString(R.string.player_fw_update_start));
        updateProgress.setProgress(0);
        updateProgress.setIndeterminate(false);
        updateProgress.setCancelable(false);
        updateProgress.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.sure),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bStopQueryUpdateState = true;
                        updateProgress.dismiss();
                    }
                });

        TextView textView = (TextView) findViewById(R.id.device_setting_id);
        textView.setText(deviceId);

        ListView listView = (ListView) findViewById(R.id.device_setting_list);
        getData();
        adapter = new Adapter(PlayerSettingActivity.this, data);
        listView.setAdapter(adapter);

        LibImpl.getInstance().addHandler(m_handler);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    private void onRestoreFactory(int flag) {
        if (mTipDlg.isShowing()) mTipDlg.dismiss();
        if (0 == flag) {
            int ret = LibImpl.getInstance().getFuncLib().P2PDevSystemControl(deviceId, 1007, "");
            toast(R.string.dlg_restore_factory_succeed_tip);
        } else {
            toast(R.string.dlg_restore_factory_failed_tip);
        }
    }

    private void onRebootDevice(int flag) {
        if (0 == flag) {
            toast(R.string.dlg_wait_device_reboot_tip);
        } else {
            toast(R.string.dlg_restore_factory_failed_tip);
        }
    }


    String m_devIdentify = "";
    private void onGetDevVersionInfo(String xml) {
        Log.e(TAG, xml);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("RESPONSE_PARAM")) {
                            m_devIdentify = parser.getAttributeValue(null, "DeviceIdentify");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private void systemUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerDevice dev = Global.getDeviceById(deviceId);
                if (null == dev) return;
                //m_devIdentify = "TH38C13-2.5.3.20-2016062016";
                if (TextUtils.isEmpty(m_devIdentify)) {
                    Log.d(TAG, "device identify is empty!!!");
                    return;
                }

                String firmwareUpdateCap = "";
                if (playerDevice.is_fw_update_v1_support()) {
                    firmwareUpdateCap = "fw_update_v1";
                }
                int ret = LibImpl.getInstance().getFuncLib().GetUpdateFWInfo(dev.m_devId, m_devIdentify, firmwareUpdateCap);
                if (0 != ret) {
                    android.os.Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_TOAST;
                    msg.arg1 = R.string.dlg_update_fw_info_failed_tip;
                    m_handler.sendMessage(msg);
                } else {
                    android.os.Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_FW_UPDATE_PROGRESS;
                    m_handler.sendMessage(msg);
                    //msg.what = Define.MSG_SHOW_TOAST;
                    //msg.arg1 = R.string.dlg_update_fw_info_success_tip;
                    //m_handler.sendMessage(msg);
                    if (!playerDevice.bIpcUpdate && !playerDevice.bNvrUpdate)
                        bFirmwarePrompt = false;
                }
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void onGetUpdateProgress() {
        updateProgress.show();
        bStopQueryUpdateState = false;
        ProgressBarAsyncTask asyncTask = new ProgressBarAsyncTask(updateProgress);
        asyncTask.execute(1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerDevice dev = Global.getDeviceById(deviceId);
                if (null == dev) return;
                String xml = "<REQUEST_PARAM ChannelId=\"\"/>";
                if (dev.isNVR() && !Global.m_nvr_firmware_update) {
                    int channelId = Integer.parseInt(dev.m_devId.substring(dev.m_devId.lastIndexOf("-") + 1)) - 1;
                    xml = "<REQUEST_PARAM ChannelId=\"" + channelId + "\"/>";
                }
                LibImpl.getInstance().getFuncLib().P2PDevSystemControl(dev.m_devId, 1092, xml);
            }
        }).start();
    }

    private void onGetUpdateState(String xml) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("RESPONSE_PARAM")) {
                            fwUpdateProgress = Integer.parseInt(parser.getAttributeValue(null, "Progress"));
                            if (fwUpdateState != 5) {
                                fwUpdateState = Integer.parseInt(parser.getAttributeValue(null, "State"));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    String deviceIdentify = "";
    String deviceComment = "";
    private void onGetUpdateComment(String xml) {
        if (xml == null) return;
        //Log.e(TAG, xml);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("param")) {
                            deviceIdentify = parser.getAttributeValue(null, "device_identify");
                            deviceComment = parser.getAttributeValue(null, "comment");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private void onGetVideoParam(int flag, NetSDK_Media_Video_Config cfg) {
        if (flag != 0 || null == cfg) {
            toast(R.string.dlg_get_media_param_fail_tip);
            return;
        }

        m_video_config = cfg;
        Log.e(TAG, m_video_config.getOverlayXMLString());
        if (cfg.encode.EncodeList.size() < 2) {
            toast(R.string.dlg_get_media_param_format_incorrect_tip);
        }
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case Define.MSG_SHOW_TOAST:
                toast(msg.arg1);
                adapter.notifyDataSetChanged();
                break;
            case Define.MSG_SHOW_FW_UPDATE_PROGRESS:
                if (this.isFinishing()) return;
                onGetUpdateProgress();
                adapter.notifyDataSetChanged();
                break;
            case 501: // 读取视频参数配置
                NetSDK_Media_Video_Config cfg = (NetSDK_Media_Video_Config) msg.obj;
                onGetVideoParam(flag, cfg);
                break;
            case 1002:
                onRestoreFactory(flag);
                break;
            case 1007:
                onRebootDevice(flag);
                break;
            case 1012:
                String xml = (String) msg.obj;
                onGetDevVersionInfo(xml);
                Global.m_firmware_update = true;
                systemUpdate();
                break;
            case 1090:
                if (flag == -16777216) fwUpdateState = 5;
                break;
            case 1092:
                xml = (String) msg.obj;
                onGetUpdateState(xml);
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_UPDATE_FW_INFO:
                LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                xml = (String) msgObj.recvObj;
                onGetUpdateComment(xml);
                if (bShowIpcDialog && playerDevice.bIpcUpdate) {
                    showIpcTipDialog();
                } else if (!bShowIpcDialog && playerDevice.bNvrUpdate) {
                    showNvrTipDialog();
                }
                break;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_TIME_CONFIG:
                NetSDK_TimeZone_DST_Config timeZoneCfg = (NetSDK_TimeZone_DST_Config) msg.obj;
                timeMode = timeZoneCfg.TimeMode;
                break;
        }
    }

    class ProgressBarAsyncTask extends AsyncTask {
        private android.app.ProgressDialog progressDialog;

        public ProgressBarAsyncTask(android.app.ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object doInBackground(Object[] params) {
            while (true) {
                try {
                    Thread.sleep(100);
                    PlayerDevice dev = Global.getDeviceById(deviceId);
                    if (null == dev) return null;
                    String xml = "<REQUEST_PARAM ChannelId=\"\"/>";
                    if (dev.isNVR() && !Global.m_nvr_firmware_update) {
                        int channelId = Integer.parseInt(dev.m_devId.substring(dev.m_devId.lastIndexOf("-") + 1)) - 1;
                        xml = "<REQUEST_PARAM ChannelId=\"" + channelId + "\"/>";
                    }
                    LibImpl.getInstance().getFuncLib().P2PDevSystemControl(dev.m_devId, 1092, xml);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishProgress(fwUpdateProgress);

                if (((fwUpdateProgress == 100) && (fwUpdateState == 3)) || (fwUpdateState == 4) || (fwUpdateState == 5) || bStopQueryUpdateState) break;
            }

            return fwUpdateProgress + params[0].toString() + "";
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_0));
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            int value = (int) values[0];
            progressDialog.setProgress(value);
            switch (fwUpdateState) {
                case 0:
                    progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_0));
                    break;
                case 1:
                    progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_1));
                    break;
                case 2:
                    progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_2));
                    break;
                case 3:
                    progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_3));
                    android.os.Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_TOAST;
                    msg.arg1 = R.string.player_fw_update_state_3;
                    m_handler.sendMessage(msg);
                    progressDialog.dismiss();
                    break;
                case 4:
                    progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_4));
                    msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_TOAST;
                    msg.arg1 = R.string.player_fw_update_state_4;
                    m_handler.sendMessage(msg);
                    progressDialog.dismiss();
                    break;
                case 5:
                    progressDialog.setMessage(getResources().getString(R.string.player_fw_update_state_5));
                    msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_SHOW_TOAST;
                    msg.arg1 = R.string.player_fw_update_state_5;
                    m_handler.sendMessage(msg);
                    progressDialog.dismiss();
                    break;
            }
        }
    }
}

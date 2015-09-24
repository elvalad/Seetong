package com.seetong.app.seetong.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

import cn.sharesdk.framework.ShareSDK;
import com.android.system.MessageNotification;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.DeviceSetting;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.Device;

import java.util.List;


/**
 * MainActivity 主要用于主界面显示，它包含一个 TabHost，其中含有三个 Fragment,分别是
 * DeviceFragment, MediaFragment 和 MoreFragment.
 *
 * Created by gmk on 2015/9/13.
 */
public class MainActivity2 extends BaseActivity {
    public static String TAG = MainActivity2.class.getName();
    private TabHost tabHost;
    private ProgressDialog mTipDlg;

    private View deviceTabView;
    private View mediaTabView;
    private View moreTabView;
    private BaseFragment currentFragment;
    private DeviceFragment2 deviceFragment;
    private MediaFragment2 mediaFragment;
    private MoreFragment2 moreFragment;

    public static final int ADD_LIVE_ID = 0x1010;
    public static final String ADD_LIVE_KEY = "add_live";
    public static String DEVICE_ID_KEY = "device_id_key";

    public static MainActivity2 m_this = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MainActivity2.class.getName(), "onCreate...");
        m_this = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mTipDlg = new ProgressDialog(this, R.string.dlg_login_recv_list_tip);
        mTipDlg.setCancelable(false);

        LibImpl.getInstance().addHandler(m_handler);
        Global.initDirs();

        MessageNotification.getInstance().setContext(this);
        String xml = getIntent().getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
        onNotifyDevData(xml, new ParseDevListResult() {
            @Override
            public void onResult(List<PlayerDevice> devices) {
                sendMessage(Define.MSG_PARSE_DEV_LIST, 0, 0, null);
            }
        });

        setContentView(R.layout.activity_main);
        initWidget();
    }

    @Override
    public void onBackPressed() {
        if (null != currentFragment) {
            if (currentFragment.onBackPressed()) return;
        }

        MyTipDialog.popDialog(this, R.string.dlg_app_exit_sure_tip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        //exit();
                        //m_this.finish();
                        //Global.onAppTerminate();
                    }
                }
        );
    }

    /**
     * 初始化此 Activity 的基本组件.
     */
    private void initWidget() {
        tabHost = (TabHost) findViewById(R.id.main_tab_host);
        tabHost.setup();

        deviceTabView = getLayoutInflater().inflate(R.layout.tab_widget2, null);
        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_on);
        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setText(R.string.main_device);
        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));

        TabHost.TabSpec device = tabHost.newTabSpec("device");
        device.setContent(R.id.fragment_device);
        device.setIndicator(deviceTabView);
        tabHost.addTab(device);

        mediaTabView = getLayoutInflater().inflate(R.layout.tab_widget2, null);
        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_off);
        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setText(R.string.main_media);
        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));

        TabHost.TabSpec media = tabHost.newTabSpec("media");
        media.setContent(R.id.fragment_media);
        media.setIndicator(mediaTabView);
        tabHost.addTab(media);

        moreTabView = getLayoutInflater().inflate(R.layout.tab_widget2, null);
        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_off);
        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setText(R.string.main_more);
        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));

        TabHost.TabSpec more = tabHost.newTabSpec("more");
        more.setContent(R.id.fragment_more);
        more.setIndicator(moreTabView);
        tabHost.addTab(more);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setCurrentFragment(tabId);
                //View v = tabHost.getCurrentTabView();
                switch (tabId) {
                    case "device":
                        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_on);
                        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));
                        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_off);
                        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_off);
                        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        break;
                    case "media":
                        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_off);
                        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_on);
                        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));
                        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_off);
                        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        break;
                    case "more":
                        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_off);
                        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_off);
                        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_on);
                        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));
                        break;
                }
            }
        });
    }

    private void setCurrentFragment(String name) {
        switch (name) {
            case "device":
                currentFragment = deviceFragment;
                break;
            case "media":
                currentFragment = mediaFragment;
                break;
            case "more":
                currentFragment = moreFragment;
                break;
        }
    }

    public void setDeviceFragment(DeviceFragment2 deviceFragment) {
        this.deviceFragment = deviceFragment;
    }

    public DeviceFragment2 getDeviceFragment() {
        return this.deviceFragment;
    }

    /**
     * 在 DeviceFragment 中点击某个 Item 的播放 Button 之后，会调用此方法进入 PlayerActivity 去播放
     * 此设备监控下的视频.
     */
    public void playVideo(String devId) {
        Intent intent = new Intent(MainActivity2.this, PlayerActivity.class);
        intent.putExtra("device_id", devId);
        startActivity(intent);
    }

    public static interface ParseDevListResult {
        void onResult(List<PlayerDevice> devices);
    }

    public void onNotifyDevData(final String xml, final ParseDevListResult cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    parseDevList(xml);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTipDlg.dismiss();
                        }
                    });
                    if (cb != null) cb.onResult(Global.getSelfDeviceList());
                    /*TODO: 告诉DeviceFragment2已经获取到设备列表 */
                    deviceFragment.updateDeviceFragment(Global.getDeviceList().size());
                } catch (Exception e) {
                    Log.e(TAG, "parse device list error," + e.toString());
                    toast(R.string.dlg_get_list_fail_tip);
                }
            }
        }).start();
    }

    private void parseDevList(String xml) {
        if (TextUtils.isEmpty(xml)) return;
        List<Device> lst = (List<Device>) new Device().fromXML(xml.getBytes(), "DeviceList");
        if (lst != null && lst.size() > 0) {
            Global.uniqueDeviceList(lst);

            for (Device dev : lst) {
                PlayerDevice d = Global.getDeviceById(dev.getDevId());
                if (null == d) {
                    d = new PlayerDevice();
                    Global.addDevice(d);
                }

                d.m_devId = dev.getDevId();
                d.m_dev = dev;
                d.m_capacity_set = LibImpl.getInstance().getCapacitySet(d.m_devId);
                DeviceSetting ds = DeviceSetting.findByDeviceId(d.m_devId);
                if (null != ds) {
                    d.m_force_forward = ds.is_force_forward();
                }
            }

            //Global.sortDeviceListByGroupName(Global.m_deviceList);
            LibImpl.putDeviceList(Global.getDeviceList());
            Log.d(TAG, "device list size=" + Global.getDeviceList().size());

            Device dev = lst.get(0);
            Log.i(TAG, "doMsgRspCB:get device is success..." + dev.toString());
            //sendMyToast(R.string.dlg_get_list_success_tip);
            LibImpl.getInstance().generateSnaphost(lst);
            LibImpl.getInstance().initAutoRecvAlarm();
        } else {
            Log.e(TAG, "Get device data is error...");
            toast(R.string.dlg_get_list_fail_tip);
        }
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        android.os.Message msg = m_handler.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        m_handler.sendMessage(msg);
    }

    public void logout() {
        Log.i(TAG, "logout begin....");
        Global.clearDeviceList();
        Global.m_devInfoList.clear();
        LibImpl.getInstance().clearHandler();
        LibImpl.clearDeviceList();
        LibImpl.mDeviceNotifyInfo.clear();
        LibImpl.mIsFirstFrameMap.clear();
        LibImpl.mVideoStateMap.clear();
        LibImpl.setIsLoginSuccess(false);
        Log.i(TAG, "logout begin----1....");
        LibImpl.getInstance().logoff(false);
        Log.i(TAG, "logout begin----2....");
        LibImpl.getInstance().clearDevice();
        Log.i(TAG, "logout end....");
    }

    public void exit() {
        Log.i(TAG, "logout begin....");
        Global.clearDeviceList();
        Global.m_devInfoList.clear();
        LibImpl.getInstance().clearHandler();
        LibImpl.clearDeviceList();
        LibImpl.mDeviceNotifyInfo.clear();
        LibImpl.mIsFirstFrameMap.clear();
        LibImpl.mVideoStateMap.clear();
        LibImpl.setIsLoginSuccess(false);
        Log.i(TAG, "logout begin----1....");
        LibImpl.getInstance().logoff(true);
        Log.i(TAG, "logout begin----2....");
        LibImpl.getInstance().clearDevice();
        Log.i(TAG, "logout end....");
    }
}


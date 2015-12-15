package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import cn.sharesdk.framework.ShareSDK;
import com.android.system.MessageNotification;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.model.DeviceSetting;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.com.SDK_CONSTANT;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
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

    private ExitHandler mExitHandler;
    public static MainActivity2 m_this = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MainActivity2.class.getName(), "onCreate...");
        m_this = this;
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        mTipDlg = new ProgressDialog(this, R.string.dlg_login_recv_list_tip);
        mTipDlg.setCancelable(false);
        mExitHandler = new ExitHandler(this);
        LibImpl.getInstance().addHandler(m_handler);
        Global.initMain();
        Global.initDirs();
        MessageNotification.getInstance().setContext(this);
        setContentView(R.layout.activity_main);
        initWidget();
        String xml = getIntent().getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
        if (xml == null) {
            deviceFragment.updateDeviceFragment(0);
            return;
        }
        onNotifyDevData(xml, new ParseDevListResult() {
            @Override
            public void onResult(List<PlayerDevice> devices) {
                boolean bExitNormally = Global.m_spu.loadBooleanSharedPreference(Define.EXIT_APP_NORMALLY, true);
                if (!bExitNormally) {
                    String devId = Global.m_spu.loadStringSharedPreference(Define.SAVE_EXIT_DEVICE);
                    if (null != devId) {
                        PlayerDevice dev = LibImpl.findDeviceByID(devId);
                        if (null != dev) {
                            playVideo(devId);
                        }
                    }
                }
                sendMessage(Define.MSG_PARSE_DEV_LIST, 0, 0, null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);
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
                        saveData();
                        exitDialog(true);
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
                        sendMessage(Define.MSG_SHOW_PICTURE_FRAGMENT, 0, 0, null);
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
                        sendMessage(Define.MSG_SHOW_PICTURE_FRAGMENT, 0, 0, null);
                        break;
                }
            }
        });
    }

    public void setTabVisible(boolean visible) {
        tabHost.getTabWidget().setVisibility(visible ? View.VISIBLE : View.GONE);
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

    public void setMediaFragment(MediaFragment2 mediaFragment) {
        this.mediaFragment = mediaFragment;
    }

    public MediaFragment2 getMediaFragment() {
        return this.mediaFragment;
    }

    public void setMoreFragment(MoreFragment2 moreFragment) {
        this.moreFragment = moreFragment;
    }

    public MoreFragment2 getMoreFragment() {
        return this.moreFragment;
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

    /**
     * 在 DeviceFragment 中长按某个设备会弹出对话框，询问用户是否删除此设备.
     * @param devId
     */
    public void deleteDevice(String devId) {
        final PlayerDevice dev = Global.getDeviceById(devId);
        final String name = dev.m_dev.getDevName();
        String deleteTitle = dev.isNVR() ? T(R.string.device_you_want_to_delete) + T(R.string.device_group) + " NVR:"+ dev.m_dev.getDevName() + " ?"
                : T(R.string.device_you_want_to_delete) +  " IPC:" + dev.m_dev.getDevId() + " ?";
        MyTipDialog.popDialog(this, deleteTitle, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        mTipDlg.setTitle(R.string.pdlg_delete_device);
                        mTipDlg.setTimeoutToast(T(R.string.timeout_retry));
                        mTipDlg.setCallback(new ProgressDialog.ICallback() {
                            @Override
                            public void onTimeout() {
                                sendMessage(Define.MSG_UPDATE_DEV_LIST, 0, 0, null);
                            }

                            @Override
                            public boolean onCancel() {
                                return false;
                            }
                        });
                        mTipDlg.show(20000);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (!dev.isNVR()) {
                                    final int ret = LibImpl.getInstance().delDevice(dev);
                                    if (0 != ret) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String err = ConstantImpl.getDelDeviceErrText(ret);
                                                toast(err + " " + T(R.string.operation_failed_retry));
                                                mTipDlg.dismiss();
                                            }
                                        });
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            /* 删除IPC设备之后更新设备列表 */
                                            sendMessage(Define.MSG_UPDATE_DEV_LIST, 0, 0, null);
                                            mTipDlg.dismiss();
                                            toast(R.string.device_delete_success);
                                        }
                                    });
                                } else {
                                    if (!"".equals(name)) {
                                        final int ret = LibImpl.getInstance().delNVR(name);
                                        if (0 != ret) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String err = ConstantImpl.getDelDeviceErrText(ret);
                                                    toast(err + " " + T(R.string.operation_failed_retry));
                                                    mTipDlg.dismiss();
                                                }
                                            });

                                            return;
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                /* 删除NVR设备之后更新设备列表 */
                                                sendMessage(Define.MSG_UPDATE_DEV_LIST, 0, 0, null);
                                                mTipDlg.dismiss();
                                                toast(R.string.device_delete_success);
                                            }
                                        });
                                    }
                                }
                            }
                        }).start();
                    }
                }
        );
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
                    loadData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTipDlg.dismiss();
                        }
                    });
                    if (cb != null) cb.onResult(Global.getSelfDeviceList());
                    /*TODO: 告诉DeviceFragment2已经获取到设备列表 */
                    deviceFragment.updateDeviceFragment(Global.getDeviceList().size());
                    sendMessage(Define.MSG_UPDATE_DEV_LIST, 0, 0, null);
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
                d.m_capacity_set = LibImpl.getInstance().getCapacitySet(d);
                d.m_net_type = LibImpl.getInstance().getDeviceNetType(d);
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
            LibImpl.getInstance().generateSnaphost(Global.getDeviceList());
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

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_DEV_ALIAS:
                deviceFragment.handleMessage(msg);
                break;
            case Define.MSG_UPDATE_SCREENSHOT_LIST:
                mediaFragment.handleMessage(msg);
                break;
            case Define.MSG_SHOW_PICTURE_FRAGMENT:
                mediaFragment.handleMessage(msg);
                break;
            case Define.MSG_UPDATE_DEV_LIST:
                deviceFragment.handleMessage(msg);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                deviceFragment.handleMessage(msg);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                deviceFragment.handleMessage(msg);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                deviceFragment.handleMessage(msg);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                deviceFragment.handleMessage(msg);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                deviceFragment.handleMessage(msg);
                break;
            case Define.MSG_ENABLE_ALIAS:
                deviceFragment.handleMessage(msg);
                break;
            default:
                break;
        }
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
        //LibImpl.setIsLoginSuccess(false);
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
        //LibImpl.setIsLoginSuccess(false);
        Log.i(TAG, "logout begin----1....");
        LibImpl.getInstance().logoff(true);
        Log.i(TAG, "logout begin----2....");
        LibImpl.getInstance().clearDevice();
        Log.i(TAG, "logout end....");
    }

    boolean m_kill_process = false;
    public void exitDialog(boolean kill_process) {
        m_kill_process = kill_process;
        mExitHandler.sendEmptyMessage(199);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mExitHandler.sendEmptyMessageDelayed(200, 15000);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long bTime = System.currentTimeMillis();
                exit();
                ShareSDK.stopSDK(m_this);
                Global.onAppTerminate();
                long aTime = System.currentTimeMillis();
                Log.i("MSG", "destory is time" + (aTime - bTime) + "ms");
                Log.i("MSG", "onDestroy mFunclibAgent.destory()");
                //mExitHandler.sendEmptyMessage(200);
                mExitHandler.sendEmptyMessageDelayed(200, (aTime - bTime > 800) ? 0 : 800);
            }
        }).start();
    }

    ProgressDialog mExitTipDlg;
    private static class ExitHandler extends Handler {
        MainActivity2 m_ui;
        public ExitHandler(MainActivity2 ui) {
            m_ui = ui;
        }

        boolean m_exit = false;
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 199:
                    m_ui.mExitTipDlg = new ProgressDialog(m_this, R.string.dlg_app_exit_tip);
                    m_ui.mExitTipDlg.setCancelable(false);
                    m_ui.mExitTipDlg.show();
                    break;
                case 200:
                    if (m_exit) return;
                    m_exit = true;
                    //MessageNotification.getInstance().cancelAll();
//				mNetworkChangeBr = null;NetWorkChangeBroadcastReceiver.mNetChangeCallback = null;sNetTipDialog = null;
                    if (m_ui.mExitTipDlg != null) m_ui.mExitTipDlg.dismiss();
                    m_ui.finish();
                    if (m_ui.m_kill_process) android.os.Process.killProcess(android.os.Process.myPid());    //Dalvik VM的本地方法
                    break;
                default:
                    break;
            }
        }
    }

    private void loadData() {
        List<String> list = new ArrayList<>();
        String jsonArrayString = Global.m_spu.loadStringSharedPreference(Define.DEV_LIST_ORDER);
        //Log.d(TAG, "json array string is : " + jsonArrayString);
        if (null == jsonArrayString) return;

        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
                //Log.d(TAG, "load device order : " + list.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < Global.getDeviceList().size(); j++) {
                if (list.get(i).equals(Global.getDeviceList().get(j).m_devId)) {
                    Global.getDeviceList().get(j).m_device_exit_index = i;
                }
            }
        }

        Global.sortDeviceListByExitIndex();
        /*for (int i = 0; i < Global.getDeviceList().size(); i++) {
            Log.d(TAG, "device exit index :" + Global.getDeviceList().get(i).m_devId + " index : " + Global.getDeviceList().get(i).m_device_exit_index);
        }*/
    }

    private void saveData() {
        Global.m_spu.saveSharedPreferences(Define.EXIT_APP_NORMALLY, true);
        boolean bSaveData = Global.m_spu.saveSharedPreferences(Define.IS_SAVE_DEV_LIST, true);
        //Log.d(TAG, "save device order : " + bSaveData);
        if (bSaveData) {
            List<String> list = new ArrayList<>();
            for (int i =  0; i < Global.getDeviceList().size(); i++) {
                list.add(Global.getDeviceList().get(i).m_devId);
            }
            JSONArray jsonArray = new JSONArray(list);
            Global.m_spu.saveSharedPreferences(Define.DEV_LIST_ORDER, jsonArray.toString());
        }
    }
}


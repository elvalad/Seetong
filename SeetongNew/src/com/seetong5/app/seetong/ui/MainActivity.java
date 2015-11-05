package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import cn.sharesdk.framework.ShareSDK;
import com.android.system.MessageNotification;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.comm.Tools;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.ext.GuideTools;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import com.seetong5.app.seetong.model.*;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.com.NetSDK_UserAccount;

import java.util.List;

import static android.view.View.OnClickListener;

public class MainActivity extends BaseActivity implements OnClickListener {
    public static String TAG = MainActivity.class.getName();
    public static MainActivity m_this = null;
    private TabHost m_tabHost;
    private View m_ind_video;
    private View m_ind_device;
    private View m_ind_friend;
    private View m_ind_message;
    private View m_ind_media;
    private View m_ind_more;

    private BaseFragment m_cur_fragment;
    private VideoFragment m_video;
    private DeviceFragment m_device;
    private MediaFragment m_media;
    private FriendFragment m_friend;
    private MessageFragment m_message;
    private MoreFragment m_more;

    private ProgressDialog mTipDlg;

    private ExitHandler mExitHandler;

    public static final int ADD_LIVE_ID = 0x1010;
    public static final String ADD_LIVE_KEY = "add_live";
    public static String DEVICE_ID_KEY = "device_id_key";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(MainActivity.class.getName(), "onCreate...");
        m_this = this;
        super.onCreate(savedInstanceState);

        mTipDlg = new ProgressDialog(this, R.string.dlg_login_recv_list_tip);
        mTipDlg.setCancelable(false);

        mExitHandler = new ExitHandler(this);
        LibImpl.getInstance().addHandler(m_handler);
        Global.initMain();

        String devId = getIntent().getStringExtra(DEVICE_ID_KEY);
        int login_succeed = getIntent().getIntExtra(Constant.EXTRA_LOGIN_SUCCEED, 0);
        if (1 != login_succeed || Define.LOGIN_TYPE_NONE == Global.m_loginType) {
            Log.i(MainActivity.class.getName(), "not login, start LoginUI");
            Intent it = new Intent(this, LoginUI2.class);
            if (!TextUtils.isEmpty(devId)) it.putExtra(DEVICE_ID_KEY, devId);
            startActivity(it);
            finish();
            return;
        }

        Global.initDirs();
        Log.i(MainActivity.class.getName(), "setContentView...");
        setContentView(R.layout.main);

        Global.mPushAgent.enable();
        //PushManager.startWork(Global.m_ctx, PushConstants.LOGIN_TYPE_API_KEY, "AmTyaZNSsxX2jgka6C3MgaLu");

        MessageNotification.getInstance().setContext(this);
        //MessageNotification.getInstance().cancelAll();

        m_tabHost = (TabHost) findViewById(R.id.id_tab_host);
        m_tabHost.setup();

        m_ind_video = getLayoutInflater().inflate(R.layout.tab_widget, null);
        m_ind_video.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tab_btn_video);
        ((TextView)m_ind_video.findViewById(R.id.id_text_view)).setText(T(R.string.video));

        TabHost.TabSpec video = m_tabHost.newTabSpec("video");
        video.setContent(R.id.fragment_video);
        video.setIndicator(m_ind_video);
        m_tabHost.addTab(video);

        m_ind_device = getLayoutInflater().inflate(R.layout.tab_widget, null);
        m_ind_device.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tab_btn_device);
        ((TextView)m_ind_device.findViewById(R.id.id_text_view)).setText(T(R.string.device));

        TabHost.TabSpec device = m_tabHost.newTabSpec("device");
        device.setContent(R.id.fragment_device);
        device.setIndicator(m_ind_device);
        m_tabHost.addTab(device);

        m_ind_friend = getLayoutInflater().inflate(R.layout.tab_widget, null);
        m_ind_friend.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tab_btn_friend);
        ((TextView) m_ind_friend.findViewById(R.id.id_text_view)).setText(T(R.string.friend));

        TabHost.TabSpec friend = m_tabHost.newTabSpec("friend");
        friend.setContent(R.id.fragment_friend);
        friend.setIndicator(m_ind_friend);
        m_tabHost.addTab(friend);

        /*m_ind_message = getLayoutInflater().inflate(R.layout.tab_widget, null);
        m_ind_message.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tab_btn_message);
        ((TextView)m_ind_message.findViewById(R.id.id_text_view)).setText(T(R.string.message));

        TabHost.TabSpec message = m_tabHost.newTabSpec("message");
        message.setContent(R.id.fragment_message);
        message.setIndicator(m_ind_message);
        m_tabHost.addTab(message);*/

        m_ind_media = getLayoutInflater().inflate(R.layout.tab_widget, null);
        m_ind_media.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tab_btn_media);
        ((TextView)m_ind_media.findViewById(R.id.id_text_view)).setText(T(R.string.media));

        TabHost.TabSpec media = m_tabHost.newTabSpec("media");
        media.setContent(R.id.fragment_media);
        media.setIndicator(m_ind_media);
        m_tabHost.addTab(media);

        m_ind_more = getLayoutInflater().inflate(R.layout.tab_widget, null);
        m_ind_more.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tab_btn_more);
        ((TextView)m_ind_more.findViewById(R.id.id_text_view)).setText(T(R.string.more));

        TabHost.TabSpec more = m_tabHost.newTabSpec("more");
        more.setContent(R.id.fragment_more);
        more.setIndicator(m_ind_more);
        m_tabHost.addTab(more);

        m_tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                if (tabId.equals("video")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    LibImpl.m_stop_render = false;
                    Global.acquirePower();
                } else {
                    LibImpl.m_stop_render = true;
                    Global.releasePower();
                }

                setCurrentFragment(tabId);

                m_ind_video.findViewById(R.id.id_image_background).setVisibility(View.GONE);
                m_ind_device.findViewById(R.id.id_image_background).setVisibility(View.GONE);
                m_ind_friend.findViewById(R.id.id_image_background).setVisibility(View.GONE);
                m_ind_media.findViewById(R.id.id_image_background).setVisibility(View.GONE);
                m_ind_more.findViewById(R.id.id_image_background).setVisibility(View.GONE);
                View v = m_tabHost.getCurrentTabView();
                v.findViewById(R.id.id_image_background).setVisibility(View.VISIBLE);
            }
        });

        String xml = getIntent().getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
        onNotifyDevData(xml, new ParseDevListResult() {
            @Override
            public void onResult(List<PlayerDevice> devices) {
                sendMessage(Define.MSG_PARSE_DEV_LIST, 0, 0, null);
            }
        });
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
        MainActivity m_ui;
        public ExitHandler(MainActivity ui) {
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
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (m_cur_fragment == m_video) Global.acquirePower();
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Global.releasePower();
    }

    @Override
    protected void onDestroy() {
        Log.i(MainActivity.class.getName(), "onDestroy...");
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (null != m_cur_fragment) {
            if (m_cur_fragment.onBackPressed()) return;
        }

        MyTipDialog.popDialog(this, R.string.dlg_app_exit_sure_tip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        exitDialog(true);
                    }
                }
        );
    }

    public VideoFragment getVideoFragment() {
        return m_video;
    }

    public void setVideoFragment(VideoFragment m_video) {
        this.m_video = m_video;
    }

    public DeviceFragment getDeviceFragment() {
        return m_device;
    }

    public void setDeviceFragment(DeviceFragment m_device) {
        this.m_device = m_device;
    }

    public MediaFragment getMediaFragment() {
        return m_media;
    }

    public void setMediaFragment(MediaFragment m_media) {
        this.m_media = m_media;
    }

    public FriendFragment getFriendFragment() {
        return m_friend;
    }

    public void setFriendFragment(FriendFragment m_friend) {
        this.m_friend = m_friend;
    }

    public MessageFragment getMessageFragment() {
        return m_message;
    }

    public void setMessageFragment(MessageFragment m_message) {
        this.m_message = m_message;
    }

    public MoreFragment getMoreFragment() {
        return m_more;
    }

    public void setMoreFragment(MoreFragment m_more) {
        this.m_more = m_more;
    }

    public void toVideo() {
        m_tabHost.setCurrentTab(0);
    }

    public void toDevice() {
        m_tabHost.setCurrentTab(1);
    }

    public void toFriend() {
        m_tabHost.setCurrentTab(2);
    }

    public void toMessage() {
        m_tabHost.setCurrentTab(3);
    }

    public void toMore() {
        m_tabHost.setCurrentTab(4);
    }

    public void setTabWidgetVisible(boolean visible) {
        m_tabHost.getTabWidget().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setCurrentFragment(String name) {
        switch (name) {
            case "video":
                m_cur_fragment = m_video;
                break;
            case "device":
                m_cur_fragment = m_device;
                m_device.notifyDataSetChanged();
                break;
            case "friend":
                m_cur_fragment = m_friend;
                break;
            case "message":
                m_cur_fragment = m_message;
                break;
            case "media":
                m_cur_fragment = m_media;
                break;
            case "more":
                m_cur_fragment = m_more;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//		if (isAllStop()){
//			if (mbIsFullScreen){
//				startFullScreen(false);
//			}
//			return ;
//		}
        Log.i("MSG", "VideoActivity@onConfigurationChanged is called...");
        int orientation = newConfig.orientation/*this.getResources().getConfiguration().orientation*/;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {

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
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Define.MSG_STATUS_EVENT:
                onStatusEvent(msg.arg1, msg.arg2, (String) msg.obj);
                return;
            case Define.MSG_PARSE_DEV_LIST:
                onParseDevList();
                return;
        }

        if (null != m_video && m_video.handleMessage(msg)) return;
        if (null != m_device && m_device.handleMessage(msg)) return;
        if (null != m_friend && m_friend.handleMessage(msg)) return;
        //if (m_message.handleMessage(msg)) return;
        if (null != m_media && m_media.handleMessage(msg)) return;
    }

    private void onStatusEvent(int lUser, int nStateCode, String response) {
        if (m_video.onStatusEvent(lUser, nStateCode, response)) return;
        if (m_device.onStatusEvent(lUser, nStateCode, response)) return;
        if (m_friend.onStatusEvent(lUser, nStateCode, response)) return;
        //if (m_message.onStatusEvent(lUser, nStateCode, response)) return;
        if (m_media.onStatusEvent(lUser, nStateCode, response)) return;
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

    private void onParseDevList() {
        m_device.initView();
        //m_device.notifyDataSetChanged();

        if (Define.LOGIN_TYPE_DEVICE == Global.m_loginType) {
            // is NVR
            if (Global.getSelfDeviceList().size() > 1) {
                Global.m_devInfo.setDevGroupName(Global.getSelfDeviceList().get(0).m_dev.getDevGroupName());
            }
        }

        int imgs[];
        int flags = Tools.getLanguageTypes();
        switch(flags){
            case 0:
                imgs = new int[]{R.drawable.guide_video_1, R.drawable.guide_video_2, R.drawable.guide_video_3};
                break;
            case 1:
                imgs = new int[]{R.drawable.guide_video_1, R.drawable.guide_video_2, R.drawable.guide_video_3};
                break;
            case 2:
                imgs = new int[]{R.drawable.guide_video_1_en, R.drawable.guide_video_2_en, R.drawable.guide_video_3_en};
                break;
            default:
                imgs = new int[]{R.drawable.guide_video_1, R.drawable.guide_video_2, R.drawable.guide_video_3};
                break;
        }

        GuideTools tools = new GuideTools(this);
        if (!tools.isFirstRun("main_video")) Global.m_guide_finished = true;
        if (Define.LOGIN_TYPE_USER == Global.m_loginType) {

            m_cur_fragment = m_video;
            LibImpl.m_stop_render = false;
            Global.acquirePower();
            m_tabHost.setCurrentTab(0);
            m_ind_video.findViewById(R.id.id_image_background).setVisibility(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            tools.setGuideImage(R.id.layout_main, imgs, "main_video", new GuideTools.IGuideFinish() {
                @Override
                public void onFinish() {
                    Global.m_guide_finished = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    /*String devId = getIntent().getStringExtra(DEVICE_ID_KEY);
                    if (!TextUtils.isEmpty(devId)) {
                        PlayerDevice dev = Global.getSelfDeviceById(devId);
                        if (null == dev) return;
                        addDeviceToLive(dev);
                    }*/
                    com.seetong5.app.seetong.model.Settings s = com.seetong5.app.seetong.model.Settings.findByUser(Global.m_devInfo.getUserName());
                    if (null == s) return;
                    List<String> ids = s.getPreviewDevices();
                    for (String id : ids) {
                        PlayerDevice dev = Global.getSelfDeviceById(id);
                        if (null == dev) continue;
                        addDeviceToLive(dev);
                    }
                }
            });
        } else if (Define.LOGIN_TYPE_DEMO == Global.m_loginType) {
            m_cur_fragment = m_device;
            LibImpl.m_stop_render = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            tools.setGuideImage(R.id.layout_main, imgs, "main_video", new GuideTools.IGuideFinish() {
                @Override
                public void onFinish() {
                    Global.m_guide_finished = true;
                    String devId = getIntent().getStringExtra(DEVICE_ID_KEY);
                    if (!TextUtils.isEmpty(devId)) {
                        PlayerDevice dev = Global.getSelfDeviceById(devId);
                        if (null == dev) return;
                        addDeviceToLive(dev);
                    } else {
                        m_tabHost.setCurrentTab(1);
                        m_ind_device.findViewById(R.id.id_image_background).setVisibility(View.VISIBLE);
                    }
                }
            });
        } else if (Define.LOGIN_TYPE_DEVICE == Global.m_loginType) {
            m_cur_fragment = m_video;
            LibImpl.m_stop_render = false;
            Global.acquirePower();

            m_tabHost.setCurrentTab(0);
            m_ind_video.findViewById(R.id.id_image_background).setVisibility(View.VISIBLE);

            m_ind_friend.setEnabled(false);
            Button btn = (Button) m_ind_friend.findViewById(R.id.btn_disable_icon);
            btn.bringToFront();
            btn.setVisibility(View.VISIBLE);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            tools.setGuideImage(R.id.layout_main, imgs, "main_video", new GuideTools.IGuideFinish() {
                @Override
                public void onFinish() {
                    Global.m_guide_finished = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    String devId = getIntent().getStringExtra(Constant.DEVICE_INFO_KEY);
                    if (!TextUtils.isEmpty(devId)) {
                        PlayerDevice dev = Global.getDeviceById(devId);
                        if (null == dev) return;
                        addDeviceToLive(dev);
                    }
                }
            });
        }

        //LibImpl.getInstance().notifyChangeDefaultPwdThreadStart();
    }

    public boolean addDeviceToLive(PlayerDevice dev) {
        /*if (dev.m_dev.getOnLine() != Device.ONLINE) {
            toast(R.string.dlg_device_offline_tip);
            return false;
        }*/

        toVideo();
        return m_video.addDeviceToLive(dev);
    }

    public boolean delDevice(List<PlayerDevice> devs) {
        return m_device.delDevice(devs);
    }

    public void setFriendPromptIcon(int count) {
        if (null == m_ind_friend) return;
        Button btn_small_icon = (Button) m_ind_friend.findViewById(R.id.btn_small_icon);
        btn_small_icon.bringToFront();
        btn_small_icon.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        btn_small_icon.setText(String.valueOf(count));
    }

    public void modifyDeviceDefaultPassword(String devId, List<NetSDK_UserAccount> obj) {
        m_device.modifyDeviceDefaultPassword(devId, obj);
    }

    public void saveSettings() {
        if (Define.LOGIN_TYPE_USER != Global.m_loginType) return;
        com.seetong5.app.seetong.model.Settings s = new com.seetong5.app.seetong.model.Settings();
        s.setUser(Global.m_devInfo.getUserName());
        s.setPreviewDevices(m_video.getPlayDeviceIds());
        s.save();
    }

    public void logout() {
        saveSettings();
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
        saveSettings();
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
}

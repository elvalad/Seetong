package com.seetong5.app.seetong;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.*;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import cn.sharesdk.framework.ShareSDK;
import com.android.utils.SharePreferenceUtil;
import com.baidu.android.pushservice.PushManager;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.comm.NetworkUtils;
import com.seetong5.app.seetong.comm.Tools;
import com.seetong5.app.seetong.model.*;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.service.MainService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.com.TPS_AlarmInfo;
import ipc.android.sdk.impl.DeviceInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2014-05-07.
 */

public class Global {
    private static String TAG = "Global";
    public static int m_resSound[] = {R.raw.alarm1, R.raw.alarm2, R.raw.alarm3, R.raw.alarm4, R.raw.alarm5,
            R.raw.alarm6, R.raw.alarm7, R.raw.alarm8, R.raw.alarm9, R.raw.alarm10
    };

    public static Context m_ctx;
    public static Resources m_res;
    public static SharePreferenceUtil m_spu;
    public static SharePreferenceUtil m_spu_login;
    public static DBHelper m_db;
    public static PowerManager.WakeLock m_wakeLock;
    public static AudioManager m_audioManage;

    private static List<PlayerDevice> m_deviceList = new ArrayList<>();
    public static DeviceInfo m_devInfo = new DeviceInfo();
    public static List<DeviceInfo> m_devInfoList = new ArrayList<>();
    public static int m_loginType = Define.LOGIN_TYPE_NONE;
    public static boolean m_guide_finished = false;

    public static FriendList m_friends = new FriendList();
    public static FriendMessageList m_messges = new FriendMessageList();

    public static AlarmMessage m_alarmMessage;

    public static int m_mobile_net_sub_type = 0;
    public static int m_mobile_net_sub_type_2 = 0;
    public static String m_mobile_net_type = "";
    public static String m_mobile_net_type_2 = "";

    public static final int MSG_ADD_ALARM_DATA = 1;
    public static final int MSG_VIDEO_UI_DESTROYED = 2;
    public static final int MSG_SHARE_START_SHARE = 3;
    public static final int MSG_SHARE_RESULT_COMPLETE = 4;
    public static final int MSG_SHARE_RESULT_ERROR = 5;

    public static final String YOUKU_CLIENT_ID = "0a51875a90cdea37";
    public static final String YOUKU_CLIENT_SECRET = "5fcef3f1b72541c2cbb54862a09f6a9c";

    public static final String OSS_ACCESS_ID = "f0BNsScZDQXv7fo7";
    public static final String OSS_ACCESS_KEY = "qei5UGYum10sYezHFiS6OTLSjdSk8i";
    //public static OSSClient m_oss;

    public static boolean m_clean_tags = true;
    public static List<String> m_pushTags = new ArrayList<>();

    public static PushAgent mPushAgent;

    synchronized public static void addDevice(PlayerDevice dev) {
        if (m_deviceList.indexOf(dev) >= 0) return;
        m_deviceList.add(dev);
    }

    synchronized public static void setDeviceList(List<PlayerDevice> list) {
        m_deviceList = list;
    }

    synchronized public static List<PlayerDevice> getDeviceList() {
        return m_deviceList;
    }

    synchronized public static List<PlayerDevice> getSelfDeviceList() {
        List<PlayerDevice> lst = new ArrayList<>();
        for (PlayerDevice dev : m_deviceList) {
            if (dev.m_friend_share) continue;
            lst.add(dev);
        }

        return lst;
    }

    synchronized public static List<PlayerDevice> getSortedDeviceList() {
        sortDeviceListByOnline(m_deviceList);
        //sortDeviceListByPlayCount(m_deviceList);
        return m_deviceList;
    }

    synchronized public static void clearDeviceList() {
        m_deviceList.clear();
    }

    synchronized public static void sortDeviceListByGroupName(List<PlayerDevice> list) {
        class DeviceSortByGroupName implements Comparator<PlayerDevice> {
            @Override
            public int compare(PlayerDevice dev1, PlayerDevice dev2) {
                int ret = dev1.m_dev.getDevGroupName().compareTo(dev2.m_dev.getDevGroupName());
                String groupName = dev1.m_dev.getDevGroupName();
                if (ret != 0 && "IPCamera".equals(groupName)) return -1;
                groupName = dev2.m_dev.getDevGroupName();
                if (ret != 0 && "IPCamera".equals(groupName)) return 1;
                return ret;
            }
        }

        Collections.sort(list, new DeviceSortByGroupName());
    }

    synchronized public static void sortDeviceListByOnline(List<PlayerDevice> list) {
        class DeviceSortByOnline implements Comparator<PlayerDevice> {
            @Override
            public int compare(PlayerDevice dev1, PlayerDevice dev2) {
                return dev1.m_dev.getOnLine() == dev2.m_dev.getOnLine() ? 0 :
                        (dev1.m_dev.getOnLine() > dev2.m_dev.getOnLine() ? -1 : 1);
            }
        }

        Collections.sort(list, new DeviceSortByOnline());
    }

    synchronized public static void sortDeviceListByPlayCount(List<PlayerDevice> list) {
        class DeviceSortByPlayCount implements Comparator<PlayerDevice> {
            @Override
            public int compare(PlayerDevice dev1, PlayerDevice dev2) {
                if (dev1.m_dev.getOnLine() == Device.OFFLINE) return 1;
                if (dev1.m_device_play_count >= dev2.m_device_play_count) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        /* JDK7更新了Collections的sort算法，如果没有正常处理相等的情况可能会抛出异常
        * 参考文档可参看链接:http://blog.2baxb.me/archives/993
        * 这里设置使用老版本的排序算法规避。
        * */
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(list, new DeviceSortByPlayCount());
    }

    synchronized public static void sortChatMessageByTime(List<FriendMessageList.Message> list) {
        class MessageSortByTime implements Comparator<FriendMessageList.Message> {
            @Override
            public int compare(FriendMessageList.Message arg1, FriendMessageList.Message arg2) {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Calendar c1 = java.util.Calendar.getInstance();
                java.util.Calendar c2 = java.util.Calendar.getInstance();

                try {
                    c1.setTime(fmt.parse(arg1.m_time));
                    c2.setTime(fmt.parse(arg2.m_time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return c1.compareTo(c2);
            }
        }

        Collections.sort(list, new MessageSortByTime());
    }

    public static void sortChatMessageByMsgId(List<FriendMessageList.Message> list) {
        class MessageSortById implements Comparator<FriendMessageList.Message> {
            @Override
            public int compare(FriendMessageList.Message arg1, FriendMessageList.Message arg2) {
                int lhs = Integer.parseInt(arg1.m_id);
                int rhs = Integer.parseInt(arg2.m_id);
                return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
            }
        }

        Collections.sort(list, new MessageSortById());
    }

    synchronized public static void uniqueDeviceList(List<Device> list) {
        Set<String> set = new HashSet<String>();
        List<Device> newList = new ArrayList<>();
        newList.addAll(list);
        for (Device obj : newList) {
            if (set.add(obj.getDevId())) continue;
            list.remove(obj);
            Log.w(TAG, "发现重复的设备ID[" + obj.getDevId() + "]");
        }
    }

    synchronized public static PlayerDevice getDeviceById(String id) {
        if (null == m_deviceList) return null;
        for (PlayerDevice dev : m_deviceList) {
            if (null == dev.m_dev) continue;
            if (dev.m_dev.getDevId().compareToIgnoreCase(id) == 0) {
                //if (m_deviceList.indexOf(dev) >= 0) return dev;
                return dev;
            }
        }

        return null;
    }

    synchronized public static PlayerDevice getSelfDeviceById(String id) {
        if (null == m_deviceList) return null;
        for (PlayerDevice dev : m_deviceList) {
            if (null == dev.m_dev) continue;
            if (dev.m_friend_share) continue;
            if (dev.m_dev.getDevId().compareToIgnoreCase(id) == 0) {
                //if (m_deviceList.indexOf(dev) >= 0) return dev;
                return dev;
            }
        }

        return null;
    }

    synchronized public static List<String> getSelfDeviceIdAry() {
        List<String> lst = new ArrayList<>();
        if (null == m_deviceList) return lst;
        for (PlayerDevice dev : m_deviceList) {
            if (null == dev.m_dev) continue;
            if (dev.m_friend_share) continue;
            lst.add(dev.m_devId);
        }

        return lst;
    }

    synchronized public static List<PlayerDevice> getDeviceByName(String name) {
        List<PlayerDevice> lst = new ArrayList<>();
        if (null == m_deviceList) return null;
        for (PlayerDevice dev : m_deviceList) {
            if (null == dev.m_dev || null == dev.m_dev.getDevName()) continue;
            if (dev.m_dev.getDevName().compareToIgnoreCase(name) == 0) lst.add(dev);
        }

        return lst;
    }

    synchronized public static List<PlayerDevice> getDeviceByGroup(String name) {
        List<PlayerDevice> lst = new ArrayList<>();
        if (null == m_deviceList) return null;
        for (PlayerDevice dev : m_deviceList) {
            if (null == dev.m_dev || null == dev.m_dev.getDevGroupName()) continue;
            if (dev.m_dev.getDevGroupName().compareToIgnoreCase(name) == 0) lst.add(dev);
        }

        return lst;
    }

    synchronized public static void delDevice(String devId) {
        if (null == m_deviceList) return;
        for (PlayerDevice dev : m_deviceList) {
            if (dev.m_friend_share) continue;
            if (dev.m_devId.compareToIgnoreCase(devId) == 0) {
                m_deviceList.remove(dev);
                return;
            }
        }
    }

    synchronized public static void delDeviceByName(String name) {
        if (null == m_deviceList) return;
        for (Iterator itr = m_deviceList.iterator(); itr.hasNext();) {
            PlayerDevice dev = (PlayerDevice) itr.next();
            if (null == dev.m_dev || null == dev.m_dev.getDevName()) continue;
            if (dev.m_dev.getDevName().compareToIgnoreCase(name) == 0) itr.remove();
        }
    }

    synchronized public static List<PlayerDevice> getDeviceByDefaultUserPwd() {
        if (null == m_deviceList) return null;
        List<PlayerDevice> lst = new ArrayList<>();
        for (PlayerDevice dev : m_deviceList) {
            if (null == dev.m_dev) continue;
            if ("admin".equals(dev.m_dev.getLoginName()) && "123456".equals(dev.m_dev.getLoginPassword())) lst.add(dev);
        }

        return lst;
    }

    synchronized public static DeviceInfo getDeviceInfoById(String id) {
        if (null == m_devInfoList) return null;
        for (DeviceInfo devInfo : m_devInfoList) {
            if (devInfo.getDevId().compareToIgnoreCase(id) == 0) return devInfo;
        }

        return null;
    }

    public static void saveAlarmMessage(TPS_AlarmInfo info) {
        m_alarmMessage.setAlarmMessage(info);
    }

    public static Messenger m_msger = null;
    private static ServiceConnection m_conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            m_msger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_msger = null;
        }
    };

    public static void sendMessageToService(Message msg) {
        if (null == m_msger) return;
        try {
            m_msger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void onAppStart(Context ctx) {
        Log.i(TAG, "onAppStart...");
        m_ctx = ctx;
        mPushAgent = PushAgent.getInstance(Global.m_ctx);
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
                String notifyString = "通知点击  title=" + msg.title + " description="
                        + msg.text + " customContent=" + msg.custom;
                Log.d("PushMessage", notifyString);
            }
        };

        mPushAgent.setNotificationClickHandler(notificationClickHandler);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread arg0, Throwable arg1) {
                Log.e(TAG, "Thread.setDefaultUncaughtExceptionHandler is fail...begin", arg1);
                arg1.printStackTrace();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                arg1.printStackTrace(new PrintStream(baos));
                MobclickAgent.reportError(m_ctx, baos.toString());
                Log.e(TAG, "Thread.setDefaultUncaughtExceptionHandler is fail...end");
            }
        });

        m_res = ctx.getResources();
        m_db = new DBHelper(m_ctx);
        /*m_oss = new OSSClient();
        m_oss.setAccessId(OSS_ACCESS_ID);
        m_oss.setAccessKey(OSS_ACCESS_KEY);*/

        String languageToLoad  = "en";
        int flags = Tools.getLanguageTypes();
        switch(flags){
            case 0:
                languageToLoad = "zh";
                break;
            case 1: //about_ch_TW
                languageToLoad = "zh";
                break;
            case 2:
                languageToLoad = "en";
                break;
            default:
                languageToLoad = "en";
                break;
        }

        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = m_ctx.getResources().getConfiguration();
        DisplayMetrics metrics = m_ctx.getResources().getDisplayMetrics();
        config.locale = locale;
        m_ctx.getResources().updateConfiguration(config, metrics);

        PowerManager localPowerManager = (PowerManager) m_ctx.getSystemService(Context.POWER_SERVICE);
        m_wakeLock = localPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        m_audioManage = (AudioManager) m_ctx.getSystemService(Context.AUDIO_SERVICE);

        m_spu = new SharePreferenceUtil(m_ctx, Define.SEETONG_CONFIG_FILE);
        m_spu_login = new SharePreferenceUtil(m_ctx, Define.LOGIN_ALL_CONFIG_FILE);
        m_alarmMessage = new AlarmMessage();
        Config.loadData();
        Intent intent = new Intent(ctx, MainService.class);
        ctx.startService(intent);
        ctx.bindService(new Intent(ctx, MainService.class), m_conn, Context.BIND_AUTO_CREATE);
    }

    public static void onAppTerminate() {
        Config.saveData();
        if (null != m_deviceList) m_deviceList.clear();
        if (null != m_devInfoList) m_devInfoList.clear();
        m_ctx.unbindService(m_conn);
        Intent intent = new Intent(m_ctx, MainService.class);
        m_ctx.stopService(intent);
    }

    public static void initMain() {
        getNetType();
        initUmServer();
        ShareSDK.initSDK(m_ctx);
    }

    public static void getNetType() {
        if (NetworkUtils.isMobile(m_ctx)) {
            m_mobile_net_type = NetworkUtils.getProvidersName(m_ctx);
            m_mobile_net_sub_type = NetworkUtils.getNetSubType(m_ctx);
            if (NetworkUtils.is2G(m_ctx)) {
                m_mobile_net_sub_type_2 = 2;
            } else if (NetworkUtils.is3G(m_ctx)) {
                m_mobile_net_sub_type_2 = 1;
            } else if (NetworkUtils.is4G(m_ctx)) {
                m_mobile_net_sub_type_2 = 5;
            }
        } else if (NetworkUtils.isWifi(m_ctx)) {
            m_mobile_net_type = "WF";
        } else if (NetworkUtils.isEthernet(m_ctx)) {
            m_mobile_net_type = "ET";
        } else {
            m_mobile_net_type = "UN";
        }

        switch (m_mobile_net_type) {
            case "YD":
                if (NetworkUtils.is2G(m_ctx)) {
                    m_mobile_net_type_2 = "M14";
                } else if (NetworkUtils.is3G(m_ctx)) {
                    m_mobile_net_type_2 = "M10";
                } else if (NetworkUtils.is4G(m_ctx)) {
                    m_mobile_net_type_2 = "M7";
                }
                break;
            case "DX":
                if (NetworkUtils.is2G(m_ctx)) {
                    m_mobile_net_type_2 = "M13";
                } else if (NetworkUtils.is3G(m_ctx)) {
                    m_mobile_net_type_2 = "M11";
                } else if (NetworkUtils.is4G(m_ctx)) {
                    m_mobile_net_type_2 = "M8";
                }
                break;
            case "LT":
                if (NetworkUtils.is2G(m_ctx)) {
                    m_mobile_net_type_2 = "M15";
                } else if (NetworkUtils.is3G(m_ctx)) {
                    m_mobile_net_type_2 = "M12";
                } else if (NetworkUtils.is4G(m_ctx)) {
                    m_mobile_net_type_2 = "M9";
                }
                break;
            case "WF":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int type = LibImpl.getInstance().getFuncLib().SearchIpType("");
                        if (type == 1) {
                            m_mobile_net_type_2 = "M1";
                        } else if (type == 2) {
                            m_mobile_net_type_2 = "M2";
                        } else if (type == 3) {
                            m_mobile_net_type_2 = "M3";
                        } else if (type == 4) {
                            m_mobile_net_type_2 = "M4";
                        } else if (type == 5) {
                            m_mobile_net_type_2 = "M5";
                        } else if (type == 6) {
                            m_mobile_net_type_2 = "M6";
                        }
                    }
                }).start();
                break;
        }

        if (TextUtils.isEmpty(m_mobile_net_type_2)) {
            m_mobile_net_type_2 = "M16";
        }
    }

    public static void initDirs() {
        File dir = new File(Define.RootDirPath);
        if (!(dir.exists())) {
            dir.mkdirs();
        }

        dir = new File(getImageDir());
        if (!(dir.exists())) {
            dir.mkdirs();
        }

        dir = new File(getVideoDir());
        if (!(dir.exists())) {
            dir.mkdirs();
        }

        dir = new File(getAudioDir());
        if (!(dir.exists())) {
            dir.mkdirs();
        }

        dir = new File(getCloudDir());
        if (!(dir.exists())) {
            dir.mkdirs();
        }

        dir = new File(getSnapshotDir());
        if (!(dir.exists())) {
            dir.mkdirs();
        }
    }

    public static String getImageDir() {
        if (Define.LOGIN_TYPE_DEVICE == m_loginType) {
            return Define.RootDirPath + "/default/images";
        } else {
            Log.e(TAG, "========================>>>>>dev info is: " + m_devInfo.getUserName());
            return Define.RootDirPath + "/" + m_devInfo.getUserName() + "/images";
        }
    }

    public static String getVideoDir() {
        if (Define.LOGIN_TYPE_DEVICE == m_loginType) {
            return Define.RootDirPath + "/default/videos";
        } else {
            return Define.RootDirPath + "/" + m_devInfo.getUserName() + "/videos";
        }
    }

    public static String getAudioDir() {
        if (Define.LOGIN_TYPE_DEVICE == m_loginType) {
            return Define.RootDirPath + "/default/audios";
        } else {
            return Define.RootDirPath + "/" + m_devInfo.getUserName() + "/audios";
        }
    }

    public static String getCloudDir() {
        if (Define.LOGIN_TYPE_DEVICE == m_loginType) {
            return Define.RootDirPath + "/default/cloud";
        } else {
            return Define.RootDirPath + "/" + m_devInfo.getUserName() + "/cloud";
        }
    }

    public static String getSnapshotDir() {
        return Define.RootDirPath + "/snapshot";
    }

    public static void initUmServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doInitUmServer();
            }
        }).start();
    }

    private static void doInitUmServer() {
        /** ######1.检测软件更新 ######*/
        checkUpdate();

        /** ######2.程序运行信息统计 ######*/
        MobclickAgent.setDebugMode(true);
        MobclickAgent.updateOnlineConfig(m_ctx);

        /** ######3.反馈得到回复时提醒 ######*/
        //当开发者回复用户反馈后，如果需要提醒用户，请在应用程序的入口Activity的OnCreate()方法中下添加以下代码
        //UMFeedbackService.enableNewReplyNotification(this, NotificationType.AlertDialog);
    }

    private static void checkUpdate() {
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.Yes: // has update
                        Log.i("--->", "callback result.");
                        UmengUpdateAgent.showUpdateDialog(m_ctx, updateInfo);
                        break;

                    case UpdateStatus.No: // has no update
                        Log.i(TAG, "没有更新.");
                        MobclickAgent.reportError(m_ctx, "没有更新.");
                        break;

                    case UpdateStatus.NoneWifi: // none wifi
                        Log.i(TAG, "没有wifi连接， 只在wifi下更新.");
                        MobclickAgent.reportError(m_ctx, "没有wifi连接， 只在wifi下更新.");
                        break;

                    case UpdateStatus.Timeout: // time out
                        Log.i(TAG, "更新超时.");
                        MobclickAgent.reportError(m_ctx, "更新超时.");
                        break;
                }
            }
        });

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.update(m_ctx);
    }

    public static void acquirePower() {
        if (!m_wakeLock.isHeld()) {
            m_wakeLock.acquire();
        }
    }

    public static void releasePower() {
        if (m_wakeLock.isHeld()) m_wakeLock.release();
    }

    public static boolean initPushTags(Context ctx) {
        Log.d(TAG, "initPushTags,login type=" + m_loginType);
        if (Define.LOGIN_TYPE_DEVICE == m_loginType) {
            if (null == m_devInfo) return false;
            String devId = m_devInfo.getDevId();
            if (TextUtils.isEmpty(devId)) return false;
            DeviceSetting setting = DeviceSetting.findByDeviceId(devId);
            if (null == setting) return true;
            if (setting.is_enable_push_msg()) {
                List<String> lst = new ArrayList<>();
                lst.add(devId);
                PushManager.setTags(m_ctx, lst);
                Log.d(TAG, "initPushTags,login type=" + m_loginType + ",set tags=" + lst);
            }
        } else {
            setPushTags();
        }

        return true;
    }

    public static void clearPushTags() {
        PushManager.delTags(m_ctx, m_pushTags);
    }

    private static void setPushTags() {
        if (m_deviceList.isEmpty()) return;
        List<String> lst = new ArrayList<>();
        for (PlayerDevice dev : m_deviceList) {
            DeviceSetting ds = DeviceSetting.findByDeviceId(dev.m_devId);
            if (null == ds || !ds.is_enable_push_msg()) continue;
            lst.add(dev.m_devId);
        }

        PushManager.setTags(m_ctx, lst);
        Log.d(TAG, "initPushTags,login type=" + m_loginType + ",set tags=" + lst);
    }
}

package com.seetong.app.seetong.sdk.impl;

import com.android.audio.AudioPlayer;
import com.android.opengles.OpenglesRender;
import com.seetong.app.seetong.comm.Define;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.com.NetSDK_IPC_ENTRY;
import ipc.android.sdk.com.NetSDK_UserAccount;
import ipc.android.sdk.com.TPS_AddWachtRsp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-07-03.
 */
public class PlayerDevice {
    public String m_devId = "";
    public Device m_dev;
    public NetSDK_IPC_ENTRY m_entry;
    public boolean m_online;
    public boolean m_play;
    public boolean m_replay;
    public boolean m_replaying;
    public boolean m_playing;
    public boolean m_voice;
    public boolean m_talk;
    public boolean m_record;
    public boolean m_snapshot = false;
    // 窗口最大化
    public boolean m_maximize;
    public boolean m_ptz_auto;
    // 用户手动停止了视频
    public boolean m_user_stop = false;
    // 是好友分享的设备
    public boolean m_friend_share;
    // 是否收到了第一帧
    public boolean m_first_frame;
    // 是否强制转发
    public boolean m_force_forward;
    // 是否提醒过修改默认密码
    public boolean m_prompt_modify_pwd;

    public boolean m_del_mode;
    public boolean m_connect_ok;

    public int m_login_id;
    public int m_play_id;
    public int m_port_id;
    public int m_replay_port_id = -1;
    public int m_view_id;
    public int m_last_view_id;
    public int m_stream_type;
    public int m_frame_type;
    public int m_net_type;

    public int m_svr_inst = -1;
    public String m_debug_msg_1 = "";
    public String m_debug_msg_2 = "";
    public int m_replay_duration;
    public double m_replay_timestamp;
    // 好友分享设备时长,单位秒
    public int m_share_video_timestamp;
    public AudioPlayer m_audio;
    public OpenglesRender m_video;
    public String m_user = "admin";
    public String m_pwd = "123456";
    public String m_tipInfo = "";
    public String m_tipTinfo2 = "";
    public String m_capacity_set = "";
    public List<String> m_lstPreset = new ArrayList<>();
    public TPS_AddWachtRsp m_add_watch_rsp;
    public List<NetSDK_UserAccount> m_lstUser = new ArrayList<>();

    public int m_add_watch_result;
    public int m_open_audio_stream_result;
    public int m_open_video_stream_result;

    public int m_device_play_count;
    public int m_device_exit_index;

    public String nvrIdentify = "";
    public String ipcIdentify = "";
    public boolean bNvrUpdate = false;
    public boolean bIpcUpdate = false;

    {
        m_port_id = -1;
        m_view_id = -1;
        m_last_view_id = -1;
        m_stream_type = Define.SUB_STREAM_TYPE;
        m_frame_type = 0;
        m_net_type = -1;
        m_replay_duration = 0;
        m_replay_timestamp = 0;
        m_share_video_timestamp = -1;
        m_prompt_modify_pwd = false;
        m_del_mode = false;
        m_connect_ok = false;
        m_add_watch_result = -1;
        m_open_audio_stream_result = -1;
        m_open_video_stream_result = -1;

        m_device_play_count = 0;
        m_device_exit_index = -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerDevice)) return false;

        PlayerDevice that = (PlayerDevice) o;

        if (!m_devId.equals(that.m_devId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return m_devId.hashCode();
    }

    public boolean isNVR() {
        if (null == m_dev) return false;
        int devType = m_dev.getDevType();
        return 200 == devType || 201 == devType;
    }

    public String getNvrId() {
        if (!isNVR()) return m_devId;
        return m_devId.substring(0, m_devId.indexOf("-CH"));
    }

    public String getDeviceName() {
        if (null == m_dev) return "";
        String _devName = isNVR() ? null : m_dev.getDevName();
        if (null == _devName || "".equals(_devName)) _devName = m_dev.getDevId();
        _devName = _devName.replace("-channel-", "-CH_");
        return _devName;
    }

    public static interface IWatchTimeout {
        public void onTimeout(PlayerDevice dev);
    }

    private IWatchTimeout m_watch_timeout_handler;
    public void startAutoStopTimer(final int seconds, IWatchTimeout handler) {
        m_watch_timeout_handler = handler;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (PlayerDevice.this) {
                        PlayerDevice.this.wait(1000 * seconds);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (null != m_watch_timeout_handler) m_watch_timeout_handler.onTimeout(PlayerDevice.this);
            }
        }).start();
    }

    public boolean is_audio_support() {
        return m_capacity_set.contains("audio_support");
    }

    public boolean is_schedule_record() {
        return m_capacity_set.contains("schedule_record");
    }

    public boolean is_ftpemail_storage() {
        return m_capacity_set.contains("ftpemail_storage");
    }

    public boolean is_picture_capture() {
        return m_capacity_set.contains("picture_capture");
    }

    public boolean is_ptz_control() {
        return m_capacity_set.contains("ptz_control");
    }

    public boolean is_gpio_input() {
        return m_capacity_set.contains("gpio_input");
    }

    public boolean is_gpio_output() {
        return m_capacity_set.contains("gpio_output");
    }

    public boolean is_zh_cn() {
        return m_capacity_set.contains("zh_cn");
    }

    public boolean is_en_us() {
        return m_capacity_set.contains("en_us");
    }

    public boolean is_zh_tw() {
        return m_capacity_set.contains("zh_tw");
    }

    public boolean is_front_replay() {
        return m_capacity_set.contains("front_replay");
    }

    public boolean is_p2p_replay() {
        return m_capacity_set.contains("P2PReplay");
    }

    public boolean is_media_capabiltiy() {
        return m_capacity_set.contains("media_capabiltiy");
    }

    public boolean is_wireless_accesspoint() {
        return m_capacity_set.contains("wireless_accesspoint");
    }

    public boolean is_wireless_station() {
        return m_capacity_set.contains("wireless_station");
    }

    public boolean is_network_storage() {
        return m_capacity_set.contains("network_storage");
    }

    public boolean is_ptz_action() {
        return m_capacity_set.contains("ptz_action");
    }

    public boolean is_ircut_setting() {
        return m_capacity_set.contains("ircut_setting");
    }

    public boolean is_evdo_support() {
        return m_capacity_set.contains("evdo_support");
    }

    public boolean is_wcdma_support() {
        return m_capacity_set.contains("wcdma_support");
    }

    public boolean is_tdscdma_support() {
        return m_capacity_set.contains("tdscdma_support");
    }

    public boolean is_dst_support() {
        return m_capacity_set.contains("dst_support");
    }

    public boolean is_P2P_ONE_LINK() {
        return m_capacity_set.contains("P2P_ONE_LINK");
    }
}
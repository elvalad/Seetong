package com.seetong.app.seetong;

import com.android.utils.SharePreferenceUtil;
import com.seetong.app.seetong.comm.Define;

/**
 * Created by Administrator on 2014-05-07.
 */
public class Config {
    public static int m_ptz_step = 5;
    public static int m_view_num = 4;
    public static boolean m_auto_play = false;
    public static boolean m_enable_alarm = false;
    public static int m_alarm_sound = 0;
    public static int m_alarm_sound_res_id = 0;
    public static boolean m_not_prompt_modify_password = false;
    public static boolean m_in_call_mode = false;
    public static boolean m_show_video_info = false;
    public static boolean m_show_alias = false;
    public static boolean m_show_devid = true;
    public static boolean m_video_fill_preview = true;
    public static boolean m_enable_hardware_decode = false;
    public static int m_polling_time = 5;
    public static int m_frame_buffer_size = 50;

    public static void loadData() {
        SharePreferenceUtil spu = Global.m_spu;
        m_ptz_step = spu.loadIntSharedPreference(Define.PTZ_SPEED);
        if (m_ptz_step < 1 || m_ptz_step > 10) m_ptz_step = 5;
        m_auto_play = spu.loadBooleanSharedPreference(Define.IS_AUTO_PLAY);
        m_view_num = spu.loadIntSharedPreference(Define.VIDEO_VIEW_NUMS);
        if (m_view_num < 1) m_view_num = 4;
        m_enable_alarm = spu.loadBooleanSharedPreference(Define.CFG_ENABLE_ALARM);
        m_alarm_sound = spu.loadIntSharedPreference(Define.CFG_ALARM_SOUND_INDEX);
        m_alarm_sound_res_id = Global.m_resSound[m_alarm_sound];
        m_not_prompt_modify_password = spu.loadBooleanSharedPreference(Define.CFG_NOT_PROMPT_MODIFY_PASSWORD);
        m_in_call_mode = spu.loadBooleanSharedPreference(Define.CFG_IN_CALL_MODE);
        m_show_video_info = spu.loadBooleanSharedPreference(Define.CFG_SHOW_VIDEO_INFO);
        m_show_alias = spu.loadBooleanSharedPreference(Define.CFG_SHOW_ALIAS);
        m_show_devid = spu.loadBooleanSharedPreference(Define.CFG_SHOW_DEVID, true);
        m_video_fill_preview = spu.loadBooleanSharedPreference(Define.CFG_VIDEO_PREVIEW, true);
        m_enable_hardware_decode = spu.loadBooleanSharedPreference(Define.CFG_ENABLE_HARDWARE_DECODE);
        m_polling_time = spu.loadIntSharedPreference(Define.POLLING_TIME);
        if (m_polling_time < 1 || m_polling_time > 10) m_polling_time = 5;
        m_frame_buffer_size = spu.loadIntSharedPreference(Define.CFG_FRAME_BUFFER_SIZE);
        if (m_frame_buffer_size < 20 || m_frame_buffer_size > 100) m_frame_buffer_size = 50;
    }

    public static void saveData() {
        SharePreferenceUtil spu = Global.m_spu;
        spu.saveSharedPreferences(Define.PTZ_SPEED, m_ptz_step);
        spu.saveSharedPreferences(Define.VIDEO_VIEW_NUMS, m_view_num);
        spu.saveSharedPreferences(Define.IS_AUTO_PLAY, m_auto_play);
        spu.saveSharedPreferences(Define.CFG_ENABLE_ALARM, m_enable_alarm);
        spu.saveSharedPreferences(Define.CFG_ALARM_SOUND_INDEX, m_alarm_sound);
        spu.saveSharedPreferences(Define.CFG_NOT_PROMPT_MODIFY_PASSWORD, m_not_prompt_modify_password);
        spu.saveSharedPreferences(Define.CFG_IN_CALL_MODE, m_in_call_mode);
        spu.saveSharedPreferences(Define.CFG_SHOW_VIDEO_INFO, m_show_video_info);
        spu.saveSharedPreferences(Define.CFG_SHOW_ALIAS, m_show_alias);
        spu.saveSharedPreferences(Define.CFG_SHOW_DEVID, m_show_devid);
        spu.saveSharedPreferences(Define.CFG_VIDEO_PREVIEW, m_video_fill_preview);
        spu.saveSharedPreferences(Define.CFG_ENABLE_HARDWARE_DECODE, m_enable_hardware_decode);
        spu.saveSharedPreferences(Define.POLLING_TIME, m_polling_time);
        spu.saveSharedPreferences(Define.CFG_FRAME_BUFFER_SIZE, m_frame_buffer_size);
    }
}

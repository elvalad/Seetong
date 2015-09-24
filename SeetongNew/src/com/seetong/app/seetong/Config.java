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
    }
}

package com.android.system;

import android.content.Context;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;

/**
 * Created by Administrator on 2014-05-08.
 */
public class MediaPlayer {
    private static android.media.MediaPlayer m_mediaPlayer;
    public static void play(int id) {
        Context ctx = Global.m_ctx;
        if (m_mediaPlayer != null) m_mediaPlayer.stop();
        m_mediaPlayer = android.media.MediaPlayer.create(ctx, id);
        m_mediaPlayer.start();
    }

    public static void play() {
        play(Config.m_alarm_sound_res_id);
    }

    public static void stop() {
        if (null == m_mediaPlayer) return;
        m_mediaPlayer.stop();
    }


}

package com.android.system;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

/**
 * Created by Administrator on 2014-05-06.
 */
public class MessageNotification {
    private Context m_ctx;
    private NotificationManager m_manager = null;
    private static MessageNotification m_inst;
    public static MessageNotification getInstance() {
        if (null == m_inst) m_inst = new MessageNotification();
        return m_inst;
    }
    public void setContext(Context ctx) {
        m_ctx = ctx;
        this.m_manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void message(int icon, String title, String content)
    {
        message(icon, title, content, null);
    }

    public void message(int icon, String title, String content, Intent intent)
    {
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        //PendingIntent pi = PendingIntent.getBroadcast(m_ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pi = PendingIntent.getBroadcast(m_ctx, 0, intent, 0);
        Notification n = new Notification(icon, "通知", System.currentTimeMillis());
        n.setLatestEventInfo(m_ctx, title, content, pi);
        n.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        //n.defaults |= Notification.DEFAULT_SOUND;
        n.vibrate = new long[]{0, 100, 50, 200, 50, 300};
        n.ledARGB = Color.BLUE;
        n.ledOnMS = 300;
        n.ledOffMS = 1000;
        m_manager.notify(0, n);
    }

    public void cancelAll() {
        m_manager.cancelAll();
    }
}

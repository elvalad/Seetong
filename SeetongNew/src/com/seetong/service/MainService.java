package com.seetong.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.seetong.app.seetong.Global;
import ipc.android.sdk.com.TPS_AlarmInfo;

/**
 * Created by Administrator on 2014-05-13.
 */
public class MainService extends Service {
    Messenger m_msger;
    IncomingHandler m_handler;
    @Override
    public void onCreate() {
        super.onCreate();
        m_handler = new IncomingHandler();
        m_msger = new Messenger(m_handler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_msger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Global.MSG_VIDEO_UI_DESTROYED:
                    Log.w("Service", "video ui destroyed");
                    break;
                case Global.MSG_ADD_ALARM_DATA:
                    TPS_AlarmInfo ta = (TPS_AlarmInfo) msg.obj;
                    Log.d("Service", "alarm msg:" + ta);
                    break;
            }
            super.handleMessage(msg);
        }
    }
}

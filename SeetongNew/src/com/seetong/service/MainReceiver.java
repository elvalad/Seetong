package com.seetong.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.MainActivity;
import com.seetong.app.seetong.ui.MainActivity2;
import ipc.android.sdk.com.TPS_AlarmInfo;

/**
 * Created by Administrator on 2014-05-13.
 */
public class MainReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String devId = intent.getStringExtra(MainActivity2.DEVICE_ID_KEY);
        if (!TextUtils.isEmpty(devId)) {
            Intent it = new Intent(context, MainActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            //it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.putExtra(MainActivity2.DEVICE_ID_KEY, devId);
            context.startActivity(it);
        }
    }
}

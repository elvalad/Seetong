package com.seetong5.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.ui.MainActivity2;
import com.seetong5.app.seetong.ui.utils.ActivityUtil;

/**
 * Created by Administrator on 2014-05-13.
 */
public class MainReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String devId = intent.getStringExtra(MainActivity2.DEVICE_ID_KEY);
        if (!TextUtils.isEmpty(devId)) {
            /*Intent it = new Intent(context, MainActivity2.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            //it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.putExtra(MainActivity2.DEVICE_ID_KEY, devId);
            context.startActivity(it);*/
            Intent it = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Global.m_spu.saveSharedPreferences(Define.EXIT_APP_NORMALLY, true);
            context.startActivity(it);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            ActivityUtil.stopApp();
        }
    }
}

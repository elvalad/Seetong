package com.seetong.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.ui.MainActivity;
import com.umeng.message.UTrack;
import com.umeng.message.UmengBaseIntentService;
import com.umeng.message.entity.UMessage;
import org.android.agoo.client.BaseConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-05-13.
 */
public class MyPushIntentService extends UmengBaseIntentService {
    private static final String TAG = MyPushIntentService.class.getName();

// 如果需要打开Activity，请调用Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)；否则无法打开Activity。

    @Override
    protected void onMessage(Context context, Intent intent) {
        super.onMessage(context, intent);
        try {
            String message = intent.getStringExtra(BaseConstants.MESSAGE_BODY);
            UMessage msg = new UMessage(new JSONObject(message));
            UTrack.getInstance(context).trackMsgClick(msg);
            // code  to handle message here
            // ...
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}

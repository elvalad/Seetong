package com.seetong5.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.ui.MainActivity2;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-05-13.
 */
public class MyPushMessageReceiver extends FrontiaPushMessageReceiver /*PushMessageReceiver*/ {
    @Override
    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        Log.d("PushMessage", "onBind,errorCode=" + errorCode + ",appId=" + appId);
        if (null == MainActivity2.m_this) return;
        PushManager.listTags(context);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {

    }

    @Override
    public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
        PushManager.listTags(context);
    }

    @Override
    public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
        PushManager.listTags(context);
    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
        if (Global.m_pushTags == null) Global.m_pushTags = new ArrayList<>();
        if (Global.m_clean_tags) {
            Global.m_clean_tags = false;
            PushManager.delTags(context, tags);
            Global.initPushTags(context);
            return;
        }

        Global.m_pushTags = tags;
        if (Global.m_pushTags == null) Global.m_pushTags = new ArrayList<>();
        Log.d("PushMessage", ":onListTags," + tags);
    }

    @Override
    public void onMessage(Context context, String message, String customContentString) {

    }

    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
        String notifyString = "通知点击  title=" + title + " description="
                + description + " customContent=" + customContentString;
        Log.d("PushMessage", notifyString);

        String devId = "";
        String alarmType = "";
        String channel = "";
        if (!TextUtils.isEmpty(customContentString)) {
            try {
                JSONObject json = new JSONObject(customContentString);
                devId = json.getString("devid");
                alarmType = json.getString("alarmtype");
                channel = json.getString("channel");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (!TextUtils.isEmpty(devId)) {
            if (!TextUtils.isEmpty(channel) && !channel.equals("0")) devId += "-" + channel;
            intent.putExtra(MainActivity2.DEVICE_ID_KEY, devId);
        }

        context.startActivity(intent);
    }
}

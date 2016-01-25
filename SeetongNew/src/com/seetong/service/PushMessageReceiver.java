package com.seetong.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.seetong.app.seetong.Global;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-05-13.
 */
public class PushMessageReceiver extends BroadcastReceiver {

    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        Global.initPushTags(context);
        PushManager.listTags(context);
    }

    public void onUnbind(Context context, int errorCode, String requestId) {

    }

    public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
        PushManager.listTags(context);
    }

    public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
        PushManager.listTags(context);
    }

    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
        Global.m_pushTags = tags;
    }

    public void onMessage(Context context, String message, String customContentString) {

    }

    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
        String notifyString = "通知点击  title=" + title + " description="
                + description + " customContent=" + customContentString;
        Log.d("PushMessage", notifyString);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
            String message = intent.getExtras().getString(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            String customContentString = intent.getStringExtra(PushConstants.EXTRA_EXTRA);
            onMessage(context, message, customContentString);
        } else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
            final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);
            int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE, PushConstants.ERROR_SUCCESS);
            String content = "";
            if (intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT) != null) {
                content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
            }

            switch (method) {
                case PushConstants.METHOD_BIND:
                    try {
                        JSONObject json = new JSONObject(content);
                        final String requestId = json.getString("request_id");
                        JSONObject response_params = json.getJSONObject("response_params");
                        final String appId = response_params.getString("appid");
                        final String userId = response_params.getString("user_id");
                        final String channelId = response_params.getString("channel_id");
                        onBind(context, errorCode, appId, userId, channelId, requestId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case PushConstants.METHOD_UNBIND:
                    try {
                        JSONObject json = new JSONObject(content);
                        final String requestId = json.getString("request_id");
                        onUnbind(context, errorCode, requestId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case PushConstants.METHOD_LISTTAGS:
                    try {
                        JSONObject json = new JSONObject(content);
                        final String requestId = json.getString("request_id");
                        JSONObject response_params = json.getJSONObject("response_params");
                        JSONArray groups = response_params.getJSONArray("groups");
                        List<String> tags = new ArrayList<>();
                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject item = groups.optJSONObject(i);
                            tags.add(item.getString("name"));
                        }
                        onListTags(context, errorCode, tags, requestId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case PushConstants.METHOD_SET_TAGS:
                    try {
                        JSONObject json = new JSONObject(content);
                        final String requestId = json.getString("request_id");
                        JSONObject response_params = json.getJSONObject("response_params");
                        JSONArray details = response_params.getJSONArray("details");
                        List<String> successTags = new ArrayList<>();
                        List<String> failTags = new ArrayList<>();
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject item = details.optJSONObject(i);
                            String result = item.getString("result");
                            if ("0".equals(result)) {
                                successTags.add(item.getString("tag"));
                            } else {
                                failTags.add(item.getString("name"));
                            }
                        }
                        onSetTags(context, errorCode, successTags, failTags, requestId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case PushConstants.METHOD_DEL_TAGS:
                    try {
                        JSONObject json = new JSONObject(content);
                        final String requestId = json.getString("request_id");
                        JSONObject response_params = json.getJSONObject("response_params");
                        JSONArray details = response_params.getJSONArray("details");
                        List<String> successTags = new ArrayList<>();
                        List<String> failTags = new ArrayList<>();
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject item = details.optJSONObject(i);
                            String result = item.getString("result");
                            if ("0".equals(result)) {
                                successTags.add(item.getString("tag"));
                            } else {
                                failTags.add(item.getString("name"));
                            }
                        }
                        onDelTags(context, errorCode, successTags, failTags, requestId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else if (intent.getAction().equals(PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
            String extra = intent.getStringExtra(PushConstants.EXTRA_EXTRA);
            String title = "";
            String description = "";
            String customContentString = "";
            onNotificationClicked(context, title, description, customContentString);
        }
    }
}

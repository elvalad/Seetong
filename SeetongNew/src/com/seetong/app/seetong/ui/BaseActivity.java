package com.seetong.app.seetong.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.ShareRecord;
import com.seetong.app.seetong.tools.MD5;
import com.seetong.app.seetong.ui.utils.ActivityUtil;
import com.umeng.analytics.MobclickAgent;
import com.youku.uploader.IUploadResponseHandler;
import com.youku.uploader.YoukuUploader;
import com.youku.video.Api;
import com.youku.video.IResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2014-05-12.
 */
public class BaseActivity extends FragmentActivity {
    private static final String TAG = "UncaughtException";
    public Resources mResources;

    private static ProgressDialog m_tip_dlg;
    private static YoukuUploader m_youku;
    protected MyHandler m_handler;

    public static String T(Object t) {
        String _msg = "";
        if (t instanceof Integer) {
            _msg = Global.m_res.getString((Integer) t);
        } else if (t instanceof String) {
            _msg = (String) t;
        }

        return _msg;
    }

    public static Drawable D(int id) {
        return Global.m_res.getDrawable(id);
    }

    public String gStr(int id) {
        EditText editText = ((EditText) findViewById(id));
        String str = (editText == null) ? "" : editText.getText().toString().trim();
        return str;
    }

    public String gStr2(int id) {
        TextView editText = ((TextView) findViewById(id));
        String str = (editText == null) ? "" : editText.getText().toString().trim();
        return str;
    }

    public void sStr(int id, String str) {
        EditText editText = ((EditText) findViewById(id));
        if (editText != null) editText.setText(str.trim());
    }

    public synchronized static boolean isNullStr(String str) {
        boolean isOK = false;
        if (str == null || "".equals(str.trim())) {
            isOK = true;
        }
        return isOK;
    }

    public void hideInputPanel(View v) {
        if (((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).isActive()) {
            if (null == v) v = getCurrentFocus();
            if (null == v) return;
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void toast(Object msg) {
        final String message = T(msg);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Global.m_ctx, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Global.m_ctx, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void toast(Object msg, final int errNO) {
        String _msg = T(msg);
        final String message = _msg + "(" + errNO + ")";
        if (Looper.myLooper() != Looper.getMainLooper()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Global.m_ctx, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Global.m_ctx, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMessage(Handler h, int what, int arg1, int arg2, Object obj) {
        Message msg = h.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        h.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_handler = new MyHandler(this);
        mResources = getResources();
        m_tip_dlg = new ProgressDialog(this);
        m_youku = YoukuUploader.getInstance(Global.YOUKU_CLIENT_ID, Global.YOUKU_CLIENT_SECRET, this);
        Global.mPushAgent.onAppStart();
        ActivityUtil.addToList(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void uploadToYouku(final String fileName) {
        String md5 = new MD5().fromFile(fileName);
        ShareRecord record = new ShareRecord().findByMd5(md5);
        if (null != record) {
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", "616432358@qq.com");
        params.put("password", "18247739");
        params.put("access_token", "3067f30985a96761ae137115842f572d");

        HashMap<String, String> uploadInfo = new HashMap<String, String>();
        uploadInfo.put("title", "shared by seetong ");
        uploadInfo.put("tags", "seetong, IP Camera, 原创");
        uploadInfo.put("file_name", fileName);

        m_youku.upload(params, uploadInfo, new IUploadResponseHandler() {
            private Api m_video = new Api(Global.YOUKU_CLIENT_ID, Global.YOUKU_CLIENT_SECRET, Global.m_ctx);
            @Override
            public void onStart() {
                m_tip_dlg.setTitle(T(R.string.dlg_upload_file_tip));
                m_tip_dlg.show();
            }

            @Override
            public void onProgressUpdate(int counter) {
                m_tip_dlg.setTitle(counter + "%");
            }

            @Override
            public void onSuccess(JSONObject response) {
                m_video.setResponseHandler(new IResponseHandler() {
                    @Override
                    public void on_show_basic(int result, JSONObject response) {
                        if (-1 == result) {
                            toast(T(R.string.dlg_upload_file_fail_tip));
                            return;
                        }

                        try {
                            String md5 = new MD5().fromFile(fileName);
                            String video_url = response.getString("link");
                            ShareRecord record = new ShareRecord();
                            record.setMd5(md5);
                            record.setFileName(fileName);
                            record.setShareUrl(video_url);
                            record.insert();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            toast(T(R.string.dlg_upload_file_fail_tip));
                        }
                    }
                });

                try {
                    String video_id = response.getString("video_id");
                    m_video.show_basic(video_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast(T(R.string.dlg_upload_file_fail_tip));
                }
            }

            @Override
            public void onFailure(JSONObject errorResponse) {
                int code = 0;
                String desc = "";
                try {
                    code = errorResponse.getJSONObject("error").getInt("code");
                    desc = errorResponse.getJSONObject("error").getString("description");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 已经上传过了
                if (120010111 == code) {
                    toast(T(R.string.dlg_share_content_complete_tip));
                    return;
                }

                toast(T(R.string.dlg_upload_file_fail_tip) + ", " + desc);
            }

            @Override
            public void onFinished() {
                m_tip_dlg.dismiss();
            }
        });
    }

    protected static class MyHandler extends Handler {
        BaseActivity m_ui;
        public MyHandler(BaseActivity ui) {
            m_ui = ui;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            m_ui.handleMessage(msg);
        }
    };

    private static class OneKeyShareHandler extends Handler {
        BaseActivity m_ui;
        public OneKeyShareHandler(BaseActivity ui) {
            m_ui = ui;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Global.MSG_SHARE_START_SHARE:
                    m_tip_dlg.show();
                    break;
                case Global.MSG_SHARE_RESULT_COMPLETE:
                    m_tip_dlg.dismiss();
                    MainActivity2.m_this.toast(T(R.string.dlg_share_content_complete_tip));
                    break;
                case Global.MSG_SHARE_RESULT_ERROR:
                    m_tip_dlg.dismiss();
                    MainActivity2.m_this.toast(T(R.string.dlg_share_content_error_tip));
                    break;
                default:
                    break;
            }
        }
    };

    protected void handleMessage(android.os.Message msg) {

    }

    public static boolean isTopActivity(String className){
        ActivityManager activityManager = (ActivityManager) Global.m_ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        return tasksInfo.size() > 0 && className.equals(tasksInfo.get(0).topActivity.getClassName());
    }
}

package com.seetong.app.seetong.ui;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import com.seetong.app.seetong.Global;

import java.util.HashMap;

public class OneKeyShareCallback implements PlatformActionListener, ShareContentCustomizeCallback {

    private Handler m_handler;

    OneKeyShareCallback(Handler handler) {
        m_handler = handler;
    }

	public void onComplete(Platform plat, int action, HashMap<String, Object> res) {
		Log.d(getClass().getSimpleName(), res.toString());
		// 在这里添加分享成功的处理代码
        if (null == m_handler) return;
        Message msg = Message.obtain();
        if (null == msg) return;
        msg.what = Global.MSG_SHARE_RESULT_COMPLETE;
        msg.arg1 = action;
        msg.obj = plat;
        m_handler.sendMessage(msg);
	}

	public void onError(Platform plat, int action, Throwable t) {
		t.printStackTrace();
        if (null == m_handler) return;
        Message msg = Message.obtain();
        if (null == msg) return;
        msg.what = Global.MSG_SHARE_RESULT_ERROR;
        msg.arg1 = action;
        msg.obj = plat;
        m_handler.sendMessage(msg);
	}

	public void onCancel(Platform plat, int action) {
        if (null == m_handler) return;
        Message msg = Message.obtain();
        if (null == msg) return;
        msg.what = Global.MSG_SHARE_RESULT_ERROR;
        msg.arg1 = action;
        msg.obj = plat;
        m_handler.sendMessage(msg);
	}

    @Override
    public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
        String name = platform.getName();
        if ("Wechat".equals(name)) {
            /*BitmapFactory.Options o = new BitmapFactory.Options();
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(paramsToShare.getImagePath(), o);
            o.inJustDecodeBounds = false;
            Bitmap imageData = BitmapFactory.decodeFile(paramsToShare.getImagePath(), o);
            paramsToShare.setImageData(imageData);
            paramsToShare.setImagePath("");*/
            /*if (!"".equals(paramsToShare.getImageUrl())) {
                paramsToShare.setShareType(Platform.SHARE_VIDEO);
            }*/
        } else if ("WechatMoments".equals(name)) {
            // 朋友圈发内容必须要有一张图片
            if (!"".equals(paramsToShare.getUrl())) {
                paramsToShare.setImageUrl("http://www.qqstore.net/images/ico_launcher.png");
                paramsToShare.setShareType(Platform.SHARE_VIDEO);
            }
        }

    }
}

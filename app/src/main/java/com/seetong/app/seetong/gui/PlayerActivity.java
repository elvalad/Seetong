package com.seetong.app.seetong.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.seetong.app.seetong.R;

/**
 * PlayerActivity 是播放设备录像的 Activity，它在 DeviceFragment 包含设备信息时，点击会进入.
 * 它自身包括播放一个窗口的录像和四个窗口的录像.
 *
 * Created by gmk on 2015/9/13.
 */
public class PlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);
    }
}

package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.Window;

import com.seetong.app.seetong.R;

/**
 * SettingActivity 主要用于设置一些 Seetong 的信息，会控制一些 Seetong 的全局变量来对
 * 整个应用产生影响.
 *
 * Created by gmk on 2015/9/12.
 */
public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);
        initWidget();
    }

    private void initWidget() {}
}

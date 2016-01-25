package com.seetong.app.seetong.ui;

import android.os.Bundle;

import com.seetong.app.seetong.R;

/**
 * AlarmActivity 主要功能是显示 Seetong 的报警信息，这些信息通过相关设备信
 * 息从服务器端获取. 点击更多的 Tab 选项，进入 MoreFragment，在 MoreFragment
 * 布局中点击关于我们的 Button 即可跳转到此 Activity，此 Activity 应该属于最
 * 后的显示页面，所以在按回退键时应该 finish 此 Activity，退回到上一级的 MainActivity.
 *
 * Created by gmk on 2015/9/12.
 */
public class AlarmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        initWidget();
    }

    /**
     *  初始化此 Activity 的基本组件.
     */
    private void initWidget() {}
}


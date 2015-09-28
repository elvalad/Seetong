package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.Window;

import com.seetong.app.seetong.R;

/**
 * AboutActivity 主要功能是显示 Seetong 的介绍，显示一些基本的软件信息.
 * 点击更多的 Tab 选项，进入 MoreFragment，在 MoreFragment 布局中点击关
 * 于我们的 Button 即可跳转到此 Activity，此 Activity 应该属于最后的显示
 * 页面，所以在按回退键时应该 finish 此 Activity，退回到上一级的 MainActivity.
 *
 * Created by gmk on 2015/9/12.
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);
        initWidget();
    }

    /**
     *  初始化此 Activity 的基本组件.
     */
    private void initWidget() {}
}


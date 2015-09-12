package com.seetong.app.seetong.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.seetong.app.seetong.R;

/**
 * Created by Administrator on 2015/9/12.
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);
        initWidget();
    }

    private void initWidget() {}
}

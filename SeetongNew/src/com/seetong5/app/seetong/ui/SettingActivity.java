package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.Window;

import com.seetong.app.seetong.R;

/**
 * SettingActivity ��Ҫ��������һЩ Seetong ����Ϣ�������һЩ Seetong ��ȫ�ֱ�������
 * ����Ӧ�ò���Ӱ��.
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

package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.Window;

import com.seetong.app.seetong.R;

/**
 * AboutActivity ��Ҫ��������ʾ Seetong �Ľ��ܣ���ʾһЩ�����������Ϣ.
 * �������� Tab ѡ����� MoreFragment���� MoreFragment �����е����
 * �����ǵ� Button ������ת���� Activity���� Activity Ӧ������������ʾ
 * ҳ�棬�����ڰ����˼�ʱӦ�� finish �� Activity���˻ص���һ���� MainActivity.
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
     *  ��ʼ���� Activity �Ļ������.
     */
    private void initWidget() {}
}


package com.seetong.app.seetong.gui;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.seetong.app.seetong.R;

/**
 * MoreFragment 是更多页面的 Fragment，它也是包含于 MainActivity 的 Tabhost 中，它自身包含一些基本的
 * 组件来完成相关的操作，通过点击相关 Button，会进入到其他 Activity.
 *
 * Created by gmk on 2015/9/11.
 */
public class MoreFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more, container);

        initWidget(view);

        return view;
    }

    private void initWidget(View view) {
        Button alarmButton = (Button) view.findViewById(R.id.more_alarm_button);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreFragment.this.getActivity(), AlarmActivity.class);
                startActivity(intent);
            }
        });

        Button settingButton = (Button) view.findViewById(R.id.more_setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreFragment.this.getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        Button aboutButton = (Button) view.findViewById(R.id.more_about_button);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreFragment.this.getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });

        Button exitButton = (Button) view.findViewById(R.id.more_exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreFragment.this.getActivity().finish();
            }
        });
    }
}

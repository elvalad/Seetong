package com.seetong.app.seetong.gui;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.seetong.app.seetong.R;

/**
 * Created by Administrator on 2015/9/11.
 */
public class DeviceFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device, container);
        initWidget(view);
        return view;
    }

    private void initWidget(final View view) {
        ImageButton deviceAddButton = (ImageButton) view.findViewById(R.id.device_add);
        deviceAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:启动增加设备页面
                Intent intent = new Intent(DeviceFragment.this.getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}

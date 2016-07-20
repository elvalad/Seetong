package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seetong.app.seetong.R;

public class DeviceNoMsgFragment extends BaseFragment {

    public static DeviceNoMsgFragment newInstance() {
        return new DeviceNoMsgFragment();
    }

    public DeviceNoMsgFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_no_msg, container, false);
        return view;
    }
}


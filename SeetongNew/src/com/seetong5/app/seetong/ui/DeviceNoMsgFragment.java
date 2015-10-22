package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seetong5.app.seetong.R;

/**
 * DeviceNoMsgFragment 是显示当前用户没有设备信息时的 Fragment，它只有在没有任何信息时才会
 * 显示. 它接 DeviceListFragment 一样是嵌套在 DeviceFragment 中使用的.
 *
 * Created by gmk on 2015/9/13.
 */
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


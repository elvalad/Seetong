package com.seetong.app.seetong.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.Toast;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import ipc.android.sdk.impl.DeviceInfo;

import java.util.List;

/**
 * DeviceFragment 是用于显示设备列表相关的 Fragment，它在 MainActivity 中被添加到 TabHost 中.
 * Seetong 登录后会进入 MainActivity，会默认显示 DeviceFragment.
 * DeviceFragment 中又包含两个不同的 Fragment，一个是 DeviceListFragment，另一个是 DeviceNoMsgFragment，
 * Seetong通过一个线程从服务器获取信息监测当前账号下是否有设备，如果有则显示 DeviceListFragment，否则显示
 * DeviceNoMsgFragment.注意在增加设备时和MainActivity2之间的交互，使用回调函数实现.
 * 注意 Fragment 嵌套使用时要使用 android.support.v4 兼容包.
 *
 * Created by gmk on 2015/9/11.
 */
public class DeviceFragment2 extends BaseFragment {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setDeviceFragment(this);
        view = inflater.inflate(R.layout.device2, container);
        initWidget(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            /* 此函数用于 Fragment 嵌套，此时默认显示 DeviceListFragment */
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.device_fragment_container, DeviceNoMsgFragment.newInstance())
                    .commit();
        }
    }

    /**
     * 初始化此 Fragment 中的基本组件.
     */
    private void initWidget(final View view) {
        ImageButton deviceAddButton = (ImageButton) view.findViewById(R.id.device_add);
        deviceAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 启动增加设备页面 */
                Intent intent = new Intent(DeviceFragment2.this.getActivity(), AddDeviceActivity.class);
                intent.putExtra(Constant.ENTER_TYPES, 1);
                startActivityForResult(intent, Constant.ADD_DEVICE_REQ_ID);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case Constant.ADD_DEVICE_REQ_ID:
                /* TODO: 收到此返回消息后需要动态更新设备列表 */
                final String devId = data.getStringExtra(Constant.DEVICE_INFO_KEY);
                String xml = data.getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
                MainActivity2.m_this.onNotifyDevData(xml, new MainActivity2.ParseDevListResult() {
                    @Override
                    public void onResult(List<PlayerDevice> devices) {
                        MainActivity2.m_this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initWidget(view);
                            }
                        });
                    }
                });
                break;
        }
    }

    public void updateDeviceFragment(int listSize) {
        if (listSize > 0) {
            showDeviceListFragment();
        } else {
            showDeviceNoMsgFragment();
        }
    }

    private void showDeviceNoMsgFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.device_fragment_container, DeviceNoMsgFragment.newInstance())
                .commit();
    }

    private void showDeviceListFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.device_fragment_container, DeviceListFragment.newInstance())
                .commit();
    }
}


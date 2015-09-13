package com.seetong.app.seetong.gui;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.seetong.app.seetong.R;

/**
 * DeviceFragment 是用于显示设备列表相关的 Fragment，它在 MainActivity 中被添加到 TabHost 中.
 * Seetong 登录后会进入 MainActivity，会默认显示 DeviceFragment.
 * DeviceFragment 中又包含两个不同的 Fragment，一个是 DeviceListFragment，另一个是 DeviceNoMsgFragment，
 * Seetong通过一个线程从服务器获取信息监测当前账号下是否有设备，如果有则显示 DeviceListFragment，否则显示
 * DeviceNoMsgFragment.
 * 注意 Fragment 嵌套使用时要使用 android.support.v4 兼容包.
 *
 * Created by Administrator on 2015/9/11.
 */
public class DeviceFragment extends Fragment {
    // TODO:创建一个线程检测devicelist是否有数据，如果有则显示devicelist_fragment否则显示devicenomsg_fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device, container);
        initWidget(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            /* 此函数用于 Fragment 嵌套，此时默认显示 DeviceListFragment */
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.device_fragment_container, DeviceListFragment.newInstance())
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
                // TODO:启动增加设备页面
                Intent intent = new Intent(DeviceFragment.this.getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}

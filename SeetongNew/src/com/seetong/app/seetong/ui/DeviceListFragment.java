package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeviceListFragment 是显示设备列表的 Fragment， 它嵌套使用在 DeviceFragment 中。
 * 它只有在检测到当前用户有设备信息时才会显示.
 * 它的主要作用是通过 ListView 设置自定义的 DeviceListAdapter 显示设备列表，每个 Item
 * 点击之后会进入到相关设备的播放页面。
 * 这里需要根据服务器端的信息将每个设备的信息填入到对应的 Map 中.
 *
 * Created by gmk on 2015/9/13.
 */
public class DeviceListFragment extends Fragment {
    public static String TAG = DeviceListFragment.class.getName();
    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    private BaseAdapter adapter;

    public static DeviceListFragment newInstance() {
        return new DeviceListFragment();
    }

    public DeviceListFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_list, container, false);

        ListView listView = (ListView) view.findViewById(R.id.device_list);

        getData();
        adapter = new DeviceListAdapter2(MainActivity2.m_this, data);
        listView.setAdapter(adapter);

        return view;
    }

    // TODO:实际需要从服务器获取的设备相关数据
    private void getData() {
        LibImpl.putDeviceList(Global.getDeviceList());
        //Log.d(TAG, "Device size is " + Global.getDeviceList().size());
        for (int i = 0; i < Global.getDeviceList().size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            //Log.d(TAG, "Device is" + Global.getSelfDeviceList().get(i).toString());
            map.put("device", Global.getSelfDeviceList().get(i));
            map.put("device_image", R.drawable.tps_list_nomsg);
            map.put("device_state", R.string.device_state_off);
            map.put("device_name", "Device " + i);
            map.put("device_num", i);
            data.add(map);
        }
    }
}


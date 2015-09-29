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
 * DeviceListFragment ����ʾ�豸�б�� Fragment�� ��Ƕ��ʹ���� DeviceFragment �С�
 * ��ֻ���ڼ�⵽��ǰ�û����豸��Ϣʱ�Ż���ʾ.
 * ������Ҫ������ͨ�� ListView �����Զ���� DeviceListAdapter ��ʾ�豸�б�ÿ�� Item
 * ���֮�����뵽����豸�Ĳ���ҳ�档
 * ������Ҫ���ݷ������˵���Ϣ��ÿ���豸����Ϣ���뵽��Ӧ�� Map ��.
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

    // TODO:ʵ����Ҫ�ӷ�������ȡ���豸�������
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


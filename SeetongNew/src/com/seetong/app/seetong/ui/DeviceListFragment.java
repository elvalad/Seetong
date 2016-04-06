package com.seetong.app.seetong.ui;

import android.os.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.widget.WrapperListAdapter;
import com.example.AsymmetricGridView.library.Utils;
import com.example.AsymmetricGridView.library.widget.AsymmetricGridView;
import com.example.AsymmetricGridView.library.widget.AsymmetricGridViewAdapter;
import com.example.AsymmetricGridView.model.DemoItem;
import com.example.AsymmetricGridView.widget.DefaultListAdapter;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import ipc.android.sdk.com.SDK_CONSTANT;

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
public class DeviceListFragment extends BaseFragment {
    public static String TAG = DeviceListFragment.class.getName();
    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> searchData = new ArrayList<>();
    private DeviceListAdapter2 adapter;
    private DeviceListAdapter2 searchAdapter;
    private View view;
    private ListView listView;
    private ListView searchListView;

    private AsymmetricGridView multiListView;
    private MultiListAdapter multiAdapter;
    private int currentOffset = 0;
    private AsymmetricGridViewAdapter<DeviceListItem> asymmetricAdapter;

    public static DeviceListFragment newInstance() {
        return new DeviceListFragment();
    }

    public DeviceListFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*view = inflater.inflate(R.layout.device_list, container, false);

        listView = (ListView) view.findViewById(R.id.device_list);

        getData();
        adapter = new DeviceListAdapter2(MainActivity2.m_this, data);
        listView.setAdapter(adapter);

        return view;*/

        view = inflater.inflate(R.layout.multi_device_list, container, false);
        multiListView = (AsymmetricGridView) view.findViewById(R.id.multiListView);
        getData();
        multiAdapter = new MultiListAdapter(this.getActivity(), multiListView, getMoreItems(data.size()) , data);
        if (multiAdapter instanceof WrapperListAdapter) {
            asymmetricAdapter = (AsymmetricGridViewAdapter) ((WrapperListAdapter) multiAdapter).getWrappedAdapter();
        } else {
            asymmetricAdapter = multiAdapter;
        }

        multiListView.setRequestedColumnCount(4);
        multiListView.setRequestedHorizontalSpacing(Utils.dpToPx(this.getActivity(), 3));
        multiListView.setAdapter(multiAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (!listView.isStackFromBottom()) {
            listView.setStackFromBottom(true);
        }
        listView.setStackFromBottom(false);*/
    }

    /* 实际需要从服务器获取的设备相关数据 */
    private void getData() {
        data.clear();
        LibImpl.putDeviceList(Global.getDeviceList());
        for (int i = 0; i < Global.getDeviceList().size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            //map.put("device", Global.getSortedDeviceList().get(i));
            map.put("device", Global.getDeviceList().get(i));
            map.put("device_image", R.drawable.tps_list_nomsg);
            map.put("device_state", R.string.device_state_off);
            map.put("device_name", "Device " + i);
            map.put("device_num", i);
            data.add(map);
        }
    }

    private void getSearchData(CharSequence s) {
        searchData.clear();
        for (int i = 0; i < Global.getDeviceList().size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            //PlayerDevice dev = Global.getSortedDeviceList().get(i);
            PlayerDevice dev = Global.getDeviceList().get(i);
            String devID = dev.m_dev.getDevId();
            String devAlias = LibImpl.getInstance().getDeviceAlias(dev.m_dev);
            if (devID != null && (devID.contains(s) || devAlias.contains(s))) {//匹配设备ID和别名
                map.put("device", dev);
                map.put("device_image", R.drawable.tps_list_nomsg);
                map.put("device_state", R.string.device_state_off);
                map.put("device_name", "Device " + i);
                map.put("device_num", i);
                searchData.add(map);
            }
        }
    }

    public void showSearchDeviceList(CharSequence s) {
        searchListView = (ListView) view.findViewById(R.id.device_search_list);
        getSearchData(s);
        searchAdapter = new DeviceListAdapter2(MainActivity2.m_this, searchData);
        searchListView.setAdapter(searchAdapter);
        searchAdapter.notifyDataSetChanged();
        searchListView.setVisibility(View.VISIBLE);
        multiListView.setVisibility(View.GONE);
    }

    public void showDeviceList() {
        if (searchListView != null) {
            searchListView.setVisibility(View.GONE);
        }
        multiListView.setVisibility(View.VISIBLE);
    }

    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_DEV_LIST:
                getData();
                Log.e(TAG, "intems offset : " + currentOffset + " device size : " + data.size());
                if (data.size() >= currentOffset) {
                    asymmetricAdapter.appendItems(getMoreItems(data.size() - currentOffset));
                } else {
                    currentOffset = 0;
                    asymmetricAdapter.setItems(getMoreItems(data.size()));
                }
                multiAdapter.updateDeviceList();
                break;
            case Define.MSG_UPDATE_DEV_ALIAS:
                PlayerDevice playerDevice = (PlayerDevice)msg.obj;
                multiAdapter.updateDeviceAlias(playerDevice);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                multiAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                getData();
                multiAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                getData();
                multiAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                getData();
                multiAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                multiAdapter.notifyDataSetChanged();
                break;
            case Define.MSG_ENABLE_ALIAS:
                multiAdapter.updateDeviceList();
                break;
            default:
                break;

        }
        /*switch (msg.what) {
            case Define.MSG_UPDATE_DEV_ALIAS:
                PlayerDevice playerDevice = (PlayerDevice)msg.obj;
                adapter.updateDeviceAlias(playerDevice);
                break;
            case Define.MSG_UPDATE_DEV_LIST:
                getData();
                adapter.updateDeviceList();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                adapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                getData();
                adapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                getData();
                adapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                getData();
                adapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                adapter.notifyDataSetChanged();
                break;
            case Define.MSG_ENABLE_ALIAS:
                adapter.updateDeviceList();
                break;
            default:
                break;
        }*/

        return false;
    }

    private List<DeviceListItem> getMoreItems(int qty) {
        List<DeviceListItem> items = new ArrayList<>();
        int colSpan;
        int rowSpan;
        for (int i = 0; i < qty; i++) {
            PlayerDevice dev = (PlayerDevice)data.get(i + currentOffset).get("device");
            if (!dev.isNVR()) {
                colSpan = 4;
                rowSpan = 2;
            } else {
                colSpan = 2;
                rowSpan = 1;
            }

            DeviceListItem item = new DeviceListItem(colSpan, rowSpan, currentOffset + i);
            items.add(item);
        }
        currentOffset += qty;

        return items;
    }
}


package com.seetong.app.seetong.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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
 * Created by Administrator on 2016/6/16.
 */
public class DeviceListFragment2 extends BaseFragment {

    public static String TAG = DeviceListFragment2.class.getName();
    private View view;
    private PullToRefreshListView mListView;
    private ListViewAdapter mListViewAdapter;
    private ArrayList<ArrayList<HashMap<String,Object>>> mArrayList = new ArrayList<>();

    private ListView searchListView;
    private List<Map<String, Object>> searchData = new ArrayList<>();
    private DeviceListAdapter2 searchAdapter;

    public static DeviceListFragment2 newInstance() {
        return new DeviceListFragment2();
    }

    public DeviceListFragment2() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.device_list, container, false);
        searchListView = (ListView) view.findViewById(R.id.device_search_list);
        getSearchData("");
        searchAdapter = new DeviceListAdapter2(MainActivity2.m_this, searchData);
        searchListView.setAdapter(searchAdapter);

        mListView = (PullToRefreshListView) view.findViewById(R.id.device_list);
        getData();
        mListViewAdapter = new ListViewAdapter(mArrayList, this.getActivity());
        mListView.setAdapter(mListViewAdapter);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        return view;
    }

    private void getData(){
        mArrayList.clear();
        LibImpl.putDeviceList(Global.getDeviceList());
        HashMap<String, Object> hashMap;
        ArrayList<HashMap<String,Object>> arrayListForEveryGridView;
        List<String> devIdList = Global.getDeviceIdList();

        for (int i = 0; i < devIdList.size(); i++) {
            arrayListForEveryGridView = new ArrayList<>();
            List<PlayerDevice> devList = Global.getDeviceByNvrId(devIdList.get(i));
            assert devList != null;
            for (int j = 0; j < devList.size(); j++) {
                hashMap = new HashMap<>();
                hashMap.put("content", "i="+i+" ,j="+j);
                hashMap.put("device", devList.get(j));
                arrayListForEveryGridView.add(hashMap);
            }
            mArrayList.add(arrayListForEveryGridView);
        }
    }

    private void getSearchData(CharSequence s) {
        searchData.clear();
        for (int i = 0; i < Global.getDeviceList().size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            PlayerDevice dev = Global.getDeviceList().get(i);
            String devID = dev.m_dev.getDevId();
            String devAlias = LibImpl.getInstance().getDeviceAlias(dev.m_dev);
            if (devID != null && (devID.contains(s) || devAlias.contains(s))) {
                map.put("device", dev);
                map.put("device_image", R.drawable.tps_list_nomsg);
                map.put("device_state", R.string.device_state_off);
                map.put("device_name", "Device " + i);
                map.put("device_num", i);
                searchData.add(map);
            }
        }
    }

    public void showDeviceList() {
        if (searchListView != null) {
            searchListView.setVisibility(View.GONE);
        }

        if (mListView != null) {
            mListView.setVisibility(View.VISIBLE);
        }
    }

    public void showSearchDeviceList(CharSequence s) {
        getSearchData(s);
        searchAdapter.notifyDataSetChanged();
        searchListView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_DEV_LIST:
                getData();
                mListViewAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                mListViewAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                mListViewAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                mListViewAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                mListViewAdapter.notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                mListViewAdapter.notifyDataSetChanged();
                break;
            case Define.MSG_UPDATE_DEV_ALIAS:
                mListViewAdapter.notifyDataSetChanged();
                break;
            case Define.MSG_ENABLE_ALIAS:
                mListViewAdapter.notifyDataSetChanged();
                break;
        }

        return false;
    }

    private class GetDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                int ret = LibImpl.getInstance().getFuncLib().ResumeDevCom();
                if (0 != ret) {
                    Log.d(TAG, "device list pull down refresh ResumeDevCom fail!");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListView.onRefreshComplete();
        }
    }
}

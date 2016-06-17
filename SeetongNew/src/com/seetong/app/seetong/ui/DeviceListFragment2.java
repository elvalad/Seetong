package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/6/16.
 */
public class DeviceListFragment2 extends BaseFragment {

    public static String TAG = DeviceListFragment2.class.getName();
    private View view;
    private ListView mListView;
    private ListViewAdapter mListViewAdapter;
    private ArrayList<ArrayList<HashMap<String,Object>>> mArrayList = new ArrayList<>();

    public static DeviceListFragment2 newInstance() {
        return new DeviceListFragment2();
    }

    public DeviceListFragment2() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.device_list, container, false);
        mListView = (ListView) view.findViewById(R.id.device_list);
        getData();
        mListViewAdapter = new ListViewAdapter(mArrayList, this.getActivity());
        mListView.setAdapter(mListViewAdapter);
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

        /*for (int i = 0; i < 10; i++) {
            arrayListForEveryGridView=new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                hashMap=new HashMap<>();
                hashMap.put("content", "i="+i+" ,j="+j);
                arrayListForEveryGridView.add(hashMap);
            }
            mArrayList.add(arrayListForEveryGridView);
        }*/
    }

    public void showDeviceList() {
        mListView.setVisibility(View.VISIBLE);
    }

    public void showSearchDeviceList(CharSequence s) {

    }

    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_DEV_LIST:
                getData();
                mListViewAdapter.updateDevList();
                break;
        }

        return false;
    }
}

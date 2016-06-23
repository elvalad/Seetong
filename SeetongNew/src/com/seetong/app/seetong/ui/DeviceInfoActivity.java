package com.seetong.app.seetong.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.seetong.app.seetong.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/6/23.
 */
public class DeviceInfoActivity extends BaseActivity {

    private List<Map<String, String>> data = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        ListView listView = (ListView) findViewById(R.id.device_info_list);
        getData();
        adapter = new Adapter(DeviceInfoActivity.this, data);
        listView.setAdapter(adapter);
    }

    private void getData() {

    }

    class Adapter extends BaseAdapter {
        Context m_context;
        LayoutInflater m_inflater;
        List<Map<String, String>> m_data;

        public Adapter(Context context, List<Map<String, String>> data) {
            m_context = context;
            m_data = data;
            m_inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}

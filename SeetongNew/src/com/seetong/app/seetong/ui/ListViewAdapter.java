package com.seetong.app.seetong.ui;

import android.util.Log;
import android.widget.GridView;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ArrayList<HashMap<String, Object>>> mList;
    private Context mContext;

    public ListViewAdapter(ArrayList<ArrayList<HashMap<String, Object>>> mList, Context mContext) {
        super();
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        } else {
            return this.mList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mList == null) {
            return null;
        } else {
            return this.mList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.listview_item, parent, false);
            holder.textView = (TextView) convertView.findViewById(R.id.listview_item_textview);
            holder.gridView = (GridView) convertView.findViewById(R.id.listview_item_gridview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.mList != null) {
            ArrayList<HashMap<String, Object>> arrayListForEveryGridView = this.mList.get(position);
            GridViewAdapter gridViewAdapter = new GridViewAdapter(mContext, arrayListForEveryGridView);
            PlayerDevice dev = (PlayerDevice) arrayListForEveryGridView.get(0).get("device");
            holder.textView.setText(LibImpl.getInstance().getDeviceAlias(dev.m_dev));
            if (dev.isNVR()) {
                holder.gridView.setNumColumns(2);
            } else {
                holder.gridView.setNumColumns(1);
            }
            holder.gridView.setAdapter(gridViewAdapter);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView textView;
        GridView gridView;
    }
}

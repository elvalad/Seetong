package com.seetong.app.seetong.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.LanDeviceInfo;
import com.seetong.app.seetong.sdk.impl.LibImpl;

import java.util.List;

/**
 * Created by Administrator on 2016/10/24.
 */
public class LanSearchListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<LanDeviceInfo> data;

    private class ViewHolder {
        public TextView devId;
        public TextView devIp;
        public Button checkBtn;
        public boolean bChecked;
    }

    public LanSearchListAdapter(Context context, List<LanDeviceInfo> data) {
        this.context = context;
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lan_search_list_item, parent, false);
            viewHolder.devId = (TextView) convertView.findViewById(R.id.dev_id);
            viewHolder.devIp = (TextView) convertView.findViewById(R.id.dev_ip);
            viewHolder.checkBtn = (Button) convertView.findViewById(R.id.dev_check);
            viewHolder.bChecked = false;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (data.get(position).getEntry().getCloudId().equals("")) {
            viewHolder.devId.setText("null");
        } else {
            viewHolder.devId.setText(data.get(position).getEntry().getCloudId());
        }
        viewHolder.devIp.setText(data.get(position).getEntry().getLanCfg().getIPAddress());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get(position).getChecked()) {
                    viewHolder.checkBtn.setBackgroundResource(R.drawable.lan_dev_checkbox);
                    data.get(position).setChecked(false);
                } else {
                    if (data.get(position).getEntry().getCloudId().equals("")) {
                        String ipAddress = "";
                        LibImpl.getInstance().getFuncLib().GetOneIPAddress(ipAddress);
                        Log.e("DDD", "null -> ipAddress :::" + ipAddress);
                        return;
                    }
                    viewHolder.checkBtn.setBackgroundResource(R.drawable.lan_dev_checkbox_select);
                    data.get(position).setChecked(true);
                }
            }
        });

        return convertView;
    }
}

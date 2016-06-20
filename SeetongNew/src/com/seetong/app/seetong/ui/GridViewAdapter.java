package com.seetong.app.seetong.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong.app.seetong.Config;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Device;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

public class GridViewAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<HashMap<String, Object>> mList;
    private List<Device> sqlList = Device.findAll();

    public GridViewAdapter(Context mContext,ArrayList<HashMap<String, Object>> mList) {
        super();
        this.mContext = mContext;
        this.mList = mList;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (this.mList == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.gridview_item, parent, false);
            return convertView;
        }

        HashMap<String, Object> hashMap = this.mList.get(position);
        PlayerDevice dev = (PlayerDevice) hashMap.get("device");
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = dev.isNVR() ? LayoutInflater.from(this.mContext).inflate(R.layout.gridview_item_2, parent, false) :
                    LayoutInflater.from(this.mContext).inflate(R.layout.gridview_item, parent, false);
            holder.deviceChosenButton = (ImageButton)convertView.findViewById(R.id.gridview_item_button);
            holder.deviceState = (TextView) convertView.findViewById(R.id.device_state);
            holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            holder.deviceId = (TextView) convertView.findViewById(R.id.device_id);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (dev.m_dev.getOnLine() != 0) {
            holder.deviceState.setText(" " + MainActivity2.m_this.getResources().getString(R.string.device_state_on) + " ");
            holder.deviceState.setBackgroundResource(R.drawable.tps_list_online);
        } else {
            holder.deviceState.setText(" " + MainActivity2.m_this.getResources().getString(R.string.device_state_off) + " ");
            holder.deviceState.setBackgroundResource(R.drawable.tps_list_offline);
        }

        Device device = new Device();
        for (int i = 0; i < sqlList.size(); i++) {
            if (dev.m_devId.equals(sqlList.get(i).getIp())) {
                device = sqlList.get(i);
                break;
            }
        }
        if (Config.m_show_alias && Config.m_show_devid) {
            holder.deviceName.setVisibility(Config.m_show_alias ? View.VISIBLE : View.GONE);
            if (dev.isNVR()) {
                holder.deviceName.setTextSize(10);
                holder.deviceId.setTextSize(10);
                if (device != null && device.getUser() != null) {
                    holder.deviceName.setText(" " + device.getUser() + " ");
                } else {
                    holder.deviceName.setText(" " + dev.getNvrId() + " ");
                }
            } else {
                holder.deviceName.setTextSize(12);
                holder.deviceId.setTextSize(12);
                holder.deviceName.setText(" " + LibImpl.getInstance().getDeviceAlias(dev.m_dev) + " ");
            }
            holder.deviceId.setVisibility(Config.m_show_devid ? View.VISIBLE : View.GONE);
            holder.deviceId.setText(" Id:" + dev.m_dev.getDevId() + " ");
        } else {
            holder.deviceName.setVisibility(Config.m_show_alias ? View.VISIBLE : View.GONE);
            if (dev.isNVR()) {
                holder.deviceName.setTextSize(10);
                holder.deviceId.setTextSize(10);
                if (device != null && device.getUser() != null) {
                    holder.deviceName.setText(" " + device.getUser() + " ");
                } else {
                    holder.deviceName.setText(" " + dev.getNvrId() + " ");
                }
            } else {
                holder.deviceName.setTextSize(12);
                holder.deviceId.setTextSize(12);
                holder.deviceName.setText(LibImpl.getInstance().getDeviceAlias(dev.m_dev) + " ");
            }
            holder.deviceId.setVisibility(Config.m_show_devid ? View.VISIBLE : View.GONE);
            holder.deviceId.setText(dev.m_dev.getDevId() + " ");
        }

        final String devId = dev.m_dev.getDevId();
        String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        try {
            bmp = BitmapFactory.decodeFile(fileName, options);
            if (null == bmp) {
                bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.camera);
            }
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.camera);
        }

        if (holder.deviceChosenButton != null && bmp != null) {
            holder.deviceChosenButton.setImageBitmap(bmp);
            holder.deviceChosenButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity2.m_this.playVideo(devId);
                }
            });
            holder.deviceChosenButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MainActivity2.m_this.deleteDevice(devId);
                    return true;
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        public ImageButton deviceChosenButton;
        public TextView deviceState;
        public TextView deviceName;
        public TextView deviceId;
    }
}


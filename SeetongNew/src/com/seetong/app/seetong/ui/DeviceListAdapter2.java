package com.seetong.app.seetong.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.seetong.app.seetong.Config;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Device;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

import java.util.List;
import java.util.Map;

public class DeviceListAdapter2 extends BaseAdapter {
    public static String TAG = DeviceListAdapter2.class.getName();
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> data;
    private List<Device> sqlList = Device.findAll();

    private class ViewHolder {
        public ImageButton deviceChooseButton;
        public ImageButton deviceReplayButton;
        public TextView deviceState;
        public TextView deviceName;
        public TextView deviceId;
    }

    public DeviceListAdapter2(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(this.context);
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
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == view) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.device_list_item2, parent, false);
            viewHolder.deviceChooseButton = (ImageButton) view.findViewById(R.id.device_check);
            viewHolder.deviceReplayButton = (ImageButton) view.findViewById(R.id.device_replay);
            viewHolder.deviceState = (TextView) view.findViewById(R.id.device_state);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceId = (TextView) view.findViewById(R.id.device_id);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Map<String, Object> map = data.get(position);

        final PlayerDevice playerDevice = (PlayerDevice) map.get("device");
        if (null == playerDevice) {
            return view;
        }

        if (playerDevice.m_dev.getOnLine() != 0) {
            viewHolder.deviceState.setText(" " + MainActivity2.m_this.getResources().getString(R.string.device_state_on) + " ");
            viewHolder.deviceState.setBackgroundResource(R.drawable.tps_list_online);
        } else {
            viewHolder.deviceState.setText(" " + MainActivity2.m_this.getResources().getString(R.string.device_state_off) + " ");
            viewHolder.deviceState.setBackgroundResource(R.drawable.tps_list_offline);
        }

        Device device = new Device();
        for (int i = 0; i < sqlList.size(); i++) {
            if (playerDevice.m_devId.equals(sqlList.get(i).getIp())) {
                device = sqlList.get(i);
                break;
            }
        }
        if (Config.m_show_alias && Config.m_show_devid) {
            viewHolder.deviceName.setVisibility(Config.m_show_alias ? View.VISIBLE : View.GONE);
            if (playerDevice.isNVR()) {
                if (device != null && device.getUser() != null) {
                    viewHolder.deviceName.setText(" " + device.getUser() + " ");
                } else {
                    viewHolder.deviceName.setText(" " + playerDevice.m_dev.getDevGroupName() + " ");
                }
            } else {
                viewHolder.deviceName.setText(" " + LibImpl.getInstance().getDeviceAlias(playerDevice.m_dev) + " ");
            }
            viewHolder.deviceId.setVisibility(Config.m_show_devid ? View.VISIBLE : View.GONE);
            viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + " ");
        } else {
            viewHolder.deviceName.setVisibility(Config.m_show_alias ? View.VISIBLE : View.GONE);
            if (playerDevice.isNVR()) {
                if (device != null && device.getUser() != null) {
                    viewHolder.deviceName.setText(" " + device.getUser() + " ");
                } else {
                    viewHolder.deviceName.setText(playerDevice.m_dev.getDevGroupName() + " ");
                }
            } else {
                viewHolder.deviceName.setText(LibImpl.getInstance().getDeviceAlias(playerDevice.m_dev) + " ");
            }
            viewHolder.deviceId.setVisibility(Config.m_show_devid ? View.VISIBLE : View.GONE);
            viewHolder.deviceId.setText(playerDevice.m_dev.getDevId() + " ");
        }

        final String devId = playerDevice.m_dev.getDevId();
        String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        try {
            bmp = BitmapFactory.decodeFile(fileName, options);
            if (null == bmp) {
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
            }
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
        }

        if (null != bmp) {
            viewHolder.deviceChooseButton.setImageBitmap(bmp);
        }

        viewHolder.deviceChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity2.m_this.playVideo(devId);
            }
        });

        viewHolder.deviceChooseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                MainActivity2.m_this.deleteDevice(devId);
                return true;
            }
        });

        return view;
    }

    public void updateDeviceAlias(PlayerDevice dev) {
        if (null == dev) return;
        sqlList = Device.findAll();
        notifyDataSetChanged();
    }

    public void updateDeviceList() {
        notifyDataSetChanged();
    }
}
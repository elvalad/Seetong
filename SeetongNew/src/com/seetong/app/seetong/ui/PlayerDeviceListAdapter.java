package com.seetong.app.seetong.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Device;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/4.
 */
public class PlayerDeviceListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> data;
    //private List<Device> sqlList = Device.findAll();

    private class ViewHolder {
        public ImageView deviceItem;
        public TextView deviceId;
    }

    PlayerDeviceListAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.player_device_list_item, viewGroup, false);
            viewHolder.deviceItem = (ImageView) view.findViewById(R.id.player_device_item);
            viewHolder.deviceId = (TextView) view.findViewById(R.id.player_device_item_id);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Map<String, Object> map = data.get(i);

        final PlayerDevice playerDevice = (PlayerDevice) map.get("device");
        if (null == playerDevice) {
            return view;
        }

        Device device = new Device();
        for (int j = 0; j < Global.m_sqlList.size(); j++) {
            if (playerDevice.m_devId.equals(Global.m_sqlList.get(j).getIp())) {
                device = Global.m_sqlList.get(j);
            }
        }

        if (playerDevice.m_dev.getOnLine() != 0) {
            if (!playerDevice.isNVR()) {
                viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + "  " +
                        "Name:" + LibImpl.getInstance().getDeviceAlias(playerDevice.m_dev) + "  " +
                        PlayerActivity.m_this.getResources().getString(R.string.device_state_on));
            } else {
                if (device != null && device.getUser() != null) {
                    viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + "  " +
                            "Name:" + device.getUser() + "  " +
                            PlayerActivity.m_this.getResources().getString(R.string.device_state_on));
                } else {
                    viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + "  " +
                            "Name:" + playerDevice.m_dev.getDevGroupName() + "  " +
                            PlayerActivity.m_this.getResources().getString(R.string.device_state_on));
                }
            }
        } else {
            if (!playerDevice.isNVR()) {
                viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + "  " +
                        "Name:" + LibImpl.getInstance().getDeviceAlias(playerDevice.m_dev) + "  " +
                        PlayerActivity.m_this.getResources().getString(R.string.device_state_off));
            } else {
                if (device != null && device.getUser() != null) {
                    viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + "  " +
                            "Name:" + device.getUser() + "  " +
                            PlayerActivity.m_this.getResources().getString(R.string.device_state_off));
                } else {
                    viewHolder.deviceId.setText(" Id:" + playerDevice.m_dev.getDevId() + "  " +
                            "Name:" + playerDevice.m_dev.getDevGroupName() + "  " +
                            PlayerActivity.m_this.getResources().getString(R.string.device_state_off));
                }
            }
        }

        final String devId = playerDevice.m_dev.getDevId();
        String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        try {
            bmp = BitmapFactory.decodeFile(fileName, options);
            if (null == bmp) {
                options.inSampleSize = 1;
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera_small, options);
            }
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            options.inSampleSize = 1;
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera_small, options);
        }

        if (null != bmp) {
            viewHolder.deviceItem.setImageBitmap(bmp);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerActivity.m_this.startChoosenPlay(playerDevice);
            }
        });

        return view;
    }
}

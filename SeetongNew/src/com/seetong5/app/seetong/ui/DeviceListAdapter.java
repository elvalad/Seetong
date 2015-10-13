package com.seetong5.app.seetong.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.NetworkUtils;
import com.seetong5.app.seetong.sdk.impl.MonitorCore;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;

/**
 * Created by Administrator on 2014-07-02.
 */
public class DeviceListAdapter extends BaseExpandableListAdapter {
    Context m_ctx;
    private LayoutInflater m_inflater;

    public DeviceListAdapter(Context ctx) {
        m_ctx = ctx;
        m_inflater = LayoutInflater.from(ctx);
    }

    private class GroupViewHolder {
        public TextView tvGroupName;
        public ImageView imgGroupIco;
    }

    private class ChildViewHolder {
        public TextView tvDeviceName;
        public TextView tvDeviceNo;
        public ImageView imgDelete;
        public ImageView imgCheck;
        public ImageView imgState;
    }

    @Override
    public int getGroupCount() {
        return MonitorCore.instance().m_cat_dev_map.size() ;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (MonitorCore.instance().m_cat_dev_map.isEmpty()) return 0;
        String key = (String) MonitorCore.instance().m_cat_dev_map.keySet().toArray()[groupPosition];
        int size = MonitorCore.instance().m_cat_dev_map.get(key).size();
        return size;
    }

    @Override
    public Object getGroup(int groupPosition) {
        String key = (String) MonitorCore.instance().m_cat_dev_map.keySet().toArray()[groupPosition];
        return key;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String key = (String) MonitorCore.instance().m_cat_dev_map.keySet().toArray()[groupPosition];
        return MonitorCore.instance().m_cat_dev_map.get(key).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return getGroupCount() > 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View v, ViewGroup parent) {
        GroupViewHolder viewHolder;
        if (null == v) {
            v = m_inflater.inflate(R.layout.device_list_group_item, parent, false);
            viewHolder = new GroupViewHolder();
            viewHolder.tvGroupName = (TextView) v.findViewById(R.id.tvGroupName);
            viewHolder.imgGroupIco = (ImageView) v.findViewById(R.id.imgGroupIco);
            v.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) v.getTag();
        }

        String key = (String) getGroup(groupPosition);
        viewHolder.tvGroupName.setText(key);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent) {
        ChildViewHolder viewHolder = null;
        if (null == v) {
            v = m_inflater.inflate(R.layout.device_list_item, parent, false);
            viewHolder = new ChildViewHolder();
            viewHolder.tvDeviceName = (TextView) v.findViewById(R.id.tvDeviceName);
            viewHolder.tvDeviceNo = (TextView) v.findViewById(R.id.tvDeviceNo);
            viewHolder.imgDelete = (ImageView) v.findViewById(R.id.imgDelete);
            viewHolder.imgCheck = (ImageView) v.findViewById(R.id.imgCheck);
            viewHolder.imgState = (ImageView) v.findViewById(R.id.imgState);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) v.getTag();
        }

        final PlayerDevice dev = (PlayerDevice) getChild(groupPosition, childPosition);
        if (null == dev) return v;

        String sn = dev.m_entry.getIpc_sn();
        String devName = dev.m_entry.getLanCfg().getIPAddress();
        viewHolder.tvDeviceName.setText(devName);

        String fileName = Global.getSnapshotDir() + "/" + sn + ".jpg";
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(fileName);
            if (null == bmp) bmp = BitmapFactory.decodeResource(m_ctx.getResources(), R.drawable.camera);
        } catch (OutOfMemoryError err) {
            // err.printStackTrace();
            bmp = BitmapFactory.decodeResource(m_ctx.getResources(), R.drawable.camera);
        }

        if (null != bmp) viewHolder.imgCheck.setImageBitmap(bmp);
        viewHolder.imgCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDeviceToLive(m_ctx, dev);
            }
        });

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDeviceToLive(m_ctx, dev);
            }
        });

        viewHolder.imgState.setImageResource(R.drawable.online);
        viewHolder.tvDeviceName.setTextColor(m_ctx.getResources().getColor(R.color.txt_device_name));
        viewHolder.tvDeviceNo.setTextColor(m_ctx.getResources().getColor(R.color.txt_device_no));

        int totle_Num = getChildrenCount(groupPosition);
        if (totle_Num == 1) {
            v.setBackgroundResource(R.drawable.default_selector);
            return v;
        }
        // 第一项
        else if (childPosition == 0) {
            v.setBackgroundResource(R.drawable.list_top_selector);
        }
        // 最后一项
        else if (childPosition == totle_Num - 1) {
            v.setBackgroundResource(R.drawable.list_bottom_selector);
        } else {
            v.setBackgroundResource(R.drawable.list_center_selector_2);
        }

        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private boolean addDeviceToLive(Context m_ctx, PlayerDevice dev) {
        //检测网络是否已打开
        if (NetworkUtils.getNetworkState(m_ctx) == NetworkUtils.NONE) {
            MainActivity.m_this.toast(R.string.dlg_network_check_tip);
            return false;
        }
        if (null == dev) return false;
        MainActivity.m_this.toVideo();
        MainActivity.m_this.getVideoFragment().addDeviceToView(dev);
        /*int AddLiveID = activity.getIntent().getIntExtra(VideoUI.ADD_LIVE_KEY, 0);
        if (AddLiveID == VideoUI.ADD_LIVE_ID) {
            activity.setResult(Activity.RESULT_OK, it);
        } else {
            activity.startActivity(it);
        }*/

        return true;
    }
}

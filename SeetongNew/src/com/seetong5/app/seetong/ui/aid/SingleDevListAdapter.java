package com.seetong5.app.seetong.ui.aid;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.MainActivity;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinglei.yin@192.168.88.9<br>
 *         2013-12-9 下午3:25:50<br>
 * @declaration 单级设备列表项适配器
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SingleDevListAdapter extends BaseAdapter implements OnClickListener {
    public class ViewHolder {
        public TextView tvDeviceName;
        public TextView tvDeviceNo;
        public ImageView imgDelete;
        public ImageView imgCheck;
        public Button btnConfig;
    }

    private List<PlayerDevice> mTitleAry = new ArrayList<PlayerDevice>();
    private LayoutInflater mInflater;
    private Resources m_res;

    public SingleDevListAdapter(List<PlayerDevice> datas) {
        m_res = MainActivity.m_this.getResources();
        mInflater = LayoutInflater.from(MainActivity.m_this.getDeviceFragment().getActivity());
        if ((datas != null) && (datas.size() > 0)) mTitleAry = datas;
    }

    @Override
    public int getCount() {
        int count = mTitleAry.size();
        return count;
    }

    @Override
    public Object getItem(int position) {
        Object obj = mTitleAry.get(position);
        return obj;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int pos, View v, ViewGroup parent) {
        ViewHolder viewHolder;
        if (v == null) {
            v = mInflater.inflate(R.layout.device_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvDeviceName = (TextView) v.findViewById(R.id.tvDeviceName);
            viewHolder.tvDeviceNo = (TextView) v.findViewById(R.id.tvDeviceNo);
            viewHolder.imgDelete = (ImageView) v.findViewById(R.id.imgDelete);
            viewHolder.imgCheck = (ImageView) v.findViewById(R.id.imgCheck);
            viewHolder.btnConfig = (Button) v.findViewById(R.id.btnConfig);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final PlayerDevice dev = mTitleAry.get(pos);
        if (dev != null) {
            String devId = dev.m_dev.getDevId();
            final ViewHolder _viewHolder = viewHolder;
            v.setOnClickListener(new OnClickListener() {//item单击
                @Override
                public void onClick(View v) {
                    addDeviceToLive(dev);
                }
            });
            viewHolder.tvDeviceName.setText(LibImpl.getInstance().getDeviceAlias(dev.m_dev));
            viewHolder.tvDeviceNo.setText(dev.m_dev.getDevGroupName() + "/" + dev.m_dev.getDevId());
            viewHolder.imgDelete.setOnClickListener(this);
            //viewHolder.imgCheck.setOnClickListener(this);
            viewHolder.imgDelete.setTag(pos + "");
            viewHolder.imgCheck.setTag(pos + "");

            String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
            Bitmap bmp = null;
            try {
                bmp = BitmapFactory.decodeFile(fileName);
                if (null == bmp) bmp = BitmapFactory.decodeResource(m_res, R.drawable.camera);
            } catch (OutOfMemoryError err) {
                // err.printStackTrace();
                bmp = BitmapFactory.decodeResource(m_res, R.drawable.camera);
            }

            if (null != bmp) viewHolder.imgCheck.setImageBitmap(bmp);

            boolean isOnline = dev.m_dev.getOnLine() != Device.OFFLINE;
            viewHolder.tvDeviceName.setTextColor(isOnline ? m_res.getColor(R.color.txt_device_name) : Color.GRAY);
            viewHolder.tvDeviceNo.setTextColor(isOnline ? m_res.getColor(R.color.txt_device_no) : Color.GRAY);
            if (dev.m_playing && isOnline) {
                viewHolder.tvDeviceName.setTextColor(Color.RED);
                viewHolder.tvDeviceNo.setTextColor(Color.RED);
            }

            viewHolder.btnConfig.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceConfig(dev);
                }
            });
        }

        int totle_Num = getCount();
        int current_Id = pos;
        v.setFocusable(false);
        // 只有一项
        if (totle_Num == 1) {
            v.setBackgroundResource(R.drawable.default_selector);
            return v;
        }
        // 第一项
        else if (current_Id == 0) {
            v.setBackgroundResource(R.drawable.list_top_selector);
        }
        // 最后一项
        else if (current_Id == totle_Num - 1) {
            v.setBackgroundResource(R.drawable.list_bottom_selector);
        } else {
            v.setBackgroundResource(R.drawable.list_center_selector);
        }
        return v;
    }

    private void onDeviceConfig(PlayerDevice dev) {
        MainActivity.m_this.getDeviceFragment().onDeviceConfig(dev.m_dev);
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        PlayerDevice dev = mTitleAry.get(pos);
        addDeviceToLive(dev);
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgDelete: {
                final int pos = Integer.parseInt((String) v.getTag());
                MyTipDialog.popDialog(MainActivity.m_this, R.string.dlg_delete_device_sure_tip, R.string.sure, R.string.cancel, new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        mTitleAry.remove(pos);
                        notifyDataSetChanged();
                    }
                });
            }
            break;

            case R.id.ivWifiInfo: {
                final int pos = Integer.parseInt((String) v.getTag());
                PlayerDevice dev = mTitleAry.get(pos);
                addDeviceToLive(dev);
            }
            break;

            default:
                break;
        }
    }

    public boolean addDeviceToLive(PlayerDevice dev) {
        if (null == dev) return false;
        MainActivity.m_this.toVideo();
        MainActivity.m_this.getVideoFragment().addDeviceToLive(dev);
        return true;
    }
}
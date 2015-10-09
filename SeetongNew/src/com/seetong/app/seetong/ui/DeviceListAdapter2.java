package com.seetong.app.seetong.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

import java.util.List;
import java.util.Map;

/**
 * DeviceListAdapter ���Զ�����豸�б�� Adapter����Ҫ�������� ListView. ����Ҫ�� DeviceListFragment
 * ��ȡ��ص����ݣ�Ȼ���� getView ������ʹ����Щ��Ϣ���޸���ص��豸�б��ֺ���Ӧ��صĲ���.
 *
 * Created by gmk on 2015/9/13.
 */
public class DeviceListAdapter2 extends BaseAdapter {
    public static String TAG = DeviceListAdapter2.class.getName();
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> data;

    private class ViewHolder {
        public ImageButton deviceCheckButton;
        public ImageButton deviceReplayButton;
        public TextView deviceState;
        public TextView deviceName;
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
            viewHolder.deviceCheckButton = (ImageButton) view.findViewById(R.id.device_check);
            viewHolder.deviceReplayButton = (ImageButton) view.findViewById(R.id.device_replay);
            viewHolder.deviceState = (TextView) view.findViewById(R.id.device_state);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            /* ע�������� inflate ֮��Ҫ��view������ص� Tag��������һ�λ�ȡ���� viewHolder����Ч�� */
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // ��ȡList�����е�map����
        Map<String, Object> map = data.get(position);

        final PlayerDevice dev = (PlayerDevice) map.get("device");//MonitorCore.instance().getPlayerDevice(position);
        if (null == dev) {
            return view;
        }

        final String devId = dev.m_dev.getDevId();
        String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(fileName);
            if (null == bmp) bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
        }

        if (null != bmp) {
            viewHolder.deviceCheckButton.setImageBitmap(bmp);
        }

        if (dev.m_dev.getOnLine() != 0) {
            viewHolder.deviceState.setText(R.string.device_state_on);
        } else {
            viewHolder.deviceState.setText(R.string.device_state_off);
        }

        viewHolder.deviceName.setText(dev.getDeviceName());

        // TODO:���ݲ����б����ز�����������ӿؼ�����������ÿ���豸�ı���ͼƬ,����Button����Ӧ�Լ�state�ı仯
        // http://blog.csdn.net/leoleohan/article/details/46553317
        // ��Щ�������ݾ�ͨ��DeiceListFragment��getData���������ȡ����ȡ��ʽ����:
        // 1.����ͼƬ��Ҫ�ӷ������˻�ȡÿ���豸����ƵͼƬ��
        // 2.checkButton��Ҫ��Ӧ������Ƶ�����¼���
        // 3.replayButton��Ҫ��Ӧ��Ƶ�ط��¼���
        // 4.state�豸״̬��Ҫ�ӷ������˻�ȡ��
        viewHolder.deviceCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "to player activity device is" + dev.toString());
                MainActivity2.m_this.playVideo(devId);
            }
        });

        viewHolder.deviceReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }
}
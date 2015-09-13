package com.seetong.app.seetong.gui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.seetong.app.seetong.R;

import java.util.List;
import java.util.Map;

/**
 * DeviceListAdapter 是自定义的设备列表的 Adapter，主要用于适配 ListView. 它需要从 DeviceListFragment
 * 获取相关的数据，然后再 getView 方法中使用这些信息来修改相关的设备列表布局和响应相关的操作.
 *
 * Created by gmk on 2015/9/13.
 */
public class DeviceListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> data;

    private class ViewHolder {
        public ImageButton deviceCheckButton;
        public ImageButton deviceReplayButton;
        public TextView deviceState;
    }

    public DeviceListAdapter(Context context, List<Map<String, Object>> data) {
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
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == view) {
            Log.e("", "+++++++++++++++++++++++++++++>>>>>>>>>>>>>view null");
            viewHolder = new ViewHolder();
            //view = View.inflate(context, R.layout.device_list_item, null);
            view = inflater.inflate(R.layout.device_list_item, parent, false);
            viewHolder.deviceCheckButton = (ImageButton) view.findViewById(R.id.device_check);
            viewHolder.deviceReplayButton = (ImageButton) view.findViewById(R.id.device_replay);
            viewHolder.deviceState = (TextView) view.findViewById(R.id.device_state);
            /* 注意这里在 inflate 之后要个view设置相关的 Tag，否则下一次获取到的 viewHolder是无效的 */
            view.setTag(viewHolder);
        } else {
            Log.e("", "-------------------------------->>>>>>>>>>>>>view null");
            viewHolder = (ViewHolder) view.getTag();
        }

        // 获取List集合中的map对象
        Map<String, Object> map = data.get(position);

        // TODO:根据播放列表的相关操作处理各个子控件，包括设置每个设备的背景图片,各个Button的响应以及state的变化
        // http://blog.csdn.net/leoleohan/article/details/46553317
        // 这些所需数据均通过DeiceListFragment的getData函数具体获取，获取方式如下:
        // 1.背景图片需要从服务器端获取每个设备的视频图片；
        // 2.checkButton需要响应启动视频播放事件；
        // 3.replayButton需要响应视频回放事件；
        // 4.state设备状态需要从服务器端获取；
        viewHolder.deviceCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.m_this.playVideo();
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

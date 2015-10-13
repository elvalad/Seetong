package com.seetong5.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong5.app.seetong.R;

/**
 * Created by Administrator on 2014-05-12.
 */
public class MediaFragment extends BaseFragment {
    private ListView m_listView;
    private ListViewAdapter m_adapter;
    private int m_itemTextRes[] = {R.string.image_file, R.string.video_file};
    private int m_itemImgRes[] = {R.drawable.photo, R.drawable.video};
    private final Class<?>[] m_itemActivity = new Class<?>[] { ImageFile2.class, VideoFile.class };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.m_this.setMediaFragment(this);
        View v = inflater.inflate(R.layout.media, container);
        m_listView = (ListView) v.findViewById(R.id.lv_media);
        m_adapter = new ListViewAdapter(m_listView.getContext());
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(m_adapter);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {

        }

        return false;
    }

    class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        public ListViewAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
        }

        public class ViewHolder {
            public ImageView imgView;
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_itemTextRes.length;
        }

        @Override
        public Object getItem(int position) {
            return m_itemTextRes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.more_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imgView = (ImageView) v.findViewById(R.id.img);
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }
            viewHolder.tvCaption.setText(m_itemTextRes[position]);
            viewHolder.imgView.setImageResource(m_itemImgRes[position]);
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Intent it = new Intent(m_ctx, m_itemActivity[pos]);
            m_ctx.startActivity(it);
        }
    }
}

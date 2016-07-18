package com.seetong.app.seetong.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.News;

import java.util.List;

/**
 * Created by Administrator on 2016/7/18.
 */
public class NewsListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<News> data;

    private class ViewHolder {
        public TextView newsTitle;
    }

    public NewsListAdapter(Context context, List<News> data) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.news_list_item, parent, false);
            viewHolder.newsTitle = (TextView) convertView.findViewById(R.id.news_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.newsTitle.setText(data.get(position).newsTitle);

        return convertView;
    }
}

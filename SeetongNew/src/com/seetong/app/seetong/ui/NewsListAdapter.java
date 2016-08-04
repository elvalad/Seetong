package com.seetong.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
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
    private DisplayImageOptions options;

    private class ViewHolder {
        public ImageView newsImg;
        public TextView newsTitle;
        public TextView newsTip;
        public TextView newsTime;
        public TextView newsCount;
    }

    public NewsListAdapter(Context context, List<News> data) {
        this.context = context;
        this.data = data;
        this.inflater = LayoutInflater.from(context);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.camera)
                .showImageOnFail(R.drawable.camera)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.NONE)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .delayBeforeLoading(100)
                .build();
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
            convertView = inflater.inflate(R.layout.news_list_item, parent, false);
            viewHolder.newsImg = (ImageView) convertView.findViewById(R.id.news_img);
            viewHolder.newsTitle = (TextView) convertView.findViewById(R.id.news_title);
            viewHolder.newsTip = (TextView) convertView.findViewById(R.id.news_tip);
            viewHolder.newsTime = (TextView) convertView.findViewById(R.id.news_time);
            viewHolder.newsCount = (TextView) convertView.findViewById(R.id.news_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.newsTitle.setText(data.get(position).newsTitle);
        viewHolder.newsTip.setText(data.get(position).newsTip);
        viewHolder.newsTime.setText(data.get(position).newsTime);
        viewHolder.newsCount.setText(data.get(position).newsCount);
        ImageLoader.getInstance().displayImage(data.get(position).newsImgUrl, viewHolder.newsImg, options,
                new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                viewHolder.newsImg.setImageBitmap(loadedImage);
                notifyDataSetChanged();
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsWebActivity.class);
                intent.putExtra("news_url", data.get(position).getNewsGoUrl());
                intent.putExtra("news_title", data.get(position).getNewsTitle());
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}

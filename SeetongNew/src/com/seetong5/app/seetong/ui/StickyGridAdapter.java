package com.seetong5.app.seetong.ui;

/**
 * Created by Administrator on 2015/9/24.
 */
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.seetong.app.seetong.R;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

public class StickyGridAdapter extends BaseAdapter implements
        StickyGridHeadersSimpleAdapter {

    private List<MediaGridItem> list;
    private LayoutInflater mInflater;
    private GridView mGridView;
    private Point mPoint = new Point(0, 0);//用来封装ImageView的宽和高的对象

    public StickyGridAdapter(Context context, List<MediaGridItem> list,
                             GridView mGridView) {
        this.list = list;
        mInflater = LayoutInflater.from(context);
        this.mGridView = mGridView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.grid_item, parent, false);
            mViewHolder.mImageView = (MyImageView) convertView
                    .findViewById(R.id.grid_item);
            convertView.setTag(mViewHolder);

            //用来监听ImageView的宽和高
            mViewHolder.mImageView.setOnMeasureListener(new MyImageView.OnMeasureListener() {

                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint.set(width, height);
                }
            });

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String path = list.get(position).getPath();

        mViewHolder.mImageView.setTag(path);

        Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint,
            new NativeImageLoader.NativeImageCallBack() {

                @Override
                public void onImageLoader(Bitmap bitmap, String path) {
                    ImageView mImageView = (ImageView) mGridView
                            .findViewWithTag(path);
                    if (bitmap != null && mImageView != null) {
                        mImageView.setImageBitmap(bitmap);
                    }
                }
            });

        if (bitmap != null) {
            mViewHolder.mImageView.setImageBitmap(bitmap);
        } else {
            mViewHolder.mImageView.setImageResource(R.drawable.tab_media);
        }

        return convertView;
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder mHeaderHolder;
        if (convertView == null) {
            mHeaderHolder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            mHeaderHolder.mTextView = (TextView) convertView
                    .findViewById(R.id.header);
            convertView.setTag(mHeaderHolder);
        } else {
            mHeaderHolder = (HeaderViewHolder) convertView.getTag();
        }

        mHeaderHolder.mTextView.setText(list.get(position).getTime());

        return convertView;
    }

    public static class ViewHolder {
        public MyImageView mImageView;
    }

    public static class HeaderViewHolder {
        public TextView mTextView;
    }

    @Override
    public long getHeaderId(int position) {
        return list.get(position).getSection();
    }

}


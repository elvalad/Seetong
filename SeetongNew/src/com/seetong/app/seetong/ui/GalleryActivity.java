package com.seetong.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import com.seetong.app.seetong.R;

/**
 * Created by gmk on 2015/9/23.
 */
public class GalleryActivity extends BaseActivity {
    public static GalleryActivity m_this = null;
    public int position = 0;	// ��ǰ��ʾͼƬ��λ��

    @Override
    public void onCreate(Bundle savedInstanceState) {
        m_this = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery);

        MyGallery  galllery = (MyGallery) findViewById(R.id.mygallery);
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);	// ��ȡGridViewActivity������ͼƬλ��position
        ImageAdapter imgAdapter = new ImageAdapter(this);
        galllery.setAdapter(imgAdapter);		// ����ͼƬImageAdapter
        galllery.setSelection(position); 		// ���õ�ǰ��ʾͼƬ

        Animation an= AnimationUtils.loadAnimation(this, R.anim.anim_image_scale);		// Gallery����
        galllery.setAnimation(an);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private int mPos;

        public ImageAdapter(Context context) {
            mContext = context;
        }

        public void setOwnposition(int ownposition) {
            this.mPos = ownposition;
        }

        public int getOwnposition() {
            return mPos;
        }

        @Override
        public int getCount() {
            return PictureFragment.mGridList.size();
        }

        @Override
        public Object getItem(int position) {
            mPos=position;
            return position;
        }

        @Override
        public long getItemId(int position) {
            mPos=position;
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mPos=position;
            ImageView imageView = new ImageView(mContext);
            imageView.setBackgroundColor(0xFF000000);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new MyGallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT));
            Bitmap bmp = BitmapFactory.decodeFile(PictureFragment.mGridList.get(position).getPath());
            imageView.setImageBitmap(bmp);

            return imageView;
        }
    }
}

package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Tools;

import java.util.ArrayList;

/**
 * Created by Administrator on 2014-07-28.
 */
public class Wizard extends BaseActivity {

    private ViewPager m_pager;
    private ArrayList<View> m_views = new ArrayList<>();
    private int m_pos = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        initWidget();
    }

    protected void initWidget() {
        m_pager = (ViewPager) findViewById(R.id.pager);
        int flags = Tools.getLanguageTypes();
        switch(flags){
            case 0:
                initImageZh();
                break;
            case 1:
                initImageZh();
                break;
            case 2:
                initImageEn();
                break;
            default:
                initImageZh();
                break;
        }

        m_pager.setAdapter(new MyPagerAdapter(m_views));

        m_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                m_pos = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (4 == m_pos) {
                    m_pos = 0;
                    Intent it = new Intent(Wizard.this, LoginActivity.class);
                    startActivity(it);
                    finish();
                    return;
                }

                if (3 == m_pos) m_pos++;
            }
        });
    }

    private void initImageZh() {
        ImageView imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard1);
        m_views.add(imgView);

        imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard2);
        m_views.add(imgView);

        imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard3);
        m_views.add(imgView);

        imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard4);
        m_views.add(imgView);
    }

    private void initImageEn() {
        ImageView imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard1_en);
        m_views.add(imgView);

        imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard2_en);
        m_views.add(imgView);

        imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard3_en);
        m_views.add(imgView);

        imgView = new ImageView(this);
        imgView.setBackgroundResource(R.drawable.wizard4_en);
        m_views.add(imgView);
    }

    public class MyPagerAdapter extends PagerAdapter {

        private ArrayList<View> views;

        public MyPagerAdapter(ArrayList<View> views) {
            this.views =views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
            container.removeView(views.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }
    }
}
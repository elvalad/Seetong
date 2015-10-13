package com.seetong5.app.seetong.ui.ext;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.seetong5.app.seetong.R;

import java.util.HashMap;
import java.util.Map;

public class GuideTools {
    private Activity m_act;
    private ViewPager m_pager;
    private MyPagerAdapter m_adapter;
    private int m_imgAry[];

    public interface IGuideFinish {
        public void onFinish();
    }

    public GuideTools(Activity act) {
        m_act = act;
    }

    public boolean isFirstRun(String keyName) {
        SharedPreferences preferences = m_act.getSharedPreferences("app_guide_help", Context.MODE_PRIVATE);
        final String key = m_act.getClass().getName() + "_" + keyName;
        return preferences.getBoolean(key, false);
    }

    public void setGuideImage(int viewId, int imageId[], String keyName, final IGuideFinish finishHandler) {
        m_imgAry = imageId;
        @SuppressWarnings("static-access")
        SharedPreferences preferences = m_act.getSharedPreferences("app_guide_help", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        final String key = m_act.getClass().getName() + "_" + keyName;
        if (!preferences.contains(key)) {
            editor.putBoolean(key, true);
            editor.commit();
        }

        if (!preferences.getBoolean(key, true)) {
            if (null != finishHandler) finishHandler.onFinish();
            return;
        }

        View view = m_act.getWindow().getDecorView().findViewById(viewId);
        ViewParent viewParent = view.getParent();
        if (viewParent instanceof FrameLayout) {
            final FrameLayout frameLayout = (FrameLayout) viewParent;

            LayoutInflater in = LayoutInflater.from(m_act.getApplicationContext());
            final View guideView = in.inflate(R.layout.guide, null);
            Button btn = (Button) guideView.findViewById(R.id.btn_i_know);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    frameLayout.removeView(guideView);
                    editor.putBoolean(key, false);
                    editor.commit();
                    if (null != finishHandler) finishHandler.onFinish();
                }
            });

            m_pager = (ViewPager) guideView.findViewById(R.id.pager);
            m_adapter = new MyPagerAdapter();
            m_pager.setAdapter(m_adapter);
            m_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i2) {

                }

                @Override
                public void onPageSelected(int i) {

                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });

            //final ImageView guideImage = new ImageView(act.getApplication());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            guideView.setLayoutParams(params);
            //guideImage.setLayoutParams(params);
            //guideImage.setScaleType(ScaleType.FIT_XY);
            //guideImage.setImageResource(imageId);
            /*guideImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    frameLayout.removeView(guideImage);
                    editor.putBoolean(key, false);
                    editor.commit();
                }
            });*/

            frameLayout.addView(guideView);
        }
    }

    public class MyPagerAdapter extends PagerAdapter {

        private Map<Integer, View> m_views = new HashMap<>();

        @Override
        public int getCount() {
            return m_imgAry.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
            container.removeView(m_views.get(position));
            m_views.put(position, null);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (null != m_views.get(position)) return m_views.get(position);
            ImageView v = new ImageView(m_act);
            v.setImageResource(m_imgAry[position]);
            v.setScaleType(ImageView.ScaleType.FIT_XY);
            v.setTag(position);
            container.addView(v);
            m_views.put(position, v);
            return v;
        }
    }
}

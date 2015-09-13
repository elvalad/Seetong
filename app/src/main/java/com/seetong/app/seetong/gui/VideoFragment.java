package com.seetong.app.seetong.gui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seetong.app.seetong.R;

/**
 * VideoFragment 是用于显示录像列表的 Fragment， 它和 PictureFragment一样都嵌套在 MediaFragment 中.
 *
 * Created by gmk on 2015/9/13.
 */
public class VideoFragment extends Fragment {

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    public VideoFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video, container, false);
        return view;
    }
}

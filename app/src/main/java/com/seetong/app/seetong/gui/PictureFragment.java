package com.seetong.app.seetong.gui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seetong.app.seetong.R;

/**
 * PictureFragment 是用于显示图片的 Fragment，它嵌套与 MediaFragment 中.
 *
 * Created by gmk on 2015/9/13.
 */
public class PictureFragment extends Fragment {

    public static PictureFragment newInstance() {
        return new PictureFragment();
    }

    public PictureFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture, container, false);
        return view;
    }
}

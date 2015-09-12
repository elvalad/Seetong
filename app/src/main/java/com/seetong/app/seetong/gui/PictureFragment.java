package com.seetong.app.seetong.gui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seetong.app.seetong.R;

/**
 * Created by Administrator on 2015/9/13.
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

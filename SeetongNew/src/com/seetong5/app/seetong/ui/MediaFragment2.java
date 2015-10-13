package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.LinearLayout;
import com.seetong5.app.seetong.R;


/**
 * MediaFragment 主要用于显示多媒体文件列表的 Fragment，它也是包含在 MainActivity 的 TabHost 中，
 * 它自身又嵌套包含两个子的 Fragment, 分别为 PictureFragment 和 VideoFragment 用于显示图片文件和
 * 录像文件.当用户点击不同的 Button 时会通过 getChildFragmentManager 显示不同的 Fragment.
 * 此 Fragment 默认显示 PictureFragment.
 *
 * Created by gmk on 2015/9/11.
 */
public class MediaFragment2 extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.media2, container);
        final LinearLayout layout = (LinearLayout) fragmentView.findViewById(R.id.media_title_layout);
        final Button picturebutton = (Button) fragmentView.findViewById(R.id.media_picture);
        final Button videoButton = (Button) fragmentView.findViewById(R.id.media_video);
        picturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setBackgroundResource(R.drawable.tps_media_picture);
                picturebutton.setTextColor(getResources().getColor(R.color.white));
                videoButton.setTextColor(getResources().getColor(R.color.green));
                showPictureFragment();
            }
        });


        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setBackgroundResource(R.drawable.tps_media_video);
                picturebutton.setTextColor(getResources().getColor(R.color.green));
                videoButton.setTextColor(getResources().getColor(R.color.white));
                showVideoFragment();
            }
        });

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.media_fragment_container, PictureFragment.newInstance())
                    .commit();
        }
    }

    private void showPictureFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, PictureFragment.newInstance())
                .commit();
    }

    private void showVideoFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, VideoFragment2.newInstance())
                .commit();
    }
}

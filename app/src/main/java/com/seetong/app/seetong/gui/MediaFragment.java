package com.seetong.app.seetong.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.seetong.app.seetong.R;


/**
 * Created by Administrator on 2015/9/11.
 */
public class MediaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.media, container);
        Button picturebutton = (Button) fragmentView.findViewById(R.id.media_picture);
        picturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureFragment();
            }
        });

        Button videoButton = (Button) fragmentView.findViewById(R.id.media_video);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                .replace(R.id.media_fragment_container, VideoFragment.newInstance())
                .commit();
    }
}

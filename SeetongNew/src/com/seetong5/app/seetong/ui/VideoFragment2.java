package com.seetong5.app.seetong.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.GridView;
import com.seetong5.app.seetong.R;

import java.util.*;

/**
 * VideoFragment 是用于显示录像列表的 Fragment， 它和 PictureFragment一样都嵌套在 MediaFragment 中.
 *
 * Created by gmk on 2015/9/13.
 */
public class VideoFragment2 extends BaseFragment {
    private VideoScanner mScanner;
    private GridView mGridView;
    private List<MediaGridItem> mGridList = new ArrayList<>();
    private static int section = 1;
    private Map<String, Integer> sectionMap = new HashMap<>();
    private StickyGridAdapter adapter;
    private boolean choosenMode;

    public static VideoFragment2 newInstance() {
        return new VideoFragment2();
    }

    public VideoFragment2(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video2, container, false);
        choosenMode = false;
        mGridView = (GridView) view.findViewById(R.id.asset_grid);
        mGridList.clear();
        mScanner = new VideoScanner(this.getActivity());
        mScanner.scanVideo(new VideoScanner.ScanCompleteCallBack() {
            @Override
            public void scanComplete(List<MediaGridItem> videoItem) {
                for (int i = 0; i < videoItem.size(); i++) {
                    mGridList.add(videoItem.get(i));
                }

                Collections.sort(mGridList, new YMComparator());

                for (ListIterator<MediaGridItem> it = mGridList.listIterator(); it.hasNext(); ) {
                    MediaGridItem mGridItem = it.next();
                    String ym = mGridItem.getTime();
                    if (!sectionMap.containsKey(ym)) {
                        mGridItem.setSection(section);
                        sectionMap.put(ym, section);
                        section++;
                    } else {
                        mGridItem.setSection(sectionMap.get(ym));
                    }
                }

                adapter = new StickyGridAdapter(VideoFragment2.this.getActivity(), mGridList, mGridView);
                mGridView.setAdapter(adapter);
                mGridView.setOnItemClickListener(listener);
            }
        });

        return view;
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            if (choosenMode) {
                if (mGridList.get(position).getIsChoosed()) {
                    mGridList.get(position).setIsChoosed(false);
                } else {
                    mGridList.get(position).setIsChoosed(true);
                }
                adapter.notifyDataSetChanged();
            } else {
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("file://" + mGridList.get(position).getPath());
                    it.setDataAndType(uri, "video/mp4");
                    startActivity(it);
                } catch (ActivityNotFoundException e) {
                    MainActivity2.m_this.toast(R.string.not_open_file_use_third_party_app);
                }
            }
        }
    };

    public void setChoosenMode() {
        if (!this.choosenMode) {
            for (int i = 0; i < mGridList.size(); i++) {
                mGridList.get(i).setIsChoosed(false);
            }
            adapter.setChoosenMode(true);
            this.choosenMode = true;
        } else {
            adapter.setChoosenMode(false);
            this.choosenMode = false;
        }
        adapter.notifyDataSetChanged();
    }
}


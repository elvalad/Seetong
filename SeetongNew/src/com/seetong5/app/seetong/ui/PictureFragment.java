package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;

import java.util.*;

/**
 * PictureFragment ��������ʾͼƬ�� Fragment����Ƕ���� MediaFragment ��.
 *
 * Created by gmk on 2015/9/13.
 */
public class PictureFragment extends BaseFragment {

    private GridView mGridView;
    private ImageScanner mScanner;
    public static List<MediaGridItem> mGridList = new ArrayList<>();
    private static int section = 1;
    private Map<String, Integer> sectionMap = new HashMap<>();
    private StickyGridAdapter adapter;
    private boolean choosenMode;

    public static PictureFragment newInstance() {
        return new PictureFragment();
    }

    public PictureFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture, container, false);
        choosenMode = false;
        mGridView = (GridView) view.findViewById(R.id.asset_grid);
        mGridList.clear();
        mScanner = new ImageScanner(this.getActivity());
        mScanner.scanImages(new ImageScanner.ScanCompleteCallBack() {
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
                adapter = new StickyGridAdapter(PictureFragment.this.getActivity(), mGridList, mGridView);
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
                Intent intent = new Intent();
                intent.setClass(PictureFragment.this.getActivity(), GalleryActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        }
    };

    public void updateScreenshotList() {
        mGridList.clear();
        section = 1;
        sectionMap.clear();
        mScanner.scanImages(new ImageScanner.ScanCompleteCallBack() {
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
                mGridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void setChoosenMode() {
        if (!this.choosenMode) {
            adapter.setChoosenMode(true);
            this.choosenMode = true;
        } else {
            adapter.setChoosenMode(false);
            this.choosenMode = false;
        }
        adapter.notifyDataSetChanged();
    }
}
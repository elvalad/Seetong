package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.seetong.app.seetong.R;

import java.io.File;
import java.util.*;

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
                adapter.setVideoMode(false);
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

    public void setAllChoosed(boolean bAllChoosed) {
        for (int i = 0; i < mGridList.size(); i++) {
            mGridList.get(i).setIsChoosed(bAllChoosed);
        }
        adapter.notifyDataSetChanged();
    }

    public void deleteChoosenItem() {
        if (mGridList.size() < 1) {
            toast(R.string.media_no_file);
            return;
        }

        final List<File> choosenFileList = new ArrayList<>();
        List<MediaGridItem> choosenMediaItemList = new ArrayList<>();
        Iterator<MediaGridItem> iterator = mGridList.iterator();
        while (iterator.hasNext()) {
            MediaGridItem item = iterator.next();
            if (item.getIsChoosed()) {
                choosenFileList.add(new File(item.getPath()));
                choosenMediaItemList.add(item);
            }
        }
        mGridList.removeAll(choosenMediaItemList);

        if (choosenFileList.size() == 0) {
            toast(R.string.media_no_choosen_file);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < choosenFileList.size(); i++) {
                    choosenFileList.get(i).delete();
                }
                choosenFileList.clear();
            }
        }).start();

        adapter.notifyDataSetChanged();
    }

    public List<String> getChoosenFileList() {
        List<String> choosenFileList = new ArrayList<>();
        for (MediaGridItem item : mGridList) {
            if (item.getIsChoosed()) {
                choosenFileList.add(item.getPath());
            }
        }

        return choosenFileList;
    }

    public void setChoosenMode(boolean choosenMode) {
        for (int i = 0; i < mGridList.size(); i++) {
            mGridList.get(i).setIsChoosed(false);
        }
        this.choosenMode = choosenMode;
        adapter.setChoosenMode(choosenMode);
        adapter.notifyDataSetChanged();
    }
}
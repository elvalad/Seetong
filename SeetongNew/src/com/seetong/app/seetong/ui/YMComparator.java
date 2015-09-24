package com.seetong.app.seetong.ui;

/**
 * Created by Administrator on 2015/9/24.
 */
import java.util.Comparator;

public class YMComparator implements Comparator<MediaGridItem> {

    @Override
    public int compare(MediaGridItem o1, MediaGridItem o2) {
        return o2.getTime().compareTo(o1.getTime());
    }
}

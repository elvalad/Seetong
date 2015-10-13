package com.seetong5.app.seetong.ui;

/**
 * Created by gmk on 2015/9/24.
 */
import java.util.Comparator;

public class YMComparator implements Comparator<MediaGridItem> {

    @Override
    public int compare(MediaGridItem o1, MediaGridItem o2) {
        return o2.getTime().compareTo(o1.getTime());
    }
}

package com.seetong5.app.seetong.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;
import android.widget.Toast;
import com.seetong5.app.seetong.R;

/**
 * Created by gmk on 2015/9/23.
 */
public class MyGallery extends Gallery {
    boolean isFirst = false;
    boolean isLast = false;

    public MyGallery(Context context) {
        super(context);
    }

    public MyGallery(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
    }

    /** 是否向左滑动（true - 向左滑动； false - 向右滑动） */
    private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
        return e2.getX() > e1.getX();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        GalleryActivity.m_this.finish();
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        GalleryActivity.ImageAdapter ia = (GalleryActivity.ImageAdapter) this.getAdapter();
        int p = ia.getOwnposition();	// 获取当前图片的position
        int count = ia.getCount();		// 获取全部图片的总数count
        int kEvent;
        if (isScrollingLeft(e1, e2)) {
            if (p == 0 && isFirst) {
                Toast.makeText(this.getContext(), R.string.media_first_pic, Toast.LENGTH_SHORT).show();
            } else if (p == 0) {
                isFirst = true;
            } else {
                isLast = false;
            }

            kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
        } else {
            if (p == count - 1 && isLast) {
                Toast.makeText(this.getContext(), R.string.media_last_pic, Toast.LENGTH_SHORT).show();
            } else if (p == count - 1) {
                isLast = true;
            } else {
                isFirst = false;
            }

            kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
        }
        onKeyDown(kEvent, null);
        return true;
    }
}

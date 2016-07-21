package com.seetong.app.seetong.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.example.AsymmetricGridView.library.widget.AsymmetricGridView;
import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.internal.EmptyViewMethodAccessor;

/**
 * Created by Administrator on 2016/6/8.
 */
public class PullToRefreshAsymGridView extends PullToRefreshAdapterViewBase<AsymmetricGridView> {

    private AsymmetricGridView gridView;

    public PullToRefreshAsymGridView(Context context) {
        super(context);
    }

    public PullToRefreshAsymGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshAsymGridView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshAsymGridView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected AsymmetricGridView createRefreshableView(Context context, AttributeSet attrs) {
        final AsymmetricGridView agv;
        agv = new InternalGridView(context, attrs);
        gridView = agv;
        return agv;
    }

    public AsymmetricGridView getGridView() {
        return gridView;
    }

    class InternalGridView extends AsymmetricGridView implements EmptyViewMethodAccessor {

        public InternalGridView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshAsymGridView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }
    }
}

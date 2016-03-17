package com.example.AsymmetricGridView.library;

import android.os.Parcelable;
import android.widget.ListAdapter;

public interface AsymmetricGridViewAdapterContract {
    public void recalculateItemsPerRow();

    public Parcelable saveState();

    public void restoreState(final Parcelable state);
}

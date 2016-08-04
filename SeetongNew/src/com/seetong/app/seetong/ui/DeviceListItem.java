package com.seetong.app.seetong.ui;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.AsymmetricGridView.library.model.AsymmetricItem;

/**
 * Created by Administrator on 2016/3/15.
 */
public class DeviceListItem implements AsymmetricItem {
    private int columnSpan;
    private int rowSpan;
    private int position;

    public DeviceListItem() {
        this(1, 1, 0);
    }

    public DeviceListItem(final int columnSpan, final int rowSpan, int position) {
        this.columnSpan = columnSpan;
        this.rowSpan = rowSpan;
        this.position = position;
    }

    public DeviceListItem(final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int getColumnSpan() {
        return columnSpan;
    }

    @Override
    public int getRowSpan() {
        return rowSpan;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("%s: %sx%s", position, rowSpan, columnSpan);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(final Parcel in) {
        columnSpan = in.readInt();
        rowSpan = in.readInt();
        position = in.readInt();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(columnSpan);
        dest.writeInt(rowSpan);
        dest.writeInt(position);
    }

    /* Parcelable interface implementation */
    public static final Parcelable.Creator<DeviceListItem> CREATOR = new Parcelable.Creator<DeviceListItem>() {

        @Override
        public DeviceListItem createFromParcel(final Parcel in) {
            return new DeviceListItem(in);
        }

        @Override
        public DeviceListItem[] newArray(final int size) {
            return new DeviceListItem[size];
        }
    };
}

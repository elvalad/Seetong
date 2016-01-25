package com.seetong.app.seetong.model;

import android.os.Parcelable;
import android.os.Parcel;

public final class ArchiveRecord implements Parcelable, Comparable<ArchiveRecord> {
    public static final int STATUS_START = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_SUCCEED = 3;
    public static final int STATUS_FAILED = 4;

    public int mDownloadStatus = 0;

    private long mDuration = 0;
    private long mEndId = 0;
    private long mStartId = 0;
    private long mStartTime = 0;
    private int mColor = 0;
    private long mColorStartTime = 0;
    private long mColorDuration = 0;
    private long mDownloadSize = 0;
    private String mName;
    private String mModifyTime;
    private String mDir;
    private String mDevId;
    private String mSize;
    private String mLocalName;
    private String mRecType;

    public static final Creator CREATOR = new Creator() {

        public ArchiveRecord createFromParcel(Parcel in) {
            return new ArchiveRecord(in);
        }

        public ArchiveRecord[] newArray(int size) {
            return new ArchiveRecord[size];
        }
    };

    public ArchiveRecord() {

    }

    public ArchiveRecord(long id_start, long id_end, long t_start, long duration, String name, String modifyTime, String dir, String devId, String size) {
        mStartId = id_start;
        mEndId = id_end;
        mStartTime = t_start;
        mDuration = duration;
        mName = name;
        mModifyTime = modifyTime;
        mDir = dir;
        mDevId = devId;
        mSize = size;
    }

    public ArchiveRecord(ArchiveRecord record) {
        mStartId = record.mStartId;
        mEndId = record.mEndId;
        mStartTime = record.mStartTime;
        mDuration = record.mDuration;
        mName = record.mName;
        mModifyTime = record.mModifyTime;
        mDir = record.mDir;
        mDevId = record.mDevId;
        mSize = record.mSize;
        mLocalName = record.mLocalName;
        mRecType = record.mRecType;
        mColorStartTime = record.mColorStartTime;
        mColorDuration = record.mColorDuration;
        mColor = record.mColor;
    }

    public ArchiveRecord(Parcel in) {
        mStartId = in.readLong();
        mEndId = in.readLong();
        mStartTime = in.readLong();
        mDuration = in.readLong();
        mDownloadSize = in.readLong();
        mName = in.readString();
        mModifyTime = in.readString();
        mDir = in.readString();
        mDevId = in.readString();
        mSize = in.readString();
        mLocalName = in.readString();
        mRecType = in.readString();
        mColorStartTime = in.readLong();
        mColorDuration = in.readLong();
        mColor = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mStartId);
        dest.writeLong(mEndId);
        dest.writeLong(mStartTime);
        dest.writeLong(mDuration);
        dest.writeLong(mDownloadSize);
        dest.writeString(mName);
        dest.writeString(mModifyTime);
        dest.writeString(mDir);
        dest.writeString(mDevId);
        dest.writeString(mSize);
        dest.writeString(mLocalName);
        dest.writeString(mRecType);
        dest.writeLong(mColorStartTime);
        dest.writeLong(mColorDuration);
        dest.writeInt(mColor);
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public long getEndId() {
        return mEndId;
    }

    public void setEndId(long endId) {
        this.mEndId = endId;
    }

    public long getStartId() {
        return mStartId;
    }

    public void setStartId(long startId) {
        this.mStartId = startId;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    public long getEndTime() {
        return mStartTime + mDuration;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setLocalName(String name) {
        mLocalName = name;
    }

    public String getLocalName() {
        return mLocalName;
    }

    public void setRecType(String type) {
        mRecType = type;
    }

    public String getRecType() {
        return mRecType;
    }

    public long getDownloadSize() {
        return mDownloadSize;
    }

    public void setDownloadSize(long size) {
        mDownloadSize += size;
    }

    public String getModifyTime() {
        return mModifyTime;
    }

    public void setModifyTime(String modifyTime) {
        mModifyTime = modifyTime;
    }

    public String getDir() {
        return mDir;
    }

    public void setDir(String dir) {
        this.mDir = dir;
    }

    public String getDevId() {
        return mDevId;
    }

    public void setDevId(String devId) {
        this.mDevId = devId;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String size) {
        this.mSize = size;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public void setColorStartTime(long time) {
        mColorStartTime = time;
    }

    public long getColorStartTime() {
        return mColorStartTime;
    }

    public long getColorEndTime() {
        return mColorStartTime + mColorDuration;
    }

    public void setColorDuration(long duration) {
        mColorDuration = duration;
    }

    public long getColorDuration() {
        return mColorDuration;
    }

    public long idStart() {
        return mStartId;
    }

    public long idEnd() {
        return mEndId;
    }

    public long startsAt() {
        return mStartTime;
    }

    public long duration() {
        return mDuration;
    }

    public boolean includes(long t) {
        return ((t >= mStartTime) && (t < (mStartTime + mDuration)));
    }

    public int compareTo(ArchiveRecord another) {
        long t = another.startsAt();
        return ((Long)mStartTime).compareTo(t);
    }

    @Override
    public String toString() {
        String name = null == mName ? "" : mName.substring(0, mName.indexOf('.'));
        return "ArchiveRecord{" +
                "mName='" + name + '\'' + "}";
    }
}

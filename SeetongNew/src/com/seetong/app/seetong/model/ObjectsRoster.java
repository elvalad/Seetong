package com.seetong.app.seetong.model;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import android.os.Parcel;
import com.seetong.app.seetong.Global;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import ipc.android.sdk.com.AbstractDataSerialBase;

public class ObjectsRoster<T extends Parcelable & Comparable<T>> extends AbstractDataSerialBase implements Parcelable {
    protected List<T> mObjRoster;
    public static final Creator CREATOR = new Creator() {
        
        public ObjectsRoster createFromParcel(Parcel in) {
            return new ObjectsRoster(in);
        }
        
        public ObjectsRoster[] newArray(int size) {
            return new ObjectsRoster[size];
        }
    };
    
    public ObjectsRoster() {
        mObjRoster = new ArrayList<T>();
    }
    
    public ObjectsRoster(ObjectsRoster<T> p1) {
        Parcel localParcel = Parcel.obtain();
        p1.writeToParcel(localParcel, 0);
        localParcel.setDataPosition(0);
        mObjRoster = new ArrayList<T>();
        List localList = Arrays.asList(localParcel.readParcelableArray(Global.m_ctx.getClassLoader()));
        Iterator localIterator = localList.iterator();
        while (localIterator.hasNext())
        {
            Parcelable localParcelable = (Parcelable)localIterator.next();
            mObjRoster.add((T) localParcelable);
        }
        localParcel.recycle();
    }
    
    public ObjectsRoster(Parcel in) {
        mObjRoster = new ArrayList<T>();
        List localList = Arrays.asList(in.readParcelableArray(Global.m_ctx.getClassLoader()));
        Iterator localIterator = localList.iterator();
        while (localIterator.hasNext())
        {
            Parcelable localParcelable = (Parcelable)localIterator.next();
            mObjRoster.add((T) localParcelable);
        }
    }
    
    public int describeContents() {
        return 0;
    }
    
    public void writeToParcel(Parcel dest, int flags) {
        Parcelable[] arrayOfParcelable = new Parcelable[mObjRoster.size()];
        dest.writeParcelableArray(mObjRoster.toArray(arrayOfParcelable), flags);
    }
    
    public boolean objectAdd(Parcelable p1, boolean toTop) {
        if (null == p1) return false;
        if (toTop) {
            mObjRoster.add(0, (T) p1);
            return true;
        }

        return this.mObjRoster.add((T) p1);
    }
    
    public boolean objectAddList(ObjectsRoster<T> p1, boolean toTop) {
        if (null == p1) return false;
        if (toTop) return mObjRoster.addAll(0, p1.mObjRoster);
        return mObjRoster.addAll(p1.mObjRoster);
    }
    
    public boolean objectRemove(int pos) {
        mObjRoster.remove(pos);
        return true;
    }

    public boolean objectRemove(T obj) {
        mObjRoster.remove(obj);
        return true;
    }

    public List<T> getObjectList() {
        return mObjRoster;
    }

    public void setObjectList(List<T> lst) {
        mObjRoster = lst;
    }
    
    public T objectAt(int pos) {
        try
        {
            Parcelable localParcelable = (Parcelable)mObjRoster.get(pos);
            return (T) localParcelable;
        }
        catch (IndexOutOfBoundsException ignored) {}
        return null;
    }

    public T objectAt(long time) {
        Iterator itr = mObjRoster.iterator();
        while (itr.hasNext()) {
            ArchiveRecord record = (ArchiveRecord) itr.next();
            if (record.includes(time)) return (T) record;
        }

        return null;
    }
    
    public int objectCount() {
        return mObjRoster.size();
    }
    
    public void objectSort() {
        try
        {
            Collections.sort(this.mObjRoster);
        }
        catch (Exception ignored){}
    }
    
    public void objectClearAll() {
        try
        {
            this.mObjRoster.clear();
        }
        catch (Exception ignored) {}
    }
    
    public boolean isEmpty() {
        return mObjRoster.isEmpty();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public Object fromXML(byte[] bytes, String rootName) {
        Object obj = null;
        if(mXStream == null || bytes == null) return obj;
        String xml = new String(bytes, mDefaultCharset);
        xml = xml.replaceAll("__", "_");	//自己替换掉__
        mXStream.alias(rootName, ObjectsRoster.class);
        mXStream.registerConverter(new XmlConverter());
        obj = mXStream.fromXML(xml);
        return obj;
    }

    class XmlConverter implements Converter {

        @Override
        public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            ObjectsRoster<ArchiveRecord> obj = new ObjectsRoster<>();
            String nodeName = reader.getNodeName();
            if (!nodeName.equals("ObjectList")) return null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
                if (!nodeName.equals("Object")) {
                    reader.moveUp();
                    continue;
                }

                ArchiveRecord record = new ArchiveRecord();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    nodeName = reader.getNodeName();
                    if (nodeName.equals("DevId")) {
                        record.setDevId(reader.getValue());
                    } else if (nodeName.equals("Name")) {
                        String name = reader.getValue();
                        record.setName(name);
                        String ary[] = name.split("/");
                        if (ary.length <= 0) {
                            reader.moveUp();
                            continue;
                        }

                        String date = ary[1];
                        String file = ary[ary.length - 1];
                        ary = file.split("-");
                        if (ary.length <= 0) {
                            reader.moveUp();
                            continue;
                        }

                        String time = ary[0];
                        String str = ary[ary.length - 1];
                        ary = str.split("\\.");
                        if (ary.length <= 0) {
                            reader.moveUp();
                            continue;
                        }

                        try {
                            time = date + time;
                            SimpleDateFormat fmt = (SimpleDateFormat) DateFormat.getInstance();
                            fmt.applyPattern("yyyyMMddHHmmss");
                            Date dt = fmt.parse(time);
                            long start = dt.getTime();
                            record.setStartTime(start);
                            long duration = Long.parseLong(ary[0]);
                            record.setDuration(duration * 60000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (nodeName.equals("ModifyTime")) {
                        String str = reader.getValue();
                        record.setModifyTime(str);
                    } else if (nodeName.equals("Size")) {
                        record.setSize(reader.getValue());
                    } else if (nodeName.equals("Dir")) {
                        record.setDir(reader.getValue());
                    } else if (nodeName.equals("RecType")) {
                        record.setRecType(reader.getValue());
                    }

                    reader.moveUp();
                }

                obj.objectAdd(record, false);
                reader.moveUp();
            }

            return obj;
        }

        @Override
        public boolean canConvert(Class aClass) {
            return aClass.equals(ObjectsRoster.class);
        }
    }
}

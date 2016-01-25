package com.seetong.app.seetong.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.seetong.app.seetong.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-06-25.
 */
public class DeviceSetting {
    private String m_devId;
    private boolean m_enable_alarm;

    private boolean m_enable_push_msg;
    private boolean m_force_forward;

    public String getDevId() {
        return m_devId;
    }

    public void setDevId(String id) {
        this.m_devId = id;
    }

    public boolean getEnableAlarm() {
        return m_enable_alarm;
    }

    public void setEnableAlarm(boolean enable) {
        this.m_enable_alarm = enable;
    }

    public boolean is_enable_push_msg() {
        return m_enable_push_msg;
    }

    public void set_enable_push_msg(boolean enable) {
        this.m_enable_push_msg = enable;
    }

    public boolean is_force_forward() {
        return m_force_forward;
    }

    public void set_force_forward(boolean force) {
        m_force_forward = force;
    }

    private long insert() {
        ContentValues values = new ContentValues();
        values.put("c_devId", m_devId);
        values.put("c_enable_alarm", m_enable_alarm);
        values.put("c_enable_push_msg", m_enable_push_msg);
        values.put("c_force_forward", m_force_forward);
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        long result = db.insert(DBHelper.TABLE_NAME_DEVICE_SETTING, null, values);
        db.close();
        return result;
    }

    private long update() {
        ContentValues values = new ContentValues();
        values.put("c_devId", m_devId);
        values.put("c_enable_alarm", m_enable_alarm);
        values.put("c_enable_push_msg", m_enable_push_msg);
        values.put("c_force_forward", m_force_forward);
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        return db.update(DBHelper.TABLE_NAME_DEVICE_SETTING, values, "c_devId=?", new String[]{m_devId});
    }

    public boolean save() {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return false;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE_SETTING, new String[]{"c_devId"}, "c_devId=?", new String[]{m_devId}, null, null, null);
        if (c.getCount() > 0) {
            c.close();
            db = null;
            return update() > 0;
        } else {
            c.close();
            db = null;
            return insert() > 0;
        }
    }

    public static DeviceSetting findByDeviceId(String devId) {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return null;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE_SETTING, new String[]{"*"}, "c_devId=?", new String[]{devId}, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        DeviceSetting record = new DeviceSetting();
        record.m_devId = c.getString(0);
        record.m_enable_alarm = c.getInt(1) == 1;
        record.m_enable_push_msg = c.getInt(2) == 1;
        record.m_force_forward = c.getInt(3) == 1;
        c.close();
        return record;
    }

    public static List<DeviceSetting> findAll() {
        List<DeviceSetting> lst = new ArrayList<>();
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return lst;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE_SETTING, new String[]{"*"}, null, null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return lst;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
            DeviceSetting record = new DeviceSetting();
            record.m_devId = c.getString(0);
            record.m_enable_alarm = c.getInt(1) == 1;
            record.m_enable_push_msg = c.getInt(2) == 1;
            record.m_force_forward = c.getInt(3) == 1;
            lst.add(record);
            c.moveToNext();
        }

        c.close();
        return lst;
    }

    public static List<String> findDeviceIdsByEnablePushMsg() {
        List<String> lst = new ArrayList<>();
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return lst;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE_SETTING, new String[]{"c_devId"}, "c_enable_push_msg=?", new String[]{"1"}, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return lst;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
            lst.add(c.getString(0));
            c.moveToNext();
        }

        c.close();
        return lst;
    }
}

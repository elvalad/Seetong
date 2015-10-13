package com.seetong5.app.seetong.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.seetong5.app.seetong.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-06-25.
 */
public class Device {
    private long m_id;
    private String m_ip;
    private int m_ptzPort;
    private int m_videoPort;
    private String m_user;
    private String m_pwd;

    public long getId() {
        return m_id;
    }

    public void setId(long m_id) {
        this.m_id = m_id;
    }

    public String getIp() {
        return m_ip;
    }

    public void setIp(String m_ip) {
        this.m_ip = m_ip;
    }

    public int getPtzPort() {
        return m_ptzPort;
    }

    public void setPtzPort(int port) {
        this.m_ptzPort = port;
    }

    public int getVideoPort() {
        return m_videoPort;
    }

    public void setVideoPort(int port) {
        this.m_videoPort = port;
    }

    public String getUser() {
        return m_user;
    }

    public void setUser(String m_user) {
        this.m_user = m_user;
    }

    public String getPwd() {
        return m_pwd;
    }

    public void setPwd(String m_pwd) {
        this.m_pwd = m_pwd;
    }

    private long insert(ContentValues values) {
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        long result = db.insert(DBHelper.TABLE_NAME_DEVICE, null, values);
        db.close();
        return result;
    }

    private long update(ContentValues values) {
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        return db.update(DBHelper.TABLE_NAME_DEVICE, values, "c_ip=?", new String[]{m_ip});
    }

    public boolean save() {
        ContentValues values = new ContentValues();
        values.put("c_ip", m_ip);
        values.put("c_ptz_port", m_ptzPort);
        values.put("c_video_port", m_videoPort);
        values.put("c_user", m_user);
        values.put("c_pwd", m_pwd);
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return false;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE, new String[]{"c_id"}, "c_ip=?", new String[]{m_ip}, null, null, null);
        if (c.getCount() > 0) {
            c.close();
            db = null;
            return update(values) > 0;
        } else {
            c.close();
            db = null;
            return insert(values) > 0;
        }
    }

    public static Device findById(String id) {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return null;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE, new String[]{"*"}, "c_id=?", new String[]{id}, null, null, "c_id desc");
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        Device record = new Device();
        record.m_id = c.getInt(0);
        record.m_ip = c.getString(1);
        record.m_ptzPort = c.getInt(2);
        record.m_videoPort = c.getInt(3);
        record.m_user = c.getString(4);
        record.m_pwd = c.getString(5);
        c.close();
        return record;
    }

    public static List<Device> findAll() {
        List<Device> lst = new ArrayList<>();
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return lst;
        Cursor c = db.query(DBHelper.TABLE_NAME_DEVICE, new String[]{"*"}, null, null, null, null, "c_id desc");
        if (c.getCount() == 0) {
            c.close();
            return lst;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Device record = new Device();
            record.m_id = c.getInt(0);
            record.m_ip = c.getString(1);
            record.m_ptzPort = c.getInt(2);
            record.m_videoPort = c.getInt(3);
            record.m_user = c.getString(4);
            record.m_pwd = c.getString(5);
            c.moveToNext();
            lst.add(record);
        }

        c.close();
        return lst;
    }
}

package com.seetong5.app.seetong.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.seetong5.app.seetong.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2014-06-25.
 */
public class Settings {
    private String m_user = "";
    private String m_preview_devices = "";

    public String getUser() {
        return m_user;
    }

    public void setUser(String user) {
        this.m_user = user;
    }

    public List<String> getPreviewDevices() {
        return Arrays.asList(m_preview_devices.split(","));
    }

    public void setPreviewDevices(List<String> devices) {
        for (String id : devices) {
            if (!TextUtils.isEmpty(m_preview_devices)) m_preview_devices += ",";
            m_preview_devices += id;
        }
    }

    private long insert() {
        ContentValues values = new ContentValues();
        values.put("c_user", m_user);
        values.put("c_preview_devices", m_preview_devices);
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        long result = db.insert(DBHelper.TABLE_NAME_SETTINGS, null, values);
        db.close();
        return result;
    }

    private long update() {
        ContentValues values = new ContentValues();
        values.put("c_preview_devices", m_preview_devices);
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        return db.update(DBHelper.TABLE_NAME_SETTINGS, values, "c_user=?", new String[]{m_user});
    }

    public boolean save() {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return false;
        Cursor c = db.query(DBHelper.TABLE_NAME_SETTINGS, new String[]{"c_user"}, "c_user=?", new String[]{m_user}, null, null, null);
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

    public static Settings findByUser(String user) {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return null;
        Cursor c = db.query(DBHelper.TABLE_NAME_SETTINGS, new String[]{"*"}, "c_user=?", new String[]{user}, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        Settings record = new Settings();
        record.m_user = c.getString(0);
        record.m_preview_devices = c.getString(1);
        c.close();
        return record;
    }

    public static List<Settings> findAll() {
        List<Settings> lst = new ArrayList<>();
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return lst;
        Cursor c = db.query(DBHelper.TABLE_NAME_SETTINGS, new String[]{"*"}, null, null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return lst;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Settings record = new Settings();
            record.m_user = c.getString(0);
            record.m_preview_devices = c.getString(1);
            lst.add(record);
            c.moveToNext();
        }

        c.close();
        return lst;
    }
}

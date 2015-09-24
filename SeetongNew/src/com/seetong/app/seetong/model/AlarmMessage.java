package com.seetong.app.seetong.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.seetong.app.seetong.Global;
import ipc.android.sdk.com.TPS_AlarmInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-06-25.
 */
public class AlarmMessage {
    public long setAlarmMessage(TPS_AlarmInfo ta) {
        ContentValues values = new ContentValues();
        values.put("c_devId", new String(ta.getSzDevId()).trim());
        values.put("c_desc", "");
        values.put("c_time", ta.getnTimestamp());
        values.put("c_type", ta.getnType());
        values.put("c_raise", ta.getnIsRaise());
        values.put("c_level", ta.getnAlarmLevel());
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        long result = db.insert(DBHelper.TABLE_NAME_ALARM_MESSAGE, null, values);
        db.close();
        return result;
    }

    public List<TPS_AlarmInfo> getAlarmMessage(int limit, int offset) {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return null;
        List<TPS_AlarmInfo> list = new ArrayList<>();
        String slimit = offset + "," + limit;
        Cursor c = db.query(DBHelper.TABLE_NAME_ALARM_MESSAGE, new String[]{"c_id","c_devId","c_desc","c_time","c_type",
                "c_raise","c_level"}, null, null, null, null, "c_time desc", slimit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            TPS_AlarmInfo ta = new TPS_AlarmInfo();
            ta.setSzDevId(c.getString(1).getBytes());
            ta.setSzDesc(c.getString(2).getBytes());
            ta.setnTimestamp(c.getInt(3));
            ta.setnType(c.getInt(4));
            ta.setnIsRaise(c.getInt(5));
            ta.setnAlarmLevel(c.getInt(6));
            list.add(ta);
            c.moveToNext();
        }

        c.close();
        return list;
    }

    public List<TPS_AlarmInfo> getAlarmMessage(List<String> devIdAry, int limit, int offset) {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return null;
        List<TPS_AlarmInfo> list = new ArrayList<>();
        if (devIdAry.isEmpty()) return list;

        String devIds = "";
        for (String devId : devIdAry) {
            if (devIds.length() > 0) devIds += ",";
            devIds += "'" + devId + "'";
        }

        String slimit = offset + "," + limit;
        Cursor c = db.query(DBHelper.TABLE_NAME_ALARM_MESSAGE, new String[]{"c_id","c_devId","c_desc","c_time","c_type",
                "c_raise","c_level"}, "c_devId IN (" + devIds + ")", null, null, null, "c_time desc", slimit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            TPS_AlarmInfo ta = new TPS_AlarmInfo();
            ta.setSzDevId(c.getString(1).getBytes());
            ta.setSzDesc(c.getString(2).getBytes());
            ta.setnTimestamp(c.getInt(3));
            ta.setnType(c.getInt(4));
            ta.setnIsRaise(c.getInt(5));
            ta.setnAlarmLevel(c.getInt(6));
            list.add(ta);
            c.moveToNext();
        }

        c.close();
        return list;
    }
}

package com.seetong5.app.seetong.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.seetong5.app.seetong.Global;

/**
 * Created by Administrator on 2014-06-25.
 */
public class ShareRecord {
    private long m_id;
    private String m_md5;
    private String m_fileName;
    private String m_shareUrl;

    public long getId() {
        return m_id;
    }

    public void setId(long id) {
        this.m_id = id;
    }

    public String getMd5() {
        return m_md5;
    }

    public void setMd5(String m_md5) {
        this.m_md5 = m_md5;
    }

    public String getFileName() {
        return m_fileName;
    }

    public void setFileName(String fileName) {
        this.m_fileName = fileName;
    }

    public String getShareUrl() {
        return m_shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.m_shareUrl = shareUrl;
    }

    public long insert() {
        ContentValues values = new ContentValues();
        values.put("c_md5", m_md5);
        values.put("c_fileName", m_fileName);
        values.put("c_shareUrl", m_shareUrl);
        SQLiteDatabase db = Global.m_db.getWritableDatabase();
        if (null == db) return -1;
        long result = db.insert(DBHelper.TABLE_NAME_SHARE_RECORD, null, values);
        db.close();
        return result;
    }

    public ShareRecord findByMd5(String md5) {
        SQLiteDatabase db = Global.m_db.getReadableDatabase();
        if (null == db) return null;
        Cursor c = db.query(DBHelper.TABLE_NAME_SHARE_RECORD, new String[]{"c_id","c_md5","c_fileName","c_shareUrl"}, "c_md5=?", new String[]{md5}, null, null, "c_id desc");
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        ShareRecord record = new ShareRecord();
        record.m_id = c.getInt(0);
        record.m_md5 = c.getString(1);
        record.m_fileName = c.getString(2);
        record.m_shareUrl = c.getString(3);
        c.close();
        return record;
    }
}

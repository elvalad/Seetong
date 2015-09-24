package com.seetong.app.seetong.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.seetong.app.seetong.Global;

/**
 * Created by Administrator on 2014-05-13.
 */
public class DBHelper extends SQLiteOpenHelper {
    private final static String TAG = "DbHelper";
    private final static String DATABASE_NAME = "tsee_db";
    private final static int DATABASE_VERSION = 8;
    public final static String TABLE_NAME_ALARM_MESSAGE = "tb_alarm_message";
    public final static String TABLE_NAME_SHARE_RECORD = "tb_share_record";
    public final static String TABLE_NAME_DEVICE_SETTING = "tb_device_setting";
    private final static String CREATE_TABLE_ALARM_MESSAGE = "CREATE TABLE IF NOT EXISTS tb_alarm_message(c_id INTEGER PRIMARY KEY AUTOINCREMENT, c_devId TEXT, c_desc TEXT, c_time DATETIME, c_type INTEGER, c_raise INTEGER, c_level INTEGER)";
    private final static String CREATE_TABLE_SHARE_RECORD = "CREATE TABLE IF NOT EXISTS tb_share_record(c_id INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, c_md5 TEXT, c_fileName TEXT, c_shareUrl TEXT)";
    private final static String CREATE_TABLE_DEVICE_SETTING = "CREATE TABLE IF NOT EXISTS tb_device_setting(c_devId TEXT UNIQUE, c_enable_alarm INTEGER, c_enable_push_msg INTEGER DEFAULT 1, c_force_forward INTEGER DEFAULT 0)";

    public final static String TABLE_NAME_DEVICE = "tb_device";
    private final static String CREATE_TABLE_DEVICE = "CREATE TABLE IF NOT EXISTS tb_device(c_id INTEGER PRIMARY KEY AUTOINCREMENT, c_ip TEXT UNIQUE, c_ptz_port INTEGER, c_video_port INTEGER, c_user TEXT, c_pwd TEXT)";

    public final static String TABLE_NAME_CHAT_MESSAGE = "tb_chat_message";
    private final static String CREATE_TABLE_CHAT_MESSAGE = "CREATE TABLE IF NOT EXISTS tb_chat_message(c_id INTEGER PRIMARY KEY AUTOINCREMENT, c_msgId TEXT UNIQUE, c_type TEXT, c_from TEXT, c_to TEXT, c_msg TEXT, c_time DATETIME, c_real_msg TEXT)";

    public final static String TABLE_NAME_SETTINGS = "tb_settings";
    private final static String CREATE_TABLE_SETTINGS = "CREATE TABLE IF NOT EXISTS tb_settings(c_user TEXT UNIQUE, c_preview_devices TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "DBHelper:DATABASE_VERSION=" + DATABASE_VERSION);
        SQLiteDatabase db = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DEVICE);
        db.execSQL(CREATE_TABLE_ALARM_MESSAGE);
        db.execSQL(CREATE_TABLE_SHARE_RECORD);
        db.execSQL(CREATE_TABLE_DEVICE_SETTING);
        db.execSQL(CREATE_TABLE_CHAT_MESSAGE);
        db.execSQL(CREATE_TABLE_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade:oldVersion=" + oldVersion + ",newVersion=" + newVersion);
        try {
            db.execSQL(CREATE_TABLE_SHARE_RECORD);
            db.execSQL(CREATE_TABLE_DEVICE_SETTING);
            db.execSQL(CREATE_TABLE_CHAT_MESSAGE);
            db.execSQL(CREATE_TABLE_SETTINGS);
            if (oldVersion < 5) {
                db.execSQL("ALTER TABLE tb_chat_message ADD c_real_msg TEXT;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (oldVersion < 6) {
                db.execSQL("ALTER TABLE tb_device_setting ADD c_enable_push_msg INTEGER DEFAULT 1;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (oldVersion < 7) {
                db.execSQL("ALTER TABLE tb_device_setting ADD c_force_forward INTEGER DEFAULT 0;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

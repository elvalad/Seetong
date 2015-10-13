package com.seetong5.app.seetong.model;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.seetong5.app.seetong.Global;
import ipc.android.sdk.com.AbstractDataSerialBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2014-06-25.
 */
public class FriendMessageList extends AbstractDataSerialBase implements Cloneable {
    public String m_ret;
    public String m_error;
    public String m_allCount;
    public String m_page;
    public String m_pageSize;

    private List<Message> m_lstMessage = new ArrayList<>();

    public static class Message {
        public String m_id;
        public String m_type;
        public String m_from;
        public String m_to;
        public String m_msg;
        public String m_real_msg;
        public String m_time;

        public boolean isReceiver() {
            return m_to.equals(Global.m_devInfo.getUserName());
        }

        public String getDate() {
            return m_time.split(" ")[0];
        }

        public String getTime() {
            return m_time.split(" ")[1];
        }

        private long insert(ContentValues values) {
            SQLiteDatabase db = Global.m_db.getWritableDatabase();
            if (null == db) return -1;
            long result = db.insert(DBHelper.TABLE_NAME_CHAT_MESSAGE, null, values);
            db.close();
            return result;
        }

        private long update(ContentValues values) {
            SQLiteDatabase db = Global.m_db.getWritableDatabase();
            if (null == db) return -1;
            return db.update(DBHelper.TABLE_NAME_CHAT_MESSAGE, values, "c_msgId=?", new String[]{m_id});
        }

        public boolean save() {
            ContentValues values = new ContentValues();
            values.put("c_msgId", m_id);
            values.put("c_type", m_type);
            values.put("c_from", m_from);
            values.put("c_to", m_to);
            values.put("c_msg", m_msg);
            values.put("c_time", m_time);
            values.put("c_real_msg", m_real_msg);
            SQLiteDatabase db = Global.m_db.getReadableDatabase();
            if (null == db) return false;
            Cursor c = db.query(DBHelper.TABLE_NAME_CHAT_MESSAGE, new String[]{"c_msgId"}, "c_msgId=?", new String[]{m_id}, null, null, null);
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

        public int delete() {
            if (null == m_id || "".equals(m_id)) return 0;
            return delete(m_id);
        }

        public static int delete(String id) {
            SQLiteDatabase db = Global.m_db.getReadableDatabase();
            if (null == db) return 0;
            return db.delete(DBHelper.TABLE_NAME_CHAT_MESSAGE, "c_msgId=?", new String[]{id});
        }

        public static Message findByMsgId(String id) {
            SQLiteDatabase db = Global.m_db.getReadableDatabase();
            if (null == db) return null;
            Cursor c = db.query(DBHelper.TABLE_NAME_CHAT_MESSAGE, new String[]{"*"}, "c_msgId=?", new String[]{id}, null, null, null);
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            c.moveToFirst();
            Message msg = new Message();
            msg.m_id = c.getString(1);
            msg.m_type = c.getString(2);
            msg.m_from = c.getString(3);
            msg.m_to = c.getString(4);
            msg.m_msg = c.getString(5);
            msg.m_time = c.getString(6);
            msg.m_real_msg = c.getString(7);
            c.close();
            return msg;
        }

        public static Message findByLastReceiver() {
            SQLiteDatabase db = Global.m_db.getReadableDatabase();
            if (null == db) return null;
            Cursor c = db.query(DBHelper.TABLE_NAME_CHAT_MESSAGE, new String[]{"*"}, "c_from!=?", new String[]{Global.m_devInfo.getUserName()}, null, null, "c_time DESC", "1");
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            c.moveToFirst();
            Message msg = new Message();
            msg.m_id = c.getString(1);
            msg.m_type = c.getString(2);
            msg.m_from = c.getString(3);
            msg.m_to = c.getString(4);
            msg.m_msg = c.getString(5);
            msg.m_time = c.getString(6);
            msg.m_real_msg = c.getString(7);
            c.close();
            return msg;
        }

        public static Message findByLastReceiver(String from) {
            SQLiteDatabase db = Global.m_db.getReadableDatabase();
            if (null == db) return null;
            Cursor c = db.query(DBHelper.TABLE_NAME_CHAT_MESSAGE, new String[]{"*"}, "c_from=?", new String[]{from}, null, null, "c_time DESC", "1");
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            c.moveToFirst();
            Message msg = new Message();
            msg.m_id = c.getString(1);
            msg.m_type = c.getString(2);
            msg.m_from = c.getString(3);
            msg.m_to = c.getString(4);
            msg.m_msg = c.getString(5);
            msg.m_time = c.getString(6);
            msg.m_real_msg = c.getString(7);
            c.close();
            return msg;
        }

        public static List<Message> findByFromTo(String from, String to) {
            List<Message> result = new ArrayList<>();
            SQLiteDatabase db = Global.m_db.getReadableDatabase();
            if (null == db) return result;
            Cursor c = db.query(DBHelper.TABLE_NAME_CHAT_MESSAGE, new String[]{"*"}, "c_from=? AND c_to=?", new String[]{from, to}, null, null, "c_msgId DESC");
            if (c.getCount() == 0) {
                c.close();
                return result;
            }
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Message msg = new Message();
                msg.m_id = c.getString(1);
                msg.m_type = c.getString(2);
                msg.m_from = c.getString(3);
                msg.m_to = c.getString(4);
                msg.m_msg = c.getString(5);
                msg.m_time = c.getString(6);
                msg.m_real_msg = c.getString(7);
                c.moveToNext();
                result.add(msg);
            }

            c.close();
            return result;
        }
    }

    public List<Message> getMessageList() {
        return m_lstMessage;
    }

    public void add(Message msg) {
        for (Message m : m_lstMessage) {
            if (m.m_id.equals(msg.m_id)) return;
        }
        m_lstMessage.add(msg);
    }

    public void addAll(Collection<Message> collection) {
        for (Message m : collection) {
            add(m);
        }
    }

    public Message get(int index) {
        return m_lstMessage.get(index);
    }

    public void sortChatMessageByTime() {
        Global.sortChatMessageByTime(m_lstMessage);
    }

    public void sortChatMessageByMsgId() {
        Global.sortChatMessageByMsgId(m_lstMessage);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public Object fromXML(byte[] bytes, String rootName) {
        Object obj = null;
        if(mXStream == null || bytes == null) return obj;
        String xml = new String(bytes, mDefaultCharset);
        xml = xml.replaceAll("__", "_");	//自己替换掉__
        mXStream.alias(rootName, FriendMessageList.class);
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
            FriendMessageList msgList = new FriendMessageList();
            String nodeName = reader.getNodeName();
            if (!nodeName.equals("xml")) return null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
                if (nodeName.equals("ret")) {
                    msgList.m_ret = reader.getValue();
                } else if (nodeName.equals("error")) {
                    msgList.m_error = reader.getValue();
                } else if (nodeName.equals("allcount")) {
                    msgList.m_allCount = reader.getValue();
                } else if (nodeName.equals("page")) {
                    msgList.m_page = reader.getValue();
                } else if (nodeName.equals("pagesize")) {
                    msgList.m_pageSize = reader.getValue();
                } else if (nodeName.equals("ls")) {
                    Message msg = new Message();
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        nodeName = reader.getNodeName();
                        if (nodeName.equals("id")) {
                            msg.m_id = reader.getValue();
                        } else if (nodeName.equals("tyid")) {
                            msg.m_type = reader.getValue();
                        } else if (nodeName.equals("fname")) {
                            msg.m_from = reader.getValue();
                        } else if (nodeName.equals("msg")) {
                            msg.m_msg = reader.getValue();
                        } else if (nodeName.equals("tm")) {
                            msg.m_time = reader.getValue();
                        }

                        reader.moveUp();
                    }

                    msgList.m_lstMessage.add(msg);
                }

                reader.moveUp();
            }

            return msgList;
        }

        @Override
        public boolean canConvert(Class aClass) {
            return aClass.equals(FriendMessageList.class);
        }
    }
}

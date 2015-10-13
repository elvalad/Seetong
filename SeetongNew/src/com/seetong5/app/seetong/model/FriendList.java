package com.seetong5.app.seetong.model;

import android.annotation.TargetApi;
import android.os.Build;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import ipc.android.sdk.com.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2014-06-25.
 */
public class FriendList extends AbstractDataSerialBase implements Cloneable {
    public String m_ret;
    public String m_allCount;
    public String m_page;
    public String m_pageSize;

    public Map<String, Friend> m_lstFriend = new HashMap<>();

    public final static int FRIEND = 0;
    public final static int REQUEST_PENDING = 1;
    public final static int RESPONSE_PENDING = 2;
    public final static int ACCEPTED = 3;
    public final static int REJECTED = 4;

    public static class Friend {
        public String m_id;
        public String m_name;
        public int m_status;
        public String m_sid;
        public String m_additionMsg;
        public int m_newMsgCount;
        public boolean m_inChat;
        public FriendMessageList m_msgList = new FriendMessageList();
        {
            m_status = -1;
            m_newMsgCount = 0;
            m_inChat = false;
        }
    }

    public Friend findById(String id) {
        if ("".equals(id) || m_lstFriend.isEmpty()) return null;
        for (String key : m_lstFriend.keySet()) {
            Friend friend = m_lstFriend.get(key);
            if (null == friend) continue;
            if (friend.m_id.equals(id)) return friend;
        }

        return null;
    }

    public Friend findByName(String name) {
        if ("".equals(name) || m_lstFriend.isEmpty()) return null;
        return m_lstFriend.get(name);
    }

    public int getNewMsgCount() {
        int count = 0;
        for (String key : m_lstFriend.keySet()) {
            Friend friend = m_lstFriend.get(key);
            if (null == friend) continue;
            count += friend.m_newMsgCount;
        }

        return count;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public Object fromXML(byte[] bytes, String rootName) {
        Object obj = null;
        if(mXStream == null || bytes == null) return obj;
        String xml = new String(bytes, mDefaultCharset);
        xml = xml.replaceAll("__", "_");	//自己替换掉__
        mXStream.alias(rootName, FriendList.class);
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
            FriendList friendList = new FriendList();
            String nodeName = reader.getNodeName();
            if (!nodeName.equals("xml")) return null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
                if (nodeName.equals("ret")) {
                    friendList.m_ret = reader.getValue();
                } else if (nodeName.equals("allcount")) {
                    friendList.m_allCount = reader.getValue();
                } else if (nodeName.equals("page")) {
                    friendList.m_page = reader.getValue();
                } else if (nodeName.equals("pagesize")) {
                    friendList.m_pageSize = reader.getValue();
                } else if (nodeName.equals("ls")) {
                    Friend friend = new Friend();
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        nodeName = reader.getNodeName();
                        if (nodeName.equals("fid")) {
                            friend.m_id = reader.getValue();
                        } else if (nodeName.equals("fname")) {
                            friend.m_name = reader.getValue();
                        }

                        reader.moveUp();
                    }

                    friend.m_status = FriendList.FRIEND;
                    friendList.m_lstFriend.put(friend.m_name, friend);
                }

                reader.moveUp();
            }

            return friendList;
        }

        @Override
        public boolean canConvert(Class aClass) {
            return aClass.equals(FriendList.class);
        }
    }
}

package com.seetong5.app.seetong.model;

import android.annotation.TargetApi;
import android.os.Build;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import ipc.android.sdk.com.AbstractDataSerialBase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2014-06-25.
 */
public class MessageList extends AbstractDataSerialBase implements Cloneable {
    public String m_ret;
    public String m_error;
    public String m_data_type;
    public String m_ac;
    public String m_uid;
    public String m_id;
    public String m_start_time;
    public String m_end_time;
    public String m_page;
    public String m_page_size;
    public String m_all_count;

    public Map<String, Message> m_lstMessage = new HashMap<>();

    public static class Message {
        public String m_alarm_id;
        public String m_alarm_type_id;
        public String m_alarm_info;
        public String m_alarm_time;
        public String m_alarm_recv_time;
        public String m_alarm_attachment;
        public String m_alarm_attachment_state;
        public String m_dev_id;
        public long m_alarm_time_long;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Message)) return false;
            Message m = (Message) o;
            return m_alarm_id.equals(m.m_alarm_id) || m_alarm_time_long == m.m_alarm_time_long;
        }
    }

    public Message findById(String id) {
        if ("".equals(id) || m_lstMessage.isEmpty()) return null;
        for (String key : m_lstMessage.keySet()) {
            Message message = m_lstMessage.get(key);
            if (null == message) continue;
            if (message.m_alarm_id.equals(id)) return message;
        }

        return null;
    }

    public static void sortMessageById(List<Message> list) {
        class MessageSortById implements Comparator<Message> {
            @Override
            public int compare(Message arg1, Message arg2) {
                int lhs = Integer.parseInt(arg1.m_alarm_id);
                int rhs = Integer.parseInt(arg2.m_alarm_id);
                return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
            }
        }

        Collections.sort(list, new MessageSortById());
    }

    public static void sortMessageByTimeDesc(List<Message> list) {
        class MessageSort implements Comparator<Message> {
            @Override
            public int compare(Message arg1, Message arg2) {
                long lhs = arg1.m_alarm_time_long;
                long rhs = arg2.m_alarm_time_long;
                return lhs > rhs ? -1 : (lhs == rhs ? 0 : 1);
            }
        }

        Collections.sort(list, new MessageSort());
    }

    public static List<Message> removeDuplicateMessage(List<Message> list) {
        List<Message> list2 = new ArrayList<>();
        for (Message m : list) {
            if (list2.contains(m)) continue;
            list2.add(m);
        }

        return list2;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public Object fromXML(byte[] bytes, String rootName) {
        Object obj = null;
        if(mXStream == null || bytes == null) return obj;
        String xml = new String(bytes, mDefaultCharset);
        xml = xml.replaceAll("__", "_");	//自己替换掉__
        mXStream.alias(rootName, MessageList.class);
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
            MessageList messageList = new MessageList();
            String nodeName = reader.getNodeName();
            if (!nodeName.equals("xml")) return null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
                if (nodeName.equals("ret")) {
                    messageList.m_ret = reader.getValue();
                } else if (nodeName.equals("error")) {
                    messageList.m_error = reader.getValue();
                } else if (nodeName.equals("datatype")) {
                    messageList.m_data_type = reader.getValue();
                } else if (nodeName.equals("ac")) {
                    messageList.m_ac = reader.getValue();
                } else if (nodeName.equals("uid")) {
                    messageList.m_uid = reader.getValue();
                } else if (nodeName.equals("id")) {
                    messageList.m_id = reader.getValue();
                } else if (nodeName.equals("starttime")) {
                    messageList.m_start_time = reader.getValue();
                } else if (nodeName.equals("endtime")) {
                    messageList.m_end_time = reader.getValue();
                } else if (nodeName.equals("page")) {
                    messageList.m_page = reader.getValue();
                } else if (nodeName.equals("pagesize")) {
                    messageList.m_page_size = reader.getValue();
                } else if (nodeName.equals("allcount")) {
                    messageList.m_all_count = reader.getValue();
                } else if (nodeName.equals("ls")) {
                    Message message = new Message();
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        nodeName = reader.getNodeName();
                        if (nodeName.equals("AlarmID")) {
                            message.m_alarm_id = reader.getValue();
                        } else if (nodeName.equals("AlarmTypeID")) {
                            message.m_alarm_type_id = reader.getValue();
                        } else if (nodeName.equals("AlarmInfo")) {
                            message.m_alarm_info = reader.getValue();
                        } else if (nodeName.equals("AlarmTime")) {
                            message.m_alarm_time = reader.getValue();
                            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                message.m_alarm_time_long = fmt.parse(message.m_alarm_time).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if (nodeName.equals("AlarmRecvTime")) {
                            message.m_alarm_recv_time = reader.getValue();
                        } else if (nodeName.equals("AlarmAttachment")) {
                            message.m_alarm_attachment = reader.getValue();
                        } else if (nodeName.equals("AlarmAttachmentState")) {
                            message.m_alarm_attachment_state = reader.getValue();
                        } else if (nodeName.equals("DevID")) {
                            message.m_dev_id = reader.getValue();
                        }

                        reader.moveUp();
                    }

                    messageList.m_lstMessage.put(message.m_alarm_id, message);
                }

                reader.moveUp();
            }

            return messageList;
        }

        @Override
        public boolean canConvert(Class aClass) {
            return aClass.equals(MessageList.class);
        }
    }
}

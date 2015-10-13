package com.seetong5.app.seetong.ui;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.model.FriendMessageList;

import java.util.List;

/**
 * Created by Administrator on 2014-07-02.
 */
public class ChatMessageViewAdapter extends BaseAdapter {

    public static interface IMsgViewType {
        int IMVT_COM_MSG = 0;
        int IMVT_TO_MSG = 1;
    }

    private static final String TAG = ChatMessageViewAdapter.class.getSimpleName();

    private List<FriendMessageList.Message> m_msgList;

    private ChatMessage m_act;

    private LayoutInflater mInflater;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    public ChatMessageViewAdapter(ChatMessage act, List<FriendMessageList.Message> list) {
        m_act = act;
        m_msgList = list;
        mInflater = LayoutInflater.from(act);
    }

    public int getCount() {
        return m_msgList.size();
    }

    public Object getItem(int position) {
        return m_msgList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        FriendMessageList.Message msg = m_msgList.get(position);

        if (msg.isReceiver()) {
            return IMsgViewType.IMVT_COM_MSG;
        } else {
            return IMsgViewType.IMVT_TO_MSG;
        }
    }

    public void notifyDataSetChanged(List<FriendMessageList.Message> lst) {
        m_msgList = lst;
        notifyDataSetChanged();
    }

    public int getViewTypeCount() {
        return 2;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        final FriendMessageList.Message msg = m_msgList.get(position);
        boolean isComMsg = msg.isReceiver();

        ViewHolder viewHolder = null;
        if (convertView == null) {
            if (isComMsg) {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
            } else {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
            }

            viewHolder = new ViewHolder();
            viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.isComMsg = isComMsg;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvSendTime.setText(msg.getDate());

        if (msg.m_msg.contains(".amr")) {
            viewHolder.tvContent.setText("");
            viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chatto_voice_playing, 0);
            viewHolder.tvTime.setText(msg.getTime());
        } else if (msg.m_type.equals("10001")) {
            viewHolder.tvContent.setText(msg.m_msg);
            viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            viewHolder.tvTime.setText("");
        } else {
            viewHolder.tvContent.setText(msg.m_msg);
            viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            viewHolder.tvTime.setText("");
        }

        viewHolder.tvContent.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (msg.m_msg.contains(".amr")) {
                    playMusic(android.os.Environment.getExternalStorageDirectory()+"/"+msg.m_msg) ;
                } else if (msg.m_type.equals("10001")) {
                    startPlayDevice(msg);
                }
            }
        });

        viewHolder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClickMsg(msg);
                return true;
            }
        });

        viewHolder.tvUserName.setText(msg.m_from);
        return convertView;
    }

    private void onLongClickMsg(FriendMessageList.Message msg) {
        m_act.onLongClickMsg(msg);
    }

    private void startPlayDevice(FriendMessageList.Message msg) {
        m_act.startPlayDevice(msg);
    }

    static class ViewHolder {
        public TextView tvSendTime;
        public TextView tvUserName;
        public TextView tvContent;
        public TextView tvTime;
        public boolean isComMsg = true;
    }

    /**
     * @Description
     * @param name
     */
    private void playMusic(String name) {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(name);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stop() {

    }
}
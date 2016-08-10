package com.seetong.app.seetong.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Comment;
import com.seetong.app.seetong.ui.utils.DataCheckUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/8/9.
 */
public class CommentListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Comment> data;

    public CommentListAdapter(Context context, List<Comment> data) {
        this.context = context;
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    private class ViewHolder {
        public TextView commentUsername;
        public TextView commentInfo;
        public TextView commentTime;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.comment_list_item, parent, false);
            viewHolder.commentUsername = (TextView) convertView.findViewById(R.id.comment_user_name);
            viewHolder.commentInfo = (TextView) convertView.findViewById(R.id.comment_info);
            viewHolder.commentTime = (TextView) convertView.findViewById(R.id.comment_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String userName = data.get(position).getUserName();
        if (DataCheckUtil.isRightPhone(userName)) {
            userName = userName.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        } else if (DataCheckUtil.isRightEmail(userName)) {
            userName = userName.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4");
        }

        viewHolder.commentUsername.setText(userName);
        viewHolder.commentInfo.setText(data.get(position).getCommentInfo());
        viewHolder.commentTime.setText(data.get(position).getCommentTime());

        return convertView;
    }
}

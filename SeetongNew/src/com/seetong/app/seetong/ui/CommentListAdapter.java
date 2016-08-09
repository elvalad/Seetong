package com.seetong.app.seetong.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.Comment;

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

        viewHolder.commentUsername.setText(data.get(position).getUserName());
        viewHolder.commentInfo.setText(data.get(position).getCommentInfo());
        viewHolder.commentTime.setText(data.get(position).getCommentTime());

        return convertView;
    }
}

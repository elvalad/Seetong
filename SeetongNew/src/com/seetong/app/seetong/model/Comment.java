package com.seetong.app.seetong.model;

/**
 * Created by Administrator on 2016/8/9.
 */
public class Comment {

    private String userName = "";

    private String commentInfo = "";

    private String commentTime = "";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCommentInfo() {
        return commentInfo;
    }

    public void setCommentInfo(String commentInfo) {
        this.commentInfo = commentInfo;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }
}

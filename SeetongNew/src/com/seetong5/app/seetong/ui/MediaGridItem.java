package com.seetong5.app.seetong.ui;

/**
 * Created by Administrator on 2015/9/24.
 */
public class MediaGridItem {
    /* ý���ļ�·�� */
    private String path;

    /* ý���ļ�ʱ�� */
    private String time;

    /* ÿ��Item��Ӧ��HeaderId  */
    private int section;

    private boolean isChoosed;

    public MediaGridItem(String path, String time) {
        super();
        this.path = path;
        this.time = time;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public int getSection() {
        return section;
    }
    public void setSection(int section) {
        this.section = section;
    }
    public void setIsChoosed(boolean isChoosed) {
        this.isChoosed = isChoosed;
    }
    public boolean getIsChoosed() {
        return isChoosed;
    }
}

package com.seetong.app.seetong.ui;

/**
 * Created by Administrator on 2015/9/24.
 */
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.seetong.app.seetong.Global;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class VideoScanner {
    private Context mContext;
    private List<MediaGridItem> mediaGridItemList = new ArrayList<>();

    public VideoScanner(Context context){
        this.mContext = context;
    }

    /**
     * ����ContentProviderɨ���ֻ��е�ͼƬ���˷��������������߳���
     */
    public void scanVideo(final ScanCompleteCallBack callback) {
        final Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                callback.scanComplete((List<MediaGridItem>)msg.obj);
            }
        };

        new Thread(new Runnable() {

            @Override
            public void run() {
                getData();
                //����Handler֪ͨ�����߳�
                Message msg = mHandler.obtainMessage();
                msg.obj = mediaGridItemList;
                mHandler.sendMessage(msg);
            }
        }).start();

    }


    public static interface ScanCompleteCallBack{
        public void scanComplete(List<MediaGridItem> videoItem);
    }

    private void getData() {
        /* ɨ���ͼĿ¼���ӽ�ͼĿ¼�л�ȡ��ص� */
        String videoDir = Global.getVideoDir() + "/";
        //File[] files = new File(videoDir).listFiles();
        List<File> files = getAllVideoFile(videoDir);
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            if (f.isFile() && (f.getPath().substring(f.getPath().lastIndexOf(".")).equals(".mp4"))) {
                Date lastModDate = new Date(f.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                MediaGridItem gridItem = new MediaGridItem(f.getPath(), format.format(lastModDate));
                mediaGridItemList.add(gridItem);
            }
        }
    }

    /* �ݹ��ȡ��ƵĿ¼�µ�������Ƶ�ļ� */
    List<File> videoFile = new ArrayList<>();
    private List<File> getAllVideoFile(String videoDir) {
        File[] files = new File(videoDir).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                getAllVideoFile(files[i].getPath());
            } else {
                videoFile.add(files[i]);
            }
        }
        return videoFile;
    }
}


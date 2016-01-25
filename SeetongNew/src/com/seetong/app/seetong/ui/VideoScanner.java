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
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
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
                //利用Handler通知调用线程
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
        /* 扫描截图目录，从截图目录中获取相关的 */
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

    /* 递归获取视频目录下的所有视频文件 */
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


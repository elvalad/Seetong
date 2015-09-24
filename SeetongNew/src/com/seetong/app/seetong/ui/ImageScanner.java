package com.seetong.app.seetong.ui;

import android.content.Context;
import com.seetong.app.seetong.Global;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2015/9/24.
 */
import android.os.Handler;


public class ImageScanner {
    private Context mContext;
    private List<MediaGridItem> mediaGridItemList = new ArrayList<>();

    public ImageScanner(Context context){
        this.mContext = context;
    }

    /**
     * ����ContentProviderɨ���ֻ��е�ͼƬ���˷��������������߳���
     */
    public void scanImages(final ScanCompleteCallBack callback) {
        final Handler mHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                callback.scanComplete((List<MediaGridItem>)msg.obj);
            }
        };

        new Thread(new Runnable() {

            @Override
            public void run() {
                getData();
                //����Handler֪ͨ�����߳�
                android.os.Message msg = mHandler.obtainMessage();
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
        String imageDir = Global.getImageDir() + "/";
        File[] files = new File(imageDir).listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile() && (f.getPath().substring(f.getPath().lastIndexOf(".")).equals(".jpg"))) {
                Date lastModDate = new Date(f.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                MediaGridItem gridItem = new MediaGridItem(f.getPath(), format.format(lastModDate));
                mediaGridItemList.add(gridItem);
            }
        }
    }
}



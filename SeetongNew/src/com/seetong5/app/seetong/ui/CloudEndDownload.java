package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.model.ArchiveRecord;
import com.seetong5.app.seetong.model.ObjectsRoster;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CloudEndDownload extends BaseActivity implements GestureDetector.OnGestureListener, View.OnClickListener, View.OnTouchListener {
    String m_device_id;
    String m_play_file_name = "";
    int m_current_page = 0;
    String m_record_date;
    ObjectsRoster<ArchiveRecord> m_records = new ObjectsRoster<ArchiveRecord>();

    private GestureDetector m_gd;
    private boolean m_is_layout_land = false;

    private ListView m_listView;
    private ListViewAdapter m_adapter;
    private PopupWindow m_menu;
    private ProgressDialog mTipDlg;
    int m_record_pos = 0;

    int m_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_end_download);
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, "");
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);
        m_record_date = getIntent().getStringExtra(Constant.EXTRA_RECORD_DATE);
        if (TextUtils.isEmpty(m_record_date)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            m_record_date = df.format(new Date());
        }

        View menu = getLayoutInflater().inflate(R.layout.cloud_end_record_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_download).setOnClickListener(this);

        m_listView = (ListView) findViewById(R.id.lv_files);
        m_adapter = new ListViewAdapter(m_listView.getContext());
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(m_adapter);

        //m_gd = new GestureDetector(this, this);
        //m_gd.setOnDoubleTapListener(new OnDoubleClick());

        setButtonStatus();

        Button btnFinish = (Button) findViewById(R.id.btn_title_right);
        btnFinish.setText(R.string.more);
        btnFinish.setVisibility(View.GONE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().addHandler(m_handler);
        loadData(m_record_date);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData(final String date) {
        m_records.objectClearAll();

        /*mTipDlg.setCallback(new ProgressDialog.ITimeoutCallback() {
            @Override
            public void onTimeout() {
                finish();
            }
        });*/

        showTipDlg(R.string.get_cloud_end_record_tip, 60000, R.string.get_cloud_end_record_timeout);
        loadData(date, m_type);
    }

    public void loadData(final String date, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = LibImpl.getInstance().getFuncLib().SearchOssObjectList(m_device_id, date, type);
                if (0 != ret) {
                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_CLOUD_END_RECORD;
                    msg.arg1 = R.string.get_cloud_end_record_failed;
                    m_handler.sendMessage(msg);
                    return;
                }
            }
        }).start();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(0);
            if (null == dev) {
                return false;
            }

            if (m_is_layout_land) {
                if (dev.m_playing) {
                    View v = findViewById(R.id.layout_h_play_control);
                    v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //maximizeView(PLAY_WND_ID);
            return true;
        }
    }

    class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context m_ctx;
        private LayoutInflater mInflater;

        public ListViewAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
        }

        public class ViewHolder extends RelativeLayout {
            public ImageView imgView;
            public TextView tvCaption;
            public ProgressBar pbDownload;
            public ImageButton btnDownload;
            public ImageButton btnDelete;
            private ArchiveRecord m_record;

            public ViewHolder(Context context) {
                this(context, null, 0);
            }

            public ViewHolder(Context context, AttributeSet attrs) {
                this(context, attrs, 0);
            }

            public ViewHolder(Context context, AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                LayoutInflater.from(context).inflate(R.layout.cloud_end_download_list_item, this);
                imgView = (ImageView) findViewById(R.id.img);
                tvCaption = (TextView) findViewById(R.id.tvCaption);
                pbDownload = (ProgressBar) findViewById(R.id.id_pb_download);
                btnDownload = (ImageButton) findViewById(R.id.btn_download);
                btnDelete = (ImageButton) findViewById(R.id.btn_delete);

                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String record_name = m_record.getName();
                        final String local_file = Global.getCloudDir() + "/" + record_name;
                        int ret = LibImpl.getInstance().startCloudDownload(m_device_id, m_record, local_file);
                        if (0 != ret) {
                            toast(ConstantImpl.getOssErrText(ret));
                            return;
                        }

                        //ViewHolder.this.invalidate();
                        m_adapter.notifyDataSetChanged();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(CloudEndDownload.this)
                                .setTitle(T(R.string.dlg_tip))
                                .setMessage(T(R.string.dlg_delete_picture_tip))
                                .setNegativeButton(T(R.string.cancel), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(T(R.string.sure), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        String record_name = m_record.getName();
                                        final String local_file = Global.getCloudDir() + "/" + record_name;
                                        File file = new File(local_file);
                                        if (!file.delete()) {
                                            toast(R.string.oss_error_del_failed);
                                        }

                                        m_adapter.notifyDataSetChanged();
                                    }
                                }).create().show();
                    }
                });
            }

            public void setRecord(ArchiveRecord record) {
                m_record = record;
            }

            public void update(int position) {
                ArchiveRecord record = m_records.objectAt(position);
                m_record = record;
                String record_name = m_record.getName();
                tvCaption.setText(record_name.substring(record_name.lastIndexOf('/') + 1));
                imgView.setImageResource(R.drawable.record);

                long size = Long.parseLong(m_record.getSize());
                if (0 == size) return;
                final String local_file = Global.getCloudDir() + "/" + record_name;
                String dir = local_file.substring(0, local_file.lastIndexOf('/'));
                File file = new File(dir);
                if (!file.exists()) file.mkdirs();
                file = new File(local_file);
                if (!file.exists()) {
                    if (record.mDownloadStatus == ArchiveRecord.STATUS_DOWNLOADING || record.mDownloadStatus == ArchiveRecord.STATUS_START) {
                        pbDownload.setVisibility(View.VISIBLE);
                        btnDownload.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.GONE);
                    } else {
                        pbDownload.setVisibility(View.GONE);
                        btnDownload.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.GONE);
                        return;
                    }
                } else {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        int len = fis.available();
                        if (len == size) {
                            btnDownload.setVisibility(View.GONE);
                            btnDelete.setVisibility(View.VISIBLE);
                            pbDownload.setVisibility(View.VISIBLE);
                            pbDownload.setProgress(100);
                            return;
                        } else {
                            if (record.mDownloadStatus == ArchiveRecord.STATUS_DOWNLOADING || record.mDownloadStatus == ArchiveRecord.STATUS_START) {
                                pbDownload.setVisibility(View.VISIBLE);
                                btnDownload.setVisibility(View.VISIBLE);
                                btnDelete.setVisibility(View.GONE);
                            } else {
                                pbDownload.setVisibility(View.GONE);
                                btnDownload.setVisibility(View.VISIBLE);
                                btnDelete.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                long dsize = m_record.getDownloadSize();
                int pos = (int) (dsize != 0 ? dsize / (size * 0.01) : 0);
                if (size == dsize) pos = 100;
                pbDownload.setProgress(pos);
            }
        }

        public void updateDownloadPos(ArchiveRecord record) {
            if (null == record) return;
            int start = m_listView.getFirstVisiblePosition();
            for (int i = start; i <= m_listView.getLastVisiblePosition(); i++) {
                if (record != m_listView.getItemAtPosition(i)) continue;
                View v = m_listView.getChildAt(i - start);
                getView(i, v, m_listView);
                break;
            }
        }

        @Override
        public int getCount() {
            return m_records.objectCount();
        }

        @Override
        public Object getItem(int position) {
            return m_records.objectAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (v == null) {
                viewHolder = new ViewHolder(CloudEndDownload.this);
            } else {
                viewHolder = (ViewHolder) v;
            }

            viewHolder.update(position);
            return viewHolder;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            ArchiveRecord record = m_records.objectAt(pos);
            playFile(Global.getCloudDir() + "/" + record.getName());
        }
    }

    private void playFile(String file) {
        if (TextUtils.isEmpty(file)) return;
        File f = new File(file);
        if (!f.exists()) {
            toast(R.string.please_download_after_player);
            return;
        }

        try {
            Intent it = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://" + file);
            //Uri uri = Uri.parse("http://test1222.oss-cn-shenzhen.aliyuncs.com/093546-1-vv-10.flv");
            it.setDataAndType(uri, "video/mp4");//mp4
            this.startActivity(it);
        } catch (ActivityNotFoundException e) {
            MainActivity2.m_this.toast(R.string.not_open_file_use_third_party_app);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_right:
                onBtnTitleRight(v);
                break;
            case R.id.btn_download:
                m_menu.dismiss();
                onBtnDownload();
                break;
            case R.id.btn_sub_stream:
                m_menu.dismiss();
                onBtnSubStream();
                break;
            default: break;
        }
    }

    private void onBtnDownload() {
        Intent it = new Intent(this, CloudEndDownload.class);
        startActivity(it);
    }

    private void onBtnMainStream() {
        m_type = 1;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        //String date = df.format(m_find_date);
        loadData(m_record_date, m_type);
    }

    private void onBtnSubStream() {
        m_type = 2;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        //String date = df.format(m_find_date);
        loadData(m_record_date, m_type);
    }

    private void onBtnTitleRight(View v) {
        if (m_menu.isShowing()) {
            m_menu.dismiss();
        } else {
            m_menu.showAsDropDown(v);
            m_menu.setBackgroundDrawable(new BitmapDrawable(null, (Bitmap)null));
            m_menu.setOutsideTouchable(true);
            m_menu.dismiss();
            m_menu.showAsDropDown(v);
            m_menu.setBackgroundDrawable(new BitmapDrawable(null, (Bitmap)null));
            m_menu.setOutsideTouchable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LibImpl.getInstance().removeHandler(m_handler);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            m_is_layout_land = true;
            startFullScreen(true);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            m_is_layout_land = false;
            startFullScreen(false);
        }
    }

    public void startFullScreen(boolean isFull) {
        // 全屏时切换为横屏显示视频
        //mbIsFullScreen = isFull;
        //配置视频视图间隔
//		float smallInterval = this.getResources().getDimensionPixelSize(R.dimen.small_interval);
//		int width = mbIsFullScreen ? 0 : FunUtils.px2dip(this, smallInterval);
//		findViewById(R.id.Linear_Fourview).setPadding(width, width, width, width);

        /*int show = isFull ? View.GONE : View.VISIBLE;
        findViewById(R.id.layout_title).setVisibility(show);
        //m_view.findViewById(R.id.hsv_video_ptz).setVisibility(show);
        findViewById(R.id.layout_play_control).setVisibility(show);
        findViewById(R.id.layout_action).setVisibility(show);

        if (!isFull) {
            findViewById(R.id.layout_h_play_control).setVisibility(View.GONE);
        }*/
    }

    @Override
    public void handleMessage(Message msg) {
        if (!isTopActivity(CloudEndDownload.class.getName())) return;
        int flag = msg.arg1;
        switch (msg.what) {
            case Define.MSG_CLOUD_END_RECORD:
                onCloudEndRecord(msg);
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_SEARCH_OSS_OBJECTLIST:
                ObjectsRoster<ArchiveRecord> resp = (ObjectsRoster<ArchiveRecord>) msg.obj;
                onGetCloudEndRecord(flag, resp);
                break;
            case SDK_CONSTANT.TPS_MSG_BEGIN_DOWNLOAD_OSS_OBJECT:
                ArchiveRecord record = (ArchiveRecord) msg.obj;
                onBeginDownloadOssObject(record);
                break;
            case SDK_CONSTANT.TPS_MSG_DOWNLOAD_OSS_OBJECT_SIZE:
                record = (ArchiveRecord) msg.obj;
                onDownloadOssObjectSize(record);
                break;
            case SDK_CONSTANT.TPS_MSG_END_DOWNLOAD_OSS_OBJECT:
                record = (ArchiveRecord) msg.obj;
                onEndDownloadOssObject(record);
                break;
            case SDK_CONSTANT.TPS_MSG_DOWNLOAD_OSS_OBJECT_FAILED:
                record = (ArchiveRecord) msg.obj;
                onDownloadOssObjectFailed(record, msg.arg1);
                break;
        }
    }

    private void onCloudEndRecord(Message msg) {
        switch (msg.arg1) {
            case R.string.get_cloud_end_record_failed:
                mTipDlg.dismiss();
                toast(R.string.get_cloud_end_record_failed);
                break;
        }
    }

    private void onGetCloudEndRecord(int flag, ObjectsRoster<ArchiveRecord> resp) {
        if (flag != 0 || null == resp) {
            mTipDlg.dismiss();
            toast(R.string.get_cloud_end_record_failed);
            return;
        }

        List<ArchiveRecord> lst = resp.getObjectList();
        Iterator itr = lst.iterator();
        while (itr.hasNext()) {
            ArchiveRecord record = (ArchiveRecord) itr.next();
            String name = record.getName();
            if (TextUtils.isEmpty(name) || !name.contains(".idx")) {
                ArchiveRecord exist = LibImpl.getInstance().getCloudDownloadObject(record.getDevId(), name);
                if (exist != null) {
                    m_records.objectAdd(exist, false);
                } else {
                    m_records.objectAdd(record, false);
                }
                continue;
            }
            itr.remove();
        }

        if (m_type == 0) {
            m_type = 1;
            loadData(m_record_date, m_type);
            return;
        } else if (m_type == 1) {
            m_type = 2;
            loadData(m_record_date, m_type);
            return;
        }

        LibImpl.getInstance().notifyCloudDownloadStart();
        m_adapter.notifyDataSetChanged();
        mTipDlg.dismiss();
    }

    private void onOssReplayFinish() {

    }

    private void onBeginDownloadOssObject(ArchiveRecord record) {
        if (null == record) return;
        m_adapter.notifyDataSetChanged();
    }

    private void onDownloadOssObjectSize(ArchiveRecord record) {
        m_adapter.updateDownloadPos(record);
    }

    private void onEndDownloadOssObject(ArchiveRecord record) {
        if (null == record) return;
        m_adapter.notifyDataSetChanged();
    }

    private void onDownloadOssObjectFailed(ArchiveRecord record, int reason) {
        if (null == record) return;
        m_adapter.notifyDataSetChanged();
        if (0 != reason) {
            toast(ConstantImpl.getOssErrText(reason));
        }
    }

    private void setButtonStatus() {
        /*findViewById(R.id.btn_prev).setEnabled(true);
        findViewById(R.id.btn_h_prev).setEnabled(true);
        findViewById(R.id.btn_next).setEnabled(true);
        findViewById(R.id.btn_h_next).setEnabled(true);
        if (m_records.m_record_files.size() < 2) {
            findViewById(R.id.btn_prev).setEnabled(false);
            findViewById(R.id.btn_h_prev).setEnabled(false);
            findViewById(R.id.btn_next).setEnabled(false);
            findViewById(R.id.btn_h_next).setEnabled(false);
            return;
        }

        if (m_record_pos <= 0) {
            m_record_pos = 0;
            findViewById(R.id.btn_prev).setEnabled(false);
            findViewById(R.id.btn_h_prev).setEnabled(false);
        } else if (m_record_pos >= m_records.m_record_files.size() - 1) {
            m_record_pos = m_records.m_record_files.size() - 1;
            findViewById(R.id.btn_next).setEnabled(false);
            findViewById(R.id.btn_h_next).setEnabled(false);
        }*/
    }
}
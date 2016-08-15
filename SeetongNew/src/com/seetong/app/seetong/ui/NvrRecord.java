package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.android.audio.AudioPlayer;
import com.android.opengles.OpenglesRender;
import com.android.opengles.OpenglesView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.ArchiveRecord;
import com.seetong.app.seetong.model.ObjectsRoster;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.aid.MarqueeTextView;
import com.seetong.app.seetong.ui.ext.DateTimeHelper;
import com.seetong.app.seetong.ui.ext.TimeLine;
import ipc.android.sdk.com.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NvrRecord extends BaseActivity implements GestureDetector.OnGestureListener, View.OnClickListener, View.OnTouchListener {
    String m_device_id;
    String m_play_file_name = "";
    final int PLAY_WND_ID = 29;
    int m_current_page = 0;
    String m_start_time;
    String m_end_time;
    String m_date;
    ObjectsRoster<ArchiveRecord> m_records = new ObjectsRoster<ArchiveRecord>();
    Map<String, ArchiveRecord> m_idx_records = new HashMap<>();
    ArchiveRecord m_current_record = null;
    int m_current_index = -1;
    OpenglesView m_glView;
    OpenglesRender m_glRender;

    private ImageView recordView;
    private Chronometer timer;

    private View m_time_line_layout;
    private TimeLine m_time_line;
    private long m_begin_time = 0;

    private GestureDetector m_gd;
    private boolean m_is_layout_land = false;

    private PopupWindow m_menu;

    private ProgressDialog mTipDlg;
    SeekBar m_seekbar_play;
    SeekBar m_seekbar_sound;
    SeekBar m_seekbar_h_sound;
    int m_play_seek_pos = 0;
    int m_play_speed = 0;
    int m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_STOP;
    int m_record_pos = 0;

    AudioManager m_audioManage;

    int m_type = 0;
    Date m_find_date = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nvr_end_record);
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);

        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null != dev) dev.m_replay = true;

        View menu = getLayoutInflater().inflate(R.layout.cloud_end_record_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_download).setOnClickListener(this);

        ((TextView) findViewById(R.id.txt_title)).setText(m_device_id);

        m_gd = new GestureDetector(this, this);
        m_gd.setOnDoubleTapListener(new OnDoubleClick());

        m_glView = (OpenglesView) findViewById(R.id.livePreview);
        m_glRender = new OpenglesRender(m_glView, PLAY_WND_ID);
        m_glRender.setVideoMode(OpenglesRender.VIDEO_MODE_CUSTOM);// VIDEO_MODE_FIT
        m_glView.setRenderer(m_glRender);
        m_glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        //m_glView.setOnClickListener(this);
        m_glView.setLongClickable(true);
        m_glView.setOnTouchListener(new TouchListener());

        m_glRender.addCheckCallback(new OpenglesRender.CheckCallback() {
            @Override
            public void recvDataTimout(int branch, OpenglesRender render) {
                /*PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(branch);
                if (render == null) {
                    if (T(R.string.tv_video_play_tip).compareToIgnoreCase(dev.m_tipInfo) != 0) {
                        setVideoInfo(branch, T(R.string.tv_video_play_tip));
                    }
                    return;
                }

                setVideoInfo(branch, T(R.string.tv_video_wait_video_stream_tip));//超时提醒*/
            }
        });

        m_time_line_layout = findViewById(R.id.timeline_layout);
        m_time_line = (TimeLine) findViewById(R.id.wgtTimeline);
        m_time_line.setRecordColor(getResources().getColor(R.color.timeline_schedule));
        m_time_line.setIndicatorColor(getResources().getColor(R.color.timeline_indicator));
        m_time_line.setSliderTimestamp(System.currentTimeMillis());
        m_time_line.setDelegate(new TimeLine.TimelineDelegate() {
            @Override
            public void onDateChanged(long time) {
                if (mTipDlg.isShowing()) return;
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                m_find_date = new Date(time);
                m_date = df.format(m_find_date);
                onBtnStop();
                m_type = 0;
                loadData(m_date);
            }

            @Override
            public void onRecordNotFound() {

            }

            @Override
            public void onRecordSelected(ArchiveRecord record, int index, long time) {
                if (mTipDlg.isShowing()) return;
                m_current_record = record;
                m_current_index = index;
                if (m_play_status != REPLAY_NVR_ACTION.NVR_ACTION_STOP) {
                    replaySeek(record, time);
                    return;
                }

                if (startReplay(record, time) != 0) return;
            }
        });

        findViewById(R.id.btnZoomIn).setOnClickListener(this);
        findViewById(R.id.btnZoomOut).setOnClickListener(this);

        m_seekbar_play = (SeekBar) findViewById(R.id.seekbar_play);
        m_seekbar_play.setOnSeekBarChangeListener(osbcl);
        m_seekbar_sound = (SeekBar) findViewById(R.id.seekbar_sound);
        m_seekbar_h_sound = (SeekBar) findViewById(R.id.seekbar_h_sound);
        m_seekbar_sound.setOnSeekBarChangeListener(osbcl_sound);
        m_seekbar_h_sound.setOnSeekBarChangeListener(osbcl_sound);

        m_audioManage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = m_audioManage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        m_seekbar_sound.setMax(maxVolume);
        m_seekbar_h_sound.setMax(maxVolume);
        int currentVolume = m_audioManage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
        m_seekbar_sound.setProgress(currentVolume);
        m_seekbar_h_sound.setProgress(currentVolume);

        View v = findViewById(R.id.layout_h_play_control);
        v.setVisibility(View.GONE);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_h_prev).setOnClickListener(this);
        findViewById(R.id.btn_slow).setOnClickListener(this);
        findViewById(R.id.btn_h_slow).setOnClickListener(this);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_h_play).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_h_stop).setOnClickListener(this);
        findViewById(R.id.btn_fast).setOnClickListener(this);
        findViewById(R.id.btn_h_fast).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_h_next).setOnClickListener(this);
        findViewById(R.id.btn_sound_min).setOnClickListener(this);
        findViewById(R.id.btn_h_sound_min).setOnClickListener(this);
        findViewById(R.id.btn_sound_max).setOnClickListener(this);
        findViewById(R.id.btn_h_sound_max).setOnClickListener(this);
        findViewById(R.id.btn_calendar).setOnClickListener(this);

        findViewById(R.id.btn_nvr_record).setOnClickListener(this);
        findViewById(R.id.btn_nvr_capture).setOnClickListener(this);

        recordView = (ImageView) findViewById(R.id.imgRecord);
        timer = (Chronometer) findViewById(R.id.recordChronometer);

        setButtonStatus();
        //setVideoInfo("[" + m_device_id + "]");

        Button btnFinish = (Button) findViewById(R.id.btn_title_right);
        btnFinish.setText(R.string.more);
        btnFinish.setVisibility(View.GONE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().addHandler(m_handler);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        m_date = df.format(m_find_date);
        loadData(m_date);
    }

    private SeekBar.OnSeekBarChangeListener osbcl = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) return;
            if (m_play_status == REPLAY_NVR_ACTION.NVR_ACTION_STOP) {
                m_seekbar_play.setProgress(0);
                return;
            }

            m_play_seek_pos = progress;
            LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_NVR_ACTION.NVR_ACTION_SEEK, progress);
            setVideoInfo(null);
            m_play_speed = 0;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener osbcl_sound = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            m_audioManage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            int currentVolume = m_audioManage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
            m_seekbar_sound.setProgress(currentVolume);
            m_seekbar_h_sound.setProgress(currentVolume);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void setVideoInfo(final String msg) {
        MarqueeTextView v = (MarqueeTextView) findViewById(R.id.tvLiveInfo);
        v.setVisibility(View.VISIBLE);

        if (null == msg) {
            v.setVisibility(View.GONE);
            return;
        }

        v.setText(msg);
    }

    public void loadData(final String date) {
        m_current_page = 0;
        m_records.objectClearAll();

        /*mTipDlg.setCallback(new ProgressDialog.ITimeoutCallback() {
            @Override
            public void onTimeout() {
                finish();
            }
        });*/

        showTipDlg(R.string.get_nvr_record_tip, 15000, R.string.get_nvr_record_timeout);
        loadData(date, m_type);
    }

    public void loadData(final String date, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 定时录像
                int ret = LibImpl.getInstance().getFuncLib().P2PSearchNvrRecByTime(m_device_id, date);
                if (0 != ret) {
                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_NVR_RECORD;
                    msg.arg1 = R.string.get_nvr_record_failed;
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
        if (m_is_layout_land) {
            m_time_line_layout.setVisibility(m_time_line_layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }

        final View v = findViewById(R.id.btn_play);
        if (v.getVisibility() == View.VISIBLE) return false;
        v.setVisibility(View.VISIBLE);
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.GONE);
            }
        }, 5000);
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
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
            if (null == dev) {
                return false;
            }

            /*if (m_is_layout_land) {
                if (dev.m_playing) {
                    View v = findViewById(R.id.layout_h_play_control);
                    v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    return true;
                }
            }*/

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //maximizeView(PLAY_WND_ID);
            return true;
        }
    }

    class TouchListener implements View.OnTouchListener {
        private PointF startPoint = new PointF();
        private Matrix matrix = new Matrix();
        private Matrix currentMatrix = new Matrix();
        private int mode = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private float startDis;//开始距离
        private PointF midPoint;//中间点
        private int m_offset = 10;

        private static final float MAX_SCALE = 4.0f;
        private static final float MIN_SCALE = 0.25f;
        private static final float MIN_SPCE = 10f;
        private float preScale;
        private float oldDist = 1f;
        private PointF start = new PointF();
        private PointF mid = new PointF();
        PointF startOffset = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //setFocusWnd((Integer) v.getTag());
            //m_current_index = (int) v.getTag();
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
            if (null == dev) return m_gd.onTouchEvent(event);
            //setButtonsStatus(dev);
            OpenglesRender render = dev.m_video;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN://手指压下屏幕
                    mode = DRAG;
                    preScale = render.bitmapScale;
                    start.set(event.getX(), event.getY());
                    startOffset.set(render.mStartX, render.mStartY);
                    break;
                case MotionEvent.ACTION_MOVE://手指在屏幕移动，该事件会不断地触发
                    if (mode == DRAG) {
                        if (render.bitmapScale > 1.0) {
                            float dtX = event.getX() - start.x;
                            float dtY = event.getY() - start.y;
                            start.set(event.getX(), event.getY());
                            render.mStartX += dtX;
                            render.mStartY -= dtY;

                            //向右拖动
                            if (dtX > 0) {
                                if (render.mScaleBitmapW < render.mViewWidth) {
                                    if (render.mViewWidth - (render.mTargetX + render.mStartX + render.mScaleBitmapW) < 10)
                                        render.mStartX = render.mViewWidth - (int) (render.mTargetX + render.mScaleBitmapW) - 10;
                                } else {
                                    int w = render.mScaleBitmapW - render.mViewWidth + 10;
                                    if ((render.mViewWidth + w) - (render.mTargetX + render.mStartX + render.mScaleBitmapW) < 10)
                                        render.mStartX = (render.mViewWidth + w) - (int) (render.mTargetX + render.mScaleBitmapW);
                                }
                            } else {
                                if (render.mScaleBitmapW < render.mViewWidth) {
                                    if (render.mTargetX - Math.abs(render.mStartX) < 10)
                                        render.mStartX = (int) -(render.mTargetX - 10);
                                } else {
                                    int w = render.mScaleBitmapW - render.mViewWidth + 10;
                                    if ((render.mTargetX + w) - Math.abs(render.mStartX) < 0)
                                        render.mStartX = (int) -(render.mTargetX + w);
                                }
                            }

                            //向下拖动
                            if (dtY > 0) {
                                if (render.mScaleBitmapH < render.mViewHeight) {
                                    if (render.mTargetY - Math.abs(render.mStartY) < 10)
                                        render.mStartY = (int) -(render.mTargetY - 10);
                                } else {
                                    int h = render.mScaleBitmapH - render.mViewHeight + 10;
                                    if ((render.mTargetY + h) - Math.abs(render.mStartY) < 0)
                                        render.mStartY = (int) -(render.mTargetY + h);
                                }
                            } else {
                                if (render.mScaleBitmapH < render.mViewHeight) {
                                    if (render.mViewHeight - (render.mTargetY + render.mStartY + render.mScaleBitmapH) < 10)
                                        render.mStartY = render.mViewHeight - (int) (render.mTargetY + render.mScaleBitmapH) - 10;
                                } else {
                                    int h = render.mScaleBitmapH - render.mViewHeight + 10;
                                    if ((render.mViewHeight + h) - (render.mTargetY + render.mStartY + render.mScaleBitmapH) < 10)
                                        render.mStartY = (render.mViewHeight + h) - (int) (render.mTargetY + render.mScaleBitmapH);
                                }
                            }
                        }
                    } else if (mode == ZOOM) {// 此实现图片的缩放功能...
                        float newDist = spacing(event);
                        if (newDist > MIN_SPCE) {
                            float scale = (newDist / oldDist) * preScale;
                            scale = (scale >= MAX_SCALE) ? MAX_SCALE : scale;
                            scale = (scale <= MIN_SCALE) ? MIN_SCALE : scale;
                            render.bitmapScale = scale;

                            float dtX = (scale - preScale) * render.mSrcBitmapW / 2;
                            float dtY = (scale - preScale) * render.mSrcBitmapH / 2;
                            render.mStartX = (int) (startOffset.x - dtX);
                            render.mStartY = (int) (startOffset.y - dtY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP://手指离开屏
                    mode = 0;
                    if (render.bitmapScale <= 1.0) {
                        render.resetScaleInfo();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP://有手指离开屏幕,但屏幕还有触点（手指）
                    mode = 0;
                    if (render.bitmapScale <= 1.0) {
                        render.resetScaleInfo();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN://当屏幕上已经有触点（手指），再有一个手指压下屏幕
                    oldDist = this.spacing(event);
                    if (oldDist > MIN_SPCE) {
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;
            }

            return m_gd.onTouchEvent(event);
        }

        public float spacing(MotionEvent event) {//两点的距离
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);
        }

        public void midPoint(PointF point, MotionEvent event) {//中点坐标
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_right:
                onBtnTitleRight(v);
                break;
            case R.id.btnZoomIn:
                onBtnZoomIn();
                break;
            case R.id.btnZoomOut:
                onBtnZoomOut();
                break;
            case R.id.btn_download:
                m_menu.dismiss();
                onBtnDownload();
                break;
            case R.id.btn_sub_stream:
                m_menu.dismiss();
                onBtnSubStream();
                break;
            case R.id.btn_prev:
            case R.id.btn_h_prev:
                onBtnPrev();
                break;
            case R.id.btn_slow:
            case R.id.btn_h_slow:
                onBtnSlow();
                break;
            case R.id.btn_play:
            case R.id.btn_h_play:
                onBtnPlay();
                break;
            case R.id.btn_stop:
            case R.id.btn_h_stop:
                onBtnStop();
                break;
            case R.id.btn_fast:
            case R.id.btn_h_fast:
                onBtnFast();
                break;
            case R.id.btn_next:
            case R.id.btn_h_next:
                onBtnNext();
                break;
            case R.id.btn_sound_min:
            case R.id.btn_h_sound_min:
                onBtnSoundMin();
                break;
            case R.id.btn_sound_max:
            case R.id.btn_h_sound_max:
                onBtnSoundMax();
                break;
            case R.id.btn_calendar:
                onBtnCalendar();
                break;
            case R.id.btn_nvr_record:
                PlayerDevice dev = Global.getDeviceById(m_device_id);
                if (null == dev) return;
                if (dev.m_record) {
                    offBtnNvrRecord();
                } else {
                    onBtnNvrRecord();
                }
                break;
            case R.id.btn_nvr_capture:
                onBtnNvrCapture();
                break;
            default: break;
        }
    }

    private void showRecordIcon(String devId, boolean bShow) {
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev || !dev.m_playing) return;
        recordView.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);
        timer.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);
        if (bShow) {
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();
        } else {
            timer.stop();
        }
    }

    private void onBtnNvrRecord() {
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp || !dev.m_first_frame) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        // TODO:增加开始录像功能
        toast("on record " + dev.getDeviceName());
        LibImpl.getInstance().startNvrRecord(dev.m_dev.getDevId());
        dev.m_record = true;
        showRecordIcon(dev.m_devId, true);
    }

    private void offBtnNvrRecord() {
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp || !dev.m_first_frame) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        // TODO: 增加关闭录像功能
        toast("off record " + dev.getDeviceName());
        LibImpl.getInstance().stopNvrRecord();
        dev.m_record = false;
        showRecordIcon(dev.m_devId, false);
    }

    private void stopVideoRecord() {
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev || !dev.m_playing) return;
        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) return;
        // TODO: 增加关闭录像功能
        LibImpl.getInstance().stopNvrRecord();
        dev.m_record = false;
        showRecordIcon(dev.m_devId, false);
    }

    private void onBtnNvrCapture() {
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;
        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp || !dev.m_first_frame) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        String strDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String fileName = Global.getImageDir() + "/" + dev.m_dev.getDevId() + "_" + strDate + ".jpg";
        boolean bShotOk = dev.m_video.startShot(fileName);
        if (bShotOk) {
            toast(R.string.snapshot_succeed);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    MainActivity2.m_this.sendMessage(Define.MSG_UPDATE_SCREENSHOT_LIST, 0, 0, null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onBtnZoomOut() {
        m_time_line.applyZoom(false);
    }

    private void onBtnZoomIn() {
        m_time_line.applyZoom(true);
    }

    private void onBtnDownload() {
        stopReplay();
        Intent it = new Intent(this, CloudEndDownload.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, m_device_id);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        it.putExtra(Constant.EXTRA_RECORD_DATE, date);
        startActivity(it);
    }

    private void onBtnMainStream() {
        m_type = 1;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        onBtnStop();
        loadData(date);
    }

    private void onBtnSubStream() {
        m_type = 2;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        onBtnStop();
        loadData(date);
    }

    private void onBtnSoundMin() {
        m_seekbar_sound.setProgress(0);
    }

    private void onBtnSoundMax() {
        m_seekbar_sound.setProgress(m_seekbar_sound.getMax());
    }

    private void onBtnPrev() {
        /*if (m_records.m_record_files.isEmpty()) {
            toast(R.string.get_front_end_record_not_found);
            return;
        }

        m_record_pos--;
        setButtonStatus();
        setVideoInfo(null);
        startReplay(m_records.m_record_files.get(m_record_pos).m_file_path);*/
    }

    private void onBtnSlow() {
        /*if (m_records.m_record_files.isEmpty()) {
            return;
        }

        if (0 == m_play_speed) {
            m_play_speed = 2;
        } else {
            m_play_speed *= 2;
        }
        if (m_play_speed > 16) {
            m_play_speed = 0;
            setVideoInfo(null);
        } else {
            setVideoInfo("-" + m_play_speed + "X");
        }

        LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_IPC_ACTION.ACTION_SLOW, m_play_speed);*/
    }

    private void onBtnPlay() {
        m_play_speed = 0;
        setVideoInfo(null);
        m_current_record = m_records.objectAt(0);
        if (null == m_current_record) return;
        if (m_play_status == REPLAY_NVR_ACTION.NVR_ACTION_STOP) {
            startReplay(m_current_record, m_current_record.getStartTime());
            return;
        }

        if (m_play_status == REPLAY_NVR_ACTION.NVR_ACTION_PAUSE) {
            replayResume();
        } else {
            replayPause();
        }
    }

    private void onBtnStop() {
        stopReplay();
        m_seekbar_play.setProgress(0);
    }

    private void onBtnFast() {
        /*if (m_records.m_record_files.isEmpty()) {
            return;
        }

        if (0 == m_play_speed) {
            m_play_speed = 2;
        } else {
            m_play_speed *= 2;
        }
        if (m_play_speed > 16) {
            m_play_speed = 0;
            setVideoInfo(null);
        } else {
            setVideoInfo("+" + m_play_speed + "X");
        }

        LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_IPC_ACTION.ACTION_FAST, m_play_speed);*/
    }

    private void onBtnNext() {
        /*if (m_records.m_record_files.isEmpty()) {
            toast(R.string.get_front_end_record_not_found);
            return;
        }

        m_record_pos++;
        setButtonStatus();
        startReplay(m_records.m_record_files.get(m_record_pos).m_file_path);*/
    }

    private void onBtnCalendar() {
        /*Intent it = new Intent(Intent.ACTION_VIEW);
        it.setDataAndType(null, CalendarActivity.MIME_TYPE);
        startActivityForResult(it, 999);*/

        //final MyCalendar calendar = new MyCalendar(this);

        /*LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 0, 5, 0);
        layout.setBackgroundColor(Color.rgb(207, 232, 179));
        layout.addView(calendar);*/

        LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View vi = inflater.inflate(R.layout.my_calendar, null);

        final AlertDialog dlg = new AlertDialog.Builder(this)/*.setTitle(R.string.dev_list_tip_title_input_user_pwd)*/
                .setView(vi).create();

        final com.seetong.app.seetong.ui.aid.CalendarView calendar = (com.seetong.app.seetong.ui.aid.CalendarView) vi.findViewById(R.id.calendar);
        calendar.setOnItemClickListener(new com.seetong.app.seetong.ui.aid.CalendarView.OnItemClickListener() {
            @Override
            public void OnItemClick(Date date) {
                m_find_date = date;
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                m_date = df.format(m_find_date);
                if (date.after(calendar.getToday())) {
                    toast(R.string.nvr_record_choose_right_time);
                    return;
                }
                m_time_line.setDisplayedDate(DateTimeHelper.getDayStartMark(date.getTime()));
                dlg.dismiss();
                onBtnStop();
                m_type = 0;
                loadData(m_date);
            }
        });

        ((TextView)vi.findViewById(R.id.calendarCenter)).setText(calendar.getYearAndMonth());

        vi.findViewById(R.id.calendarLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.clickLeftMonth();
                ((TextView)vi.findViewById(R.id.calendarCenter)).setText(calendar.getYearAndMonth());
            }
        });

        vi.findViewById(R.id.calendarRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.clickRightMonth();
                ((TextView)vi.findViewById(R.id.calendarCenter)).setText(calendar.getYearAndMonth());
            }
        });

        dlg.show();
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
        LibImpl.getInstance().m_stop_play = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        LibImpl.getInstance().m_stop_play = false;
        LibImpl.getInstance().addHandler(m_handler);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LibImpl.getInstance().removeHandler(m_handler);
        stopVideoRecord();
        stopReplay();
        m_glRender.destory();
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null != dev) {
            dev.m_replay = false;
            dev.m_video = null;
        }
        LibImpl.getInstance().m_stop_play = false;
        PlayerActivity.m_this.stopPlayFromNvr();
        PlayerActivity.m_this.startPlayFromNvr();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mTipDlg.dismiss();
        finish();
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
        int show = isFull ? View.GONE : View.VISIBLE;
        m_time_line_layout.setVisibility(show);
        findViewById(R.id.layout_title).setVisibility(show);
        //findViewById(R.id.layout_h_play_control).setVisibility(show);
    }

    @Override
    public void handleMessage(Message msg) {
        if (!isTopActivity(NvrRecord.class.getName())) return;
        int flag = msg.arg1;
        switch (msg.what) {
            case Define.MSG_NVR_RECORD:
                onNvrRecord(msg);
                break;
            case Define.MSG_RECEIVER_MEDIA_FIRST_FRAME:
                onReceiverMediaFirstFrame();
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_SEARCH_NVR_REC:
                TPS_NotifyInfo ni = (TPS_NotifyInfo) msg.obj;
                onGetNvrRecord(flag, ni);
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_NVR_REPLAY:
                TPS_ReplayDevFileRsp rdfr = (TPS_ReplayDevFileRsp) msg.obj;
                onNvrReplayResp(flag, rdfr);
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_OSS_REPLAY_PARAM:
                onOssReplayParam();
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_OSS_REPLAY_FINISH:
                onOssReplayFinish();
                break;
            case SDK_CONSTANT.TPS_MSG_OSS_PLAY_BEGIN_CACHE:
                onOssPlayBeginCache();
                break;
            case SDK_CONSTANT.TPS_MSG_OSS_PLAY_END_CACHE:
                onOssPlayEndCache();
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_REPLAY_DEV_FILE:
                onReplayDevFile(msg);
                break;
            case LibImpl.MSG_VIDEO_SET_STATUS_INFO:
                LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                    /*if (null != msgObj.reserveObj) {
                        setTipText(msgObj.devID, msgObj.recvObj, (String) msgObj.reserveObj);
                    } else {
                        setTipText(msgObj.devID, msgObj.recvObj);
                    }*/
                break;
            case LibImpl.MSG_REPLAY_SET_POSITION:
                onReplaySetPosition(msg);
                break;
        }
    }

    private void onNvrReplayResp(int flag, TPS_ReplayDevFileRsp rdfr) {
        if (flag != 0) {
            mTipDlg.dismiss();
            if (-7 == flag) {
                toast(T(R.string.nvr_record_replay_failed) + "-" + T(R.string.err_all_stream_full));
            } else if (-9 == flag) {
                toast(T(R.string.nvr_record_replay_failed) + "-" + T(R.string.err_get_video_cfg_fail));
            } else if (-10 == flag) {
                toast(T(R.string.nvr_record_replay_failed) + "-" + T(R.string.err_get_stream_fail));
            } else {
                toast(T(R.string.nvr_record_replay_failed) + "-" + flag);
            }
        }
    }

    private void onOssReplayFinish() {
        ArchiveRecord r = m_time_line.getNext(m_current_index);
        if (null == r) return;
        Log.i(NvrRecord.class.getName(), "onOssReplayFinish:current file=[" + m_current_record.getName() + "],next file=[" + r.getName() + "]");
        m_time_line.selectTime(r.getStartTime());
    }

    private void onOssPlayBeginCache() {
        if (m_play_status != REPLAY_NVR_ACTION.NVR_ACTION_PLAY) return;
        mTipDlg.setCancelable(true);
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
            }

            @Override
            public boolean onCancel() {
                NvrRecord.this.finish();
                return true;
            }
        });

        showTipDlg(R.string.cloud_player_wait_buffer, 0, R.string.request_timeout);
    }

    private void onOssPlayEndCache() {
        if (m_play_status != REPLAY_NVR_ACTION.NVR_ACTION_PLAY) return;
        mTipDlg.dismiss();
    }

    private void onReceiverMediaFirstFrame() {
        m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_PLAY;
        mTipDlg.dismiss();
    }

    private void onOssReplayParam() {

    }

    private void onNvrRecord(Message msg) {
        switch (msg.arg1) {
            case R.string.get_nvr_record_failed:
                mTipDlg.dismiss();
                toast(R.string.get_nvr_record_failed);
                break;
            case R.string.nvr_record_replay_failed:
                mTipDlg.dismiss();
                if (msg.arg2 != 0) {
                    toast(ConstantImpl.getOssErrText(msg.arg2));
                } else {
                    toast(R.string.nvr_record_replay_failed);
                }
                break;
        }
    }

    private void onReplaySetPosition(Message msg) {
        double timestamp = (double) msg.obj;
        long time = m_begin_time + (long)timestamp;
        m_time_line.setSliderTimestamp(time);
    }

    private void onReplayDevFile(Message msg) {
        int result = msg.arg1;
        if (result != 0) {
            toast(R.string.front_end_record_replay_failed);
            return;
        }

        TPS_ReplayDevFileRsp data = (TPS_ReplayDevFileRsp) msg.obj;
        switch (data.getnActionType()) {
            case REPLAY_NVR_ACTION.NVR_ACTION_PLAY:
                onActionPlay(data);
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_PAUSE:
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.play);
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_RESUME:
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.pause);
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_FAST:
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_SLOW:
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_SEEK:
                m_seekbar_play.setProgress(m_play_seek_pos);
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_FRAMESKIP:
                break;
            case REPLAY_NVR_ACTION.NVR_ACTION_STOP:
                onActionStop();
                break;
        }
    }

    private void onActionPlay(TPS_ReplayDevFileRsp data) {
        PlayerDevice dev = Global.getDeviceById(m_device_id);//LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
        if (null == dev) return;
        dev.m_replay_duration = data.getnVideoSecs();
        m_seekbar_play.setMax(dev.m_replay_duration);
        final View v = findViewById(R.id.btn_play);
        v.setBackgroundResource(R.drawable.pause);
        findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.pause);
        v.setVisibility(View.GONE);
    }

    private void onActionStop() {
        final View v = findViewById(R.id.btn_play);
        v.setBackgroundResource(R.drawable.play);
        findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.play);
        v.setVisibility(View.VISIBLE);
    }

    private void onGetNvrRecord(int flag, TPS_NotifyInfo ni) {
        if (flag != 0 || null == ni) {
            mTipDlg.dismiss();
            toast(R.string.get_nvr_record_failed);
            return;
        }

        String szInfo = new String(ni.getSzInfo()).trim();
        if (TextUtils.isEmpty(szInfo)) {
            mTipDlg.dismiss();
            toast(R.string.get_nvr_record_not_found);
            return;
        }

        ObjectsRoster<ArchiveRecord> resp = new ObjectsRoster<ArchiveRecord>();
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        Calendar c = Calendar.getInstance();
        try {
            Date d = df.parse(date);
            c.setTime(d);
            m_begin_time = c.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        char t = 'C';
        long first_time = 0;
        ArchiveRecord r = null;
        for (int i = 0; i < 1440; i++) {
            c.add(Calendar.MINUTE, 1);
            int type = -1;
            if (szInfo.charAt(i) == 'A') {
                // 计划录像
                type = 0;
            } else if (szInfo.charAt(i) == 'B') {
                // 移动录像
                type = 1;
            } else if (szInfo.charAt(i) == 'C') {
                // 无录像
                type = 3;
            } else if (szInfo.charAt(i) == 'D') {
                type = 4;
            }

            if (t == szInfo.charAt(i) && (i + 1 < 1440)) continue;
            t = szInfo.charAt(i);

            if (null == r) {
                if (szInfo.charAt(i) == 'C') {
                    first_time = c.getTimeInMillis();
                    r = new ArchiveRecord();
                    r.setDevId(m_device_id);
                    r.setStartTime(c.getTimeInMillis());
                    r.setRecType(String.valueOf(type));
                } else {
                    first_time = c.getTimeInMillis();
                    r = new ArchiveRecord();
                    r.setDevId(m_device_id);
                    r.setStartTime(c.getTimeInMillis());
                    r.setRecType(String.valueOf(type));
                    if (type == 0) {
                        r.setColor(getResources().getColor(R.color.timeline_schedule));
                    } else if (type == 1) {
                        r.setColor(getResources().getColor(R.color.timeline_motion));
                    }
                }
            } else {
                if (szInfo.charAt(i) == 'C') {
                    r.setDuration(c.getTimeInMillis() - r.getStartTime());
                    resp.objectAdd(r, false);
                    r = null;
                } else {
                    r.setDuration(c.getTimeInMillis() - r.getStartTime());
                    r.setColorStartTime(r.getStartTime());
                    r.setColorDuration(r.getDuration());
                    resp.objectAdd(r, false);

                    r = new ArchiveRecord();
                    r.setDevId(m_device_id);
                    r.setStartTime(c.getTimeInMillis());
                    r.setRecType(String.valueOf(type));
                    if (type == 0) {
                        r.setColor(getResources().getColor(R.color.timeline_schedule));
                    } else if (type == 1) {
                        r.setColor(getResources().getColor(R.color.timeline_motion));
                    }
                }
            }
        }

        if (resp.getObjectList().isEmpty()) {
            mTipDlg.dismiss();
            toast(R.string.get_nvr_record_not_found);
            return;
        }

        m_records = resp;
        m_time_line.setRecords(m_records);
        m_time_line.setSliderTimestamp(first_time);
        mTipDlg.dismiss();
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

    private int startReplay(final ArchiveRecord record, final long time) {
        if (null == record) return -1;
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        final String date = df.format(new Date(time));
        Log.d(NvrRecord.class.getName(), "startReplay:record=[" + record + "],pos=[" + date + "]");
        if (m_play_status != REPLAY_NVR_ACTION.NVR_ACTION_STOP) {
            stopReplay();
        }

        Global.acquirePower();
        final PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return -1;

        dev.m_view_id = PLAY_WND_ID;
        dev.m_audio = new AudioPlayer(PLAY_WND_ID);
        dev.m_audio.addRecordCallback(new AudioPlayer.MyRecordCallback() {
            @Override
            public void recvRecordData(byte[] data, int length, int reserver) {
                if (reserver >= 0) {
                    PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(reserver);
                    if (null == dev || null == dev.m_dev) return;
                    LibImpl.getInstance().recvRecordData(data, length, dev.m_dev.getDevId(), reserver);
                }
            }
        });

        dev.m_video = m_glRender;
        View v = dev.m_video.getSurface();
        v.setBackgroundColor(Color.TRANSPARENT);

        if (null != dev.m_video) {
            dev.m_video.mIsStopVideo = false;
        }

        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                mTipDlg.setCallback(null);
                onBtnStop();
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        showTipDlg(R.string.cloud_player_wait_buffer, 15000, R.string.request_timeout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = LibImpl.getInstance().startNvrReplay(m_device_id, PLAY_WND_ID, record, date);
                if (ret != 0) ret = LibImpl.getInstance().startNvrReplay(m_device_id, PLAY_WND_ID, record, date);
                if (ret != 0) {
                    dev.m_view_id = -1;
                    dev.m_voice = false;
                    dev.m_audio = null;
                    dev.m_video = null;

                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_NVR_RECORD;
                    msg.arg1 = R.string.nvr_record_replay_failed;
                    msg.arg2 = ret;
                    m_handler.sendMessage(msg);
                    return;
                }

                dev.m_first_frame = false;
                m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_PLAY;
                TPS_ReplayDevFileRsp rdfr = new TPS_ReplayDevFileRsp();
                rdfr.setnResult(0);
                rdfr.setnActionType(m_play_status);
                rdfr.setnVideoSecs((int)time);
                sendMessage(m_handler, SDK_CONSTANT.TPS_MSG_RSP_REPLAY_DEV_FILE, 0, 0, rdfr);
            }
        }).start();

        return 0;
    }

    private void replaySeek(final ArchiveRecord record, final long time) {
        if (null == record) return;
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        final String date = df.format(new Date(time));
        Log.d(NvrRecord.class.getName(), "replaySeek:record=[" + record + "],pos=[" + date + "]");

        final PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;

        if (m_play_status == REPLAY_NVR_ACTION.NVR_ACTION_PAUSE) {
            replayResume();
        }

        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                mTipDlg.setCallback(null);
                onBtnStop();
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        showTipDlg(R.string.cloud_player_wait_buffer, 15000, R.string.request_timeout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = LibImpl.getInstance().getFuncLib().P2PControlNVRReplay(m_device_id, REPLAY_NVR_ACTION.NVR_ACTION_SEEK, 0, date);
                if (ret != 0) ret = LibImpl.getInstance().getFuncLib().P2PControlNVRReplay(m_device_id, REPLAY_NVR_ACTION.NVR_ACTION_SEEK, 0, date);
                if (ret != 0) {
                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_NVR_RECORD;
                    msg.arg1 = R.string.nvr_record_replay_failed;
                    msg.arg2 = ret;
                    m_handler.sendMessage(msg);
                    return;
                }

                dev.m_first_frame = false;
            }
        }).start();
    }

    private void replayPause() {
        final PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;

        int ret = LibImpl.getInstance().getFuncLib().P2PControlNVRReplay(m_device_id, REPLAY_NVR_ACTION.NVR_ACTION_PAUSE, 0, "");
        if (ret != 0) {
            Message msg = m_handler.obtainMessage();
            msg.what = Define.MSG_NVR_RECORD;
            msg.arg1 = R.string.nvr_record_replay_failed;
            msg.arg2 = ret;
            m_handler.sendMessage(msg);
            return;
        }

        m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_PAUSE;
        TPS_ReplayDevFileRsp rdfr = new TPS_ReplayDevFileRsp();
        rdfr.setnResult(0);
        rdfr.setnActionType(m_play_status);
        sendMessage(m_handler, SDK_CONSTANT.TPS_MSG_RSP_REPLAY_DEV_FILE, 0, 0, rdfr);
    }

    private void replayResume() {
        final PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;

        int ret = LibImpl.getInstance().getFuncLib().P2PControlNVRReplay(m_device_id, REPLAY_NVR_ACTION.NVR_ACTION_RESUME, 0, "");
        if (ret != 0) {
            Message msg = m_handler.obtainMessage();
            msg.what = Define.MSG_NVR_RECORD;
            msg.arg1 = R.string.nvr_record_replay_failed;
            msg.arg2 = ret;
            m_handler.sendMessage(msg);
            return;
        }

        m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_RESUME;
        TPS_ReplayDevFileRsp rdfr = new TPS_ReplayDevFileRsp();
        rdfr.setnResult(0);
        rdfr.setnActionType(m_play_status);
        sendMessage(m_handler, SDK_CONSTANT.TPS_MSG_RSP_REPLAY_DEV_FILE, 0, 0, rdfr);
    }

    public void stopReplay() {
        Log.d(NvrRecord.class.getName(), "stopReplay:m_play_status=[" + m_play_status + "]");
        Global.releasePower();
        if (m_play_status == REPLAY_NVR_ACTION.NVR_ACTION_STOP) return;

        PlayerDevice dev = Global.getDeviceById(m_device_id);//LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
        if (null == dev) return;

        LibImpl.getInstance().stopNvrReplay(dev.m_devId, PLAY_WND_ID);
        m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_STOP;
        TPS_ReplayDevFileRsp rdfr = new TPS_ReplayDevFileRsp();
        rdfr.setnResult(0);
        rdfr.setnActionType(m_play_status);
        sendMessage(m_handler, SDK_CONSTANT.TPS_MSG_RSP_REPLAY_DEV_FILE, 0, 0, rdfr);

        dev.m_view_id = -1;
        dev.m_playing = false;
        dev.m_replay = false;
        dev.m_voice = false;
        if (null != dev.m_audio) {
            dev.m_audio.stopOutAudio();
            dev.m_audio = null;
        }

        if (null != dev.m_video) {
            dev.m_video.mIsStopVideo = true;
            dev.m_video.getSurface().setBackgroundColor(Color.BLACK);
            dev.m_video.resetScaleInfo();    //缩放窗口复原
        }

        m_seekbar_play.setProgress(0);
    }

    private void setReplayPos(final ArchiveRecord record, final long time) {
        if (null == record) return;
        m_play_status = REPLAY_NVR_ACTION.NVR_ACTION_SEEK;
        long offset = time - record.getStartTime();
        final int pos = (int)offset / 1000;
        Log.d(NvrRecord.class.getName(), "setReplayPos:pos=[" + pos + "]");
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
        if (null == dev) return;
        showTipDlg(R.string.cloud_player_wait_buffer, 60000, R.string.request_timeout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = LibImpl.getInstance().setCloudReplayPos(dev.m_devId, pos);
                if (ret != 0) {
                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_CLOUD_END_RECORD;
                    msg.arg1 = R.string.cloud_end_record_replay_failed;
                    msg.arg2 = ret;
                    m_handler.sendMessage(msg);
                    return;
                }

                dev.m_first_frame = false;
            }
        }).start();
    }
}

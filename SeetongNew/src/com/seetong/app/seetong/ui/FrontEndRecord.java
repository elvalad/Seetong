package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
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
import android.util.FloatMath;
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
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.aid.*;
import ipc.android.sdk.com.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class FrontEndRecord extends BaseActivity implements GestureDetector.OnGestureListener, View.OnClickListener, View.OnTouchListener {
    String m_device_id;
    String m_play_file_name = "";
    final int PLAY_WND_ID = 30;
    int m_current_page = 0;
    String m_start_time;
    String m_end_time;
    String m_media_type = "VIDEO";
    TPS_RecordFileResponse m_records = new TPS_RecordFileResponse();
    LinearLayout m_layout;
    OpenglesView m_glView;
    OpenglesRender m_glRender;

    private GestureDetector m_gd;
    private boolean m_is_layout_land = false;

    private PopupWindow m_menu;

    private ProgressDialog mTipDlg;
    SeekBar m_seekbar_play;
    SeekBar m_seekbar_sound;
    SeekBar m_seekbar_h_sound;
    int m_play_seek_pos = 0;
    int m_play_speed = 0;
    int m_play_status = REPLAY_IPC_ACTION.ACTION_STOP;
    int m_record_pos = 0;

    int m_stream_type = 1;
    Date m_find_date = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_end_record);
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);

        PlayerDevice dev = Global.getDeviceById(m_device_id);
        // 确保先停止预览
        dev.m_replay = true;

        m_layout = (LinearLayout) findViewById(R.id.layout_record_file);

        View menu = getLayoutInflater().inflate(R.layout.front_end_record_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_main_stream).setOnClickListener(this);
        menu.findViewById(R.id.btn_sub_stream).setOnClickListener(this);

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

        m_seekbar_play = (SeekBar) findViewById(R.id.seekbar_play);
        m_seekbar_play.setOnSeekBarChangeListener(osbcl);
        m_seekbar_sound = (SeekBar) findViewById(R.id.seekbar_sound);
        m_seekbar_h_sound = (SeekBar) findViewById(R.id.seekbar_h_sound);
        m_seekbar_sound.setOnSeekBarChangeListener(osbcl_sound);
        m_seekbar_h_sound.setOnSeekBarChangeListener(osbcl_sound);

        int maxVolume = Global.m_audioManage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        m_seekbar_sound.setMax(maxVolume);
        m_seekbar_h_sound.setMax(maxVolume);

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

        int mode = Global.m_audioManage.getMode();
        int currentVolume = 0;
        if (AudioManager.MODE_NORMAL == mode) {
            currentVolume = Global.m_audioManage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
        } else if (AudioManager.MODE_IN_CALL == mode) {
            currentVolume = Global.m_audioManage.getStreamVolume(AudioManager.STREAM_VOICE_CALL);  //获取当前值
        }

        m_seekbar_sound.setProgress(currentVolume);
        m_seekbar_h_sound.setProgress(currentVolume);

        setButtonStatus();

        Button btnFinish = (Button) findViewById(R.id.btn_title_right);
        btnFinish.setText(R.string.search);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().addHandler(m_handler);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        String startTime = date + " 00:00:00";
        String endTime = date + " 23:59:59";
        loadData(startTime, endTime, m_stream_type, m_media_type);
    }

    private SeekBar.OnSeekBarChangeListener osbcl = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) return;
            if (m_play_status == REPLAY_IPC_ACTION.ACTION_STOP) {
                m_seekbar_play.setProgress(0);
                return;
            }

            m_play_seek_pos = progress;
            LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_IPC_ACTION.ACTION_SEEK, progress);
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
            if (!fromUser) return;
            Global.m_audioManage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            int currentVolume = Global.m_audioManage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
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
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
        if (null == dev) return;

        MarqueeTextView v = (MarqueeTextView) findViewById(R.id.tvLiveInfo);
        v.setVisibility(View.VISIBLE);

        if (null == msg) {
            v.setVisibility(View.GONE);
            return;
        }

        v.setText(msg);
    }

    public void loadData(String startTime, String endTime, int streamType, String mediaType) {
        m_current_page = 0;
        m_records.m_record_files.clear();
        m_start_time = startTime;
        m_end_time = endTime;
        m_stream_type = streamType;
        m_media_type = mediaType;
        loadData();
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                finish();
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });
        showTipDlg(R.string.get_front_end_record_tip, 20000, R.string.get_front_end_record_timeout);
    }

    private void loadData() {
        String req = "<REQUEST_PARAM\n" +
                "RecordMode=\"ALL\"\n" +
                "StartTime=\"" + m_start_time + "\"\n" +
                "EndTime=\"" + m_end_time + "\"\n" +
                "MediaType=\"" + m_media_type + "\"\n" +
                "StreamIndex=\"" + m_stream_type + "\"\n" +
                "MinSize=\"-1\"\n" +
                "MaxSize=\"-1\"\n" +
                "Page=\"" + m_current_page + "\"\n" + "/>";
        int ret = LibImpl.getInstance().getFuncLib().P2PDevSystemControl(m_device_id, 1021, req);
        if (0 != ret) {
            Message msg = m_handler.obtainMessage();
            msg.what = Define.MSG_FRONT_END_RECORD;
            msg.arg1 = R.string.get_front_end_record_failed;
            m_handler.sendMessage(msg);
            return;
        }
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
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
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
            case R.id.btn_main_stream:
                m_menu.dismiss();
                onBtnMainStream();
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
            default: break;
        }
    }

    private void onBtnMainStream() {
        m_stream_type = 1;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        String startTime = date + " 00:00:00";
        String endTime = date + " 23:59:59";
        onBtnStop();
        m_media_type = "VIDEO";
        loadData(startTime, endTime, m_stream_type, m_media_type);
    }

    private void onBtnSubStream() {
        m_stream_type = 2;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(m_find_date);
        String startTime = date + " 00:00:00";
        String endTime = date + " 23:59:59";
        onBtnStop();
        m_media_type = "VIDEO";
        loadData(startTime, endTime, m_stream_type, m_media_type);
    }

    private void onBtnSoundMin() {
        m_seekbar_sound.setProgress(0);
    }

    private void onBtnSoundMax() {
        m_seekbar_sound.setProgress(m_seekbar_sound.getMax());
    }

    private void onBtnPrev() {
        if (m_records.m_record_files.isEmpty()) {
            toast(R.string.get_front_end_record_not_found);
            return;
        }

        m_record_pos--;
        setButtonStatus();
        setVideoInfo(null);
        startReplay(m_records.m_record_files.get(m_record_pos).m_file_path);
    }

    private void onBtnSlow() {
        if (m_records.m_record_files.isEmpty()) {
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

        LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_IPC_ACTION.ACTION_SLOW, m_play_speed);
    }

    private void onBtnPlay() {
        if (m_records.m_record_files.isEmpty()) {
            toast(R.string.get_front_end_record_not_found);
            return;
        }

        m_play_speed = 0;
        setVideoInfo(null);
        if (m_play_status == REPLAY_IPC_ACTION.ACTION_STOP) {
            findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
            findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.pause);
            startReplay(m_records.m_record_files.get(m_record_pos).m_file_path);
            return;
        }

        if (m_play_status == REPLAY_IPC_ACTION.ACTION_PAUSE) {
            m_play_status = REPLAY_IPC_ACTION.ACTION_RESUME;
        } else {
            m_play_status = REPLAY_IPC_ACTION.ACTION_PAUSE;
        }

        LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, m_play_status, 0);
    }

    private void onBtnStop() {
        if (m_records.m_record_files.isEmpty()) {
            return;
        }

        if (m_play_status != REPLAY_IPC_ACTION.ACTION_STOP) {
            findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
            findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.play);
            //LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_IPC_ACTION.ACTION_STOP, 0);
            m_play_status = REPLAY_IPC_ACTION.ACTION_STOP;
            m_seekbar_play.setProgress(0);
        }

        stopReplay();
    }

    private void onBtnFast() {
        if (m_records.m_record_files.isEmpty()) {
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

        LibImpl.getInstance().getFuncLib().ControlReplay(m_device_id, REPLAY_IPC_ACTION.ACTION_FAST, m_play_speed);
    }

    private void onBtnNext() {
        if (m_records.m_record_files.isEmpty()) {
            toast(R.string.get_front_end_record_not_found);
            return;
        }

        m_record_pos++;
        setButtonStatus();
        startReplay(m_records.m_record_files.get(m_record_pos).m_file_path);
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
                String sdate = df.format(m_find_date);
                String startTime = sdate + " 00:00:00";
                String endTime = sdate + " 23:59:59";
                dlg.dismiss();
                onBtnStop();
                m_media_type = "VIDEO";
                loadData(startTime, endTime, 1, m_media_type);
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
                ((TextView) vi.findViewById(R.id.calendarCenter)).setText(calendar.getYearAndMonth());
            }
        });

        /*calendar.setOnCellTouchListener(new MyCalendar.OnCellTouchListener() {
            @Override
            public void onTouch(Cell cell) {
                int year = calendar.getYear();
                int month = calendar.getMonth();
                int day = cell.getDayOfMonth();

                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                m_find_date = c.getTime();

                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                String date = df.format(m_find_date);
                String startTime = date + " 00:00:00";
                String endTime = date + " 23:59:59";
                dlg.dismiss();
                onBtnStop();
                loadData(startTime, endTime, 1);
            }
        });*/

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
        super.onDestroy();
        LibImpl.getInstance().removeHandler(m_handler);
        PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (dev.m_replay) {
            stopReplay();
        }
        dev.m_replay = false;
        LibImpl.getInstance().m_stop_play = false;
        m_glRender.destory();
    }

    @Override
    public void onBackPressed() {
        mTipDlg.dismiss();
        stopReplay();
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
        // 全屏时切换为横屏显示视频
        //mbIsFullScreen = isFull;
        //配置视频视图间隔
//		float smallInterval = this.getResources().getDimensionPixelSize(R.dimen.small_interval);
//		int width = mbIsFullScreen ? 0 : FunUtils.px2dip(this, smallInterval);
//		findViewById(R.id.Linear_Fourview).setPadding(width, width, width, width);

        int show = isFull ? View.GONE : View.VISIBLE;
        findViewById(R.id.layout_title).setVisibility(show);
        //m_view.findViewById(R.id.hsv_video_ptz).setVisibility(show);
        findViewById(R.id.layout_play_control).setVisibility(show);
        findViewById(R.id.layout_action).setVisibility(show);

        if (!isFull) {
            findViewById(R.id.layout_h_play_control).setVisibility(View.GONE);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case 1021: // 前端录像
                TPS_RecordFileResponse resp = (TPS_RecordFileResponse) msg.obj;
                onGetFrontEndRecord(flag, resp);
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
            case Define.MSG_FRONT_END_RECORD:
                onFrontEndRecord(msg);
                break;
            case Define.MSG_RECEIVER_MEDIA_FIRST_FRAME:
                onReceiverMediaFirstFrame();
                break;
        }
    }

    private void onReceiverMediaFirstFrame() {
        mTipDlg.dismiss();
    }

    private void onFrontEndRecord(Message msg) {
        switch (msg.arg1) {
            case R.string.front_end_record_replay_failed:
                if (msg.arg2 > 0) {
                    toast(ConstantImpl.getTPSErrText(msg.arg2));
                } else {
                    toast(msg.arg1);
                }
                break;
            case R.string.get_front_end_record_failed:
                finish();
                break;
        }
    }

    private void onReplaySetPosition(Message msg) {
        double timestamp = (double) msg.obj;
        int pos = (int) (timestamp * 0.001);
        m_seekbar_play.setProgress(pos);
    }

    private void onReplayDevFile(Message msg) {
        int result = msg.arg1;
        if (result != 0) {
            toast(R.string.front_end_record_replay_failed);
            return;
        }

        TPS_ReplayDevFileRsp data = (TPS_ReplayDevFileRsp) msg.obj;
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
        if (null == dev) return;
        switch (data.getnActionType()) {
            case REPLAY_IPC_ACTION.ACTION_PLAY:
                dev.m_replay_duration = data.getnVideoSecs();
                m_seekbar_play.setMax(dev.m_replay_duration);
                m_seekbar_play.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.pause);
                break;
            case REPLAY_IPC_ACTION.ACTION_PAUSE:
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.play);
                break;
            case REPLAY_IPC_ACTION.ACTION_RESUME:
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.pause);
                break;
            case REPLAY_IPC_ACTION.ACTION_FAST:
                break;
            case REPLAY_IPC_ACTION.ACTION_SLOW:
                break;
            case REPLAY_IPC_ACTION.ACTION_SEEK:
                m_seekbar_play.setProgress(m_play_seek_pos);
                break;
            case REPLAY_IPC_ACTION.ACTION_FRAMESKIP:
                break;
            case REPLAY_IPC_ACTION.ACTION_STOP:
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.play);
                break;
            case REPLAY_IPC_ACTION.ACTION_CAPIMG:
                break;
            case REPLAY_IPC_ACTION.ACTION_CHANGE_SOUND:
                break;
            case REPLAY_IPC_ACTION.ACTION_RECV_DECODEPARAM:
                break;
        }
    }

    class ViewHolder {
        ImageView m_imgIcon;
        TextView m_labName;
    }

    private void onGetFrontEndRecord(int flag, TPS_RecordFileResponse resp) {
        if (flag != 0 || null == resp) {
            m_layout.removeAllViews();
            mTipDlg.dismiss();
            toast(R.string.get_front_end_record_failed);
            return;
        }

        try {
            m_records.m_record_files.addAll(resp.m_record_files);
            int file_count = Integer.parseInt(resp.m_file_count);
            if (resp.m_record_files.size() < file_count) {
                m_current_page++;
                loadData();
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if ("VIDEO".equals(m_media_type)) {
            m_media_type = "AUDIOVIDEO";
            m_current_page = 0;
            loadData();
            return;
        }

        if (m_records.m_record_files.isEmpty()) {
            m_layout.removeAllViews();
            mTipDlg.dismiss();
            toast(R.string.get_front_end_record_not_found);
            return;
        }

        m_layout.removeAllViews();
        mTipDlg.dismiss();
        int index = 0;
        LayoutInflater in = LayoutInflater.from(this);
        for (final TPS_RecordFileResponse.TPS_RecordFile r : m_records.m_record_files) {
            View v = in.inflate(R.layout.front_end_record_item, null);
            ImageView imgIcon = (ImageView) v.findViewById(R.id.img_icon);
            TextView labName = (TextView) v.findViewById(R.id.lab_name);
            imgIcon.setBackgroundResource(R.drawable.video_thumb);
            String name = r.m_file_path.substring(r.m_file_path.lastIndexOf("/") + 1);
            labName.setText(name);
            v.setTag(index);
            index++;

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_record_pos = (int) v.getTag();
                    setButtonStatus();
                    startReplay(r.m_file_path);
                }
            });

            m_layout.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        setButtonStatus();
    }

    private void setButtonStatus() {
        findViewById(R.id.btn_prev).setEnabled(true);
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
        }
    }

    private void startReplay(final String fileName) {
        stopReplay();
        Global.acquirePower();
        final PlayerDevice dev = Global.getDeviceById(m_device_id);
        if (null == dev) return;

        dev.m_view_id = PLAY_WND_ID;
        dev.m_audio = new AudioPlayer(PLAY_WND_ID);
        dev.m_audio.addRecordCallback(new AudioPlayer.MyRecordCallback() {
            @Override
            public void recvRecordData(byte[] data, int length, int reserver) {
                if (reserver >= 0) {
//						mFunclibAgent.recvRecordData(new byte[1024], 1024, "102304", reserver);
                    PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(reserver);
                    if (null == dev || null == dev.m_dev) return;
                    LibImpl.getInstance().recvRecordData(data, length, dev.m_dev.getDevId(), reserver);
                }
            }
        });

        dev.m_video = m_glRender;
        dev.m_online = true;
        dev.m_playing = false;
        dev.m_voice = true;

        View v = dev.m_video.getSurface();
        v.setBackgroundColor(Color.TRANSPARENT);

        m_play_file_name = fileName;

        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
                findViewById(R.id.btn_h_play).setBackgroundResource(R.drawable.play);
                m_play_file_name = "";
                m_play_status = REPLAY_IPC_ACTION.ACTION_STOP;
                mTipDlg.setCallback(null);
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        showTipDlg(R.string.cloud_player_wait_buffer, 60000, R.string.request_timeout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = LibImpl.startReplay(PLAY_WND_ID, dev, fileName);
                if (0 != ret) {
                    dev.m_view_id = -1;
                    dev.m_voice = false;
                    dev.m_audio = null;
                    dev.m_video = null;
                    m_play_file_name = "";

                    Message msg = m_handler.obtainMessage();
                    msg.what = Define.MSG_FRONT_END_RECORD;
                    msg.arg1 = R.string.front_end_record_replay_failed;
                    msg.arg2 = ret;
                    m_handler.sendMessage(msg);
                    return;
                }

                dev.m_first_frame = false;
                m_play_status = REPLAY_IPC_ACTION.ACTION_PLAY;
            }
        }).start();
    }

    public void stopReplay() {
        Global.releasePower();
        m_play_speed = 0;
        setVideoInfo(null);
        m_play_status = REPLAY_IPC_ACTION.ACTION_STOP;
        if ("".equals(m_play_file_name)) return;
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(PLAY_WND_ID);
        if (null == dev) return;
        LibImpl.stopReplay(PLAY_WND_ID, dev, m_play_file_name);

        dev.m_view_id = -1;
        dev.m_playing = false;
        dev.m_replay = false;
        dev.m_voice = false;
        if (null != dev.m_audio) {
            dev.m_audio.stopOutAudio();
            dev.m_audio = null;
        }

        m_glRender.getSurface().setBackgroundColor(Color.BLACK);
        m_glRender.resetScaleInfo();    //缩放窗口复原
        m_seekbar_play.setProgress(0);
    }
}
package com.seetong5.app.seetong.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.FloatMath;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.android.audio.AudioPlayer;
import com.android.opengles.OpenglesRender;
import com.android.opengles.OpenglesView;
import com.android.utils.NetworkUtils;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.MonitorCore;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.aid.ClearEditText;
import com.seetong5.app.seetong.ui.aid.MarqueeTextView;
import com.seetong5.app.seetong.ui.ext.IntegerEditText;
import ipc.android.sdk.com.*;
import ipc.android.sdk.impl.FunclibAgent;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2014-05-12.
 */
public class VideoFragment extends BaseFragment implements GestureDetector.OnGestureListener, View.OnClickListener, View.OnTouchListener {
    private View m_view;
    private final int MAX_VIEW = 4;
    private final int MAX_VIEW_BY_ROW = 2;
    private int m_view_num;
    private int m_view_num_by_row;
    private LinearLayout m_main_layout;
    private LinearLayout m_ptz_layout;
    private LinearLayout m_change_view_layout;
    private LinearLayout m_stream_type_layout;
    private GestureDetector m_gd;
    private Map<String, Integer> m_index_map = new HashMap<>();
    private int m_current_index;
    private Map<Integer, RelativeLayout> m_layout_map = new HashMap<>();
    private Map<Integer, OpenglesRender> m_render_map = new HashMap<>();

    HeadsetPlugReceiver mHeadsetPlugReceiver;

    private int m_ptzSpeed = 5;
    private boolean m_is_layout_land = false;

    private ImageButton m_btnSelectViewNum;

    private PopupWindow m_menu;
    private Button m_btn_change_stream;
    private Button m_btn_change_view;
    private RadioButton m_btn_main_stream;
    private RadioButton m_btn_sub_stream;

    private ImageButton m_btnPlay;
    private ImageButton m_btnVoice;
    private ImageButton m_btnTalk;
    private ImageButton m_btnShot;
    private ImageButton m_btnRecord;
    private ImageButton m_btnPtz;
    private ImageButton m_btnPtzAuto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.m_this.setVideoFragment(this);
        m_view = inflater.inflate(R.layout.video, container);
        //Button btn_video_num = (Button) m_view.findViewById(R.id.btn_title_right);
        //btn_video_num.setOnClickListener(this);

        mHeadsetPlugReceiver = new HeadsetPlugReceiver();
        mHeadsetPlugReceiver.register();

        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);

        m_view.findViewById(R.id.btn_video_num_1).setOnClickListener(this);
        m_view.findViewById(R.id.btn_video_num_4).setOnClickListener(this);
        m_view.findViewById(R.id.btn_video_num_9).setOnClickListener(this);
        m_view.findViewById(R.id.btn_video_num_16).setOnClickListener(this);
        m_btnPlay = (ImageButton) m_view.findViewById(R.id.btn_video_play);
        m_btnPlay.setOnClickListener(this);
        m_btnVoice = (ImageButton) m_view.findViewById(R.id.btn_video_voice);
        m_btnVoice.setOnClickListener(this);
        m_btnTalk = (ImageButton) m_view.findViewById(R.id.btn_video_talk);
        //m_btnTalk.setOnClickListener(this);
        m_btnShot = (ImageButton) m_view.findViewById(R.id.btn_video_shot);
        m_btnShot.setOnClickListener(this);
        m_btnRecord = (ImageButton) m_view.findViewById(R.id.btn_video_record);
        m_btnRecord.setOnClickListener(this);
        m_btnPtz = (ImageButton) m_view.findViewById(R.id.btn_video_ptz);
        m_btnPtz.setOnClickListener(this);

        m_btnTalk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
                    if (null == dev || null == dev.m_audio) return true;
                    startTalk(dev, true);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
                    if (null == dev || null == dev.m_audio) return true;
                    stopTalk(dev, false);
                    startVoice(dev, false);
                    return true;
                }

                return false;
            }
        });

        m_gd = new GestureDetector(m_view.getContext(), this);
        m_gd.setOnDoubleTapListener(new OnDoubleClick());

        m_main_layout = (LinearLayout) m_view.findViewById(R.id.linear_layout_video);
        m_ptz_layout = (LinearLayout) m_view.findViewById(R.id.linear_layout_ptz);

        m_change_view_layout = (LinearLayout) m_view.findViewById(R.id.layout_select_video_num);
        m_stream_type_layout = (LinearLayout) m_view.findViewById(R.id.layout_select_stream_type);

        m_btnPtzAuto = (ImageButton) m_ptz_layout.findViewById(R.id.btn_ptz_auto);

        /*m_ptz_layout.findViewById(R.id.btn_ptz_left_up).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_up).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_right_up).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_left).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_auto).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_right).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_left_down).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_down).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_right_down).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_zoom_in).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_zoom_out).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_focus_in).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_focus_out).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_iris_in).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_iris_out).setOnClickListener(this);*/

        m_ptz_layout.findViewById(R.id.btn_ptz_left_up).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_up).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_right_up).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_left).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_auto).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_right).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_left_down).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_down).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_right_down).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_zoom_in).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_zoom_out).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_focus_in).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_focus_out).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_iris_in).setOnTouchListener(this);
        m_ptz_layout.findViewById(R.id.btn_ptz_iris_out).setOnTouchListener(this);

        m_ptz_layout.findViewById(R.id.btn_set_preset).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_set_preset1).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_call_preset).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_call_preset1).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_del_preset).setOnClickListener(this);
        m_ptz_layout.findViewById(R.id.btn_del_preset1).setOnClickListener(this);

        Button btn_right = (Button) m_view.findViewById(R.id.btn_title_right);
        btn_right.setText(R.string.more);
        btn_right.setVisibility(View.VISIBLE);
        btn_right.setOnClickListener(this);

        View menu = this.getActivity().getLayoutInflater().inflate(R.layout.video_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        m_btn_change_stream = (Button) menu.findViewById(R.id.btn_change_stream);
        m_btn_change_stream.setOnClickListener(this);
        m_btn_change_stream.setEnabled(false);
        m_btn_change_view = (Button) menu.findViewById(R.id.btn_change_view);
        m_btn_change_view.setOnClickListener(this);

        m_btn_main_stream = (RadioButton) m_view.findViewById(R.id.btn_main_stream);
        m_btn_main_stream.setOnClickListener(this);
        m_btn_sub_stream = (RadioButton) m_view.findViewById(R.id.btn_sub_stream);
        m_btn_sub_stream.setOnClickListener(this);

        m_view_num = MAX_VIEW;
        m_view_num_by_row = 2;
        m_current_index = 0;
        initView();

        int resId = R.id.btn_video_num_4;
        if (Config.m_view_num == 1) {
            resId = R.id.btn_video_num_1;
        }else if (Config.m_view_num == 4) {
            resId = R.id.btn_video_num_4;
        } else if (Config.m_view_num == 9) {
            resId = R.id.btn_video_num_9;
        } else if (Config.m_view_num == 16) {
            resId = R.id.btn_video_num_16;
        }

        RadioButton btn = (RadioButton) m_view.findViewById(resId);
        btn.performClick();

        setFocusWnd(m_current_index);
        return m_view;
    }

    /**
     * ###############################耳机插拔广播...begin##############################
     */
    public class HeadsetPlugReceiver extends BroadcastReceiver {
        @SuppressWarnings("unused")
        private static final String TAG = "HeadsetPlugReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("state")) {
                int value = intent.getIntExtra("state", 0);
                //AudioPlayer audioPlayer = (mlstAudioPlayers==null)?null:mlstAudioPlayers.get(mCurLiveIndex);
                if (value == 0) {//耳机已排出
                    AudioPlayer.mIsHSP = false;
                    Log.i("HSP", "videoui recv a hsp broadcast...false");
                } else if (value == 1) {//耳机已插入
                    AudioPlayer.mIsHSP = true;
                    Log.i("HSP", "videoui recv a hsp broadcast...true");
                } else {
                    Log.i("HSP", "videoui recv a hsp broadcast...unknow=" + value);
                }
            }
            Log.i("HSP", "videoui recv a hsp broadcast...");
        }

        public HeadsetPlugReceiver() {
            AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            @SuppressWarnings("deprecation")
            boolean isHeadsetOn = (audioManager != null) && audioManager.isWiredHeadsetOn();
            AudioPlayer.mIsHSP = isHeadsetOn;
            Log.i("HSP", "videoui init a hsp broadcast..." + isHeadsetOn);
        }

        public void register() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            getActivity().registerReceiver(this, intentFilter);
        }

        public void unRegister() {
            getActivity().unregisterReceiver(this);
        }
    }


    /**
     * ###########################手势识别 begin...###########################
     */
    public static final int FLING_MIN_HORIZONTAL = 100; // 横方向滑最少像素
    public static final int FLING_MIN_VELOCITY = 100; // 竖方向滑最少像素
    public static final int FLING_SPEED = 300; // 每秒划动的像素

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
        if (e1 == null || e2 == null) return false;
        float x_dis = e1.getX() - e2.getX();
        float y_dis = e1.getY() - e2.getY();
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || null == dev.m_video) return false;
        boolean isScale = dev.m_video.bitmapScale != 1.0;//处于缩放状态
        if (isScale) return false;

        if (!hasPtz(dev)) return false;

        // 判断视频是否播放，划动速度是否小于300，划动距离是否小于100
        if ((Math.abs(x_dis) < FLING_MIN_HORIZONTAL && Math.abs(y_dis) < FLING_MIN_VELOCITY)) {
            return false;
        }

        if (dev.m_friend_share) {
            toast(R.string.video_share_device_not_use_ptz);
            return false;
        }

        int action = 0;
        if (x_dis > FLING_MIN_HORIZONTAL) {
            // 向左转动
            action = R.id.btn_ptz_left;
        } else if (x_dis < -FLING_MIN_HORIZONTAL) {
            // 向右转动
            action = R.id.btn_ptz_right;
        }

        if (y_dis > FLING_MIN_VELOCITY) {
            // 向上转动
            action = R.id.btn_ptz_up;
        } else if (y_dis < -FLING_MIN_VELOCITY) {
            // 向下转动
            action = R.id.btn_ptz_down;
        }

        Pair<String, String> p = getPtzCmdByResID(action);
        String ptzXml = p.second;
        if (!isNullStr(ptzXml)) {
            LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), ptzXml);
            setTipText(dev.m_dev.getDevId(), p.first);
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || null == dev.m_video) return false;

        boolean isScale = dev.m_video.bitmapScale != 1.0;//处于缩放状态
        if (isScale) return false;

        if (!hasPtz(dev)) {
            toast(R.string.tv_not_support_ptz_control_tip);
            return false;
        }

        return false;
    }

    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            m_btn_change_stream.setEnabled(true);
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
            if (null == dev) {
                m_stream_type_layout.setVisibility(View.GONE);
                m_btn_change_stream.setEnabled(false);
                return false;
            }

            if (dev.m_stream_type == Define.MAIN_STREAM_TYPE) {
                m_btn_main_stream.setChecked(true);
            } else {
                m_btn_sub_stream.setChecked(true);
            }

            if (m_is_layout_land) {
                if (dev.m_playing) {
                    View v = m_view.findViewById(R.id.ll_video_toolbar);
                    v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    m_ptz_layout.setVisibility(View.GONE);
                    return true;
                }
            }

            if (!dev.m_play) {
                MainActivity.m_this.toDevice();
                return true;
            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            maximizeView(m_current_index);
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
            setFocusWnd((Integer) v.getTag());
            m_current_index = (int) v.getTag();
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
            if (null == dev) return m_gd.onTouchEvent(event);
            setButtonsStatus(dev);
            OpenglesRender render = dev.m_video;
            if (null == render) return m_gd.onTouchEvent(event);
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

                    if (hasPtz(dev)) {
                        String ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_STOP).toXMLString();
                        LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), ptzXml);
                        postDelayedVideoInfoMsg(m_current_index);
                        dev.m_ptz_auto = false;
                        setButtonsStatus(dev);
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

    private void initView() {
        LayoutInflater li = LayoutInflater.from(m_view.getContext());
        LinearLayout row = new LinearLayout(m_main_layout.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < m_view_num; i++) {
            RelativeLayout layout = m_layout_map.get(i);
            if (null == layout) {
                layout = (RelativeLayout) li.inflate(R.layout.video_item, null);
                layout.setTag(i);
                layout.setOnClickListener(this);
                m_layout_map.put(i, layout);
            }

            Button btnPerview = (Button) layout.findViewById(R.id.btn_live_preview);
            btnPerview.setTag(i);
            btnPerview.setOnClickListener(this);

            OpenglesView glView = (OpenglesView) layout.findViewById(R.id.livePreview);
            OpenglesRender glRender = m_render_map.get(i);
            if (null == glRender) {
                glRender = new OpenglesRender(glView, i);
                glRender.setVideoMode(OpenglesRender.VIDEO_MODE_CUSTOM);// VIDEO_MODE_FIT
                glView.setRenderer(glRender);
                glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                glView.setTag(i);
                glView.setOnClickListener(this);

                glView.setLongClickable(true);
                glView.setOnTouchListener(new TouchListener());

                glRender.addCheckCallback(new OpenglesRender.CheckCallback() {
                    @Override
                    public void recvDataTimout(int branch, OpenglesRender render) {
                        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(branch);
                        if (render == null) {
                            if (T(R.string.tv_video_play_tip).compareToIgnoreCase(dev.m_tipInfo) != 0) {
                                setVideoInfo(branch, T(R.string.tv_video_play_tip));
                            }
                            return;
                        }

                        setVideoInfo(branch, T(R.string.tv_video_wait_video_stream_tip));//超时提醒
                    }
                });

                m_render_map.put(i, glRender);
                //改变设备语言后Activity被重新创建，重建之前播放设备的render
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(i);
                if (null != dev) dev.m_video = glRender;
            }

            row.addView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            if ((i + 1) % m_view_num_by_row != 0) continue;
            m_main_layout.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            row = new LinearLayout(m_main_layout.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    private void changeView() {
        int rowNum = m_view_num / m_view_num_by_row;
        for (int i = 0; i < MAX_VIEW_BY_ROW; i++) {
            //RelativeLayout layout = m_layout_map.get(i);
            //OpenglesView glView = (OpenglesView) layout.findViewById(R.id.livePreview);

            LinearLayout ll = (LinearLayout) m_main_layout.getChildAt(i);
            ll.setVisibility(i >= rowNum ? View.GONE : View.VISIBLE);
            for (int j = 0; j < MAX_VIEW_BY_ROW; j++) {
                int index = i * MAX_VIEW_BY_ROW + j;
                ll.getChildAt(j).setVisibility(i < rowNum && j < m_view_num_by_row ? View.VISIBLE : View.GONE);
                m_render_map.get(index).getSurface().setVisibility(View.VISIBLE);
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(index);
                if (null == dev || null == dev.m_video) continue;
                dev.m_video.getSurface().setVisibility(i < rowNum && j < m_view_num_by_row ? View.VISIBLE : View.GONE);
                if (i < rowNum && j < m_view_num_by_row) {
                    if (dev.m_user_stop) continue;
                    startPlay(index, dev);
                } else {
                    stopPlay(dev, false, false);
                }
            }
        }

        // set current view maximize is false
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null != dev) dev.m_maximize = false;
    }

    private boolean m_full_view = false;
    private void maximizeView(int index) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(index);
        if (null == dev) return;
        if (dev.m_maximize) {
            for (int i = 0; i < MAX_VIEW; i++) {
                m_render_map.get(i).getSurface().setVisibility(View.VISIBLE);
            }

            changeView();
            m_full_view = false;
        } else {
            m_full_view = true;
            dev.m_maximize = true;
            // hide all
            int maxRowNum = MAX_VIEW / MAX_VIEW_BY_ROW;
            for (int i = 0; i < maxRowNum; i++) {
                m_main_layout.getChildAt(i).setVisibility(View.GONE);
            }

            for (int i = 0; i < MAX_VIEW; i++) {
                if (index == i) continue;
                m_render_map.get(i).getSurface().setVisibility(View.GONE);
            }

            // show current row
            int row = index / MAX_VIEW_BY_ROW;
            m_main_layout.getChildAt(row).setVisibility(View.VISIBLE);
            int start = row * MAX_VIEW_BY_ROW;
            int end = (row + 1) * MAX_VIEW_BY_ROW;

            // show current view
            for (int i = start; i < end; i++) {
                if (index == i) {
                    dev = LibImpl.getInstance().getPlayerDevice(i);
                    if (null == dev || null == dev.m_video) continue;
                    dev.m_video.getSurface().setVisibility(View.VISIBLE);
                    m_layout_map.get(i).setVisibility(View.VISIBLE);
                } else {
                    m_layout_map.get(i).setVisibility(View.GONE);
                }
            }
        }
    }

    private void showAlarmIcon(String devId, boolean show) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devId);
        if (null == dev || !dev.m_playing) return;

        if (dev.m_view_id < 0) return;
        RelativeLayout layout = m_layout_map.get(dev.m_view_id);
        if (null == layout) return;
        ImageView iv = (ImageView) layout.findViewById(R.id.imgAlarm);
        iv.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        AnimationDrawable anim = (AnimationDrawable) iv.getBackground();
        if (null == anim) return;
        if (show) {
            anim.stop();
            anim.start();
        } else {
            anim.stop();
        }
    }

    private void showRecordIcon(String devId, boolean show) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devId);
        if (null == dev || !dev.m_playing) return;

        if (dev.m_view_id < 0) return;
        RelativeLayout layout = m_layout_map.get(dev.m_view_id);
        ImageView iv = (ImageView) layout.findViewById(R.id.imgRecord);
        iv.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void startFullScreen(boolean isFull) {
        // 全屏时切换为横屏显示视频
        //mbIsFullScreen = isFull;
        //配置视频视图间隔
//		float smallInterval = this.getResources().getDimensionPixelSize(R.dimen.small_interval);
//		int width = mbIsFullScreen ? 0 : FunUtils.px2dip(this, smallInterval);
//		findViewById(R.id.Linear_Fourview).setPadding(width, width, width, width);

        int show = isFull ? View.GONE : View.VISIBLE;
        m_view.findViewById(R.id.layout_title).setVisibility(show);
        //m_view.findViewById(R.id.hsv_video_ptz).setVisibility(show);
        m_view.findViewById(R.id.ll_video_toolbar).setVisibility(show);
        m_change_view_layout.setVisibility(View.GONE);
        m_stream_type_layout.setVisibility(View.GONE);
        m_ptz_layout.setVisibility(View.GONE);
        if (m_menu.isShowing()) m_menu.dismiss();
        MainActivity.m_this.setTabWidgetVisible(!isFull);
    }

    public void setFocusWnd(int index) {
        if (index < 0) return;
        m_layout_map.get(m_current_index).setBackgroundColor(getResources().getColor(R.color.video_view_normal_border));
        m_current_index = index;
        m_layout_map.get(m_current_index).setBackgroundColor(getResources().getColor(R.color.video_view_focus_border));
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        setButtonsStatus(dev);
    }

    public List<String> getPlayDeviceIds() {
        ArrayList<String> lst = new ArrayList<>();
        for (int i = 0; i < MAX_VIEW; i++) {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(i);
            if (null == dev) continue;
            lst.add(dev.m_devId);
        }

        return lst;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mHeadsetPlugReceiver != null) {
            mHeadsetPlugReceiver.unRegister();
            mHeadsetPlugReceiver = null;
        }

        stopAll();
        super.onDestroy();
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onBtnClick(v);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
            if (null == dev) return false;
            if (!hasPtz(dev)) return false;

            String ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_STOP).toXMLString();
            LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), ptzXml);
            postDelayedVideoInfoMsg(m_current_index);
            dev.m_ptz_auto = false;
            setButtonsStatus(dev);
        }

        return false;
    }

    private void onBtnClick(View v) {
        onPtzControl(v);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_right:
                onBtnTitleRight(v);
                break;
            case R.id.btn_change_stream:
                m_menu.dismiss();
                onBtnChangeStream(v);
                break;
            case R.id.btn_change_view:
                m_menu.dismiss();
                onBtnChangeView(v);
                break;
            case R.id.btn_main_stream:
                onBtnMainStream(v);
                m_stream_type_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_sub_stream:
                onBtnSubStream(v);
                m_stream_type_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_video_num_1:
                m_view_num = 1;
                m_view_num_by_row = 1;
                changeView();
                m_change_view_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_video_num_4:
                m_view_num = 4;
                m_view_num_by_row = 2;
                changeView();
                m_change_view_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_video_num_9:
                m_view_num = 9;
                m_view_num_by_row = 3;
                changeView();
                m_change_view_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_video_num_16:
                m_view_num = 16;
                m_view_num_by_row = 4;
                changeView();
                m_change_view_layout.setVisibility(View.GONE);
                break;
            case R.id.video_item:
            case R.id.livePreview:
                break;
            case R.id.btn_live_preview:
                setFocusWnd((Integer) v.getTag());
                m_current_index = (int) v.getTag();
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
                if (null != dev && dev.m_play) return;
                MainActivity.m_this.toDevice();
                break;
            case R.id.btn_video_play:
                onBtnVideoPlay();
                break;
            case R.id.btn_video_voice:
                onBtnVideoVoice();
                break;
            case R.id.btn_video_talk:
                onBtnVideoTalk();
                break;
            case R.id.btn_video_shot:
                onBtnVideoShot();
                break;
            case R.id.btn_video_record:
                onBtnVideoRecord();
                break;
            case R.id.btn_video_ptz:
                onBtnVideoPtz();
                break;
            case R.id.btn_ptz_left_up:
                //onPtzControl(PTZ_CMD_TYPE.UP_LEFT);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_up:
                //onPtzControl(PTZ_CMD_TYPE.TILT_UP);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_right_up:
                //onPtzControl(PTZ_CMD_TYPE.UP_RIGHT);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_left:
                //onPtzControl(PTZ_CMD_TYPE.PAN_LEFT);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_auto:
                //onPtzControl(PTZ_CMD_TYPE.PAN_AUTO);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_right:
                //onPtzControl(PTZ_CMD_TYPE.PAN_RIGHT);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_left_down:
                //onPtzControl(PTZ_CMD_TYPE.DOWN_LEFT);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_down:
                //onPtzControl(PTZ_CMD_TYPE.TILT_DOWN);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_right_down:
                //onPtzControl(PTZ_CMD_TYPE.DOWN_RIGHT);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_zoom_in:
                //onPtzControl(PTZ_CMD_TYPE.ZOOM_IN_VALUE);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_zoom_out:
                //onPtzControl(PTZ_CMD_TYPE.ZOOM_OUT_VALUE);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_focus_in:
                //onPtzControl(PTZ_CMD_TYPE.FOCUS_NEAR);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_focus_out:
                //onPtzControl(PTZ_CMD_TYPE.FOCUS_FAR);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_iris_in:
                //onPtzControl(PTZ_CMD_TYPE.IRIS_OPEN);
                onPtzControl(v);
                break;
            case R.id.btn_ptz_iris_out:
                //onPtzControl(PTZ_CMD_TYPE.IRIS_CLOSE);
                onPtzControl(v);
                break;
            case R.id.btn_set_preset:
            case R.id.btn_set_preset1:
                onBtnSetPreset(v);
                break;
            case R.id.btn_call_preset:
            case R.id.btn_call_preset1:
                onBtnCallPreset(v);
                break;
            case R.id.btn_del_preset:
            case R.id.btn_del_preset1:
                onBtnDelPreset(v);
                break;
        }
    }

    private void onBtnSubStream(View v) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (Define.SUB_STREAM_TYPE == dev.m_stream_type) return;
        stopPlay(dev, false, false);
        dev.m_stream_type = Define.SUB_STREAM_TYPE;
        startPlay(m_current_index, dev);
    }

    private void onBtnMainStream(View v) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (Define.MAIN_STREAM_TYPE == dev.m_stream_type) return;
        stopPlay(dev, false, false);
        dev.m_stream_type = Define.MAIN_STREAM_TYPE;
        startPlay(m_current_index, dev);
    }

    private void onBtnChangeView(View v) {
        m_change_view_layout.setVisibility(m_change_view_layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void onBtnChangeStream(View v) {
        View vv = m_view.findViewById(R.id.layout_select_stream_type);
        vv.setVisibility(vv.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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
            m_menu.setBackgroundDrawable(new BitmapDrawable(null, (Bitmap) null));
            m_menu.setOutsideTouchable(true);
        }
    }

    public void setButtonsStatus(PlayerDevice dev) {
        if (null == dev) {
            m_btnPlay.setImageResource(R.drawable.n_video_btm_play);
            m_btnVoice.setImageResource(R.drawable.n_video_btm_voice);
            m_btnTalk.setImageResource(R.drawable.n_video_btm_talk);
            m_btnRecord.setImageResource(R.drawable.n_video_btm_local_video);
            m_btnPtzAuto.setTag(PTZ_AUTO_STOP);
            m_btnPtzAuto.setImageResource(R.drawable.viewpic_ebtn_start);
            return;
        }

        m_btnPlay.setImageResource(dev.m_play ? R.drawable.n_video_btm_play_dd : R.drawable.n_video_btm_play);
        m_btnVoice.setImageResource(dev.m_voice ? R.drawable.n_video_btm_voice_dd : R.drawable.n_video_btm_voice);
        m_btnTalk.setImageResource(dev.m_talk ? R.drawable.n_video_btm_talk_dd : R.drawable.n_video_btm_talk);
        m_btnRecord.setImageResource(dev.m_record ? R.drawable.n_video_btm_local_video_dd : R.drawable.n_video_btm_local_video);

        if (dev.m_ptz_auto) {
            m_btnPtzAuto.setTag(PTZ_AUTO_START);
            m_btnPtzAuto.setImageResource(R.drawable.viewpic_ebtn_stop);
        } else {
            m_btnPtzAuto.setTag(PTZ_AUTO_STOP);
            m_btnPtzAuto.setImageResource(R.drawable.viewpic_ebtn_start);
        }
    }

    public void addDeviceToView(PlayerDevice dev) {
        int index = m_current_index;
        if (null == dev) return;
        if (dev.m_play) {
            return;
            // 自动寻找下一个没有设备的窗口
            /*for (int i = 0; i < m_view_num; i++) {
                dev = MonitorCore.instance().getPlayerDevice(i);
                if (null != dev && dev.m_playing) continue;
                index = i;
                break;
            }*/
        }

        RelativeLayout layout = m_layout_map.get(index);
        if (null == layout) return;

        dev.m_audio = new AudioPlayer(m_current_index);
        dev.m_video = m_render_map.get(m_current_index);
        dev.m_online = true;
        dev.m_playing = false;

        MonitorCore.instance().setPlayerDevice(index, dev);
        m_index_map.put(dev.m_entry.getIpc_sn(), m_current_index);
        startPlay(index, dev);

        View v = layout.findViewById(R.id.btn_live_preview);
        v.setVisibility(View.GONE);
        v = dev.m_video.getSurface();
        v.setBackgroundColor(Color.TRANSPARENT);
    }

    public boolean startPlay(int index, PlayerDevice dev) {
        if (null == dev) return false;
        if (dev.m_play) return true;
        RelativeLayout layout = m_layout_map.get(index);
        if (null == layout) return false;

        dev.m_video = m_render_map.get(index);
        dev.m_video.mIsStopVideo = false;
        int ret = LibImpl.startPlay(index, dev, dev.m_stream_type, dev.m_frame_type);
        if (ret == 0) {
            dev.m_online = true;
            dev.m_playing = false;
            setVideoInfo(index, T(R.string.tv_video_req_tip));
        } else {
            String selfID = "";
            if (LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(dev.m_dev.getDevId())) != null) {
                selfID = LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(dev.m_dev.getDevId())).getNotifyStr();
            }

            Log.i("DeviceNotifyInfo", "DeviceNotifyInfo ary:" + LibImpl.mDeviceNotifyInfo + ".");
            selfID = (isNullStr(selfID)) ? "" : ("(" + selfID + ")");
            setVideoInfo(index, ConstantImpl.getTPSErrText(ret, false) + selfID);
            toast(ConstantImpl.getTPSErrText(ret, false) + selfID);
            return false;
        }

        dev.m_play = true;
        dev.m_user_stop = false;
        dev.m_view_id = index;

        View v = layout.findViewById(R.id.btn_live_preview);
        v.setVisibility(View.GONE);
        v = layout.findViewById(R.id.tvLiveInfo);
        v.setVisibility(View.VISIBLE);
        v = dev.m_video.getSurface();
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setVisibility(View.VISIBLE);

        CheckDefaultUserPwd(dev);

        m_btn_change_stream.setEnabled(true);
        if (dev.m_stream_type == Define.MAIN_STREAM_TYPE) {
            m_btn_main_stream.setChecked(true);
        } else {
            m_btn_sub_stream.setChecked(true);
        }

        setButtonsStatus(dev);
        return true;
    }

    public void realPlay(int login_id) {
        Integer index = MonitorCore.instance().getIndexByLoginId(login_id);
        if (null == index) return;
        int ret = MonitorCore.instance().realPlay(index, login_id);
        if (ret <= 0) {
            toast(R.string.play_device_failed);
        }
    }

    public void stopAndResetPlay(PlayerDevice dev) {
        if (m_full_view) changeView();
        stopPlay(dev, true, true);
    }

    public void stopPlay(PlayerDevice dev, boolean resetView, boolean remove) {
        if (null == dev) return;
        //if (!dev.m_play) return;
        final String devId = dev.m_dev.getDevId();
        showAlarmIcon(devId, false);
        showRecordIcon(devId, false);

        stopVoice(dev, false);
        stopTalk(dev, false);
        stopRecord(dev, false);

        setVideoInfo(dev.m_view_id, T(R.string.tv_video_stop_tip));
        int view_id = dev.m_view_id;
        LibImpl.stopPlay(dev.m_view_id, dev);

        if (null != dev.m_video) {
            dev.m_video.mIsStopVideo = true;
            dev.m_video.getSurface().setBackgroundColor(Color.BLACK);//setAlpha(0);
            dev.m_video.resetScaleInfo();    //缩放窗口复原
        }

        if (resetView) {
            RelativeLayout layout = m_layout_map.get(dev.m_view_id);
            if (null == layout) layout = m_layout_map.get(dev.m_last_view_id);
            if (null != layout) {
                View v = layout.findViewById(R.id.livePreview);
                v.setBackgroundColor(Color.WHITE);
                v = layout.findViewById(R.id.tvLiveInfo);
                v.setVisibility(View.GONE);
                v = layout.findViewById(R.id.tvMsgInfo);
                v.setVisibility(View.GONE);
                v = layout.findViewById(R.id.btn_live_preview);
                v.setVisibility(View.VISIBLE);
                layout.invalidate();
            }
        }

        setButtonsStatus(dev);
        if (remove) {
            LibImpl.getInstance().removePlayerDevice(view_id);
            dev.m_view_id = -1;
        }

        dev.m_video = null;
    }

    private void startVoice(PlayerDevice dev, boolean showToast) {
        if (null == dev) return;
        if (null == dev.m_add_watch_rsp || !dev.m_add_watch_rsp.hasAudio()) {
            if (showToast) toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }
        stopAllVoice();
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        dev.m_audio.startOutAudio();
        dev.m_voice = true;
        setButtonsStatus(dev);
        if (showToast) toast(R.string.fvu_tip_open_voice);
    }

    private void stopVoice(PlayerDevice dev, boolean showToast) {
        if (null == dev) return;
        if (!dev.m_voice) return;
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        dev.m_audio.stopOutAudio();
        dev.m_voice = false;
        setButtonsStatus(dev);
        if (showToast) toast(R.string.fvu_tip_close_voice);
    }

    private void startTalk(PlayerDevice dev, boolean showToast) {

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        stopAllVoice();
        stopAllTalk();

        if (null == dev || !dev.m_playing) {
            if (showToast) toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) {
            if (showToast) toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!rsp.hasAudio()) {
            if (showToast) toast(R.string.tv_talk_fail_invalid_audio_device);
            return;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            if (showToast) toast(R.string.tv_talk_fail_illegal_format_tip);
            return;
        }

        if (dev.m_talk) return;

        int ret = LibImpl.getInstance().getFuncLib().StartTalkAgent(dev.m_dev.getDevId());
        if (0 != ret) {
            if (showToast) toast(R.string.tv_talk_fail_tip);
                return;
        }

        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        dev.m_audio.startTalk();
        dev.m_talk = true;
        setButtonsStatus(dev);
    }

    private void stopTalk(PlayerDevice dev, boolean showToast) {
        if (null == dev) return;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        LibImpl.getInstance().getFuncLib().StopTalkAgent(dev.m_dev.getDevId());
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        if (!dev.m_talk) return;
        dev.m_audio.stopTalk();
        dev.m_talk = false;
        setButtonsStatus(dev);
    }

    private void startRecord(PlayerDevice dev, boolean showToast) {
        if (null == dev) return;
        String filePath = Global.getVideoDir() + "/" + dev.m_dev.getDevId();
        File dir = new File(filePath);
        if (!(dir.exists())) dir.mkdirs();
        int ret = LibImpl.getInstance().getFuncLib().StartRecordAgent(dev.m_dev.getDevId(), filePath, EtcInfo.PER_RECORD_TIME_LENGTH);
        if (0 != ret) {
            toast(R.string.fvu_tip_start_record_failed);
            return;
        }

        dev.m_record = true;
        setButtonsStatus(dev);
        if (showToast) toast(R.string.fvu_tip_open_record_video);
    }

    private void stopRecord(PlayerDevice dev, boolean showToast) {
        if (null == dev) return;
        LibImpl.getInstance().getFuncLib().StopRecordAgent(dev.m_dev.getDevId());
        dev.m_record = false;
        setButtonsStatus(dev);
        if (showToast) toast(R.string.fvu_tip_close_record_video);
    }

    public void stopAll() {
        for (int i = 0; i < MAX_VIEW; i++) {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(i);
            if (null == dev) continue;
            stopPlay(dev, true, false);
            if (null != dev.m_video) {
                dev.m_video.destory();
                dev.m_video = null;
            }

            stopVoice(dev, false);
            stopTalk(dev, false);
        }
    }

    private void stopAllVoice() {
        for (int i = 0; i < MAX_VIEW; i++) {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(i);
            if (null == dev || null == dev.m_audio) continue;
            dev.m_audio.stopOutAudio();
            dev.m_voice = false;
        }
    }

    private void stopAllTalk() {
        for (int i = 0; i < MAX_VIEW; i++) {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(i);
            if (null == dev || null == dev.m_audio) continue;
            LibImpl.getInstance().getFuncLib().StopTalkAgent(dev.m_dev.getDevId());
            dev.m_audio.stopTalk();
            dev.m_talk = false;
        }
    }

    //------------云台自动巡航
    private final String PTZ_AUTO_START = "ptz_auto_start";
    private final String PTZ_AUTO_STOP = "ptz_auto_stop";

    public boolean onStatusEvent(int lUser, int nStateCode, String response) {
        PlayerDevice dev = MonitorCore.instance().getPlayerDeviceByLoginId(lUser);
        if (null == dev) return true;
        switch (nStateCode) {
            case NetSatateEvent.EVENT_CONNECTOK:
                break;
            case NetSatateEvent.EVENT_CONNECTFAILED:
                dev.m_play = false;
                toast(R.string.connect_device_failed);
                break;
            case NetSatateEvent.EVENT_LOGINOK:
                realPlay(lUser);
                break;
            case NetSatateEvent.EVENT_LOGINFAILED:
                dev.m_play = false;
                toast(R.string.login_device_failed);
                break;
        }

        return true;
    }

    private void onBtnVideoPlay() {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) {
            toast(R.string.before_open_video_preview);
            return;
        }

        if (dev.m_play) {
            dev.m_user_stop = true;
            stopPlay(dev, false, false);
        } else {
            startPlay(m_current_index, dev);
        }
    }

    private void onBtnVideoVoice() {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!rsp.hasAudio()) {
            toast(R.string.fvu_tip_open_voice_fail_invalid_audio_device);
            return;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.fvu_tip_open_voice_fail_illegal_format_tip);
            return;
        }

        if (dev.m_voice) {
            stopVoice(dev, true);
        } else {
            startVoice(dev, true);
        }
    }

    private void onBtnVideoTalk() {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!rsp.hasAudio()) {
            toast(R.string.tv_talk_fail_invalid_audio_device);
            return;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.tv_talk_fail_illegal_format_tip);
            return;
        }

        if (dev.m_talk) {
            stopTalk(dev, true);
        } else {
            startTalk(dev, true);
        }
    }

    private void onBtnVideoShot() {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        String strDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String fileName = Global.getImageDir() + "/" + dev.m_dev.getDevId() + "_" + strDate + ".jpg";
        boolean isShotOK = dev.m_video.startShot(fileName);
        if (isShotOK) {
            toast(R.string.snapshot_succeed);
        } else {
            toast(R.string.snapshot_failed);
        }
    }

    private void onBtnVideoRecord() {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (dev.m_record) {
            showRecordIcon(dev.m_devId, false);
            stopRecord(dev, true);
        } else {
            startRecord(dev, true);
            showRecordIcon(dev.m_devId, true);
        }
    }

    private void onBtnVideoPtz() {
        if (m_ptz_layout.isShown()) {
            m_ptz_layout.setVisibility(View.GONE);
            return;
        }

        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev || !dev.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        if (dev.m_friend_share) {
            toast(R.string.video_share_device_not_use_ptz);
            return;
        }

        TPS_AddWachtRsp rsp = dev.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!dev.is_ptz_control()) {
            toast(R.string.tv_not_support_ptz_control_tip);
            return;
        }

        m_ptz_layout.setVisibility(m_ptz_layout.isShown() ? View.GONE : View.VISIBLE);
    }

    private void onPtzControl(int ptzCmd) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;

        LibImpl.ptzControl(dev.m_dev.getDevId(), ptzCmd, m_ptzSpeed, m_ptzSpeed);
        if (ptzCmd != PTZ_CMD_TYPE.PAN_AUTO) return;
        dev.m_ptz_auto = !dev.m_ptz_auto;
        setButtonsStatus(dev);
    }

    private void onPtzControl(View v) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (!hasPtz(dev)) return;
        Pair<String, String> pair;
        if (v.getId() == R.id.btn_ptz_auto) {
            String ptzXml = "";
            String ptzMsg = "";
            if (dev.m_ptz_auto) {
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_STOP, m_ptzSpeed, m_ptzSpeed, false).toXMLString();
            } else {
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_AUTO, m_ptzSpeed, m_ptzSpeed, false).toXMLString();
            }

            pair = new Pair<>(ptzMsg, ptzXml);
            dev.m_ptz_auto = !dev.m_ptz_auto;
            setButtonsStatus(dev);
        } else {
            pair = getPtzCmdByResID(v.getId());
        }

        if (!isNullStr(pair.second)) {
            LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), pair.second);
        }

        if (!isNullStr(pair.first)) {
            setTipText(dev.m_dev.getDevId(), pair.first);
        }
    }

    private boolean hasPtz(PlayerDevice dev) {
        // 设备支持云台且在播放中才显示云台文本提示
        if (null == dev || null == dev.m_dev || !dev.m_playing) return false;
        //if (dev.m_dev.getWithPTZ() == 1) return true;
        return dev.is_ptz_control();
    }

    public Pair<String, String> getPtzCmdByResID(int resID) {
        String ptzXml = null;
        String ptzMsg = null;
        switch (resID) {
            // 云台操作
            case R.id.btn_ptz_auto:
                break;
            case R.id.btn_ptz_left:
                ptzMsg = T(R.string.tv_ptz_left);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_LEFT, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_right:
                ptzMsg = T(R.string.tv_ptz_right);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_RIGHT, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_up:
                ptzMsg = T(R.string.tv_ptz_up);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_UP, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_down:
                ptzMsg = T(R.string.tv_ptz_down);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_DOWN, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_left_up:
                ptzMsg = T(R.string.tv_ptz_leftup);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_LEFT_UP, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_left_down:
                ptzMsg = T(R.string.tv_ptz_leftdown);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_LEFT_DOWN, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_right_up:
                ptzMsg = T(R.string.tv_ptz_rightup);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_RIGHT_UP, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            case R.id.btn_ptz_right_down:
                ptzMsg = T(R.string.tv_ptz_rightdown);
                ptzXml = new TPS_PtzInfo(SDK_CONSTANT.PTZ_RIGHT_DOWN, m_ptzSpeed, m_ptzSpeed).toXMLString();
                break;
            //----------------------
            case R.id.btn_ptz_zoom_out:
                //setScale(-0.25f);
                ptzMsg = T(R.string.tv_zoom_out);
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_ZOOMDEC).toXMLString();
                break;
            case R.id.btn_ptz_zoom_in:
                //setScale(0.25f);
                ptzMsg = T(R.string.tv_zoom_in);
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_ZOOMADD).toXMLString();
                break;
            case R.id.btn_ptz_focus_out:
                //setStartX(-50);
                ptzMsg = T(R.string.tv_focus_out);
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_FOCUSDEC).toXMLString();
                break;
            case R.id.btn_ptz_focus_in:
                //setStartX(50);
                ptzMsg = T(R.string.tv_focus_in);
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_FOCUSADD).toXMLString();
                break;
            case R.id.btn_ptz_iris_out:
                //setStartY(-50);
                ptzMsg = T(R.string.tv_iris_out);
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_IRISDEC).toXMLString();
                break;
            case R.id.btn_ptz_iris_in:
                //setStartY(50);
                ptzMsg = T(R.string.tv_iris_in);
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_IRISADD).toXMLString();
                break;
            case R.id.btn_set_preset:
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_SETPOINT).toXMLString();
                break;
            case R.id.btn_call_preset:
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_GOTOPOINT).toXMLString();
                break;
            case R.id.btn_get_preset:
            case R.id.btn_get_preset1:
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_GETPOINT).toXMLString();
                break;
            case R.id.btn_del_preset:
            case R.id.btn_del_preset1:
                ptzXml = new TPS_PtzInfoBase(SDK_CONSTANT.PTZ_CLEARPOINT).toXMLString();
                break;
            default:
                break;
        }

        return new Pair<String, String>(ptzMsg, ptzXml);
    }

    private void onBtnGetPreset(View v) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        //if (!hasPtz(dev)) return;
        String ptzXml = getPtzCmdByResID(v.getId()).second;
        if (isNullStr(ptzXml)) return;
        int ret = LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), ptzXml);
        if (ret != 0) {
            toast(ConstantImpl.getTPSErrText(ret));
            return;
        }
    }

    private void onBtnCallPreset(View v) {
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (!hasPtz(dev)) return;
        final Context ctx = this.getActivity();
        final IntegerEditText etPreset = new IntegerEditText(ctx);
        etPreset.setHint(R.string.hit_input_preset);
        etPreset.setPadding(10, 10, 10, 10);
        etPreset.setSingleLine(true);
        etPreset.setText("");
        etPreset.setRange(1, 255);
        new AlertDialog.Builder(ctx).setTitle(R.string.tv_call_preset)
                .setView(etPreset)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        MainActivity.m_this.hideInputPanel(etPreset);
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!etPreset.validate()) {
                    etPreset.setShakeAnimation();
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                String value = etPreset.getText().toString();
                MainActivity.m_this.hideInputPanel(etPreset);
                String xml = "<xml><cmd>callpreset</cmd><preset>" + value + "</preset>" + "</xml>";
                int ret = LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), xml);
                if (ret != 0) {
                    toast(ConstantImpl.getTPSErrText(ret));
                    return;
                }

                toast(R.string.call_preset_succeed);
                dialog.dismiss();
            }
        }).create().show();
    }

    private void onBtnDelPreset2(View v) {
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (!hasPtz(dev)) return;
        final Context ctx = this.getActivity();
        final IntegerEditText etPreset = new IntegerEditText(ctx);
        etPreset.setHint(R.string.hit_input_preset);
        etPreset.setPadding(10, 10, 10, 10);
        etPreset.setSingleLine(true);
        etPreset.setText("");
        etPreset.setRange(1, 255);
        new AlertDialog.Builder(ctx).setTitle(R.string.tv_del_preset)
                .setView(etPreset)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        MainActivity.m_this.hideInputPanel(etPreset);
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!etPreset.validate()) {
                    etPreset.setShakeAnimation();
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                String value = etPreset.getText().toString();
                MainActivity.m_this.hideInputPanel(etPreset);
                String xml = "<xml><cmd>clearpreset</cmd><preset>" + value + "</preset>" + "</xml>";
                int ret = LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), xml);
                if (ret != 0) {
                    toast(ConstantImpl.getTPSErrText(ret));
                    return;
                }

                toast(R.string.del_preset_succeed);
                dialog.dismiss();
            }
        }).create().show();
    }

    private void onBtnDelPreset(View v) {
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (!hasPtz(dev)) return;
        if (dev.m_lstPreset.isEmpty()) {
            toast(R.string.get_preset_no_data);
            return;
        }

        final Context ctx = this.getActivity();
        final String presets[] = new String[dev.m_lstPreset.size()];
        new AlertDialog.Builder(ctx).setTitle(R.string.tv_del_preset)
                .setItems(dev.m_lstPreset.toArray(presets), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = presets[which];
                        String xml = "<xml><cmd>clearpreset</cmd><preset>" + value + "</preset>" + "</xml>";
                        int ret = LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), xml);
                        if (ret != 0) {
                            toast(ConstantImpl.getTPSErrText(ret));
                            return;
                        }

                        dev.m_lstPreset.remove(value);
                        toast(R.string.del_preset_succeed);
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void onBtnSetPreset(View v) {
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        if (!hasPtz(dev)) return;
        final Context ctx = this.getActivity();
        final IntegerEditText etPreset = new IntegerEditText(ctx);
        etPreset.setHint(R.string.hit_input_preset);
        etPreset.setPadding(10, 10, 10, 10);
        etPreset.setSingleLine(true);
        etPreset.setText("");
        etPreset.setRange(1, 255);
        new AlertDialog.Builder(ctx).setTitle(R.string.tv_set_preset)
                .setView(etPreset)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        MainActivity.m_this.hideInputPanel(etPreset);
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!etPreset.validate()) {
                    etPreset.setShakeAnimation();
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                String value = etPreset.getText().toString();
                MainActivity.m_this.hideInputPanel(etPreset);
                String xml = "<xml><cmd>setpreset</cmd><preset>" + value + "</preset>" + "</xml>";
                int ret = LibImpl.getInstance().getFuncLib().PTZActionAgent(dev.m_dev.getDevId(), xml);
                if (ret != 0) {
                    toast(ConstantImpl.getTPSErrText(ret));
                    return;
                }

                if (!dev.m_lstPreset.contains(value)) dev.m_lstPreset.add(value);
                toast(R.string.set_preset_succeed);
                dialog.dismiss();
            }
        }).create().show();
    }

    public boolean addDeviceToLive(PlayerDevice dev) {
        if (dev == null) return false;
        Activity activity = this.getActivity();
        //检测网络是否已打开
        if (NetworkUtils.getNetworkState(activity) == NetworkUtils.NONE) {
            toast(R.string.dlg_network_check_tip);
            return false;
        }

        //先退出全屏播放
        if (m_full_view) changeView();

        int index = m_current_index;
        if (dev.m_play) {
            // 判断是否在可见的窗口播放，是则设置焦点，否则停止后重新查找窗口播放
            boolean found = false;
            int idleIndex = -1;
            int nullIndex = -1;
            for (int i = 0, j = 0; i < m_view_num_by_row;) {
                if (j >= m_view_num_by_row) {
                    i++;
                    j = 0;
                    continue;
                }

                index = i * MAX_VIEW_BY_ROW + j;
                j++;
                PlayerDevice exist = LibImpl.getInstance().getPlayerDevice(index);
                if (nullIndex == -1) {
                    if (null == exist) nullIndex = index;
                }

                if (idleIndex == -1) {
                    if (null != exist && !exist.m_play) idleIndex = index;
                }

                if (dev.m_view_id >= i * MAX_VIEW_BY_ROW && dev.m_view_id < (i * MAX_VIEW_BY_ROW) + m_view_num_by_row) found = true;
            }

            // 当前在可见窗口播放，设置焦点
            if (found) {
                setFocusWnd(dev.m_view_id);
                return true;
            }

            // 当前不在可见窗口播放，停止并设置到可见窗口播放
            stopPlay(dev, true, false);
            index = nullIndex > -1 ? nullIndex : idleIndex;
            if (index == -1) index = 0;
        } else if (dev.m_last_view_id > -1) {
            // 判断是否在可见的窗口播放，是则设置焦点，否则停止后重新查找窗口播放
            boolean found = false;
            int idleIndex = -1;
            int nullIndex = -1;
            for (int i = 0, j = 0; i < m_view_num_by_row;) {
                if (j >= m_view_num_by_row) {
                    i++;
                    j = 0;
                    continue;
                }

                index = i * MAX_VIEW_BY_ROW + j;
                j++;
                PlayerDevice exist = LibImpl.getInstance().getPlayerDevice(index);
                if (nullIndex == -1) {
                    if (null == exist) nullIndex = index;
                }

                if (idleIndex == -1) {
                    if (null != exist && !exist.m_play) idleIndex = index;
                }

                if (dev.m_last_view_id >= i * MAX_VIEW_BY_ROW && dev.m_last_view_id < (i * MAX_VIEW_BY_ROW) + m_view_num_by_row) found = true;
            }

            // 上次在可见窗口播放，设置为上次播放的窗口
            if (found) {
                index = dev.m_last_view_id;
                // 上次播放的窗口已经有其它设备在播放，自动查找空闲窗口播放，没有空闲窗口自动从第一个开始替换。
                PlayerDevice last = LibImpl.getInstance().getPlayerDevice(dev.m_last_view_id);
                if (null != last && last.m_play) {
                    index = nullIndex > -1 ? nullIndex : idleIndex;
                    if (index == -1) index = 0;
                }
            } else {
                // 不在可见窗口中，先停止原来窗口的播放
                PlayerDevice lastViewDev = LibImpl.getInstance().getPlayerDevice(dev.m_last_view_id);
                stopPlay(lastViewDev, true, false);
                index = nullIndex > -1 ? nullIndex : idleIndex;
                if (index == -1) index = 0;
            }
        } else {
            // 自动查找空闲窗口播放，没有空闲窗口自动从第一个开始替换。
            boolean found = false;
            int idleIndex = -1;
            int nullIndex = -1;
            for (int i = 0, j = 0; i < m_view_num_by_row;) {
                if (j >= m_view_num_by_row) {
                    i++;
                    j = 0;
                    continue;
                }

                index = i * MAX_VIEW_BY_ROW + j;
                j++;
                PlayerDevice exist = LibImpl.getInstance().getPlayerDevice(index);
                if (nullIndex == -1) {
                    if (null == exist) nullIndex = index;
                }

                if (idleIndex == -1) {
                    if (null != exist && !exist.m_play) idleIndex = index;
                }

                if (m_current_index >= i * MAX_VIEW_BY_ROW && m_current_index < (i * MAX_VIEW_BY_ROW) + m_view_num_by_row) found = true;
            }

            // 当前焦点窗口中的设备不在可见窗口播放
            if (!found) {
                index = nullIndex > -1 ? nullIndex : idleIndex;
                if (index == -1) index = 0;
            } else {
                PlayerDevice current_dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
                if (null != current_dev && !current_dev.m_dev.getDevId().equals(dev.m_dev.getDevId()) && current_dev.m_play) {
                    index = nullIndex > -1 ? nullIndex : idleIndex;
                    if (index == -1) index = 0;
                } else {
                    index = m_current_index;
                }
            }
        }

        // 先停止窗口现有设备
        PlayerDevice src_dev = LibImpl.getInstance().getPlayerDevice(index);
        if (null != src_dev) {
            stopPlay(src_dev, false, false);
        }
        // 判断当前播放设备是否在可见窗口，不在则关闭
        /*PlayerDevice current_dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null != current_dev) {
            stopPlay(current_dev);
        }*/

        dev.m_view_id = index;
        dev.m_last_view_id = index;
        //dev.m_stream_type = m_view_num == 1 ? Define.MAIN_STREAM_TYPE : Define.SUB_STREAM_TYPE;
        dev.m_stream_type = Define.SUB_STREAM_TYPE;
        dev.m_frame_type = m_view_num <= 4 ? 0 : 1;
        dev.m_audio = new AudioPlayer(index);
        dev.m_audio.mIsAecm = false;
        dev.m_audio.mIsNoiseReduction = false;
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

        if (!startPlay(index, dev)) {
            stopPlay(dev, true, true);
            return false;
        }

        setFocusWnd(index);

        if (dev.m_lstPreset.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    onBtnGetPreset(m_view.findViewById(R.id.btn_get_preset));
                }
            }).start();
        }

        if (Define.LOGIN_TYPE_DEMO == Global.m_loginType) {
            dev.startAutoStopTimer(180, new PlayerDevice.IWatchTimeout() {
                @Override
                public void onTimeout(final PlayerDevice dev) {
                    VideoFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopPlay(dev, true, false);
                            toast(R.string.demo_only_preview_limit);
                        }
                    });
                }
            });
        } else if (dev.m_share_video_timestamp > 0) {
            dev.startAutoStopTimer(dev.m_share_video_timestamp, new PlayerDevice.IWatchTimeout() {
                @Override
                public void onTimeout(final PlayerDevice dev) {
                    VideoFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dev.m_share_video_timestamp = -1;
                            stopPlay(dev, true, false);
                            LibImpl.getInstance().removePlayerDevice(dev.m_devId);
                            toast(R.string.video_share_finish_prompt);
                        }
                    });
                }
            });
        }

        return true;
    }

    public void setVideoInfo(final int index, final String msg) {
        final PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(index);
        if (null == dev) return;
        if (!dev.m_play) return;

        RelativeLayout layout = m_layout_map.get(index);
        if (null == layout) return;
        MarqueeTextView v = (MarqueeTextView) layout.findViewById(R.id.tvLiveInfo);
        v.setVisibility(View.VISIBLE);

        if (null == msg) {
            v.setVisibility(View.GONE);
        } else if (T(R.string.tv_video_play_tip).compareToIgnoreCase(msg) == 0) { //playing
            StringBuffer msgBuf = new StringBuffer();
            String _devName = dev.getDeviceName();
            msgBuf.append("[").append(_devName).append("]");
            String _msg = msgBuf.toString();
            v.setText(_msg);
            dev.m_tipInfo = msg;
        } else {
            StringBuffer msgBuf = new StringBuffer();
            if (!isNullStr(dev.m_dev.getDevId())) {
                //加载别名
                String _devName = dev.getDeviceName();
                msgBuf.append("[").append(_devName).append("]:").append(msg);
                if (!"".equals(msg)) msgBuf.append(".");
            } else {
                msgBuf.append(msg);
                if (!"".equals(msg)) msgBuf.append(".");
            }

            String _msg = msgBuf.toString();
            v.setText(_msg);
        }
    }

    public void setVideoInfo(String devID, final String msg) {
        Log.d("setTipText", devID + "," + msg);
        int index = LibImpl.getInstance().getIndexByDeviceID(devID);
        setVideoInfo(index, msg);
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_TALK://TPS_TALKRsp
                LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_TALKRsp ts = (TPS_TALKRsp) msgObj.recvObj;
                String devID = new String(ts.getSzDevId()).trim();
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devID);
                TPS_AUDIO_PARAM audioParm = ts.getAudioParam();
                if (audioParm == null) {
                    toast(R.string.tv_talk_fail_param_error_tip);
                    stopTalk(dev, false);
                    return true;
                }

                if (ts.getnResult() != 0) {
                    toast(R.string.tv_talk_fail_tip, ts.getnResult());
                    stopTalk(dev, false);
                }

                if (SDK_CONSTANT.AUDIO_TYPE_G711.compareToIgnoreCase(new String(audioParm.getAudio_encoder()).trim()) != 0) {
                    toast(R.string.tv_talk_fail_illegal_format_tip);
                    stopTalk(dev, false);
                }

                toast(R.string.tv_talk_success_tip);
                //startTalk(dev, true);
                return true;
            case Global.MSG_ADD_ALARM_DATA:
                TPS_AlarmInfo ta = (TPS_AlarmInfo) msg.obj;
                showAlarmIcon(new String(ta.getSzDevId()).trim(), true);
                return true;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED:
                onLoginFailed((PlayerDevice) msg.obj);
                return true;
            case LibImpl.MSG_VIDEO_SET_STATUS_INFO:
                msgObj = (LibImpl.MsgObject) msg.obj;
                if (null != msgObj.reserveObj) {
                    setTipText(msgObj.devID, msgObj.recvObj, (String) msgObj.reserveObj);
                } else {
                    setTipText(msgObj.devID, msgObj.recvObj);
                }
                return true;
            case SDK_CONSTANT.TPS_MSG_RSP_PTZREQ:
                String data = (String) msg.obj;
                onPtzReqResp(data);
                return true;
            case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH:
                TPS_AddWachtRsp tas = (TPS_AddWachtRsp) msg.obj;
                onAddWatchResp(tas);
                return true;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_NotifyInfo tn = (TPS_NotifyInfo) msgObj.recvObj;
                onMsgP2pOffline(tn);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_NotifyInfo tni = (TPS_NotifyInfo) msgObj.recvObj;
                onMsgP2pNvrOffline(tni);
                break;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG:
                msgObj = (LibImpl.MsgObject) msg.obj;
                List<NetSDK_UserAccount> lst = (List<NetSDK_UserAccount>) msgObj.recvObj;
                //CheckDefaultUserPwd(LibImpl.getInstance().getPlayerDevice(msgObj.devID));
                return false;
            case Define.MSG_RECEIVER_MEDIA_FIRST_FRAME:
                onRecvFirstFrame((PlayerDevice) msg.obj);
                return true;
        }

        return false;
    }

    private void onRecvFirstFrame(PlayerDevice dev) {
        if (null == dev) return;
        setTipText(dev.m_devId, "");
    }

    private void CheckDefaultUserPwd(final PlayerDevice dev) {
        if (null == dev) return;
        if (Global.m_loginType == Define.LOGIN_TYPE_DEMO) return;
        int index = LibImpl.getInstance().getIndexByDeviceID(dev.m_devId);
        RelativeLayout layout = m_layout_map.get(index);
        if (null == layout) return;

        /*View v = layout.findViewById(R.id.tvMsgInfo);
        if ("admin".equals(dev.m_dev.getLoginName()) && "123456".equals(dev.m_dev.getLoginPassword())) {
            String title = Global.m_res.getString(R.string.prompt_modify_default_password);
            ((MarqueeTextView)v).setText(title);
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.GONE);
        }*/

        if (!dev.isNVR() && "admin".equals(dev.m_dev.getLoginName()) && "123456".equals(dev.m_dev.getLoginPassword()) && !dev.m_prompt_modify_pwd) {
            dev.m_prompt_modify_pwd = true;
            String title = Global.m_res.getString(R.string.prompt_modify_default_password);
            final TextView tv = (TextView) m_view.findViewById(R.id.txt_prompt);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<NetSDK_UserAccount> users = new ArrayList<NetSDK_UserAccount>();
                    NetSDK_UserAccount u = new NetSDK_UserAccount();
                    u.setUserName(dev.m_dev.getLoginName());
                    u.setPassword(dev.m_dev.getLoginPassword());
                    users.add(u);
                    MainActivity.m_this.modifyDeviceDefaultPassword(dev.m_devId, users);
                }
            });

            tv.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(m_view.getContext(), R.anim.my_slide_in_from_top);
            tv.startAnimation(animation);
            tv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(m_view.getContext(), R.anim.my_slide_out_to_top);
                    tv.startAnimation(animation);
                    tv.setVisibility(View.GONE);
                }
            }, 5000);
        }
    }

    private void onPtzReqResp(String data) {
        if ("".equals(data)) return;
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
        if (null == dev) return;
        String preset[] = data.split("\\^");
        if (preset.length > 0 && dev.m_lstPreset.isEmpty()) {
            if ("0".equals(preset[0])) return;
            dev.m_lstPreset.addAll(Arrays.asList(preset).subList(1, preset.length));
        }
    }

    private void onAddWatchResp(TPS_AddWachtRsp ts) {
        final String devId = new String(ts.getSzDevId()).trim();
        if (ts.getnResult() == 0) {//视频请求成功
            setTipText(devId, R.string.tv_video_req_succeed_tip);
        } else {
            setTipText(devId, R.string.tv_video_req_fail_tip, ts.getnResult() + "");
        }
    }

    private void onMsgP2pOffline(TPS_NotifyInfo tn) {
        /*if (null == tn) return;
        String devId = new String(tn.getSzDevId()).trim();
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devId);
        if (null == dev) return;
        stopPlay(dev, false, false);*/
    }

    private void onMsgP2pNvrOffline(TPS_NotifyInfo tn) {
        /*if (null == tn) return;
        String devId = new String(tn.getSzDevId());
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devId);
        if (null == dev) return;
        stopPlay(dev, false, false);*/
    }

    private void onLoginFailed(final PlayerDevice dev) {
        if (null == dev) return;
        setTipText(dev.m_devId, R.string.tv_video_req_user_name_pwd_incorrect_tip);
        Context ctx = this.getActivity();
        final ClearEditText etUser = new ClearEditText(ctx);
        etUser.setHint(R.string.dev_list_hint_input_user_name);
        etUser.setPadding(10, 10, 10, 10);
        etUser.setSingleLine(true);
        etUser.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_FILTER | EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etUser.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});

        final ClearEditText etPwd = new ClearEditText(ctx);
        etPwd.setHint(R.string.dev_list_hint_input_password);
        etPwd.setPadding(10, 10, 10, 10);
        etPwd.setSingleLine(true);
        etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etPwd.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        etPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_PWD_LENGTH)});

        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 0, 5, 0);
        layout.setBackgroundColor(Color.rgb(207, 232, 179));
        layout.addView(etUser);
        layout.addView(etPwd);

        AlertDialog d = new AlertDialog.Builder(ctx).setTitle(R.string.dev_list_tip_title_input_user_pwd)
                .setView(layout)
                .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        MainActivity.m_this.hideInputPanel(etUser);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(this.getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userName = etUser.getText().toString();
                        String password = etPwd.getText().toString();
                        if ("".equals(userName) || "".equals(password)) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            return;
                        }

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        MainActivity.m_this.hideInputPanel(etUser);
                        dialog.dismiss();

                        int ret = FunclibAgent.getInstance().ModifyDevPassword(dev.m_dev.getDevId(), userName, password);
                        if (0 != ret) {
                            toast(R.string.dlg_set_user_info_fail_tip);
                            return;
                        }

                        toast(R.string.dlg_set_user_info_succeed_tip);
                        int index = LibImpl.getInstance().getIndexByDeviceID(dev.m_devId);
                        stopPlay(dev, false, false);
                        startPlay(index, dev);
                    }
                }).create();
        d.show();
    }

    public void setTipText(String devID, Object msg) {
        setTipText(devID, msg, "");
    }

    public void setTipText(String devID, Object msg, String reserver) {
        String _msg = T(msg);
        if (!TextUtils.isEmpty(reserver)) _msg += "(" + reserver + ")";
        setVideoInfo(devID, _msg);
    }

    /**
     * 云台控制信息显示的停留时间，默认为1.5秒
     */
    private final long PTZ_TIP_Delayed_TIME = (long) (1.5 * 1000);

    public void postDelayedVideoInfoMsg(int index) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(index);
        if (null == dev) return;

        RelativeLayout layout = m_layout_map.get(index);
        if (null == layout) return;
        MarqueeTextView v = (MarqueeTextView) layout.findViewById(R.id.tvLiveInfo);
        v.setVisibility(View.VISIBLE);
        v.postDelayed(mRunnable, PTZ_TIP_Delayed_TIME);
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(m_current_index);
            if (null == dev) return;
            setVideoInfo(m_current_index, dev.m_tipInfo);
        }
    };
}

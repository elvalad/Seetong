package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.*;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.*;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.audio.AudioPlayer;
import com.android.opengles.OpenglesRender;
import com.android.opengles.OpenglesView;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.aid.MarqueeTextView;
import ipc.android.sdk.com.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.view.animation.Animation.*;

/**
 * Created by Administrator on 2015/9/15.
 */
public class PlayVideoFragment extends BaseFragment {
    private String TAG = PlayVideoFragment.class.getName();
    private View fragmentView;
    private RelativeLayout mainLayout;
    private OpenglesView openglesView;
    private OpenglesRender openglesRender;
    private PlayerDevice playerDevice;
    private int currentIndex = 0;
    private GestureDetector gestureDetector;

    public static final int FLING_MOVEMENT_THRESHOLD = 100;

    public static PlayVideoFragment newInstance(PlayerDevice playerDevice, int index) {
        return new PlayVideoFragment(playerDevice, index);
    }

    public PlayVideoFragment() {}

    public PlayVideoFragment(PlayerDevice playerDevice, int index) {
        this.playerDevice = playerDevice;
        this.currentIndex = index;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PlayerActivity.m_this.setPlayVideoFragment(this);
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        fragmentView = inflater.inflate(R.layout.play_video, container, false);
        mainLayout = (RelativeLayout) fragmentView.findViewById(R.id.play_video_layout);
        gestureDetector = new GestureDetector(fragmentView.getContext(), new MyOnGestureListener());
        gestureDetector.setOnDoubleTapListener(new OnDoubleClick());
        initView();
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPlay();
    }

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
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
            if (e1 == null || e2 == null) {
                return false;
            }

            /* 如果GestureDetector检测到用户向左滑动，则显示上一个设备的视频 */
            if ((e2.getX() -e1.getX()) > FLING_MOVEMENT_THRESHOLD) {
                //toast("previous");
                showPreviousDeviceVideo(playerDevice);
            }

            /* 如果GestureDetector检测到用户向右滑动，这显示下一个设备的视频 */
            if ((e1.getX() - e2.getX()) > FLING_MOVEMENT_THRESHOLD) {
                //toast("next");
                showNextDeviceVideo(playerDevice);
            }

            return false;
        }
    }

    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //toast("play multi video");
            //stopCurrentPlay();
            PlayerActivity.m_this.setCurrentFragment("play_multi_video_fragment");
            PlayerActivity.m_this.playMultiVideo(playerDevice, currentIndex);
            return true;
        }
    }

    class TouchListener implements View.OnTouchListener {
        private int mode = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
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
            if (null == playerDevice) {
                return gestureDetector.onTouchEvent(event);
            }
            OpenglesRender render = playerDevice.m_video;
            if (null == render) {
                return gestureDetector.onTouchEvent(event);
            }
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    preScale = render.bitmapScale;
                    start.set(event.getX(), event.getY());
                    startOffset.set(render.mStartX, render.mStartY);
                    break;
                case MotionEvent.ACTION_MOVE:
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
                    } else if (mode == ZOOM) {
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
                case MotionEvent.ACTION_UP:
                    mode = 0;
                    if (render.bitmapScale <= 1.0) {
                        render.resetScaleInfo();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    if (render.bitmapScale <= 1.0) {
                        render.resetScaleInfo();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = this.spacing(event);
                    if (oldDist > MIN_SPCE) {
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;
            }
            return gestureDetector.onTouchEvent(event);
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
        openglesView = (OpenglesView) mainLayout.findViewById(R.id.liveVideoView);
        openglesRender = new OpenglesRender(openglesView, 0);
        openglesRender.setVideoMode(OpenglesRender.VIDEO_MODE_CUSTOM);
        openglesView.setRenderer(openglesRender);
        openglesView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        openglesView.setTag(0);

        openglesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        openglesView.setLongClickable(true);
        openglesView.setOnTouchListener(new TouchListener());
        /*openglesView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });*/

        openglesRender.addCheckCallback(new OpenglesRender.CheckCallback() {
            @Override
            public void recvDataTimout(int branch, OpenglesRender rander) {
                if (null == rander) {
                    setVideoInfo(branch, T(R.string.tv_video_play_tip));
                    return;
                }
                setVideoInfo(branch, T(R.string.tv_video_wait_video_stream_tip));
            }
        });
    }

    public void startPlay() {
        startPlay(playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        mainLayout.findViewById(R.id.tvLiveInfo).setVisibility(View.VISIBLE);
    }

    public void startChoosenPlay(PlayerDevice dev) {
        stopCurrentPlay();
        playerDevice = dev;
        startPlay(dev);
    }

    public void autoCyclePlay() {
        showNextDeviceVideo(this.playerDevice);
    }

    public void startSpeak() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        stopAllVoice();
        stopAllTalk();

        if (null == playerDevice || !playerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        if (null == playerDevice.m_audio) {
            return;
        }

        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
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

        if (playerDevice.m_talk) return;

        int ret = LibImpl.getInstance().getFuncLib().StartTalkAgent(playerDevice.m_dev.getDevId());
        if (0 != ret) {
            toast(R.string.tv_talk_fail_tip);
            return;
        }

        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        playerDevice.m_audio.startTalk();
        playerDevice.m_talk = true;
    }

    private void stopAllVoice() {
        if (null == playerDevice || null == playerDevice.m_audio) return;
        playerDevice.m_audio.stopOutAudio();
        playerDevice.m_voice = false;
    }

    private void stopAllTalk() {
        if (null == playerDevice || null == playerDevice.m_audio) return;
        LibImpl.getInstance().getFuncLib().StopTalkAgent(playerDevice.m_dev.getDevId());
        playerDevice.m_audio.stopTalk();
        playerDevice.m_talk = false;
    }

    public void stopSpeak() {
        if (null == playerDevice || null == playerDevice.m_audio) return;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        LibImpl.getInstance().getFuncLib().StopTalkAgent(playerDevice.m_dev.getDevId());
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        if (!playerDevice.m_talk) return;
        playerDevice.m_audio.stopTalk();
        playerDevice.m_talk = false;
    }

    public void videoCapture() {
        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        String strDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String fileName = Global.getImageDir() + "/" + playerDevice.m_dev.getDevId() + "_" + strDate + ".jpg";
        boolean bShotOk = playerDevice.m_video.startShot(fileName);
        if (bShotOk) {
            toast(R.string.snapshot_succeed);
            final ImageView screenShotView = (ImageView) mainLayout.findViewById(R.id.screenShotFlash);
            screenShotView.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(Global.getSnapshotDir() + "/" + playerDevice.m_dev.getDevId() + ".jpg");
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
            }
            screenShotView.setImageBitmap(bitmap);
            RotateAnimation fade = new RotateAnimation(0, -60);
            fade.setDuration(500);
            fade.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation anim) {
                    screenShotView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            screenShotView.startAnimation(fade);
        } else {
            toast(R.string.snapshot_failed);
        }
    }

    public boolean startHighDefinition() {
        if (null == playerDevice) {
            return false;
        }
        LibImpl.stopPlay(0, playerDevice);
        playerDevice.m_stream_type = Define.MAIN_STREAM_TYPE;
        startPlay(playerDevice);
        return true;
    }

    public void stopHighDefinition() {
        if (null == playerDevice) {
            return;
        }
        LibImpl.stopPlay(0, playerDevice);
        playerDevice.m_stream_type = Define.SUB_STREAM_TYPE;
        startPlay(playerDevice);
    }

    public boolean startVideoRecord() {
        if (null == playerDevice || !playerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return false;
        }

        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        String filePath = Global.getVideoDir() + "/" + playerDevice.m_dev.getDevId();
        File dir = new File(filePath);
        if (!(dir.exists())) dir.mkdirs();
        int ret = LibImpl.getInstance().getFuncLib().StartRecordAgent(playerDevice.m_dev.getDevId(), filePath, EtcInfo.PER_RECORD_TIME_LENGTH);
        Log.d(TAG, "video record ret is : " + ret);
        if (0 != ret) {
            toast(R.string.fvu_tip_start_record_failed);
            return false;
        }

        playerDevice.m_record = true;
        showRecordIcon(playerDevice.m_devId, true);

        return true;
    }

    public void stopVideoRecord() {
        if (null == playerDevice || !playerDevice.m_playing) {
            //toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        LibImpl.getInstance().getFuncLib().StopRecordAgent(playerDevice.m_dev.getDevId());
        playerDevice.m_record = false;
        showRecordIcon(playerDevice.m_devId, false);
    }

    public void showRecordIcon(String devId, boolean bShow) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devId);
        if (null == dev || !dev.m_playing) return;

        if (dev.m_view_id < 0) return;
        ImageView imageView = (ImageView) mainLayout.findViewById(R.id.imgRecord);
        imageView.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void startRecordPlayBack() {
        if (null == playerDevice) return;
        if (!playerDevice.is_p2p_replay()) {
            toast(R.string.tv_not_support_front_end_record);
            return;
        }

        /* 开启回放前先关闭正在播放的设备 */
        stopPlay();

        Intent it = new Intent(this.getActivity(), FrontEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, playerDevice.m_dev.getDevId());
        this.startActivity(it);
    }

    public boolean startVideoSound() {
        if (null == playerDevice || !playerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return false;
        }

        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        if (!rsp.hasAudio()) {
            toast(R.string.fvu_tip_open_voice_fail_invalid_audio_device);
            return false;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.fvu_tip_open_voice_fail_illegal_format_tip);
            return false;
        }

        if (null == playerDevice.m_add_watch_rsp || !playerDevice.m_add_watch_rsp.hasAudio()) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        playerDevice.m_audio.startOutAudio();
        playerDevice.m_voice = true;
        toast(R.string.fvu_tip_open_voice);
        PlayerActivity.m_this.setVideoSoundWidget();

        return true;
    }

    public void stopVideoSound() {
        if (null == playerDevice || !playerDevice.m_playing) {
            //toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
        if (null == rsp) {
            //toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!rsp.hasAudio()) {
            //toast(R.string.fvu_tip_open_voice_fail_invalid_audio_device);
            return;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.fvu_tip_open_voice_fail_illegal_format_tip);
            return;
        }

        if (!playerDevice.m_voice) return;
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        playerDevice.m_audio.stopOutAudio();
        playerDevice.m_voice = false;
        toast(R.string.fvu_tip_close_voice);
    }

    private boolean startPlay(PlayerDevice dev) {
        if (null == dev) {
            Log.e(TAG, "device is null!!!");
            return false;
        }

        dev.m_device_play_count++;
        dev.m_video = this.openglesRender;
        dev.m_video.mIsStopVideo = false;

        if (!dev.m_play) {
            int ret = LibImpl.startPlay(0, dev, dev.m_stream_type, dev.m_frame_type);
            if (ret == 0) {
                dev.m_online = true;
                dev.m_playing = false;
                setVideoInfo(0, T(R.string.tv_video_req_tip));
            } else {
                String selfID = "";
                if (LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(dev.m_dev.getDevId())) != null) {
                    selfID = LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(dev.m_dev.getDevId())).getNotifyStr();
                }

                Log.i("DeviceNotifyInfo", "DeviceNotifyInfo ary:" + LibImpl.mDeviceNotifyInfo + ".");
                selfID = (isNullStr(selfID)) ? "" : ("(" + selfID + ")");
                setVideoInfo(0, ConstantImpl.getTPSErrText(ret, false) + selfID);
                toast(ConstantImpl.getTPSErrText(ret, false) + selfID);
                return false;
            }
        } else {
            setVideoInfo(0, dev.m_tipInfo);
            setVideoInfo2(0, dev.m_tipTinfo2);
        }

        dev.m_play = true;
        dev.m_view_id = 0;
        View view = mainLayout.findViewById(R.id.tvLiveInfo);
        view.setVisibility(View.VISIBLE);
        view = dev.m_video.getSurface();
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setVisibility(View.VISIBLE);

        dev.m_audio = new AudioPlayer(currentIndex);
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

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void stopPlay() {
        /* 停止视频播放之前先停止录像 */
        stopVideoRecord();
        stopVideoSound();
        LibImpl.stopPlay(0, playerDevice);
        View view = mainLayout.findViewById(R.id.liveVideoView);
        view.setBackgroundColor(Color.BLACK);
        setVideoInfo(0, T(R.string.tv_video_stop_tip));
        setVideoInfo2(0, "");
        playerDevice.m_video.mIsStopVideo = true;
        mainLayout.invalidate();
    }

    private void stopCurrentPlay() {
        PlayerActivity.m_this.resetWidget();
         /* 停止视频播放之前先停止录像 */
        stopVideoRecord();
        stopVideoSound();
        LibImpl.stopPlay(0, playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.tvMsgInfo).setVisibility(View.GONE);
        playerDevice.m_video.mIsStopVideo = true;
        playerDevice.m_video = null;
    }

    public PlayerDevice getCurrentDevice() {
        return this.playerDevice;
    }

    private void setCurrentDevice(PlayerDevice device) {
        this.playerDevice = device;
    }

    private void showPreviousDeviceVideo(PlayerDevice device) {
        /* 恢复PlayerActivity的button状态 */
        PlayerActivity.m_this.resetWidget();

         /* 停止视频播放之前先停止录像 */
        stopVideoRecord();
        stopVideoSound();
        stopHighDefinition();

        /* 停止当前正在播放的设备，开始播放上一个设备 */
        LibImpl.stopPlay(0, device);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
        device.m_video.mIsStopVideo = true;
        device.m_video = null;

        List<PlayerDevice> deviceList = new LinkedList<>();
        deviceList.addAll(Global.getSelfDeviceList());
        for (int i = 0; i < deviceList.size(); i++) {
            if (device.equals(deviceList.get(i))) {
                if (i > 0) {
                    setCurrentDevice(deviceList.get(i - 1));
                } else {
                    setCurrentDevice(deviceList.get(deviceList.size() - 1));
                }
                break;
            }
        }

        /* 切换之后设置PlayerActivity当前播放的设备ID */
        PlayerActivity.m_this.setCurrentDeviceId(this.playerDevice.m_devId);

        Boolean bRet = startPlay(this.playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        if (!bRet) {
            Log.e(TAG, "Start previous device err!!!");
        }
    }

    private void showNextDeviceVideo(PlayerDevice device) {
        /* 恢复PlayerActivity的button状态 */
        PlayerActivity.m_this.resetWidget();

        /* 停止视频播放之前先停止录像 */
        stopVideoRecord();
        stopVideoSound();
        stopHighDefinition();

        /* 停止当前正在播放的设备，开始播放下一个设备 */
        LibImpl.stopPlay(0, device);

        List<PlayerDevice> deviceList = new LinkedList<>();
        deviceList.addAll(Global.getSelfDeviceList());
        for (int i = deviceList.size() - 1; i >= 0; i--) {
            if (device.equals(deviceList.get(i))) {
                if (i < deviceList.size() - 1 ) {
                    setCurrentDevice(deviceList.get(i + 1));
                } else {
                    setCurrentDevice(deviceList.get(0));
                }
                break;
            }
        }

        /* 切换之后设置PlayerActivity当前播放的设备ID */
        PlayerActivity.m_this.setCurrentDeviceId(this.playerDevice.m_devId);

        //fragmentView.setBackgroundColor(Color.BLACK);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
        device.m_video.mIsStopVideo = true;
        device.m_video = null;

        Boolean bRet = startPlay(this.playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        if (!bRet) {
            Log.e(TAG, "Start next device err!!!");
        }
    }

    public void setVideoInfo(String devID, final String msg) {
        Log.d("setTipText", devID + "," + msg);
        int index = LibImpl.getInstance().getIndexByDeviceID(devID);
        setVideoInfo(index, msg);
    }

    public void setVideoInfo(final int index, final String msg) {
        MarqueeTextView v = (MarqueeTextView) mainLayout.findViewById(R.id.tvLiveInfo);
        if (null == playerDevice) {
            Log.e(TAG, "device is null, return from setVideoInfo err!!!");
            return;
        }

        String dev_type = "";
        /*
        if (0 == playerDevice.m_net_type) {
            dev_type = "-O";
        } else if (16 == playerDevice.m_net_type) {
            dev_type = "-R";
        } else if (-1 != playerDevice.m_net_type) {
            dev_type = "-P";
        }*/

        if (null == msg) {
            v.setVisibility(View.GONE);
        } else if (T(R.string.tv_video_play_tip).compareToIgnoreCase(msg) == 0) { //playing
            StringBuilder msgBuf = new StringBuilder();
            String _devName = playerDevice.getDeviceName();
            msgBuf.append("[").append(_devName).append(dev_type).append("]");
            String _msg = msgBuf.toString();
            v.setText(_msg);
        } else {
            StringBuilder msgBuf = new StringBuilder();
            if (!isNullStr(playerDevice.m_dev.getDevId())) {
                //加载别名
                String _devName = playerDevice.getDeviceName();
                msgBuf.append("[").append(_devName).append(dev_type).append("]");
                if (!TextUtils.isEmpty(msg)) msgBuf.append(":").append(msg).append(".");
            } else {
                msgBuf.append(msg);
                if (!"".equals(msg)) msgBuf.append(".");
            }

            playerDevice.m_tipInfo = msg;
            String _msg = msgBuf.toString();
            v.setText(_msg);
        }
    }

    public void setVideoInfo2(String devID, final String msg) {
        int index = LibImpl.getInstance().getIndexByDeviceID(devID);
        setVideoInfo2(index, msg);
    }

    public void setVideoInfo2(final int index, final String msg) {
        MarqueeTextView v = (MarqueeTextView) mainLayout.findViewById(R.id.tvMsgInfo);
        v.setVisibility(Config.m_show_video_info ? View.VISIBLE : View.GONE);
        playerDevice.m_tipTinfo2 = msg;
        if(msg.equals("")) {
            v.setText(msg);
        } else {
            v.setText(Global.m_mobile_net_type_2 + msg);
        }
    }

    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_TALK://TPS_TALKRsp
                LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_TALKRsp ts = (TPS_TALKRsp) msgObj.recvObj;
                String devID = new String(ts.getSzDevId()).trim();
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devID);
                TPS_AUDIO_PARAM audioParm = ts.getAudioParam();
                if (audioParm == null) {
                    toast(R.string.tv_talk_fail_param_error_tip);
                    stopSpeak();
                    return true;
                }

                if (ts.getnResult() != 0) {
                    toast(R.string.tv_talk_fail_tip, ts.getnResult());
                    stopSpeak();
                }

                if (SDK_CONSTANT.AUDIO_TYPE_G711.compareToIgnoreCase(new String(audioParm.getAudio_encoder()).trim()) != 0) {
                    toast(R.string.tv_talk_fail_illegal_format_tip);
                    stopSpeak();
                }

                toast(R.string.tv_talk_success_tip);
                //startTalk(dev, true);
                return true;
            case Global.MSG_ADD_ALARM_DATA:
                TPS_AlarmInfo ta = (TPS_AlarmInfo) msg.obj;
                //showAlarmIcon(new String(ta.getSzDevId()).trim(), true);
                return true;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED:
                //onLoginFailed((PlayerDevice) msg.obj);
                return true;
            case LibImpl.MSG_VIDEO_SET_STATUS_INFO:
                //onSetStatusInfo(msg);
                return true;
            case SDK_CONSTANT.TPS_MSG_RSP_PTZREQ:
                String data = (String) msg.obj;
                //onPtzReqResp(data);
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
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_NotifyInfo tnii = (TPS_NotifyInfo) msgObj.recvObj;
                onMsgP2pNvrChnOffline(tnii);
                break;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG:
                msgObj = (LibImpl.MsgObject) msg.obj;
                List<NetSDK_UserAccount> lst = (List<NetSDK_UserAccount>) msgObj.recvObj;
                //CheckDefaultUserPwd(LibImpl.getInstance().getPlayerDevice(msgObj.devID));
                return false;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DISP_INFO:
                tni = (TPS_NotifyInfo) msg.obj;
                onNotifyDispInfo(tni);
                return true;
            case Define.MSG_RECEIVER_MEDIA_FIRST_FRAME:
                onRecvFirstFrame((PlayerDevice) msg.obj);
                return true;
        }

        return false;
    }

    private void onMsgP2pOffline(TPS_NotifyInfo tni) {
        String devId = new String(tni.getSzDevId()).trim();
        setTipText(devId, R.string.dlg_device_offline_tip);
    }

    private void onMsgP2pNvrOffline(TPS_NotifyInfo tni) {
        String devId = new String(tni.getSzDevId()).trim();
        setTipText(devId, R.string.dlg_device_offline_tip);
    }

    private void onMsgP2pNvrChnOffline(TPS_NotifyInfo tni) {
        String devId = new String(tni.getSzDevId()).trim();
        setTipText(devId, R.string.dlg_device_offline_tip);
    }

    private void onSetStatusInfo(android.os.Message msg) {
        if (msg.arg1 == 0) {
            LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
            if (null != msgObj.reserveObj) {
                setTipText(msgObj.devID, msgObj.recvObj, (String) msgObj.reserveObj);
            } else {
                setTipText(msgObj.devID, msgObj.recvObj);
            }
        } else if (msg.arg1 == 1) {
            LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
            setVideoInfo2(msgObj.devID, (String)msgObj.recvObj);
        }
    }

    private void onRecvFirstFrame(PlayerDevice dev) {
        if (null == dev) return;
        setTipText(dev.m_devId, "");
    }

    private void onAddWatchResp(TPS_AddWachtRsp ts) {
        final String devId = new String(ts.getSzDevId()).trim();
        int result = ts.getnResult();
        if (result == 0) {//视频请求成功
            PlayerDevice dev = LibImpl.findDeviceByID(devId);
            if (dev.m_online && !dev.m_play) {
                setTipText(devId, R.string.tv_video_req_succeed_tip);
            }
        } else {
            /*if (-1 == result) {
                setTipText(devId, R.string.dlg_login_fail_user_pwd_incorrect_tip);
            } else*/ if (-2 == result) {
                setTipText(devId, R.string.err_illegal_channel_id);
            } else if (-3 == result) {
                setTipText(devId, R.string.err_illegal_stream_id);
            } else if (-4 == result) {
                setTipText(devId, R.string.err_illegal_audio_enable);
            } else if (-7 == ts.getnResult()) {
                setTipText(devId, R.string.err_all_stream_full);
            } else if (-8 == ts.getnResult()) {
                setTipText(devId, R.string.err_session_stream_full);
            } else if (-9 == ts.getnResult()) {
                setTipText(devId, R.string.err_get_video_cfg_fail);
            } else if (-10 == ts.getnResult()) {
                setTipText(devId, R.string.err_get_stream_fail);
            } else {
                setTipText(devId, R.string.tv_video_req_fail_tip, ts.getnResult() + "");
            }
        }
    }

    private void onNotifyDispInfo(TPS_NotifyInfo tni) {
        if (playerDevice.m_play) {
            String devId = new String(tni.getSzDevId()).trim();
            String msg = new String(tni.getSzInfo()).trim();
            PlayerDevice dev = LibImpl.findDeviceByID(devId);
            if (null != dev) {
                setVideoInfo2(dev.m_devId, msg);
            } else {
                List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
                if (null != lst) {
                    for (PlayerDevice d : lst) {
                        setVideoInfo2(d.m_devId, msg);
                    }
                }
            }
        }
    }

    public void setTipText(String devID, Object msg) {
        setTipText(devID, msg, "");
    }

    public void setTipText(String devID, Object msg, String reserver) {
        String _msg = T(msg);
        if (!TextUtils.isEmpty(reserver)) _msg += "(" + reserver + ")";
        setVideoInfo(devID, _msg);
    }
}

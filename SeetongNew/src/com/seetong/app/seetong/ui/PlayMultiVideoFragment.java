package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.audio.AudioPlayer;
import com.android.opengles.OpenglesRender;
import com.android.opengles.OpenglesView;
import com.custom.etc.EtcInfo;
import com.seetong.app.seetong.Config;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.aid.MarqueeTextView;
import ipc.android.sdk.com.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by gmk on 2015/9/18.
 */
public class PlayMultiVideoFragment extends BaseFragment {
    private String TAG = PlayMultiVideoFragment.class.getName();
    private View fragmentView;
    private LinearLayout mainLayout;
    private PlayerDevice playerDevice;
    private PlayerDevice chosenPlayerDevice;
    private int[] location = new int[2];
    List<PlayerDevice> deviceList = new LinkedList<>();
    private GestureDetector gestureDetector;
    private int currentIndex = 0;
    public static final int FLING_MOVEMENT_THRESHOLD = 100;
    public static final int MAX_WINDOW = 4;
    public static final int MAX_WINDOW_BY_ROW = 2;
    public static final int MAX_WINDOW_BY_COLUMN = 2;
    private static boolean bFullScreen = false;
    private Animation animation;
    private Map<Integer, RelativeLayout> layoutMap = new HashMap<>();
    private Map<Integer, OpenglesRender> renderMap = new HashMap<>();

    public static PlayMultiVideoFragment newInstance(PlayerDevice playerDevice, int index) {
        return new PlayMultiVideoFragment(playerDevice, index);
    }

    public PlayMultiVideoFragment(PlayerDevice playerDevice, int index) {
        this.playerDevice = getPlayerDeviceByIndex(playerDevice, index);
        this.chosenPlayerDevice = this.playerDevice;
        this.currentIndex = index;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "PlayMultiVideoFragment onCreateView...");
        PlayerActivity.m_this.setPlayMultiVideoFragment(this);
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        fragmentView = inflater.inflate(R.layout.play_multi_video, container, false);
        mainLayout = (LinearLayout) fragmentView.findViewById(R.id.play_multi_video_layout);
        /* TODO:需要获取精确的每个图形绘制窗口的宽高用于多画面到单画面时选择正确的device */
        location = PlayerActivity.m_this.getFragmentLocation();
        deviceList = getDeviceList(playerDevice);
        gestureDetector = new GestureDetector(fragmentView.getContext(), new MyOnGestureListener());
        gestureDetector.setOnDoubleTapListener(new OnDoubleClick());
        initView();

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPlay(deviceList);
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
                showPreviousDeviceListVideo(playerDevice);
            }

            /* 如果GestureDetector检测到用户向右滑动，这显示下一个设备的视频 */
            if ((e1.getX() - e2.getX()) > FLING_MOVEMENT_THRESHOLD) {
                //toast("next");
                showNextDeviceListVideo(playerDevice);
            }

            return false;
        }
    }

    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            setCurrentWindow(e);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setCurrentWindow(e);
            stopCurrentPlayList();
            PlayerActivity.m_this.setCurrentFragment("play_video_fragment");
            PlayerActivity.m_this.playSignalVideo(getChoosenDevice(), currentIndex);

            /*if (bFullScreen) {
                resetCurrentWindow();
                bFullScreen = false;
            } else {
                fullCurrentWindow();
                bFullScreen = true;
            }*/

            return true;
        }
    }

    private void setCurrentWindow(MotionEvent e) {
        float x = e.getRawX();
        float y = e.getRawY();
        float w = location[1] - location[0];
        float h = location[3] - location[2];

        Log.d(TAG, "x:" + x + " y:" + y + " w:" + w + " h:" + h);

        layoutMap.get(currentIndex).setBackgroundColor(getResources().getColor(R.color.video_view_normal_border));

        if ((x >= location[0]) && (x < location[0] + w / 2)
                && (y >= location[2]) && (y < location[2] + h / 2)) {
            chosenPlayerDevice = deviceList.get(0);
            currentIndex = 0;
        }

        if ((x >= location[0] + w / 2) && (x < location[0] + w)
                && (y >= location[2]) && (y < location[2] + h / 2)) {
            chosenPlayerDevice = deviceList.get(1);
            currentIndex = 1;
        }

        if ((x >= location[0]) && (x < location[0] + w / 2)
                && (y >= location[2] + h / 2) && (y < location[2] + h)) {
            chosenPlayerDevice = deviceList.get(2);
            currentIndex = 2;
        }

        if ((x >= location[0] + w / 2) && (x < location[0] + w)
                && (y >= location[2] + h / 2) && (y < location[2] + h)) {
            chosenPlayerDevice = deviceList.get(3);
            currentIndex = 3;
        }

        layoutMap.get(currentIndex).setBackgroundColor(getResources().getColor(R.color.video_view_focus_border));
    }

    private void fullCurrentWindow() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            Log.e(TAG, "i is " + i + "current index is " + currentIndex);
            layoutMap.get(i).setVisibility(View.GONE);
        }
        layoutMap.get(currentIndex).setVisibility(View.VISIBLE);
    }

    private void resetCurrentWindow() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            layoutMap.get(i).setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        LayoutInflater layoutInflater = LayoutInflater.from(fragmentView.getContext());
        LinearLayout row = new LinearLayout(mainLayout.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < MAX_WINDOW; i++) {
            RelativeLayout layout = layoutMap.get(i);
            if (null == layout) {
                layout = (RelativeLayout) layoutInflater.inflate(R.layout.play_multi_video_item, null);
                layout.setTag(i);
                //layout.setOnClickListener(this);
                layoutMap.put(i, layout);
            }

            layoutMap.get(i).setBackgroundColor(getResources().getColor(R.color.video_view_normal_border));

            OpenglesView openglesView = (OpenglesView) layout.findViewById(R.id.liveVideoView);
            OpenglesRender openglesRender = renderMap.get(i);
            if (null == openglesRender) {
                openglesRender = new OpenglesRender(openglesView, i);
                openglesRender.setVideoMode(OpenglesRender.VIDEO_MODE_CUSTOM);
                openglesView.setRenderer(openglesRender);
                openglesView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                openglesView.setTag(i);
                openglesView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                openglesView.setLongClickable(true);
                openglesView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        gestureDetector.onTouchEvent(motionEvent);
                        return true;
                    }
                });

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

                renderMap.put(i, openglesRender);
            }

            row.addView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            if ((i + 1) % MAX_WINDOW_BY_ROW != 0) {
                continue;
            }

            mainLayout.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            row = new LinearLayout(mainLayout.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
        }

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_switch_next_video);
    }

    private List<PlayerDevice> getDeviceList(PlayerDevice device) {
        List<PlayerDevice> list = new LinkedList<>();
        List<PlayerDevice> currentList = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        for (int i = 0; i < list.size(); i++) {
            if (device.equals(list.get(i))) {
                for (int j = 0; j < MAX_WINDOW; j++) {
                    if ((i + j) < list.size()) {
                        currentList.add(j, list.get(i + j));
                    } else {
                        currentList.add(j, list.get(i + j - list.size()));
                    }
                }
                break;
            }
        }
        return currentList;
    }

    public void startPlayList() {
        startPlay(deviceList);
    }

    public void autoCyclePlay() {
        showNextDeviceListVideo(this.playerDevice);
    }

    public void startSpeak() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        stopAllVoice();
        stopAllTalk();

        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
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

        if (chosenPlayerDevice.m_talk) return;

        int ret = LibImpl.getInstance().getFuncLib().StartTalkAgent(chosenPlayerDevice.m_dev.getDevId());
        if (0 != ret) {
            toast(R.string.tv_talk_fail_tip);
            return;
        }

        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        chosenPlayerDevice.m_audio.startTalk();
        chosenPlayerDevice.m_talk = true;
    }

    public void stopSpeak() {
        if (null == chosenPlayerDevice) return;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        LibImpl.getInstance().getFuncLib().StopTalkAgent(chosenPlayerDevice.m_dev.getDevId());
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        if (!chosenPlayerDevice.m_talk) return;
        chosenPlayerDevice.m_audio.stopTalk();
        chosenPlayerDevice.m_talk = false;
    }

    private void stopAllTalk() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            if (null == deviceList.get(i) || null == deviceList.get(i).m_audio) continue;
            LibImpl.getInstance().getFuncLib().StopTalkAgent(deviceList.get(i).m_dev.getDevId());
            deviceList.get(i).m_audio.stopTalk();
            deviceList.get(i).m_talk = false;
        }
    }

    public void videoCapture() {
        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        String strDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String fileName = Global.getImageDir() + "/" + playerDevice.m_dev.getDevId() + "_" + strDate + ".jpg";
        boolean bShotOk = chosenPlayerDevice.m_video.startShot(fileName);
        if (bShotOk) {
            toast(R.string.snapshot_succeed);
        } else {
            toast(R.string.snapshot_failed);
        }
    }

    public boolean startHighDefinition() {
        if (null == chosenPlayerDevice) {
            return false;
        }
        LibImpl.stopPlay(0, chosenPlayerDevice);
        chosenPlayerDevice.m_stream_type = Define.MAIN_STREAM_TYPE;
        LibImpl.startPlay(0, chosenPlayerDevice, chosenPlayerDevice.m_stream_type, chosenPlayerDevice.m_frame_type);
        return true;
    }

    public void stopHighDefinition() {
        if (null == chosenPlayerDevice) {
            return;
        }
        LibImpl.stopPlay(0, chosenPlayerDevice);
        chosenPlayerDevice.m_stream_type = Define.SUB_STREAM_TYPE;
        LibImpl.startPlay(0, chosenPlayerDevice, chosenPlayerDevice.m_stream_type, chosenPlayerDevice.m_frame_type);
    }

    public boolean startVideoRecord() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return false;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        String filePath = Global.getVideoDir() + "/" + chosenPlayerDevice.m_dev.getDevId();
        File dir = new File(filePath);
        if (!(dir.exists())) dir.mkdirs();
        int ret = LibImpl.getInstance().getFuncLib().StartRecordAgent(chosenPlayerDevice.m_dev.getDevId(), filePath, EtcInfo.PER_RECORD_TIME_LENGTH);
        if (0 != ret) {
            toast(R.string.fvu_tip_start_record_failed);
            return false;
        }

        chosenPlayerDevice.m_record = true;

        return true;
    }

    public void stopVideoRecord() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            if (null == this.deviceList.get(i) || !this.deviceList.get(i).m_playing) {
                //toast(R.string.before_open_video_preview);
                return;
            }

            TPS_AddWachtRsp rsp = this.deviceList.get(i).m_add_watch_rsp;
            if (null == rsp) {
                toast(R.string.tv_video_wait_video_stream_tip);
                return;
            }

            LibImpl.getInstance().getFuncLib().StopRecordAgent(this.deviceList.get(i).m_dev.getDevId());
            this.deviceList.get(i).m_record = false;
        }
    }

    private void stopAllVoice() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            if (null == deviceList.get(i) || null == deviceList.get(i).m_audio) continue;
            deviceList.get(i).m_audio.stopOutAudio();
            deviceList.get(i).m_voice = false;
        }
    }

    public void startRecordPlayBack() {
        if (null == chosenPlayerDevice) return;
        if (!chosenPlayerDevice.is_p2p_replay()) {
            toast(R.string.tv_not_support_front_end_record);
            return;
        }

        /* 开启回放前先关闭正在播放的设备 */
        stopCurrentPlayList();

        Intent it = new Intent(this.getActivity(), FrontEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, chosenPlayerDevice.m_dev.getDevId());
        this.startActivity(it);
        this.getActivity().finish();
    }

    public boolean startVideoSound() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return false;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
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

        if (null == chosenPlayerDevice.m_add_watch_rsp || !chosenPlayerDevice.m_add_watch_rsp.hasAudio()) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        stopAllVoice();
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        chosenPlayerDevice.m_audio.startOutAudio();
        chosenPlayerDevice.m_voice = true;
        toast(R.string.fvu_tip_open_voice);

        return true;
    }

    public void stopVideoSound() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            //toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
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

        if (!chosenPlayerDevice.m_voice) return;
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        chosenPlayerDevice.m_audio.stopOutAudio();
        chosenPlayerDevice.m_voice = false;
        toast(R.string.fvu_tip_close_voice);
    }

    private boolean startPlay(List<PlayerDevice> devList) {
        RelativeLayout layout;
        if (null == devList) {
            Log.e(TAG, "the device list is null!!!");
            return false;
        }

        for (int i = 0; i < MAX_WINDOW; i++) {
            layout = layoutMap.get(i);

            devList.get(i).m_audio = new AudioPlayer(currentIndex);
            devList.get(i).m_video = renderMap.get(i);
            Log.d(TAG, "====>device list is" + devList.toString() + " i is " + i + " device is " + devList.get(i).getDeviceName());
            devList.get(i).m_video.mIsStopVideo = false;

            int ret = LibImpl.startPlay(0, devList.get(i), devList.get(i).m_stream_type, devList.get(i).m_frame_type);
            if (ret == 0) {
                devList.get(i).m_online = true;
                devList.get(i).m_playing = false;
                setVideoInfo(i, T(R.string.tv_video_req_tip));
            } else {
                String selfID = "";
                if (LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(devList.get(i).m_dev.getDevId())) != null) {
                    selfID = LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(devList.get(i).m_dev.getDevId())).getNotifyStr();
                }

                Log.i("DeviceNotifyInfo", "DeviceNotifyInfo ary:" + LibImpl.mDeviceNotifyInfo + ".");
                selfID = (isNullStr(selfID)) ? "" : ("(" + selfID + ")");
                setVideoInfo(i, ConstantImpl.getTPSErrText(ret, false) + selfID);
                toast(ConstantImpl.getTPSErrText(ret, false) + selfID);
                continue;
            }

            devList.get(i).m_play = true;
            devList.get(i).m_view_id = i;

            View view = layout.findViewById(R.id.tvLiveInfo);
            view.setVisibility(View.VISIBLE);
            view = devList.get(i).m_video.getSurface();
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setVisibility(View.VISIBLE);
        }

        fragmentView.setAnimation(animation);
        layoutMap.get(currentIndex).setBackgroundColor(getResources().getColor(R.color.video_view_focus_border));

        return true;
    }

    public void stopPlayList() {
        RelativeLayout layout;
        View view;
        PlayerActivity.m_this.resetWidget();
        stopVideoRecord();
        stopVideoSound();
        for (int i = 0; i< MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
            layout = layoutMap.get(i);
            view = layout.findViewById(R.id.liveVideoView);
            view.setBackgroundColor(Color.BLACK);
            view = layout.findViewById(R.id.tvLiveInfo);
            view.setVisibility(View.GONE);
            this.deviceList.get(i).m_video.mIsStopVideo = true;
            layout.invalidate();
        }
    }

    private void stopCurrentPlayList() {
        PlayerActivity.m_this.resetWidget();
        stopVideoRecord();
        stopVideoSound();
        for (int i = 0; i < MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
        }
    }

    private void setCurrentDevice(PlayerDevice device) {
        this.playerDevice = device;
    }

    private PlayerDevice getChoosenDevice() {
        return this.chosenPlayerDevice;
    }

    private PlayerDevice getPlayerDeviceByIndex(PlayerDevice device, int index) {
        List<PlayerDevice> list = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        for (int i = 0; i < list.size(); i++) {
            if (device.equals(list.get(i))) {
                if (i - index >= 0) {
                    return list.get(i - index);
                } else {
                    return list.get(i + list.size() - index);
                }
            }
        }

        Log.e(TAG, "the device is not in the device list err!!!");
        return device;
    }

    private void showPreviousDeviceListVideo(PlayerDevice device) {
        /* 恢复PlayerActivity的button状态 */
        PlayerActivity.m_this.resetWidget();

        /* 停止所有正在录制的视频 */
        stopVideoRecord();
        stopVideoSound();
        stopHighDefinition();

        /* 停止当前正在播放的设备列表 */
        RelativeLayout layout;
        for (int i = 0; i < MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
            layout = layoutMap.get(i);
            layout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
            this.deviceList.get(i).m_video.mIsStopVideo = true;
            this.deviceList.get(i).m_video = null;
        }

        List<PlayerDevice> list = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        for (int i = 0; i < list.size(); i++) {
            if (device.equals(list.get(i))) {
                if ((i - MAX_WINDOW) >= 0) {
                    setCurrentDevice(list.get(i -  MAX_WINDOW));
                } else {
                    setCurrentDevice(list.get(i + list.size() - MAX_WINDOW));
                }
                break;
            }
        }

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_switch_prev_video);
        this.deviceList = getDeviceList(this.playerDevice);
        Boolean bRet = startPlay(this.deviceList);
        for (int i = 0; i < MAX_WINDOW; i++) {
            layout = layoutMap.get(i);
            layout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        }

        if (!bRet) {
            Log.e(TAG, "Start previous device err!!!");
        }
    }

    private void showNextDeviceListVideo(PlayerDevice device) {
        /* 恢复PlayerActivity的button状态 */
        PlayerActivity.m_this.resetWidget();

        /* 停止所有正在录制的视频 */
        stopVideoRecord();
        stopVideoSound();
        stopHighDefinition();

        /* 停止当前正在播放的设备列表 */
        RelativeLayout layout;
        for (int i = 0; i < MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
            layout = layoutMap.get(i);
            layout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
            this.deviceList.get(i).m_video.mIsStopVideo = true;
            this.deviceList.get(i).m_video = null;
        }

        List<PlayerDevice> list = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        for (int i = list.size() - 1; i >= 0; i--) {
            if (device.equals(list.get(i))) {
                if ((i + MAX_WINDOW) <= (list.size() - 1)) {
                    setCurrentDevice(list.get(i + MAX_WINDOW));
                } else {
                    setCurrentDevice(list.get(i + MAX_WINDOW - list.size()));
                }
                break;
            }
        }

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_switch_next_video);
        this.deviceList = getDeviceList(this.playerDevice);
        Boolean bRet = startPlay(this.deviceList);
        for (int i = 0; i < MAX_WINDOW; i++) {
            layout = layoutMap.get(i);
            layout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        }

        if (!bRet) {
            Log.e(TAG, "Start next device err!!!");
        }
    }


    public void setVideoInfo(String devID, final String msg) {
        Log.d("setTipText", devID + "," + msg);
        int index = LibImpl.getInstance().getIndexByDeviceID(devID);
        if ((index < 0) || (index > MAX_WINDOW - 1)) {
            return;
        }
        setVideoInfo(index, msg);
    }

    public void setVideoInfo(final int index, final String msg) {
        RelativeLayout layout = layoutMap.get(index);
        MarqueeTextView v = (MarqueeTextView) layout.findViewById(R.id.tvLiveInfo);
        //v.setVisibility(View.VISIBLE);
        if (null == msg) {
            v.setVisibility(View.GONE);
        } else if (T(R.string.tv_video_play_tip).compareToIgnoreCase(msg) == 0) { //playing
            StringBuffer msgBuf = new StringBuffer();
            String _devName = deviceList.get(index).getDeviceName();
            msgBuf.append("[").append(_devName).append("]");
            String _msg = msgBuf.toString();
            v.setText(_msg);
            deviceList.get(index).m_tipInfo = msg;
        } else {
            StringBuffer msgBuf = new StringBuffer();
            if (!isNullStr(deviceList.get(index).m_dev.getDevId())) {
                //加载别名
                String _devName = deviceList.get(index).getDeviceName();
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
                    //stopTalk(dev, false);
                    return true;
                }

                if (ts.getnResult() != 0) {
                    toast(R.string.tv_talk_fail_tip, ts.getnResult());
                    //stopTalk(dev, false);
                }

                if (SDK_CONSTANT.AUDIO_TYPE_G711.compareToIgnoreCase(new String(audioParm.getAudio_encoder()).trim()) != 0) {
                    toast(R.string.tv_talk_fail_illegal_format_tip);
                    //stopTalk(dev, false);
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
                msgObj = (LibImpl.MsgObject) msg.obj;
                if (null != msgObj.reserveObj) {
                    setTipText(msgObj.devID, msgObj.recvObj, (String) msgObj.reserveObj);
                } else {
                    setTipText(msgObj.devID, msgObj.recvObj);
                }
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
                //onMsgP2pOffline(tn);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_NotifyInfo tni = (TPS_NotifyInfo) msgObj.recvObj;
                //onMsgP2pNvrOffline(tni);
                break;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG:
                msgObj = (LibImpl.MsgObject) msg.obj;
                List<NetSDK_UserAccount> lst = (List<NetSDK_UserAccount>) msgObj.recvObj;
                //CheckDefaultUserPwd(LibImpl.getInstance().getPlayerDevice(msgObj.devID));
                return false;
            case Define.MSG_RECEIVER_MEDIA_FIRST_FRAME:
                //onRecvFirstFrame((PlayerDevice) msg.obj);
                return true;
        }

        return false;
    }

    private void onAddWatchResp(TPS_AddWachtRsp ts) {
        final String devId = new String(ts.getSzDevId()).trim();
        if (ts.getnResult() == 0) {//视频请求成功
            setTipText(devId, R.string.tv_video_req_succeed_tip);
        } else {
            setTipText(devId, R.string.tv_video_req_fail_tip, ts.getnResult() + "");
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

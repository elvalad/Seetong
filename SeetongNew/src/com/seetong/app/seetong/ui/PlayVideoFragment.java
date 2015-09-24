package com.seetong.app.seetong.ui;

import android.graphics.Color;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
        startPlay(playerDevice);
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

            /* ���GestureDetector��⵽�û����󻬶�������ʾ��һ���豸����Ƶ */
            if ((e2.getX() -e1.getX()) > FLING_MOVEMENT_THRESHOLD) {
                //toast("previous");
                showPreviousDeviceVideo(playerDevice);
            }

            /* ���GestureDetector��⵽�û����һ���������ʾ��һ���豸����Ƶ */
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
            stopCurrentPlay();
            PlayerActivity.m_this.setCurrentFragment("play_multi_video_fragment");
            PlayerActivity.m_this.playMultiVideo(playerDevice, currentIndex);
            return true;
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
    }

    public void startPlay() {
        startPlay(playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        mainLayout.findViewById(R.id.tvLiveInfo).setVisibility(View.VISIBLE);
    }

    public void autoCyclePlay() {
        showNextDeviceVideo(this.playerDevice);
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
        } else {
            toast(R.string.snapshot_failed);
        }
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
        if (0 != ret) {
            toast(R.string.fvu_tip_start_record_failed);
            return false;
        }

        playerDevice.m_record = true;

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

        return true;
    }

    public void stopVideoSound() {
        if (null == playerDevice || !playerDevice.m_playing) {
            //toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = playerDevice.m_add_watch_rsp;
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

        dev.m_audio = new AudioPlayer(currentIndex);
        dev.m_video = this.openglesRender;
        dev.m_video.mIsStopVideo = false;
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

        dev.m_play = true;
        dev.m_view_id = 0;

        View view = mainLayout.findViewById(R.id.tvLiveInfo);
        view.setVisibility(View.VISIBLE);
        view = dev.m_video.getSurface();
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setVisibility(View.VISIBLE);

        return true;
    }

    public void stopPlay() {
        PlayerActivity.m_this.resetWidget();
        /* ֹͣ��Ƶ����֮ǰ��ֹͣ¼�� */
        stopVideoRecord();
        stopVideoSound();
        LibImpl.stopPlay(0, playerDevice);
        View view = mainLayout.findViewById(R.id.liveVideoView);
        view.setBackgroundColor(Color.BLACK);
        view = mainLayout.findViewById(R.id.tvLiveInfo);
        view.setVisibility(View.GONE);
        playerDevice.m_video.mIsStopVideo = true;
        mainLayout.invalidate();
    }

    private void stopCurrentPlay() {
        PlayerActivity.m_this.resetWidget();
         /* ֹͣ��Ƶ����֮ǰ��ֹͣ¼�� */
        stopVideoRecord();
        stopVideoSound();
        LibImpl.stopPlay(0, playerDevice);
    }

    public PlayerDevice getCurrentDevice() {
        return this.playerDevice;
    }

    private void setCurrentDevice(PlayerDevice device) {
        this.playerDevice = device;
    }

    private void showPreviousDeviceVideo(PlayerDevice device) {
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

        /* �ָ�PlayerActivity��button״̬ */
        PlayerActivity.m_this.resetWidget();

         /* ֹͣ��Ƶ����֮ǰ��ֹͣ¼�� */
        stopVideoRecord();
        stopVideoSound();

        /* ֹͣ��ǰ���ڲ��ŵ��豸����ʼ������һ���豸 */
        LibImpl.stopPlay(0, device);

        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
        device.m_video.mIsStopVideo = true;
        device.m_video = null;

        Boolean bRet = startPlay(this.playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        if (!bRet) {
            Log.e(TAG, "Start previous device err!!!");
        }
    }

    private void showNextDeviceVideo(PlayerDevice device) {
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

        /* �ָ�PlayerActivity��button״̬ */
        PlayerActivity.m_this.resetWidget();

        /* ֹͣ��Ƶ����֮ǰ��ֹͣ¼�� */
        stopVideoRecord();
        stopVideoSound();

        /* ֹͣ��ǰ���ڲ��ŵ��豸����ʼ������һ���豸 */
        LibImpl.stopPlay(0, device);

        //fragmentView.setBackgroundColor(Color.BLACK);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
        device.m_video.mIsStopVideo = true;
        device.m_video = null;

        Boolean bRet = startPlay(this.playerDevice);
        mainLayout.findViewById(R.id.liveVideoView).setVisibility(View.VISIBLE);
        Log.d(TAG, "=====>start play the next device is " + this.playerDevice.m_dev.getDevId());
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

        if (null == msg) {
            v.setVisibility(View.GONE);
        } else if (T(R.string.tv_video_play_tip).compareToIgnoreCase(msg) == 0) { //playing
            StringBuffer msgBuf = new StringBuffer();
            String _devName = playerDevice.getDeviceName();
            msgBuf.append("[").append(_devName).append("]");
            String _msg = msgBuf.toString();
            v.setText(_msg);
            playerDevice.m_tipInfo = msg;
        } else {
            StringBuffer msgBuf = new StringBuffer();
            if (!isNullStr(playerDevice.m_dev.getDevId())) {
                //���ر���
                String _devName = playerDevice.getDeviceName();
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
        if (ts.getnResult() == 0) {//��Ƶ����ɹ�
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

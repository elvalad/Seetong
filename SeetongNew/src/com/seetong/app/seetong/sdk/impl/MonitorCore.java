package com.seetong.app.seetong.sdk.impl;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.audio.AudioBuffer;
import com.android.audio.AudioPlayer;
import com.android.opengles.FrameBuffer;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.Device;
import ipc.android.sdk.com.*;
import ipc.android.sdk.impl.FunclibAgent;
import ipc.android.sdk.impl.PlayCtrlAgent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Created by Administrator on 2014-06-12.
 */
public class MonitorCore {
    private DirectConnectCB m_cb = new DirectConnectCB();
    private FunclibAgent m_func = FunclibAgent.getInstance();
    private PlayCtrlAgent m_play = PlayCtrlAgent.getInstance();

    private Handler m_handler;

    public List<Device> m_local_dev_list = new ArrayList<>();
    public Map<Integer, PlayerDevice> m_device_map = new HashMap<>();
    public Map<String, List<PlayerDevice>> m_cat_dev_map = new HashMap<>();
    Map<Integer, PlayerDevice> m_play_entry_map = new HashMap<>();
    Map<Integer, Integer> m_login_index_map = new HashMap<>();
    Map<Integer, Integer> m_play_index_entry_map = new HashMap<>();
    Map<Integer, Integer> m_port_id_map = new HashMap<>();

    private static MonitorCore inst;

    private MonitorCore() {
        /*m_play.setIPlayCtrlAgentCB(m_cb);
        m_func.setIDirectConnectCB(m_cb);
        m_func.setDirectConnectCallbackFunc();*/
    }

    public static MonitorCore instance() {
        if (null == inst) {
            inst = new MonitorCore();
            inst.loadDevice();
        }
        return inst;
    }

    public FunclibAgent getFuncLib() {
        return m_func;
    }

    public void loadDevice() {
        m_local_dev_list = Device.findAll();
        for (Device d : m_local_dev_list) {
            PlayerDevice dev = new PlayerDevice();
            dev.m_entry = new NetSDK_IPC_ENTRY();
            dev.m_entry.setIpc_sn(String.valueOf(System.currentTimeMillis()));
            dev.m_entry.setDeviceType(Define.DEVICE_TYPE_CAMERA);
            NetSDK_LANConfig lc = new NetSDK_LANConfig();
            lc.setIPAddress(d.getIp());
            dev.m_entry.setLanCfg(lc);

            NetSDK_StreamAccessConfig sc = new NetSDK_StreamAccessConfig();
            sc.setPtzPort(d.getPtzPort());
            sc.setVideoPort(d.getVideoPort());
            dev.m_entry.setStreamCfg(sc);

            NetSDK_UserConfig uc = new NetSDK_UserConfig();
            NetSDK_UserAccount[] u = new NetSDK_UserAccount[1];
            u[0] = new NetSDK_UserAccount();
            u[0].setUserName(d.getUser());
            u[0].setPassword(d.getPwd());
            uc.setAccounts(u);
            dev.m_entry.setUserCfg(uc);
            addDeviceToList(dev);
        }
    }

    private void addDeviceToList(PlayerDevice dev) {
        NetSDK_IPC_ENTRY entry = dev.m_entry;
        if (m_cat_dev_map.get(entry.getDeviceType()) == null) {
            List<PlayerDevice> devList = new ArrayList<>();
            devList.add(dev);
            m_cat_dev_map.put(entry.getDeviceType(), devList);
        } else {
            boolean found = false;
            for (PlayerDevice d : m_cat_dev_map.get(entry.getDeviceType())) {
                found = d.m_entry.getIpc_sn().compareTo(entry.getIpc_sn()) == 0;
                if (found) break;
                found = d.m_entry.getLanCfg().getIPAddress().compareTo(entry.getLanCfg().getIPAddress()) == 0;
                if (found) break;
            }

            if (!found) m_cat_dev_map.get(entry.getDeviceType()).add(dev);
        }
    }

    public void setHandler(Handler h) {
        m_handler = h;
    }

    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        if (m_handler == null) return;
        Message msg = Message.obtain();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        m_handler.sendMessage(msg);
    }

    public int msgRspCB(int nMsgType, byte[] pData, int nDataLen) {
        switch (nMsgType) {
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_OK:
                if (pData != null && nDataLen == UserRight.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    UserRight ts = (UserRight) UserRight.createObjectByByteBuffer(byteBuffer);
                    Log.i("MSG", "msgRspCallBack-->UserRight@" + ts.toString());
                }
                break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED:
                break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DEV_DATA:
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH:
                //onMsgRspAddWatch(pData, nDataLen);
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_SELF_ID:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    String devID = new String(ni.getSzDevId()).trim();
                    String errorText = new String(ni.getSzInfo()).trim();
                    // remove "_test"
                    errorText = errorText.substring(0, errorText.length() - 5);
                    int nResult = ni.getnResult();
                    ni.setSzInfo(errorText.getBytes());
                }
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    String devID = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();
                }
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_TALK:
                break;
            case SDK_CONSTANT.TPS_MSG_RSP_TALK_CLOSE:
                Log.w("talk", "TPS_MSG_RSP_TALK_CLOSE-->" + nDataLen);
                break;
            case SDK_CONSTANT.TPS_MSG_REC_STOP:
                break;
            case SDK_CONSTANT.TPS_MSG_ALARM:
                break;
            default:
                break;
        }
        return 0;
    }

    class DirectConnectCB implements FunclibAgent.IDirectConnectCB, PlayCtrlAgent.IPlayCtrlAgentCB {
        private static final String TAG = "DirectConnectCB";

        @Override
        public int SearchIPC(int nEventCode, int index, NetSDK_IPC_ENTRY entry) {
            PlayerDevice dev = new PlayerDevice();
            dev.m_entry = entry;
            m_device_map.put(index, dev);
            addDeviceToList(dev);
            Device d = new Device();
            d.setIp(entry.getLanCfg().getIPAddress());
            d.setPtzPort(entry.getStreamCfg().getPtzPort());
            d.setVideoPort(entry.getStreamCfg().getVideoPort());
            if (entry.getUserCfg().getAccounts().length > 0) {
                d.setUser(entry.getUserCfg().getAccounts()[0].getUserName());
                d.setPwd(entry.getUserCfg().getAccounts()[0].getPassword());
            }

            boolean ret = d.save();
            sendMessage(Define.MSG_SEARCH_DEVICE_RESP, 0, 0, null);
            return 0;
        }

        @Override
        public int StatusEvent(int lUser, int nStateCode, String pResponse) {
            sendMessage(Define.MSG_STATUS_EVENT, lUser, nStateCode, pResponse);
            return 0;
        }

        @Override
        public int AUXResponse(int lUser, int nType, String pResponse) {
            //sendMyToast("AUXResponse，lUser=" + lUser + ",nType=" + nType + "," + pResponse);
            return 0;
        }

        @Override
        public int VoiceData(int lVoiceComHandle, String pRecvDataBuffer, int dwBufSize, byte byAudioFlag, FRAME_EXTDATA pUser) {
            return 0;
        }

        @Override
        public int RealData(int lRealHandle, int dwDataType, byte[] pBuffer, int dwBufSize, FRAME_EXTDATA pExtData) {
            if (dwDataType == 0) {// 接收视频数据，H264格式
                if (dwBufSize <= 0 || null == pExtData) return -1;
                int isKey = pExtData.getbIsKey();
                PlayerDevice dev = getPlayerDevice(m_play_index_entry_map.get(lRealHandle));
                if (null == dev) return -1;
                //接收数据交给底层库解码，解码回调函数为：decDataCB（如需要自己处理，可不调用此函数）
                m_play.InputVideoDataAgent(dev.m_port_id, pBuffer, dwBufSize, isKey, (int) pExtData.getTimestamp());
            } else if (dwDataType == 1) {// 接收音频数据，G711格式
                if (dwBufSize <= 0 || null == pExtData) return -1;
                PlayerDevice dev = getPlayerDevice(m_play_index_entry_map.get(lRealHandle));
                if (null == dev) return -1;
                //接收数据交给底层库解码，解码回调函数为：decDataCB（如需要自己处理，可不调用此函数）
                m_play.InputAudioDataAgent(dev.m_port_id, pBuffer, dwBufSize, (int) pExtData.getTimestamp());
            } else if (dwDataType == 2) {
                PlayerDevice dev = getPlayerDevice(m_play_index_entry_map.get(lRealHandle));
                if (null == dev) return -1;
                if (m_port_id_map.get(dev.m_port_id) != null) return 0;
                //解码参数
                ByteBuffer byteBuffer = ByteBuffer.allocate(dwBufSize);
                byteBuffer.order(ByteOrder.nativeOrder());
                byteBuffer.put(pBuffer, 0, dwBufSize);
                byteBuffer.rewind();
                NetSDK_STREAM_AV_PARAM param = (NetSDK_STREAM_AV_PARAM) NetSDK_STREAM_AV_PARAM.createObjectByByteBuffer(byteBuffer);
                int prot = m_play.GetProtAgent();
                Log.i(TAG, "VideoParam=" + param.getVideoParam().toString());
                TPS_VIDEO_PARAM vp = new TPS_VIDEO_PARAM();
                vp.setStream_index(0);
                vp.setVideo_encoder(param.getVideoParam().getCodec().getBytes());
                vp.setWidth(param.getVideoParam().getWidth());
                vp.setHeight(param.getVideoParam().getHeight());
                vp.setFramerate(param.getVideoParam().getFramerate());
                vp.setIntraframerate(param.getVideoParam().getFramerate() * 4);
                vp.setBitrate(param.getVideoParam().getBitrate());
                vp.setConfig(param.getVideoParam().getVol_data().getBytes());
                vp.setConfig_len(param.getVideoParam().getVol_length());
                byte[] videoParm = vp.objectToByteBuffer(ByteOrder.nativeOrder()).array();
                int ret = m_play.OpenStreamAgent(prot, videoParm, videoParm.length, 0/*0:视频 1:音频*/, 25 * 1/*最大缓冲帧数*/);
                if (0 != ret) {
                    Log.i(TAG, "Video OpenStreamAgent failed.");
                    return -1;
                }

                //判断是否有音频，如果有音频才进行打开音频流及配置语音参数
                if (param.getbHaveAudio() != 0) {
                    Log.i(TAG, "AudioParam=" + param.getAudioParam().toString());
                    //设置音频流参数（摄像机端要配置音频编码方式为G711，其它格式暂不支持）
                    TPS_AUDIO_PARAM ap = new TPS_AUDIO_PARAM();
                    ap.setStream_index(0);
                    ap.setAudio_encoder(param.getAudioParam().getCodec().getBytes());
                    ap.setSamplerate(param.getAudioParam().getSamplerate());
                    ap.setSamplebitswitdh(param.getAudioParam().getBitspersample());
                    ap.setChannels(param.getAudioParam().getChannels());
                    ap.setBitrate(param.getAudioParam().getBitrate());
                    ap.setFramerate(param.getAudioParam().getFramerate());
                    byte[] audioParm = ap.objectToByteBuffer(ByteOrder.nativeOrder()).array();
                    ret = m_play.OpenStreamAgent(prot, audioParm, audioParm.length, 1/*0:视频 1:音频*/, 20/*最大缓冲帧数*/);
                    if (0 != ret) {
                        Log.i(TAG, "Audio OpenStreamAgent failed.");
                        return -1;
                    }

                    // 根据摄像机音频参数，配置AudioPlayer的参数
                    AudioPlayer audioPlayer = m_play_entry_map.get(0).m_audio;
                    if (audioPlayer == null) return -1;
                    AudioPlayer.MyAudioParameter audioParameter = new AudioPlayer.MyAudioParameter(param.getAudioParam().getSamplerate(), param.getAudioParam().getChannels(), param.getAudioParam().getBitspersample());
                    audioPlayer.initAudioParameter(audioParameter);
                }

                //0表示decDataCB解码出来的为yuv数据, 非0表示对应位数的rgb数据（支持的位数有：16、24、32）
                m_play.PlayAgent(prot, 0);
                //记录端口句柄和设备ID间的映射关系
                dev.m_port_id = prot;
                m_port_id_map.put(prot, lRealHandle);
            }
            return 0;
        }

        @Override
        public int ReplayData(int i, int i1, byte[] bytes, int i2, FRAME_EXTDATA frame_extdata) {
            return 0;
        }

        @Override
        public int PlayActionEvent(int lUser, int nType, int nFlag, String pData) {
            return 0;
        }

        @Override
        public int Exception(int dwType, int lUserID, int lHandle) {
            return 0;
        }

        @Override
        public int EncodeAudio(int lType, int lPara1, int lPara2) {
            return 0;
        }

        @Override
        public int SerialData(int lUser, byte[] pRecvDataBuffer, int dwBufSize) {
            return 0;
        }

        @Override
        public int RecFileName(int lRealHandle, byte[] pRecFileNameBuf, int dwBufSize) {
            return 0;
        }

        @Override
        public int decDataCB(int nPort, byte[] pDecData, int nSize, int nWidth, int nHeight, int nTimestamp, int nType, int nFrameRate, int nIsVideo, byte[] pUser) {
            if (nSize <= FRAME_INFO.SIZE) return -1;
            Log.i(TAG, "decDataCB-->data is success:nSize=" + nSize);
            // 处理解码后的媒体数据
            if (nIsVideo == 1 && nWidth > 0 && nHeight > 0) {//处理视频数据(yuv或rgb数据)
                FrameBuffer buf = new FrameBuffer(nWidth, nHeight);
                PlayerDevice dev = getPlayerDevice(m_play_index_entry_map.get(m_port_id_map.get(nPort)));
                if (null == dev) return -1;
                if (null == dev.m_video) {
                    Log.e(TAG, "_glRender is null");
                    return - 1;
                }

                // pDecData:TPS_VIDEO_FRAME_HEADER(16)+y+v+u
                int frameSize = nSize;
                buf.fData = ByteBuffer.wrap(pDecData, 0, frameSize).array();
                dev.m_video.updateView(buf);
                dev.m_playing = true;
            } else if (nIsVideo == 0) {//处理音频数据(pcm数据)
                /*if(mAudioPlayerAry[0] != null){
                    int audioLen = nSize - TPS_AUDIO_PARAM.SIZE;
                    byte[] audioData = ByteBuffer.wrap(pDecData,TPS_AUDIO_PARAM.SIZE,audioLen).array();
                    AudioBuffer buf = new AudioBuffer(audioData, audioLen);
                    mAudioPlayerAry[0].addToBuf(buf);
                }*/
                PlayerDevice dev = getPlayerDevice(m_play_index_entry_map.get(m_port_id_map.get(nPort)));
                if (null == dev) return -1;
                AudioBuffer audioBuf = new AudioBuffer(pDecData, nSize);
                dev.m_audio.addToBuf(audioBuf);
            }

            return 0;
        }
    }


    public int StartSearchDev() {
        return m_func.StartSearchDev();
    }

    public int StopSearchDev() {
        return m_func.StopSearchDev();
    }

    public PlayerDevice getPlayerDevice(int index) {
        return m_play_entry_map.get(index);
    }

    public PlayerDevice getPlayerDeviceByLoginId(int id) {
        Integer index = getIndexByLoginId(id);
        if (null == index) return null;
        return m_play_entry_map.get(index);
    }

    public void setPlayerDevice(int index, PlayerDevice dev) {
        m_play_entry_map.put(index, dev);
    }

    public int loginDev(int index, PlayerDevice dev) {
        String ip = dev.m_entry.getLanCfg().getIPAddress();
        int port = dev.m_entry.getStreamCfg().getPtzPort();
        NetSDK_UserAccount account = dev.m_entry.getUserCfg().getAccounts()[0];
        String user = account.getUserName();
        String pwd = account.getPassword();
        int ret = m_func.LoginDev(ip, port, user, pwd);
        if (ret <= 0) return ret;
        dev.m_login_id = ret;
        m_login_index_map.put(ret, index);
        setPlayerDevice(index, dev);
        return ret;
    }

    public int logoutDev(int login_id) {
        return m_func.LogOutDev(login_id);
    }

    public int realPlay(int index, int login_id) {
        PlayerDevice dev = getPlayerDevice(m_login_index_map.get(login_id));
        if (null == dev) return 0;
        NetSDK_USER_VIDEOINFO info = new NetSDK_USER_VIDEOINFO();
        info.setbIsTcp(1);
        NetSDK_IPC_ENTRY entry = dev.m_entry;
        info.setnVideoPort(entry.getStreamCfg().getVideoPort());
        info.setnVideoChannle(1);
        int ret = m_func.RealPlay(login_id, info);
        if (ret == 0) return ret;
        dev.m_play = true;
        dev.m_play_id = ret;
        m_play_index_entry_map.put(ret, index);
        return ret;
    }

    public int stopRealPlay(int playId) {
        return m_func.StopRealPlay(playId);
    }

    public int startPlay(int index, PlayerDevice dev) {
        int ret = loginDev(index, dev);
        if (ret <= 0) return ret;
        return ret;
    }

    public int stopPlay(PlayerDevice dev) {
        int ret = stopRealPlay(dev.m_play_id);
        return ret;
    }

    public Integer getIndexByLoginId(int login_id) {
        return m_login_index_map.get(login_id);
    }

    public Integer getIndexByPlayId(int play_id) {
        return m_play_index_entry_map.get(play_id);
    }

    public int ptzControl(int lUserId, int nPtzCmd, int nHSpeed, int nVSpeed) {
        return FunclibAgent.getInstance().PTZControl(lUserId, nPtzCmd, nHSpeed, nVSpeed);
    }









    public int logoutAll() {
        int isOK = m_func.LogoutAgent();
        return isOK;
    }

    public void generateSnaphost(List<PlayerDevice> lst) {
        //m_func.LoginAgent("yinql", "123456", EtcInfo.DEFAULT_P2P_URL, (short)80);
        /*final List<Device> lstDev = lst;
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Device dev : lstDev) {
                    if (dev.getOnLine() != Device.ONLINE) continue;
                    generateSnaphost(dev);
                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t.run();*/
    }

    private void generateSnaphost(PlayerDevice dev) {
        /*if (null == dev) return;
        String devId = dev.getDevId();
        if (!addWatch(devId, 1, 1)) return;*/
    }
}

package com.seetong5.app.seetong.sdk.impl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.audio.AudioBuffer;
import com.android.audio.AudioPlayer;
import com.android.opengles.FrameBuffer;
import com.android.opengles.OpenglesRender;
import com.android.system.MediaPlayer;
import com.android.system.MessageNotification;
import com.android.utils.NetworkUtils;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.model.*;
import com.seetong5.app.seetong.tools.Event;
import com.seetong5.app.seetong.ui.BaseActivity;
import com.seetong5.app.seetong.ui.MainActivity2;
import ipc.android.sdk.com.*;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.impl.FunclibAgent;
import ipc.android.sdk.impl.PlayCtrlAgent;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by csw on 2014/5/5.
 */
public class LibImpl implements FunclibAgent.IFunclibAgentCB, PlayCtrlAgent.IPlayCtrlAgentCB, FunclibAgent.ILogCB {
    private static String TAG = "LibImpl";
    private static LibImpl m_impl = null;

    private boolean m_exit = true;
    public boolean m_stop_play = false;
    private boolean m_stop_snapshot = false;
    private boolean m_fc_inited = false;

    private LibImpl() {
        s_func = FunclibAgent.getInstance();
        s_pca = PlayCtrlAgent.getInstance();
        s_func.setIFunclibAgentCB(this);
        s_func.setILogCB(this);
        s_pca.setIPlayCtrlAgentCB(this);
        m_mediaDataThread.start();
        m_download_thread.start();
        m_change_pwd_thread.start();
    }

    public static LibImpl getInstance() {
        if (null == m_impl) m_impl = new LibImpl();
        m_impl.initFuncLib();
        return m_impl;
    }

    public void init() {
        m_exit = false;
        m_stop_play = false;
        m_stop_snapshot = false;
    }

    public void logoff(boolean exit) {
        if (m_exit) return;
        m_exit = true;
        stopAll(exit);
        logoutAll();
        if (!exit) return;

        s_func.free();
        s_pca.free();
        m_fc_inited = false;
    }

    private synchronized void initFuncLib() {
        if (m_fc_inited || m_exit) return;
        Thread thread = new Thread() {
            public void run() {
                Global.getNetType();
                int ret = s_func.initExAgent(Global.m_mobile_net_sub_type_2);
                m_fc_inited = (0 == ret);
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Global.getNetType();
                int ret = s_func.initExAgent(Global.m_mobile_net_sub_type_2);
                m_fc_inited = (0 == ret);
            }
        }).start();*/
    }

    public static int startPlay(int index, PlayerDevice dev, int nStreamNo, int nFrameType) {
        if (null == dev) return -1;
        String devId = dev.m_dev.getDevId();
        dev.m_first_frame = false;
        int type = dev.m_force_forward ? 1 : 0;
        int ret = addWatch(devId, nStreamNo, nFrameType, 0);
        if (ret == SDK_CONSTANT.ERR_P2P_DISCONNECTED) ret = 0;
        if (ret != 0) return ret;

        if (m_index_id_map.containsValue(devId)) {
            Iterator<Map.Entry<Integer, String>> itr = m_index_id_map.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Integer, String> entry = itr.next();
                if (entry.getValue().equals(devId)) itr.remove();
            }
        }

        m_index_id_map.put(index, devId);
        m_devMap.put(devId, dev);
        dev.m_play = true;
        return ret;
    }

    public static int stopPlay(int index, PlayerDevice dev) {
        //LibImpl.getInstance().removePlayerDevice(index);
        if (null == dev) return 0;
        if (!dev.m_play) return 0;
        stopWatch(dev.m_devId);
        if (dev.m_port_id != -1) {
            s_pca.StopAgent(dev.m_port_id);
            s_pca.FreeProtAgent(dev.m_port_id);
            dev.m_port_id = -1;
        }

        dev.m_playing = false;
        dev.m_play = false;
        dev.m_ptz_auto = false;
        dev.m_debug_msg_1 = "";
        dev.m_debug_msg_2 = "";
        dev.m_first_frame = false;
        return 0;
    }

    public static int startReplay(int index, PlayerDevice dev, String fileName) {
        if (null == dev) return -1;
        String devId = dev.m_dev.getDevId();
        int ret = s_func.ReplayDeviceFile(devId, fileName);
        if (ret != 0) return ret;

        m_index_id_map.put(index, devId);
        m_devMap.put(devId, dev);
        dev.m_play = true;
        dev.m_replaying = true;
        return ret;
    }

    public static int stopReplay(int index, PlayerDevice dev, String file_name) {
        if (null == dev) return -1;
        String devId = dev.m_dev.getDevId();
        int ret = s_func.ControlReplay(devId, REPLAY_IPC_ACTION.ACTION_STOP, 0);
        if (ret != 0) return ret;

        m_index_id_map.remove(index);
        if (dev.m_port_id != -1) {
            s_pca.StopAgent(dev.m_port_id);
            s_pca.FreeProtAgent(dev.m_port_id);
            dev.m_port_id = -1;
        }

        if (null != dev.m_audio) dev.m_audio.stopOutAudio();
        dev.m_play = false;
        dev.m_replaying = false;
        dev.m_first_frame = false;
        return ret;
    }

    public PlayerDevice getPlayerDevice(int index) {
        String devId = m_index_id_map.get(index);
        if (null == devId) return null;
        return m_devMap.get(devId);
    }

    public PlayerDevice getPlayerDevice(String devId) {
        if (null == devId) return null;
        return m_devMap.get(devId);
    }

    public void removePlayerDevice(int index) {
        String devId = m_index_id_map.get(index);
        if (null == devId) return;
        m_devMap.remove(devId);
        m_index_id_map.remove(index);
    }

    public void removePlayerDevice(String devId) {
        if (null == devId) return;
        PlayerDevice dev = m_devMap.get(devId);
        if (null == dev) return;
        String devId2 = m_index_id_map.get(dev.m_last_view_id);
        if (null == devId2) return;
        if (!devId.equals(devId2)) return;
        m_devMap.remove(devId);
        m_index_id_map.remove(dev.m_last_view_id);
    }

    public int getIndexByDeviceID(String devId) {
        int index = -1;
        PlayerDevice dev = getPlayerDevice(devId);
        if (null == dev) return index;
        return dev.m_view_id;
    }

    public int startCloudReplay(String devId, int index, ArchiveRecord record, ArchiveRecord idx_record, int pos) {
        Log.d(LibImpl.class.getName(), "startCloudReplay:1:record=[" + record + "],pos=[" + pos + "]");
        if (null == devId || null == record) return -1;
        int size = Integer.parseInt(record.getSize());
        int idx_size = Integer.parseInt(idx_record.getSize());

        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return -1;
        if (m_devMap.get(devId) == null) m_index_id_map.put(index, devId);
        m_devMap.put(devId, dev);
        dev.m_replaying = true;

        int ret = s_func.ReqOssObjectStream(devId, record.getName(), size, idx_record.getName(), idx_size, pos);
        Log.d(LibImpl.class.getName(), "startCloudReplay:2:ret=[" + ret + "]");
        if (ret != 0) return ret;
        dev.m_replay = true;
        dev.m_video.mIsStopVideo = false;
        dev.m_online = true;
        dev.m_playing = false;
        dev.m_voice = true;
        dev.m_audio.startOutAudio();
        return ret;
    }

    public int stopCloudReplay(String devId, int index) {
        Log.d(LibImpl.class.getName(), "stopCloudReplay:1:devId=[" + devId + "]");
        if (null == devId) return -1;
        int ret = s_func.StopOssObjectStream(devId);
        Log.d(LibImpl.class.getName(), "stopCloudReplay:2:ret=[" + ret + "]");
        if (ret != 0) return ret;

        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return -1;
        m_index_id_map.remove(index);
        m_devMap.remove(devId);
        if (dev.m_port_id != -1) {
            s_pca.StopAgent(dev.m_port_id);
            s_pca.FreeProtAgent(dev.m_port_id);
            dev.m_port_id = -1;
        }

        dev.m_replay = false;
        dev.m_replaying = false;
        dev.m_first_frame = false;
        return ret;
    }

    public int setCloudReplayPos(String devId, int pos) {
        Log.d(LibImpl.class.getName(), "setCloudReplayPos:1:devId=[" + devId + "],pos=[" + pos + "]");
        if (null == devId) return -1;
        int ret = s_func.SetOssObjectReplayPos(devId, pos);
        Log.d(LibImpl.class.getName(), "setCloudReplayPos:2:ret=[" + ret + "]");
        if (ret != 0) return ret;
        return ret;
    }

    public int startCloudDownload(String devId, ArchiveRecord record, String save_path) {
        if (TextUtils.isEmpty(devId) || null == record || TextUtils.isEmpty(save_path)) return -1;
        record.setLocalName(save_path);
        record.mDownloadStatus = ArchiveRecord.STATUS_START;
        record.setDownloadSize(0);
        List<ArchiveRecord> val = m_cloud_download_map.get(devId);
        if (null == val) val = new Vector<>();
        if (!val.contains(record)) val.add(record);
        m_cloud_download_map.put(devId, val);
        synchronized (m_download_notify) {
            m_download_notify.notify();
        }
        return 0;
    }

    public ArchiveRecord getCloudDownloadObject(String devId, String fileName) {
        if (TextUtils.isEmpty(devId) || TextUtils.isEmpty(fileName)) return null;
        List<ArchiveRecord> lst = m_cloud_download_map.get(devId);
        if (null == lst || lst.isEmpty()) return null;
        for (ArchiveRecord item : lst) {
            if (!item.getName().equals(fileName)) continue;
            return item;
        }

        return null;
    }

    public void notifyCloudDownloadStart() {
        if (null == m_download_thread) return;
        synchronized (m_download_thread) {
            m_download_thread.notify();
        }
    }

    synchronized public int startNvrReplay(String devId, int index, ArchiveRecord record, String date) {
        Log.d(LibImpl.class.getName(), "startNvrReplay:1:record=[" + record + "]");
        if (null == devId || null == record) return -1;
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return -1;
        if (dev.m_snapshot) {
            stopWatch(devId);
        }

        if (m_devMap.get(devId) == null) m_index_id_map.put(index, devId);
        m_devMap.put(devId, dev);
        dev.m_replaying = true;

        int ret = s_func.P2PNvrReplayByTime(devId, date);
        Log.d(LibImpl.class.getName(), "startNvrReplay:2:ret=[" + ret + "]");
        if (ret != 0) return ret;
        dev.m_replay = true;
        dev.m_video.mIsStopVideo = false;
        dev.m_online = true;
        dev.m_playing = false;
        dev.m_voice = true;
        return ret;
    }

    synchronized public int stopNvrReplay(String devId, int index) {
        Log.d(LibImpl.class.getName(), "stopNvrReplay:1:devId=[" + devId + "]");
        if (null == devId) return -1;
        int ret = s_func.P2PControlNVRReplay(devId, REPLAY_NVR_ACTION.NVR_ACTION_STOP, 0, "");
        Log.d(LibImpl.class.getName(), "stopNvrReplay:2:ret=[" + ret + "]");
        if (ret != 0) return ret;

        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return -1;
        m_index_id_map.remove(index);
        m_devMap.remove(devId);
        if (dev.m_replay_port_id > -1) {
            mPortOrIDMap.remove(dev.m_replay_port_id + "");
            s_pca.StopAgent(dev.m_replay_port_id);
            s_pca.FreeProtAgent(dev.m_replay_port_id);
            dev.m_replay_port_id = -1;
        }

        dev.m_replay = false;
        dev.m_replaying = false;
        dev.m_first_frame = false;
        return ret;
    }

    public int delDevice(PlayerDevice dev) {
        int ret = s_func.DelDevice(dev.m_devId);
        if (ret == 0) {
            Global.delDevice(dev.m_devId);
        }

        return ret;
    }

    public int delNVR(String name) {
        int ret = 0;
        ret = s_func.DelDevice(name);
        if (ret == 0) {
            Global.delDeviceByName(name);
        }

        return ret;
    }

    /**
     * #######################UI method...begin#############################
     */
    public static class MsgObject {
        public String devID;
        public Object recvObj;
        public Object reserveObj;
    }

    public static final int MSG_TYPES_DEFAULT = 0x7000;
    public static final int MSG_TYPES_LOGIN = 0x7001;
    public static final int MSG_TYPES_RECV_LIST = 0x7010;
    public static final int MSG_TYPES_VIDEO = 0x7020;
    public static final int MSG_TYPES_ALARM = 0x7030;
    public static final int MSG_TYPES_LOG = 0x7100;
    public static final int MSG_VIDEO_SET_STATUS_INFO = 0x8000;
    public static final int MSG_REPLAY_SET_POSITION = 0x8100;

    public static PlayerDevice m_change_default_pwd_dev = null;
    public static PlayerDevice m_change_media_param_dev = null;

    final int MaxBranch = 4;

    private static Map<String, PlayerDevice> m_devMap = new HashMap<>();
    private static Map<Integer, String> m_index_id_map = new HashMap<>();
    private static Map<String, String> m_device_capacityset_map = new HashMap<>();
    private static Map<String, Integer> m_device_net_type_map = new HashMap<>();
    private static Map<String, List<ArchiveRecord>> m_cloud_download_map = new HashMap<>();
    private static ArchiveRecord m_current_download_record = null;

    final Object m_download_notify = new Object();
    final Thread m_download_thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!m_exit) {
                ArchiveRecord record = getDownloadItem();
                if (null == record || (null != m_current_download_record && m_current_download_record.mDownloadStatus == ArchiveRecord.STATUS_DOWNLOADING)) {
                    try {
                        synchronized (m_download_notify) {
                            Log.d("oss download", "begin download notify object wait.");
                            m_download_notify.wait();
                            Log.d("oss download", "end download notify object wait.");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue;
                }

                if (null == m_current_download_record) m_current_download_record = record;
                String obj = record.getName();
                Log.w("oss download", "start download file, file=" + obj);
                int ret = s_func.DownloadOssObject(record.getDevId(), obj, record.getLocalName());
                if (ret != 0) {
                    Log.w("oss download", "download file failed, file=" + obj);
                    doMsgRspCB(SDK_CONSTANT.TPS_MSG_DOWNLOAD_OSS_OBJECT_FAILED, obj.getBytes(), obj.getBytes().length);
                    continue;
                }

                try {
                    synchronized (m_download_thread) {
                        Log.d("oss download", "begin download thread object wait.");
                        m_download_thread.wait();
                        Log.d("oss download", "end download thread object wait.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ArchiveRecord getDownloadItem() {
            for (String key : m_cloud_download_map.keySet()) {
                List<ArchiveRecord> lst = m_cloud_download_map.get(key);
                for (ArchiveRecord record : lst) {
                    if (record.mDownloadStatus != ArchiveRecord.STATUS_START) continue;
                    return record;
                }
            }

            return null;
        }
    });

    final Object m_change_pwd_notify = new Object();
    final Thread m_change_pwd_thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!m_exit) {
                try {
                    synchronized (m_change_pwd_thread) {
                        m_change_pwd_thread.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (m_exit) break;
                //List<PlayerDevice> lst = Global.getDeviceByDefaultUserPwd();
                List<PlayerDevice> lst = Global.getSelfDeviceList();
                if (null == lst || lst.isEmpty()) continue;
                for (PlayerDevice dev : lst) {
                    if (m_exit) return;
                    if (dev.m_dev.getOnLine() == Device.OFFLINE) continue;
                    int ret = FunclibAgent.getInstance().GetP2PDevConfig(dev.m_devId, 501);
                    if (0 != ret) {
                        Log.d("default password", "get media param config failed, devId=" + dev.m_devId);
                        continue;
                    }

                    try {
                        synchronized (m_change_pwd_notify) {
                            m_change_pwd_notify.wait(10000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    public void notifyChangeDefaultPwdThreadStart() {
        if (null == m_change_pwd_thread) return;
        synchronized (m_change_pwd_thread) {
            m_change_pwd_thread.notify();
        }
    }

    public static void sendMyToast(final Object msg) {
        MainActivity2.m_this.toast(msg);
    }

    public static void setTipText(String devID, Object msg) {
        MsgObject msgObj = new MsgObject();
        msgObj.devID = devID;
        msgObj.recvObj = msg;
        sendMessage(MSG_VIDEO_SET_STATUS_INFO, 0, 0, msgObj);
    }

    public static void setTipText2(String devID, Object msg) {
        MsgObject msgObj = new MsgObject();
        msgObj.devID = devID;
        msgObj.recvObj = msg;
        sendMessage(MSG_VIDEO_SET_STATUS_INFO, 1, 0, msgObj);
    }

    public static void setTipText(String devID, Object msg, String reserver) {
        MsgObject msgObj = new MsgObject();
        msgObj.devID = devID;
        msgObj.recvObj = msg;
        msgObj.reserveObj = reserver;
        sendMessage(MSG_VIDEO_SET_STATUS_INFO, 0, 0, msgObj);
    }

    private static final List<Handler> m_lstHandler = new ArrayList<>();
    private static Handler m_mediaParamHandler;

    public void addHandler(Handler handler) {
        synchronized (m_lstHandler) {
            if (m_lstHandler.contains(handler)) return;
            m_lstHandler.add(handler);
        }
    }

    public void removeHandler(Handler handler) {
        synchronized (m_lstHandler) {
            if (!m_lstHandler.contains(handler)) return;
            m_lstHandler.remove(handler);
        }
    }

    public void clearHandler() {
        m_lstHandler.clear();
    }

    public void setMediaParamHandler(Handler handler) {
        m_mediaParamHandler = handler;
    }

    public FunclibAgent getFuncLib() {
        return s_func;
    }

    public static void sendMessage(int what, int arg1, int arg2, Object recvObj) {
        if (m_lstHandler.isEmpty()) return;
        synchronized (m_lstHandler) {
            for (Handler h : m_lstHandler) {
                Message msg = h.obtainMessage();
                msg.arg1 = arg1;
                msg.arg2 = arg2;
                msg.what = what;
                msg.obj = recvObj;
                h.sendMessage(msg);
            }
        }
    }

    public static void sendMessage(int msgTypes, Object recvObj) {
        sendMessage(MSG_TYPES_DEFAULT, msgTypes, 0, recvObj);
    }

    public static void sendAlarmMessage(int what, int arg1, int arg2, Object obj) {
        sendMessage(what, arg1, arg2, obj);
        if (null != Global.m_msger) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj = obj;
            Global.sendMessageToService(msg);
        }
    }

    public static void sendMessageToMediaParamUI(int what, int arg1, int arg2, Object obj) {
        if (null == m_mediaParamHandler) return;
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        m_mediaParamHandler.sendMessage(msg);
    }

    /**
     * #######################UI method...end#############################
     */

    public void recvRecordData(byte[] data, int length, String devID, int reserver) {
        Log.i(TAG, "recvRecordData is call...begin");
        if (!BaseActivity.isNullStr(devID) && data != null && length > 0) {
            // 发送音频数据
            TPS_AudioData audioData = new TPS_AudioData(length, data, reserver);
            int ret = s_func.InputAudioDataAgent(devID, audioData.objectToByteBuffer(ByteOrder.nativeOrder()).array());
            Log.i(TAG, "InputAudioDataAgent, ret=" + ret + "data=" + audioData.toString());
        }
        Log.i(TAG, "recvRecordData is call...end");
    }

    public static OpenglesRender getRenderByDevID(String devID) {
        if (null == m_devMap.get(devID)) return null;
        GLSurfaceView.Renderer _OpenGLRender = m_devMap.get(devID).m_video;
        OpenglesRender render = (_OpenGLRender != null) ? (OpenglesRender) _OpenGLRender : null;
        return render;
    }

    public static void setRenderVideoState(String devID, int state) {
        OpenglesRender render = getRenderByDevID(devID);
        if (render != null) {
            render.mVideoState = state;
        }
    }

    public static void audioCallback(final String devID, final byte[] data, final int len) {
        if (null == m_devMap.get(devID)) return;
        AudioPlayer audioPlayer = m_devMap.get(devID).m_audio;
        if (audioPlayer != null) {
            AudioBuffer audioBuf = new AudioBuffer(data, len, true);
            audioPlayer.addToBuf(audioBuf);
        }
    }

    public int Login(String pUserName, String pPwd, String pVmsIp, short nVmsPort) {
        return s_func.LoginAgent(pUserName, pPwd, pVmsIp, nVmsPort);
    }

    public void stopSnapshot() {
        m_stop_snapshot = true;
        synchronized (m_event) {
            m_event.notify();
        }

        if (null != m_snapshotThread) m_snapshotThread.interrupt();
        m_snapshotMap.clear();
    }

    public void stopAll(boolean exit) {
        m_stop_play = true;
        for (PlayerDevice dev : m_devMap.values()) {
            if (null == dev.m_dev) continue;
            if (exit) {
                stopWatch2(dev.m_dev.getDevId());
            } else {
                stopWatch2(dev.m_dev.getDevId());
            }
        }
    }

    public int logoutAll() {
        int isOK = 0;
        mLoginStateMap.clear();
        isOK = s_func.LogoutAgent();
        return isOK;
    }

    public void clearDevice() {
        m_devMap.clear();
        m_index_id_map.clear();
    }

    public static int ptzControl(String devId, int nPtzCmd, int nHSpeed, int nVSpeed) {
        return FunclibAgent.getInstance().PTZActionEx(devId, nPtzCmd, nHSpeed, nVSpeed);
    }

    public static boolean isValidAudioFormat(String format) {
        if ("G.711".compareToIgnoreCase(format) != 0
                && SDK_CONSTANT.AUDIO_TYPE_G711.compareToIgnoreCase(format) != 0
                && SDK_CONSTANT.AUDIO_TYPE_PCMU.compareToIgnoreCase(format) != 0) {
            return false;
        }

        return true;
    }

    /**
     * 判断设备ID是否存在<br>
     *
     * @param devID 设备ID<br>
     * @return
     */
    public boolean isExistDeviceID(String devID) {
        return (findDeviceByID(devID) != null);
    }

    public static synchronized PlayerDevice findDeviceByID(String devId) {
        PlayerDevice device = null;
        if ((!BaseActivity.isNullStr(devId)) && (mDeviceListMap != null && mDeviceListMap.size() > 0)) {
            Set<String> keys = mDeviceListMap.keySet();
            for (String key : keys) {
                if (BaseActivity.isNullStr(key)) continue;
                List<PlayerDevice> devAry = mDeviceListMap.get(key);
                if (devAry == null || devAry.size() < 1) continue;

                for (PlayerDevice dev : devAry) {
                    String tmpDevId = (dev != null) ? dev.m_dev.getDevId() : "";
                    if (devId.compareToIgnoreCase(tmpDevId) == 0) {
                        device = dev;
                        break;
                    }
                }
                if (device != null) break;
            }
        }

        return device;
    }

    public static synchronized void putDeviceList(List<PlayerDevice> lst) {
        mDeviceListMap.put(LibImpl.DEFAULT_P2P_URL, lst);
    }

    public static synchronized void clearDeviceList() {
        mDeviceListMap.clear();
    }

    /**
     * 主要用于NVR的设备ID(100101-chanel-1)
     *
     * @param devID
     * @return
     */
    public static String getRightDeviceID(String devID) {
        String str = "";
        if (devID != null && (!"".equals(devID.trim()))) {
            String[] strAry = devID.split("-");
            if (strAry.length == 3) {
                str = strAry[0];
                str = devID;
            } else {
                str = devID;
            }
        }
        return str;
    }

    public static boolean m_stop_render = false;

    /**
     * 得到设备的别名(仅保存本地文件中)<br>
     *
     * @param dev 设备<br>
     * @return 本地无别名时，返回设备的六位云ID<br>
     */
    public String getDeviceAlias(Device dev) {
        String _devName = dev.getDevName();
        if (null == _devName || "".equals(_devName)) _devName = dev.getDevId();
        _devName = _devName.replace("-channel-", "-CH_");
        return _devName;
    }

    /**
     * 保存设备别名<br>
     *
     * @param devID     设备ID<br>
     * @param alias     设备别名<br>
     * @param loginType 登录类型 1用户登录，0设备登录
     */
    public int saveDeviceAlias(String devID, String alias, int loginType) {
        if ("".equals(devID) || "".equals(alias)) return -1;
        return s_func.ModifyDevName(devID, alias, loginType);
    }

    public static final String DEFAULT_P2P_URL = EtcInfo.DEFAULT_P2P_URL;
    private static String mCurLoginIP = DEFAULT_P2P_URL;
    public static Map<String, Boolean> mLoginStateMap = new HashMap<String, Boolean>();
    public static Map<String, List<PlayerDevice>> mDeviceListMap = new HashMap<String, List<PlayerDevice>>(); //设备列表
    public static Map<String, TPS_NotifyInfo> mDeviceNotifyInfo = new HashMap<String, TPS_NotifyInfo>(); //设备消息提示信息

    public static Map<String, Boolean> mIsFirstFrameMap = new HashMap<String, Boolean>();    //是否为第一帧
    public static Map<String, Integer> mVideoStateMap = new HashMap<String, Integer>();//0:none 1:start 2:playing 3:stop

    private Thread m_snapshotThread = null;
    public static Map<String, Device> m_snapshotMap = new HashMap<>();

    private static FunclibAgent s_func = FunclibAgent.getInstance();
    private static PlayCtrlAgent s_pca = PlayCtrlAgent.getInstance();
    /**
     * 设备ID和端口句柄映射类，键为口句柄，值为端设备ID
     */
    public static Map<String, String> mPortOrIDMap = new HashMap<String, String>();

    synchronized public static int addWatch(String devId, int nStreamNo, int nFrameType, int nComType) {
        Log.d(TAG, "addWatch,devId=" + devId + ",nStreamNo=" + nStreamNo + ",nFrameType=" + nFrameType + ",nComType=" + nComType);
        PlayerDevice dev = findDeviceByID(devId);
        if (null == dev) return -1;
        Log.d(TAG, "addWatch,devId=" + devId + ",add_watch_result=" + dev.m_add_watch_result + ",portId=" + dev.m_port_id + ",playing=" + dev.m_playing + ",play=" + dev.m_play);
        m_snapshotMap.put(devId, dev.m_dev);
        if (dev.m_add_watch_result >= 0) return 0;
        int ret = s_func.AddWatchEx(devId, nStreamNo, nFrameType, nComType);
        dev.m_add_watch_result = ret;
        return ret;
    }

    synchronized public static void stopWatch(String devId) {
        Log.d(TAG, "stopWatch,devId=" + devId);
        PlayerDevice dev = findDeviceByID(devId);
        if (null == dev) return;
        dev.m_open_audio_stream_result = -1;
        dev.m_open_video_stream_result = -1;
        Log.d(TAG, "stopWatch,devId=" + devId + ",portId=" + dev.m_port_id + ",playing=" + dev.m_playing + ",play=" + dev.m_play);
        int ret = s_func.StopWatchAgent(devId);
        dev.m_add_watch_result = -1;
        if (dev.m_audio != null) dev.m_audio.stopOutAudio();
        dev.m_dev.setVideoLiveState(OpenglesRender.VIDEO_STATE_STOP);

        mIsFirstFrameMap.remove(devId);

        if (dev.m_port_id < 0) return;
        mPortOrIDMap.remove(dev.m_port_id + "");
        s_pca.StopAgent(dev.m_port_id);
        s_pca.FreeProtAgent(dev.m_port_id);
        dev.m_port_id = -1;
    }

    synchronized private static void stopWatch2(String devId) {
        Log.i(TAG, "stopWatch2,devId=" + devId);
        PlayerDevice dev = findDeviceByID(devId);
        if (null == dev) return;
        Log.i(TAG, "stopWatch2,devId=" + devId + ",portId=" + dev.m_port_id + ",playing=" + dev.m_playing + ",play=" + dev.m_play);
        //s_func.StopWatchAgent(devId);
        dev.m_add_watch_result = -1;
        if (dev.m_audio != null) dev.m_audio.stopOutAudio();
        dev.m_dev.setVideoLiveState(OpenglesRender.VIDEO_STATE_STOP);

        mIsFirstFrameMap.remove(devId);

        if (dev.m_port_id < 0) return;
        mPortOrIDMap.remove(dev.m_port_id + "");
        s_pca.StopAgent(dev.m_port_id);
        s_pca.FreeProtAgent(dev.m_port_id);
        dev.m_port_id = -1;
    }

    public void resetPlayer() {

    }

    private void onGetVideoParamConfig(String devId, NetSDK_Media_Video_Config cfg) {
        if (null == cfg) return;
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;

        NetSDK_Media_Video_Config config = (NetSDK_Media_Video_Config) cfg.clone();
        config.addHead(false);
        /*String xmlOverlay = "";
        String title = Global.m_res.getString(R.string.prompt_modify_default_password);
        String zhTitle = "^请修改设备默认密码";
        String enTitle = "^Please change the default password";
        if ("admin".equals(dev.m_dev.getLoginName()) && "123456".equals(dev.m_dev.getLoginPassword())) {
            config.addHead(false);
            int pos = config.overlay.titleOverlay.Title.indexOf(zhTitle);
            if (pos < 0) pos = config.overlay.titleOverlay.Title.indexOf(enTitle);
            if (pos < 0) {
                config.overlay.titleOverlay.Title += title;
                config.overlay.Enable = "1";
                xmlOverlay = config.getOverlayXMLString();
            }
        } else {
            config.addHead(false);
            int pos = config.overlay.titleOverlay.Title.indexOf(zhTitle);
            if (pos < 0) pos = config.overlay.titleOverlay.Title.indexOf(enTitle);
            if (pos >= 0) {
                config.overlay.titleOverlay.Title = config.overlay.titleOverlay.Title.substring(0, pos);
                config.overlay.Enable = "1";
                xmlOverlay = config.getOverlayXMLString();
            }
        }

        if (!TextUtils.isEmpty(xmlOverlay)) {
            int ret = FunclibAgent.getInstance().SetP2PDevConfig(devId, 525, xmlOverlay);
            if (0 != ret) {
                Log.d("default password", "set overlay param failed, devId=" + devId);
            }
        }*/

        /*if (NetworkUtils.getNetworkState(Global.m_ctx) == NetworkUtils.MOBILE && m_change_media_param_dev != dev) {
            String xmlEncodeCfg = "";
            for (NetSDK_Media_Video_Config.EncodeConfig encodeConfig : config.encode.EncodeList) {
                if ((Integer.parseInt(encodeConfig.Stream) - 1) != Define.SUB_STREAM_TYPE) continue;
                if (Integer.parseInt(encodeConfig.BitRate) <= 150 && Integer.parseInt(encodeConfig.FrameRate) <= 18) continue;
                if (Integer.parseInt(encodeConfig.BitRate) > 150) encodeConfig.BitRate = "150";
                if (Integer.parseInt(encodeConfig.FrameRate) > 18) encodeConfig.FrameRate = "18";
                if (Integer.parseInt(encodeConfig.Initquant) > 18) encodeConfig.Initquant = "18";
                xmlEncodeCfg = config.getEncodeXMLString();
            }

            if (!TextUtils.isEmpty(xmlEncodeCfg)) {
                Log.d(TAG + ":onGetVideoParamConfig", "is mobile network, change bitRate=150, frameRate=18, devId=" + devId);
                int ret = FunclibAgent.getInstance().SetP2PDevConfig(devId, 523, xmlEncodeCfg);
                if (0 != ret) {
                    Log.d(TAG + ":onGetVideoParamConfig", "set encode config failed, devId=" + devId);
                }
            }
        }

        // 界面操作修改密码
        if (m_change_default_pwd_dev == dev) return;

        synchronized (m_change_pwd_notify) {
            m_change_pwd_notify.notify();
        }*/
    }

    private void onParseCapacitySet(String devId, String xml) {
        String capacity = "";
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("DevId")) {
                            eventType = parser.next();
                            devId = parser.getText();
                        } else if (parser.getName().equals("RESPONSE_PARAM")) {
                            capacity = parser.getAttributeValue(null, "SystemConfigString");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        capacity = (capacity != null) ? capacity : "";
        m_device_capacityset_map.put(devId, capacity);
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;
        dev.m_capacity_set = capacity;
    }

    public String getCapacitySet(PlayerDevice dev) {
        String capacity = m_device_capacityset_map.get(dev.m_devId);
        if (TextUtils.isEmpty(capacity)) capacity = m_device_capacityset_map.get(dev.m_dev.getDevGroupName());
        return capacity == null ? "" : capacity;
    }

    public int getDeviceNetType(PlayerDevice dev) {
        Integer type = m_device_net_type_map.get(dev.m_devId);
        if (null == type) {
            if (null != dev.m_dev) type = m_device_net_type_map.get(dev.m_dev.getDevGroupName());
        }

        return null == type ? -1 : type;
    }

    private void onReplayDevFileResp(int nMsgType, byte[] pData, int nDataLen) {
        int size = TPS_ReplayDevFileRsp.SIZE;
        if (pData == null || nDataLen != size) return;
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_ReplayDevFileRsp data = (TPS_ReplayDevFileRsp) TPS_ReplayDevFileRsp.createObjectByByteBuffer(byteBuffer);
        Log.i(TAG, "onReplayDevFileResp, act=" + data.getnActionType() + ",have audio=" + data.getbHaveAudio());

        final String devId = data.getSzDevId();
        int result = data.getnResult();
        if (result != 0) {
            setTipText(devId, R.string.tv_video_req_fail_tip, data.getnResult() + "");
            sendMessage(nMsgType, result, 0, null);
            return;
        }

        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;

        if (data.getnActionType() == REPLAY_IPC_ACTION.ACTION_PLAY) {
            if (dev.m_port_id >= 0) {
                s_pca.StopAgent(dev.m_port_id);
                s_pca.FreeProtAgent(dev.m_port_id);
                dev.m_port_id = -1;
            }

            mIsFirstFrameMap.put(devId, true);
            mVideoStateMap.put(devId, OpenglesRender.VIDEO_STATE_START);
            setRenderVideoState(devId, OpenglesRender.VIDEO_STATE_START);

            //获取端口句柄
            int port = dev.m_port_id;
            if (port < 0) port = s_pca.GetProtAgent();
            Log.d(TAG, "onReplayDevFileResp-->GetProtAgent, devId=" + devId + ",port=" + port);
            if (port < 0) {
                try {
                    Log.e(TAG, "onReplayDevFileResp-->GetProtAgent, devId=" + devId + ",port=" + port);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                port = s_pca.GetProtAgent();
                Log.d(TAG, "onReplayDevFileResp-->GetProtAgent, devId=" + devId + ",port=" + port);
                if (port < 0) {
                    Log.e(TAG, "onReplayDevFileResp-->GetProtAgent, devId=" + devId + ",port=" + port);
                    return;
                }
            }

            //设置视频流参数
            NetSDK_VIDEO_PARAM tvp = data.getVideoParam();
            TPS_VIDEO_PARAM vp = new TPS_VIDEO_PARAM();
            vp.setStream_index(0);
            vp.setVideo_encoder(tvp.getCodec().getBytes());
            vp.setWidth(tvp.getWidth());
            vp.setHeight(tvp.getHeight());
            vp.setFramerate(tvp.getFramerate());
            vp.setIntraframerate(tvp.getFramerate() * 4);
            vp.setBitrate(tvp.getBitrate());
            int len = tvp.getVol_length();
            byte[] config = new byte[len];
            ByteBuffer buf = ByteBuffer.wrap(tvp.getVol_data().getBytes());
            if (buf.limit() > 0) buf.get(config);
            vp.setConfig(config);
            vp.setConfig_len(len);
            byte[] videoParam = vp.objectToByteBuffer(ByteOrder.nativeOrder()).array();

            //0:视频 1:音频
            //最大缓冲帧数
            int ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 50);
            dev.m_open_video_stream_result = ret;
            Log.i(TAG, "onReplayDevFileResp-->OpenStreamAgent, port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
            if (ret != 0) {
                Log.e(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                try {
                    Thread.sleep(100);
                    ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 50);
                    dev.m_open_video_stream_result = ret;
                    Log.d(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                    if (ret != 0) {
                        setTipText(dev.m_devId, R.string.tv_video_req_fail_media_param_incorrect_tip);
                        Log.e(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                        s_pca.FreeProtAgent(port);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //判断是否有音频，如果有音频才进行打开音频流及配置语音参数
            if (data.getbHaveAudio() != 0) {
                //设置音频流参数（摄像机端要配置音频编码方式为G711，其它格式暂不支持）
                NetSDK_AUDIO_PARAM tap = data.getAudioParam();
                TPS_AUDIO_PARAM ap = new TPS_AUDIO_PARAM();
                ap.setStream_index(0);
                ap.setAudio_encoder(tap.getCodec().getBytes());
                ap.setSamplerate(tap.getSamplerate());
                ap.setSamplebitswitdh(tap.getBitspersample());
                ap.setChannels(tap.getChannels());
                ap.setBitrate(tap.getBitrate());
                ap.setFramerate(tap.getFramerate());
                byte[] audioParm = ap.objectToByteBuffer(ByteOrder.nativeOrder()).array();

                //0:视频 1:音频
                //最大缓冲帧数
                s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);
                dev.m_open_audio_stream_result = ret;
                Log.d(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                if (ret != 0) {
                    Log.e(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                    try {
                        Thread.sleep(100);
                        ret = s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);
                        dev.m_open_audio_stream_result = ret;
                        Log.d(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                        if (ret != 0) {
                            Log.e(TAG, "onReplayDevFileResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                            //s_pca.FreeProtAgent(port);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 根据摄像机音频参数，配置AudioPlayer的参数
                if (null != m_devMap.get(devId) && ret >= 0) {
                    AudioPlayer audioPlayer = m_devMap.get(devId).m_audio;
                    if (audioPlayer != null) {
                        AudioPlayer.MyAudioParameter audioParameter = new AudioPlayer.MyAudioParameter(data.getAudioParam().getSamplerate(), data.getAudioParam().getChannels(), data.getAudioParam().getBitspersample());
                        audioPlayer.initAudioParameter(audioParameter);
                        audioPlayer.startOutAudio();
                    }
                }
            }

            //0表示decDataCB解码出来的为yuv数据, 非0表示对应位数的rgb数据（支持的位数有：16、24、32）
            ret = s_pca.PlayAgent(port, 0);
            Log.d(TAG, "onReplayDevFileResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
            if (ret != 0) {
                Log.e(TAG, "onReplayDevFileResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                try {
                    Thread.sleep(100);
                    ret = s_pca.PlayAgent(port, 0);
                    Log.d(TAG, "onReplayDevFileResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                    if (ret != 0) {
                        Log.e(TAG, "onReplayDevFileResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                        s_pca.FreeProtAgent(port);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //记录端口句柄和设备ID间的映射关系
            dev.m_port_id = port;
            mPortOrIDMap.put(port + "", devId);
        }

        sendMessage(nMsgType, result, 0, data);
    }

    private void onNvrReplayResp(int nMsgType, byte[] pData, int nDataLen) {
        int size = TPS_ReplayDevFileRsp.SIZE;
        if (pData == null || nDataLen != size) return;
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_ReplayDevFileRsp data = (TPS_ReplayDevFileRsp) TPS_ReplayDevFileRsp.createObjectByByteBuffer(byteBuffer);
        Log.i(TAG, "onNvrReplayResp, act=" + data.getnActionType() + ",have audio=" + data.getbHaveAudio());

        String devId = data.getSzDevId();
        int result = data.getnResult();
        if (result != 0) {
            sendMessage(nMsgType, result, 0, data);
            return;
        }

        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;

        if (data.getnActionType() == REPLAY_NVR_ACTION.NVR_ACTION_PLAY) {
            if (dev.m_replay_port_id >= 0) {
                s_pca.StopAgent(dev.m_replay_port_id);
                s_pca.FreeProtAgent(dev.m_replay_port_id);
                dev.m_replay_port_id = -1;
            }

            mIsFirstFrameMap.put(devId, true);
            mVideoStateMap.put(devId, OpenglesRender.VIDEO_STATE_START);
            setRenderVideoState(devId, OpenglesRender.VIDEO_STATE_START);

            //获取端口句柄
            int port = dev.m_replay_port_id;
            if (port < 0) port = s_pca.GetProtAgent();
            Log.d(TAG, "onNvrReplayResp-->GetProtAgent, devId=" + devId + ",port=" + port);
            if (port < 0) {
                try {
                    Log.e(TAG, "onNvrReplayResp-->GetProtAgent, devId=" + devId + ",port=" + port);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                port = s_pca.GetProtAgent();
                Log.d(TAG, "onNvrReplayResp-->GetProtAgent, devId=" + devId + ",port=" + port);
                if (port < 0) {
                    Log.e(TAG, "onNvrReplayResp-->GetProtAgent, devId=" + devId + ",port=" + port);
                    return;
                }
            }

            //设置视频流参数
            NetSDK_VIDEO_PARAM tvp = data.getVideoParam();
            TPS_VIDEO_PARAM vp = new TPS_VIDEO_PARAM();
            vp.setStream_index(0);
            vp.setVideo_encoder(tvp.getCodec().getBytes());
            vp.setWidth(tvp.getWidth());
            vp.setHeight(tvp.getHeight());
            vp.setFramerate(tvp.getFramerate());
            vp.setIntraframerate(tvp.getFramerate() * 4);
            vp.setBitrate(tvp.getBitrate());
            int len = tvp.getVol_length();
            byte[] config = new byte[len];
            ByteBuffer buf = ByteBuffer.wrap(tvp.getVol_data().getBytes());
            buf.get(config);
            vp.setConfig(config);
            vp.setConfig_len(len);
            byte[] videoParam = vp.objectToByteBuffer(ByteOrder.nativeOrder()).array();

            //0:视频 1:音频
            //最大缓冲帧数
            int ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 50);
            dev.m_open_video_stream_result = ret;
            Log.i(TAG, "onNvrReplayResp-->OpenStreamAgent, port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
            if (ret != 0) {
                Log.e(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                try {
                    Thread.sleep(100);
                    ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 50);
                    dev.m_open_video_stream_result = ret;
                    Log.d(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                    if (ret != 0) {
                        setTipText(dev.m_devId, R.string.tv_video_req_fail_media_param_incorrect_tip);
                        Log.e(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                        s_pca.FreeProtAgent(port);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //判断是否有音频，如果有音频才进行打开音频流及配置语音参数
            if (data.getbHaveAudio() != 0) {
                //设置音频流参数（摄像机端要配置音频编码方式为G711，其它格式暂不支持）
                NetSDK_AUDIO_PARAM tap = data.getAudioParam();
                TPS_AUDIO_PARAM ap = new TPS_AUDIO_PARAM();
                ap.setStream_index(0);
                ap.setAudio_encoder(tap.getCodec().getBytes());
                ap.setSamplerate(tap.getSamplerate());
                ap.setSamplebitswitdh(tap.getBitspersample());
                ap.setChannels(tap.getChannels());
                ap.setBitrate(tap.getBitrate());
                ap.setFramerate(tap.getFramerate());
                byte[] audioParm = ap.objectToByteBuffer(ByteOrder.nativeOrder()).array();

                //0:视频 1:音频
                //最大缓冲帧数
                s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);
                dev.m_open_audio_stream_result = ret;
                Log.d(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                if (ret != 0) {
                    Log.e(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                    try {
                        Thread.sleep(100);
                        ret = s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);
                        dev.m_open_audio_stream_result = ret;
                        Log.d(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                        if (ret != 0) {
                            Log.e(TAG, "onNvrReplayResp-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                            //s_pca.FreeProtAgent(port);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 根据摄像机音频参数，配置AudioPlayer的参数
                if (null != m_devMap.get(devId) && ret >= 0) {
                    AudioPlayer audioPlayer = m_devMap.get(devId).m_audio;
                    if (audioPlayer != null) {
                        AudioPlayer.MyAudioParameter audioParameter = new AudioPlayer.MyAudioParameter(data.getAudioParam().getSamplerate(), data.getAudioParam().getChannels(), data.getAudioParam().getBitspersample());
                        audioPlayer.initAudioParameter(audioParameter);
                        audioPlayer.startOutAudio();
                    }
                }
            }

            //0表示decDataCB解码出来的为yuv数据, 非0表示对应位数的rgb数据（支持的位数有：16、24、32）
            ret = s_pca.PlayAgent(port, 0);
            Log.d(TAG, "onNvrReplayResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
            if (ret != 0) {
                Log.e(TAG, "onNvrReplayResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                try {
                    Thread.sleep(100);
                    ret = s_pca.PlayAgent(port, 0);
                    Log.d(TAG, "onNvrReplayResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                    if (ret != 0) {
                        Log.e(TAG, "onNvrReplayResp-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                        s_pca.FreeProtAgent(port);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //记录端口句柄和设备ID间的映射关系
            dev.m_replay_port_id = port;
            mPortOrIDMap.put(port + "", devId);
        }

        sendMessage(nMsgType, result, 0, data);
    }

    private void onOssReplayParam(int nMsgType, byte[] pData, int nDataLen) {
        int size = TPS_ReplayDevFileRsp.SIZE;
        if (pData == null || nDataLen != size) return;
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_ReplayDevFileRsp data = (TPS_ReplayDevFileRsp) TPS_ReplayDevFileRsp.createObjectByByteBuffer(byteBuffer);
        Log.i(TAG, "onOssReplayParam, act=" + data.getnActionType() + ",have audio=" + data.getbHaveAudio());

        String playFile = data.getSzReplayFile();
        String devId = playFile.substring(0, playFile.indexOf('/'));
        int result = data.getnResult();
        if (result != 0) {
            setTipText(devId, R.string.tv_video_req_fail_tip, data.getnResult() + "");
            sendMessage(nMsgType, result, 0, null);
            return;
        }

        PlayerDevice dev = getPlayerDevice(devId);
        if (null == dev) return;

        if (data.getnActionType() == REPLAY_IPC_ACTION.ACTION_PLAY) {
            if (dev.m_port_id != -1) {
                s_pca.StopAgent(dev.m_port_id);
                s_pca.FreeProtAgent(dev.m_port_id);
            }

            mIsFirstFrameMap.put(devId, true);
            mVideoStateMap.put(devId, OpenglesRender.VIDEO_STATE_START);
            setRenderVideoState(devId, OpenglesRender.VIDEO_STATE_START);

            //获取端口句柄
            int port = s_pca.GetProtAgent();
            Log.i(TAG, "onOssReplayParam-->GetProtAgent, port=" + port);
            //设置视频流参数
            NetSDK_VIDEO_PARAM tvp = data.getVideoParam();
            TPS_VIDEO_PARAM vp = new TPS_VIDEO_PARAM();
            vp.setStream_index(0);
            vp.setVideo_encoder(tvp.getCodec().getBytes());
            vp.setWidth(tvp.getWidth());
            vp.setHeight(tvp.getHeight());
            vp.setFramerate(tvp.getFramerate());
            vp.setIntraframerate(tvp.getFramerate() * 4);
            vp.setBitrate(tvp.getBitrate());
            vp.setConfig(tvp.getVol_data().getBytes());
            vp.setConfig_len(tvp.getVol_length());
            byte[] videoParam = vp.objectToByteBuffer(ByteOrder.nativeOrder()).array();

            //0:视频 1:音频
            //最大缓冲帧数
            int ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 40);
            Log.i(TAG, "onOssReplayParam-->OpenStreamAgent, port=" + port + ",videoParam=" + tvp + ",ret=" + ret);

            //判断是否有音频，如果有音频才进行打开音频流及配置语音参数
            if (data.getbHaveAudio() != 0) {
                //设置音频流参数（摄像机端要配置音频编码方式为G711，其它格式暂不支持）
                NetSDK_AUDIO_PARAM tap = data.getAudioParam();
                TPS_AUDIO_PARAM ap = new TPS_AUDIO_PARAM();
                ap.setStream_index(0);
                ap.setAudio_encoder(tap.getCodec().getBytes());
                ap.setSamplerate(tap.getSamplerate());
                ap.setSamplebitswitdh(tap.getBitspersample());
                ap.setChannels(tap.getChannels());
                ap.setBitrate(tap.getBitrate());
                ap.setFramerate(tap.getFramerate());
                byte[] audioParm = ap.objectToByteBuffer(ByteOrder.nativeOrder()).array();

                //0:视频 1:音频
                //最大缓冲帧数
                s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);

                // 根据摄像机音频参数，配置AudioPlayer的参数
                if (null == m_devMap.get(devId)) return;
                AudioPlayer audioPlayer = m_devMap.get(devId).m_audio;
                if (audioPlayer != null) {
                    AudioPlayer.MyAudioParameter audioParameter = new AudioPlayer.MyAudioParameter(data.getAudioParam().getSamplerate(), data.getAudioParam().getChannels(), data.getAudioParam().getBitspersample());
                    audioPlayer.initAudioParameter(audioParameter);
                    audioPlayer.startOutAudio();
                    Log.i(TAG, "onOssReplayParam-->Audio is init....");
                } else {
                    Log.w(TAG, "onOssReplayParam-->Audio isn't init....");
                }
            }

            //0表示decDataCB解码出来的为yuv数据, 非0表示对应位数的rgb数据（支持的位数有：16、24、32）
            Log.i(TAG, "onOssReplayParam-->PlayAgent, port=" + port);
            s_pca.PlayAgent(port, 0);

            //记录端口句柄和设备ID间的映射关系
            dev.m_port_id = port;
            mPortOrIDMap.put(port + "", devId);

            setTipText(devId, R.string.ipc_err_p2p_svr_connect_success);//ipc_err_p2p_svr_connect_success-tv_video_wait_video_stream_tip
        }

        sendMessage(nMsgType, result, 0, data);
    }

    private void onSearchOssObjectList(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        String xml = new String(pData).trim();
        ObjectsRoster<ArchiveRecord> lst = new ObjectsRoster<ArchiveRecord>();
        lst = (ObjectsRoster<ArchiveRecord>) lst.fromXML(pData, "ObjectList");
        sendMessage(nMsgType, 0, 0, lst);
    }

    private void onOssReplayFinish(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        String fileName = new String(pData).trim();
        String devId = fileName.substring(0, fileName.indexOf('/'));
        sendMessage(nMsgType, 0, 0, null);
    }

    private void onBeginDownloadOssObject(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        String fileName = new String(pData).trim();
        Log.d("oss download", "onBeginDownloadOssObject fileName=" + fileName);
        String devId = fileName.substring(0, fileName.indexOf('/'));
        ArchiveRecord record = getCloudDownloadObject(devId, fileName);
        if (null == record) return;
        record.mDownloadStatus = ArchiveRecord.STATUS_DOWNLOADING;
        sendMessage(nMsgType, 0, 0, record);
    }

    private void onDownloadOssObjectSize(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        String xml = new String(pData).trim();
        Log.d("oss download", "onDownloadOssObjectSize xml=" + xml);
        String fileName = "";
        String size = "";
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "utf-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (null == parser.getName()) {
                            eventType = parser.next();
                            continue;
                        }

                        if (parser.getName().equals("Object")) {
                            fileName = parser.nextText();
                        } else if (parser.getName().equals("Size")) {
                            size = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(fileName)) return;
        String devId = fileName.substring(0, fileName.indexOf('/'));
        ArchiveRecord record = getCloudDownloadObject(devId, fileName);
        if (null == record) return;
        record.setDownloadSize(Long.parseLong(size));
        Log.d("oss download", "onDownloadOssObjectSize fileName=" + fileName + ",size=" + record.getSize() + ",size1=" + record.getDownloadSize());
        sendMessage(nMsgType, 0, 0, record);
    }

    private void onEndDownloadOssObject(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        String fileName = new String(pData).trim();
        Log.d("oss download", "onEndDownloadOssObject fileName=" + fileName);
        String devId = fileName.substring(0, fileName.indexOf('/'));
        ArchiveRecord record = getCloudDownloadObject(devId, fileName);
        if (null == record) return;
        record.setDownloadSize(Long.parseLong(record.getSize()));
        record.mDownloadStatus = ArchiveRecord.STATUS_SUCCEED;

        synchronized (m_download_thread) {
            m_download_thread.notify();
        }

        sendMessage(nMsgType, 0, 0, record);
    }

    private void onDownloadOssObjectFailed(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        String fileName = new String(pData).trim();
        Log.d("oss download", "onDownloadOssObjectFailed fileName=" + fileName);
        String devId = fileName.substring(0, fileName.indexOf('/'));
        ArchiveRecord record = getCloudDownloadObject(devId, fileName);
        if (null == record) return;
        record.mDownloadStatus = ArchiveRecord.STATUS_FAILED;
        final String local_file = Global.getCloudDir() + "/" + record.getName();
        File file = new File(local_file);
        if (file.exists()) file.delete();

        synchronized (m_download_thread) {
            m_download_thread.notify();
        }

        sendMessage(nMsgType, 0, 0, record);
    }

    private void onOssPlayBeginCache(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        sendMessage(nMsgType, 0, 0, null);
    }

    private void onOssPlayEndCache(int nMsgType, byte[] pData, int nDataLen) {
        if (pData == null) return;
        sendMessage(nMsgType, 0, 0, null);
    }

    private void onP2PNetType(int nMsgType, byte[] pData, int nDataLen) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
        Log.d(TAG, "onP2PNetType" + ni.toString());
        String devId = new String(ni.getSzDevId()).trim();
        m_device_net_type_map.put(devId, ni.getnResult());
        PlayerDevice dev = findDeviceByID(devId);
        if (null == dev) {
            List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
            if (null != lst) {
                for (PlayerDevice d : lst) {
                    d.m_net_type = ni.getnResult();
                    setTipText(d.m_devId, d.m_tipInfo);
                }
            }
        } else {
            dev.m_net_type = ni.getnResult();
            setTipText(devId, dev.m_tipInfo);
        }
    }

    public int doMsgRspCB(int nMsgType, byte[] pData, int nDataLen) {
        Log.i(TAG, "[doMsgRspCB:nMsgType=" + nMsgType + ",pData=" + ((pData == null) ? "null" : new String(pData).trim()) + ",nDataLen=" + nDataLen + ", exit=]" + m_exit);
        if (m_exit) return 0;

        MsgObject msgObj = new MsgObject();
        switch (nMsgType) {
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_OK: {//8193
                if (pData != null && nDataLen == UserRight.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    UserRight ts = (UserRight) UserRight.createObjectByByteBuffer(byteBuffer);
                    Log.i(TAG, "doMsgRspCB-->UserRight@" + ts.toString());
                    //sendMyToast(R.string.dlg_login_success_tip);

                    msgObj.recvObj = ts;
                    mLoginStateMap.put(mCurLoginIP, true);
                    sendMessage(MSG_TYPES_DEFAULT, nMsgType, 0, msgObj);
                    return 0;
                }
            }
            break;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED: {//8194
                Log.i(TAG, "doMsgRspCB@login is failed...");
                // 登录失败，弹出输入用户名密码的界面
                if (null == pData) return 0;
                String devId = new String(pData).trim();
                if (null == m_devMap.get(devId)) return 0;

                OpenglesRender _glRender = m_devMap.get(devId).m_video;
                // 当前是在获取截图，并且设备未播放，不通知界面弹出输入用户名密码
                if (null != m_snapshotMap.get(devId) && null == _glRender) {
                    notifyNextSnapshot(devId);
                    return 0;
                }

                PlayerDevice device = getPlayerDevice(devId);
                sendMessage(nMsgType, 0, 0, device);
                mLoginStateMap.put(mCurLoginIP, false);
                return 0;
            }
            case SDK_CONSTANT.TPS_MSG_NOTIFY_AUTH_FAILED:
                Log.i(TAG, "doMsgRspCB:TPS_MSG_NOTIFY_AUTH_FAILED");
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    Log.i(TAG, "doMsgRspCB:TPS_MSG_NOTIFY_AUTH_FAILED, ni=" + ni.toString());
                    int result = ni.getnResult();
                    String devId = new String(ni.getSzDevId()).trim();
                    PlayerDevice dev = Global.getDeviceById(devId);
                    if (null == dev) {
                        List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
                        if (null == lst) return 0;
                        for (PlayerDevice d : lst) {
                            if (-1 == result) setTipText(d.m_devId, R.string.dlg_login_fail_user_pwd_incorrect_tip);
                        }
                    } else {
                        if (-1 == result) {
                            setTipText(devId, R.string.dlg_login_fail_user_pwd_incorrect_tip);
                        }
                    }

                    OpenglesRender _glRender = null;
                    if (null != m_devMap.get(devId)) _glRender = m_devMap.get(devId).m_video;
                    // 当前是在获取截图，并且设备未播放，不通知界面弹出输入用户名密码
                    if (null != m_snapshotMap.get(devId) && null == _glRender) {
                        notifyNextSnapshot(devId);
                        return 0;
                    }

                    PlayerDevice device = getPlayerDevice(devId);
                    if (null == device) return 0;
                    sendMessage(nMsgType, 0, 0, device);
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DEV_DATA: {//8195-返回xml格式设备数据
                if (null != pData && pData.length > 0) {
                    String xml = new String(pData).trim();
                    Log.d(TAG, "device list xml[" + xml + "]");
                    msgObj.recvObj = xml;
                } else {
                    Log.e(TAG, "doMsgRspCB:Get device data is error...");
                    sendMyToast(R.string.dlg_get_list_fail_tip);
                }

                sendMessage(MSG_TYPES_DEFAULT, nMsgType, 0, msgObj);
                return 0;
            }
            case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH: //8196-返回TPS_AddWachtRsp
                onMsgRspAddWatch(pData, nDataLen, msgObj);
                return 0;
            /**#########################Video Msg notify...begin####################################*/
            case SDK_CONSTANT.TPS_MSG_P2P_INIT_FAILED: //8204
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    int result = ni.getnResult();
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_INIT_FAILED/SELF_ID#TPS_NotifyInfo@" + ni.toString());
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_SELF_ID://8205
                if (false && pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_SELF_ID/SELF_ID#TPS_NotifyInfo@" + ni.toString());
                    String devID = new String(ni.getSzDevId()).trim();
                    String errorText = new String(ni.getSzInfo()).trim();
                    // remove "_test"
                    errorText = errorText.substring(0, errorText.length() - 5);
                    int nResult = ni.getnResult();
                    ni.setSzInfo(errorText.getBytes());

                    mDeviceNotifyInfo.put(devID, ni);
                    PlayerDevice dev = findDeviceByID(devID);
                    if (null == dev) {
                        List<PlayerDevice> lst = Global.getDeviceByGroup(devID);
                        if (null == lst) return 0;
                        for (PlayerDevice d : lst) {
                            if (nResult == 0) setTipText(d.m_devId, R.string.ipc_err_p2p_disconnected, errorText);
                        }
                    } else {
                        if (nResult == 0) setTipText(devID, R.string.ipc_err_p2p_disconnected, errorText);
                        //if(nResult == 0) setTipText(devID, errorText);
                    }
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_CONNECT_OK#TPS_NotifyInfo@" + ni.toString());
                    String devId = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();

                    mDeviceNotifyInfo.put(devId, ni);
                    PlayerDevice dev = findDeviceByID(devId);
                    if (null != dev) {
                        dev.m_connect_ok = true;
                        dev.m_dev.setOnLine(Device.ONLINE);
                        if (nResult == 0) setTipText(devId, R.string.ipc_err_p2p_svr_connect_success);
                    } else {
                        List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
                        if (null != lst) {
                            for (PlayerDevice d : lst) {
                                d.m_connect_ok = true;
                            }
                        }
                    }

                    sendMessage(nMsgType, 0, 0, msgObj);
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CONNECT_REFUSE:
                Log.i(TAG, "doMsgRspCB:TPS_MSG_P2P_NVR_CONNECT_REFUSE");
                /*if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    Log.i(TAG, "doMsgRspCB:TPS_MSG_P2P_NVR_CONNECT_REFUSE, ni=" + ni.toString());
                    int result = ni.getnResult();
                    String devId = new String(ni.getSzDevId()).trim();
                    PlayerDevice dev = Global.getDeviceById(devId);
                    if (null == dev) {
                        List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
                        if (null != lst) {
                            for (PlayerDevice d : lst) {
                                if (-2 == result) {
                                    setTipText(d.m_devId, R.string.dlg_login_fail_client_num_full_tip);
                                } else if (-3 == result) {
                                    setTipText(d.m_devId, R.string.dlg_login_fail_client_link_full_tip);
                                } else if (-4 == result) {
                                    setTipText(d.m_devId, R.string.dlg_login_fail_addr_error_tip);
                                }
                            }
                        }
                    }

                    OpenglesRender _glRender = null;
                    if (null != m_devMap.get(devId)) _glRender = m_devMap.get(devId).m_video;
                    // 当前是在获取截图，并且设备未播放，不通知界面弹出输入用户名密码
                    if (null != m_snapshotMap.get(devId) && null == _glRender) {
                        notifyNextSnapshot(devId);
                        return 0;
                    }

                    PlayerDevice device = getPlayerDevice(devId);
                    if (null == device) return 0;
                    sendMessage(nMsgType, 0, 0, device);
                }*/
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_TST_RPT:
                Log.i(TAG, "doMsgRspCB:TPS_MSG_P2P_NVR_TST_RPT");
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    Log.i(TAG, "doMsgRspCB:TPS_MSG_P2P_NVR_TST_RPT, ni=" + ni.toString());
                    int result = ni.getnResult();
                    String devId = new String(ni.getSzDevId()).trim();
                    String chId = "";
                    String msg = new String(ni.getSzInfo()).trim();
                    PlayerDevice dev = null;
                    if (!TextUtils.isEmpty(msg) && msg.contains("ChannelId=")) {
                        chId = "-CH-" + msg.substring(10, msg.indexOf(','));
                        devId += chId;
                        dev = Global.getDeviceById(devId);
                        if (null == dev) return 0;
                        dev.m_debug_msg_1 = msg;
                        msg = "";
                    }

                    if (null != dev) {
                        String tip = dev.m_debug_msg_1;
                        if (!TextUtils.isEmpty(dev.m_debug_msg_2)) tip += "&&" + dev.m_debug_msg_2;
                        setTipText2(dev.m_devId, tip);
                        if (-2 == result) {
                            setTipText(dev.m_devId, R.string.dlg_login_fail_client_num_full_tip);
                        } else if (-3 == result) {
                            setTipText(dev.m_devId, R.string.dlg_login_fail_client_link_full_tip);
                        } else if (-4 == result) {
                            setTipText(dev.m_devId, R.string.dlg_login_fail_addr_error_tip);
                        }
                    } else {
                        List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
                        if (null != lst) {
                            for (PlayerDevice d : lst) {
                                d.m_debug_msg_2 = msg;
                                String tip = d.m_debug_msg_2;
                                if (!TextUtils.isEmpty(d.m_debug_msg_1)) tip = d.m_debug_msg_1 + "-" + tip;
                                setTipText2(d.m_devId, tip);
                                if (-2 == result) {
                                    setTipText(d.m_devId, R.string.dlg_login_fail_client_num_full_tip);
                                } else if (-3 == result) {
                                    setTipText(d.m_devId, R.string.dlg_login_fail_client_link_full_tip);
                                } else if (-4 == result) {
                                    setTipText(d.m_devId, R.string.dlg_login_fail_addr_error_tip);
                                }
                            }
                        }
                    }
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_OFFLINE#TPS_NotifyInfo@" + ni.toString());
                    String devID = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();

                    PlayerDevice dev = findDeviceByID(devID);
                    if (dev != null) {
                        dev.m_connect_ok = false;
                        dev.m_dev.setOnLine(Device.OFFLINE);
                        if (nResult == 0) setTipText(devID, R.string.dlg_device_offline_tip);
                    }

                    sendMessage(nMsgType, 0, 0, msgObj);
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_NVR_OFFLINE#TPS_NotifyInfo@" + ni.toString());
                    String devID = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();
                    if (TextUtils.isEmpty(devID)) return 0;

                    List<PlayerDevice> lst = Global.getDeviceByGroup(devID);
                    if (null != lst) {
                        for (PlayerDevice dev : lst) {
                            dev.m_connect_ok = false;
                            dev.m_dev.setOnLine(Device.OFFLINE);
                            setTipText(dev.m_devId, R.string.dlg_device_offline_tip);
                        }
                    }

                    sendMessage(nMsgType, 0, 0, msgObj);
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_NVR_CH_OFFLINE#TPS_NotifyInfo@" + ni.toString());
                    String devID = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();

                    PlayerDevice dev = findDeviceByID(devID);
                    if (dev != null) {
                        dev.m_connect_ok = false;
                        dev.m_dev.setOnLine(Device.OFFLINE);
                        setTipText(devID, R.string.dlg_device_offline_tip);
                    }

                    sendMessage(nMsgType, 0, 0, msgObj);
                }
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_P2P_NVR_CH_ONLINE#TPS_NotifyInfo@" + ni.toString());
                    String devID = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();

                    mDeviceNotifyInfo.put(devID, ni);
                    PlayerDevice dev = findDeviceByID(devID);
                    if (dev != null) {
                        dev.m_connect_ok = true;
                        dev.m_dev.setOnLine(Device.ONLINE);
                        setTipText(devID, dev.m_tipInfo);
                    }

                    sendMessage(nMsgType, 0, 0, msgObj);
                }
                return 0;
            /**#########################Video Msg notify...begin####################################*/

            case SDK_CONSTANT.TPS_MSG_RSP_TALK://TPS_TALKRsp
                if (pData != null && nDataLen == TPS_TALKRsp.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_TALKRsp ts = (TPS_TALKRsp) TPS_TALKRsp.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ts;
                    Log.i(TAG, "doMsgRspCB-->TPS_TALKRsp@" + ts.toString());
                    String devID = new String(ts.getSzDevId()).trim();
                    sendMessage(nMsgType, 0, 0, msgObj);
                } else {//end...pData != null && nDataLen == TPS_TALKRsp.SIZE
                    sendMyToast(R.string.tv_talk_fail_data_error_tip);
                }

                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_TALK_CLOSE:
                Log.w(TAG, "TPS_MSG_RSP_TALK_CLOSE-->" + nDataLen);
                sendMessage(nMsgType, 0, 0, null);
                return 0;
            case SDK_CONSTANT.TPS_MSG_REC_STOP: {//录像已停止
                if (pData != null && nDataLen == TPS_NotifyInfo.SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
                    byteBuffer.order(ByteOrder.nativeOrder());
                    byteBuffer.put(pData, 0, nDataLen);
                    byteBuffer.rewind();
                    TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
                    msgObj.recvObj = ni;
                    Log.i(TAG, "doMsgRspCB-->TPS_MSG_REC_STOP#TPS_NotifyInfo@" + ni.toString());
                    String devID = new String(ni.getSzDevId()).trim();
                    int nResult = ni.getnResult();
                }
            }
            break;
            case SDK_CONSTANT.TPS_MSG_ALARM:
                onMsgAlarm(pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_SEARCH_ALARM:
                onSearchAlarmResp(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_GET_FRIEND_LIST:
                onGetFriendListResp(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_GET_OFFLINE_MSG:
                onGetOffineMsgResp(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_PTZREQ:
                onPtzReqResp(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_REPLAY_DEV_FILE:
                onReplayDevFileResp(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_SEARCH_OSS_OBJECTLIST:
                onSearchOssObjectList(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_OSS_REPLAY_PARAM:
                onOssReplayParam(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_OSS_REPLAY_FINISH:
                onOssReplayFinish(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_BEGIN_DOWNLOAD_OSS_OBJECT:
                onBeginDownloadOssObject(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_DOWNLOAD_OSS_OBJECT_SIZE:
                onDownloadOssObjectSize(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_END_DOWNLOAD_OSS_OBJECT:
                onEndDownloadOssObject(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_DOWNLOAD_OSS_OBJECT_FAILED:
                onDownloadOssObjectFailed(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_OSS_PLAY_BEGIN_CACHE:
                onOssPlayBeginCache(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_OSS_PLAY_END_CACHE:
                onOssPlayEndCache(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_P2P_NETTYPE:
                onP2PNetType(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_SEARCH_NVR_REC:
                onRspSearchNvrRec(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_RSP_NVR_REPLAY:
                onNvrReplayResp(nMsgType, pData, nDataLen);
                return 0;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DISP_INFO:
                onNofityDispInfo(nMsgType, pData, nDataLen);
                return 0;
            default:
                break;
        }

        sendMessage(nMsgType, msgObj);    //发送Handler消息
        return 0;
    }

    private void onRspSearchNvrRec(int nMsgType, byte[] pData, int nDataLen) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
        Log.d(TAG, "onRspSearchNvrRec" + ni.toString());
        String devId = new String(ni.getSzDevId()).trim();
        PlayerDevice dev = findDeviceByID(devId);
        if (null == dev) return;
        sendMessage(nMsgType, 0, 0, ni);
    }

    private void onNofityDispInfo(int nMsgType, byte[] pData, int nDataLen) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_NotifyInfo ni = (TPS_NotifyInfo) TPS_NotifyInfo.createObjectByByteBuffer(byteBuffer);
        Log.d(TAG, "onNofityDispInfo" + ni.toString());
        sendMessage(nMsgType, 0, 0, ni);
    }

    @Override
    public void onLog(int prio, String tag, String msg) {
        sendMessage(MSG_TYPES_LOG, prio, 0, tag + "\t" + msg);
    }

    @Override
    public int msgRspCB(int nMsgType, byte[] pData, int nDataLen) {
        try {
            return doMsgRspCB(nMsgType, pData, nDataLen);
        } catch (OutOfMemoryError e) {
            System.gc();
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int cmdRspCB(int nMsgType, String devId, String xml) {
        if (m_exit) return 0;
        int flag = nMsgType & 0xff000000;
        Log.i(TAG, "[cmdRspCB:nMsgType=" + nMsgType + ",devId=" + devId + ",flag=" + flag + ",xml=" + xml + "]");
        MsgObject msgObject = new MsgObject();
        switch (nMsgType) {
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_TIME_CONFIG:
                if ("".equals(xml)) return 0;
                NetSDK_TimeZone_DST_Config ntc = (NetSDK_TimeZone_DST_Config) new NetSDK_TimeZone_DST_Config().fromXML(xml);
                sendMessage(nMsgType, flag, 0, ntc);
                break;
            case NetSDK_CMD_TYPE.CMD_SET_SYSTEM_TIME_CONFIG:
                sendMessage(nMsgType, flag, 0, null);
                break;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG:
                if ("".equals(xml)) return 0;
                List<NetSDK_UserAccount> lst = (List<NetSDK_UserAccount>) new NetSDK_UserAccount().fromXML(xml.getBytes());
                msgObject.devID = devId;
                msgObject.recvObj = lst;
                sendMessage(nMsgType, flag, 0, msgObject);
                break;
            case NetSDK_CMD_TYPE.CMD_SET_SYSTEM_USER_CONFIG:
                sendMessage(nMsgType, flag, 0, null);
                break;
            case 501: // 读取视频参数配置
                if (TextUtils.isEmpty(xml)) return 0;
                NetSDK_Media_Video_Config cfg = (NetSDK_Media_Video_Config) new NetSDK_Media_Video_Config().fromXML(xml);
                sendMessageToMediaParamUI(nMsgType, flag, 0, cfg);
                onGetVideoParamConfig(devId, cfg);
                break;
            case 523: // 设置视频参数配置
                sendMessageToMediaParamUI(nMsgType, flag, 0, null);
                break;
            case 524: // 设置视频采集参数配置
                sendMessageToMediaParamUI(nMsgType, flag, 0, null);
                break;
            case 1031: // 读取媒体编码能力
                NetSDK_Media_Capability cap = null;
                if (!"".equals(xml)) cap = (NetSDK_Media_Capability) new NetSDK_Media_Capability().fromXML(xml);
                sendMessageToMediaParamUI(nMsgType, flag, 0, cap);
                break;
            case 802:
                NetSDK_Alarm_Config config = new NetSDK_Alarm_Config();
                if (!"".equals(xml))
                    config.motionDetectAlarm = (NetSDK_Alarm_Config.MotionDetectAlarm) config.fromMotionDetectAlarmXML(xml);
                sendMessageToMediaParamUI(nMsgType, flag, 0, config);
                break;
            case 822:
                sendMessageToMediaParamUI(nMsgType, flag, 0, null);
                break;
            case 1005: //修改系统时间
                sendMessageToMediaParamUI(nMsgType, flag, 0, null);
                break;
            case 1014:
                if ("".equals(xml)) return 0;
                NetSDK_Storage_Info nssi = (NetSDK_Storage_Info) new NetSDK_Storage_Info().fromXML(xml);
                sendMessageToMediaParamUI(nMsgType, flag, 0, nssi);
                break;
            case 1017:
                sendMessageToMediaParamUI(nMsgType, flag, 0, null);
                break;
            case 1018:
                sendMessageToMediaParamUI(nMsgType, flag, 0, null);
                break;
            case 1020:
                if ("".equals(xml)) return 0;
                onParseCapacitySet(devId, xml);
                break;
            case 1021:
                if ("".equals(xml)) return 0;
                TPS_RecordFileResponse resp = (TPS_RecordFileResponse) new TPS_RecordFileResponse().fromXML(xml);
                sendMessage(nMsgType, flag, 0, resp);
                break;
            case 1048:
                if ("".equals(xml)) return 0;
                NetSDK_TimeConfig nstc = (NetSDK_TimeConfig) new NetSDK_TimeConfig().fromXML(xml);
                sendMessage(nMsgType, flag, 0, nstc);
                break;
            case 1002:
                sendMessage(nMsgType, flag, 0, null);
                break;
            case 1007:
                sendMessage(nMsgType, flag, 0, null);
                break;
        }

        return 0;
    }

    private void onMsgRspAddWatch(byte[] pData, int nDataLen, MsgObject msgObj) {
        if (pData != null && nDataLen == TPS_AddWachtRsp.SIZE) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.put(pData, 0, nDataLen);
            byteBuffer.rewind();
            TPS_AddWachtRsp ts = (TPS_AddWachtRsp) TPS_AddWachtRsp.createObjectByByteBuffer(byteBuffer);
            msgObj.recvObj = ts;
            Log.i(TAG, "onMsgRspAddWatch-->TPS_AddWachtRsp@" + ts.toString());
            final String devId = new String(ts.getSzDevId()).trim();
            int result = ts.getnResult();
            sendMessage(SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH, 0, 0, ts);
            PlayerDevice dev = Global.getDeviceById(devId);
            if (null == dev) return;
            dev.m_svr_inst = ts.getnSvrInst();

            if (0 != result) {
                Log.i(TAG, "onMsgRspAddWatch-->response failed, devId=" + devId + ",result=" + result);
                notifyNextSnapshot(devId);
                if (null == m_devMap.get(devId)) return;
                OpenglesRender _glRender = m_devMap.get(devId).m_video;
                // 当前没有设备在界面播放，返回
                if (null == _glRender) return;
                return;
            }

            //注意，接收到播放响应消息的时候，必须判断下当前返回的这个设备id是否有正在播放，如果有必须先停止播放后再去重新创建播放器。
            //因为设备在播放过程中有可能被修改了媒体参数，或者进行了重连，这个时候设备都会重新将参数返回上来
            //所以在播放过程中也可能接收到一次或多次TPS_MSG_RSP_ADDWATCH消息
            Log.d(TAG, "onMsgRspAddWatch-->GetProtAgent, devId=" + devId + ",old port=" + dev.m_port_id);
            if (dev.m_port_id >= 0) {
                Log.i(TAG, "onMsgRspAddWatch-->device playing, stop it, port=" + dev.m_port_id);
                s_pca.StopAgent(dev.m_port_id);
                s_pca.FreeProtAgent(dev.m_port_id);
                dev.m_port_id = -1;
            }

            mIsFirstFrameMap.put(devId, true);
            mVideoStateMap.put(devId, OpenglesRender.VIDEO_STATE_START);
            setRenderVideoState(devId, OpenglesRender.VIDEO_STATE_START);
            dev.m_add_watch_rsp = ts;

            //获取端口句柄
            int port = dev.m_port_id;
            if (port < 0) port = s_pca.GetProtAgent();
            Log.d(TAG, "onMsgRspAddWatch-->GetProtAgent, devId=" + devId + ",port=" + port);
            if (port < 0) {
                try {
                    Log.e(TAG, "onMsgRspAddWatch-->GetProtAgent, devId=" + devId + ",port=" + port);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                port = s_pca.GetProtAgent();
                Log.d(TAG, "onMsgRspAddWatch-->GetProtAgent, devId=" + devId + ",port=" + port);
                if (port < 0) {
                    Log.e(TAG, "onMsgRspAddWatch-->GetProtAgent, devId=" + devId + ",port=" + port);
                    return;
                }
            }

            //设置视频流参数
            TPS_VIDEO_PARAM tvp = ts.getVideoParam();
            byte[] videoParam = tvp.objectToByteBuffer(ByteOrder.nativeOrder()).array();

            //0:视频 1:音频
            //最大缓冲帧数
            int ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 50);
            dev.m_open_video_stream_result = ret;
            Log.d(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
            if (ret != 0) {
                Log.e(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                try {
                    Thread.sleep(100);
                    ret = s_pca.OpenStreamAgent(port, videoParam, videoParam.length, 0, 50);
                    dev.m_open_video_stream_result = ret;
                    Log.d(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                    if (ret != 0) {
                        setTipText(dev.m_devId, R.string.tv_video_req_fail_media_param_incorrect_tip);
                        Log.e(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",videoParam=" + tvp + ",ret=" + ret);
                        s_pca.FreeProtAgent(port);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //判断是否有音频，如果有音频才进行打开音频流及配置语音参数
            if (ts.hasAudio()) {
                //设置音频流参数（摄像机端要配置音频编码方式为G711，其它格式暂不支持）
                TPS_AUDIO_PARAM tap = ts.getAudioParam();
                byte[] audioParm = tap.objectToByteBuffer(ByteOrder.nativeOrder()).array();
                //0:视频 1:音频
                //最大缓冲帧数
                ret = s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);
                dev.m_open_audio_stream_result = ret;
                Log.d(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                if (ret != 0) {
                    Log.e(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                    try {
                        Thread.sleep(100);
                        ret = s_pca.OpenStreamAgent(port, audioParm, audioParm.length, 1, 20);
                        dev.m_open_audio_stream_result = ret;
                        Log.d(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                        if (ret != 0) {
                            Log.e(TAG, "onMsgRspAddWatch-->OpenStreamAgent, devId=" + devId + ",port=" + port + ",audioParm=" + tap + ",ret=" + ret);
                            //s_pca.FreeProtAgent(port);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 根据摄像机音频参数，配置AudioPlayer的参数
                if (null != m_devMap.get(devId) && ret >= 0) {
                    AudioPlayer audioPlayer = m_devMap.get(devId).m_audio;
                    if (audioPlayer != null) {
                        AudioPlayer.MyAudioParameter audioParameter = new AudioPlayer.MyAudioParameter(ts.getAudioParam().getSamplerate(), ts.getAudioParam().getChannels(), ts.getAudioParam().getSamplebitswitdh());
                        audioPlayer.initAudioParameter(audioParameter);
                        Log.i(TAG, "Audio is init....");
                    } else {
                        Log.w(TAG, "Audio isn't init....");
                    }
                }
            }

            //0表示decDataCB解码出来的为yuv数据, 非0表示对应位数的rgb数据（支持的位数有：16、24、32）
            ret = s_pca.PlayAgent(port, 0);
            Log.d(TAG, "onMsgRspAddWatch-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
            if (ret != 0) {
                Log.e(TAG, "onMsgRspAddWatch-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                try {
                    Thread.sleep(100);
                    ret = s_pca.PlayAgent(port, 0);
                    Log.d(TAG, "onMsgRspAddWatch-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                    if (ret != 0) {
                        Log.e(TAG, "onMsgRspAddWatch-->PlayAgent, devId=" + devId + ",port=" + port + ",ret=" + ret);
                        s_pca.FreeProtAgent(port);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //记录端口句柄和设备ID间的映射关系
            dev.m_port_id = port;
            mPortOrIDMap.put(port + "", devId);
        }
    }

    private void onMsgAlarm(byte[] pData, int nDataLen) {
        Log.i(TAG, "recv alarm message.");
        if (null == pData) return;
        if (!Config.m_enable_alarm) return;
        ByteBuffer byteBuffer = ByteBuffer.allocate(nDataLen);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(pData, 0, nDataLen);
        byteBuffer.rewind();
        TPS_AlarmInfo ai = (TPS_AlarmInfo) TPS_AlarmInfo.createObjectByByteBuffer(byteBuffer);
        String devId = new String(ai.getSzDevId()).trim();
        // 设备未处于播放状态
        /*OpenglesRender _glRender = mRendersMap.get(devId);
        if (null == _glRender) return;*/
        PlayerDevice device = findDeviceByID(devId);
        if (null == device) return;
        String desc = new String(ai.getSzDesc()).trim();
        desc = device.m_dev.getDevName() + "(" + devId + "):" + ConstantImpl.getAlarmTypeDesc(ai.getnType());

        Global.saveAlarmMessage(ai);
        sendAlarmMessage(Global.MSG_ADD_ALARM_DATA, 0, 0, ai);

        Intent intent = new Intent(Define.INTENT_ACTION_ALARM_EVENT);
        //intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //intent.setClass(Global.m_ctx, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity2.DEVICE_ID_KEY, device.m_dev.getDevId());
        //Intent intent = activity.getIntent();
        MessageNotification.getInstance().message(R.drawable.ico_launcher, "告警信息", desc, intent);
        if (Config.m_enable_alarm) MediaPlayer.play();
    }

    private void onSearchAlarmResp(int nMsgType, byte[] pData, int nDataLen) {
        if (nDataLen <= 0) return;
        String xml = new String(pData).trim();
        MessageList lstMsg = (MessageList) new MessageList().fromXML(pData, "xml");
        sendMessage(nMsgType, 0, 0, lstMsg);
    }

    private void onGetFriendListResp(int nMsgType, byte[] pData, int nDataLen) {
        if (nDataLen <= 0) return;
        FriendList friendList = (FriendList) new FriendList().fromXML(pData, "xml");
        for (String key : friendList.m_lstFriend.keySet()) {
            FriendList.Friend friend = friendList.m_lstFriend.get(key);
            if (null == friend) continue;
            if (null == Global.m_friends.findById(friend.m_id)) {
                Global.m_friends.m_lstFriend.put(friend.m_name, friend);
            }
        }

        sendMessage(nMsgType, 0, 0, friendList);
    }

    private void onGetOffineMsgResp(int nMsgType, byte[] pData, int nDataLen) {
        if (nDataLen <= 0) return;
        FriendMessageList lstMsg = (FriendMessageList) new FriendMessageList().fromXML(pData, "xml");
        Global.m_messges = lstMsg;
        sendMessage(nMsgType, 0, 0, lstMsg);
    }

    private void onPtzReqResp(int nMsgType, byte[] pData, int nDataLen) {
        if (nDataLen <= 0) return;
        String data = new String(pData).trim();
        sendMessage(nMsgType, 0, 0, data);
    }

    private final Event m_event = new Event();
    private final Event m_online_event = new Event();

    public void generateSnaphost(List<PlayerDevice> lst) {
        final List<PlayerDevice> lstDev = new ArrayList<>();
        lstDev.addAll(lst);
        if (null != m_snapshotThread && m_snapshotThread.isAlive()) return;
        m_snapshotThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!m_exit) {
                    if (m_stop_snapshot) break;
                    for (PlayerDevice dev : lstDev) {
                        if (m_exit || m_stop_snapshot) break;
                        if (null == dev.m_dev) continue;
                        if (dev.m_dev.getOnLine() == Device.OFFLINE) {
                            Log.i(TAG, "generateSnapshot dev offline, devId=" + dev.m_devId);
                            continue;
                        }

                        if (!dev.m_connect_ok) {
                            Log.d(TAG, "generateSnapshot dev not connect ok, skip, devId=" + dev.m_devId);
                            continue;
                        }

                        if (dev.m_replay) {
                            Log.d(TAG, "generateSnapshot dev is replay, skip, devId=" + dev.m_devId);
                            continue;
                        }

                        try {
                            String devId = dev.m_devId;
                            generateSnapshot(dev.m_dev);
                            if (m_event.timedWait(30000)) {
                                Log.d(TAG, "generateSnapshot wait timeout, devId=" + devId);
                            }
                            removeSnapshot(devId);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    try {
                        synchronized (m_online_event) {
                            m_online_event.wait(60000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.i(TAG, "generateSnapshot, the thread exit.");
                m_snapshotThread = null;
            }
        });

        m_snapshotThread.start();
    }

    private void generateSnapshot(Device dev) {
        generateSnapshot(dev, Define.SUB_STREAM_TYPE, 0);
    }

    private void generateSnapshot(Device dev, int nStreamNo, int nFrameType) {
        if (null == dev) return;
        String devId = dev.getDevId();
        Log.i(TAG, "generateSnapshot devId=" + devId);
        if (m_snapshotMap.get(devId) != null) {
            synchronized (m_event) {
                m_event.notify();
            }

            return;
        }

        m_snapshotMap.put(devId, dev);
        PlayerDevice pdev = Global.getDeviceById(devId);
        if (null == pdev) return;
        // 当前正在播放
        if (null != pdev.m_video) return;
        pdev.m_snapshot = true;
        int ret = addWatch(devId, nStreamNo, nFrameType, 0);
        if (0 != ret) {
            synchronized (m_event) {
                m_event.notify();
            }
        }
    }

    private void removeSnapshot(String devId) {
        Log.i(TAG, "generateSnapshot removeSnapshot,devId=" + devId);
        m_snapshotMap.remove(devId);
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;
        dev.m_snapshot = false;
        if (null == dev.m_video) {
            // 当前设备未在播放，断开
            stopWatch(devId);
        }
    }

    private void notifyNextSnapshot(String devId) {
        if (null != m_snapshotThread && m_snapshotThread.isAlive()) {
            synchronized (m_event) {
                Log.d(TAG, "generateSnapshot notify thread snapshot next device");
                m_event.notify();
            }

            return;
        }

        // 如果快照线程不存在，则手动移除当前设备
        removeSnapshot(devId);
    }

    class MediaDataBuffer {
        String devId;
        int mediaType;
        byte frameData[];
        int dataLen;
        byte extData[];
    }

    Queue<MediaDataBuffer> m_mediaDataQueue = new LinkedBlockingQueue<>();
    Stack<MediaDataBuffer> s = new Stack<>();

    final Thread m_mediaDataThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (m_mediaDataQueue.isEmpty()) {
                    try {
                        synchronized (m_mediaDataThread) {
                            m_mediaDataThread.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }

                MediaDataBuffer buf = m_mediaDataQueue.poll();
                if (null == buf) continue;
                //mediaDataProc(buf.devId, buf.mediaType, buf.frameData, buf.dataLen, buf.extData);
            }
        }
    });

    /*@Override
    public int mediaRecvCB(byte[] pDevId, int nMediaType, byte[] pFrameData, int nDataLen, byte[] pExtData) {
        String devId = new String(pDevId).trim();
        *//*Log.w(TAG, "mediaRecvCB:nMediaType=" + nMediaType + ",nDataLen=" + nDataLen);
        MediaDataBuffer buf = new MediaDataBuffer();
        buf.devId = devId;
        buf.mediaType = nMediaType;
        buf.frameData = ByteBuffer.wrap(pFrameData, 0, nDataLen).array();
        buf.dataLen = nDataLen;
        buf.extData = ByteBuffer.wrap(pExtData, 0, pExtData.length).array();
        m_mediaDataQueue.add(buf);
        synchronized (m_mediaDataThread) {
            m_mediaDataThread.notify();
        }
        Log.w(TAG, "mediaRecvCB2:nMediaType=" + nMediaType + ",nDataLen=" + nDataLen);
        return 0;*//*
    }*/

    @Override
    public int mediaRecvCB(byte[] pDevId, int nMediaType, byte[] pFrameData, int nDataLen, int isKey, double timestamp) {
        String devId = new String(pDevId).trim();
        Log.d(TAG, "MediaRecvCallBack:devId=" + devId + ",nMediaType=" + nMediaType + ",nDataLen=" + nDataLen + ",isKey=" + isKey + ",timestamp=" + timestamp);
        if (m_exit || m_stop_play) return 0;
        if (devId.contains(".flv")) devId = devId.substring(0, devId.indexOf('/'));

        //if (true) return 0;

        int ret = -1;
        if (0 == nMediaType) {
            if (null != m_snapshotMap.get(devId)) {
                PlayerDevice dev = Global.getDeviceById(devId);
                if (null == dev) return 0;
                if (dev.m_port_id > -1) ret = s_pca.InputVideoDataAgent(dev.m_port_id, pFrameData, nDataLen, isKey, (int) timestamp);
                if (!dev.m_play && !dev.m_replay) return 0;
            }
        }

        Device sdev = m_snapshotMap.get(devId);
        PlayerDevice dev = m_devMap.get(devId);
        if (null == sdev && (null == dev || (!dev.m_play && !dev.m_replay))) {
            //stopWatch(devId);
            return 0;
        }

        if (null == dev) return 0;
        if (dev.m_port_id < 0 && dev.m_replay_port_id < 0 && dev.m_open_video_stream_result < 0) {
            return 0;
        }

        if (!dev.m_first_frame) {
            dev.m_first_frame = true;
            sendMessage(Define.MSG_RECEIVER_MEDIA_FIRST_FRAME, 0, 0, dev);
        }

        if (!TextUtils.isEmpty(dev.m_tipInfo)) {
            setTipText(devId, "");
        }

        OpenglesRender _glRender = dev.m_video;
        if (null == _glRender) return -1;
        if (!_glRender.isSurfaceVisible() && _glRender.getFrameBufferSize() > 3) {
            Log.w(TAG, "MediaRecvCallBack:FrameBufferSize > 3, FrameBufferSize=" + _glRender.getFrameBufferSize());
            _glRender.updateView(null);
            return 0;
        }

        if (!dev.m_replaying) {
            if (m_stop_render) return 0;
        }

        if (_glRender.getFrameBufferSize() > 3) {
            Log.w(TAG, "MediaRecvCallBack:FrameBufferSize > 3, FrameBufferSize=" + _glRender.getFrameBufferSize());
            _glRender.updateView(null);
            return 0;
        }

        int port = dev.m_play ? dev.m_port_id : dev.m_replay_port_id;
        if (nMediaType == 0) {//video
            if (s_pca != null && nDataLen > 0) {
                dev.m_replay_timestamp = timestamp;
                Log.i(TAG, "MediaRecvCallBack:video,devId=" + devId + ",isKey=" + isKey + ",timestamp=" + dev.m_replay_timestamp);

//				    //去除16个字节头的视频数据
//					int _fSize = nDataLen - TPS_EXT_DATA.SIZE;
//					byte[] _fData = ByteBuffer.wrap(pFrameData, TPS_EXT_DATA.SIZE, _fSize).array();

                //接收数据交给底层库解码，解码回调函数为：decDataCB（如需要自己处理，可不调用此函数）
                int count = 0;
                while (count < 3) {
                    count++;
                    ret = s_pca.InputVideoDataAgent(port, pFrameData, nDataLen, isKey, (int) timestamp);
                    if (ret != 0) {
                        Log.e(TAG, "MediaRecvCallBack:video,InputVideoDataAgent,return " + ret);
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        continue;
                    }

                    break;
                }

                if (ret != 0) {
                    Log.e(TAG, "MediaRecvCallBack:video,InputVideoDataAgent,return " + ret);
                    return -1;
                }

                if (dev.m_replaying) sendMessage(MSG_REPLAY_SET_POSITION, 0, 0, dev.m_replay_timestamp);
                Log.i(TAG, "MediaRecvCallBack:video,end");
            }
        } else if (nMediaType == 1) {//audio
            if (s_pca != null && nDataLen > 0) {
                if (!dev.m_voice) return 0;
                if (dev.m_open_audio_stream_result < 0) return 0;
                //接收数据交给底层库解码，解码回调函数为：decDataCB（如需要自己处理，可不调用此函数）
                ret = s_pca.InputAudioDataAgent(port, pFrameData, nDataLen, (int) timestamp);
                Log.i(TAG, "MediaRecvCallBack:audio,InputVideoDataAgent,return " + ret);
            } else {
                Log.w(TAG, "MediaRecvCallBack:audio is failed.");
            }
        } else if (nMediaType == 2) {
            //onReplayDevFileResp();
        }

        return 0;
    }

    @Override
    public int decDataCB(int nPort, byte[] pDecData, int nSize, int nWidth, int nHeight, int nTimestamp, int nType, int nFrameRate, int nIsVideo, byte[] pUser) {
        String devId = mPortOrIDMap.get(nPort + "");
        Log.i(TAG, "DecThreadProc:decDataCB:begin, devId=" + devId);
        //ByteBuffer bBuf = (ByteBuffer) ByteBuffer.wrap(pFrameInfo).order(ByteOrder.nativeOrder()).rewind();
        //FRAME_INFO fInfo = (FRAME_INFO) FRAME_INFO.createObjectByByteBuffer(bBuf);

        //if (true) return 0;

        if (nIsVideo == 1) {
            Log.i(TAG, "DecThreadProc:decDataCB:devId=" + devId + ",nPort=" + nPort + ",nSize=" + nSize + ",is video=" + nIsVideo + ",timestamp=" + nTimestamp);
            if (null != m_snapshotMap.get(devId)) {
                snapshotPlay(devId, nPort, pDecData, nSize, nWidth, nHeight, nTimestamp, nType, nFrameRate, nIsVideo, pUser);
            }
        }

        PlayerDevice dev = m_devMap.get(devId);
        if (null == dev) return 0;
        // 已经调用stopWatch但还有数据发过来,同一窗口播放2个设备的情况
        if (!dev.m_play && !dev.m_replay) {
            Log.w(TAG, "DecThreadProc:decDataCB:devId＝" + devId + " is already stop !!!");
            stopWatch(devId);
            return 0;
        }

        OpenglesRender _glRender = dev.m_video;
        if (null == _glRender) return -1;
        if (!_glRender.isSurfaceVisible() && _glRender.getFrameBufferSize() > 3) {
            Log.w(TAG, "DecThreadProc:decDataCB:FrameBufferSize > 3, FrameBufferSize=" + _glRender.getFrameBufferSize());
            _glRender.updateView(null);
            return 0;
        }
        if (!dev.m_replaying) {
            if (m_stop_render) return 0;
        }

        if (_glRender.getFrameBufferSize() > 3) {
            Log.w(TAG, "DecThreadProc:decDataCB:FrameBufferSize > 3, FrameBufferSize=" + _glRender.getFrameBufferSize());
            _glRender.updateView(null);
            return 0;
        }

        if (nSize > FRAME_INFO.SIZE) {
            /*ByteBuffer bBuf = (ByteBuffer) ByteBuffer.wrap(pFrameInfo).order(ByteOrder.nativeOrder()).rewind();
            FRAME_INFO fInfo = (FRAME_INFO) FRAME_INFO.createObjectByByteBuffer(bBuf);*/
            // 处理解码后的媒体数据
            //处理视频数据(yuv数据)
            if (nIsVideo == 1 && nWidth > 0 && nHeight > 0) {//处理视频数据(yuv或rgb数据)
                FrameBuffer buf = new FrameBuffer(nWidth, nHeight);
                Integer state = (devId == null) ? null : mVideoStateMap.get(devId);
                if ((state != null) && state == OpenglesRender.VIDEO_STATE_START) {
                    mVideoStateMap.put(devId, OpenglesRender.VIDEO_STATE_PLAYING);
                    setRenderVideoState(devId, OpenglesRender.VIDEO_STATE_PLAYING);
                    PlayerDevice device = findDeviceByID(devId);
                    if (device != null) device.m_dev.setVideoLiveState(OpenglesRender.VIDEO_STATE_PLAYING);
                    setTipText(devId, R.string.tv_video_play_tip);//"视频播放中."
                }

                Log.d(TAG, "DecThreadProc:decDataCB:video,devId=" + devId + ",port=" + nPort + ",timestamp=" + nTimestamp + ",size=" + nSize + ",render=" + _glRender);
                // pDecData:TPS_VIDEO_FRAME_HEADER(16)+y+v+u,去除16字节头
                //buf.fData = new byte[nSize];
                buf.fData = pDecData;
                //System.arraycopy(pDecData, 0, buf.fData, 0, buf.fData.length);
                _glRender.updateView(buf);
                dev.m_playing = true;
                Log.d(TAG, "DecThreadProc:decDataCB:video,updateView end");
            } else if (nIsVideo == 0) {//处理音频数据(pcm数据)
                Log.d(TAG, "DecThreadProc:decDataCB:audio,devId=" + devId + ",timestamp=" + nTimestamp + ",size=" + nSize);
                audioCallback(mPortOrIDMap.get(nPort + ""), pDecData, nSize);
            }
            //Log.i(TAG, "decDataCB:data is success:nSize=" + nSize + "," + fInfo.toString());
        } else {
            Log.i(TAG, "DecThreadProc:decDataCB:data is fail");
        }

        return 0;
    }

    private void snapshotPlay(String devId, int nPort, byte[] pDecData, int nSize, int nWidth, int nHeight, int nTimestamp, int nType, int nFrameRate, int nIsVideo, byte[] pUser) {
        Log.d(TAG, "generateSnapshot snapshotPlay, devId=" + devId);
        if (nSize <= FRAME_INFO.SIZE) return;
        // 处理解码后的媒体数据
        if (nIsVideo == 1 && nWidth > 0 && nHeight > 0) {//处理视频数据(yuv或rgb数据)
            String filename = Global.getSnapshotDir() + "/" + devId + ".jpg";
            // pDecData:TPS_VIDEO_FRAME_HEADER(16)+y+v+u,去除16字节头
            /*buf.fData = new byte[nSize - 16];
            System.arraycopy(pDecData, 16, buf.fData, 0, buf.fData.length);
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                YuvImage img = new YuvImage(buf.fData, ImageFormat.NV21, fInfo.getnWidth(), fInfo.getnHeight(), null);
                img.compressToJpeg(new Rect(0, 0, fInfo.getnWidth(), fInfo.getnHeight()), 100, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            int[] pRgbBuffer = new int[nWidth * nHeight * 16 / 8];
            s_pca.YUV2IntRGBAgent(nWidth, nHeight, pDecData, pRgbBuffer, 16);
            Bitmap _bitmap = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.RGB_565);
            _bitmap.setPixels(pRgbBuffer, 0, nWidth, 0, 0, nWidth, nHeight);

            try {
                FileOutputStream fos = new FileOutputStream(filename);
                _bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
                _bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Helper.generateJPEG(buf.fData, 800, 600, 80, filename);
            notifyNextSnapshot(devId);
            removeSnapshot(devId);
        }
    }

    public int SetAutoRecvAlm(String m_device_id, boolean checked) {
        return s_func.SetAutoRecvAlm(m_device_id, checked ? 1 : 0);
    }

    public void initAutoRecvAlarm() {
        List<DeviceSetting> lst = DeviceSetting.findAll();
        for (DeviceSetting setting : lst) {
            if (!setting.getEnableAlarm()) continue;
            SetAutoRecvAlm(setting.getDevId(), setting.getEnableAlarm());
        }
    }

    public int getMediaParam(PlayerDevice dev) {
        if (null == dev) return -1;
        return FunclibAgent.getInstance().GetP2PDevConfig(dev.m_devId, 501);
    }
}

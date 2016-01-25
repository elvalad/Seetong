package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.FriendList;
import com.seetong.app.seetong.model.FriendMessageList;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatMessage extends BaseActivity implements View.OnClickListener {
    FriendList.Friend m_friend;
    private ProgressDialog mTipDlg;
    private ListView m_listView;
    private ChatMessageViewAdapter mAdapter;
    EditText m_txt_send;

    private PullToRefreshListView mPullRefreshListView;

    private int m_queryCount = 20;
    private int m_queryPage = 0;
    private String m_queryStartTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LibImpl.getInstance().addHandler(m_handler);
        setContentView(R.layout.chat_message);
        //m_listView = (ListView) findViewById(R.id.listview);
        String id = getIntent().getStringExtra(Constant.EXTRA_FRIEND_ID);
        m_friend = Global.m_friends.findById(id);
        m_friend.m_inChat = true;
        ((TextView) findViewById(R.id.txt_title)).setText(m_friend.m_name);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        m_txt_send = (EditText) findViewById(R.id.et_sendmessage);

        findViewById(R.id.btn_add_share_video).setOnClickListener(this);

        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.listview);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(Global.m_ctx, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                new GetDataTask().execute();
            }
        });

        m_listView = mPullRefreshListView.getRefreshableView();

        m_friend.m_newMsgCount = 0;
        MainActivity.m_this.getFriendFragment().updateList();

        /*Button btnFinish = (Button) findViewById(R.id.btn_title_right);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);*/
        loadData();
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData() {
        String sender = m_friend.m_name;
        String receiver = Global.m_devInfo.getUserName();
        List<FriendMessageList.Message> lstMsg = FriendMessageList.Message.findByFromTo(sender, receiver);
        m_friend.m_msgList.addAll(lstMsg);

        sender = Global.m_devInfo.getUserName();
        receiver = m_friend.m_name;
        lstMsg = FriendMessageList.Message.findByFromTo(sender, receiver);
        m_friend.m_msgList.addAll(lstMsg);

        m_friend.m_msgList.sortChatMessageByMsgId();
        mAdapter = new ChatMessageViewAdapter(this, m_friend.m_msgList.getMessageList());
        m_listView.setAdapter(mAdapter);

        new GetDataTask().execute();
    }

    private class GetDataTask extends AsyncTask<Void, Void, FriendMessageList> {

        @Override
        protected FriendMessageList doInBackground(Void... params) {
            FriendMessageList.Message msg = FriendMessageList.Message.findByLastReceiver(m_friend.m_name);
            if (null != msg) m_queryStartTime = msg.m_time;
            LibImpl.getInstance().getFuncLib().ReadOfflineMsg(m_friend.m_id, m_queryPage, m_queryCount, m_queryStartTime, "");

            synchronized (m_friend) {
                try {
                    m_friend.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return m_friend.m_msgList;
        }

        @Override
        protected void onPostExecute(FriendMessageList result) {
            updateList();
            mPullRefreshListView.onRefreshComplete();
            super.onPostExecute(result);
        }
    }

    private void updateList() {
        m_friend.m_msgList.sortChatMessageByMsgId();
        mAdapter.notifyDataSetChanged();
        m_listView.setSelection(m_listView.getCount() - 1);
    }

    public void saveData() {
        /*m_new_alarm_config = (NetSDK_Alarm_Config) m_alarm_config.clone();
        m_new_alarm_config.motionDetectAlarm.Enable = m_tb_enable_alarm.isChecked() ? "1" : "0";
        m_new_alarm_config.addHead(false);
        String xml = m_new_alarm_config.getMotionDetectAlarmXMLString();
        int ret = FunclibAgent.getInstance().SetP2PDevConfig(m_device_id, 822, xml);
        if (0 != ret) {
            sendMyToast(R.string.dlg_set_motion_detect_alarm_param_fail_tip);
            return;
        }

        mTipDlg.setCallback(null);
        showTipDlg(R.string.dlg_set_motion_detect_alarm_param_tip, 20000, R.string.dlg_set_motion_detect_alarm_param_timeout_tip);*/

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                break;
            case R.id.btn_send:
                onBtnSend();
                break;
            case R.id.btn_add:
                onBtnAdd();
                break;
            case R.id.btn_add_share_video:
                onBtnAddShareVideo();
                break;
            default: break;
        }
    }

    private void onBtnAddShareVideo() {
        View v = findViewById(R.id.layout_chat_add_content);
        v.setVisibility(View.GONE);
        Intent it = new Intent(this, ChatAddVideo.class);
        startActivityForResult(it, Constant.CHAT_ADD_VIDEO_REQ_ID);
    }

    private void onBtnAdd() {
        View v = findViewById(R.id.layout_chat_add_content);
        v.setVisibility(v.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void onBtnSend() {
        String contString = m_txt_send.getText().toString();
        if (contString.trim().length() <= 0) return;

        FriendMessageList.Message msg = new FriendMessageList.Message();
        msg.m_id = getDate("yyyyMMddHHmmss");
        msg.m_type = "10000";
        msg.m_from = Global.m_devInfo.getUserName();
        msg.m_to = m_friend.m_name;
        msg.m_msg = contString;
        msg.m_time = getDate("yyyy-MM-dd HH:mm:ss");
        long msgId = LibImpl.getInstance().getFuncLib().SendOfflineMsg(m_friend.m_id, msg.m_msg);
        if (msgId < 0) {
            toast(R.string.message_send_failed);
            return;
        }

        msg.m_id = String.valueOf(msgId);
        msg.save();

        m_friend.m_msgList.add(msg);
        mAdapter.notifyDataSetChanged(m_friend.m_msgList.getMessageList());

        m_txt_send.setText("");
        m_listView.setSelection(m_listView.getCount() - 1);
    }

    private String getDate(String format) {
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        return fmt.format(new Date());
        /*Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(c.get(Calendar.MONTH));
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
        String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String mins = String.valueOf(c.get(Calendar.MINUTE));
        String seconds = String.valueOf(c.get(Calendar.SECOND));
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins + ":" + seconds);
        return sbBuffer.toString();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        LibImpl.getInstance().removeHandler(m_handler);
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
        MainActivity.m_this.getFriendFragment().decNewMsgCount(m_friend.m_newMsgCount);
        m_friend.m_newMsgCount = 0;
        m_friend.m_inChat = false;
        MainActivity.m_this.getFriendFragment().updateList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case Constant.CHAT_ADD_VIDEO_REQ_ID:
                String devId = data.getStringExtra(Constant.EXTRA_DEVICE_ID);
                int share_time = data.getIntExtra(Constant.EXTRA_CHAT_SHARE_VIDEO_TIME, 3);
                onAddVideo(devId, share_time);
                break;
            default: break;
        }
    }

    private void onAddVideo(String devId, int share_time) {
        String contString = T(R.string.video_share_send_prompt) + "(" + devId + ")";
        FriendMessageList.Message msg = new FriendMessageList.Message();
        msg.m_id = getDate("yyyyMMddHHmmss");
        msg.m_type = "10000";
        msg.m_from = Global.m_devInfo.getUserName();
        msg.m_to = m_friend.m_name;
        msg.m_msg = contString;
        msg.m_time = getDate("yyyy-MM-dd HH:mm:ss");
        long msgId = LibImpl.getInstance().getFuncLib().PutVideoMsg(m_friend.m_id, devId, share_time);
        if (msgId < 0) {
            toast(R.string.message_send_failed);
            return;
        }

        msg.m_id = String.valueOf(msgId);
        //msg.save();

        m_friend.m_msgList.add(msg);
        mAdapter.notifyDataSetChanged(m_friend.m_msgList.getMessageList());
        m_listView.setSelection(m_listView.getCount() - 1);
    }

    public void onLongClickMsg(final FriendMessageList.Message msg) {
        if (null == msg) return;
        final Context ctx = this;
        final String presets[] = {T(R.string.delete)};
        new AlertDialog.Builder(ctx).setTitle("")
                .setItems(presets, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (0 == which) {
                            final int ret = LibImpl.getInstance().getFuncLib().DeleteOfflineMsg(msg.m_id, m_friend.m_id);
                            if (ret != 0) {
                                toast(ConstantImpl.getDeleteOfflineMsgErrText(ret));
                                return;
                            }
                        }

                        msg.delete();
                        m_friend.m_msgList.getMessageList().remove(msg);
                        mAdapter.notifyDataSetChanged(m_friend.m_msgList.getMessageList());
                        dialog.dismiss();
                    }
                }).create().show();
    }

    public void startPlayDevice(final FriendMessageList.Message message) {
        if (null == message) return;
        String msg = message.m_real_msg;
        if (null == msg || "".equals(msg)) return;
        String param[] = msg.split("@");
        if (param.length < 5) {
            MainActivity.m_this.toast(R.string.request_video_failed);
            return;
        }

        final int share_time = Integer.parseInt(msg.substring(msg.indexOf("@time=") + 6));
        msg = msg.substring(0, msg.indexOf("@time="));
        final String pwd = msg.substring(msg.indexOf("@loginpsw=") + 10);
        msg = msg.substring(0, msg.indexOf("@loginpsw="));
        final String user = msg.substring(msg.indexOf("@loginuser=") + 11);
        msg = msg.substring(0, msg.indexOf("@loginuser"));
        final String devId = msg.substring(msg.indexOf("@devid=") + 7);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTipDlg(R.string.tv_video_req_tip, 30000, R.string.request_video_failed);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerDevice dev = Global.getDeviceById(devId);
                if (null == dev) {
                    // 不是自己的设备，先请求登录
                    int ret = LibImpl.getInstance().getFuncLib().LoginShareDev(devId, user, pwd);
                    if (ret != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTipDlg.dismiss();
                                MainActivity.m_this.toast(R.string.request_video_failed);
                            }
                        });
                        return;
                    }

                    // 登录后已经返回了TPS_MSG_NOTIFY_DEV_DATA消息，设备已经添加，再查询一次
                    dev = Global.getDeviceById(devId);
                    if (null == dev) {
                        dev = new PlayerDevice();
                        dev.m_devId = devId;
                        dev.m_dev = new Device();
                        dev.m_dev.setDevId(devId);
                        Global.addDevice(dev);
                    }

                    dev.m_friend_share = true;
                }

                dev.m_share_video_timestamp = share_time * 60;
                LibImpl.getInstance().getFuncLib().DeleteOfflineMsg(message.m_id, "-1");
                message.delete();
                dev.m_user = user;
                dev.m_pwd = pwd;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTipDlg.dismiss();
                        PlayerDevice dev = Global.getDeviceById(devId);
                        MainActivity.m_this.addDeviceToLive(dev);
                        finish();
                    }
                });
            }
        }).start();
    }

    private void startPlay(PlayerDevice dev) {
    }

    @Override
    public void handleMessage(Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_GET_OFFLINE_MSG:
                onRspGetOfflineMsg(msg);
                break;
        }
    }

    private void onRspGetOfflineMsg(Message message) {
        FriendMessageList lstMsg = (FriendMessageList) message.obj;
        if (null == lstMsg) return;

        try {
            if (m_queryCount < Integer.parseInt(lstMsg.m_allCount)) {
                FriendMessageList.Message msg = FriendMessageList.Message.findByLastReceiver(m_friend.m_name);
                if (null != msg) {
                    m_queryStartTime = msg.m_time;
                } else {
                    m_queryPage++;
                }

                new GetDataTask().execute();
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        /*for (FriendMessageList.Message msg : lstMsg.getMessageList()) {
            if (msg.m_type.equals("10000")) {
                msg.m_to = Global.m_devInfo.getUserName();
                msg.save();
                m_friend.m_msgList.add(msg);
            }
        }*/

        synchronized (m_friend) {
            m_friend.notify();
        }
    }
}
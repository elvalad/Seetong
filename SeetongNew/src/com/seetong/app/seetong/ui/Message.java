package com.seetong.app.seetong.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.MessageList;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.tools.Event;
import ipc.android.sdk.com.SDK_CONSTANT;
import ipc.android.sdk.com.TPS_AlarmInfo;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2014-05-12.
 */
public class Message extends BaseActivity {
    private MessageList m_data = new MessageList();
    private List<TPS_AlarmInfo> m_local_data = new ArrayList<>();
    private ListView m_lvAlarm;
    private AlarmListAdapter m_adapter;
    private final Event m_event = new Event();

    //private SwipeRefreshLayout m_swipeLayout;
    private PullToRefreshListView mPullRefreshListView;

    private int m_local_page = 0;
    private int m_queryCount = 20;
    private int m_queryPage = 0;
    private String m_queryStartTime = "";

    private boolean m_first_init = true;
    private ProgressDialog mTipDlg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LibImpl.getInstance().addHandler(m_handler);
        setContentView(R.layout.alarm_message);
        initWidget();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_SEARCH_ALARM:
                onRspSearchAlarm(msg);
        }
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.please_wait_communication);
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);

        //m_lvAlarm = (ListView) findViewById(R.id.lv_AlarmList);

        /*m_swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipe_refresh);
        m_swipeLayout.setOnRefreshListener(this);
        m_swipeLayout.setColorScheme(R.color.holo_red_light, R.color.holo_green_light,
                R.color.holo_blue_bright, R.color.holo_orange_light);*/

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        // Set a listener to be invoked when the list should be refreshed.
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(Global.m_ctx, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                // Do work to refresh the list here.
                m_first_init = false;
                new GetDataTask(m_queryPage).execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(Global.m_ctx, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                m_local_page = m_local_data.size() / m_queryCount;
                // Do work to refresh the list here.
                m_first_init = false;
                new GetLocalDataTask(m_local_page).execute();
            }
        });

        // Add an end-of-list listener
        /*mPullRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                Toast.makeText(AlarmMessageUI.this, "End of List!", Toast.LENGTH_SHORT).show();
            }
        });*/

        m_lvAlarm = mPullRefreshListView.getRefreshableView();
        m_adapter = new AlarmListAdapter(Global.m_ctx);

        m_lvAlarm.setAdapter(m_adapter);
        m_lvAlarm.setOnItemClickListener(m_adapter);

        List<MessageList.Message> lst = new ArrayList<>();
        lst.addAll(m_data.m_lstMessage.values());
        m_adapter.setListData(lst);
        m_adapter.setListData(lst);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData() {
        new GetDataTask(0).execute();
    }

    private class GetDataTask extends AsyncTask<Void, Void, MessageList> {

        public GetDataTask(int page) {
            m_page = page;
        }

        @Override
        protected MessageList doInBackground(Void... params) {
            getAlarmMessage(m_page);
            synchronized (m_event) {
                try {
                    m_event.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return m_data;
        }

        @Override
        protected void onPostExecute(MessageList result) {
            List<MessageList.Message> lst = new ArrayList<>();
            lst.addAll(m_data.m_lstMessage.values());
            m_adapter.setListData(lst);
            m_adapter.notifyDataSetChanged();

            // Call onRefreshComplete when the list has been refreshed.
            mPullRefreshListView.onRefreshComplete();
            super.onPostExecute(result);
        }

        private int m_page;
    }

    private class GetLocalDataTask extends AsyncTask<Void, Void, List<TPS_AlarmInfo>> {

        public GetLocalDataTask(int page) {
            m_page = page;
        }

        @Override
        protected List<TPS_AlarmInfo> doInBackground(Void... params) {
            List<String> devIdAry = Global.getSelfDeviceIdAry();
            if (devIdAry.isEmpty()) return new ArrayList<>();
            return Global.m_alarmMessage.getAlarmMessage(devIdAry, m_queryCount, m_page * m_queryCount);
        }

        @Override
        protected void onPostExecute(List<TPS_AlarmInfo> result) {
            if (result.isEmpty()) {
                toast(T(R.string.no_more_message));
                mPullRefreshListView.onRefreshComplete();
                super.onPostExecute(result);
                return;
            }

            for (TPS_AlarmInfo ai : result) {
                if (!m_local_data.contains(ai)) m_local_data.add(ai);
            }

            MergeMessage(result);
            List<MessageList.Message> lst = new ArrayList<>();
            lst.addAll(m_data.m_lstMessage.values());
            m_adapter.setListData(lst);
            m_adapter.notifyDataSetChanged();

            // Call onRefreshComplete when the list has been refreshed.
            mPullRefreshListView.onRefreshComplete();
            super.onPostExecute(result);
        }

        private int m_page;
    }

    private void MergeMessage(List<TPS_AlarmInfo> data) {
        for (TPS_AlarmInfo v : data) {
            MessageList.Message msg = new MessageList.Message();
            msg.m_dev_id = new String(v.getSzDevId()).trim();
            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long time = (long)(v.getnTimestamp()) * 1000;
            Date dt = new Date(time);
            msg.m_alarm_id = "local_" + System.currentTimeMillis();
            msg.m_alarm_time = fmt.format(dt);
            msg.m_alarm_time_long = time;
            switch (v.getnType()) {
                case ConstantImpl.TPS_ALARM_MOTION:
                case ConstantImpl.TPS_ALARM_MOTION_DETECT:
                    msg.m_alarm_type_id = "1";
                    break;
                case ConstantImpl.TPS_ALARM_GPIO3_HIGH2LOW:
                    msg.m_alarm_type_id = "2";
                    break;
                default:
                    msg.m_alarm_type_id = "-1";
            }

            m_data.m_lstMessage.put(msg.m_alarm_id, msg);
        }
    }

    /*@Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                m_swipeLayout.setRefreshing(false);
                m_data = Global.m_db.getAlarmMessage();
                m_adapter.setListData(m_data);
                m_adapter.notifyDataSetChanged();
            }
        }, 500);
    }*/

    private void getAlarmMessage(int page) {
        int ret = 0;
        if (Global.m_loginType == Define.LOGIN_TYPE_USER || Global.m_loginType == Define.LOGIN_TYPE_DEMO) {
            ret = LibImpl.getInstance().getFuncLib().SearchUserDevAlarm(page, m_queryCount, m_queryStartTime, "");
        } else if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {
            if (null == Global.m_devInfo) return;
            String devId = Global.m_devInfo.getDevGroupName();
            if (null == devId || "".equals(devId)) {
                devId = Global.m_devInfo.getDevId();
            } else {
                List<PlayerDevice> lst = Global.getDeviceByGroup(devId);
                if (lst.isEmpty()) return;
                devId = lst.get(0).m_dev.getDevId();
            }
            ret = LibImpl.getInstance().getFuncLib().SearchDevAlarm(devId, page, m_queryCount, m_queryStartTime, "");
        }

        if (ret != 0) {
            mTipDlg.dismiss();
            toast(ConstantImpl.getSearchDevAlarmErrText(ret));
        }
    }

    private void onRspSearchAlarm(android.os.Message msg) {
        mTipDlg.dismiss();
        MessageList lstMessage = (MessageList) msg.obj;
        if (null == lstMessage) return;
        if (!lstMessage.m_lstMessage.isEmpty()) {
            m_data.m_lstMessage.putAll(lstMessage.m_lstMessage);
        }

        List<String> devIdAry = Global.getSelfDeviceIdAry();
        List<TPS_AlarmInfo> lst = Global.m_alarmMessage.getAlarmMessage(devIdAry, m_queryCount, 0);
        if (!lst.isEmpty()) {
            for (TPS_AlarmInfo ai : lst) {
                if (!m_local_data.contains(ai)) m_local_data.add(ai);
            }

            MergeMessage(lst);
        }


        /*if (m_queryCount < Integer.parseInt(lstMessage.m_all_count)) {
            MessageList.Message item = lstMessage.m_lstMessage.get(0);
            if (null != item) {
                m_queryStartTime = item.m_alarm_time;
            } else {
                m_queryPage++;
            }

            new GetDataTask(m_queryPage).execute();
            return;
        }*/

        synchronized (m_event) {
            m_event.notify();
        }
    }

    private class AlarmListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context mContext;
        private LayoutInflater mInflater;
        //private Map<String, MessageList.Message> m_data = new HashMap<>();
        private List<MessageList.Message> m_data = new ArrayList<>();

        public AlarmListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setListData(List<MessageList.Message> data) {
            data = MessageList.removeDuplicateMessage(data);
            MessageList.sortMessageByTimeDesc(data);
            m_data = data;
        }

        @Override
        public int getCount() {
            return m_data.size();
        }

        @Override
        public Object getItem(int position) {
            return m_data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.alarm_message_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.btnDelete = (Button) v.findViewById(R.id.btnDelete);
                viewHolder.imgView = (ImageView) v.findViewById(R.id.img);
                viewHolder.labDatetime = (TextView) v.findViewById(R.id.lab_datetime);
                viewHolder.labDesc = (TextView) v.findViewById(R.id.lab_desc);
                viewHolder.labDevName = (TextView) v.findViewById(R.id.lab_dev_name);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            MessageList.Message msg = (MessageList.Message) getItem(position);
            if (null == msg) return v;
            viewHolder.labDatetime.setText(msg.m_alarm_time);
            String desc = ConstantImpl.getCloudAlarmTypeDesc(Integer.parseInt(msg.m_alarm_type_id));
            viewHolder.labDesc.setText(desc);
            String devId = msg.m_dev_id;
            String devName = devId;
            PlayerDevice dev = Global.getDeviceById(devId);
            if (null != dev) devName = LibImpl.getInstance().getDeviceAlias(dev.m_dev) + "(" + devId + ")";
            viewHolder.labDevName.setText(devName);
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }

        public class ViewHolder {
            public Button btnDelete;
            public ImageView imgView;
            public TextView labDatetime;
            public TextView labDesc;
            public TextView labDevName;
        }
    }
}

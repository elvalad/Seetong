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
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import ipc.android.sdk.com.TPS_AlarmInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by Administrator on 2014-05-13.
 */
public class AlarmMessageUI extends BaseActivity /*implements SwipeRefreshLayout.OnRefreshListener*/ {
    List<TPS_AlarmInfo> m_data;
    private ListView m_lvAlarm;
    private AlarmListAdapter m_adapter;

    //private SwipeRefreshLayout m_swipeLayout;
    private PullToRefreshListView mPullRefreshListView;

    private final int NUM_RECORD_IN_PAGE = 9;
    private int m_currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_message);
        initWidget();
    }

    protected void initWidget() {
        m_data = Global.m_alarmMessage.getAlarmMessage(NUM_RECORD_IN_PAGE, 0);
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
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                LibImpl.getInstance().getFuncLib().SearchUserDevAlarm(1, 20, "", "");
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                m_currentPage = m_data.size() / NUM_RECORD_IN_PAGE + 1;

                LibImpl.getInstance().getFuncLib().SearchUserDevAlarm(m_currentPage, 20, "", "");

                // Do work to refresh the list here.
                new GetDataTask().execute();
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
        m_adapter.setListData(m_data);
    }

    private class GetDataTask extends AsyncTask<Void, Void, List<TPS_AlarmInfo>> {

        @Override
        protected List<TPS_AlarmInfo> doInBackground(Void... params) {
            m_data = Global.m_alarmMessage.getAlarmMessage(m_currentPage * NUM_RECORD_IN_PAGE, 0);
            m_adapter.setListData(m_data);
            return m_data;
        }

        @Override
        protected void onPostExecute(List<TPS_AlarmInfo> result) {
            m_adapter.notifyDataSetChanged();

            // Call onRefreshComplete when the list has been refreshed.
            mPullRefreshListView.onRefreshComplete();
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private class AlarmListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<TPS_AlarmInfo> m_lstData = new Vector<TPS_AlarmInfo>();

        public AlarmListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void addItem(TPS_AlarmInfo ta) {
            m_lstData.add(ta);
        }
        public void setListData(List<TPS_AlarmInfo> data) {
            m_lstData = data;
        }

        @Override
        public int getCount() {
            return m_lstData.size();
        }

        @Override
        public Object getItem(int position) {
            return m_lstData.get(position);
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

            TPS_AlarmInfo ta = m_lstData.get(position);
            long t = ta.getnTimestamp();
            Date date = new Date(t * 1000);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String s = fmt.format(date);
            viewHolder.labDatetime.setText(s);
            String desc = ConstantImpl.getAlarmTypeDesc(ta.getnType());
            viewHolder.labDesc.setText(desc);
            String devId = new String(ta.getSzDevId()).trim();
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

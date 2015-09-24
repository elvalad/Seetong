package com.seetong.app.seetong.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.NetSDK_Storage_Info;

import java.util.ArrayList;
import java.util.List;

public class StorageSettingUI extends BaseActivity implements View.OnClickListener {
    String m_device_id;
    private ProgressDialog mTipDlg;
    private ListView m_listView;
    private ListViewAdapter m_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_setting_ui);
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, "");
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);

        findViewById(R.id.btn_refresh).setOnClickListener(this);

        m_listView = (ListView) findViewById(R.id.lv_storage_device);
        m_adapter = new ListViewAdapter(m_listView.getContext());
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(m_adapter);

        Button btnFinish = (Button) findViewById(R.id.btn_title_right);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.GONE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().setMediaParamHandler(m_handler);
        loadData();
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void loadData() {
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                findViewById(R.id.layout_list_view).setVisibility(View.GONE);
                findViewById(R.id.layout_refresh).setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = LibImpl.getInstance().getFuncLib().P2PDevSystemControl(m_device_id, 1014, "");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
            findViewById(R.id.layout_list_view).setVisibility(View.GONE);
            findViewById(R.id.layout_refresh).setVisibility(View.VISIBLE);
        }
    }

    public void saveData() {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                onBtnFinish();
                break;
            case R.id.btn_refresh:
                onBtnRefresh();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().setMediaParamHandler(m_handler);
    }

    private void onBtnFinish() {
        saveData();
    }

    private void onBtnRefresh() {
        loadData();
    }

    private void onBtnFormatSd1() {
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = FormatStorage("sd1");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
        }
    }

    private void onBtnUnloadSd1() {
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = UnloadStorage("sd1");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
        }
    }

    private void onBtnFormatSd2() {
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = FormatStorage("sd2");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
        }
    }

    private void onBtnUnloadSd2() {
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = UnloadStorage("sd2");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
        }
    }

    private void onBtnFormatUsb() {
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = FormatStorage("usb");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
        }
    }

    private void onBtnUnloadUsb() {
        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
        int ret = UnloadStorage("usb");
        if (0 != ret) {
            toast(R.string.operation_failed_retry);
        }
    }

    private int FormatStorage(String storage) {
        String xml = "<REQUEST_PARAM DevName = \"" + storage + "\"/>";
        return LibImpl.getInstance().getFuncLib().P2PDevSystemControl(m_device_id, 1017, xml);
    }

    private int UnloadStorage(String storage) {
        String xml = "<REQUEST_PARAM DevName = \"" + storage + "\"/>";
        return LibImpl.getInstance().getFuncLib().P2PDevSystemControl(m_device_id, 1018, xml);
    }

    @Override
    public void handleMessage(Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case 1014:
                NetSDK_Storage_Info nssi = (NetSDK_Storage_Info) msg.obj;
                onGetStorageInfo(flag, nssi);
                break;
            case 1017:
                onFormatResult(flag);
                break;
            case 1018:
                onUnloadResult(flag);
                break;
        }
    }

    private void onGetStorageInfo(int flag, NetSDK_Storage_Info ossi) {
        mTipDlg.setCallback(null);
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.operation_failed_retry);
            findViewById(R.id.layout_list_view).setVisibility(View.GONE);
            findViewById(R.id.layout_refresh).setVisibility(View.VISIBLE);
            return;
        }

        findViewById(R.id.layout_list_view).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_refresh).setVisibility(View.GONE);
        m_adapter.setViewData(ossi.m_dev_list);
    }

    private void onFormatResult(int flag) {
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.operation_failed_retry);
        } else {
            toast(R.string.operation_succeed);
        }
    }

    private void onUnloadResult(int flag) {
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.operation_failed_retry);
        } else {
            toast(R.string.operation_succeed);
        }
    }

    class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        List<NetSDK_Storage_Info.DeviceInfo> m_data = new ArrayList<>();

        public ListViewAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
        }

        public void setViewData(List<NetSDK_Storage_Info.DeviceInfo> data) {
            if (null != data) m_data = data;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RelativeLayout {
            public TextView tvCaption;
            public ProgressBar pbDownload;
            public TextView tvTotalSpace;
            public TextView tvUsedSpace;
            public Button btnFormat;
            public Button btnUnload;
            NetSDK_Storage_Info.DeviceInfo m_info;

            public ViewHolder(Context context) {
                this(context, null, 0);
            }

            public ViewHolder(Context context, AttributeSet attrs) {
                this(context, attrs, 0);
            }

            public ViewHolder(Context context, AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                LayoutInflater.from(context).inflate(R.layout.storage_info_list_item, this);
                tvCaption = (TextView) findViewById(R.id.tvCaption);
                pbDownload = (ProgressBar) findViewById(R.id.id_pb_download);
                tvTotalSpace = (TextView) findViewById(R.id.tv_total_space);
                tvUsedSpace = (TextView) findViewById(R.id.tv_used_space);
                btnFormat = (Button) findViewById(R.id.btn_format);
                btnUnload = (Button) findViewById(R.id.btn_unload);

                btnFormat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
                        int ret = FormatStorage(m_info.DevName);
                        if (0 != ret) {
                            mTipDlg.dismiss();
                            toast(R.string.operation_failed_retry);
                        }
                    }
                });

                btnUnload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTipDlg(R.string.please_wait_communication, 30000, R.string.timeout_retry);
                        int ret = UnloadStorage(m_info.DevName);
                        if (0 != ret) {
                            mTipDlg.dismiss();
                            toast(R.string.operation_failed_retry);
                        }
                    }
                });
            }

            public void update(int position) {
                m_info = m_data.get(position);
                boolean available = "0".equals(m_info.Total) ? false : true;
                //String available = "0".equals(m_info.Total) ? T(R.string.unavailable) : T(R.string.available);
                //String caption = m_info.DevName + "   (" + available + ")";
                String caption = m_info.DevName;
                tvCaption.setText(caption);
                int percent = Integer.parseInt(m_info.Percent);
                pbDownload.setProgress(percent);
                String total = T(R.string.total_space) + m_info.Total + "(MB)";
                tvTotalSpace.setText(total);
                String used = T(R.string.used_space) + m_info.Used + "(MB)";
                tvUsedSpace.setText(used);
                btnFormat.setVisibility(available ? View.VISIBLE : View.GONE);
                btnUnload.setVisibility(available ? View.VISIBLE : View.GONE);
            }
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
            final ViewHolder viewHolder;
            if (v == null) {
                viewHolder = new ViewHolder(StorageSettingUI.this);
            } else {
                viewHolder = (ViewHolder) v;
            }

            viewHolder.update(position);
            return viewHolder;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        }
    }
}
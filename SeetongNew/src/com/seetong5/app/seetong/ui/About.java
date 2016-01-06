package com.seetong5.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import java.util.List;

/**
 * Created by Administrator on 2014-07-28.
 */
public class About extends BaseActivity {

    private ListView m_listView;
    private ListViewAdapter m_adapter;
    private int m_itemTextRes[] = {/*R.string.tv_soft_description,*/R.string.tv_crash_info  ,R.string.tv_feedback, R.string.tv_version_update};
    private final Class<?>[] m_itemActivity = new Class<?>[] { CrashInfo.class ,SoftDescriptionUI.class };
    private TextView m_version;
    private ProgressDialog mTipDlg;
    private ImageView aboutQrcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        initWidget();
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent.toString().contains("mailto:")) {
            PackageManager pm = getPackageManager();
            List<ResolveInfo> lst = pm.queryIntentActivities(intent, 0);
            if (null == lst || lst.size() == 0) {
                toast(R.string.not_app_execute_action);
                return;
            }
        }
        super.startActivity(intent);
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, "");
        mTipDlg.setCancelable(true);

        aboutQrcode = (ImageView) findViewById(R.id.about_qr_code);
        aboutQrcode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toast("Save seetong qrcode");
                return false;
            }
        });

        m_listView = (ListView) findViewById(R.id.lvItems);
        m_adapter = new ListViewAdapter(m_listView.getContext());
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(m_adapter);
        m_version = (TextView) findViewById(R.id.lab_version);
        m_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer count = (Integer) v.getTag(R.id.lab_version);
                if (null == count) count = 0;
                count++;
                if (count == 6) {
                }
                v.setTag(R.id.lab_version, count);
            }
        });


        m_version.setText(T(R.string.tv_version_info_prefix) + Global.m_pkg_info.versionName);
    }

    class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        public ListViewAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
        }

        public class ViewHolder {
            public ImageView imgView;
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_itemTextRes.length;
        }

        @Override
        public Object getItem(int position) {
            return m_itemTextRes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.about_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imgView = (ImageView) v.findViewById(R.id.img);
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }
            viewHolder.tvCaption.setText(m_itemTextRes[position]);
            //viewHolder.imgView.setImageResource(m_itemImgRes[position]);
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            if (pos == 1) {
                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:help@seetong.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, "意见反馈");
                data.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(data);
            } else if (pos == 2) {
                //mTipDlg.setTitle(T(R.string.check_new_version_please_wait));
                //mTipDlg.show();
                //checkUpdate();
                toast(R.string.about_update_delay);
            } else {
                Intent it = new Intent(m_ctx, m_itemActivity[pos]);
                m_ctx.startActivity(it);
            }
        }
    }

    private void checkUpdate() {
        final String LOG_TAG = About.class.getName();
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                mTipDlg.dismiss();
                switch (updateStatus) {
                    case UpdateStatus.Yes: // has update
                        UmengUpdateAgent.showUpdateDialog(Global.m_ctx, updateInfo);
                        break;
                    case UpdateStatus.No: // has no update
                        toast(R.string.no_new_version);
                        MobclickAgent.reportError(Global.m_ctx, "没有更新.");
                        break;
                    case UpdateStatus.NoneWifi: // none wifi
                        Log.i(LOG_TAG, "没有wifi连接， 只在wifi下更新.");
                        MobclickAgent.reportError(Global.m_ctx, "没有wifi连接， 只在wifi下更新.");
                        break;
                    case UpdateStatus.Timeout: // time out
                        toast(R.string.check_new_version_timeout);
                        MobclickAgent.reportError(Global.m_ctx, "更新超时.");
                        break;
                }
            }
        });

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.forceUpdate(this);
    }
}
package com.seetong5.app.seetong.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baidu.android.pushservice.PushManager;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;

/**
 * Created by Administrator on 2014-05-12.
 */
public class MoreFragment extends BaseFragment implements View.OnClickListener {
    private ListView m_listView;
    private ListViewAdapter m_adapter;
    private int m_itemTextRes[] = {/*R.string.tv_local_album_title, R.string.tv_local_video_title, */R.string.alarm_message_title, R.string.tv_setting_title, /*R.string.tv_help_title, */R.string.tv_about_title};
    private int m_itemImgRes[] = {/*R.drawable.main_category_image, R.drawable.main_category_video, */R.drawable.main_category_message, R.drawable.main_category_setup, /*R.drawable.main_category_help, */R.drawable.main_category_about};
    private final Class<?>[] m_itemActivity = new Class<?>[]{/*ImageFile2.class, VideoFile.class, */Message.class, SettingUI.class, /*Help.class, */About.class};

    ProgressDialog mExitTipDlg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.m_this.setMoreFragment(this);
        View v = inflater.inflate(R.layout.more, container);
        m_listView = (ListView) v.findViewById(R.id.lv_more);
        m_adapter = new ListViewAdapter(m_listView.getContext());
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(m_adapter);
        Button btnLogout = (Button) v.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_logout:
                onBtnLogout();
                break;
        }
    }

    private void onBtnLogout() {
        MyTipDialog.popDialog(MoreFragment.this.getActivity(), R.string.dlg_app_logout_sure_tip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        Global.clearPushTags();
                        if (Define.LOGIN_TYPE_USER == Global.m_loginType) {
                            Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_PWD, false);
                            Global.m_spu_login.saveSharedPreferences(Define.USR_PSW, "");
                        }

                        mExitTipDlg = new ProgressDialog(MoreFragment.this.getActivity(), R.string.dlg_app_logout_tip);
                        mExitTipDlg.setCancelable(false);
                        mExitTipDlg.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.m_this.logout();
                                MoreFragment.this.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mExitTipDlg.dismiss();
                                        MainActivity.m_this.finish();
                                        PushManager.stopWork(Global.m_ctx);
                                        Intent it = new Intent(MoreFragment.this.getActivity(), LoginUI2.class);
                                        MoreFragment.this.startActivity(it);
                                    }
                                });
                            }
                        }).start();
                    }
                }
        );
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
                v = mInflater.inflate(R.layout.more_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imgView = (ImageView) v.findViewById(R.id.img);
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }
            viewHolder.tvCaption.setText(m_itemTextRes[position]);
            viewHolder.imgView.setImageResource(m_itemImgRes[position]);
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            Intent it = new Intent(m_ctx, m_itemActivity[pos]);
            m_ctx.startActivity(it);
        }
    }
}

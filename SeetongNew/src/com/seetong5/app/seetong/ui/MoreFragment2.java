package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.baidu.android.pushservice.PushManager;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;

/**
 * MoreFragment 是更多页面的 Fragment，它也是包含于 MainActivity 的 Tabhost 中，它自身包含一些基本的
 * 组件来完成相关的操作，通过点击相关 Button，会进入到其他 Activity.
 *
 * Created by gmk on 2015/9/11.
 */
public class MoreFragment2 extends BaseFragment {

    ProgressDialog mExitTipDlg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more2, container);

        initWidget(view);

        return view;
    }

    private void initWidget(View view) {
        Button alarmButton = (Button) view.findViewById(R.id.more_alarm_button);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreFragment2.this.getActivity(), Message.class);
                startActivity(intent);
            }
        });

        Button settingButton = (Button) view.findViewById(R.id.more_setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreFragment2.this.getActivity(), SettingUI.class);
                startActivity(intent);
            }
        });

        Button aboutButton = (Button) view.findViewById(R.id.more_about_button);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreFragment2.this.getActivity(), About.class);
                startActivity(intent);
            }
        });

        Button exitButton = (Button) view.findViewById(R.id.more_exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MoreFragment2.this.getActivity().finish();
                onBtnLogout();
            }
        });
    }

    private void onBtnLogout() {
        MyTipDialog.popDialog(MoreFragment2.this.getActivity(), R.string.dlg_app_logout_sure_tip, R.string.sure, R.string.cancel,
                new MyTipDialog.IDialogMethod() {
                    @Override
                    public void sure() {
                        Global.clearPushTags();
                        if (Define.LOGIN_TYPE_USER == Global.m_loginType) {
                            Global.m_spu_login.saveSharedPreferences(Define.IS_SAVE_PWD, false);
                            Global.m_spu_login.saveSharedPreferences(Define.USR_PSW, "");
                        }

                        mExitTipDlg = new ProgressDialog(MoreFragment2.this.getActivity(), R.string.dlg_app_logout_tip);
                        mExitTipDlg.setCancelable(false);
                        mExitTipDlg.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity2.m_this.logout();
                                MoreFragment2.this.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mExitTipDlg.dismiss();
                                        MainActivity2.m_this.finish();
                                        PushManager.stopWork(Global.m_ctx);
                                        Intent it = new Intent(MoreFragment2.this.getActivity(), LoginActivity.class);
                                        MoreFragment2.this.startActivity(it);
                                    }
                                });
                            }
                        }).start();
                    }
                }
        );
    }

}


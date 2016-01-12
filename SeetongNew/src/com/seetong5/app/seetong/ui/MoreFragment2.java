package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.EditText;
import com.baidu.android.pushservice.PushManager;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
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
    private int playSettingState = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setMoreFragment(this);
        View view = inflater.inflate(R.layout.more2, container);
        loadData();
        initWidget(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Config.saveData();
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

        Button playSettingButton = (Button) view.findViewById(R.id.more_play_setting_button);
        playSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBtnPlaySetting();
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

        final Button exitButton = (Button) view.findViewById(R.id.more_exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MoreFragment2.this.getActivity().finish();
                onBtnLogout();
            }
        });
        exitButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    exitButton.getBackground().setAlpha(150);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    exitButton.getBackground().setAlpha(255);
                }
                return false;
            }
        });
    }

    private void loadData() {
        int bufferSize = Config.m_frame_buffer_size;
        if (bufferSize == 20) {
            playSettingState = 0;
        } else if (bufferSize == 50) {
            playSettingState = 1;
        } else {
            playSettingState = 2;
        }
    }

    private void onBtnPlaySetting() {
        new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.more_play_setting)
                .setSingleChoiceItems(new String[]{
                        getString(R.string.more_play_setting_low_delay),
                        getString(R.string.more_play_setting_normal),
                        getString(R.string.more_play_setting_smooth)}, playSettingState, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Config.m_frame_buffer_size = 20;
                                playSettingState = 0;
                                break;
                            case 1:
                                Config.m_frame_buffer_size = 50;
                                playSettingState = 1;
                                break;
                            case 2:
                                Config.m_frame_buffer_size = 100;
                                playSettingState = 2;
                                break;
                            default:
                                Config.m_frame_buffer_size = 50;
                                playSettingState = 1;
                                break;
                        }
                        dialog.dismiss();
                    }
                }).show();
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
                            MainActivity2.m_this.saveData();
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


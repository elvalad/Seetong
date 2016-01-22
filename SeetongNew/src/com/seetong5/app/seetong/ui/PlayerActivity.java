package com.seetong5.app.seetong.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.TrafficStats;
import android.os.*;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.widget.*;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import ipc.android.sdk.com.NetSDK_CMD_TYPE;
import ipc.android.sdk.impl.DeviceInfo;

import java.util.*;

/**
 * PlayerActivity �ǲ����豸¼��� Activity������ DeviceFragment �����豸��Ϣʱ����������.
 * �������������һ����Ƶ�Ĵ��ں��ĸ���Ƶ�Ĵ���.
 *
 * Created by gmk on 2015/9/13.
 */
public class PlayerActivity extends BaseActivity {
    private String TAG = PlayerActivity.class.getName();
    public static PlayerActivity m_this = null;
    private String deviceId = null;

    private PlayerDevice playerDevice;
    private String currentFragmentName = "play_multi_video_fragment";
    private BaseFragment currentFragment;
    private PlayVideoFragment playVideoFragment;
    private PlayMultiVideoFragment multiVideoFragment;
    private int[] viewLocation = new int[4];
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    private Timer timer = new Timer();

    private static boolean bPlaying = true;
    private static boolean bSinglePlay = true;
    private static boolean bAutoCyclePlaying = false;
    private static boolean bVideoRecord = false;
    private static boolean bVideoSoundOn = false;
    private static boolean bHighDefinition = false;
    private static boolean bActive = true;
    private static boolean bSlidingOpen = false;
    public ProgressDialog mTipDlg;
    public DeviceInfo m_modifyInfo;
    public boolean m_modifyDefaultPassword = false;
    public PlayerDevice m_modifyUserPwdDev = null;

    private ImageButton playerBackButton;
    private ImageButton playerStopButton;
    private ImageButton playerSwitchWindowButton;
    private ImageButton playerCycleButton;
    private ImageButton playerPlaybackButton;
    private ImageButton playerSoundButton;
    private Button playerResolutionButton;
    private ImageButton playerSettingButton;
    private Button playerRecordButton;
    private Button playerSpeakButton;
    private Button playerCaptureButton;
    private SlidingDrawer slidingDrawer;
    private ImageButton slidingHandle;
    private LinearLayout playerMainButtonLayout;
    private ListView playerDeviceListView;
    private PlayerDeviceListAdapter adapter;
    private List<Map<String, Object>> data = new ArrayList<>();
    private LinearLayout.LayoutParams initParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m_this = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        deviceId = getIntent().getStringExtra("device_id");

        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        LibImpl.getInstance().addHandler(m_handler);
        playerDevice = LibImpl.findDeviceByID(PlayerActivity.m_this.getCurrentDeviceId());
        playerDevice.m_device_play_count++;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        initParams = (LinearLayout.LayoutParams) findViewById(R.id.player_fragment_container).getLayoutParams();
        initParams.width = dm.widthPixels;
        initParams.height = (initParams.width * 9) / 16;
        findViewById(R.id.player_fragment_container).setLayoutParams(initParams);

        bSinglePlay = Global.m_spu.loadBooleanSharedPreference(Define.SAVE_EXIT_WINDOW, true);
        initWidget();
        if (currentFragmentName.equals("play_multi_video_fragment")) {
            setCurrentFragment("play_multi_video_fragment");
            showPlayMultiVideoFragment();
        } else {
            setCurrentFragment("play_video_fragment");
            showPlayVideoFragment();
        }
        startShowNetSpeed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Log.e(TAG, "player activity onRestart");
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.startPlay();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.startPlayList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Global.m_spu.saveSharedPreferences(Define.EXIT_APP_NORMALLY, false);
        Global.m_spu.saveSharedPreferences(Define.SAVE_EXIT_DEVICE, multiVideoFragment.getDeviceList().get(0).m_devId);
        Global.m_spu.saveSharedPreferences(Define.SAVE_EXIT_WINDOW, bSinglePlay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setFullScreen(true);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullScreen(false);
        }

        if (bSlidingOpen) {
            slidingHandle.setImageResource(R.drawable.down);
            playerMainButtonLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.e(TAG, "player activity onStop");
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopPlay();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.stopPlayList();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(PlayerActivity.class.getName(), "onDestroy...");
        LibImpl.getInstance().removeHandler(m_handler);
        stopShowNetSpeed();
        bSlidingOpen = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        MainActivity2.m_this.sendMessage(Define.MSG_UPDATE_DEV_LIST, 0, 0, null);
        Global.riseToTop(playerDevice);
        PlayerActivity.this.finish();
        /* TODO:�ڵ�·fragment�Ͷ�·fragment֮���л�ʱ��Ҫע���������δ��� */
        if (currentFragmentName.equals("play_video_fragment")) {
            /* �˳������沥��ҳ��ʱҪ�ر��Զ�ѭ������ */
            if (autoPlayThread != null) {
                bAutoCyclePlaying = false;
                handler.removeCallbacks(autoPlayThread);
            }
        } else if (currentFragmentName.equals("play_multi_video_fragment")){
            /* �˳��໭�沥��ʱҪ�ر��Զ�ѭ������*/
            if (autoPlayThread != null) {
                bAutoCyclePlaying = false;
                handler.removeCallbacks(autoPlayThread);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case Constant.REQ_ID_DEVICE_CONFIG:
                onDeviceConfigResult(data);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setFullScreen(true);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullScreen(false);
            if (bSlidingOpen) {
                slidingHandle.setImageResource(R.drawable.down);
                playerMainButtonLayout.setVisibility(View.GONE);
            }
        }
    }

    public void onWindowFocusChanged (boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            View view = findViewById(R.id.player_fragment_container);
            viewLocation[0] = view.getLeft();
            viewLocation[1] = view.getRight();
            viewLocation[2] = view.getTop();
            viewLocation[3] = view.getBottom();
        }
    }

    /*�ж�Ӧ���Ƿ���ǰ̨*/
    public static boolean isForeground(Context context)
    {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void setFullScreen(boolean bFullScreen) {
        LinearLayout.LayoutParams params;
        int show = bFullScreen ? View.GONE : View.VISIBLE;
        findViewById(R.id.player_title).setVisibility(show);
        findViewById(R.id.player_blank).setFadingEdgeLength(show);
        findViewById(R.id.player_operation_button).setVisibility(show);
        findViewById(R.id.player_main_button).setVisibility(show);
        findViewById(R.id.player_sliding_drawer).setVisibility(show);
        multiVideoFragment.showControlPanel();
        if (bFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //DisplayMetrics dm = getResources().getDisplayMetrics();
            //params.width = dm.widthPixels;
            //params.height = dm.heightPixels;
            findViewById(R.id.player_fragment_container).setLayoutParams(params);
        } else {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            findViewById(R.id.player_fragment_container).setLayoutParams(initParams);
        }
    }

    private void onDeviceConfigResult(Intent data) {
        String devId = data.getStringExtra(Constant.EXTRA_DEVICE_ID);
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;
        int type = data.getIntExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, 0);
        switch (type) {
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_ALIAS:
                String alias = data.getStringExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME);
                if (dev.m_dev.getDevType() == 100) {
                    dev.m_dev.setDevName(alias);
                } else if (dev.m_dev.getDevType() == 200 || dev.m_dev.getDevType() == 201) {
                    dev.m_dev.setDevGroupName(alias);
                }
                MainActivity2.m_this.sendMessage(Define.MSG_UPDATE_DEV_ALIAS, 0, 0, dev);
                break;
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_USER_PWD:
                modifyUserPwd(dev);
                break;
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_MEDIA_PARAM:
                break;
        }
    }

    public void modifyUserPwd(final PlayerDevice dev) {
        if (null == dev) return;
        m_modifyUserPwdDev = dev;
        showTipDlg(R.string.dlg_get_user_list_tip, 20000, R.string.dlg_check_your_device_user_and_pwd);
        int ret = LibImpl.getInstance().getFuncLib().GetP2PDevConfig(dev.m_dev.getDevId(), NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG);
        if (0 == ret) return;
        toast(R.string.dlg_check_your_device_user_and_pwd);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                m_modifyInfo = null;
                m_modifyDefaultPassword = false;
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });
        mTipDlg.show(timeout);
    }

    private void initWidget() {
        playerBackButton = (ImageButton) findViewById(R.id.player_back);
        playerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity2.m_this.sendMessage(Define.MSG_UPDATE_DEV_LIST, 0, 0, null);
                Global.riseToTop(playerDevice);
                PlayerActivity.this.finish();
                /* TODO:�ڵ�·fragment�Ͷ�·fragment֮���л�ʱ��Ҫע���������δ��� */
                if (currentFragmentName.equals("play_video_fragment")) {
                    /* �˳������沥��ҳ��ʱҪ�ر��Զ�ѭ������ */
                    if (autoPlayThread != null) {
                        bAutoCyclePlaying = false;
                        handler.removeCallbacks(autoPlayThread);
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")){
                    /* �˳��໭�沥��ʱҪ�ر��Զ�ѭ������*/
                    if (autoPlayThread != null) {
                        bAutoCyclePlaying = false;
                        handler.removeCallbacks(autoPlayThread);
                    }
                }
            }
        });

        playerStopButton = (ImageButton) findViewById(R.id.player_stop_all);
        playerStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAutoCyclePlaying) {
                    return;
                }

                if (currentFragmentName.equals("play_video_fragment")) {
                    if (bPlaying) {
                        playerStopButton.setImageResource(R.drawable.tps_play_multi);
                        playVideoFragment.stopPlay();
                        bPlaying = false;
                    } else {
                        playerStopButton.setImageResource(R.drawable.tps_play_single);
                        playVideoFragment.startPlay();
                        bPlaying = true;
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    if (bPlaying) {
                        playerStopButton.setImageResource(R.drawable.tps_play_multi);
                        multiVideoFragment.stopPlayList();
                        bPlaying = false;
                    } else {
                        playerStopButton.setImageResource(R.drawable.tps_play_single);
                        multiVideoFragment.startPlayList();
                        bPlaying = true;
                    }
                }
            }
        });

        playerSwitchWindowButton = (ImageButton) findViewById(R.id.player_switch_window);
        if (bSinglePlay) {
            playerSwitchWindowButton.setImageResource(R.drawable.tps_play_single);
        } else {
            playerSwitchWindowButton.setImageResource(R.drawable.tps_play_multi);
        }

        playerSwitchWindowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAutoCyclePlaying) {
                    return;
                }

                if (bSinglePlay) {
                    playerSwitchWindowButton.setImageResource(R.drawable.tps_play_multi);
                    multiVideoFragment.setSinglePlay(false);
                    bSinglePlay = false;
                } else {
                    playerSwitchWindowButton.setImageResource(R.drawable.tps_play_single);
                    multiVideoFragment.setSinglePlay(true);
                    bSinglePlay = true;
                }
            }
        });

        playerCycleButton = (ImageButton) findViewById(R.id.player_cycle);
        playerCycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragmentName.equals("play_video_fragment")) {
                    if (bAutoCyclePlaying) {
                        /* ����handler��removeCallbacks������ɾ�������е��̣߳�ֹͣ�Զ�ѭ�������߳� */
                        toast(R.string.player_auto_play_off);
                        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_off);
                        handler.removeCallbacks(autoPlayThread);
                        bAutoCyclePlaying = false;
                    } else {
                        /* ����handler��post��������ִ���̷߳��뵽������ */
                        toast(R.string.player_auto_play_on);
                        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_on);
                        handler.post(autoPlayThread);
                        bAutoCyclePlaying = true;
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    if (bAutoCyclePlaying) {
                        toast(R.string.player_auto_play_off);
                        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_off);
                        handler.removeCallbacks(autoPlayThread);
                        bAutoCyclePlaying = false;
                    } else {
                        toast(R.string.player_auto_play_on);
                        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_on);
                        handler.post(autoPlayThread);
                        bAutoCyclePlaying = true;
                    }
                }
            }
        });

        playerPlaybackButton = (ImageButton) findViewById(R.id.player_record_playback);
        playerPlaybackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAutoCyclePlaying) {
                    return;
                }
                onRecordPlayBack();
            }
        });
        playerPlaybackButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (bAutoCyclePlaying) {
                    return false;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    playerPlaybackButton.setImageResource(R.drawable.tps_play_recordplayback_on);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    playerPlaybackButton.setImageResource(R.drawable.tps_play_recordplayback_off);
                }
                return false;
            }
        });

        playerSoundButton = (ImageButton) findViewById(R.id.player_sound);
        playerSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAutoCyclePlaying) {
                    return;
                }

                /* ֹͣ���� */
                if (bVideoSoundOn) {
                    playerSoundButton.setImageResource(R.drawable.tps_play_sound_off);
                    bVideoSoundOn = false;
                    offVideoSound();
                } else {
                    playerSoundButton.setImageResource(R.drawable.tps_play_sound_on);
                    bVideoSoundOn = true;
                    onVideoSound();
                }
            }
        });

        playerResolutionButton = (Button) findViewById(R.id.player_resolution);
        playerResolutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAutoCyclePlaying) {
                    return;
                }

                /* ѡ����岥�� */
                if (bHighDefinition) {
                    bHighDefinition = false;
                    offHighDefinition();
                } else {
                    bHighDefinition = true;
                    onHighDefinition();
                }
            }
        });

        playerSettingButton = (ImageButton) findViewById(R.id.player_setting);
        playerSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* �������� */
                if (bAutoCyclePlaying) {
                    return;
                }

                Intent intent = new Intent(PlayerActivity.this, PlayerSettingActivity.class);
                intent.putExtra("device_setting_id", PlayerActivity.this.deviceId);
                startActivityForResult(intent, Constant.REQ_ID_DEVICE_CONFIG);
            }
        });
        playerSettingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (bAutoCyclePlaying) {
                    return false;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    playerSettingButton.setImageResource(R.drawable.tps_play_set_on);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    playerSettingButton.setImageResource(R.drawable.tps_play_set_off);
                }
                return false;
            }
        });

        playerRecordButton = (Button) findViewById(R.id.player_record);
        playerRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* TODO:�豸¼�� */
                if (bAutoCyclePlaying) {
                    return;
                }

                if (currentFragmentName.equals("play_video_fragment")) {
                    if (playVideoFragment.getCurrentDevice().m_record) {
                        offVideoRecord();
                    } else {
                        onVideoRecord();
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    if (multiVideoFragment.getChoosenDevice().m_record) {
                        offVideoRecord();
                    } else {
                        onVideoRecord();
                    }
                }
            }
        });

        playerSpeakButton = (Button) findViewById(R.id.player_microphone);
        playerSpeakButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (bAutoCyclePlaying) {
                    return false;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    playerSpeakButton.setBackgroundResource(R.drawable.tps_play_microphone_on);
                    playerSpeakButton.setTextColor(getResources().getColor(R.color.green));
                    onSepak();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    playerSpeakButton.setBackgroundResource(R.drawable.tps_play_microphone_off);
                    playerSpeakButton.setTextColor(getResources().getColor(R.color.gray));
                    offSpeak();
                }
                return false;
            }
        });

        playerCaptureButton = (Button) findViewById(R.id.player_capture);
        playerCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* �豸��ͼ */
                if (bAutoCyclePlaying) {
                    return;
                }
                onVideoCapture();

                /* ���ﴴ��һ���µ��̲߳�������1000ms֮���MainActivity2����Message��ԭ����
                *  ������ͽ�ͼ����֮�����Ϸ���message��֪PictureFragment���½�ͼ�б��ᵼ����
                *  �µ�һ�Ž�ͼ��û�����ɣ���ʱɨ���ͼĿ¼����ȡ���������һ�Ž�ͼ������һ��
                *  ��ͼ�����������¡�
                * */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                            MainActivity2.m_this.sendMessage(Define.MSG_UPDATE_SCREENSHOT_LIST, 0, 0, null);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        playerCaptureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (bAutoCyclePlaying) {
                    return false;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    playerCaptureButton.setBackgroundResource(R.drawable.tps_play_capture_on);
                    playerCaptureButton.setTextColor(getResources().getColor(R.color.green));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    playerCaptureButton.setBackgroundResource(R.drawable.tps_play_capture_off);
                    playerCaptureButton.setTextColor(getResources().getColor(R.color.gray));
                }
                return false;
            }
        });

        slidingDrawer = (SlidingDrawer) findViewById(R.id.player_sliding_drawer);
        playerMainButtonLayout = (LinearLayout) findViewById(R.id.player_main_button);
        slidingHandle = (ImageButton) findViewById(R.id.player_handle);
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                slidingHandle.setImageResource(R.drawable.down);
                playerMainButtonLayout.setVisibility(View.GONE);
                bSlidingOpen = true;
            }
        });
        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                slidingHandle.setImageResource(R.drawable.up);
                playerMainButtonLayout.setVisibility(View.VISIBLE);
                bSlidingOpen = false;
            }
        });

        playerDeviceListView = (ListView) findViewById(R.id.player_content);
        getData();
        adapter = new PlayerDeviceListAdapter(this, data);
        playerDeviceListView.setAdapter(adapter);
    }

    final Handler handler = new Handler();
    final Runnable autoPlayThread = new Runnable() {
        @Override
        public void run() {
            try {
                /* �����߳�����1000ms����ֹ�Զ�ѭ�������߳̿���ʱCPUռ����һֱ�ܸ� */
                Thread.sleep(1000);
                if (currentFragmentName.equals("play_video_fragment")) {
                    playVideoFragment.autoCyclePlay();
                    /* �����Զ�ѭ������ʱ�� */
                    handler.postDelayed(autoPlayThread, (Config.m_polling_time + 5) * 1000);
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    multiVideoFragment.autoCyclePlay();
                    /* �����Զ�ѭ������ʱ�� */
                    handler.postDelayed(autoPlayThread, (Config.m_polling_time + 5) * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void onSepak() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.startSpeak();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.startSpeak();
        }
    }

    private void offSpeak() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopSpeak();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.stopSpeak();
        }
    }

    private void onVideoCapture() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.videoCapture();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.videoCapture();
        }
    }

    private void onHighDefinition() {
        boolean bRet = false;
        if (currentFragmentName.equals("play_video_fragment")) {
            bRet = playVideoFragment.startHighDefinition();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            bRet = multiVideoFragment.startHighDefinition();
        }

        if (!bRet) {
            resetWidget();
        }
        setResolutionState(true);
    }

    private void offHighDefinition() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopHighDefinition();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.stopHighDefinition();
        }
        setResolutionState(false);
    }

    private void onVideoRecord() {
        boolean bRet = false;
        if (currentFragmentName.equals("play_video_fragment")) {
            bRet = playVideoFragment.startVideoRecord();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.initRecordStartTime();
            bRet = multiVideoFragment.startVideoRecord();
        }

        if (!bRet) {
            resetWidget();
            return;
        }
        setRecordState(true);
    }

    private void offVideoRecord() {
        multiVideoFragment.initRecordEndTime();
        if (multiVideoFragment.bRecordShort()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyTipDialog.popDialog(PlayerActivity.m_this, R.string.dlg_tip, R.string.player_exit_record, R.string.sure);
                }
            });
        } else {
            if (currentFragmentName.equals("play_video_fragment")) {
                playVideoFragment.stopVideoRecord();
            } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                multiVideoFragment.stopVideoRecord();
            }
            setRecordState(false);
        }
    }

    private void onRecordPlayBack() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.startRecordPlayBack();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.startRecordPlayBack();
        }
    }

    private void onVideoSound() {
        boolean bRet = false;
        if (currentFragmentName.equals("play_video_fragment")) {
            bRet = playVideoFragment.startVideoSound();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            bRet = multiVideoFragment.startVideoSound();
        }

        if (!bRet) {
            resetWidget();
        }
    }

    private void offVideoSound() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopVideoSound();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.stopVideoSound();
        }
    }

    public void setVideoSoundWidget() {
        playerSoundButton.setImageResource(R.drawable.tps_play_sound_off);
    }

    public void resetWidget() {
        playerStopButton.setImageResource(R.drawable.tps_play_single);
        bPlaying = true;
        playerPlaybackButton.setImageResource(R.drawable.tps_play_recordplayback_off);
        playerSoundButton.setImageResource(R.drawable.tps_play_sound_off);
        bVideoSoundOn = false;
        playerResolutionButton.setTextColor(getResources().getColor(R.color.gray));
        playerResolutionButton.setText(R.string.player_resolution);
        bHighDefinition = false;
        playerSettingButton.setImageResource(R.drawable.tps_play_set_off);

        playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_off);
        playerRecordButton.setTextColor(getResources().getColor(R.color.gray));
    }

    public void resetPlayCycleButton() {
        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_off);
    }

    public void setCurrentFragment(String fragmentName) {
        /* �����ʱ��ѭ������״̬������˫���л�������Ͷ໭�� */
        if (bAutoCyclePlaying) return;
        switch(fragmentName) {
            case "play_video_fragment":
                this.currentFragment = playVideoFragment;
                this.currentFragmentName = "play_video_fragment";
                break;
            case "play_multi_video_fragment":
                this.currentFragment = multiVideoFragment;
                this.currentFragmentName = "play_multi_video_fragment";
                break;
            default:
                this.currentFragment = playVideoFragment;
                break;
        }
    }

    public void setPlayVideoFragment(PlayVideoFragment playVideoFragment) {
        this.playVideoFragment = playVideoFragment;
    }

    public void setPlayMultiVideoFragment(PlayMultiVideoFragment playMultiVideoFragment) {
        this.multiVideoFragment = playMultiVideoFragment;
    }

    private void showPlayVideoFragment() {
        if (playVideoFragment == null) {
            playVideoFragment = new PlayVideoFragment(this.playerDevice, 0);
        }
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.player_fragment_container, playVideoFragment)
            .commit();
    }

    private void showPlayMultiVideoFragment() {
        if (multiVideoFragment == null) {
            multiVideoFragment = new PlayMultiVideoFragment(this.playerDevice, 0);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.player_fragment_container, multiVideoFragment)
                .commit();
    }

    public void startChoosenPlay(PlayerDevice choosenDevice) {
        if (bAutoCyclePlaying) return;
        if (currentFragmentName.equals("play_video_fragment")) {
            this.playVideoFragment.startChoosenPlay(choosenDevice);
            this.playerDevice = choosenDevice;
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            this.multiVideoFragment.startChoosenPlay(choosenDevice);
            this.playerDevice = choosenDevice;
        }
    }

    public int[] getFragmentLocation() {
        return viewLocation;
    }

    public String getCurrentDeviceId() {
        return this.deviceId;
    }

    public void setCurrentDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setRecordState(boolean bRecord) {
        if (bRecord) {
            playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_on);
            playerRecordButton.setTextColor(getResources().getColor(R.color.green));
        } else {
            playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_off);
            playerRecordButton.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    public void setResolutionState(boolean bHighReso) {
        if (bHighReso) {
            playerResolutionButton.setTextColor(getResources().getColor(R.color.green));
            playerResolutionButton.setText(R.string.player_high_resolution);
        } else {
            playerResolutionButton.setTextColor(getResources().getColor(R.color.gray));
            playerResolutionButton.setText(R.string.player_resolution);
        }
    }

    public void setSwitchWindowState(boolean bSingle) {
        if (bSingle) {
            playerSwitchWindowButton.setImageResource(R.drawable.tps_play_single);
            bSinglePlay = true;
        } else {
            playerSwitchWindowButton.setImageResource(R.drawable.tps_play_multi);
            bSinglePlay = false;
        }
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        android.os.Message msg = m_handler.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        m_handler.sendMessage(msg);
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        if (this.currentFragmentName.equals("play_video_fragment")) {
            if (null != playVideoFragment) {
                playVideoFragment.handleMessage(msg);
            }
        } else if (this.currentFragmentName.equals("play_multi_video_fragment")) {
            if (null != multiVideoFragment) {
                multiVideoFragment.handleMessage(msg);
            }
        }
    }

    private void getData() {
        data.clear();
        LibImpl.putDeviceList(Global.getDeviceList());
        for (int i = 0; i < Global.getDeviceList().size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("device", Global.getSelfDeviceList().get(i));
            data.add(map);
        }
    }

    private void showNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//����ת��

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        Message msg = m_handler.obtainMessage();
        msg.what = Define.MSG_UPDATE_NET_SPEED;
        msg.obj = String.valueOf(speed) + " kb/s";
        m_handler.sendMessage(msg);
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(PlayerActivity.m_this.getApplicationInfo().uid) ==
                TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes() / 1024);
    }

    public void startShowNetSpeed() {
        lastTotalRxBytes = getTotalRxBytes();
        lastTimeStamp = System.currentTimeMillis();
        timer.schedule(task, 1000, 2000);
    }

    public void stopShowNetSpeed() {
        timer.cancel();
        task.cancel();
    }
}
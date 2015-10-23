package com.seetong5.app.seetong.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;

import java.util.List;

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
    private String currentFragmentName = "play_video_fragment";
    private BaseFragment currentFragment;
    private PlayVideoFragment playVideoFragment;
    private PlayMultiVideoFragment multiVideoFragment;

    private static boolean bPlaying = true;
    private static boolean bAutoCyclePlaying = false;
    private static boolean bVideoRecord = false;
    private static boolean bVideoSoundOn = false;
    private static boolean bHighDefinition = false;
    private static boolean bActive = true;

    private ImageButton playerBackButton;
    private ImageButton playerStopButton;
    private ImageButton playerCycleButton;
    private ImageButton playerPlaybackButton;
    private ImageButton playerSoundButton;
    private Button playerResolutionButton;
    private ImageButton playerSettingButton;
    private Button playerRecordButton;
    private Button playerSpeakButton;
    private Button playerCaptureButton;
    private LinearLayout.LayoutParams initParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m_this = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player);
        deviceId = getIntent().getStringExtra("device_id");
        Log.d(TAG, "the device id is " + deviceId);
        Log.d(TAG, "the current device is:" + LibImpl.findDeviceByID(deviceId));

        LibImpl.getInstance().addHandler(m_handler);
        playerDevice = LibImpl.findDeviceByID(PlayerActivity.m_this.getCurrentDeviceId());
        initParams = (LinearLayout.LayoutParams) findViewById(R.id.player_fragment_container).getLayoutParams();
        initWidget();
        if (currentFragmentName.equals("play_multi_video_fragment")) {
            setCurrentFragment("play_multi_video_fragment");
            showPlayMultiVideoFragment();
        } else {
            setCurrentFragment("play_video_fragment");
            showPlayVideoFragment();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.e(TAG, "player activity onStart");
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.startPlay();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.startPlayList();
        }
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.e(TAG, "player activity onStop");
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopPlay();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.startPlayList();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(PlayerActivity.class.getName(), "onDestroy...");
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        PlayerActivity.this.finish();
        /* TODO:�ڵ�·fragment�Ͷ�·fragment֮���л�ʱ��Ҫע���������δ��� */
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopPlay();
            /* �˳������沥��ҳ��ʱҪ�ر��Զ�ѭ������ */
            if (autoPlayThread != null) {
                bAutoCyclePlaying = false;
                handler.removeCallbacks(autoPlayThread);
            }
        } else if (currentFragmentName.equals("play_multi_video_fragment")){
            multiVideoFragment.stopPlayList();
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
        findViewById(R.id.player_split_line).setVisibility(show);
        findViewById(R.id.player_main_button).setVisibility(show);
        MainActivity2.m_this.setTabVisible(!bFullScreen);
        if (bFullScreen) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            findViewById(R.id.player_fragment_container).setLayoutParams(params);
        } else {
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
                break;
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_MEDIA_PARAM:
                break;
        }
    }

    private void initWidget() {
        playerBackButton = (ImageButton) findViewById(R.id.player_back);
        playerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerActivity.this.finish();
                /* TODO:�ڵ�·fragment�Ͷ�·fragment֮���л�ʱ��Ҫע���������δ��� */
                if (currentFragmentName.equals("play_video_fragment")) {
                    playVideoFragment.stopPlay();
                    /* �˳������沥��ҳ��ʱҪ�ر��Զ�ѭ������ */
                    if (autoPlayThread != null) {
                        bAutoCyclePlaying = false;
                        handler.removeCallbacks(autoPlayThread);
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")){
                    multiVideoFragment.stopPlayList();
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
                        playerStopButton.setImageResource(R.drawable.tps_play_stopall_on);
                        playVideoFragment.stopPlay();
                        bPlaying = false;
                    } else {
                        playerStopButton.setImageResource(R.drawable.tps_play_stopall_off);
                        playVideoFragment.startPlay();
                        bPlaying = true;
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    if (bPlaying) {
                        playerStopButton.setImageResource(R.drawable.tps_play_stopall_on);
                        multiVideoFragment.stopPlayList();
                        bPlaying = false;
                    } else {
                        playerStopButton.setImageResource(R.drawable.tps_play_stopall_off);
                        multiVideoFragment.startPlayList();
                        bPlaying = true;
                    }
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
                view = findViewById(R.id.player_record_playback);
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
                    playerResolutionButton.setTextColor(getResources().getColor(R.color.gray));
                    bHighDefinition = false;
                    offHighDefinition();
                } else {
                    playerResolutionButton.setTextColor(getResources().getColor(R.color.green));
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

                view = findViewById(R.id.player_setting);
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

                if (bVideoRecord) {
                    /* �ر���Ƶ¼�� */
                    playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_off);
                    playerRecordButton.setTextColor(getResources().getColor(R.color.gray));
                    bVideoRecord = false;
                    offVideoRecord();
                } else {
                    /* ����Ƶ¼�� */
                    playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_on);
                    playerRecordButton.setTextColor(getResources().getColor(R.color.green));
                    bVideoRecord = true;
                    onVideoRecord();
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
                    handler.postDelayed(autoPlayThread, 5000);
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    multiVideoFragment.autoCyclePlay();
                    /* �����Զ�ѭ������ʱ�� */
                    handler.postDelayed(autoPlayThread, 5000);
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
    }

    private void offHighDefinition() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopHighDefinition();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.stopHighDefinition();
        }
    }

    private void onVideoRecord() {
        boolean bRet = false;
        if (currentFragmentName.equals("play_video_fragment")) {
            bRet = playVideoFragment.startVideoRecord();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            bRet = multiVideoFragment.startVideoRecord();
        }

        if (!bRet) {
            resetWidget();
        }
    }

    private void offVideoRecord() {
        if (currentFragmentName.equals("play_video_fragment")) {
            playVideoFragment.stopVideoRecord();
        } else if (currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.stopVideoRecord();
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

    public void resetWidget() {
        playerStopButton.setImageResource(R.drawable.tps_play_stopall_off);
        playerPlaybackButton.setImageResource(R.drawable.tps_play_recordplayback_off);
        playerSoundButton.setImageResource(R.drawable.tps_play_sound_off);
        playerResolutionButton.setTextColor(getResources().getColor(R.color.gray));
        playerSettingButton.setImageResource(R.drawable.tps_play_set_off);

        playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_off);
        playerRecordButton.setTextColor(getResources().getColor(R.color.gray));
    }

    public void resetPlayCycleButton() {
        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_off);
    }

    public void setCurrentFragment(String fragmentName) {
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
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.player_fragment_container, PlayVideoFragment.newInstance(this.playerDevice, 0))
            .commit();
    }

    private void showPlayMultiVideoFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.player_fragment_container, PlayMultiVideoFragment.newInstance(this.playerDevice, 0))
                .commit();
    }

    public void playSignalVideo(PlayerDevice playerDevice, int index) {
        this.playerDevice = playerDevice;
        /* �໭�沥���򵥻��沥���л�ʱҪ�ر��Զ�ѭ������ */
        if (autoPlayThread != null) {
            resetPlayCycleButton();
            handler.removeCallbacks(autoPlayThread);
            bAutoCyclePlaying = false;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.player_fragment_container, PlayVideoFragment.newInstance(this.playerDevice, index))
                .commit();
    }

    public void playMultiVideo(PlayerDevice playerDevice, int index) {
        this.playerDevice = playerDevice;
        /* �����沥����໭�沥���л�ʱҪ�ر��Զ�ѭ������ */
        if (autoPlayThread != null) {
            resetPlayCycleButton();
            handler.removeCallbacks(autoPlayThread);
            bAutoCyclePlaying = false;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.player_fragment_container, PlayMultiVideoFragment.newInstance(this.playerDevice, index))
                .commit();
    }

    public int[] getFragmentLocation() {
        View view = findViewById(R.id.player_fragment_container);
        int[] viewLocation = new int[4];
        viewLocation[0] = view.getLeft();
        viewLocation[1] = view.getRight();
        viewLocation[2] = view.getTop();
        viewLocation[3] = view.getBottom();
        return viewLocation;
    }

    public String getCurrentDeviceId() {
        return this.deviceId;
    }

    public void setCurrentDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
            playVideoFragment.handleMessage(msg);
        } else if (this.currentFragmentName.equals("play_multi_video_fragment")) {
            multiVideoFragment.handleMessage(msg);
        }
    }
}
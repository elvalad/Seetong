package com.seetong.app.seetong.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;

/**
 * PlayerActivity 是播放设备录像的 Activity，它在 DeviceFragment 包含设备信息时，点击会进入.
 * 它自身包括播放一个窗口的录像和四个窗口的录像.
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
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onDestroy() {
        Log.i(PlayerActivity.class.getName(), "onDestroy...");
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    private void initWidget() {
        playerBackButton = (ImageButton) findViewById(R.id.player_back);
        playerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerActivity.this.finish();
                /* TODO:在单路fragment和多路fragment之间切换时需要注意这里该如何处理 */
                if (currentFragmentName.equals("play_video_fragment")) {
                    playVideoFragment.stopPlay();
                    /* 退出单画面播放页面时要关闭自动循环播放 */
                    if (autoPlayThread != null) {
                        bAutoCyclePlaying = false;
                        handler.removeCallbacks(autoPlayThread);
                    }
                } else if (currentFragmentName.equals("play_multi_video_fragment")){
                    multiVideoFragment.stopPlayList();
                    /* 退出多画面播放时要关闭自动循环播放*/
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
                        /* 调用handler的removeCallbacks方法，删除队列中的线程，停止自动循环播放线程 */
                        toast(R.string.player_auto_play_off);
                        playerCycleButton.setImageResource(R.drawable.tps_play_cycle_off);
                        handler.removeCallbacks(autoPlayThread);
                        bAutoCyclePlaying = false;
                    } else {
                        /* 调用handler的post方法，将执行线程放入到队列中 */
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
                onRecordPlayBack();
            }
        });
        playerPlaybackButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
                /* 停止声音 */
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
                /* 选择高清播放 */
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
                /* TODO:播放设置 */
            }
        });
        playerSettingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
                /* TODO:设备录像 */
                if (bVideoRecord) {
                    /* 关闭视频录像 */
                    playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_off);
                    playerRecordButton.setTextColor(getResources().getColor(R.color.gray));
                    bVideoRecord = false;
                    offVideoRecord();
                } else {
                    /* 打开视频录像 */
                    playerRecordButton.setBackgroundResource(R.drawable.tps_play_record_on);
                    playerRecordButton.setTextColor(getResources().getColor(R.color.green));
                    bVideoRecord = true;
                    onVideoRecord();
                }
            }
        });

        playerSpeakButton = (Button) findViewById(R.id.player_microphone);
        playerSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* TODO:设备录像 */
            }
        });
        playerSpeakButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    playerSpeakButton.setBackgroundResource(R.drawable.tps_play_microphone_on);
                    playerSpeakButton.setTextColor(getResources().getColor(R.color.green));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    playerSpeakButton.setBackgroundResource(R.drawable.tps_play_microphone_off);
                    playerSpeakButton.setTextColor(getResources().getColor(R.color.gray));
                }
                return false;
            }
        });

        playerCaptureButton = (Button) findViewById(R.id.player_capture);
        playerCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 设备截图 */
                onVideoCapture();
            }
        });
        playerCaptureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
                /* 这里线程休眠1000ms，防止自动循环播放线程开启时CPU占用率一直很高 */
                Thread.sleep(1000);
                if (currentFragmentName.equals("play_video_fragment")) {
                    playVideoFragment.autoCyclePlay();
                    /* 设置自动循环播放时间 */
                    handler.postDelayed(autoPlayThread, 5000);
                } else if (currentFragmentName.equals("play_multi_video_fragment")) {
                    multiVideoFragment.autoCyclePlay();
                    /* 设置自动循环播放时间 */
                    handler.postDelayed(autoPlayThread, 5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

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
        /* 多画面播放向单画面播放切换时要关闭自动循环播放 */
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
        /* 单画面播放向多画面播放切换时要关闭自动循环播放 */
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
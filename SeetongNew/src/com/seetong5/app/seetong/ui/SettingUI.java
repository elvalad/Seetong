package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.system.MediaPlayer;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;

import java.util.ArrayList;
import java.util.Collections;

public class SettingUI extends BaseActivity {
    TextView mtvPtzValue;
    SeekBar mPtzBar;
    ToggleButton mtbAutoPlay;
    RadioGroup mrgViewSelect;
    Spinner mspPicSetting;
    ToggleButton m_btnAlarm;
    Spinner m_cbxAlarmSound;
    ToggleButton m_btnInCallMode;
    ArrayList<String> m_soundAry = new ArrayList<>();
    ArrayAdapter<String> m_adpAry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_ui);
        initWidget();
    }

    protected void initWidget() {
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_setting_title));
        mtbAutoPlay = (ToggleButton) findViewById(R.id.tbAutoPlay);
        mrgViewSelect = (RadioGroup) findViewById(R.id.rgViewSelect);
        mspPicSetting = (Spinner) findViewById(R.id.spPicSetting);
        mtvPtzValue = (TextView) findViewById(R.id.tvPtzCtrlValue);
        mPtzBar = (SeekBar) findViewById(R.id.sbPtzCtrl); // 云台步长设置
        mPtzBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { // 云台步长拖动改变监听器
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seekBar.setProgress((progress < 1?1:progress));
                mtvPtzValue.setText((progress < 1 ? 1 : progress) + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mspPicSetting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        m_btnAlarm = (ToggleButton) findViewById(R.id.tb_enable_alarm);
        m_btnAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int visible = isChecked ? View.VISIBLE : View.GONE;
                if (!isChecked) {
                    MediaPlayer.stop();
                }

                findViewById(R.id.layout_alarm_sound).setVisibility(visible);
            }
        });

        m_btnInCallMode = (ToggleButton) findViewById(R.id.tb_in_call_mode);

        String[] ls = getResources().getStringArray(R.array.string_ary_alarm_sound_name);
        Collections.addAll(m_soundAry, ls);

        m_adpAry = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_soundAry);
        m_adpAry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_cbxAlarmSound = (Spinner) findViewById(R.id.sp_alarm_sound);
        m_cbxAlarmSound.setAdapter(m_adpAry);
        m_cbxAlarmSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Config.m_alarm_sound == position) return;
                Config.m_alarm_sound = position;
                Config.m_alarm_sound_res_id = Global.m_resSound[position];
                MediaPlayer.play();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadData();
        if (!Config.m_enable_alarm) {
            findViewById(R.id.layout_alarm_sound).setVisibility(View.GONE);
        }
    }

    public void loadData() {
        mPtzBar.setProgress(Config.m_ptz_step);
        mtvPtzValue.setText(mPtzBar.getProgress() + "/" + mPtzBar.getMax());
        mtbAutoPlay.setChecked(Config.m_auto_play);
//		mrgViewSelect.check((Config.m_view_num == 0)? R.id.rbOneView : R.id.rbFourView);
        int pos = 1;
        if (Config.m_view_num == 1) {
            pos = 0;
        }else if (Config.m_view_num >= 4) {
            pos = 1;
        } /*else if (Config.m_view_num == 9) {
            pos = 2;
        } else if (Config.m_view_num == 16) {
            pos = 3;
        }*/

        mspPicSetting.setSelection(pos);
        m_btnAlarm.setChecked(Config.m_enable_alarm);
        m_cbxAlarmSound.setSelection(Config.m_alarm_sound);
        m_btnInCallMode.setChecked(Config.m_in_call_mode);
    }

    public void saveData() {
        Config.m_ptz_step = (mPtzBar.getProgress() < 1)? 1: mPtzBar.getProgress();
        int pos = mspPicSetting.getSelectedItemPosition();
        String view_nums[] = getResources().getStringArray(R.array.t_view_types);
        Config.m_view_num = Integer.valueOf(view_nums[pos]);
        Config.m_auto_play = mtbAutoPlay.isChecked();
        Config.m_enable_alarm = m_btnAlarm.isChecked();
        Config.m_alarm_sound = m_cbxAlarmSound.getSelectedItemPosition();
        Config.m_in_call_mode = m_btnInCallMode.isChecked();
        Config.saveData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayer.stop();
        saveData();
    }
}
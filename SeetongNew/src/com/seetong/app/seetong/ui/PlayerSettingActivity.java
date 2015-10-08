package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import com.seetong.app.seetong.R;

/**
 * Created by gmk on 2015/10/8.
 */
public class PlayerSettingActivity extends BaseActivity {

    private String deviceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_player_setting);
        deviceId = getIntent().getStringExtra("device_setting_id");
        initWidget();
    }

    private void initWidget() {
        TextView textView = (TextView) findViewById(R.id.device_setting_id);
        textView.setText(deviceId);
    }
}

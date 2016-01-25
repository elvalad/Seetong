package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.widget.TextView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;

import java.io.*;

/**
 * Created by Administrator on 2016/1/4.
 */
public class CrashInfo extends BaseActivity {
    private TextView crashInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_info);
        initWidget();
    }

    private void initWidget() {
        crashInfoView = (TextView) findViewById(R.id.crash_info);
        File crashFile = new File(Global.getCrashinfoDir() + "/crash.log");
        StringBuffer crashInfo = new StringBuffer();
        if (!crashFile.exists()) {
            crashInfo.append("No crash info!");
        } else {
            try {
                InputStreamReader read = new InputStreamReader(new FileInputStream(crashFile), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(read);
                String tmp = null;
                while ((tmp = bufferedReader.readLine()) != null) {
                    crashInfo.append(tmp);
                    crashInfo.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        crashInfoView.setText(crashInfo.toString());
    }
}

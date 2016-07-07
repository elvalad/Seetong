package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.seetong.app.seetong.R;

/**
 * Created by Administrator on 2016/7/7.
 */
public class FeedbackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initWidget();
    }

    private void initWidget() {
        ImageButton backButton = (ImageButton) findViewById(R.id.feedback_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackActivity.this.finish();
            }
        });
    }
}

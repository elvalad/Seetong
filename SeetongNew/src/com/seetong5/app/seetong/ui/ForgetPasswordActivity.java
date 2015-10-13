package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import com.seetong5.app.seetong.R;

/**
 * Created by Administrator on 2015/9/29.
 */
public class ForgetPasswordActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_forget_password);
        initWidget();
    }

    private void initWidget() {
        ImageButton backButton = (ImageButton) findViewById(R.id.forget_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgetPasswordActivity.this.finish();
            }
        });
    }
}

package com.seetong.app.seetong.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.seetong.app.seetong.R;

/**
 * LoginActivity 是首次进入 Seetong 后的第一个页面，用于让用户注册登录 Seetong.
 * 此页面包含几个相关的 Button，点击之后会进入不容的 Activity.
 * 需要和服务器端交互用于登录用户信息的检查，主页新的用户，忘记密码等信息.
 *
 * Created by gmk on 2015/9/11.
 */
public class LoginActivity extends Activity {
    @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initWidget();
    }

    // TODO:需要和服务器交互完成用户账号密码检查，用户注册等操作
    private void initWidget() {
        Button loginButton = (Button) findViewById(R.id.login_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        loginButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v = findViewById(R.id.login_login);
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.getBackground().setAlpha(150);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        Button registerButton = (Button) findViewById(R.id.login_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}

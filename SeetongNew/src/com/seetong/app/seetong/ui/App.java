package com.seetong.app.seetong.ui;

import com.baidu.frontia.FrontiaApplication;
import com.seetong.app.seetong.Global;

public class App extends FrontiaApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Global.onAppStart(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Global.onAppTerminate();
    }
}

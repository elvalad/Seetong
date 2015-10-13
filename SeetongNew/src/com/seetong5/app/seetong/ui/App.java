package com.seetong5.app.seetong.ui;

import com.baidu.frontia.FrontiaApplication;
import com.seetong5.app.seetong.Global;

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

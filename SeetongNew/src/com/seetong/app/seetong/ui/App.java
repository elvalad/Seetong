package com.seetong.app.seetong.ui;

import android.app.Application;
import com.seetong.app.seetong.Global;

public class App extends Application {
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

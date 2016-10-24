package com.seetong.app.seetong.model;

import ipc.android.sdk.com.NetSDK_IPC_ENTRY;

/**
 * Created by Administrator on 2016/10/24.
 */
public class LanDeviceInfo {

    private NetSDK_IPC_ENTRY entry;

    private boolean bChecked;

    public NetSDK_IPC_ENTRY getEntry() {
        return entry;
    }

    public void setEntry(NetSDK_IPC_ENTRY entry) {
        this.entry = entry;
    }

    public boolean getChecked() {
        return bChecked;
    }

    public void setChecked(boolean bChecked) {
        this.bChecked = bChecked;
    }
}

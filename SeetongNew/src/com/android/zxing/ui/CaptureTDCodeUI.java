package com.android.zxing.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.SurfaceHolder.Callback;
import com.android.zxing.decode.CaptureActivity;
import com.google.zxing.Result;
import com.seetong.app.seetong.ui.MainActivity2;

/**
 * @author dswitkin@google.com (Daniel Switkin)
 * @declaration 二维码截图并解码界面
 * @date 2012-10-13 上午10:40:16
 */
public class CaptureTDCodeUI extends CaptureActivity implements Callback {

    public void handleResult(Result obj, Bitmap barcode) {
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra(TD_CODE_RESULT_KEY, obj.getText());
        setResult(RESULT_OK, intent);
        finish();
    }
}
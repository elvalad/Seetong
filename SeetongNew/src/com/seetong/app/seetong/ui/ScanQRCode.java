package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceHolder;
import com.android.zxing.decode.CaptureActivity;
import com.google.zxing.Result;

/**
 * Created by Administrator on 2014-07-18.
 */
public class ScanQRCode extends CaptureActivity implements SurfaceHolder.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void handleResult(Result obj, Bitmap barcode) {
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra(TD_CODE_RESULT_KEY, obj.getText());
        setResult(RESULT_OK, intent);
    }
}

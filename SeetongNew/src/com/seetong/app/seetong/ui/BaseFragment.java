package com.seetong.app.seetong.ui;

import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by Administrator on 2014-05-12.
 */
public class BaseFragment extends Fragment {
    public String T(Object t) {
        String _msg = "";
        if (t instanceof Integer) {
            _msg = getResources().getString((Integer)t);
        } else if (t instanceof String) {
            _msg = (String) t;
        }

        return _msg;
    }

    public Drawable D(int id) {
        return getResources().getDrawable(id);
    }

    public void toast(Object msg) {
        final String message = T(msg);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseFragment.this.getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void toast(Object msg, final int errNO) {
        String _msg = T(msg);
        final String message = _msg + "(" + errNO + ")";
        if (Looper.myLooper() != Looper.getMainLooper()) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseFragment.this.getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMessage(Handler h, int what, int arg1, int arg2, Object obj) {
        android.os.Message msg = h.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        h.sendMessage(msg);
    }

    public synchronized static boolean isNullStr(String str) {
        boolean isOK = false;
        if (str == null || "".equals(str.trim())) {
            isOK = true;
        }
        return isOK;
    }

    public boolean onStatusEvent(int lUser, int nStateCode, String response) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread arg0, Throwable arg1) {
                Log.e("DEBUG", "Thread.setDefaultUncaughtExceptionHandler is fail...begin");
                arg1.printStackTrace();
                Log.e("DEBUG", "Thread.setDefaultUncaughtExceptionHandler is fail...end");
            }
        });*/
    }

    public boolean onBackPressed() {
        return false;
    }
}

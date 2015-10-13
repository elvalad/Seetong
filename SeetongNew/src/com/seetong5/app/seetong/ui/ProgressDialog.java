package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;

public class ProgressDialog {
	private Context act;
	private String msg;
    private AlertDialog dialog;
	private View view;

	private boolean cancelable;
	private TextView loading_text;
    private int m_timeout;
    private String m_timeoutToast;
    private boolean m_isTimeout;
    private boolean m_isCanceled;
    private TimeoutThread m_thread;
    private ICallback m_cb;
    private MyHandler m_handler;

	public ProgressDialog(Context act) {
		this.act = act;
		buildView();
	}

	public ProgressDialog(Context act, Object msg) {
		this.act = act;
		this.msg = BaseActivity.T(msg);
		buildView();
	}

	public ProgressDialog setCancelable(boolean flag) {
		cancelable = flag;
		return this;
	}

	public void setTitle(Object text){
		changeText(text);
	}

    public void setTimeoutToast(String text) {
        m_timeoutToast = text;
    }
    public void setCallback(ICallback cb) {
        m_cb = cb;
    }
	
	public void changeText(Object text) {
		msg = BaseActivity.T(text);
        if (null == loading_text) buildView();
		loading_text.setText(msg);
	}

	public boolean isShowing() {
        return null != dialog && dialog.isShowing();
    }

	private void buildView() {
        m_handler = new MyHandler(this);
        LayoutInflater inflater = LayoutInflater.from(act);
		view = inflater.inflate(R.layout.loading, null);
		if (msg != null) {
			loading_text = (TextView) view.findViewById(R.id.loading_text);
			loading_text.setText(msg);
		}
	}

	public ProgressDialog show() {
		return show(0);
	}

    public ProgressDialog show(int timeout) {
        if (timeout > 0) {
            if (null != m_thread) m_thread.interrupt();
            m_thread = new TimeoutThread();
            m_isTimeout = false;
            m_timeout = timeout;
            m_thread.start();
        }

        m_isCanceled = false;
        buildView();		//"Error:The specified child already has a parent." modify by yinql
        dialog = new AlertDialog.Builder(act).setCancelable(cancelable).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                    if (!ProgressDialog.this.cancelable) return false;
                    ProgressDialog.this.cancel();
                    return true;
                }
                return false;
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return this;
    }

	public void cancel() {
        if (null != m_thread) m_thread.interrupt();
        m_isTimeout = false;
        m_isCanceled = true;
        if (null != dialog) dialog.cancel();
        if (null != m_cb) m_cb.onCancel();
	}
	
	public void dismiss(){
        if (null != m_thread) m_thread.interrupt();
        m_isTimeout = false;
        m_isCanceled = false;
		if (null != dialog) dialog.dismiss();
	}

    private void dismissByTimeout() {
        if (null != dialog && !dialog.isShowing()) return;
        m_isTimeout = true;
        if (null != dialog) dialog.dismiss();
        if (null == m_cb) return;
        m_cb.onTimeout();
    }

    public boolean isTimeout() { return m_isTimeout; }

    public boolean isCanceled() { return m_isCanceled; }

    private static class MyHandler extends Handler {
        ProgressDialog m_self;
        public MyHandler(ProgressDialog self) {
            m_self = self;
        }

        @Override
        public void handleMessage(Message msg) {
            m_self.dismissByTimeout();
            if (null == m_self.m_timeoutToast) return;
            Toast.makeText(Global.m_ctx, m_self.m_timeoutToast, Toast.LENGTH_SHORT).show();
        }
    }

    private class TimeoutThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(m_timeout, 0);
                if (m_isCanceled) return;
                Message message = new Message();
                message.what = 1;
                m_handler.sendMessage(message);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public interface ICallback {
        void onTimeout();
        boolean onCancel();
    }
}

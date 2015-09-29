package com.seetong.app.seetong.ui.ext;

/**
 * Created by Administrator on 2015/9/28.
 */
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;

public class CountDownButtonHelper {
    // ����ʱtimer
    private CountDownTimer countDownTimer;
    // ��ʱ�����Ļص��ӿ�
    private OnFinishListener listener;
    private Button button;

    /**
     * @param button
     *            ��Ҫ��ʾ����ʱ��Button
     * @param defaultString
     *            Ĭ����ʾ���ַ���
     * @param max
     *            ��Ҫ���е���ʱ�����ֵ,��λ����
     * @param interval
     *            ����ʱ�ļ������λ����
     */
    public CountDownButtonHelper(final Button button,
                                 final String defaultString, int max, int interval) {

        this.button = button;
        // ����CountDownTimer������׼ȷ��ʱ����onTick�������õ�ʱ��time����1-10ms���ҵ�����ᵼ�����һ�벻�����onTick()
        // ��ˣ����ü����ʱ��Ĭ�ϼ�ȥ��10ms���Ӷ���ȥ��
        // �������ϵ�΢�������һ�����ʾʱ�������10ms�ӳٵĻ��ۣ�������ʾʱ���1s��max*10ms��ʱ�䣬����ʱ�����ʾ����,��ʱ������
        countDownTimer = new CountDownTimer(max * 1000, interval * 1000 - 10) {
            @Override
            public void onTick(long time) {
                // ��һ�ε��û���1-10ms���������Ҫ+15ms����ֹ��һ��������ʾ���ڶ�������ʾ2s
                button.setText(defaultString + "(" + ((time + 15) / 1000)
                        + "s)");
                Log.d("CountDownButtonHelper", "time = " + (time) + " text = "
                        + ((time + 15) / 1000));
            }

            @Override
            public void onFinish() {
                button.setEnabled(true);
                button.setText(defaultString);
                if (listener != null) {
                    listener.finish();
                }
            }
        };
    }

    /**
     * ��ʼ����ʱ
     */
    public void start() {
        button.setEnabled(false);
        countDownTimer.start();
    }

    /**
     * ���õ���ʱ�����ļ�����
     * @param listener
     */
    public void setOnFinishListener(OnFinishListener listener) {
        this.listener = listener;
    }

    /**
     * ��ʱ�����Ļص��ӿ�
     * @author zhaokaiqiang
     */
    public interface OnFinishListener {
        public void finish();
    }
}


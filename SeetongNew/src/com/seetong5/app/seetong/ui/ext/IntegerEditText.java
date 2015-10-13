package com.seetong5.app.seetong.ui.ext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

/**
 * Created by Administrator on 2014-07-21.
 */
public class IntegerEditText extends EditText implements TextWatcher {
    private int m_min = 0;
    private int m_max = 6;

    public IntegerEditText(Context context) {
        this(context, null);
    }

    public IntegerEditText(Context context, AttributeSet attrs) {
        //这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public IntegerEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        addTextChangedListener(this);
    }

    public void setRange(int min, int max) {
        m_min = min;
        m_max = max;
    }

    public Pair<Integer, Integer> getRange() {
        return new Pair<>(m_min, m_max);
    }

    public boolean validate() {
        String text = getText().toString();
        if ("".equals(text)) return false;
        int val = 0;

        try {
            val = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return false;
        }

        return val >= m_min && val <= m_max;
    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 设置晃动动画
     */
    public void setShakeAnimation(){
        clearAnimation();
        clearFocus();
        this.setAnimation(shakeAnimation(5));
        requestFocus();
    }


    /**
     * 晃动动画
     * @param counts 1秒钟晃动多少下
     * @return
     */
    public static Animation shakeAnimation(int counts){
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }
}


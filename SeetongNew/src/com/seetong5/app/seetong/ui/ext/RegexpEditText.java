package com.seetong5.app.seetong.ui.ext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2014-07-21.
 */
public class RegexpEditText extends EditText implements TextWatcher {
    boolean m_required = true;
    private String m_regexp = "";

    public RegexpEditText(Context context) {
        this(context, null);
    }

    public RegexpEditText(Context context, AttributeSet attrs) {
        //这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public RegexpEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        requestFocusFromTouch();
        addTextChangedListener(this);
    }

    public void setRegexp(String regexp) {
        m_regexp = regexp;
    }

    public void setRequired(boolean require) {
        m_required = require;
    }

    public boolean isRequired() {
        return m_required;
    }

    public boolean validate() {
        String text = getText().toString();
        if ("".equals(text) && m_required) return false;
        if ("".equals(m_regexp)) return true;
        Pattern p = Pattern.compile(m_regexp);
        Matcher m = p.matcher(text);
        return m.matches();
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

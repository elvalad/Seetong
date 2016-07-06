package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.TextView;
import com.android.utils.NetworkUtils;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.ui.ext.CountDownButtonHelper;
import com.seetong.app.seetong.ui.utils.DataCheckUtil;
import com.seetong.service.SMSBroadcastReceiver;
import ipc.android.sdk.com.SDK_CONSTANT;
import java.io.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegisterActivity主要用于通过App从服务器端注册用户，注册目前只支持邮箱注册和手机注册；
 * Created by gmk on 2015/9/11.
 */
public class RegisterActivity extends BaseActivity {

    public static class RegisterInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String userEmail;
        public String userPhone;
        public String userPwd;
        public String verifyCode;

        public RegisterInfo() {

        }

        public RegisterInfo(String userEmail, String userPhone, String userPwd, String verifyCode) {
            this.userEmail = userEmail;
            this.userPhone = userPhone;
            this.userPwd = userPwd;
            this.verifyCode = verifyCode;
        }
    }

    private RegisterInfo mRegInfo;
    private ProgressDialog mTipDlg;
    private boolean bRegByMail = true;
    private String verifyCode = new String();
    private ImageButton backButton;
    private Button obtainCheckCodeButton;
    private Button registerButton;
    private Timestamp startTime = new Timestamp(System.currentTimeMillis());
    private Timestamp endTime;
    private EditText registerEditPassword;
    private TextView passwordStrength;
    private static final int MSG_GET_VERIFY_CODE_FASE = 0;
    private SMSBroadcastReceiver smsBroadcastReceiver;
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        initWidget();
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.reg_is_getting_code);
        mTipDlg.setCancelable(true);

        backButton = (ImageButton) findViewById(R.id.register_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });

        obtainCheckCodeButton = (Button) findViewById(R.id.register_verify_button);
        obtainCheckCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetVerifyCode();
            }
        });
        obtainCheckCodeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view = findViewById(R.id.register_verify_button);
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.getBackground().setAlpha(150);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        registerButton = (Button) findViewById(R.id.register_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegister();
            }
        });
        registerButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v = findViewById(R.id.register_register);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getBackground().setAlpha(150);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        passwordStrength = (TextView) findViewById(R.id.register_password_strength);
        registerEditPassword = (EditText) findViewById(R.id.register_password);
        registerEditPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordStrength.setVisibility(View.VISIBLE);
                switch (DataCheckUtil.checkPassword(charSequence.toString())) {
                    case 0:
                        passwordStrength.setText(R.string.register_password_strength_0);
                        passwordStrength.setTextColor(getResources().getColor(R.color.password_strength_0));
                        break;
                    case 1:
                        passwordStrength.setText(R.string.register_password_strength_1);
                        passwordStrength.setTextColor(getResources().getColor(R.color.password_strength_1));
                        break;
                    case 2:
                        passwordStrength.setText(R.string.register_password_strength_2);
                        passwordStrength.setTextColor(getResources().getColor(R.color.password_strength_2));
                        break;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (registerEditPassword.getText().length() == 0) {
                    passwordStrength.setVisibility(View.GONE);
                }
            }
        });
        registerEditPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    registerEditPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    registerEditPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        smsBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        final EditText verifyCodeText = (EditText) findViewById(R.id.register_verify_code);
        this.registerReceiver(smsBroadcastReceiver, intentFilter);
        smsBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {
            @Override
            public void onReceived(String message) {
                verifyCodeText.setText(getValidCode(message));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(smsBroadcastReceiver);
    }

    private String getValidCode(String message) {
        Pattern pattern = Pattern.compile("\\d{6}");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private void onRegister() {
        if (getFormatData()) {
            /* 如果在按menu或back按钮的时候，以及打开了软键盘，则将软键盘隐藏 */
            hideInputPanel(null);
            mTipDlg.setTitle(R.string.please_wait_communication);
            mTipDlg.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int iRet;
                    if (bRegByMail) {
                        iRet = LibImpl.getInstance().getFuncLib().RegCSUserEx(mRegInfo.userEmail, mRegInfo.userPwd, mRegInfo.userEmail, "", mRegInfo.verifyCode);
                    } else {
                        iRet = LibImpl.getInstance().getFuncLib().RegCSUserEx(mRegInfo.userPhone, mRegInfo.userPwd, "", mRegInfo.userPhone, mRegInfo.verifyCode);
                    }
                    if (mTipDlg.isCanceled()) {
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTipDlg.dismiss();

                            if (iRet == SDK_CONSTANT.reg_error_null) {
                                Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
                                it.putExtra(Constant.REG_USER_KEY, gStr(R.id.register_user));
                                it.putExtra(Constant.REG_PASSWORD_KEY, gStr(R.id.register_password));
                                Global.m_spu_login.saveSharedPreferences(Define.IS_FIRST_LOGIN, true);
                                startActivity(it);
                                RegisterActivity.this.finish();
                            } else {
                                toast(ConstantImpl.getRegErrText(iRet, false));
                            }
                        }
                    });

                }
            }).start();
        }
    }

    private void onGetVerifyCode() {
        if (!DataCheckUtil.isRightEmail(gStr(R.id.register_user)) &&
                !DataCheckUtil.isRightPhone(gStr(R.id.register_user))) {
            toast(R.string.register_invalid_user_name);
        } else {
            getVerifyCode();
        }
    }

    public String gStrRemoveColon(Object msg) {
        String _msg = "";
        if (msg instanceof Integer) {
            _msg = T((Integer) msg);
        } else if (msg instanceof String) {
            _msg = (String) msg;
        }
        if (_msg != null) {
            _msg = _msg.replaceAll(":", "").replaceAll("：", "").replaceAll("　", "");
        } else {
            _msg = "";
        }
        return _msg;
    }

    private boolean getFormatData() {
        boolean bRet = false;
        if (isNullStr(gStr(R.id.register_user))) {
            toast(R.string.register_user_null);
            return false;
        }

        if (isNullStr(gStr(R.id.register_password))) {
            toast(R.string.register_password_null);
            return false;
        }

        if (gStr(R.id.register_password).length() > 30) {
            toast(R.string.register_password_too_long);
            return false;
        }

        if (isNullStr(gStr(R.id.register_verify_code))) {
            toast(R.string.register_verify_code_null);
            return false;
        }

        /* 检查用户输入数据合法性 */
        if (!DataCheckUtil.isRightEmail(gStr(R.id.register_user)) &&
                !DataCheckUtil.isRightPhone(gStr(R.id.register_user))) {
            toast(R.string.register_invalid_user_name);
        } else if (!DataCheckUtil.isRightUserPwd(gStr(R.id.register_password))) {
            toast(R.string.register_invalid_user_password);
        } else {
            if (mRegInfo == null) {
                mRegInfo = new RegisterInfo();
            }

            if (DataCheckUtil.isRightEmail(gStr(R.id.register_user))) {
                bRegByMail = true;
                mRegInfo.userEmail = gStr(R.id.register_user);
            } else if (DataCheckUtil.isRightPhone(gStr(R.id.register_user))) {
                bRegByMail = false;
                mRegInfo.userPhone = gStr(R.id.register_user);
            }
            mRegInfo.userPwd = gStr(R.id.register_password);
            mRegInfo.verifyCode = gStr(R.id.register_verify_code);

            /* 检查校验码是否过期 */
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -10);
            endTime = new Timestamp(calendar.getTimeInMillis());
            if (endTime.after(startTime)) {
                toast(R.string.register_verify_code_invalid);
                return false;
            }
        }

        return true;
    }

    private boolean getVerifyCode() {
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(R.string.dlg_network_check_tip);
            return false;
        } else {
            /*int bRet = LibImpl.getInstance().getFuncLib().GetRegNumber(gStr(R.id.register_user), "zh-cn");
            if (bRet != 0) {
                toast(ConstantImpl.getRegNumberErrText(bRet));
                return false;
            }*/
            obtainCheckCodeButton.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                     /* TODO:后续需要区分中文和英文的反馈信息 */
                    int bRet = LibImpl.getInstance().getFuncLib().GetRegNumber(gStr(R.id.register_user), "zh-cn");
                    if (bRet != 0) {
                        sendMessage(MSG_GET_VERIFY_CODE_FASE, 0, 0, null);
                        if (bRet == SDK_CONSTANT.get_reg_number_error_other) {
                            toast(R.string.register_network_err);
                        } else {
                            toast(ConstantImpl.getRegNumberErrText(bRet));
                        }
                    } else {
                        sendMessage(MSG_GET_VERIFY_CODE_FASE, 0, 0, null);
                        startTime = new Timestamp(System.currentTimeMillis());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CountDownButtonHelper helper = new CountDownButtonHelper(obtainCheckCodeButton,
                                        getResources().getString(R.string.register_gain_verify_code_hint_text),
                                        180, 1);
                                helper.setOnFinishListener(new CountDownButtonHelper.OnFinishListener() {
                                    @Override
                                    public void finish() {
                                    }
                                });
                                helper.start();
                            }
                        });
                    }
                }
            }).start();
        }
        return true;
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        android.os.Message msg = m_handler.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        m_handler.sendMessage(msg);
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case MSG_GET_VERIFY_CODE_FASE:
                obtainCheckCodeButton.setEnabled(true);
                break;
        }
    }
}


package com.seetong5.app.seetong.ui;

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
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.ext.CountDownButtonHelper;
import com.seetong5.app.seetong.ui.utils.DataCheckUtil;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by Administrator on 2015/9/29.
 */
public class ForgetPasswordActivity extends BaseActivity {

    private static class ForgetInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String userName;
        public String userEmail;
        public String userPhone;
        public String userPwd;
        public String verifyCode;

        public ForgetInfo() {}

        public ForgetInfo(String userName, String userEmail, String userPhone, String userPwd, String verifyCode) {
            this.userName = userName;
            this.userEmail = userEmail;
            this.userPhone = userPhone;
            this.userPwd = userPwd;
            this.verifyCode = verifyCode;
        }
    }

    private ForgetInfo forgetInfo;
    private ProgressDialog mTipDlg;
    private boolean bRegByMail = true;
    private String verifyCode = new String();
    private ImageButton backButton;
    private Button obtainCheckCodeButton;
    private Button resetPasswordButton;
    private Timestamp startTime = new Timestamp(System.currentTimeMillis());
    private Timestamp endTime;
    private EditText forgetPwdEditText;
    private TextView passwordStrength;
    private static final int MSG_GET_VERIFY_CODE_FASE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initWidget();
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.reg_is_getting_code);
        mTipDlg.setCancelable(true);

        backButton = (ImageButton) findViewById(R.id.forget_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgetPasswordActivity.this.finish();
            }
        });

        obtainCheckCodeButton = (Button) findViewById(R.id.forget_verify_button);
        obtainCheckCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetVerifyCode();
            }
        });
        obtainCheckCodeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view = findViewById(R.id.forget_verify_button);
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.getBackground().setAlpha(150);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        resetPasswordButton = (Button) findViewById(R.id.forget_reset_password);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetPassword();
            }
        });
        resetPasswordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v = findViewById(R.id.forget_reset_password);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getBackground().setAlpha(150);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getBackground().setAlpha(255);
                }
                return false;
            }
        });

        passwordStrength = (TextView) findViewById(R.id.forget_password_strength);
        forgetPwdEditText = (EditText) findViewById(R.id.forget_password);
        forgetPwdEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    forgetPwdEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    if (DataCheckUtil.isNullStr(gStr(R.id.forget_user))) {
                        toast(R.string.forget_user_null);
                        return;
                    }

                    if (!DataCheckUtil.isRightEmail(gStr(R.id.forget_user)) &&
                            !DataCheckUtil.isRightPhone(gStr(R.id.forget_user))) {
                        findViewById(R.id.mix_forget_phone_mail).setVisibility(View.VISIBLE);
                        findViewById(R.id.forget_phone_mail_line).setVisibility(View.VISIBLE);
                        if (isNullStr(gStr(R.id.forget_phone_mail))) {
                            toast(R.string.forget_input_phone_mail);
                            return;
                        }

                        if (!DataCheckUtil.isRightEmail(gStr(R.id.forget_phone_mail)) &&
                                !DataCheckUtil.isRightPhone(gStr(R.id.forget_phone_mail))) {
                            toast(R.string.forget_input_correct_phone_mail);
                            return;
                        }
                    }
                } else {
                    forgetPwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        forgetPwdEditText.addTextChangedListener(new TextWatcher() {
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
                if (forgetPwdEditText.getText().length() == 0) {
                    passwordStrength.setVisibility(View.GONE);
                }
            }
        });
    }

    private void onGetVerifyCode() {
        if (isNullStr(gStr(R.id.forget_user))) {
            toast(R.string.forget_user_null);
            return;
        }

        getVerifyCode();
    }

    private boolean getVerifyCode() {
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(R.string.dlg_network_check_tip);
            return false;
        } else {
            obtainCheckCodeButton.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int iRet;
                    if (DataCheckUtil.isRightEmail(gStr(R.id.forget_user)) ||
                            DataCheckUtil.isRightPhone(gStr(R.id.forget_user))) {
                        iRet = LibImpl.getInstance().getFuncLib().GetResetRegNumber(gStr(R.id.forget_user), gStr(R.id.forget_user), "zh-cn");
                    } else {
                        iRet = LibImpl.getInstance().getFuncLib().GetResetRegNumber(gStr(R.id.forget_phone_mail), gStr(R.id.forget_user), "zh-cn");
                    }

                    if (iRet != 0) {
                        sendMessage(MSG_GET_VERIFY_CODE_FASE, 0, 0, null);
                        toast(ConstantImpl.getRegNumberErrText(iRet));
                    } else {
                        sendMessage(MSG_GET_VERIFY_CODE_FASE, 0, 0, null);
                        startTime = new Timestamp(System.currentTimeMillis());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CountDownButtonHelper helper = new CountDownButtonHelper(obtainCheckCodeButton,
                                        getResources().getString(R.string.forget_gain_verify_code_hint_text),
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

    private boolean getFormatData() {
        if (isNullStr(gStr(R.id.forget_user))) {
            toast(R.string.forget_user_null);
            return false;
        }

        if (isNullStr(gStr(R.id.forget_password))) {
            toast(R.string.forget_password_null);
            return false;
        }

        if (gStr(R.id.forget_password).length() > 30) {
            toast(R.string.forget_password_too_long);
            return false;
        }

        if (isNullStr(gStr(R.id.forget_verify_code))) {
            toast(R.string.forget_verify_code_null);
            return false;
        }

        if (!DataCheckUtil.isRightUserPwd(gStr(R.id.forget_password))) {
            toast(R.string.forget_invalid_user_password);
            return false;
        } else {
            if (forgetInfo == null) {
                forgetInfo = new ForgetInfo();
            }

            forgetInfo.userName = gStr(R.id.forget_user);
            if (DataCheckUtil.isRightEmail(gStr(R.id.forget_user))) {
                bRegByMail = true;
                forgetInfo.userEmail = gStr(R.id.forget_user);
            } else if (DataCheckUtil.isRightPhone(gStr(R.id.forget_user))) {
                bRegByMail = false;
                forgetInfo.userPhone = gStr(R.id.forget_user);
            } else {
                if (DataCheckUtil.isRightEmail(gStr(R.id.forget_phone_mail))) {
                    bRegByMail = true;
                    forgetInfo.userEmail = gStr(R.id.forget_phone_mail);
                } else if (DataCheckUtil.isRightPhone(gStr(R.id.forget_phone_mail))) {
                    bRegByMail = false;
                    forgetInfo.userPhone = gStr(R.id.forget_phone_mail);
                }
            }
            forgetInfo.userPwd = gStr(R.id.forget_password);
            forgetInfo.verifyCode = gStr(R.id.forget_verify_code);

            /* 检查校验码是否过期 */
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -10);
            endTime = new Timestamp(calendar.getTimeInMillis());
            if (endTime.after(startTime)) {
                toast(R.string.forget_verify_code_invalid);
                return false;
            }
        }

        return true;
    }

    private void onResetPassword() {
        if (getFormatData()) {
            hideInputPanel(null);
            mTipDlg.setTitle(R.string.please_wait_communication);
            mTipDlg.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int iRet;
                    if (bRegByMail) {
                        iRet = LibImpl.getInstance().getFuncLib().ResetUserPassword(forgetInfo.userEmail, forgetInfo.userName, forgetInfo.userPwd, forgetInfo.verifyCode, "zh-cn");
                    } else {
                        iRet = LibImpl.getInstance().getFuncLib().ResetUserPassword(forgetInfo.userPhone, forgetInfo.userName, forgetInfo.userPwd, forgetInfo.verifyCode, "zh-cn");
                    }

                    if (mTipDlg.isCanceled()) {
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTipDlg.dismiss();
                            if (iRet == SDK_CONSTANT.reset_psw_error_null) {
                                ForgetPasswordActivity.this.finish();
                            } else {
                                toast(ConstantImpl.getForgetPasswordErrText(iRet));
                            }
                        }
                    });

                }
            }).start();
        }
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

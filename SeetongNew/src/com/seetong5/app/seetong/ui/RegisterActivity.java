package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.utils.NetworkUtils;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.ext.CountDownButtonHelper;
import com.seetong5.app.seetong.ui.utils.DataCheckUtil;
import ipc.android.sdk.com.SDK_CONSTANT;
import java.io.*;

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
        public String confirmPwd;
        public String verifyCode;

        public RegisterInfo() {

        }

        public RegisterInfo(String userEmail, String userPhone, String userPwd, String confirmPwd, String verifyCode) {
            this.userEmail = userEmail;
            this.userPhone = userPhone;
            this.userPwd = userPwd;
            this.confirmPwd = confirmPwd;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                        iRet = LibImpl.getInstance().getFuncLib().RegCSUserAgent(mRegInfo.userEmail, mRegInfo.userPwd, mRegInfo.userEmail, null, mRegInfo.verifyCode);
                    } else {
                        iRet = LibImpl.getInstance().getFuncLib().RegCSUserAgent(mRegInfo.userPhone, mRegInfo.userPwd, null, mRegInfo.userPhone, mRegInfo.verifyCode);
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

        if (isNullStr(gStr(R.id.register_confirm_password))) {
            toast(R.string.register_confirm_password_null);
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
            if (gStr(R.id.register_password).compareToIgnoreCase(gStr(R.id.register_confirm_password)) != 0) {
                toast(R.string.register_invalid_user_confirm_password);
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
                mRegInfo.confirmPwd = gStr(R.id.register_confirm_password);
                mRegInfo.verifyCode = gStr(R.id.register_verify_code);
                bRet = true;
            }
        }

        return bRet;
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

            new Thread(new Runnable() {
                @Override
                public void run() {
                     /* TODO:后续需要区分中文和英文的反馈信息 */
                    int bRet = LibImpl.getInstance().getFuncLib().GetRegNumber(gStr(R.id.register_user), "zh-cn");
                    if (bRet != 0) {
                        if (bRet == SDK_CONSTANT.get_reg_number_error_other) {
                            toast(R.string.register_network_err);
                        } else {
                            toast(ConstantImpl.getRegNumberErrText(bRet));
                        }
                    } else {
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
}


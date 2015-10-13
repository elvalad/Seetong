package com.seetong5.app.seetong.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.utils.NetworkUtils;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.utils.DataCheckUtil;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qinglei.yin@192.168.88.9<br>
 *         2014-2-17 下午5:34:36<br>
 * @declaration 用户注册界面
 */
@SuppressLint("SimpleDateFormat")
public class UserRegisterUI2 extends BaseActivity implements OnClickListener {
    public static class RegisterInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String userName;
        public String userPwd;
        public String configPwd;
        public String email;
        public String phone;
        public String code;

        public RegisterInfo() {

        }

        public RegisterInfo(String userName, String userPwd, String configPwd,
                            String email, String phone, String code) {
            this.userName = userName;
            this.userPwd = userPwd;
            this.configPwd = configPwd;
            this.email = email;
            this.phone = phone;
            this.code = code;
        }

        public RegisterInfo(String userName, String userPwd, String configPwd, String code) {
            this(userName, userPwd, configPwd, userPwd, "", "");
        }
    }

    private RegisterInfo mRegInfo;
    private ProgressDialog mTipDlg;
    private ImageView m_img_captcha;
    private boolean m_get_captcha_ok = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register_ui2);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_user_register_title));
        initWidget();
    }

    protected void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.reg_is_getting_code);
        mTipDlg.setCancelable(true);

        m_img_captcha = (ImageView) findViewById(R.id.imgRegCode);
        m_img_captcha.setOnClickListener(this);
        findViewById(R.id.btnRegisterUser).setOnClickListener(this);
        findViewById(R.id.btnRegisterUser).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //test:topseetest--yinql:123456
                String _userName = "test100";//"Test_"+new Date().getTime();
                sStr(R.id.etUserName, _userName);
                sStr(R.id.etUserPwd, "123456");
                sStr(R.id.etConfigPwd, "123456");
                return true;
            }
        });

        Button btnLeft = (Button) findViewById(R.id.btnLeft);
        btnLeft.setBackgroundResource(R.drawable.back);
        btnLeft.setText(R.string.back);
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setOnClickListener(this);

        startGetCode();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLeft:
                finish();
                break;
            case R.id.btnBackLogin://返回登录
                finish();
                break;

            case R.id.btnRegisterUser: {//用户注册
                if (getFormData()) {
                    // 如果在按menu或back按钮的时候，以及打开了软键盘，则将软键盘隐藏
                    hideInputPanel(null);
                    mTipDlg.setTitle(R.string.please_wait_communication);
                    mTipDlg.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final int isOK = LibImpl.getInstance().getFuncLib().RegCSUserAgent(mRegInfo.userName, mRegInfo.userPwd, mRegInfo.email, mRegInfo.phone, mRegInfo.code);
                            if (mTipDlg.isCanceled()) return;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTipDlg.dismiss();
                                    if (isOK == SDK_CONSTANT.reg_error_null) {//注册成功，返回登录界面
                                        Intent it = new Intent(UserRegisterUI2.this, LoginUI2.class);
                                        it.putExtra(LoginUI.USER_INFO_CONTENT_KEY, (Serializable) mRegInfo);
                                        setResult(RESULT_OK, it);
                                        finish();
                                    } else {
                                        toast(ConstantImpl.getRegErrText(isOK, false));
                                        startGetCode();
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
            break;

            case R.id.imgRegCode: {//更换验证码
                startGetCode();
            }
            break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startGetCode() {
        m_get_captcha_ok = false;
        //检测网络是否已打开
        if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE) {
            toast(R.string.dlg_network_check_tip);
        } else {
            Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.wait_tip);
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            m_img_captcha.setImageResource(R.drawable.progress_bar_icon);
            m_img_captcha.startAnimation(operatingAnim);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String strDate = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());
                    final String pathName = (UserRegisterUI2.this.getCacheDir().getAbsolutePath() + "/").replaceAll("//", "/") + "RegImg#" + strDate + ".jpg";
                    final File f = new File(pathName);
                    if (f != null) f.deleteOnExit();//程序退出后删除
                    final int isOK = LibImpl.getInstance().getFuncLib().GetRegImgAgent(pathName);
                    m_img_captcha.clearAnimation();
                    if (!isTopActivity(UserRegisterUI2.class.getName())) return;

                    runOnUiThread(new Runnable() {//update code
                        @Override
                        public void run() {
                            if (isOK == SDK_CONSTANT.reg_error_null) {
                                m_get_captcha_ok = true;
                                Bitmap bm = BitmapFactory.decodeFile(pathName);
                                m_img_captcha.setImageBitmap(bm);
                                Log.i("UserRegister", "fileInfo:filePath=" + pathName + ",fileSize=" + (f == null ? -1 : f.length()) + "byte");
                            } else {
                                toast(R.string.reg_get_code_error);
                            }
                        }
                    });
                }
            }).start();
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

    public String gStrNullError(Object msg, Object error) {
        String _msg = "";
        if (error instanceof Integer) {
            _msg = T((Integer) error);
        } else if (error instanceof String) {
            _msg = (String) error;
        }
        _msg = gStrRemoveColon(msg).concat(_msg);
        return _msg;
    }

    public String gStrNullError(Object msg) {
        return gStrNullError(msg, R.string.can_not_null);
    }

    public boolean getFormData() {
        boolean isOK = false;
        if (!m_get_captcha_ok) {
            toast(R.string.reg_get_code_error);
            return false;
        }

        // 非空检测
        if (isNullStr(gStr(R.id.etUserName))) {
            toast(gStrNullError(gStr2(R.id.tvUserName)));
            return false;
        }

        if (isNullStr(gStr(R.id.etUserPwd))) {
            toast(gStrNullError(gStr2(R.id.tvUserPwd)));
            return false;
        }

        if (isNullStr(gStr(R.id.etConfigPwd))) {
            toast(gStrNullError(gStr2(R.id.tvConfigPwd)));
            return false;
        }

        if (isNullStr(gStr(R.id.etRegCode))) {
            toast(gStrNullError(gStr2(R.id.tvRegCode)));
            return false;
        }

        //数据合法性检测
        if (!DataCheckUtil.isRightUserName(gStr(R.id.etUserName))) {
            toast(R.string.reg_illegal_user);
        } else if (!DataCheckUtil.isRightUserPwd(gStr(R.id.etUserPwd))) {
            toast(R.string.reg_illegal_pwd);
            //邮箱和手机号可以不填，要填必须填对
        } else if (!isNullStr(gStr(R.id.etEmail)) && !DataCheckUtil.isRightEmail(gStr(R.id.etEmail))) {
            toast(R.string.reg_illegal_email);
        } else if (!isNullStr(gStr(R.id.etPhone)) && !DataCheckUtil.isRightPhone(gStr(R.id.etPhone))) {
            toast(R.string.reg_illegal_phone);
        } else {
            //两次密码是否一致
            if (gStr(R.id.etUserPwd).compareToIgnoreCase(gStr(R.id.etConfigPwd)) != 0) {
                toast(R.string.reg_two_pwd_inconsistent);
            } else {
                if (mRegInfo == null) mRegInfo = new RegisterInfo();
                mRegInfo.userName = gStr(R.id.etUserName);
                mRegInfo.userPwd = gStr(R.id.etUserPwd);
                mRegInfo.configPwd = gStr(R.id.etConfigPwd);
                mRegInfo.email = gStr(R.id.etEmail);
                mRegInfo.phone = gStr(R.id.etPhone);
                mRegInfo.code = gStr(R.id.etRegCode);
                isOK = true;
            }
        }

        return isOK;
    }
}

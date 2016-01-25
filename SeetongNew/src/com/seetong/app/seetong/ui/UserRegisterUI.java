package com.seetong.app.seetong.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.utils.NetworkUtils;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.ConstantImpl;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.ui.utils.DataCheckUtil;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @declaration 用户注册界面
 * @author qinglei.yin@192.168.88.9<br>
 *2014-2-17 下午5:34:36<br>
 */
@SuppressLint("SimpleDateFormat")
public class UserRegisterUI extends BaseActivity implements OnClickListener{
	public static class RegisterInfo implements Serializable{
		private static final long serialVersionUID = 1L;
		public String userName;
		public String userPwd;
		public String configPwd;
		public String email;
		public String phone;
		public String code;
		public RegisterInfo(){
			
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_user_register_title));
        initWidget();
    }

	protected void initWidget() {
		mTipDlg = new ProgressDialog(this, R.string.reg_is_getting_code);
		mTipDlg.setCancelable(true);
		
		findViewById(R.id.btnBackLogin).setOnClickListener(this);
		findViewById(R.id.btnRegisterUser).setOnClickListener(this);
		findViewById(R.id.imgRegCode).setOnClickListener(this);
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
		
		startGetCode();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBackLogin://返回登录
			finish();
			break;
			
		case R.id.btnRegisterUser:{//用户注册
			if(getFormData()){
				// 如果在按menu或back按钮的时候，以及打开了软键盘，则将软键盘隐藏
                hideInputPanel(null);
                int isOK = LibImpl.getInstance().getFuncLib().RegCSUserAgent(mRegInfo.userName, mRegInfo.userPwd, mRegInfo.email, mRegInfo.phone, mRegInfo.code);
                if(isOK == SDK_CONSTANT.reg_error_null){//注册成功，返回登录界面
                    Intent it = new Intent(this, LoginUI.class);
                    it.putExtra(LoginUI.USER_INFO_CONTENT_KEY, (Serializable)mRegInfo);
                    setResult(RESULT_OK, it);
                    finish();
                }else{
                    toast(ConstantImpl.getRegErrText(isOK, false));
                    startGetCode();
                }
            }
		}
			break;

		case R.id.imgRegCode:{//更换验证码
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
	
	public void startGetCode(){
		//检测网络是否已打开
		if (NetworkUtils.getNetworkState(this) == NetworkUtils.NONE){
			toast(R.string.dlg_network_check_tip);
		}else{
			mTipDlg.show();
			((ImageView) findViewById(R.id.imgRegCode)).setImageBitmap(null);
			new Thread(new Runnable() {				
				@Override
				public void run() {
					String strDate = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());
					final String pathName = (UserRegisterUI.this.getCacheDir().getAbsolutePath()+"/").replaceAll("//", "/")+"RegImg#" + strDate + ".jpg";
					final File f = new File(pathName);
					if(f!= null) f.deleteOnExit();//程序退出后删除
					final int isOK = LibImpl.getInstance().getFuncLib().GetRegImgAgent(pathName);
						runOnUiThread(new Runnable() {//update code						
							@Override
							public void run() {
								mTipDlg.dismiss();
								if(isOK == SDK_CONSTANT.reg_error_null){
									ImageView img = (ImageView) findViewById(R.id.imgRegCode);
									Bitmap bm = BitmapFactory.decodeFile(pathName);
									img.setImageBitmap(bm);
									Log.i("UserRegister", "fileInfo:filePath="+pathName+",fileSize="+(f==null ? -1 : f.length())+"byte");								
								}else{
									toast(R.string.reg_get_code_error);
								}			
							}
						});
				}
			}).start();
		}
	}
	
	public String gStrRemoveColon(Object msg){
		String _msg = "";
		if(msg instanceof Integer){
			_msg = T((Integer) msg);
		}else if(msg instanceof String){
			_msg = (String)msg;
		}
		if(_msg != null){
			_msg = _msg.replaceAll(":", "").replaceAll("：", "").replaceAll("　", "");
		}else{
			_msg = "";
		}
		return _msg;
	}
	public String gStrNullError(Object msg, Object error){
		String _msg = "";
		if(error instanceof Integer){
			_msg = T((Integer) error);
		}else if(error instanceof String){
			_msg = (String)error;
		}
		_msg = gStrRemoveColon(msg).concat(_msg);
		return _msg;
	}
	public String gStrNullError(Object msg){
		return gStrNullError(msg, R.string.can_not_null);
	}
	
	public boolean getFormData(){
		boolean isOK = false;
		// 非空检测
		if(isNullStr(gStr(R.id.etUserName))){
			toast(gStrNullError(gStr2(R.id.tvUserName)));
		}else if(isNullStr(gStr(R.id.etUserPwd))){
            toast(gStrNullError(gStr2(R.id.tvUserPwd)));
		}else if(isNullStr(gStr(R.id.etConfigPwd))){
            toast(gStrNullError(gStr2(R.id.tvConfigPwd)));
		}else if(isNullStr(gStr(R.id.etRegCode))){
            toast(gStrNullError(gStr2(R.id.tvRegCode)));
		}else{
			//数据合法性检测
			if(!DataCheckUtil.isRightUserName(gStr(R.id.etUserName))){
                toast(R.string.reg_illegal_user);
			}else if(!DataCheckUtil.isRightUserPwd(gStr(R.id.etUserPwd))){
                toast(R.string.reg_illegal_pwd);
			//邮箱和手机号可以不填，要填必须填对
			}else if(!isNullStr(gStr(R.id.etEmail)) && !DataCheckUtil.isRightEmail(gStr(R.id.etEmail))){
                toast(R.string.reg_illegal_email);
			}else if(!isNullStr(gStr(R.id.etPhone)) && !DataCheckUtil.isRightPhone(gStr(R.id.etPhone))){
                toast(R.string.reg_illegal_phone);
			}else{
				//两次密码是否一致
				if(gStr(R.id.etUserPwd).compareToIgnoreCase(gStr(R.id.etConfigPwd)) != 0){
                    toast(R.string.reg_two_pwd_inconsistent);
				}else{
					if(mRegInfo == null) mRegInfo = new RegisterInfo();
					mRegInfo.userName = gStr(R.id.etUserName);
					mRegInfo.userPwd = gStr(R.id.etUserPwd);
					mRegInfo.configPwd = gStr(R.id.etConfigPwd);
					mRegInfo.email = gStr(R.id.etEmail);
					mRegInfo.phone = gStr(R.id.etPhone);
					mRegInfo.code = gStr(R.id.etRegCode);
					isOK = true;
				}
			}
		}
		return isOK;
	}
}

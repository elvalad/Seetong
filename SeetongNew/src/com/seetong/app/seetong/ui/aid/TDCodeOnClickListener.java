package com.seetong.app.seetong.ui.aid;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.zxing.ui.CaptureTDCodeUI;
import com.custom.etc.EtcInfo;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.ui.BaseActivity;
import ipc.android.sdk.impl.DeviceInfo;

/**
 * @declaration 二维码事件监听及处理类<br>
 * @author qinglei.yin@192.168.88.9<br>
 *2014-2-27 下午5:42:58<br>
 */
public class TDCodeOnClickListener {
	public static interface TDCodeInterface{
		public void handleData(String codeText);
	}
	
	public static final int TD_CODE_REQ_ID = 0x1FFF;
	private final String CODE_HEAD_STRING = "p2p://";
	private TDCodeInterface mCodeInterface;
	
	public TDCodeOnClickListener(TDCodeInterface td) {
		mCodeInterface = td;
	}

	public void tdCodeRecv(int requestCode, int resultCode, Intent data) {
		tdCodeRecv(requestCode, resultCode, data, null);
	}
	public void tdCodeRecv(int requestCode, int resultCode, Intent data, Object obj){
		if(requestCode == TD_CODE_REQ_ID && resultCode == Activity.RESULT_OK){
			String codeText = data.getStringExtra(CaptureTDCodeUI.TD_CODE_RESULT_KEY);
            if(mCodeInterface != null) mCodeInterface.handleData(codeText);
		}
	}
	
	/**
	 * 检测二维码的合法性<br>
	 * 样品如：p2p://admin:123456@100111.s1.seetong.com<br>
	 * @param codeText
	 * @return
	 */
	public boolean isRightCode(String codeText){
		boolean isOK = false;
		if(!BaseActivity.isNullStr(codeText)){
			isOK = codeText.contains(CODE_HEAD_STRING) && codeText.contains("@");
		}
		return isOK;
	}
	
	/**
	 * 解析二维码数据
	 * @param codeText
	 * @return
	 */
	public DeviceInfo getDevInfoByCode(String codeText){
		DeviceInfo devInfo = null;
		if(isRightCode(codeText)){
			String code = codeText.trim().replaceAll(CODE_HEAD_STRING, "");
			String[] ary = code.split("@");
			if(ary != null && ary.length == 2){
				String nameOrpwd = ary[0];
				if(nameOrpwd.split(":") != null && nameOrpwd.split(":").length == 2){
					String idOrser = ary[1];
					int atPos = idOrser.indexOf(".");
					if(atPos > 0){
						devInfo = new DeviceInfo();
						devInfo.setUserName(nameOrpwd.split(":")[0]);
						devInfo.setUserPassword(nameOrpwd.split(":")[1]);
						
						devInfo.setDevId(idOrser.substring(0, atPos));
						devInfo.setDevIP(idOrser.substring(atPos+1));
						devInfo.setDevPort(EtcInfo.DEFAULT_SERVER_PORT);
					}//atPos
				}//nameOrpwd
			}//length=2
		}
		return devInfo;
	}
}

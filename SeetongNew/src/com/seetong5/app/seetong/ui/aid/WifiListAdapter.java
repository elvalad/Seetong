package com.seetong5.app.seetong.ui.aid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.utils.NetworkUtils;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.BaseActivity;
import com.seetong5.app.seetong.ui.MainActivity;
import com.seetong5.app.seetong.ui.WifiEtcUI;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import com.seetong5.app.seetong.wifi.AccessPoint;
import ipc.android.sdk.com.Device;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @declaration 单级设备列表项适配器
 * @author qinglei.yin@192.168.88.9<br>
 *2013-12-9 下午3:25:50<br>
 */
public class WifiListAdapter extends BaseAdapter implements OnItemClickListener,OnClickListener {
	public static class WifiFlagInfo{
		public String mStateText;
		public boolean mIsConnected;
		public SupplicantState mSupplicantState;
	}
	public class ViewHolder {
		public TextView tvWifiName;
		public TextView tvWifiEncrypted;
		public ImageView imgWifiIno;
		public ImageView imgWifiState;
	}
	
	private BaseActivity mActivity;
	private List<ScanResult> mTitleAry = new ArrayList<ScanResult>();
	public HashMap<String, WifiFlagInfo> mWifHashMap = new HashMap<String, WifiFlagInfo>();
	private LayoutInflater mInflater;
	private int[] mWifiLockRes = {R.drawable.ic_wifi_lock_signal_1, R.drawable.ic_wifi_lock_signal_2, R.drawable.ic_wifi_lock_signal_3, R.drawable.ic_wifi_lock_signal_4};
	private int[] mWifiUnlockRes = {R.drawable.ic_wifi_signal_1, R.drawable.ic_wifi_signal_2, R.drawable.ic_wifi_signal_3, R.drawable.ic_wifi_signal_4};

	public WifiListAdapter(BaseActivity context, List<ScanResult> datas) {
		mActivity = context;
		mInflater = LayoutInflater.from(mActivity);
		if((datas != null) && (datas.size() > 0)){
			mTitleAry = datas;
			if(mWifHashMap == null) mWifHashMap = new HashMap<String, WifiFlagInfo>();
			for(int i = 0; i < datas.size(); i++){
				mWifHashMap.put(datas.get(i).SSID, new WifiFlagInfo());
			}
		}
	}

	@Override
	public int getCount() {
		int count = (mTitleAry == null) ? 0 : mTitleAry.size();
		return count;
	}

	@Override
	public Object getItem(int position) {
		Object obj = mTitleAry.get(position);
		return obj;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int pos, View v, ViewGroup parent) {
		ViewHolder viewHolder;
		if (v == null) {
			v = mInflater.inflate(R.layout.wifi_etc_item_1, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.tvWifiName = (TextView) v.findViewById(R.id.tvWifiName);
			viewHolder.tvWifiEncrypted = (TextView) v.findViewById(R.id.tvWifiEncrypted);
			viewHolder.imgWifiIno = (ImageView) v.findViewById(R.id.ivWifiInfo);
			viewHolder.imgWifiState = (ImageView) v.findViewById(R.id.ivWifiState);
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}
		
		viewHolder.tvWifiName.setTextColor(Color.BLACK);
		ScanResult dev = mTitleAry.get(pos);
		if(dev != null){
			AccessPoint ap = new AccessPoint(mActivity, dev);
			String secType = ap.getSecurityString(true);
			boolean isNoSec = secType == null || "".equals(secType);
			viewHolder.tvWifiName.setText(dev.SSID);
//			viewHolder.tvWifiEncrypted.setText(isNoSec ? "未加密" : "加密类型:"+secType);
			viewHolder.tvWifiEncrypted.setText(isNoSec ? "" : mActivity.getResources().getString(R.string.tip_wifi_encryp_info_text, secType));
			viewHolder.imgWifiIno.setOnClickListener(this);
			viewHolder.imgWifiIno.setTag(pos+"");
			
			int level = ap.getLevel();
			level = (level >= 4 || level < 0) ? 0 : level;
			viewHolder.imgWifiState.setImageResource(isNoSec ? mWifiUnlockRes[level] : mWifiLockRes[level]);
			
			WifiFlagInfo wifiInfo = mWifHashMap.get(dev.SSID);
			if(wifiInfo != null){
				int activeColor = Color.parseColor("#008214");
				int color = wifiInfo.mIsConnected ? activeColor/*Color.RED*/ : Color.BLACK;
				viewHolder.tvWifiName.setTextColor(color);
				String _text = wifiInfo.mStateText;
				_text = (_text == null || "".equals(_text)) ?  (isNoSec ? "" : mActivity.getResources().getString(R.string.tip_wifi_encryp_info_text, secType)) : _text;
				viewHolder.tvWifiEncrypted.setText(_text);
			}
		}
		
		int totle_Num = getCount();
		int current_Id = pos;
		v.setFocusable(false);
		// 只有一项
		if (totle_Num == 1) {
			v.setBackgroundResource(R.drawable.default_selector);
			return v;
		}
		// 第一项
		else if (current_Id == 0) {
			v.setBackgroundResource(R.drawable.list_top_selector);
		}
		// 最后一项
		else if (current_Id == totle_Num - 1) {
			v.setBackgroundResource(R.drawable.list_bottom_selector);
		} else {
			v.setBackgroundResource(R.drawable.list_center_selector);
		}
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		final ScanResult dev = mTitleAry.get(pos);
		if(dev != null){
			WifiFlagInfo wifiInfo = mWifHashMap.get(dev.SSID);
			if(wifiInfo != null && wifiInfo.mSupplicantState == SupplicantState.COMPLETED){
				final String _ssid = dev.SSID;
				if(WifiListAdapter.isRightSSID(_ssid)){
					((WifiEtcUI)mActivity).autoSwitchNextStep();
                }else{
                	AlertDialog mAlertDialog = MyTipDialog.getMyDialog(mActivity,
                            mActivity.T(R.string.dlg_tip),
                            mActivity.mResources.getString(R.string.tip_wifi_connect_success_info, _ssid),
                            mActivity.T(R.string.sure),
                            mActivity.T(R.string.cancel),
                            new MyTipDialog.IDialogMethod() {
                                @Override
                                public void sure() {
                                    ((WifiEtcUI) mActivity).autoSwitchNextStep();
                                }
                            });
                	mAlertDialog.show();
                }
			}else{
				final AccessPoint ap = new AccessPoint(mActivity, dev);
				if(isRightSSID(dev.SSID)){
                    String pwd = getDefaultWifiPwd(dev);
					((WifiEtcUI)mActivity).connectDeviceAp(ap, dev.SSID, pwd);
				}else{
					MyTipDialog.popDialog(mActivity,
							mActivity.T(R.string.dlg_tip),
                			mActivity.mResources.getString(R.string.tip_wifi_try_connect_info, dev.SSID),
                			mActivity.T(R.string.sure),
                			mActivity.T(R.string.cancel),
							new MyTipDialog.IDialogMethod() {
						@Override
						public void sure() {
							((WifiEtcUI)mActivity).connectAp(ap, dev.SSID, "88776655");
						}
					});
				}
			}
		}else{
			Log.w("MSG", "此Wifi节点信息有误...");
		}
	}
	
	public static boolean isRightSSID(String ssid){
		boolean isOK = false;
		if(BaseActivity.isNullStr(ssid)) return false;
        String pstr = "^([0-9]{6,32})|(camera_([A-Za-z0-9]{6,32}))$"; //6-32位的云ID或camera_xxxxxxx
        Pattern p = Pattern.compile(pstr, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(ssid);
        isOK = m.find();
        if (!isOK) {
            pstr = "^([0-9]{6,32})|(Furbo_([A-Za-z0-9]{6,32}))$"; //6-32位的云ID或camera_xxxxxxx
            p = Pattern.compile(pstr, Pattern.CASE_INSENSITIVE);
            m = p.matcher(ssid);
            isOK = m.find();
        }

        return isOK;
	}

    public static String getDefaultWifiPwd(ScanResult dev) {
		String ssid = dev.SSID;
		if (ssid.indexOf('_') >= 0) ssid = ssid.substring(ssid.indexOf('_') + 1);
		if (ssid.length() >= 8) return ssid.substring(ssid.length() - 8);
		String pwd = "00000000";
		pwd = pwd.substring(ssid.length()) + ssid;
		return pwd;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivWifiInfo:{
//			final int pos = Integer.parseInt((String) v.getTag());
//			ScanResult dev = mTitleAry.get(pos);
//			((WifiEtcUI)mActivity).initUIByStep(WifiEtcUI.STEP_WIFI_INFO);
		}
			break;
			
		default:
			break;
		}
	}
	
	public boolean addDeviceToLive(BaseActivity activity, Device dev){
		//检测网络是否已打开
		if (NetworkUtils.getNetworkState(activity) == NetworkUtils.NONE){
			activity.toast(R.string.dlg_network_check_tip);
			return false;
		}
		boolean isOK = false;
		if(dev != null){
			int windownNO = LibImpl.getInstance().getIndexByDeviceID(dev.getDevId());
			if(windownNO < 0){//dev.getVideoLiveState() != FunclibAgent.VIDEO_STATE_PLAYING
				if(!SDK_CONSTANT.IS_CHECK_DEVICE_ONLINE || dev.getOnLine() == 2){
					Intent it = new Intent(activity, MainActivity.class);
					it.putExtra(MainActivity.DEVICE_ID_KEY, dev.getDevId());
					
					int AddLiveID = activity.getIntent().getIntExtra(MainActivity.ADD_LIVE_KEY, 0);
					if(AddLiveID == MainActivity.ADD_LIVE_ID){
						activity.setResult(Activity.RESULT_OK, it);
					}else{
						activity.startActivity(it);
					}
					activity.finish();
				}else{
					activity.toast(R.string.dlg_device_offline_tip);
				}
			}else{
				String _tip = activity.getResources().getString(R.string.dlg_device_id_live_exist_tip, windownNO+1);
				activity.toast(_tip);
			}
			isOK = true;
		}
		return isOK;
	}

    public void resetContent() {
        mTitleAry.clear();
        mWifHashMap.clear();
        notifyDataSetChanged();
    }
}
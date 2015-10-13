package com.seetong5.app.seetong.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.MonitorCore;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.aid.ClearEditText;
import com.seetong5.app.seetong.ui.aid.MultiDevListAdapter;
import com.seetong5.app.seetong.ui.aid.SingleDevListAdapter;
import ipc.android.sdk.com.*;
import ipc.android.sdk.impl.DeviceInfo;
import ipc.android.sdk.impl.FunclibAgent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014-05-12.
 */
public class DeviceFragment extends BaseFragment implements View.OnClickListener {
    private View m_view;
    private ImageView m_img_wait;
    private ExpandableListView m_listView;
    private MultiDevListAdapter m_adapter;
    private Thread m_thread = new SearchDevThread();
    private ProgressDialog m_pdlg;
    public static int m_lastPos = 0;
    private EditText metSearchByID;
    private ProgressDialog mTipDlg;
    private DeviceInfo m_modifyInfo;

    AbsListView.OnScrollListener m_scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                m_lastPos = m_listView.getFirstVisiblePosition();  //ListPos记录当前可见的List顶端的一行的位置
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.m_this.setDeviceFragment(this);
        m_view = inflater.inflate(R.layout.device_list_ui, container);

        Animation operatingAnim = AnimationUtils.loadAnimation(MainActivity.m_this, R.anim.wait_tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        m_img_wait = (ImageView) m_view.findViewById(R.id.img_wait);
        m_img_wait.startAnimation(operatingAnim);

        m_adapter = new MultiDevListAdapter(MainActivity.m_this);
        m_listView = (ExpandableListView) m_view.findViewById(R.id.lv_device_list);
        m_listView.setGroupIndicator(null);
        m_listView.setAdapter(m_adapter);

        m_listView.setDivider(new ColorDrawable(0xffa7a4a4/*R.color.list_seperator_gray*/));
        m_listView.setDividerHeight(1);
        m_listView.setChildIndicator(null);
        m_listView.setOnScrollListener(m_scrollListener);

        lvResult = (ListView) m_view.findViewById(R.id.lvDeviceSearchList);

        m_view.findViewById(R.id.btn_search_device).setOnClickListener(this);
        m_view.findViewById(R.id.btn_scan_qrcode).setOnClickListener(this);
        m_view.findViewById(R.id.btn_add_device).setOnClickListener(this);

        m_pdlg = new ProgressDialog(this.getActivity());

        mTipDlg = new ProgressDialog(this.getActivity(), R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);

        metSearchByID = (EditText) m_view.findViewById(R.id.etSearchDevice);
        metSearchByID.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTextChanged(s, start, before, count);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button btnAdd = (Button) m_view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        if (Global.m_loginType == Define.LOGIN_TYPE_DEMO) btnAdd.setVisibility(View.GONE);

        return m_view;
    }

    public void initView() {
        m_img_wait.clearAnimation();
        m_img_wait.setVisibility(View.GONE);
        m_listView.setVisibility(View.VISIBLE);

        m_adapter.setData(Global.getSelfDeviceList());
        m_adapter.notifyDataSetChanged();
        m_listView.expandGroup(0);

        //将所有项设置成默认展开
        int groupCount = m_adapter.getGroupCount();
        /*for (int i = 0; i < groupCount && SDK_CONSTANT.IS_DEVICE_LIST_EXPAND; i++) {
            m_listView.expandGroup(i);
        }*/

        int group_pos = (int) MultiDevListAdapter.m_sel_group_pos;
        int child_pos = (int) MultiDevListAdapter.m_sel_child_pos;
        if (group_pos >= 0  && group_pos < groupCount && child_pos >= 0) {
            int childCount = m_adapter.getChildrenCount(group_pos);
            if (child_pos < childCount) m_listView.setSelectedChild(group_pos, child_pos, true);
        }

        if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {
            String devId = getActivity().getIntent().getStringExtra(Constant.DEVICE_INFO_KEY);
            DeviceInfo devInfo = Global.getDeviceInfoById(devId);
            if (null == devInfo) devInfo = Global.m_devInfo;
            PromptModifyPassword(devInfo);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_search_device:
                onSearchDevice();
                break;
            case R.id.btn_scan_qrcode:
                onScanQRCode();
                break;
            case R.id.btn_add_device:
                onAddDevice();
            case R.id.btnAdd:
                onBtnAddDevice();
                break;
        }
    }

    private void onBtnAddDevice() {
        if (Global.m_loginType == Define.LOGIN_TYPE_DEVICE) {//device
            Intent it = new Intent(DeviceFragment.this.getActivity(), AddDeviceUI.class);
            it.putExtra(Constant.ENTER_TYPES, 1);
            startActivityForResult(it, Constant.ADD_DEVICE_REQ_ID);
        } else if (Global.m_loginType == Define.LOGIN_TYPE_USER) {//user
            Intent it = new Intent(DeviceFragment.this.getActivity(), AddDeviceUI.class);
            it.putExtra(Constant.ENTER_TYPES, 1);
            //startActivityForResult(it, DeviceListUI.ADD_ONLINE_DEVICE_REQ_ID);
            startActivityForResult(it, Constant.ADD_DEVICE_REQ_ID);
        }
    }

    private class SearchDevThread extends Thread {
        @Override
        public void run() {
            try {
                MonitorCore.instance().StartSearchDev();
                Thread.sleep(30000);
                MonitorCore.instance().StopSearchDev();
                m_pdlg.dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void onSearchDevice() {
        if (m_thread.isAlive()) return;
        m_pdlg.setTitle(R.string.pdlg_search_device);
        m_pdlg.show();
        m_thread = new SearchDevThread();
        m_thread.start();
    }

    private void onScanQRCode() {
        Intent it = new Intent(this.getActivity(), ScanQRCode.class);
        this.startActivityForResult(it, ScanQRCode.TD_CODE_REQ_ID);
    }

    private void onAddDevice() {
        Intent it = new Intent(this.getActivity(), AddDevice.class);
        this.startActivity(it);
    }

    public boolean delDevice(final List<PlayerDevice> devs) {
        if (null == devs || devs.isEmpty()) return false;
        if (mTipDlg.isShowing()) return true;

        mTipDlg.setTitle(R.string.pdlg_delete_device);
        mTipDlg.setTimeoutToast(T(R.string.timeout_retry));
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                List<PlayerDevice> devAry = Global.getSelfDeviceList();
                m_adapter.myNotifyDataSetChanged(devAry, m_listView);
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        mTipDlg.show(20000);
        for (PlayerDevice dev : devs) {
            MainActivity.m_this.getVideoFragment().stopAndResetPlay(dev);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String name = "";
                for (PlayerDevice dev : devs) {
                    if (!dev.isNVR()) {
                        final int ret = LibImpl.getInstance().delDevice(dev);
                        if (0 != ret) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String err = ConstantImpl.getDelDeviceErrText(ret);
                                    toast(err + " " + T(R.string.operation_failed_retry));
                                    mTipDlg.dismiss();
                                }
                            });

                            break;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                List<PlayerDevice> devAry = Global.getSelfDeviceList();
                                m_adapter.myNotifyDataSetChanged(devAry, m_listView);
                                mTipDlg.dismiss();
                            }
                        });

                        continue;
                    }

                    name = dev.m_dev.getDevName();
                }

                if (!"".equals(name)) {
                    final int ret = LibImpl.getInstance().delNVR(name);
                    if (0 != ret) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String err = ConstantImpl.getDelDeviceErrText(ret);
                                toast(err + " " + T(R.string.operation_failed_retry));
                                mTipDlg.dismiss();
                            }
                        });

                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<PlayerDevice> devAry = Global.getSelfDeviceList();
                            m_adapter.myNotifyDataSetChanged(devAry, m_listView);
                            mTipDlg.dismiss();
                        }
                    });
                }
            }
        }).start();

        return true;
    }

    public void notifyDataSetChanged() {
        m_adapter.notifyDataSetChanged();
    }

    ListView lvResult;
    SingleDevListAdapter singleAdapter;
    List<PlayerDevice> datas;

    public void searchTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() > 0) {
            if (Global.getSelfDeviceList().size() < 1) return;
            datas = new ArrayList<PlayerDevice>();
            for (PlayerDevice dev : Global.getSelfDeviceList()) {
                String devID = dev.m_dev.getDevId();
                String devAlias = LibImpl.getInstance().getDeviceAlias(dev.m_dev);
                if (devID != null && (devID.contains(s) || devAlias.contains(s))) {//匹配设备ID和别名
                    datas.add(dev);
                }
            }

            m_view.findViewById(R.id.lvDeviceSearchList).setVisibility(View.VISIBLE);
            m_view.findViewById(R.id.lv_device_list).setVisibility(View.GONE);
            singleAdapter = new SingleDevListAdapter(datas);
            //lvResult.setOnItemClickListener(singleAdapter);
            lvResult.setAdapter(singleAdapter);
            singleAdapter.notifyDataSetChanged();
        } else {
            if (datas != null) datas.clear();
            singleAdapter = null;
            m_view.findViewById(R.id.lvDeviceSearchList).setVisibility(View.GONE);
            m_view.findViewById(R.id.lv_device_list).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case ScanQRCode.TD_CODE_REQ_ID:
                String codeText = data.getStringExtra(ScanQRCode.TD_CODE_RESULT_KEY);
                break;
            case Constant.EDIT_DEVICE_REQ_ID:
                List<PlayerDevice> devAry = Global.getSelfDeviceList();
                m_adapter.myNotifyDataSetChanged(devAry, m_listView);
                break;
            case Constant.ADD_DEVICE_REQ_ID:
                // update the search list
                String searchText = metSearchByID.getText().toString();
                if (!TextUtils.isEmpty(searchText)) {
                    metSearchByID.setText("");
                    metSearchByID.setText(searchText);
                }

                final String devId = data.getStringExtra(Constant.DEVICE_INFO_KEY);
                String xml = data.getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
                MainActivity.m_this.onNotifyDevData(xml, new MainActivity.ParseDevListResult() {
                    @Override
                    public void onResult(List<PlayerDevice> devices) {
                        MainActivity.m_this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initView();
                                DeviceInfo devInfo = Global.getDeviceInfoById(devId);
                                PromptModifyPassword(devInfo);
                                // notify auto change media parameter
                                //LibImpl.getInstance().notifyChangeDefaultPwdThreadStart();
                            }
                        });
                    }
                });

                break;
            //TODO
            /*case VideoUI.ADD_LIVE_ID:
                devAry = Global.m_deviceList;
                if (devAry == null) {
                    devAry = new ArrayList<PlayerDevice>();
                    toast(T(R.string.dlg_get_list_fail_tip));
                }
                m_adapter.myNotifyDataSetChanged(devAry, m_listView);
                break;*/
            case Constant.ADD_ONLINE_DEVICE_REQ_ID:
                toast(R.string.ad_error_null);
                devAry = Global.getSelfDeviceList();

                // update the search list
                searchText = metSearchByID.getText().toString();
                if (!TextUtils.isEmpty(searchText)) {
                    metSearchByID.setText("");
                    metSearchByID.setText(searchText);
                }

                m_adapter.myNotifyDataSetChanged(devAry, m_listView);
                break;
            case Constant.REQ_ID_DEVICE_CONFIG:
                onDeviceConfigResult(data);
                break;
            default: break;
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Define.MSG_SHOW_TOAST:
                toast(msg.arg1);
                break;
            case Define.MSG_SEARCH_DEVICE_RESP:
                notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                notifyDataSetChanged();
                break;
            case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                notifyDataSetChanged();
                break;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG:
                LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                List<NetSDK_UserAccount> lst = (List<NetSDK_UserAccount>) msgObj.recvObj;
                onGetUserConfig(msg.arg1, msgObj.devID, lst);
                return true;
            case NetSDK_CMD_TYPE.CMD_SET_SYSTEM_USER_CONFIG:
                onSetUserConfig(msg.arg1);
                return true;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED:
                onLoginFailed((PlayerDevice)msg.obj);
                return true;
        }

        return false;
    }

    public void onDeviceConfig(Device dev) {
        if (dev.getOnLine() == Device.OFFLINE) {
            toast(R.string.dlg_device_offline_tip);
            return;
        }

        if (Global.m_loginType == Define.LOGIN_TYPE_DEMO) {
            toast(R.string.demo_not_support_limit);
            return;
        }

        Intent it = new Intent(this.getActivity(), DeviceConfigUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, dev.getDevId());
        this.startActivityForResult(it, Constant.REQ_ID_DEVICE_CONFIG);
    }

    private void onDeviceConfigResult(Intent data) {
        String devId = data.getStringExtra(Constant.EXTRA_DEVICE_ID);
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;
        int type = data.getIntExtra(Constant.EXTRA_DEVICE_CONFIG_TYPE, 0);
        switch (type) {
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_ALIAS:
                String alias = data.getStringExtra(Constant.EXTRA_MODIFY_DEVICE_ALIAS_NAME);
                m_adapter.updateDeviceAlias(devId, alias);
                break;
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_USER_PWD:
                modifyUserPwd(dev);
                break;
            case Constant.DEVICE_CONFIG_ITEM_MODIFY_MEDIA_PARAM:
                modifyMediaParameter(dev);
                break;
        }
    }

    public void modifyMediaParameter(PlayerDevice dev) {
        if (null == dev) return;
        Intent it = new Intent(this.getActivity(), MediaParamUI.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, dev.m_dev.getDevId());
        this.startActivity(it);
    }

    private boolean m_modifyDefaultPassword = false;
    private void PromptModifyPassword(DeviceInfo devInfo) {
        /*if (null == devInfo) return;
        String deviceId = devInfo.getDevId();
        if (Global.m_loginType != Define.LOGIN_TYPE_DEVICE) return;
        PlayerDevice dev = Global.getDeviceById(deviceId);
        if (null == dev || dev.m_dev.getOnLine() == Device.OFFLINE) return;
        if (!"admin".equals(devInfo.getUserName()) || !"123456".equals(devInfo.getUserPassword())) {
            m_modifyDefaultPassword = false;
            return;
        }

        // 用户选择了下次不再提示
        if (Config.m_not_prompt_modify_password) return;
        // 当前为向导界面，直接返回
        if (!Global.m_guide_finished) return;
        m_modifyDefaultPassword = true;
        modifyUserPwd(Global.getDeviceById(devInfo.getDevId()));*/
    }

    private PlayerDevice m_modifyUserPwdDev = null;

    public void modifyUserPwd(final PlayerDevice dev) {
        if (null == dev) return;
        m_modifyUserPwdDev = dev;
        showTipDlg(R.string.dlg_get_user_list_tip, 20000, R.string.dlg_get_user_list_timeout_tip);
        int ret = LibImpl.getInstance().getFuncLib().GetP2PDevConfig(dev.m_dev.getDevId(), NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG);
        if (0 == ret) return;
        toast(R.string.dlg_get_user_list_fail_tip);
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                m_modifyInfo = null;
                m_modifyDefaultPassword = false;
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });
        mTipDlg.show(timeout);
    }

    /**
     * 设置用户列表响应
     * @param flag 设置是否成功
     */
    private void onSetUserConfig(int flag) {
        //mTipDlg.dismiss();
        if (0 != flag) {
            m_modifyInfo = null;
            toast(R.string.dlg_set_user_info_fail_tip);
            return;
        }

        final PlayerDevice dev = m_modifyUserPwdDev;
        if (null == dev) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 已经设置新的用户信息，再次发送获取请求并验证
                int ret = FunclibAgent.getInstance().GetP2PDevConfig(dev.m_dev.getDevId(), NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG);
                if (0 != ret) {
                    MainActivity.m_this.sendMessage(Define.MSG_SHOW_TOAST, R.string.dlg_set_user_info_fail_tip, 0, null);
                }
            }
        }).start();
    }

    public void modifyDeviceDefaultPassword(String devId, List<NetSDK_UserAccount> obj) {
        m_modifyDefaultPassword = true;
        PlayerDevice dev = Global.getDeviceById(devId);
        modifyUserPwd(dev);
    }

    private AlertDialog m_modify_user_pwd_dlg = null;

    /**
     * 获取设备用户列表并修改用户名和密码
     * @param flag 获取是否成功
     * @param obj 设备的用户列表
     */
    private void onGetUserConfig(int flag, String devId, List<NetSDK_UserAccount> obj) {
        final List<NetSDK_UserAccount> lstUser = obj;
        final PlayerDevice dev = m_modifyUserPwdDev;
        if (null == dev) return;
        mTipDlg.dismiss();
        if (null != m_modify_user_pwd_dlg && m_modify_user_pwd_dlg.isShowing()) return;
        if (0 != flag || lstUser.isEmpty()) {
            toast(R.string.dlg_get_user_list_fail_tip);
            return;
        }

        // 已经设置新的用户信息，再次获取并验证
        if (null != m_modifyInfo) {
            m_modifyDefaultPassword = false;
            boolean found = false;
            for(NetSDK_UserAccount u : lstUser) {
                if (!u.getUserName().equals(m_modifyInfo.getUserName()) || !u.getPassword().equals(m_modifyInfo.getUserPassword())) continue;
                found = true;
                break;
            }

            final DeviceInfo info = m_modifyInfo;
            m_modifyInfo = null;
            if (!found) {
                toast(R.string.dlg_set_user_info_fail_tip);
                return;
            }

            m_modifyUserPwdDev.m_dev.setLoginName(info.getUserName());
            m_modifyUserPwdDev.m_dev.setLoginPassword(info.getUserPassword());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 验证设置成功，调用更新函数通知云平台同步修改
                    int ret = FunclibAgent.getInstance().ModifyDevPassword(dev.m_dev.getDevId(), info.getUserName(), info.getUserPassword());

                    // 获取视频参数，取消修改默认密码字幕
                    LibImpl.m_change_default_pwd_dev = dev;
                    FunclibAgent.getInstance().GetP2PDevConfig(dev.m_devId, 501);

                    if (0 != ret) {
                        MainActivity.m_this.sendMessage(Define.MSG_SHOW_TOAST, R.string.dlg_set_user_info_fail_tip, 0, null);
                        return;
                    }

                    Global.m_devInfo.setUserName(info.getUserName());
                    Global.m_devInfo.setUserPassword(info.getUserPassword());
                    MainActivity.m_this.sendMessage(Define.MSG_SHOW_TOAST, R.string.dlg_set_user_info_succeed_tip, 0, null);
                }
            }).start();

            return;
        }

        final ClearEditText etUser = new ClearEditText(this.getActivity());
        etUser.setHint(R.string.dev_list_hint_input_user_name);
        etUser.setPadding(10, 10, 10, 10);
        etUser.setSingleLine(true);
        etUser.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_FILTER|EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etUser.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});
        if (m_modifyDefaultPassword) etUser.setText("admin");

        final ClearEditText etPwd = new ClearEditText(this.getActivity());
        etPwd.setHint(R.string.dev_list_hint_input_password);
        etPwd.setPadding(10, 10, 10, 10);
        etPwd.setSingleLine(true);
        //etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etPwd.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        etPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_PWD_LENGTH)});
        if (m_modifyDefaultPassword) etPwd.setText("123456");

        final ClearEditText etNewUser = new ClearEditText(this.getActivity());
        etNewUser.setHint(R.string.dev_list_hint_input_new_user_name);
        etNewUser.setPadding(10, 10, 10, 10);
        etNewUser.setSingleLine(true);
        etNewUser.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        etNewUser.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});

        final ClearEditText etNewPwd = new ClearEditText(this.getActivity());
        etNewPwd.setHint(R.string.dev_list_hint_input_new_password);
        etNewPwd.setPadding(10, 10, 10, 10);
        etNewPwd.setSingleLine(true);
        etNewPwd.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_PWD_LENGTH)});

        final ClearEditText etNewPwd2 = new ClearEditText(this.getActivity());
        etNewPwd2.setHint(R.string.dev_list_hint_input_new_password_2);
        etNewPwd2.setPadding(10, 10, 10, 10);
        etNewPwd2.setSingleLine(true);
        etNewPwd2.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPwd2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_PWD_LENGTH)});

        final CheckBox cbxPrompt = new CheckBox(this.getActivity());
        cbxPrompt.setText(R.string.dev_list_tip_title_next_not_prompt);

        LinearLayout layout = new LinearLayout(this.getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 0, 5, 0);
        layout.setBackgroundColor(Color.rgb(207, 232, 179));
        layout.addView(etUser);
        layout.addView(etPwd);
        layout.addView(etNewUser);
        layout.addView(etNewPwd);
        layout.addView(etNewPwd2);
        if (m_modifyDefaultPassword) layout.addView(cbxPrompt);
        int titleId = m_modifyDefaultPassword ? R.string.dev_list_tip_title_modify_default_user_pwd : R.string.dev_list_tip_title_modify_user_pwd;

        m_modify_user_pwd_dlg = new AlertDialog.Builder(this.getActivity()).setTitle(titleId)
                .setView(layout)
                .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        if (m_modifyDefaultPassword) {
                            Config.m_not_prompt_modify_password = cbxPrompt.isChecked();
                        }

                        m_modifyDefaultPassword = false;
                        MainActivity.m_this.hideInputPanel(etUser);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(this.getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userName = etUser.getText().toString();
                        String password = etPwd.getText().toString();
                        String newUser = etNewUser.getText().toString();
                        String newPwd = etNewPwd.getText().toString();
                        String newPwd2 = etNewPwd2.getText().toString();
                        if ("".equals(userName) || "".equals(password) || "".equals(newUser)) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            if ("".equals(userName)) {
                                etUser.setShakeAnimation();
                                return;
                            }

                            if ("".equals(password)) {
                                etPwd.setShakeAnimation();
                                return;
                            }

                            if ("".equals(newUser)) {
                                etNewUser.setShakeAnimation();
                                return;
                            }

                            return;
                        }

                        boolean found = false;
                        // 找到要修改的用户
                        NetSDK_UserAccount foundUser = null;
                        for (NetSDK_UserAccount u : lstUser) {
                            if (!u.getUserName().equals(userName) || !u.getPassword().equals(password)) continue;
                            foundUser = u;
                            found = true;
                            break;
                        }

                        if (!found) {
                            // 未找到，提示重新输入
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            toast(R.string.dlg_set_user_info_username_or_pwd_incorrect_tip);
                            return;
                        }

                        // 是否需要修改密码
                        if ("".equals(newPwd) || "".equals(newPwd2)) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            if ("".equals(newPwd)) {
                                etNewPwd.setShakeAnimation();
                                return;
                            }

                            if ("".equals(newPwd2)) {
                                etNewPwd2.setShakeAnimation();
                                return;
                            }

                            return;
                        }

                        if (password.equals(newPwd)) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            if ("".equals(newPwd)) {
                                etNewPwd.setShakeAnimation();
                                return;
                            }

                            toast(R.string.dlg_set_user_info_new_pwd_incorrect_tip);
                            return;
                        }

                        if (!newPwd.equals(newPwd2)) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            toast(R.string.dlg_set_user_info_confirm_pwd_incorrect_tip);
                            return;
                        }

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        if (m_modifyDefaultPassword) {
                            Config.m_not_prompt_modify_password = cbxPrompt.isChecked();
                        }

                        MainActivity.m_this.hideInputPanel(etUser);
                        dialog.dismiss();
                        showTipDlg(R.string.dlg_set_user_list_tip, 20000, R.string.dlg_set_user_info_timeout_tip);
                        m_modifyInfo = new DeviceInfo();
                        m_modifyInfo.setUserName(newUser);
                        m_modifyInfo.setUserPassword(newPwd);
                        foundUser.setUserName(newUser);
                        foundUser.setPassword(newPwd);
                        List<AbstractDataSerialBase> lst = new ArrayList<>();
                        lst.addAll(lstUser);
                        foundUser.addHead(false);
                        final String xml = foundUser.toXMLString(lst, "UserConfig");

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 设置设备用户信息
                                int ret = FunclibAgent.getInstance().SetP2PDevConfig(dev.m_dev.getDevId(), NetSDK_CMD_TYPE.CMD_SET_SYSTEM_USER_CONFIG, xml);
                                if (0 != ret) {
                                    MainActivity.m_this.sendMessage(Define.MSG_SHOW_TOAST, R.string.dlg_set_user_info_fail_tip, 0, null);
                                    return;
                                }
                            }
                        }).start();
                    }
                }).create();
        m_modify_user_pwd_dlg.show();
    }

    private AlertDialog m_input_user_pwd_dlg = null;
    // 登录设备失败，提示用户输入正确的用户名密码
    private void onLoginFailed(final PlayerDevice dev) {
        // 向导未结束，不弹出界面
        if (!Global.m_guide_finished) return;
        if (null != m_input_user_pwd_dlg && m_input_user_pwd_dlg.isShowing()) return;
        mTipDlg.dismiss();
        final ClearEditText etUser = new ClearEditText(this.getActivity());
        etUser.setHint(R.string.dev_list_hint_input_user_name);
        etUser.setPadding(10, 10, 10, 10);
        etUser.setSingleLine(true);
        etUser.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_FILTER|EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etUser.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});

        final ClearEditText etPwd = new ClearEditText(this.getActivity());
        etPwd.setHint(R.string.dev_list_hint_input_password);
        etPwd.setPadding(10, 10, 10, 10);
        etPwd.setSingleLine(true);
        etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etPwd.setInputType(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        etPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_PWD_LENGTH)});

        LinearLayout layout = new LinearLayout(this.getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 0, 5, 0);
        layout.setBackgroundColor(Color.rgb(207, 232, 179));
        layout.addView(etUser);
        layout.addView(etPwd);

        m_input_user_pwd_dlg = new AlertDialog.Builder(this.getActivity()).setTitle(R.string.dev_list_tip_title_input_user_pwd)
                .setView(layout)
                .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        MainActivity.m_this.hideInputPanel(etUser);
                        dialog.dismiss();
                        mTipDlg.dismiss();
                    }
                })
                .setPositiveButton(this.getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userName = etUser.getText().toString();
                        String password = etPwd.getText().toString();
                        if ("".equals(userName) || "".equals(password)) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            return;
                        }

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        MainActivity.m_this.hideInputPanel(etUser);
                        dialog.dismiss();

                        int ret = FunclibAgent.getInstance().ModifyDevPassword(dev.m_dev.getDevId(), userName, password);
                        if (0 != ret) {
                            toast(T(R.string.dlg_set_user_info_fail_tip));
                            return;
                        }

                        toast(R.string.dlg_set_user_info_succeed_tip);
                        // 再次进行修改用户名和密码
                        modifyUserPwd(m_modifyUserPwdDev);
                    }
                }).create();
        m_input_user_pwd_dlg.show();
    }
}

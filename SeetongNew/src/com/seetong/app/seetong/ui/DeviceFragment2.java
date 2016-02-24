package com.seetong.app.seetong.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.aid.ClearEditText;
import com.umeng.analytics.MobclickAgent;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.util.List;

/**
 * DeviceFragment ��������ʾ�豸�б���ص� Fragment������ MainActivity �б���ӵ� TabHost ��.
 * Seetong ��¼������ MainActivity����Ĭ����ʾ DeviceFragment.
 * DeviceFragment ���ְ���������ͬ�� Fragment��һ���� DeviceListFragment����һ���� DeviceNoMsgFragment��
 * Seetongͨ��һ���̴߳ӷ�������ȡ��Ϣ��⵱ǰ�˺����Ƿ����豸�����������ʾ DeviceListFragment��������ʾ
 * DeviceNoMsgFragment.ע���������豸ʱ��MainActivity2֮��Ľ�����ʹ�ûص�����ʵ��.
 * ע�� Fragment Ƕ��ʹ��ʱҪʹ�� android.support.v4 ���ݰ�.
 *
 * Created by gmk on 2015/9/11.
 */
public class DeviceFragment2 extends BaseFragment {
    private View view;
    private DeviceNoMsgFragment deviceNoMsgFragment;
    private DeviceListFragment deviceListFragment;
    private BaseFragment currentFragment;
    private ProgressDialog mTipDlg;
    private ImageButton deviceAddButton;
    private ClearEditText searchText;
    private TextView deviceText;
    private ImageButton searchButton;
    public boolean bShowSearchText = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setDeviceFragment(this);
        view = inflater.inflate(R.layout.device2, container);
        deviceNoMsgFragment = DeviceNoMsgFragment.newInstance();
        deviceListFragment = DeviceListFragment.newInstance();
        currentFragment = deviceListFragment;
        initWidget(view);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (bShowSearchText) {
            if (deviceListFragment != null) {
                deviceListFragment.showDeviceList();
            }
            searchText.setText("");
            searchText.setVisibility(View.GONE);
            deviceText.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            bShowSearchText = false;
            return true;
        }
        return super.onBackPressed();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("MainActivity2");
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("MainActivity2");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            /* �˺������� Fragment Ƕ�ף���ʱĬ����ʾ DeviceListFragment */
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.device_fragment_container, deviceListFragment)
                    .commit();
        }
    }

    /**
     * ��ʼ���� Fragment �еĻ������.
     */
    private void initWidget(final View view) {
        deviceAddButton = (ImageButton) view.findViewById(R.id.device_add);
        deviceAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* ���������豸ҳ�� */
                Intent intent = new Intent(DeviceFragment2.this.getActivity(), AddDeviceActivity.class);
                intent.putExtra(Constant.ENTER_TYPES, 1);
                startActivityForResult(intent, Constant.ADD_DEVICE_REQ_ID);
            }
        });

        searchText = (ClearEditText) view.findViewById(R.id.etSearchDevice);
        deviceText = (TextView) view.findViewById(R.id.device_list_text);
        searchButton = (ImageButton) view.findViewById(R.id.device_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setVisibility(View.VISIBLE);
                deviceText.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
                bShowSearchText = true;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() > 0) {
            if (deviceListFragment != null) {
                deviceListFragment.showSearchDeviceList(s);
            }
        } else {
            if (deviceListFragment != null) {
                deviceListFragment.showDeviceList();
            }

            searchText.setVisibility(View.GONE);
            deviceText.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            MainActivity2.m_this.hideInputPanel(null);
            bShowSearchText = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case Constant.ADD_DEVICE_REQ_ID:
                /* TODO: �յ��˷�����Ϣ����Ҫ��̬�����豸�б� */
                mTipDlg = new ProgressDialog(MainActivity2.m_this, R.string.device_add_now);
                mTipDlg.setCancelable(false);
                final String devId = data.getStringExtra(Constant.DEVICE_INFO_KEY);
                String xml = data.getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
                MainActivity2.m_this.onNotifyDevData(xml, new MainActivity2.ParseDevListResult() {
                    @Override
                    public void onResult(List<PlayerDevice> devices) {
                        MainActivity2.m_this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initWidget(view);
                                mTipDlg.setCallback(new ProgressDialog.ICallback() {
                                    @Override
                                    public void onTimeout() {
                                        mTipDlg.dismiss();
                                        toast(R.string.device_add_success);
                                    }

                                    @Override
                                    public boolean onCancel() {
                                        return false;
                                    }
                                });
                                mTipDlg.show(3000);
                            }
                        });
                    }
                });
                break;
            default:
                break;
        }
    }

    public void updateDeviceFragment(int listSize) {
        if (listSize > 0) {
            showDeviceListFragment();
            currentFragment = deviceListFragment;
        } else {
            showDeviceNoMsgFragment();
            currentFragment = deviceNoMsgFragment;
        }
    }

    private void showDeviceNoMsgFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.device_fragment_container, deviceNoMsgFragment)
                .commit();
    }

    private void showDeviceListFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.device_fragment_container, deviceListFragment)
                .commit();
    }

    public void handleMessage(android.os.Message msg) {
        //Log.e("msg", ":::::::::::::::::::::>" + (currentFragment instanceof DeviceListFragment) + " " +msg.what);
        if (currentFragment instanceof DeviceListFragment) {
            switch (msg.what) {
                case Define.MSG_UPDATE_DEV_ALIAS:
                    deviceListFragment.handleMessage(msg);
                    break;
                case Define.MSG_UPDATE_DEV_LIST:
                    if (bShowSearchText) {
                        if (deviceListFragment != null) {
                            deviceListFragment.showDeviceList();
                        }
                        searchText.setText("");
                        searchText.setVisibility(View.GONE);
                        deviceText.setVisibility(View.VISIBLE);
                        searchButton.setVisibility(View.VISIBLE);
                        bShowSearchText = false;
                    }
                    deviceListFragment.handleMessage(msg);
                    break;
                case SDK_CONSTANT.TPS_MSG_P2P_CONNECT_OK:
                    deviceListFragment.handleMessage(msg);
                    break;
                case SDK_CONSTANT.TPS_MSG_P2P_OFFLINE:
                    deviceListFragment.handleMessage(msg);
                    break;
                case SDK_CONSTANT.TPS_MSG_P2P_NVR_OFFLINE:
                    deviceListFragment.handleMessage(msg);
                    break;
                case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_OFFLINE:
                    deviceListFragment.handleMessage(msg);
                    break;
                case SDK_CONSTANT.TPS_MSG_P2P_NVR_CH_ONLINE:
                    deviceListFragment.handleMessage(msg);
                    break;
                case Define.MSG_ENABLE_ALIAS:
                    deviceListFragment.handleMessage(msg);
                    break;
                default:
                    break;
            }
        }
    }
}


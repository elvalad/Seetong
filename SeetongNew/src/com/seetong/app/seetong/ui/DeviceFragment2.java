package com.seetong.app.seetong.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.TextView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.Tools;
import com.seetong.app.seetong.model.News;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.aid.ClearEditText;
import com.umeng.analytics.MobclickAgent;
import ipc.android.sdk.com.SDK_CONSTANT;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DeviceFragment 是用于显示设备列表相关的 Fragment，它在 MainActivity 中被添加到 TabHost 中.
 * Seetong 登录后会进入 MainActivity，会默认显示 DeviceFragment.
 * DeviceFragment 中又包含两个不同的 Fragment，一个是 DeviceListFragment，另一个是 DeviceNoMsgFragment，
 * Seetong通过一个线程从服务器获取信息监测当前账号下是否有设备，如果有则显示 DeviceListFragment，否则显示
 * DeviceNoMsgFragment.注意在增加设备时和MainActivity2之间的交互，使用回调函数实现.
 * 注意 Fragment 嵌套使用时要使用 android.support.v4 兼容包.
 *
 * Created by gmk on 2015/9/11.
 */
public class DeviceFragment2 extends BaseFragment {
    private View view;
    private DeviceNoMsgFragment deviceNoMsgFragment;
    private DeviceListFragment2 deviceListFragment;
    private BaseFragment currentFragment;
    private ProgressDialog mTipDlg;
    private ImageButton deviceAddButton;
    private ClearEditText searchText;
    private TextView deviceText;
    private ImageButton searchButton;
    public boolean bShowSearchText = false;
    private ImageButton newsButton;
    private ImageView newsPrompt;
    private List<Integer> newsId = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setDeviceFragment(this);
        view = inflater.inflate(R.layout.device2, container);
        deviceNoMsgFragment = DeviceNoMsgFragment.newInstance();
        deviceListFragment = DeviceListFragment2.newInstance();
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
        int flags = Tools.getLanguageTypes();
        if (flags == 0 || flags == 1) {
            LibImpl.getInstance().getFuncLib().GetServiceMessage(0);
        } else {
            LibImpl.getInstance().getFuncLib().GetServiceMessage(1);
        }
        if (savedInstanceState == null) {
            /* 此函数用于 Fragment 嵌套，此时默认显示 DeviceListFragment */
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.device_fragment_container, deviceListFragment)
                    .commit();
        }
    }

    /**
     * 初始化此 Fragment 中的基本组件.
     */
    private void initWidget(final View view) {
        deviceAddButton = (ImageButton) view.findViewById(R.id.device_add);
        deviceAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 启动增加设备页面 */
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

        newsPrompt = (ImageView) view.findViewById(R.id.news_prompt);
        newsButton = (ImageButton) view.findViewById(R.id.device_news);
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceFragment2.this.getActivity(), NewsActivity.class);
                startActivity(intent);
                newsPrompt.setVisibility(View.GONE);
            }
        });
    }

    private void parseNewsXML(String xml) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            int id = 0;
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("ID")) {
                            id = Integer.parseInt(parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        newsId.add(id);
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
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
                /* TODO: 收到此返回消息后需要动态更新设备列表 */
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
        if (currentFragment instanceof DeviceListFragment2) {
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
                case SDK_CONSTANT.TPS_MSG_RSP_GET_SERVICE_MSG_LIST:
                    int maxNewsId = Global.m_spu.loadIntSharedPreference(Define.MAX_NEWS_ID);
                    parseNewsXML(Global.getNewsListXML());
                    if (Collections.max(newsId) > maxNewsId) {
                        newsPrompt.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}


package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seetong5.app.seetong.R;

/**
 * DeviceNoMsgFragment ����ʾ��ǰ�û�û���豸��Ϣʱ�� Fragment����ֻ����û���κ���Ϣʱ�Ż�
 * ��ʾ. ���� DeviceListFragment һ����Ƕ���� DeviceFragment ��ʹ�õ�.
 *
 * Created by gmk on 2015/9/13.
 */
public class DeviceNoMsgFragment extends BaseFragment {

    public static DeviceNoMsgFragment newInstance() {
        return new DeviceNoMsgFragment();
    }

    public DeviceNoMsgFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_no_msg, container, false);
        return view;
    }
}


package com.seetong5.app.seetong.ui.utils;

import com.seetong5.app.seetong.sdk.impl.PlayerDevice;

import java.util.Comparator;


public class DeviceSortUtil implements Comparator<PlayerDevice>{
	@Override
	public int compare(PlayerDevice dev1, PlayerDevice dev2) {
		int flag = dev2.m_dev.getOnLine() - dev1.m_dev.getOnLine();
		if(flag == 0){
			flag = compareNVR(dev1.m_dev.getDevId(), dev2.m_dev.getDevId());//dev1.getDevId().compareToIgnoreCase(dev2.getDevId());
			if(flag == 0){
				flag = compareNVR(dev1.m_dev.getDevName(), dev2.m_dev.getDevName());//dev1.getDevName().compareToIgnoreCase(dev2.getDevName());
			}
		}
		return flag;
	}

	/**
	 *比较NVR的设备ID(100101-chanel-1) 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public int compareNVR(String str1, String str2){
		int flag = -1;
		if(str1 != null && str2 != null){
			String[] strAry1 = str1.split("-");
			String[] strAry2 = str2.split("-");
			if(strAry1 != null && strAry1.length >= 3 && strAry2 != null && strAry2.length >= 3){//NVR&NVR
				flag = strAry1[0].compareToIgnoreCase(strAry2[0]);
				if(flag == 0){
					flag = strAry1[1].compareToIgnoreCase(strAry2[1]);
					if(flag == 0){
						int n1,n2;
						try {
							n1 = Integer.parseInt(strAry1[2]);
							n2 = Integer.parseInt(strAry2[2]);
						} catch (NumberFormatException e) {
							n1 = 1;
							n2 = 2;
						}						
						flag = n1 - n2;
					}
				}
			}else{//NVR&IPC or IPC&IPC
				flag = str1.compareToIgnoreCase(str2);
			}
		}
		return flag;
	}
}

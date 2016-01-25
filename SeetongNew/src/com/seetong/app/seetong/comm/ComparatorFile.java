package com.seetong.app.seetong.comm;

import java.io.File;
import java.util.Comparator;

/**
 * 文件比较操作(比较顺序：文件名、文件最近修改时间、文件尺寸)
 * 
 * @author yinql
 * 
 */
public class ComparatorFile implements Comparator<File>{

	@Override
	public int compare(File f1, File f2) {
		/** 相册文件比较规则如下：*/
		// 文件最近修改时间
		int flag = compareLong2(f1.lastModified(), f2.lastModified());//(int)(f2.lastModified()-f1.lastModified());
		if (flag == 0){
			// 文件名
			flag =f1.getName().compareToIgnoreCase(f2.getName());
			if (flag == 0){
				// 文件尺寸
				return compareLong(f1.length(), f2.length());
			}
			return flag;
		}
		return flag;
	}
	public int compareLong2(long l1, long l2){
		int ret = 0;
		long t = l2 - l1;
		if(t > 0){
			ret = 1;
		}else if(t == 0){
			ret = 0;
		}else{
			ret = -1;
		}
		return ret;
	}
	
	public int compareLong(long l1, long l2){
		String str1 = l1+"";
		String str2 = l2+"";
		return str1.compareToIgnoreCase(str2);
	}
	
	public int compareInt(int l1, int l2){
		String str1 = l1+"";
		String str2 = l2+"";
		return str1.compareToIgnoreCase(str2);
	}
}

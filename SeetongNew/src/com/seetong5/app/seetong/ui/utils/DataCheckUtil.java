package com.seetong5.app.seetong.ui.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @declaration 数据合法性检测类<br>
 * @author qinglei.yin@192.168.88.9<br>
 *2014-4-10 下午2:30:09<br>
 */
public class DataCheckUtil {
	public static void main(String[] args) {
		String value = "123d46dsfs";
		System.out.println(value+" isRightDevID="+isRightDevID(value));
		value = "45125454545";
		System.out.println(value+" isRightDevID="+isRightDevID(value));
		
		value = "45125454ds545";
		System.out.println(value+" isRightUserName="+isRightUserName(value));

		value = "41ds5125454545";
		System.out.println(value+" isRightUserPwd="+isRightUserPwd(value));
		
		value = "u-45_4-5+kk@4.c";
		System.err.println(value+" isRightEmail="+isRightEmail(value));
		
		value = "45125454545";
		System.out.println(value+" isRightPhone="+isRightPhone(value));		
		value = "12345678902";
		System.out.println(value+" isRightPhone="+isRightPhone(value));	
	}
	public static boolean isRightDevID(String value){
		boolean isOK = false;
		if(!isNullStr(value)){
			String reg = "^\\d{6,32}$"; //6-32位的云ID
		    Pattern p = Pattern.compile(reg);   
		    Matcher m = p.matcher(value);
		    isOK = m.find();
		}
		return isOK;
	}
	public static boolean isRightUserName(String value){
		boolean isOK = false;
		if(!isNullStr(value)){
			String reg = "^[A-Za-z0-9_]{4,32}$"; //\w匹配任何字类字符，包括下划线。与“[A-Za-z0-9_]”等效
		    Pattern p = Pattern.compile(reg);   
		    Matcher m = p.matcher(value);
		    isOK = m.find();
		}
		return isOK;
	}
	public static boolean isRightUserPwd(String value){
		boolean isOK = false;
		if(!isNullStr(value)){
			String reg = "^[A-Za-z0-9_]{6,32}$"; //\w匹配任何字类字符，包括下划线。与“[A-Za-z0-9_]”等效
		    Pattern p = Pattern.compile(reg);   
		    Matcher m = p.matcher(value);
		    isOK = m.find();
		}
		return isOK;
	}
	public static boolean isRightEmail(String value){
		boolean isOK = false;
		if(!isNullStr(value)){
			//"^[A-Za-z0-9_]{1,}([-+.][A-Za-z0-9_]{1,}){0,}@[A-Za-z0-9_]{1,}([-.][A-Za-z0-9_]{1,}){0,}\\.[A-Za-z0-9_]{1,}([-.][A-Za-z0-9_]{1,}){0,}"
			String reg = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";//abc@tt.com
		    Pattern p = Pattern.compile(reg);   
		    Matcher m = p.matcher(value);
		    isOK = m.find();
		}
		return isOK;
	}
	public static boolean isRightPhone(String value){
		boolean isOK = false;
		if(!isNullStr(value)){
			String reg = "^1\\d{10}$"; //15677889988
		    Pattern p = Pattern.compile(reg);   
		    Matcher m = p.matcher(value);
		    isOK = m.find();
		}
		return isOK;
	}
	public static boolean isNullStr(String value){
		boolean isOK = false;
		if(value == null || "".equals(value.trim())){
			isOK = true;
		}
		return isOK;
	}
}

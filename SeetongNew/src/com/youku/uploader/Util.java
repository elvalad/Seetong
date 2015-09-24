package com.youku.uploader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.loopj.android.http.*;
import org.apache.http.HttpEntity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class Util {

	private static AsyncHttpClient client = new AsyncHttpClient();

	protected static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(url, params, responseHandler);
	}

	protected static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(url, params, responseHandler);
	}
	
	protected static AsyncHttpClient getAsyncHttpClient() {
		return client;
	}

	protected static void post(Context context, String url, HttpEntity entity, String contentType,
			AsyncHttpResponseHandler responseHandler) {
		client.post(context, url, entity, contentType, responseHandler);
	}

	protected static void Log(String tag, String msg) {
		if (Config.DEBUG) {
			android.util.Log.e(tag, msg);
		}
	}

	public static String parseSize(long size) {
		long sizeKB = size / 1024;
		if (sizeKB >= 1024 * 1024) {
			float sizeKBFloat = Float.parseFloat(sizeKB + "");
			float sizeGB = new BigDecimal(sizeKBFloat / 1024f / 1024f).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
			return sizeGB + "GB";
		} else if (sizeKB >= 1024) {
			float sizeKBFloat = Float.parseFloat(sizeKB + "");
			float sizeMB = new BigDecimal(sizeKBFloat / 1024f).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
			return sizeMB + "MB";
		} else
			return sizeKB + "KB";
	}

	protected static byte[] readSliceData(String file_name, long offset, int length) {
		byte[] data = new byte[length];
		try {
			FileInputStream fis = new FileInputStream(file_name);
			fis.skip(offset);
			fis.read(data);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	protected static String getCRC(byte[] data) {
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		return Long.toHexString(crc32.getValue());
	}

	protected static String getFileMD5(String fileName) {
		InputStream fis;
		try {
			fis = new FileInputStream(fileName);
			byte[] buffer = new byte[1024];
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			fis.close();
			return bufferToHex(md5.digest());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String bufferToHex(byte[] bytes) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = md5Chars[(bt & 0xf0) >> 4];
		char c1 = md5Chars[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	private static char md5Chars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String md5(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void sendHandlerMsg(int resType, Bundle bundle, Handler handler) {
		Message msg = handler.obtainMessage(resType);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	public static String getErrorMsg(String errorType, String description, int code) {
		return "{'error':{'type':'" + errorType + "','description':'" + description + "','code':" + code + "}}";
	}
}
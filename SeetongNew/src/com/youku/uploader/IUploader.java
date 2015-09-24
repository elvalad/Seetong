package com.youku.uploader;

import android.os.Handler;

import java.util.HashMap;

public interface IUploader {
	void upload(HashMap<String, String> params, HashMap<String, String> uploadInfo, Handler handler);
	
	void cancel();
}

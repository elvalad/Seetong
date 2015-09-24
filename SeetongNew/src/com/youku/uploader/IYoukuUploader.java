package com.youku.uploader;

import java.util.HashMap;

public interface IYoukuUploader {

	void upload(HashMap<String, String> params, HashMap<String, String> uploadInfo, final IUploadResponseHandler handler);

	Boolean cancel();

}

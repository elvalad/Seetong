package com.youku.uploader;

import org.json.JSONObject;

public interface IUploadResponseHandler {
	void onStart();

	void onProgressUpdate(int counter);

	void onSuccess(JSONObject response);

	void onFailure(JSONObject errorResponse);

	void onFinished();

}
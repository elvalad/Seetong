package com.youku.video;

import org.json.JSONObject;

public interface IResponseHandler {
	void on_show_basic(int result, JSONObject response);
}
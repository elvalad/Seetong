package com.youku.uploader;

import android.content.Context;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.entity.ByteArrayEntity;

import java.util.HashMap;

public class Api implements IRequest {

    protected String client_id = null;
    protected String client_secret = null;

    protected Context context = null;

	public Api(String client_id, String client_secret, Context context) {
		this.client_id = client_id;
		this.client_secret = client_secret;
		this.context = context;
	}

	@Override
	public void login(String username, String password, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("client_id", this.client_id);
		params.put("client_secret", this.client_secret);
		params.put("username", username);
		params.put("password", password);
		params.put("grant_type", "password");
		Util.post(Config.LOGIN_URL, params, responseHandler);
	}

	@Override
	public void refresh_token(String refresh_token, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("client_id", this.client_id);
		params.put("client_secret", this.client_secret);
		params.put("grant_type", "refresh_token");
		params.put("refresh_token", refresh_token);
		Util.post(Config.LOGIN_URL, params, responseHandler);
	}

	@Override
	public void create(String access_token, HashMap<String, String> uploadInfo, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams(uploadInfo);
		params.put("client_id", this.client_id);
		params.put("access_token", access_token);
		Util.get(Config.CREATE_URL, params, responseHandler);
	}

	@Override
	public void create_file(String upload_token, String file_size, String ext, String upload_server_uri,
			AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("upload_token", upload_token);
		params.put("file_size", file_size);
		params.put("ext", ext);
		params.put("slice_length", Config.SLICE_LENGTH + "");
		Util.post(getRealUrl(Config.CREATE_FILE_URL, upload_server_uri), params, responseHandler);
	}

	@Override
	public void new_slice(String upload_token, String upload_server_uri, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams("upload_token", upload_token);
		Util.get(getRealUrl(Config.NEW_SLICE_URL, upload_server_uri), params, responseHandler);
	}

	@Override
	public void upload_slice(String upload_token, String upload_server_uri, HashMap<String, String> sliceInfo, byte[] data,
			AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams(sliceInfo);
		params.put("upload_token", upload_token);
		params.put("crc", Util.getCRC(data));
		Util.post(context, getRealUrl(Config.UPLOAD_SLICE_URL, upload_server_uri) + "?" + params.toString(), new ByteArrayEntity(data),
				"multipart/form-data; boundary=***** ", responseHandler);
	}

	@Override
	public void check(String upload_token, String upload_server_uri, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams("upload_token", upload_token);
		Util.get(getRealUrl(Config.CHECK_URL, upload_server_uri), params, responseHandler);
	}

	@Override
	public void commit(String access_token, String upload_token, String upload_server_ip, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("client_id", this.client_id);
		params.put("access_token", access_token);
		params.put("upload_token", upload_token);
		params.put("upload_server_ip", upload_server_ip);
		Util.post(Config.COMMIT_URL, params, responseHandler);
	}

	private String getRealUrl(String url, String upload_server_uri) {
		return url.replace("upload_server_uri", upload_server_uri);
	}

	public Context getContext() {
		return context;
	}

	@Override
	public void cancel(String access_token, String upload_token, String upload_server_ip, AsyncHttpResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("client_id", this.client_id);
		params.put("access_token", access_token);
		params.put("upload_token", upload_token);
		params.put("upload_server_ip", upload_server_ip);
		Util.get(Config.CANCEL_URL, params, responseHandler);
	}

	public void versionUpdate() {
		RequestParams params = new RequestParams();
		params.put("client_id", this.client_id);
		params.put("version", Config.VERSION);
		params.put("type", "android");
		Util.get(Config.VERSION_UPDATE_URL, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				super.onSuccess(arg0, arg1);
			}
		});
	}
}

package com.youku.uploader;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.HashMap;

public interface IRequest {

	/**
	 * 获取access_token
	 */
	void login(String username, String password, AsyncHttpResponseHandler responseHandler);

	/**
	 * 刷新access_token
	 */
	void refresh_token(String refresh_token, AsyncHttpResponseHandler responseHandler);

	/**
	 * 获取upload_token、upload_server_url
	 */
	void create(String access_token, HashMap<String, String> uploadInfo, AsyncHttpResponseHandler responseHandler);

	/**
	 * 创建上传文件、提交上传信息
	 */
	void create_file(String upload_token, String file_size, String ext, String upload_server_uri, AsyncHttpResponseHandler responseHandler);

	/**
	 * 请求创建slice_task_id, 获取分片offset、长度等
	 */
	void new_slice(String upload_token, String upload_server_uri, AsyncHttpResponseHandler responseHandler);

	/**
	 * 上传分片
	 */
	void upload_slice(String upload_token, String upload_server_uri, HashMap<String, String> sliceInfo, byte[] data, AsyncHttpResponseHandler responseHandler);

	/**
	 * 检查上传任务是否完成
	 */
	void check(String upload_token, String upload_server_uri, AsyncHttpResponseHandler responseHandler);

	/**
	 * 确认上传结束
	 */
	void commit(String access_token, String upload_token, String upload_server_ip, AsyncHttpResponseHandler responseHandler);
	
	void cancel(String access_token, String upload_token, String upload_server_ip, AsyncHttpResponseHandler responseHandler);
}

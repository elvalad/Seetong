package com.youku.uploader;

import java.io.File;
import java.util.HashMap;

public class VideoInfo {

	/**
	 * 上传步骤flag
	 */
	private int step = -1;

	/**
	 * token 过期前step
	 */
	private int expire_step;

	/**
	 * 上传token
	 */
	private String upload_token;

	/**
	 * 上传服务器URI
	 */
	private String upload_server_uri;
	
	/**
	 * 上传服务器IP，用于commit、cancel
	 */
	private String upload_server_ip;
	
	/**
	 * 上传文件信息
	 * 
	 * @params file_name
	 * @params file_md5
	 * @params file_size
	 * @params title
	 * @params tags
	 */
	private HashMap<String, String> uploadInfo;

	public VideoInfo(HashMap<String, String> uploadInfo) {
		this.uploadInfo = uploadInfo;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public HashMap<String, String> getUploadInfo() {
		return uploadInfo;
	}

	public String getUploadInfo(String key) {
		return uploadInfo.containsKey(key) ? uploadInfo.get(key) : null;
	}

	public String getUploadServerUri() {
		return upload_server_uri;
	}

	public void setUploadServerUri(String upload_server_uri) {
		this.upload_server_uri = upload_server_uri;
	}

	public int getExpireStep() {
		return expire_step;
	}

	public void setExpireStep(int expire_step) {
		this.expire_step = expire_step;
	}

	public String getUploadToken() {
		return upload_token;
	}

	public void setUploadToken(String upload_token) {
		this.upload_token = upload_token;
	}

	public Boolean checkUploadInfo() {
		String file_name = uploadInfo.get("file_name");
		if (file_name != null) {
			Util.Log("upload file_name", file_name);
			File file = new File(file_name);
			if (!file.exists()) {
				return false;
			} else {
				uploadInfo.put("file_md5", Util.getFileMD5(file_name));
				uploadInfo.put("file_size", String.valueOf(file.length()));
				uploadInfo.put("ext", file_name.substring(file_name.lastIndexOf(".") + 1));
				return true;
			}
		} else {
			return false;
		}
	}

	public String getUploadServerIp() {
		return upload_server_ip;
	}

	public void setUploadServerIp(String upload_server_ip) {
		this.upload_server_ip = upload_server_ip;
	}
}

package com.youku.uploader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.client.HttpResponseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class Uploader implements IUploader {

	private final int STEP_LOGIN = 0;
	private final int STEP_CREATE = 1;
	private final int STEP_CREATE_FILE = 2;
	private final int STEP_NEW_SLICE = 3;
	private final int STEP_UPLOAD_SLICE = 4;
	private final int STEP_CHECK = 5;
	private final int STEP_COMMIT = 6;
	private final int STEP_REFRESH_TOKEN = 7;

	/**
	 * OAuth2 授权
	 */
	private String access_token = null;

	/**
	 * 刷新access_token
	 */
	private String refresh_token = null;

	/**
	 * access_token 有效时长，秒
	 */
	// private int expires_in = 0;

	private Api api = null;

	private VideoInfo videoInfo = null;

	/**
	 * UI 线程handler
	 */
	private Handler handler = null;

	/**
	 * send handler message (bundle)
	 */
	private Bundle bundle = null;

	private Boolean isLoop = false;

	public Uploader(String client_id, String client_secret, Context context) {
		api = new Api(client_id, client_secret, context);
	}

	@Override
	public void upload(HashMap<String, String> params, HashMap<String, String> uploadInfo, Handler handler) {
		this.handler = handler;
		bundle = new Bundle();
		isLoop = true;

		if (params.isEmpty() || uploadInfo.isEmpty()) {
			bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_SYSTEM, Config.ERROR_1012, 1012));
			Util.sendHandlerMsg(Config.RES_FAILURE, bundle, this.handler);
			return;
		}

		if (params.get("access_token") == null && (params.get("username") == null || params.get("password") == null)) {
			bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_SYSTEM, Config.ERROR_1012, 1012));
			Util.sendHandlerMsg(Config.RES_FAILURE, bundle, this.handler);
			return;
		}

		if (uploadInfo.get("title") == null || uploadInfo.get("tags") == null || uploadInfo.get("file_name") == null) {
			bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_SYSTEM, Config.ERROR_1012, 1012));
			Util.sendHandlerMsg(Config.RES_FAILURE, bundle, this.handler);
			return;
		}

		videoInfo = new VideoInfo(uploadInfo);

		// 文件不存在
		if (!videoInfo.checkUploadInfo()) {
			bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_FILE_NOT_FOUND, Config.ERROR_120020001, 120020001));
			Util.sendHandlerMsg(Config.RES_FAILURE, bundle, this.handler);
			return;
		}

		if (params.get("debug") != null) {
			Config.DEBUG = true;
		}
		
		api.versionUpdate();

		Util.getAsyncHttpClient().setTimeout(Config.TIMEOUT);
		if (params.get("access_token") != null) {
			// create
			Util.Log("upload ", "access_token exist ==> step create");
			access_token = params.get("access_token");
			videoInfo.setStep(STEP_CREATE);
			api.create(access_token, uploadInfo, new HttpResponseHandler(videoInfo));
		} else {
			// login
			Util.Log("upload ", "step Login");
			videoInfo.setStep(STEP_LOGIN);
			api.login(params.get("username"), params.get("password"), new HttpResponseHandler(videoInfo));
		}
	}

	@Override
	public void cancel() {
		isLoop = false;
		if (videoInfo.getUploadToken() != null) {
			Util.Log("upload cancel", "uploader cancel void");
			AsyncHttpClient client = Util.getAsyncHttpClient();
			client.setTimeout(Config.TIMEOUT);
			client.cancelRequests(api.getContext(), true);
			api.cancel(access_token, videoInfo.getUploadToken(), videoInfo.getUploadServerIp(), new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(int result, JSONObject response) {
					super.onSuccess(result, response);
					Util.Log("upload cancel", response.toString());
				}
			});
		}
	}

	public VideoInfo getVideoInfo() {
		return videoInfo;
	}

	private void parseError() {
		isLoop = false;
		bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_SYSTEM, Config.ERROR_1002, 1002));
		Util.sendHandlerMsg(Config.RES_FAILURE, bundle, Uploader.this.handler);
	}

	private int getProgress(String file_size, long transferred) {
		return (int) (transferred * 100 / Long.valueOf(file_size));
	}

	private void doLogin(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		VideoInfo videoInfo = httpResponseHandler.getVideoInfo();
		try {
			access_token = response.getString("access_token");
			refresh_token = response.getString("refresh_token");

			// create
			videoInfo.setStep(STEP_CREATE);
			Util.Log("upload ", "step Create");
			api.create(access_token, videoInfo.getUploadInfo(), httpResponseHandler);
		} catch (JSONException e) {
			parseError();
		}
	}

	private void doCreate(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		VideoInfo videoInfo = httpResponseHandler.getVideoInfo();
		try {
			videoInfo.setUploadToken(response.getString("upload_token"));
			videoInfo.setUploadServerUri(response.getString("upload_server_uri"));
			String instant_upload_ok = response.getString("instant_upload_ok");

			// 秒传
			if (instant_upload_ok == "yes") {
				Util.Log("upload ", "step Check 秒传");
				videoInfo.setStep(STEP_CHECK);
				api.check(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), httpResponseHandler);
			} else {
				Util.Log("upload ", "step Create File");
				videoInfo.setStep(STEP_CREATE_FILE);
				api.create_file(videoInfo.getUploadToken(), videoInfo.getUploadInfo("file_size"), videoInfo.getUploadInfo("ext"),
						videoInfo.getUploadServerUri(), httpResponseHandler);
			}
		} catch (JSONException e) {
			parseError();
		}
	}

	private void doCreateFile(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		VideoInfo videoInfo = httpResponseHandler.getVideoInfo();
		if (result == 201) {
			Util.Log("upload ", "step New Slice");
			videoInfo.setStep(STEP_NEW_SLICE);
			api.new_slice(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), httpResponseHandler);
		}
	}

	/**
	 * new slice, uplaod_slice
	 * 
	 * @param result
	 * @param response
	 */
	private void doSlice(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		VideoInfo videoInfo = httpResponseHandler.getVideoInfo();
		HashMap<String, String> sliceInfo = new HashMap<String, String>();
		try {
			int slice_task_id = response.getInt("slice_task_id");
			boolean finished = response.getBoolean("finished");
			long transferred = response.getLong("transferred");
			String offset = response.getString("offset");
			String length = response.getString("length");

			handler.obtainMessage(Config.RES_PROGRESS_UPDATE, getProgress(videoInfo.getUploadInfo("file_size"), transferred))
					.sendToTarget();

			// 全部上传完成，走check 流程
			if (finished || slice_task_id == 0) {
				Util.Log("upload ", "step Check");
				videoInfo.setStep(STEP_CHECK);
				Util.getAsyncHttpClient().setTimeout(Config.TIMEOUT);
				api.check(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), httpResponseHandler);
			} else {
				sliceInfo.put("slice_task_id", slice_task_id + "");
				sliceInfo.put("offset", offset);
				sliceInfo.put("length", length);

				Util.Log("upload ", "step upload slice");
				videoInfo.setStep(STEP_UPLOAD_SLICE);
				Util.getAsyncHttpClient().setTimeout(Config.TIMEOUT_UPLOAD_DATA);
				api.upload_slice(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), sliceInfo,
						Util.readSliceData(videoInfo.getUploadInfo("file_name"), Long.parseLong(offset), Integer.parseInt(length)),
						httpResponseHandler);
			}
		} catch (JSONException e) {
			parseError();
		}
	}

	private void doCheck(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		VideoInfo videoInfo = httpResponseHandler.getVideoInfo();
		try {
			int status = response.getInt("status");
			videoInfo.setUploadServerIp(response.getString("upload_server_ip"));

			Util.Log("upload check status", status + "");
			switch (status) {
			case 1:
				videoInfo.setStep(STEP_COMMIT);
				api.commit(access_token, videoInfo.getUploadToken(), videoInfo.getUploadServerIp(), httpResponseHandler);
				break;
			case 2:
			case 3:
				videoInfo.setStep(STEP_CHECK);
				try {
					Util.Log("upload thread", "sleep " + Config.SLEEPTIME);
					Thread.sleep(Config.SLEEPTIME);
					api.check(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), httpResponseHandler);
				} catch (InterruptedException e) {
					isLoop = false;
					e.printStackTrace();
				}
				break;
			case 4:
				// TODO 4 暂时不处理
				// 还有分片上传任务未分配，slice, reset_slice
				// int empty_tasks = response.getInt("empty_tasks");
				isLoop = false;
				bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_SYSTEM, Config.ERROR_1002, 1002));
				Util.sendHandlerMsg(Config.RES_FAILURE, bundle, Uploader.this.handler);
				break;
			}

		} catch (JSONException e) {
			parseError();
		}
	}

	private void doSuccess(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		videoInfo = null;
		handler.obtainMessage(Config.RES_PROGRESS_UPDATE, 100).sendToTarget();
		handler.obtainMessage(Config.RES_SUCCESS, response).sendToTarget();
	}

	private void doRefreshToken(int result, JSONObject response, HttpResponseHandler httpResponseHandler) {
		VideoInfo videoInfo = httpResponseHandler.getVideoInfo();
		try {
			// expires_in = response.getInt("expires_in");
			access_token = response.getString("access_token");
			refresh_token = response.getString("refresh_token");

			if (videoInfo.getStep() == STEP_NEW_SLICE) {
				Util.Log("upload ", "step new slice");
				videoInfo.setStep(videoInfo.getExpireStep());
				api.new_slice(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), httpResponseHandler);
			} else if (videoInfo.getStep() == STEP_CHECK) {
				Util.Log("upload ", "step check");
				videoInfo.setStep(videoInfo.getExpireStep());
				api.check(videoInfo.getUploadToken(), videoInfo.getUploadServerUri(), httpResponseHandler);
			}
			videoInfo.setExpireStep(0);
		} catch (JSONException e) {
			parseError();
		}
	}

	class HttpResponseHandler extends JsonHttpResponseHandler {

		private VideoInfo videoInfo;

		HttpResponseHandler(VideoInfo videoInfo) {
			this.videoInfo = videoInfo;
		}

		public VideoInfo getVideoInfo() {
			return videoInfo;
		}

		@Override
		protected void handleFailureMessage(Throwable e, String errorResponse) {
			super.handleFailureMessage(e, errorResponse);

			Util.Log("upload failuer", e.toString());
			Util.Log("upload failuer error", errorResponse);

			isLoop = false;

			// connect exception
			if (!(e instanceof HttpResponseException) && e instanceof IOException) {
				bundle.putString("failed", Util.getErrorMsg(Config.ERROR_TYPE_CONNECT, Config.ERROR_50002, 50002));
				Util.sendHandlerMsg(Config.RES_FAILURE, bundle, Uploader.this.handler);
				return;
			}

			try {
				JSONObject error = new JSONObject(errorResponse).getJSONObject("error");
				int code = error.getInt("code");

				// upload_token 过期, refresh_token
				if (code == 120010223) {
					Util.Log("upload ", "step refresh token");
					videoInfo.setExpireStep(videoInfo.getStep());
					videoInfo.setStep(STEP_REFRESH_TOKEN);
					api.refresh_token(refresh_token, this);
				} else {
					bundle.putString("failed", errorResponse.toString());
					Util.sendHandlerMsg(Config.RES_FAILURE, bundle, Uploader.this.handler);
				}

			} catch (JSONException e1) {
				parseError();
			}
		}

		@Override
		public void onSuccess(int result, JSONObject response) {
			Util.Log("result", response.toString());
			if (!isLoop) {
				return;
			}
			switch (videoInfo.getStep()) {
			case STEP_LOGIN:
				doLogin(result, response, this);
				break;
			case STEP_CREATE:
				doCreate(result, response, this);
				break;

			case STEP_CREATE_FILE:
				doCreateFile(result, response, this);
				break;

			case STEP_NEW_SLICE:
				doSlice(result, response, this);
				break;

			case STEP_UPLOAD_SLICE:
				doSlice(result, response, this);
				break;

			case STEP_CHECK:
				doCheck(result, response, this);
				break;

			case STEP_COMMIT:
				doSuccess(result, response, this);
				break;

			case STEP_REFRESH_TOKEN:
				doRefreshToken(result, response, this);
				break;
			}
		}
	}
}
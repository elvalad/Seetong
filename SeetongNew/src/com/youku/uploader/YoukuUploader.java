package com.youku.uploader;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class YoukuUploader implements IYoukuUploader {

	private static YoukuUploader instance = null;

	private static Uploader uploader = null;

	private Thread uploadThread = null;

	private IUploadResponseHandler uploadResponseHandler = null;

	private MyHandler m_handler;

	private static class MyHandler extends Handler {
		YoukuUploader m_self;
		public MyHandler(YoukuUploader self) {
			m_self = self;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Config.RES_START:
				m_self.uploadResponseHandler.onStart();
				break;

			case Config.RES_SUCCESS:
				m_self.uploadResponseHandler.onSuccess((JSONObject) msg.obj);
				m_self.uploadResponseHandler.onFinished();
				m_self.uploadThread = null;
				break;

			case Config.RES_FAILURE:
				try {
					m_self.uploadResponseHandler.onFailure(new JSONObject(msg.getData().getString("failed")));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				m_self.uploadResponseHandler.onFinished();
				m_self.uploadThread = null;
				break;

			case Config.RES_PROGRESS_UPDATE:
				m_self.uploadResponseHandler.onProgressUpdate((Integer) msg.obj);
				break;

			case Config.RES_FINISHED:
				m_self.uploadResponseHandler.onFinished();
				m_self.uploadThread = null;
				break;
			}
		}
	}

	private YoukuUploader() {
		m_handler = new MyHandler(this);
	}

	public static YoukuUploader getInstance(String client_id, String client_secret, Context context) {
		if (instance == null) {
			synchronized (YoukuUploader.class) {
				if (instance == null) {
					uploader = new Uploader(client_id, client_secret, context);
					instance = new YoukuUploader();
				}
			}
		}
		return instance;
	}

	@Override
	public void upload(final HashMap<String, String> params, final HashMap<String, String> uploadInfo,
			final IUploadResponseHandler responseHandler) {
		if (uploadThread == null) {
			this.uploadResponseHandler = responseHandler;
			Util.Log("upload ", "start Thread");
			uploadThread = new Thread(new Runnable() {

				@Override
				public void run() {
					Util.Log("upload ", "Thread run");
					// onStart
					m_handler.obtainMessage(Config.RES_START).sendToTarget();

					// upload video
					uploader.upload(params, uploadInfo, m_handler);
				}
			});
			uploadThread.start();
		} else {
			try {
				uploadResponseHandler.onFailure(new JSONObject(Util.getErrorMsg(Config.ERROR_TYPE_UPLOAD_TASK, Config.ERROR_50001,
						50001)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Boolean cancel() {
		if (uploadThread == null) {
			return false;
		}
		
		VideoInfo videoInfo = uploader.getVideoInfo();
		if (videoInfo == null) {
			return false;
		}
		
		uploader.cancel();
		// check sleep
		if (!uploadThread.isInterrupted()) {
			uploadThread.interrupt();
		}
		uploadThread = null;
		return true;
	}
}

package com.youku.video;

import android.content.Context;
import com.loopj.android.http.*;
import com.youku.uploader.Config;
import com.youku.uploader.Util;
import org.apache.http.client.HttpResponseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 2014-06-11.
 */
public class Api extends com.youku.uploader.Api {

    protected static final int OP_SHOW_BASIC = 1;
    protected static final String SHOW_BASIC = "https://openapi.youku.com/v2/videos/show_basic.json";
    private static AsyncHttpClient client = new AsyncHttpClient();
    private IResponseHandler m_resp;

    public Api(String client_id, String client_secret, Context context) {
        super(client_id, client_secret, context);
    }

    public void setResponseHandler(IResponseHandler resp) {
        m_resp = resp;
    }

    class HttpResponseHandler extends JsonHttpResponseHandler {
        private int m_op;

        HttpResponseHandler(int op) {
            m_op = op;
        }

        @Override
        protected void handleFailureMessage(Throwable e, String errorResponse) {
            super.handleFailureMessage(e, errorResponse);
            try {
                JSONObject error;
                if (!(e instanceof HttpResponseException) && e instanceof IOException) {
                    error = new JSONObject(Util.getErrorMsg(Config.ERROR_TYPE_CONNECT, Config.ERROR_50002,
                            50002)).getJSONObject("error");
                } else {
                    error = new JSONObject(errorResponse).getJSONObject("error");
                }

                switch (m_op) {
                    case Api.OP_SHOW_BASIC:
                        m_resp.on_show_basic(-1, error);
                        break;
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void onSuccess(int result, JSONObject response) {
            switch (m_op) {
                case Api.OP_SHOW_BASIC:
                    m_resp.on_show_basic(result, response);
                    break;
            }
        }
    }

    public void show_basic(String video_id) {
        RequestParams params = new RequestParams();
        params.put("client_id", this.client_id);
        params.put("video_id", video_id);
        client.get(SHOW_BASIC, params, new HttpResponseHandler(OP_SHOW_BASIC));
    }
}

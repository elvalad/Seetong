package com.seetong.app.seetong.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.seetong.app.seetong.R;

/**
 * Created by Administrator on 2016/7/19.
 */
public class NewsWebActivity extends BaseActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private String newsUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web);
        newsUrl = getIntent().getStringExtra("news_url");
        initWidget();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWidget() {
        progressBar = (ProgressBar) findViewById(R.id.progress_web);

        webView = (WebView) findViewById(R.id.news_web);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new MyChromeWebViewClient(NewsWebActivity.this));
        webView.loadUrl(newsUrl);
    }

    class MyChromeWebViewClient extends WebChromeClient {

        private Activity activity;
        private Animation animation;

        public MyChromeWebViewClient(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setMax(100);
            if (newProgress < 100) {
                if(progressBar.getVisibility()==View.GONE)
                    progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            } else {
                progressBar.setProgress(100);
                animation = AnimationUtils.loadAnimation(activity, R.anim.anim_news_web);
                progressBar.startAnimation(animation);
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        }
    }
}

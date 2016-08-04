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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.favorite.WechatFavorite;
import com.seetong.app.seetong.R;

/**
 * Created by Administrator on 2016/7/19.
 */
public class NewsWebActivity extends BaseActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private String newsUrl = "";
    private String newsTitle = "";
    private ImageButton backButton;
    private TextView newsTitleView;
    private ImageButton shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web);
        newsUrl = getIntent().getStringExtra("news_url");
        newsTitle = getIntent().getStringExtra("news_title");
        initWidget();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWidget() {
        backButton = (ImageButton) findViewById(R.id.news_web_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsWebActivity.this.finish();
            }
        });

        newsTitleView = (TextView) findViewById(R.id.news_web_title);
        newsTitleView.setText(newsTitle);

        shareButton = (ImageButton) findViewById(R.id.news_web_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShare();
            }
        });

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

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        oks.addHiddenPlatform(SinaWeibo.NAME);
        oks.addHiddenPlatform(WechatFavorite.NAME);

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(newsUrl);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(newsTitle);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(newsUrl);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(newsUrl);

        // 启动分享GUI
        oks.show(this);
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

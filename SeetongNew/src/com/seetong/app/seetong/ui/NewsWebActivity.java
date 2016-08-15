package com.seetong.app.seetong.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.favorite.WechatFavorite;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.Comment;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.SDK_CONSTANT;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/19.
 */
public class NewsWebActivity extends BaseActivity {

    private static final String TAG = NewsWebActivity.class.getName();
    private WebView webView;
    private ProgressBar progressBar;
    private String newsUrl = "";
    private String newsTitle = "";
    private String newsId = "";
    private ImageButton backButton;
    private TextView newsTitleView;
    private ImageButton shareButton;
    private LinearLayout layout;
    private MyScrollView scrollView;
    private Button postButton;
    private EditText commentText;
    private LinearLayout commentPageLayout;
    private TextView pageIndexView;
    private Button prevButton;
    private Button nextButton;
    private ListViewForScroll listView;
    private CommentListAdapter adapter;
    private List<Comment> data = new ArrayList<>();
    private int pageIndex = 0;
    private int allCount = 0;
    private int allPage = 0;
    private final static int COMMENT_COUNT_IN_ONE_PAGE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web);
        newsUrl = getIntent().getStringExtra("news_url");
        newsTitle = getIntent().getStringExtra("news_title");
        newsId = getIntent().getStringExtra("news_id");
        initWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LibImpl.getInstance().removeHandler(m_handler);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWidget() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = LibImpl.getInstance().getFuncLib().GetCommentInfo(newsId, pageIndex, COMMENT_COUNT_IN_ONE_PAGE);
                if (0 != ret) {
                    Log.d(TAG, "get comment info err : " + ret);
                }
            }
        }).start();

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

        scrollView = (MyScrollView) findViewById(R.id.news_scroll_view);
        layout = (LinearLayout) findViewById(R.id.news_comment);
        scrollView.setScrollViewListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                }
            }
        });

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

        commentText = (EditText) findViewById(R.id.comment_content);
        postButton = (Button) findViewById(R.id.post_comment);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commentText.length() == 0) {
                    toast(R.string.comment_can_not_null);
                } else if (commentText.length() > 128) {
                    toast(R.string.comment_too_long);
                } else {
                    int ret = LibImpl.getInstance().getFuncLib().AddCommentInfo(commentText.getText().toString(), newsId);
                    if (ret == 0) {
                        toast(R.string.post_comment_success);
                        commentText.setText("");
                    } else {
                        toast(R.string.post_comment_failure);
                        Log.d(TAG, "add comment info err : " + ret);
                    }

                }
            }
        });

        commentPageLayout = (LinearLayout) findViewById(R.id.comment_page);
        pageIndexView = (TextView) findViewById(R.id.pageIndex);
        listView = (ListViewForScroll) findViewById(R.id.comment_list);
        getData();
        adapter = new CommentListAdapter(NewsWebActivity.this, data);
        listView.setAdapter(adapter);
        prevButton = (Button) findViewById(R.id.comment_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageIndex == 0) {
                    toast(R.string.comment_first_page);
                } else {
                    pageIndex = pageIndex - 1;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int ret = LibImpl.getInstance().getFuncLib().GetCommentInfo(newsId, pageIndex, COMMENT_COUNT_IN_ONE_PAGE);
                            if (0 != ret) {
                                Log.d(TAG, "get comment info err : " + ret);
                                pageIndex = pageIndex + 1;
                            }
                        }
                    }).start();
                }
            }
        });
        nextButton = (Button) findViewById(R.id.comment_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageIndex == allPage - 1) {
                    toast(R.string.comment_last_page);
                } else {
                    pageIndex = pageIndex + 1;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int ret = LibImpl.getInstance().getFuncLib().GetCommentInfo(newsId, pageIndex, COMMENT_COUNT_IN_ONE_PAGE);
                            if (0 != ret) {
                                Log.d(TAG, "get comment info err : " + ret);
                                pageIndex = pageIndex - 1;
                            }
                        }
                    }).start();
                }
            }
        });
    }

    private String getSeetongImgPath() {
        String fileName = "seetong_logo.png";
        File dir = new File(Define.RootDirPath + "/default/images/");
        if (!(dir.exists())) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        if (file.exists()) {
            return file.getAbsolutePath();
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ico_launcher);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
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
        oks.setTitle(newsTitle);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(newsUrl);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(newsTitle);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(getSeetongImgPath());//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(newsUrl);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment(newsTitle + newsUrl);
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(newsUrl);

        // 启动分享GUI
        oks.show(this);
    }

    private void getData() {
        String xml = Global.getCommentListXML();
        data.clear();
        if (!xml.equals("")) {
            parseCommentXML(xml);
        }
    }

    private void parseCommentXML(String xml) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            Comment comment = null;
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("allpage")) {
                            allPage = Integer.parseInt(parser.nextText());
                            if (allPage == 1 || allPage == 0) {
                                commentPageLayout.setVisibility(View.GONE);
                                pageIndexView.setVisibility(View.GONE);
                            } else if (allPage > 1) {
                                commentPageLayout.setVisibility(View.VISIBLE);
                                pageIndexView.setVisibility(View.VISIBLE);
                                pageIndexView.setText((pageIndex + 1) + "/" + allPage);
                            }
                            //Log.e(TAG, "all page : " + allPage);
                        } else if (parser.getName().equals("allcount")) {
                            allCount = Integer.parseInt(parser.nextText());
                            //Log.e(TAG, "all count : " + allCount);
                        } else if (parser.getName().equals("ls")) {
                            comment = new Comment();
                        } else if (comment != null) {
                            if (parser.getName().equals("UserName")) {
                                comment.setUserName(parser.nextText());
                            } else if (parser.getName().equals("Msg")) {
                                comment.setCommentInfo(parser.nextText());
                            } else if (parser.getName().equals("AddTM")) {
                                comment.setCommentTime(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("ls") && comment != null) {
                            data.add(comment);
                            comment = null;
                        }
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_GET_SERVICE_MSG_COMMENT_LIST:
                /*LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                String xml = (String) msgObj.recvObj;
                Log.e(TAG, xml);*/
                getData();
                adapter.notifyDataSetChanged();
                break;
        }
    }
}

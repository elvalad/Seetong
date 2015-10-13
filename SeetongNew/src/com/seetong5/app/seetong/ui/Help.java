package com.seetong5.app.seetong.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.Tools;

import java.util.List;

/**
 * Created by Administrator on 2014-07-28.
 */
public class Help extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        initWidget();
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent.toString().contains("mailto:")) {
            PackageManager pm = getPackageManager();
            List<ResolveInfo> lst = pm.queryIntentActivities(intent, 0);
            if (null == lst || lst.size() == 0) {
                toast(R.string.not_app_execute_action);
                return;
            }
        }
        super.startActivity(intent);
    }

    private WebView mwvHelp;
    @SuppressLint("SetJavaScriptEnabled")
    public void initWidget() {
        mwvHelp = (WebView) findViewById(R.id.wvHelp);
        mwvHelp.getSettings().setJavaScriptEnabled(true);
        /**
         * 如果页面中链接，如果希望点击链接继续在当前browser中响应，<br>
         * 而不是新开Android的系统browser中响应该链接，<br>
         * 必须覆盖webview的WebViewClient对象<br>
         * */
        mwvHelp.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("mailto:")) {
                    Intent data=new Intent(Intent.ACTION_SENDTO);
                    data.setData(Uri.parse(url));
                    data.putExtra(Intent.EXTRA_SUBJECT, "意见反馈");
                    data.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(data);
                    return true;
                }

                view.loadUrl(url);
                return false;
            }

        });

        int flags = Tools.getLanguageTypes();
        switch(flags){
            case 0:
                mwvHelp.loadUrl("http://seetong.ox114.com/seetong/manual/zh_cn/index.htm");
                break;
            case 1:
                mwvHelp.loadUrl("http://seetong.ox114.com/seetong/manual/zh_cn/index.htm");//about_ch_TW
                break;
            case 2:
                mwvHelp.loadUrl("http://seetong.ox114.com/seetong/manual/en_us/index.htm");
                break;
            default:
                mwvHelp.loadUrl("http://seetong.ox114.com/seetong/manual/en_us/index.htm");
                break;
        }
    }
}
package com.seetong.app.seetong.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.model.News;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.SDK_CONSTANT;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/7/18.
 */
public class NewsActivity extends BaseActivity {

    private static final String TAG = NewsActivity.class.getName();
    private ImageButton newsBackButton;
    private ListView listView;
    private NewsListAdapter adapter;
    private List<News> newsData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
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
        Global.m_spu.saveSharedPreferences(Define.MAX_NEWS_ID, getMaxNewsId());
        LibImpl.getInstance().removeHandler(m_handler);
    }

    private void initWidget() {
        if (Global.getNewsListXML().equals("")) {
            LibImpl.getInstance().addHandler(m_handler);
            int ret = LibImpl.getInstance().getFuncLib().GetServiceMessage();
            if (0 != ret) {
                Log.d(TAG, "Get service message err : " + ret);
            }
        }

        newsBackButton = (ImageButton) findViewById(R.id.news_back);
        newsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsActivity.this.finish();
            }
        });

        listView = (ListView) findViewById(R.id.news_list);
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        getData();
        adapter = new NewsListAdapter(NewsActivity.this, newsData);
        listView.setAdapter(adapter);
    }

    private void getData() {
        String xml = Global.getNewsListXML();
        newsData.clear();
        parseNewsXML(xml);
    }

    private void parseNewsXML(String xml) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            News news = null;
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("ls")) {
                            news = new News();
                        } else if (news != null) {
                            if (parser.getName().equals("ID")) {
                                news.setNewsId(parser.nextText());
                            } else if (parser.getName().equals("Title")) {
                                news.setNewsTitle(parser.nextText());
                            } else if (parser.getName().equals("Url")) {
                                news.setNewsUrl(parser.nextText());
                            } else if (parser.getName().equals("AddTM")) {
                                news.setNewsTime(parser.nextText());
                            } else if (parser.getName().equals("Count")) {
                                news.setNewsCount(parser.nextText());
                            } else if (parser.getName().equals("Img")) {
                                news.setNewsImgUrl(parser.nextText());
                            } else if (parser.getName().equals("Tip")) {
                                news.setNewsTip(parser.nextText());
                            } else if (parser.getName().equals("gourl")) {
                                news.setNewsGoUrl(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("ls") && news != null) {
                            newsData.add(news);
                            news = null;
                        }
                        break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private int getMaxNewsId() {
        List<Integer> newsIdList = new ArrayList<>();
        for (News aNewsData : newsData) {
            newsIdList.add(Integer.parseInt(aNewsData.getNewsId()));
        }
        return Collections.max(newsIdList);
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_GET_SERVICE_MSG_LIST:
                /*LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                String xml = (String) msgObj.recvObj;
                Log.e(TAG, xml);*/
                getData();
                adapter.notifyDataSetChanged();
                break;
        }
    }
}

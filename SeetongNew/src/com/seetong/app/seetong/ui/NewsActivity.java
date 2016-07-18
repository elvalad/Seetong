package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.News;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import ipc.android.sdk.com.SDK_CONSTANT;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
        getData();
        adapter = new NewsListAdapter(NewsActivity.this, newsData);
        listView.setAdapter(adapter);

        Button testButton = (Button) findViewById(R.id.news_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LibImpl.getInstance().getFuncLib().GetServiceMessage();
            }
        });
    }

    private void getData() {
        String xml = Global.getNewsListXML();
        newsData.clear();
        Log.e(TAG, "--------------------------------\n" + xml);
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

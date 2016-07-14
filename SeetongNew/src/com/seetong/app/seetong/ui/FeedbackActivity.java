package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;

/**
 * Created by Administrator on 2016/7/7.
 */
public class FeedbackActivity extends BaseActivity {

    private String feedbackContact;
    private String feedbackContent;
    private ProgressDialog mTipDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initWidget();
    }

    private void initWidget() {
        mTipDlg = new ProgressDialog(this, R.string.feedback_is_submit);
        mTipDlg.setCancelable(false);

        ImageButton backButton = (ImageButton) findViewById(R.id.feedback_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackActivity.this.finish();
            }
        });

        ImageButton submitButton = (ImageButton) findViewById(R.id.feedback_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
    }

    private boolean getFormatData() {
        feedbackContact = gStr(R.id.feedback_contact);
        feedbackContent = gStr(R.id.feedback_content);

        if (isNullStr(feedbackContact)) {
            toast(R.string.feedback_contact_null);
            return false;
        }

        if (isNullStr(feedbackContent)) {
            toast(R.string.feedback_content_null);
            return false;
        }

        if (feedbackContent.length() > 256) {
            toast(R.string.feedback_content_too_long);
            return false;
        }

        return true;
    }

    private void onSubmit() {
        if (getFormatData()) {
            mTipDlg.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final int ret = LibImpl.getInstance().getFuncLib().AddFeedback(feedbackContent, feedbackContact);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTipDlg.dismiss();
                            if (ret == 0) {
                                toast(R.string.feedback_success);
                                FeedbackActivity.this.finish();
                            } else {
                                toast(R.string.feedback_failure);
                            }
                        }
                    });
                }
            }).start();
        }
    }
}

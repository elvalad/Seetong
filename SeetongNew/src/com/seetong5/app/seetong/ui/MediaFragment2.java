package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;

import java.io.File;


/**
 * MediaFragment 主要用于显示多媒体文件列表的 Fragment，它也是包含在 MainActivity 的 TabHost 中，
 * 它自身又嵌套包含两个子的 Fragment, 分别为 PictureFragment 和 VideoFragment 用于显示图片文件和
 * 录像文件.当用户点击不同的 Button 时会通过 getChildFragmentManager 显示不同的 Fragment.
 * 此 Fragment 默认显示 PictureFragment.
 *
 * Created by gmk on 2015/9/11.
 */
public class MediaFragment2 extends BaseFragment {
    private PictureFragment pictureFragment;
    private VideoFragment2 videoFragment;
    private BaseFragment currentFragment;
    private String currentFragmentName;
    private boolean choosenMode;
    private boolean bAllChoosed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setMediaFragment(this);
        View fragmentView = inflater.inflate(R.layout.media2, container);
        pictureFragment = PictureFragment.newInstance();
        videoFragment = VideoFragment2.newInstance();
        currentFragmentName = "picture";
        choosenMode = false;
        bAllChoosed = false;
        final LinearLayout layout = (LinearLayout) fragmentView.findViewById(R.id.media_title_layout);
        final Button picturebutton = (Button) fragmentView.findViewById(R.id.media_picture);
        final Button videoButton = (Button) fragmentView.findViewById(R.id.media_video);
        final Button editButton = (Button) fragmentView.findViewById(R.id.media_edit);
        final Button chooseAllButton = (Button) fragmentView.findViewById(R.id.media_choose_all);
        final ImageButton deleteButton = (ImageButton) fragmentView.findViewById(R.id.media_delete);
        picturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setBackgroundResource(R.drawable.tps_media_picture);
                picturebutton.setTextColor(getResources().getColor(R.color.white));
                videoButton.setTextColor(getResources().getColor(R.color.green));
                showPictureFragment();
                setCurrentFragment("picture");
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setBackgroundResource(R.drawable.tps_media_video);
                picturebutton.setTextColor(getResources().getColor(R.color.green));
                videoButton.setTextColor(getResources().getColor(R.color.white));
                showVideoFragment();
                setCurrentFragment("video");
            }
        });

        chooseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAllChoosed) {
                    if (currentFragmentName.equals("picture")) {
                        pictureFragment.setAllChoosed(false);
                    } else if (currentFragmentName.equals("video")) {
                        videoFragment.setAllChoosed(false);
                    }
                    chooseAllButton.setTextColor(getResources().getColor(R.color.green));
                    bAllChoosed = false;
                } else {
                    if (currentFragmentName.equals("picture")) {
                        pictureFragment.setAllChoosed(true);
                    } else if (currentFragmentName.equals("video")) {
                        videoFragment.setAllChoosed(true);
                    }
                    chooseAllButton.setTextColor(getResources().getColor(R.color.gray));
                    bAllChoosed = true;
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity2.m_this)
                        .setTitle(T(R.string.dlg_tip))
                        .setMessage(T(R.string.dlg_delete_picture_tip))
                        .setNegativeButton(T(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(T(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (currentFragmentName.equals("picture")) {
                                    pictureFragment.deleteChoosenItem();
                                } else if (currentFragmentName.equals("video")) {
                                    videoFragment.deleteChoosenItem();
                                }
                            }
                        }).create().show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (choosenMode) {
                    if (currentFragmentName.equals("picture")) {
                        pictureFragment.setChoosenMode(false);
                    } else if (currentFragmentName.equals("video")) {
                        videoFragment.setChoosenMode(false);
                    }
                    choosenMode = false;
                    layout.setVisibility(View.VISIBLE);
                    chooseAllButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                    editButton.setTextColor(getResources().getColor(R.color.green));
                } else {
                    if (currentFragmentName.equals("picture")) {
                        pictureFragment.setChoosenMode(true);
                    } else if (currentFragmentName.equals("video")) {
                        videoFragment.setChoosenMode(true);
                    }
                    choosenMode = true;
                    layout.setVisibility(View.GONE);
                    chooseAllButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                    editButton.setTextColor(getResources().getColor(R.color.gray));
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.media_fragment_container, pictureFragment)
                    .commit();
        }
    }

    private void setCurrentFragment(String name) {
        switch (name) {
            case "picture":
                currentFragment = pictureFragment;
                currentFragmentName = "picture";
                break;
            case "video":
                currentFragment = videoFragment;
                currentFragmentName = "video";
                break;
            default:
                currentFragment = pictureFragment;
                currentFragmentName = "picture";
                break;
        }
    }

    private void showPictureFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, pictureFragment)
                .commit();
    }

    private void showVideoFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, videoFragment)
                .commit();
    }

    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_SCREENSHOT_LIST:
                pictureFragment.updateScreenshotList();
                break;
        }
    }
}

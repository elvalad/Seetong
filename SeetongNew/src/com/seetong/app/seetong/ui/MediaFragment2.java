package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;

public class MediaFragment2 extends BaseFragment {
    private PictureFragment pictureFragment;
    private VideoFragment2 videoFragment;
    private BaseFragment currentFragment;
    private String currentFragmentName;
    private boolean choosenMode;
    private boolean bAllChoosed;

    private View fragmentView;
    private LinearLayout layout;
    private Button picturebutton;
    private Button videoButton;
    private Button editButton;
    private Button chooseAllButton;
    private ImageButton deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setMediaFragment(this);
        fragmentView = inflater.inflate(R.layout.media2, container);
        pictureFragment = PictureFragment.newInstance();
        videoFragment = VideoFragment2.newInstance();
        currentFragmentName = "picture";
        choosenMode = false;
        bAllChoosed = false;

        layout = (LinearLayout) fragmentView.findViewById(R.id.media_title_layout);
        picturebutton = (Button) fragmentView.findViewById(R.id.media_picture);
        videoButton = (Button) fragmentView.findViewById(R.id.media_video);
        editButton = (Button) fragmentView.findViewById(R.id.media_edit);
        chooseAllButton = (Button) fragmentView.findViewById(R.id.media_choose_all);
        deleteButton = (ImageButton) fragmentView.findViewById(R.id.media_delete);
        picturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureFragment();
                setCurrentFragment("picture");
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    chooseAllButton.setText(R.string.media_choose_all);
                    bAllChoosed = false;
                } else {
                    if (currentFragmentName.equals("picture")) {
                        pictureFragment.setAllChoosed(true);
                    } else if (currentFragmentName.equals("video")) {
                        videoFragment.setAllChoosed(true);
                    }
                    chooseAllButton.setText(R.string.media_choose_none);
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
                    editButton.setText(R.string.edit);
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
                    editButton.setText(R.string.media_edit_ok);
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
        layout.setBackgroundResource(R.drawable.tps_media_picture);
        picturebutton.setTextColor(getResources().getColor(R.color.white));
        videoButton.setTextColor(getResources().getColor(R.color.green));
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, pictureFragment)
                .commit();
    }

    private void showVideoFragment() {
        layout.setBackgroundResource(R.drawable.tps_media_video);
        picturebutton.setTextColor(getResources().getColor(R.color.green));
        videoButton.setTextColor(getResources().getColor(R.color.white));
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, videoFragment)
                .commit();
    }

    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_SCREENSHOT_LIST:
                pictureFragment.updateScreenshotList();
                break;
            case Define.MSG_SHOW_PICTURE_FRAGMENT:
                showPictureFragment();
                break;
        }
    }
}

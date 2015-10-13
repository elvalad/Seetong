package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.ComparatorFile;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.comm.Tools;
import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2014-07-28.
 */
public class VideoFile extends BaseActivity implements View.OnClickListener {
    private ExpandableListView m_listView;
    private VideoFileListAdapter m_adapter;
    private Map<String, List<File>> m_lstData = new HashMap<>();

    private Button m_btn_right;
    private PopupWindow m_menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_file);

        m_btn_right = (Button) findViewById(R.id.btn_title_right);
        m_btn_right.setText(R.string.more);
        m_btn_right.setVisibility(View.VISIBLE);
        m_btn_right.setOnClickListener(this);

        View menu = getLayoutInflater().inflate(R.layout.video_file_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_share).setOnClickListener(this);
        menu.findViewById(R.id.btn_delete).setOnClickListener(this);

        initVideoFile(Global.getVideoDir());

        m_adapter = new VideoFileListAdapter(this);
        m_listView = (ExpandableListView) findViewById(R.id.lv_video_file_list);
        m_listView.setGroupIndicator(null);
        m_listView.setAdapter(m_adapter);
        m_listView.expandGroup(0);

        m_adapter.setListData(m_lstData);
    }

    private void initVideoFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;

        File[] oldFileAry = file.listFiles(Tools.getFilenameFilter(Define.VideoExts, false));
        if (oldFileAry.length > 0) {
            for (File f : oldFileAry) {
                String ary[] = f.getName().split("-");
                String devId = ary[0];

                String strDir = Global.getVideoDir() + "/" + devId;
                File dir = new File(strDir);
                if (!(dir.exists())) {
                    dir.mkdirs();
                }

                File newFile = new File(strDir + "/" + f.getName());
                f.renameTo(newFile);
            }
        }

        File[] dirAry = file.listFiles(Tools.getFilenameFilter(Define.VideoExts));
        if (dirAry.length <= 0) {
            m_btn_right.setVisibility(View.GONE);
            toast(R.string.no_any_video_in_directory);
            return;
        }

        m_btn_right.setVisibility(View.VISIBLE);
        for (File dir : dirAry) {
            List<File> lstFile = new ArrayList<>();
            File[] fileAry = dir.listFiles(Tools.getFilenameFilter(Define.VideoExts));
            if (null == fileAry || fileAry.length <= 0) {
                m_lstData.put(dir.getName(), lstFile);
                return;
            }

            for (File f : fileAry) {
                if (f.isFile() && !f.isHidden()) {
                    lstFile.add(f);
                }
            }

            ComparatorFile cmpFile = new ComparatorFile();
            Collections.sort(lstFile, cmpFile);
            m_lstData.put(dir.getName(), lstFile);
        }
    }

    @Override
    public void onBackPressed() {
        if (m_adapter.isEdit()) {
            m_adapter.setEdit(false);
            m_adapter.notifyDataSetChanged();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_right:
                onBtnTitleRight(v);
                break;
            case R.id.btn_share:
                m_menu.dismiss();
                onBtnShare(v);
                break;
            case R.id.btn_delete:
                m_menu.dismiss();
                onBtnDelete(v);
                break;
            default:
        }
    }

    private void onBtnDelete(final View v) {
        /*new AlertDialog.Builder(this)
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
                        deleteImg();
                    }
                }).create().show();*/
        m_adapter.setEdit(true);
        m_adapter.notifyDataSetChanged();
    }

    private void onBtnShare(View v) {

    }

    private void onBtnTitleRight(View v) {
        if (m_menu.isShowing()) {
            m_menu.dismiss();
        } else {
            m_menu.showAsDropDown(v);
            m_menu.setBackgroundDrawable(new BitmapDrawable(null, (Bitmap)null));
            m_menu.setOutsideTouchable(true);
            m_menu.dismiss();
            m_menu.showAsDropDown(v);
            m_menu.setBackgroundDrawable(new BitmapDrawable(null, (Bitmap)null));
            m_menu.setOutsideTouchable(true);
        }
    }

    private class VideoFileListAdapter extends BaseExpandableListAdapter {
        Context m_ctx;
        private LayoutInflater m_inflater;
        private Map<String, List<File>> m_data;
        private boolean m_isEdit;

        public VideoFileListAdapter(Context ctx) {
            m_ctx = ctx;
            m_inflater = LayoutInflater.from(ctx);
            m_data = new HashMap<>();

        }

        public void setListData(Map<String, List<File>> data) {
            m_data = data;
            this.notifyDataSetChanged();
        }

        public void setEdit(boolean b) {
            m_isEdit = b;
        }

        public boolean isEdit() {
            return m_isEdit;
        }

        private class GroupViewHolder {
            public TextView tvGroupName;
            public ImageView imgGroupIco;
            public Button btnDelete;
        }

        private class ChildViewHolder {
            public Button btnDelete;
            public ImageView imgView;
            public TextView tvCaption;
            public TextView tvInfo;
            public Button btnShare;
        }

        @Override
        public int getGroupCount() {
            return m_data.size() ;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (m_data.isEmpty()) return 0;
            String key = (String) m_data.keySet().toArray()[groupPosition];
            int size = m_data.get(key).size();
            return size;
        }

        @Override
        public Object getGroup(int groupPosition) {
            String key = (String) m_data.keySet().toArray()[groupPosition];
            return key;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            String key = (String) m_data.keySet().toArray()[groupPosition];
            return m_data.get(key).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return getGroupCount() > 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View v, ViewGroup parent) {
            GroupViewHolder viewHolder;
            if (null == v) {
                v = m_inflater.inflate(R.layout.video_file_group_item, parent, false);
                viewHolder = new GroupViewHolder();
                viewHolder.btnDelete = (Button) v.findViewById(R.id.btnDelete);
                viewHolder.tvGroupName = (TextView) v.findViewById(R.id.tvGroupName);
                viewHolder.imgGroupIco = (ImageView) v.findViewById(R.id.imgGroupIco);
                v.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) v.getTag();
            }

            final int pos = groupPosition;
            final String key = (String) getGroup(groupPosition);
            viewHolder.tvGroupName.setText(key);

            int show = m_isEdit ? View.VISIBLE : View.GONE;
            viewHolder.btnDelete.setVisibility(show);
            viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteDir(pos, key);
                }
            });

            return v;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent) {
            final int gpos = groupPosition;
            final int cpos = childPosition;
            ChildViewHolder viewHolder = null;
            if (null == v) {
                v = m_inflater.inflate(R.layout.video_file_item, parent, false);
                viewHolder = new ChildViewHolder();
                viewHolder.btnDelete = (Button) v.findViewById(R.id.btnDelete);
                viewHolder.imgView = (ImageView) v.findViewById(R.id.img);
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                viewHolder.tvInfo = (TextView) v.findViewById(R.id.tvInfo);
                viewHolder.btnShare = (Button) v.findViewById(R.id.btnShare);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) v.getTag();
            }

            final File file = (File) getChild(groupPosition, childPosition);
            if (null == file) return v;

            String fileName = file.getName();
            viewHolder.tvCaption.setText(fileName);

// 显示文件修改日期与大小
            String strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
            String strSize = Tools.getStringByLength(file.length());
            viewHolder.tvInfo.setText(strDate + "/" + strSize);

            FinalBitmap fb = FinalBitmap.create(m_ctx);
            fb.configLoadingImage(R.drawable.video_thumb);
//			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), 1);
//			bitmap = (bitmap == null) ? BitmapFactory.decodeResource(mResources, R.drawable.video_thumb): bitmap;
//			viewHolder.imgView.setImageBitmap(bitmap);
            fb.display(viewHolder.imgView, "");

            // 显示、隐藏删除图标
            int show = m_isEdit ? View.VISIBLE : View.GONE;
            viewHolder.btnDelete.setVisibility(show);
            viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFile(gpos, cpos, file);
                }
            });

            viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadToYouku(m_data.get((String)getGroup(gpos)).get(cpos).getPath());
                }
            });

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playFile(file);
                }
            });

            return v;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private void onDeleteDir(int pos, final String devId) {
            new AlertDialog.Builder(m_ctx)
                    .setTitle(devId)
                    .setMessage(R.string.delete_device_all_video_tip)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //String curDir = file.getParentFile().getAbsolutePath();
                            File file = new File(Global.getVideoDir() + "/" + devId);
                            boolean isOK = Tools.deleteDirectory(file.getAbsolutePath());
                            if (isOK){
                                Toast.makeText(m_ctx, R.string.delete_local_video_success, Toast.LENGTH_SHORT).show();
                                //setDirectory(curDir);
                                m_data.remove(devId);
                                if (m_data.isEmpty()) setEdit(false);
                                notifyDataSetChanged();
                            }else {
                                Toast.makeText(m_ctx, R.string.delete_local_video_fail, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }

        private void deleteFile(final int gpos, final int cpos, final File file) {
            new AlertDialog.Builder(m_ctx)
                    .setTitle(file.getName())
                    .setMessage(R.string.delete_local_video_tip)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //String curDir = file.getParentFile().getAbsolutePath();
                            boolean isOK = file.delete();
                            if (isOK){
                                Toast.makeText(m_ctx, R.string.delete_local_video_success, Toast.LENGTH_SHORT).show();
                                //setDirectory(curDir);
                                String key = (String) m_data.keySet().toArray()[gpos];
                                List<File> lst = m_data.get(key);
                                m_data.get(key).remove(cpos);
                                if (lst.size() < 1){
                                    //mBtnRight.setVisibility(View.GONE);
                                }
                                notifyDataSetChanged();
                            }else {
                                Toast.makeText(m_ctx, R.string.delete_local_video_fail, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }

        private void playFile(File file) {
            if (!m_isEdit) {
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("file://" + file.getAbsolutePath());
                    it.setDataAndType(uri, "video/mp4");//mp4
                    m_ctx.startActivity(it);
                } catch (ActivityNotFoundException e) {
                    MainActivity.m_this.toast(R.string.not_open_file_use_third_party_app);
                }
            }
        }
    }
}
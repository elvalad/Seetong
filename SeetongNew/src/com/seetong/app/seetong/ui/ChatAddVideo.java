package com.seetong.app.seetong.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.view.*;
import android.widget.*;
import com.android.opengles.OpenglesRender;
import com.android.system.MediaPlayer;
import com.seetong.app.seetong.Config;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.ComparatorFile;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.Tools;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import ipc.android.sdk.com.Device;

import java.io.File;
import java.util.*;

/**
 * Created by Administrator on 2014-07-28.
 */
public class ChatAddVideo extends BaseActivity implements View.OnClickListener {
    private Button m_btn_right;
    private PopupWindow m_menu;
    boolean m_is_layout_land = false;
    GridView m_grid;
    ImageAdapter m_adapter;
    ArrayList<String> m_shareTimeAry = new ArrayList<>();
    ArrayAdapter<String> m_shareTimeAdapter;
    int m_share_time_value[] = {3, 5, 10, 30, 0};
    int m_share_time = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_add_video);

        m_btn_right = (Button) findViewById(R.id.btn_title_right);
        m_btn_right.setText(R.string.more);
        m_btn_right.setVisibility(View.GONE);
        m_btn_right.setOnClickListener(this);

        String[] ls = getResources().getStringArray(R.array.string_ary_share_video_time);
        Collections.addAll(m_shareTimeAry, ls);

        m_shareTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_shareTimeAry);
        m_shareTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner cbxShareTime = (Spinner) findViewById(R.id.sp_share_time);
        cbxShareTime.setAdapter(m_shareTimeAdapter);
        cbxShareTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                m_share_time = m_share_time_value[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cbxShareTime.setSelection(0);

        m_grid = (GridView) findViewById(R.id.grid_video);
        m_adapter = new ImageAdapter(this);
        m_grid.setAdapter(m_adapter);
        m_grid.setOnItemClickListener(m_adapter);

       /* View menu = getLayoutInflater().inflate(R.layout.image_file_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_share).setOnClickListener(this);
        menu.findViewById(R.id.btn_delete).setOnClickListener(this);*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            m_is_layout_land = true;
            findViewById(R.id.layout_title).setVisibility(View.GONE);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            m_is_layout_land = false;
            findViewById(R.id.layout_title).setVisibility(View.VISIBLE);
        }
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
            default: break;
        }
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

    private void addDeviceToChat(PlayerDevice dev) {
        if (null == dev) return;
        Intent it = new Intent(this, ChatMessage.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, dev.m_dev.getDevId());
        it.putExtra(Constant.EXTRA_CHAT_SHARE_VIDEO_TIME, m_share_time);
        setResult(Activity.RESULT_OK, it);
        finish();
    }

    class ImageAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
    {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<PlayerDevice> m_data = Global.getSelfDeviceList();

        public class ViewHolder {
            public ImageView imgView;
            public TextView tvCaption;
        }

        public ImageAdapter(Context ctx)
        {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            Global.sortDeviceListByGroupName(m_data);
        }

        // 获取图片的个数
        public int getCount()
        {
            return m_data.size();
        }

        // 获取图片在库中的位置
        public Object getItem(int position)
        {
            return position;
        }


        // 获取图片ID
        public long getItemId(int position)
        {
            return position;
        }


        public View getView(int position, View v, ViewGroup parent)
        {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.chat_add_video_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imgView = (ImageView) v.findViewById(R.id.btn_image);
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.txt_dev_id);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            final PlayerDevice dev = m_data.get(position);
            if (null == dev) return v;

            String devId = dev.m_dev.getDevId();
            viewHolder.tvCaption.setText(devId);
            String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
            Bitmap bmp = null;
            try {
                bmp = BitmapFactory.decodeFile(fileName);
                if (null == bmp) bmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
            } catch (OutOfMemoryError err) {
                // err.printStackTrace();
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
            }

            if (null != bmp) viewHolder.imgView.setImageBitmap(bmp);
            viewHolder.imgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    addDeviceToChat(dev);
                }
            });

            boolean isOnline = dev.m_dev.getOnLine() != Device.OFFLINE;
            viewHolder.tvCaption.setTextColor(isOnline ? getResources().getColor(R.color.txt_device_name) : Color.GRAY);

            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            PlayerDevice dev = m_data.get(pos);
            addDeviceToChat(dev);
        }
    }
}
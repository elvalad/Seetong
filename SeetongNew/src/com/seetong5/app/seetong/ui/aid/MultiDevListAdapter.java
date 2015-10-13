package com.seetong5.app.seetong.ui.aid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.text.InputFilter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.MainActivity;
import com.seetong5.app.seetong.ui.ext.MyTipDialog;
import com.seetong5.app.seetong.ui.utils.DeviceSortUtil;
import ipc.android.sdk.com.Device;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author qinglei.yin@192.168.88.9<br>
 *         2013-12-9 下午3:25:50<br>
 * @declaration 多级设备列表项适配器
 */
public class MultiDevListAdapter extends BaseExpandableListAdapter /*implements OnChildClickListener*/ {
    private MyGestureDetector m_gd;
    private MySimpleOnGestureListener m_sogl = new MySimpleOnGestureListener();

    public class MyOnTouchListener implements View.OnTouchListener {
        private PlayerDevice m_dev;
        public MyOnTouchListener(PlayerDevice dev) {
            m_dev = dev;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (m_sogl.onUp(event)) return m_gd.onTouch(m_dev, v, event);
                addDeviceToLive(m_dev);
            }
            return m_gd.onTouch(m_dev, v, event);
        }
    }

    public class MyGestureDetector extends GestureDetector {
        public View m_view;
        public PlayerDevice m_dev;
        public MyGestureDetector(Context context, OnGestureListener listener) {
            super(context, listener);
        }

        public MyGestureDetector(Context context, OnGestureListener listener, Handler handler) {
            super(context, listener, handler);
        }

        public MyGestureDetector(Context context, OnGestureListener listener, Handler handler, boolean unused) {
            super(context, listener, handler, unused);
        }

        public boolean onTouch(PlayerDevice dev, View v, MotionEvent e) {
            m_dev = dev;
            m_view = v;
            return onTouchEvent(e);
        }
    }

    public class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        boolean m_move = false;
        @Override
        public boolean onDown(MotionEvent e) {
            m_move = false;
            return super.onDown(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            m_move = true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (m_move) return true;
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            m_move = true;
            if (!m_gd.m_dev.isNVR() && Global.m_loginType != Define.LOGIN_TYPE_DEMO) {
                ChildViewHolder v = (ChildViewHolder) m_gd.m_view.getTag();
                if (e1.getX() - e2.getX() > 30) {
                    if (v.imgDelete.getVisibility() == View.GONE) {
                        v.imgDelete.setVisibility(View.VISIBLE);
                        v.btnConfig.setVisibility(View.GONE);
                        m_gd.m_dev.m_del_mode = true;
                        return true;
                    }
                } else if (e2.getX() - e1.getX() > 30) {
                    if (v.imgDelete.getVisibility() == View.VISIBLE) {
                        v.btnConfig.setVisibility(View.VISIBLE);
                        v.imgDelete.setVisibility(View.GONE);
                        m_gd.m_dev.m_del_mode = false;
                        return true;
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (m_move) return true;
            return super.onSingleTapConfirmed(e);
        }

        public boolean onUp(MotionEvent event) {
            return m_move;
        }
    }

    public class GroupViewHolder {
        public TextView tvGroupName;
        public ImageView imgGroupIco;
        public ImageView imgDelete;
    }

    public class ChildViewHolder {
        public TextView tvDeviceName;
        public TextView tvDeviceNo;
        public ImageView imgDelete;
        public ImageView imgCheck;
        public Button btnConfig;
    }

    public class GroupKey {
        int m_type;
        String m_key;
        String m_groupName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GroupKey groupKey = (GroupKey) o;

            if (m_type != groupKey.m_type) return false;
            if (!m_groupName.equals(groupKey.m_groupName)) return false;
            if (!m_key.equals(groupKey.m_key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = m_type;
            result = 31 * result + m_key.hashCode();
            result = 31 * result + m_groupName.hashCode();
            return result;
        }
    }

    private MainActivity mActivity;
    //private Map<DevGroup> mDeviceMapData = new ArrayList<DevGroup>();
    private Map<String, List<PlayerDevice>> mDeviceMapData = new HashMap<>();
    private List<GroupKey> mGroupKeys = new ArrayList<GroupKey>();
    private LayoutInflater mInflater;
//	private boolean mIsDefaultUnfolded = true;

    public static long m_sel_group_pos = ExpandableListView.PACKED_POSITION_VALUE_NULL;
    public static long m_sel_child_pos = ExpandableListView.PACKED_POSITION_VALUE_NULL;

    public MultiDevListAdapter(MainActivity context) {
        mActivity = context;
        mInflater = LayoutInflater.from(mActivity);

        m_gd = new MyGestureDetector(context, m_sogl);
    }

    public void setData(List<PlayerDevice> datas) {
        mDeviceMapData.clear();
        mGroupKeys.clear();
        if ((datas != null) && (datas.size() > 0)) {
            //Global.sortDeviceListByGroupName(datas);
            for (PlayerDevice dev : datas) {
                GroupKey key = getGroupKey(dev);
                List<PlayerDevice> lstDevs = mDeviceMapData.get(key.m_key);
                if (lstDevs == null) {
                    lstDevs = new ArrayList<PlayerDevice>();
                    lstDevs.add(dev);
                    mDeviceMapData.put(key.m_key, lstDevs);
                    mGroupKeys.add(key);
                    continue;
                }
                lstDevs.add(dev);
            }

            DeviceSortUtil devSortUtil = new DeviceSortUtil();
            for (GroupKey key : mGroupKeys) {
                List<PlayerDevice> devs = mDeviceMapData.get(key.m_key);
                Collections.sort(devs, devSortUtil);
            }
        }

        notifyDataSetChanged();
    }

    public void myNotifyDataSetChanged(List<PlayerDevice> datas, ExpandableListView expandListView) {
        if ((datas != null) && (datas.size() > 0)) {
            int size = datas.size();
            mDeviceMapData.clear();
            mGroupKeys.clear();
            for (int i = 0; i < size; i++) {
                PlayerDevice dev = datas.get(i);
                GroupKey key = getGroupKey(dev);
                List<PlayerDevice> lstDevs = mDeviceMapData.get(key.m_key);
                if (lstDevs == null) {
                    lstDevs = new ArrayList<PlayerDevice>();
                    lstDevs.add(dev);
                    mDeviceMapData.put(key.m_key, lstDevs);
                    mGroupKeys.add(key);
                    continue;
                }
                lstDevs.add(dev);
            }
//			if(expandListView != null){
//				//将所有项设置成默认展开
//				int groupCount = expandListView.getCount();
//				for (int i = 0; i < groupCount && SDK_CONSTANT.IS_DEVICE_LIST_EXPAND; i++) {
//					expandListView.expandGroup(i);
//				}
//			}
        } else {
            mDeviceMapData.clear();
            mGroupKeys.clear();
        }

        super.notifyDataSetChanged();
    }

    public GroupKey getGroupKey(PlayerDevice dev) {
        GroupKey key = new GroupKey();
        key.m_type = dev.m_dev.getDevType();
        key.m_groupName = dev.m_dev.getDevGroupName();
        if (dev.m_dev.getDevType() == 100) key.m_key = dev.m_dev.getDevGroupName();
        if (dev.m_dev.getDevType() == 200) key.m_key = dev.m_dev.getDevName() + "&" + dev.m_dev.getDevGroupName();
        return key;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mDeviceMapData.get(mGroupKeys.get(groupPosition).m_key).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPos, final int childPos, boolean isLastChild, View v, ViewGroup parent) {
        final PlayerDevice dev = (PlayerDevice) getChild(groupPos, childPos);
        if (null == dev) return v;

        ChildViewHolder viewHolder;
        if (v == null) {
            v = mInflater.inflate(R.layout.device_list_item, parent, false);
            viewHolder = new ChildViewHolder();
            viewHolder.tvDeviceName = (TextView) v.findViewById(R.id.tvDeviceName);
            viewHolder.tvDeviceNo = (TextView) v.findViewById(R.id.tvDeviceNo);
            viewHolder.imgDelete = (ImageView) v.findViewById(R.id.imgDelete);
            viewHolder.imgCheck = (ImageView) v.findViewById(R.id.imgCheck);
            viewHolder.btnConfig = (Button) v.findViewById(R.id.btnConfig);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) v.getTag();
        }

        v.setOnClickListener(new OnClickListener() {//item单击
            @Override
            public void onClick(View v) {
                //addDeviceToLive(dev);
                m_sel_group_pos = groupPos;
                m_sel_child_pos = childPos;
            }
        });

        //v.setLongClickable(true);
        v.setOnTouchListener(new MyOnTouchListener(dev));

        String devId = dev.m_dev.getDevId();
        if (dev.m_del_mode) {
            viewHolder.imgDelete.setVisibility(View.VISIBLE);
            viewHolder.btnConfig.setVisibility(View.GONE);
        } else {
            viewHolder.imgDelete.setVisibility(View.GONE);
            viewHolder.btnConfig.setVisibility(View.VISIBLE);
        }

        //加载别名
        String _devName = LibImpl.getInstance().getDeviceAlias(dev.m_dev);
        viewHolder.tvDeviceName.setText(_devName);
        viewHolder.tvDeviceNo.setText(dev.m_dev.getDevId());
        viewHolder.btnConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeviceConfig(dev.m_dev);
            }
        });
        viewHolder.imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PlayerDevice> devs = new ArrayList<PlayerDevice>();
                devs.add(dev);
                delDevice(devs);
            }
        });

        String fileName = Global.getSnapshotDir() + "/" + devId + ".jpg";
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(fileName);
            if (null == bmp) bmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.camera);
        } catch (OutOfMemoryError err) {
            // err.printStackTrace();
            bmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.camera);
        }

        if (null != bmp) viewHolder.imgCheck.setImageBitmap(bmp);

        boolean isOnline = dev.m_dev.getOnLine() != Device.OFFLINE;
        viewHolder.tvDeviceName.setTextColor(isOnline ? mActivity.getResources().getColor(R.color.txt_device_name) : Color.GRAY);
        viewHolder.tvDeviceNo.setTextColor(isOnline ? mActivity.getResources().getColor(R.color.txt_device_no) : Color.GRAY);
        if (dev.m_playing && isOnline) {
            viewHolder.tvDeviceName.setTextColor(Color.RED);
            viewHolder.tvDeviceNo.setTextColor(Color.RED);
        }

        int totle_Num = getChildrenCount(groupPos);
        int current_Id = childPos;
        v.setFocusable(false);
        // 只有一项
        if (totle_Num == 1) {
            v.setBackgroundResource(R.drawable.default_selector);
            return v;
        }
        // 第一项
        else if (current_Id == 0) {
            v.setBackgroundResource(R.drawable.list_top_selector);
        }
        // 最后一项
        else if (current_Id == totle_Num - 1) {
            v.setBackgroundResource(R.drawable.list_bottom_selector);
        } else {
            v.setBackgroundResource(R.drawable.list_center_selector);
        }
        return v;
    }

    private void modifyNvrAlias(final PlayerDevice dev) {
        final String _devName = dev.m_dev.getDevGroupName();
        Context context = mActivity;
        Resources mResources = mActivity.getResources();
        final ClearEditText etAddGroup = new ClearEditText(context);
        etAddGroup.setHint(R.string.dev_list_hint_input_dev_alias);
        etAddGroup.setPadding(10, 10, 10, 10);
        etAddGroup.setSingleLine(true);
        etAddGroup.setText(_devName);
        etAddGroup.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});
        new AlertDialog.Builder(context).setTitle(R.string.dev_list_tip_title_input_dev_alias)
                .setView(etAddGroup)
                .setNegativeButton(mResources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        mActivity.hideInputPanel(etAddGroup);
                        dialog.dismiss();
                    }
                }).setPositiveButton(mResources.getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = etAddGroup.getText().toString();
                if ("".equals(value)) {
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                int enterTypes = Global.m_loginType;
                int ret = LibImpl.getInstance().saveDeviceAlias(dev.m_dev.getDevId(), value, enterTypes);
                if (ret != 0) {
                    MainActivity.m_this.toast(ConstantImpl.getModifyDevNameErrText(ret));
                    return;
                }

                mActivity.hideInputPanel(etAddGroup);
                GroupKey key = getGroupKey(dev);
                List<PlayerDevice> listDev = mDeviceMapData.remove(key.m_key);
                for (PlayerDevice aListDev : listDev) {
                    aListDev.m_dev.setDevGroupName(value);
                }

                int index = mGroupKeys.indexOf(key);
                key = getGroupKey(dev);
                mDeviceMapData.put(key.m_key, listDev);
                mGroupKeys.set(index, key);
                updateDeviceList();
                dialog.dismiss();
            }
        }).create().show();
    }

    private void onDeviceConfig(Device dev) {
        mActivity.getDeviceFragment().onDeviceConfig(dev);
    }

    public void updateDeviceAlias(String devId, String alias) {
        PlayerDevice dev = Global.getDeviceById(devId);
        if (null == dev) return;
        if (dev.m_dev.getDevType() == 100) {
            dev.m_dev.setDevName(alias);
        } else if (dev.m_dev.getDevType() == 200) {
            GroupKey key = getGroupKey(dev);
            List<PlayerDevice> listDev = mDeviceMapData.remove(key.m_key);
            for (PlayerDevice aListDev : listDev) {
                aListDev.m_dev.setDevGroupName(alias);
            }

            int index = mGroupKeys.indexOf(key);
            key = getGroupKey(dev);
            mDeviceMapData.put(key.m_key, listDev);
            mGroupKeys.set(index, key);
        }

        updateDeviceList();
    }

    public void updateDeviceList() {
        notifyDataSetChanged();
        /*List<PlayerDevice> lstDev = new ArrayList<>();
        Set<String> keys = mDeviceMapData.keySet();
        for (String key : keys) {
            lstDev.addAll(mDeviceMapData.get(key));
        }

        //Global.sortDeviceListByGroupName(lstDev);
        LibImpl.setDeviceList(lstDev);*/
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition >= mGroupKeys.size()) return 0;
        return (mDeviceMapData.get(mGroupKeys.get(groupPosition).m_key) == null) ? 0
                : mDeviceMapData.get(mGroupKeys.get(groupPosition).m_key).size();
    }

    public int getOnlineChildrenCount(int groupPosition) {
        int onlineNums = 0;
        if (mDeviceMapData.get(mGroupKeys.get(groupPosition).m_key) != null) {
            List<PlayerDevice> lstDev = mDeviceMapData.get(mGroupKeys.get(groupPosition).m_key);
            for (PlayerDevice dev : lstDev) {
                if ((dev != null) && (dev.m_dev.getOnLine() != Device.OFFLINE)) {
                    onlineNums++;
                }
            }
        }
        return onlineNums;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDeviceMapData.get(mGroupKeys.get(groupPosition).m_key);
    }

    @Override
    public int getGroupCount() {
        return mGroupKeys.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int gPos, boolean isExpanded, View v, ViewGroup parent) {
        GroupViewHolder viewHolder;
        if (v == null) {
            v = mInflater.inflate(R.layout.device_list_group_item, parent, false);
            viewHolder = new GroupViewHolder();
            viewHolder.tvGroupName = (TextView) v.findViewById(R.id.tvGroupName);
            viewHolder.imgGroupIco = (ImageView) v.findViewById(R.id.imgGroupIco);
            viewHolder.imgDelete = (ImageView) v.findViewById(R.id.imgDelete);
            v.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) v.getTag();
        }

        if (Global.m_loginType == Define.LOGIN_TYPE_DEMO) {
            viewHolder.imgDelete.setVisibility(View.GONE);
        }

        /*v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return m_gd.onTouch(v, event);
            }
        });*/

        final List<PlayerDevice> lstDev = (List<PlayerDevice>) getGroup(gPos);
        if ((lstDev != null) && (lstDev.size() > 0)) {
            int childSize = getChildrenCount(gPos);
            GroupKey key = mGroupKeys.get(gPos);
            viewHolder.tvGroupName.setText(key.m_groupName + " [" + getOnlineChildrenCount(gPos) + "/" + childSize + "]");
            if (key.m_type == 200) {//NVR
                if (childSize == 4) {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_4);
                } else if (childSize == 8) {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_8);
                } else if (childSize == 9) {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_9);
                } else if (childSize == 16) {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_16);
                } else if (childSize == 24) {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_24);
                } else if (childSize == 32) {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_32);
                } else {
                    viewHolder.imgGroupIco.setImageResource(R.drawable.dvr_0);
                }
            } else {
                viewHolder.imgGroupIco.setImageResource(R.drawable.camera);
            }


            viewHolder.imgDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyTipDialog.popDialog(mActivity, R.string.prompt_delete_confirm, R.string.sure, R.string.cancel,
                            new MyTipDialog.IDialogMethod() {
                                @Override
                                public void sure() {
                                    delDevice(lstDev);
                                }
                            }
                    );
                }
            });
        }
        return v;
    }

    @Override
    public boolean hasStableIds() {
        return getGroupCount() > 0;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private boolean addDeviceToLive(PlayerDevice dev) {
        if (null == dev) return false;
        MainActivity.m_this.addDeviceToLive(dev);
        /*int AddLiveID = activity.getIntent().getIntExtra(VideoUI.ADD_LIVE_KEY, 0);
        if (AddLiveID == VideoUI.ADD_LIVE_ID) {
            activity.setResult(Activity.RESULT_OK, it);
        } else {
            activity.startActivity(it);
        }*/

        return true;
    }

    private void delDevice(List<PlayerDevice> devs) {
        MainActivity.m_this.delDevice(devs);
    }
}
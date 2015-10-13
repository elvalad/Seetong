package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.seetong5.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.model.FriendList;
import com.seetong5.app.seetong.model.FriendMessageList;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.ui.ext.RegexpEditText;
import ipc.android.sdk.com.SDK_CONSTANT;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2014-05-12.
 */
public class FriendFragment extends BaseFragment implements View.OnClickListener {
    private View m_view;
    private ListView m_listView;
    private ListViewAdapter m_adapter;
    private PullToRefreshListView mPullRefreshListView;
    private Map<String, FriendList.Friend> m_lstFriend = new HashMap<>();
    private Map<String, FriendList.Friend> m_lstPending = new HashMap<>();
    private Map<String, FriendList.Friend> m_lstReqPending = new HashMap<>();

    private ProgressDialog mTipDlg;
    private Button m_btn_right;
    private PopupWindow m_menu;

    private int m_queryCount = 30;
    private int m_queryPage = 0;
    private String m_queryStartTime = "";
    private int m_newMsgCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.m_this.setFriendFragment(this);
        m_view = inflater.inflate(R.layout.friend, container);

        mTipDlg = new ProgressDialog(m_view.getContext(), "");
        mTipDlg.setCancelable(false);

        //m_listView = (ListView) m_view.findViewById(R.id.lv_friend);
        mPullRefreshListView = (PullToRefreshListView) m_view.findViewById(R.id.lv_friend);

        // Set a listener to be invoked when the list should be refreshed.
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(Global.m_ctx, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                m_queryPage = 0;
                loadData();
            }
        });

        // Add an end-of-list listener
        /*mPullRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                Toast.makeText(Global.m_ctx, "End of List!", Toast.LENGTH_SHORT).show();
            }
        });*/

        m_listView = mPullRefreshListView.getRefreshableView();
        // Need to use the Actual ListView when registering for Context Menu
        //registerForContextMenu(m_listView);

        m_adapter = new ListViewAdapter(m_listView.getContext());
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(m_adapter);

        m_btn_right = (Button) m_view.findViewById(R.id.btn_title_right);
        m_btn_right.setText(R.string.more);
        m_btn_right.setVisibility(View.VISIBLE);
        m_btn_right.setOnClickListener(this);

        if (Global.m_loginType != Define.LOGIN_TYPE_USER) {
            m_btn_right.setVisibility(View.GONE);
        }

        View menu = this.getActivity().getLayoutInflater().inflate(R.layout.friend_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_add).setOnClickListener(this);
        menu.findViewById(R.id.btn_delete).setOnClickListener(this);
        return m_view;
    }

    public void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LibImpl.getInstance().getFuncLib().GetFriendList(0, 500);
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public void updateList() {
        m_lstFriend.clear();
        m_lstFriend.putAll(m_lstReqPending);
        m_lstFriend.putAll(m_lstPending);
        m_lstPending.clear();
        m_lstFriend.putAll(Global.m_friends.m_lstFriend);
        m_adapter.notifyDataSetChanged();
        MainActivity.m_this.setFriendPromptIcon(Global.m_friends.getNewMsgCount());
    }

    public void setNewMsgCount(int count) {
        m_newMsgCount = count;
        if (m_newMsgCount < 0) m_newMsgCount = 0;
        MainActivity.m_this.setFriendPromptIcon(m_newMsgCount);
    }

    public void decNewMsgCount(int count) {
        m_newMsgCount -= count;
        if (m_newMsgCount < 0) m_newMsgCount = 0;
        MainActivity.m_this.setFriendPromptIcon(m_newMsgCount);
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_GET_FRIEND_LIST:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FriendMessageList.Message msg = FriendMessageList.Message.findByLastReceiver();
                        if (null != msg) m_queryStartTime = msg.m_time;
                        LibImpl.getInstance().getFuncLib().ReadOfflineMsg("-1", m_queryPage, m_queryCount, m_queryStartTime, "");
                    }
                }).start();
                return true;
            case SDK_CONSTANT.TPS_MSG_RSP_GET_OFFLINE_MSG:
                onRspGetOfflineMsg();
                return true;
        }

        return false;
    }

    private void onRspGetOfflineMsg() {
        if (Global.m_messges.getMessageList().isEmpty()) {
            updateList();
            mPullRefreshListView.onRefreshComplete();
            return;
        }

        for (FriendMessageList.Message msg : Global.m_messges.getMessageList()) {
            if (msg.m_type.equals("1")) {
                String ary[] = msg.m_msg.split("\n");
                String pName = ary[0];
                String sid = ary[1].split("sid=")[1];
                FriendList.Friend friend = new FriendList.Friend();
                friend.m_name = pName;
                friend.m_status = FriendList.RESPONSE_PENDING;
                friend.m_sid = sid;
                if (ary.length > 2) friend.m_additionMsg = ary[2];
                friend.m_msgList.add(msg);
                m_lstPending.put(friend.m_name, friend);
            } else if (msg.m_type.equals("2")) {
                //new GetDataTask().execute();
            } else if (msg.m_type.equals("3")) {
                FriendList.Friend friend = new FriendList.Friend();
                String ary[] = msg.m_msg.split("\n");
                friend.m_name = ary[0];
                friend.m_status = FriendList.REJECTED;
                friend.m_msgList.add(msg);
                m_lstPending.put(friend.m_name, friend);
                m_lstReqPending.remove(friend.m_name);
            } else if (msg.m_type.equals("10000")) {
                msg.m_to = Global.m_devInfo.getUserName();
                if (msg.m_msg.contains("@devid=")) {
                    msg.m_type = "10001";
                    msg.m_real_msg = msg.m_msg;
                    msg.m_msg = T(R.string.video_share_recv_prompt);
                }

                FriendList.Friend friend = Global.m_friends.findByName(msg.m_from);
                if (null == friend) continue;
                friend.m_status = FriendList.FRIEND;
                friend.m_msgList.add(msg);
                if (FriendMessageList.Message.findByMsgId(msg.m_id) != null) continue;
                msg.save();
                // 正在聊天中，不更新该好友新消息数
                if (friend.m_inChat) continue;
                friend.m_newMsgCount++;
                m_newMsgCount++;
            }
        }

        if ((m_queryCount * m_queryPage) < Integer.parseInt(Global.m_messges.m_allCount)) {
            FriendMessageList.Message msg = FriendMessageList.Message.findByLastReceiver();
            if (null != msg) {
                m_queryStartTime = msg.m_time;
            }

            m_queryPage++;
            loadData();
            return;
        }

        updateList();
        mPullRefreshListView.onRefreshComplete();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_title_right:
                onBtnTitleRight(v);
                break;
            case R.id.btn_add:
                m_menu.dismiss();
                onBtnAdd(v);
                break;
            case R.id.btn_delete:
                m_menu.dismiss();
                onBtnDelete(v);
                break;
            case R.id.btn_accept:
                onBtnAccept(v);
                break;
            case R.id.btn_reject:
                onBtnReject(v);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (m_adapter.isEdit()) {
            m_adapter.setEdit(false);
            m_adapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    private void onBtnDelete(View v) {
        m_adapter.setEdit(true);
        m_adapter.notifyDataSetChanged();
    }

    private void onBtnAdd(View v) {
        final Context ctx = this.getActivity();
        final RegexpEditText txtName = new RegexpEditText(ctx);
        txtName.setHint(R.string.hit_input_friend_name);
        txtName.setPadding(10, 10, 10, 10);
        txtName.setSingleLine(true);
        txtName.setText("");
        txtName.setRequired(true);
        txtName.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
        txtName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Define.DEVICE_NAEM_LENGTH)});

        final RegexpEditText txtInfo = new RegexpEditText(ctx);
        txtInfo.setHint(R.string.hit_input_addition_info);
        txtInfo.setPadding(10, 10, 10, 10);
        txtInfo.setSingleLine(true);
        txtInfo.setText("");
        txtInfo.setRequired(false);
        txtInfo.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
        txtInfo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});

        LinearLayout layout = new LinearLayout(this.getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 0, 5, 0);
        layout.setBackgroundColor(Color.rgb(207, 232, 179));
        layout.addView(txtName);
        layout.addView(txtInfo);
        txtName.requestFocus();

        new AlertDialog.Builder(ctx).setTitle(R.string.dlg_title_add_friend)
                .setView(layout)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        MainActivity.m_this.hideInputPanel(txtName);
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if (!txtName.validate()) {
                    txtName.setShakeAnimation();
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                final String value = txtName.getText().toString();
                final String msg = txtInfo.getText().toString();
                if (value.equals(Global.m_devInfo.getUserName())) {
                    txtName.setShakeAnimation();
                    toast(R.string.can_not_add_self_friend);
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if (Global.m_friends.findByName(value) != null) {
                    txtName.setShakeAnimation();
                    toast(R.string.friend_is_exists);
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                MainActivity.m_this.hideInputPanel(txtName);
                showTipDlg(R.string.please_wait_communication, 20000, R.string.timeout_retry);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int ret = LibImpl.getInstance().getFuncLib().AddFriend(value, msg);
                        FriendFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTipDlg.dismiss();
                                if (ret != 0) {
                                    toast(ConstantImpl.getAddFriendErrText(ret));
                                    return;
                                }

                                dialog.dismiss();
                                FriendList.Friend friend = new FriendList.Friend();
                                friend.m_name = value;
                                friend.m_status = FriendList.REQUEST_PENDING;
                                m_lstReqPending.put(friend.m_name, friend);
                                toast(R.string.msg_add_friend_succeed);
                                updateList();
                            }
                        });
                    }
                }).start();
            }
        }).create().show();
    }

    private void onBtnReject(View v) {
        final FriendList.Friend friend = (FriendList.Friend) v.getTag();
        if (null == friend) return;
        showTipDlg(R.string.please_wait_communication, 20000, R.string.timeout_retry);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int ret = LibImpl.getInstance().getFuncLib().ConfirmAddFriend(friend.m_sid, 0);
                FriendFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTipDlg.dismiss();
                        if (ret != 0) {
                            toast(ConstantImpl.getConfirmAddFriendErrText(ret));
                            return;
                        }

                        m_lstPending.remove(friend.m_name);
                        updateList();
                    }
                });
            }
        }).start();
    }

    private void onBtnAccept(View v) {
        final FriendList.Friend friend = (FriendList.Friend) v.getTag();
        if (null == friend) return;
        showTipDlg(R.string.please_wait_communication, 20000, R.string.timeout_retry);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int ret = LibImpl.getInstance().getFuncLib().ConfirmAddFriend(friend.m_sid, 1);
                FriendFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTipDlg.dismiss();
                        if (ret != 0) {
                            toast(ConstantImpl.getConfirmAddFriendErrText(ret));
                            return;
                        }

                        m_lstPending.remove(friend.m_name);
                        updateList();
                    }
                });
            }
        }).start();
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

    class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private boolean m_isEdit;

        public ListViewAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
        }

        public class ViewHolder {
            public Button btnDelete;
            public ImageView imgCheck;
            public TextView tvFriendName;
            public TextView tvFriendNo;
            public TextView tvAdditionMsg;
            public Button btnAccept;
            public Button btnReject;
            public Button btnMsgIcon;
            public ImageView imgState;
        }

        public void setEdit(boolean b) {
            m_isEdit = b;
        }

        public boolean isEdit() {
            return m_isEdit;
        }

        @Override
        public int getCount() {
            return m_lstFriend.size();
        }

        @Override
        public Object getItem(int position) {
            String key = (String) m_lstFriend.keySet().toArray()[position];
            return m_lstFriend.get(key);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.friend_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.btnDelete = (Button) v.findViewById(R.id.btnDelete);
                viewHolder.imgCheck = (ImageView) v.findViewById(R.id.imgCheck);
                viewHolder.tvFriendName = (TextView) v.findViewById(R.id.tvFriendName);
                viewHolder.tvFriendNo = (TextView) v.findViewById(R.id.tvFriendNo);
                viewHolder.tvAdditionMsg = (TextView) v.findViewById(R.id.tvAdditionMsg);
                viewHolder.btnAccept = (Button) v.findViewById(R.id.btn_accept);
                viewHolder.btnReject = (Button) v.findViewById(R.id.btn_reject);
                viewHolder.btnMsgIcon = (Button) v.findViewById(R.id.btn_small_icon);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.btnDelete.setVisibility(View.GONE);
            viewHolder.btnAccept.setVisibility(View.GONE);
            viewHolder.btnReject.setVisibility(View.GONE);
            viewHolder.btnMsgIcon.setVisibility(View.GONE);

            final int pos = position;
            final FriendList.Friend friend = (FriendList.Friend) getItem(pos);
            viewHolder.tvFriendName.setText(friend.m_name);
            if (friend.m_status == FriendList.REQUEST_PENDING) {
                viewHolder.tvFriendNo.setText(R.string.msg_add_friend_wait_confirm);
            } else if (friend.m_status == FriendList.RESPONSE_PENDING) {
                viewHolder.tvFriendNo.setText(R.string.msg_request_add_friend);
                viewHolder.tvAdditionMsg.setText(friend.m_additionMsg);
                viewHolder.tvAdditionMsg.setVisibility(View.VISIBLE);
                viewHolder.btnAccept.setVisibility(View.VISIBLE);
                viewHolder.btnReject.setVisibility(View.VISIBLE);
                viewHolder.btnAccept.setTag(friend);
                viewHolder.btnReject.setTag(friend);
                viewHolder.btnAccept.setOnClickListener(FriendFragment.this);
                viewHolder.btnReject.setOnClickListener(FriendFragment.this);
            } else if (friend.m_status == FriendList.REJECTED) {
                viewHolder.tvFriendNo.setText(R.string.msg_add_friend_reject);
            } else if (friend.m_status == FriendList.ACCEPTED) {
                viewHolder.tvFriendNo.setText(R.string.msg_add_friend_accept);
            } else {
                viewHolder.tvFriendNo.setText(friend.m_id);
            }

            viewHolder.btnMsgIcon.setText(String.valueOf(friend.m_newMsgCount));
            viewHolder.btnMsgIcon.setVisibility(friend.m_newMsgCount > 0 ? View.VISIBLE : View.GONE);

            int show = m_isEdit ? View.VISIBLE : View.GONE;
            viewHolder.btnDelete.setVisibility(show);
            viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteFriend(friend, pos);
                }
            });

            int totle_Num = getCount();
            if (totle_Num == 1) {
                v.setBackgroundResource(R.drawable.default_selector);
                return v;
            }
            // 第一项
            else if (position == 0) {
                v.setBackgroundResource(R.drawable.list_top_selector);
            }
            // 最后一项
            else if (position == totle_Num - 1) {
                v.setBackgroundResource(R.drawable.list_bottom_selector);
            } else {
                v.setBackgroundResource(R.drawable.list_center_selector_2);
            }

            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            FriendList.Friend friend = (FriendList.Friend) getItem(pos - 1);
            if (null == friend) return;
            if (friend.m_status != FriendList.FRIEND) return;

            setNewMsgCount(m_newMsgCount - friend.m_newMsgCount);

            Intent it = new Intent(m_ctx, ChatMessage.class);
            it.putExtra(Constant.EXTRA_FRIEND_ID, friend.m_id);
            startActivity(it);
        }
    }

    private void onDeleteFriend(final FriendList.Friend friend, final int pos) {
        if (null == friend) return;
        if (friend.m_status != FriendList.FRIEND) {
            FriendMessageList lstMsg = m_lstPending.get(friend.m_name).m_msgList;
            final FriendMessageList.Message msg = lstMsg.get(0);
            if (null == msg) return;
            showTipDlg(R.string.please_wait_communication, 20000, R.string.timeout_retry);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int ret = LibImpl.getInstance().getFuncLib().DeleteOfflineMsg(msg.m_id, "-1");
                    FriendFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTipDlg.dismiss();
                            if (ret != 0) {
                                toast(ConstantImpl.getDeleteOfflineMsgErrText(ret));
                                return;
                            }

                            m_lstFriend.remove(friend.m_name);
                            m_lstPending.remove(friend.m_name);
                            m_adapter.notifyDataSetChanged();
                        }
                    });
                }
            }).start();

            return;
        }

        new AlertDialog.Builder(this.getActivity())
                .setTitle(T(R.string.dlg_tip))
                .setMessage(T(R.string.dlg_delete_friend_tip))
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
                        showTipDlg(R.string.please_wait_communication, 20000, R.string.timeout_retry);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final int ret = LibImpl.getInstance().getFuncLib().DelFriend(friend.m_id);
                                FriendFragment.this.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTipDlg.dismiss();
                                        if (ret != 0) {
                                            //toast(R.string.delete_friend_failed);
                                            toast(ConstantImpl.getDelFriendErrText(ret));
                                            return;
                                        }

                                        m_lstFriend.remove(friend.m_name);
                                        Global.m_friends.m_lstFriend.remove(friend.m_name);
                                        m_adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }).start();
                    }
                }).create().show();
    }
}

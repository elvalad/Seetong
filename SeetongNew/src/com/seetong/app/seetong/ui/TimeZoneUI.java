package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.sdk.impl.LibImpl;
import com.seetong.app.seetong.sdk.impl.PlayerDevice;
import com.seetong.app.seetong.ui.ext.DatetimeView;
import ipc.android.sdk.com.NetSDK_CMD_TYPE;
import ipc.android.sdk.com.NetSDK_TimeConfig;
import ipc.android.sdk.com.NetSDK_TimeZone_DST_Config;
import ipc.android.sdk.impl.FunclibAgent;
import org.xmlpull.v1.XmlPullParserException;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeZoneUI extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "TimeZoneUI";
    private static final String KEY_ID = "id";
    private static final String KEY_DISPLAYNAME = "name";
    private static final String KEY_SUB_NAME = "sub_name";
    private static final String KEY_GMT = "gmt";
    private static final String KEY_OFFSET = "offset";
    private static final String KEY_TZ_VALUE = "tz_value";
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;
    private static final int HOURS_24 = 24 * HOURS_1;
    private static final int HOURS_HALF = HOURS_1 / 2;

    String m_device_id;
    PlayerDevice m_dev;
    private ProgressDialog mTipDlg;
    TextView m_tv_zone;
    TextView m_tv_time;
    ToggleButton m_tb_dts;
    Button m_btn_start_month;
    Button m_btn_start_week_num;
    Button m_btn_start_week;
    TextView m_tv_start_time;
    Button m_btn_end_month;
    Button m_btn_end_week_num;
    Button m_btn_end_week;
    TextView m_tv_end_time;
    TextView m_tv_dst_offset;
    TimeZone m_tz;
    TimeZone m_tz_sel;
    List<HashMap> m_timezoneSortedList;
    List<HashMap> m_timezoneMergerSortedList;
    HashMap<Integer, HashMap> m_timezoneMap = new HashMap<>();

    String[] m_ary_month;
    String[] m_ary_week_num;
    String[] m_ary_week;
    String[] m_ary_dst_offset;
    int[] m_ary_dst_offset_val = {30, 60, 90, 120};

    int m_zone_index = 0;
    Date m_time;
    int m_dst_start_month_index = 0;
    int m_dst_start_week_num_index = 0;
    int m_dst_start_week_index = 0;
    String m_dst_start_time;
    int m_dst_end_month_index = 0;
    int m_dst_end_week_num_index = 0;
    int m_dst_end_week_index = 0;
    String m_dst_end_time;
    int m_dst_offset_index = 0;

    NetSDK_TimeZone_DST_Config m_timezone_dst_config;
    NetSDK_TimeConfig m_time_config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_zone_ui);
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_timezone_setting));
        initWidget();
    }

    protected void initWidget() {
        m_ary_month = getResources().getStringArray(R.array.string_ary_month);
        m_ary_week_num = getResources().getStringArray(R.array.string_ary_week_num);
        m_ary_week = getResources().getStringArray(R.array.string_ary_week);
        m_ary_dst_offset = getResources().getStringArray(R.array.string_ary_dst_offset);
        m_timezoneSortedList = getZones();
        m_tz = TimeZone.getDefault();
        m_tz_sel = m_tz;

        MyComparator comparator = new MyComparator(KEY_OFFSET);
        Collections.sort(m_timezoneSortedList, comparator);
        for (int i = 0; i < m_timezoneSortedList.size(); i++) {
            int offset = (int)m_timezoneSortedList.get(i).get(KEY_OFFSET);
            if (m_timezoneMap.containsKey(offset)) {
                String sub_name = m_timezoneMap.get(offset).get(KEY_SUB_NAME).toString();
                if (!TextUtils.isEmpty(sub_name)) sub_name += ",";
                sub_name += m_timezoneSortedList.get(i).get(KEY_DISPLAYNAME);
                m_timezoneMap.get(offset).put(KEY_SUB_NAME, sub_name);
                continue;
            }

            m_timezoneSortedList.get(i).put(KEY_SUB_NAME, "");
            m_timezoneMap.put(offset, m_timezoneSortedList.get(i));
        }

        m_timezoneMergerSortedList = new ArrayList(m_timezoneMap.values());
        Collections.sort(m_timezoneMergerSortedList, comparator);
        for (int i = 0; i < m_timezoneMergerSortedList.size(); i++) {
            int offset = (int)m_timezoneMergerSortedList.get(i).get(KEY_OFFSET);
            if (offset == m_tz.getRawOffset()) m_zone_index = i;
        }

        mTipDlg = new ProgressDialog(this, R.string.dlg_login_server_tip);
        mTipDlg.setCancelable(false);
        m_device_id = getIntent().getStringExtra(Constant.EXTRA_DEVICE_ID);
        m_dev = Global.getDeviceById(m_device_id);
        m_tb_dts = (ToggleButton) findViewById(R.id.tb_dst);

        findViewById(R.id.btn_start_month).setOnClickListener(this);
        findViewById(R.id.btn_start_week_num).setOnClickListener(this);
        findViewById(R.id.btn_start_week).setOnClickListener(this);
        findViewById(R.id.btn_end_month).setOnClickListener(this);
        findViewById(R.id.btn_end_week_num).setOnClickListener(this);
        findViewById(R.id.btn_end_week).setOnClickListener(this);
        findViewById(R.id.lab_start_time).setOnClickListener(this);
        findViewById(R.id.lab_end_time).setOnClickListener(this);
        findViewById(R.id.lab_dst_offset).setOnClickListener(this);

        m_tv_zone = (TextView) findViewById(R.id.lab_zone_value);
        m_tv_zone.setText(m_timezoneMergerSortedList.get(m_zone_index).get(KEY_DISPLAYNAME).toString());
        m_tv_zone.setOnClickListener(this);

        m_tv_time = (TextView) findViewById(R.id.lab_time_value);
        m_time = getLocalTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        m_tv_time.setText(fmt.format(m_time));
        m_tv_time.setOnClickListener(this);

        m_btn_start_month = (Button) findViewById(R.id.btn_start_month);
        m_btn_start_month.setText(m_ary_month[0]);

        m_btn_start_week_num = (Button) findViewById(R.id.btn_start_week_num);
        m_btn_start_week_num.setText(m_ary_week_num[0]);

        m_btn_start_week = (Button) findViewById(R.id.btn_start_week);
        m_btn_start_week.setText(m_ary_week[0]);

        m_tv_start_time = (TextView) findViewById(R.id.lab_start_time);
        m_tv_start_time.setText("00:00:00");

        m_btn_end_month = (Button) findViewById(R.id.btn_end_month);
        m_btn_end_month.setText(m_ary_month[0]);

        m_btn_end_week_num = (Button) findViewById(R.id.btn_end_week_num);
        m_btn_end_week_num.setText(m_ary_week_num[0]);

        m_btn_end_week = (Button) findViewById(R.id.btn_end_week);
        m_btn_end_week.setText(m_ary_week[0]);

        m_tv_end_time = (TextView) findViewById(R.id.lab_end_time);
        m_tv_end_time.setText("00:00:00");

        m_tv_dst_offset = (TextView) findViewById(R.id.lab_dst_offset);
        m_tv_dst_offset.setText(m_ary_dst_offset[0]);

        m_tb_dts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.tbr_dst_start_date).setVisibility(View.VISIBLE);
                    findViewById(R.id.tbr_dst_start_time).setVisibility(View.VISIBLE);
                    findViewById(R.id.tbr_dst_end_date).setVisibility(View.VISIBLE);
                    findViewById(R.id.tbr_dst_end_time).setVisibility(View.VISIBLE);
                    findViewById(R.id.tbr_dst_offset).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.tbr_dst_start_date).setVisibility(View.GONE);
                    findViewById(R.id.tbr_dst_start_time).setVisibility(View.GONE);
                    findViewById(R.id.tbr_dst_end_date).setVisibility(View.GONE);
                    findViewById(R.id.tbr_dst_end_time).setVisibility(View.GONE);
                    findViewById(R.id.tbr_dst_offset).setVisibility(View.GONE);
                }
            }
        });

        m_tb_dts.setChecked(false);
        if (null != m_dev) {
            m_tb_dts.setVisibility(m_dev.is_dst_support() ? View.VISIBLE : View.GONE);
        }

        Button btnFinish = (Button) findViewById(R.id.btnRight);
        btnFinish.setText(R.string.finish);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(this);
        LibImpl.getInstance().addHandler(m_handler);
        loadData();
    }

    public void showTipDlg(int resId, int timeout, int timeoutMsg) {
        mTipDlg.setTitle(T(resId));
        mTipDlg.setTimeoutToast(T(timeoutMsg));
        mTipDlg.show(timeout);
    }

    public Time getLocalTime() {
        long c = System.currentTimeMillis();
        int p = m_tz.getRawOffset() + m_tz.getDSTSavings();
        int z = m_tz_sel.getRawOffset() + m_tz_sel.getDSTSavings();
        // System.currentTimeMillis()返回的是UTC时间，SimpleDateFormat是与时区相关的，这里这样计算是因为SimpleDateFormat设置的是系统当前的时区，所以这里先减去了当前时区的时差
        // 其实这里不用减，直接设置SimpleDateFormat的时区为选择的时区，然后format中传入System.currentTimeMillis()就好了。
        long t = (c - p + z);
        Log.d(TAG, "CTM=" + c + ",z0=" + m_tz.getRawOffset() + ",z1=" + m_tz.getDSTSavings() + ",z2=" + m_tz_sel.getRawOffset() + ",z3=" + m_tz_sel.getDSTSavings() + ",p=" + p + ",z=" + z + ",t=" + t);
        return new Time(t);
    }

    public void loadData() {
        /*int ret = FunclibAgent.getInstance().P2PDevSystemControl(m_device_id, 1048, "");
        if (0 != ret) {
            toast(R.string.dlg_get_config_info_failed_tip);
            finish();
            return;
        }*/

        int ret = FunclibAgent.getInstance().GetP2PDevConfig(m_device_id, NetSDK_CMD_TYPE.CMD_GET_SYSTEM_TIME_CONFIG, "");
        if (0 != ret) {
            toast(R.string.dlg_get_config_info_failed_tip);
            finish();
            return;
        }

        mTipDlg.setCallback(new ProgressDialog.ICallback() {
            @Override
            public void onTimeout() {
                finish();
            }

            @Override
            public boolean onCancel() {
                return false;
            }
        });

        showTipDlg(R.string.dlg_get_config_info_tip, 20000, R.string.dlg_get_config_info_timeout_tip);
    }

    public void saveData() {
        int tz = (int)m_timezoneMergerSortedList.get(m_zone_index).get(KEY_TZ_VALUE);
        //if (m_dev.is_dst_support()) {
            String start_time = String.format("M%02dW%dD%dT%s", m_dst_start_month_index + 1, m_dst_start_week_num_index + 1, m_dst_start_week_index, m_dst_start_time);
            String end_time = String.format("M%02dW%dD%dT%s", m_dst_end_month_index + 1, m_dst_end_week_num_index + 1, m_dst_end_week_index, m_dst_end_time);

            NetSDK_TimeZone_DST_Config ntzdc = (NetSDK_TimeZone_DST_Config) m_timezone_dst_config.clone();
            ntzdc.addHead(false);
            ntzdc.TimeMode = "MANUAL";
            ntzdc.TimeZone = String.valueOf(tz);
            ntzdc.dst.StartTime = start_time;
            ntzdc.dst.EndTime = end_time;
            ntzdc.dst.Enable = m_tb_dts.isChecked() ? "1" : "0";
            ntzdc.dst.Delta = String.valueOf(m_ary_dst_offset_val[m_dst_offset_index]);
            String xml = ntzdc.toXMLString();
            Log.d(TAG, "save config 222, xml=" + xml);
            int ret = FunclibAgent.getInstance().SetP2PDevConfig(m_device_id, NetSDK_CMD_TYPE.CMD_SET_SYSTEM_TIME_CONFIG, xml);
            if (0 != ret) {
                toast(R.string.dlg_set_config_info_failed_tip);
                return;
            }
        //}

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        xml = "<REQUEST_PARAM Time=\"" + fmt.format(m_time) + "\" TimeZone=\"" + tz + "\" />";
        Log.d(TAG, "save config 1005, xml=" + xml);
        ret = FunclibAgent.getInstance().P2PDevSystemControl(m_device_id, 1005, xml);
        if (0 != ret) {
            toast(R.string.dlg_set_config_info_failed_tip);
            return;
        }

        mTipDlg.setCallback(null);
        showTipDlg(R.string.dlg_set_config_info_tip, 20000, R.string.dlg_set_config_info_timeout_tip);
    }

    private static class MyComparator implements Comparator<HashMap> {
        private String mSortingKey;

        public MyComparator(String sortingKey) {
            mSortingKey = sortingKey;
        }

        public void setSortingKey(String sortingKey) {
            mSortingKey = sortingKey;
        }

        public int compare(HashMap map1, HashMap map2) {
            Object value1 = map1.get(mSortingKey);
            Object value2 = map2.get(mSortingKey);

            /*
             * This should never happen, but just in-case, put non-comparable
             * items at the end.
             */
            if (!isComparable(value1)) {
                return isComparable(value2) ? 1 : 0;
            } else if (!isComparable(value2)) {
                return -1;
            }

            return ((Comparable) value1).compareTo(value2);
        }

        private boolean isComparable(Object value) {
            return (value != null) && (value instanceof Comparable);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRight:
                onBtnFinish();
                break;
            case R.id.lab_zone_value:
                onZoneValue();
                break;
            case R.id.lab_time_value:
                onTimeValue();
                break;
            case R.id.btn_start_month:
                onBtnStartMonth();
                break;
            case R.id.btn_start_week_num:
                onBtnStartWeekNum();
                break;
            case R.id.btn_start_week:
                onBtnStartWeek();
                break;
            case R.id.btn_end_month:
                onBtnEndMonth();
                break;
            case R.id.btn_end_week_num:
                onBtnEndWeekNum();
                break;
            case R.id.btn_end_week:
                onBtnEndWeek();
                break;
            case R.id.lab_start_time:
                onDstStartTime();
                break;
            case R.id.lab_end_time:
                onDstEndTime();
                break;
            case R.id.lab_dst_offset:
                onDstOffset();
                break;
            default: break;
        }
    }

    private List<HashMap> getZones() {
        List<HashMap> myData = new ArrayList<HashMap>();
        long date = Calendar.getInstance().getTimeInMillis();
        try {
            XmlResourceParser xrp = getResources().getXml(R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG) continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(myData, id, displayName, date);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Ill-formatted timezones.xml file");
        } catch (java.io.IOException ioe) {
            Log.e(TAG, "Unable to read timezones.xml file");
        }

        return myData;
    }

    protected void addItem(List<HashMap> myData, String id, String displayName, long date) {
        HashMap map = new HashMap();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);
        TimeZone tz = TimeZone.getTimeZone(id);
        int offset = tz.getOffset(date);
        int p = Math.abs(offset);
        StringBuilder name = new StringBuilder();
        name.append("GMT");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / (HOURS_1));
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        map.put(KEY_GMT, name.toString());
        map.put(KEY_OFFSET, offset);

        int h = offset / 60000;
        h = h + 12 * 60;
        map.put(KEY_TZ_VALUE, h);

        if (id.equals(TimeZone.getDefault().getID())) {
            m_zone_index = myData.size();
        }

        myData.add(map);
    }

    private void onZoneValue() {
        String[] from = new String[] {KEY_DISPLAYNAME, KEY_GMT, KEY_SUB_NAME};
        int[] to = new int[] {android.R.id.text1, android.R.id.text2, R.id.text3};
        SimpleAdapter mTimezoneSortedAdapter = new SimpleAdapter(this, (List) m_timezoneMergerSortedList, R.layout.simple_list_item_2, from, to);

        //mTimezoneSortedAdapter.setSelection(mDefault);

        new AlertDialog.Builder(this).setAdapter(mTimezoneSortedAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_zone_index = which;
                String tz_id = (String)m_timezoneMergerSortedList.get(m_zone_index).get(KEY_ID);
                m_tz_sel = TimeZone.getTimeZone(tz_id);
                m_time = getLocalTime();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //fmt.setTimeZone(m_tz_sel);

                m_tv_zone.setText(m_timezoneMergerSortedList.get(which).get(KEY_DISPLAYNAME).toString());
                m_tv_time.setText(fmt.format(m_time));
            }
        }).create().show();
    }

    private void onTimeValue() {
        final DatetimeView dtv = new DatetimeView(this, R.style.datetime_dialog);
        dtv.show();
        dtv.setOnDatetimeChangedListener(new DatetimeView.OnDatetimeChangedListener() {
            @Override
            public void onChanged(String year, String month, String day, String hour, String minute, String second) {

            }
        });

        dtv.setOnFinishListener(new DatetimeView.OnFinishListener() {
            @Override
            public void onOk() {
                m_time = dtv.getValue();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                m_tv_time.setText(fmt.format(m_time));
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void onDstEndTime() {
        /*Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            }
        }, hour, minute, true).show();*/
        final DatetimeView dtv = new DatetimeView(this, R.style.datetime_dialog);
        dtv.show();
        dtv.setShowTime();
        dtv.setOnDatetimeChangedListener(new DatetimeView.OnDatetimeChangedListener() {
            @Override
            public void onChanged(String year, String month, String day, String hour, String minute, String second) {

            }
        });

        dtv.setOnFinishListener(new DatetimeView.OnFinishListener() {
            @Override
            public void onOk() {
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
                m_dst_end_time = fmt.format(dtv.getValue());
                m_tv_end_time.setText(m_dst_end_time);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void onDstStartTime() {
        //new TimePickerDialog(this, null, 11, 11, true).show();
        final DatetimeView dtv = new DatetimeView(this, R.style.datetime_dialog);
        dtv.show();
        dtv.setShowTime();
        dtv.setOnDatetimeChangedListener(new DatetimeView.OnDatetimeChangedListener() {
            @Override
            public void onChanged(String year, String month, String day, String hour, String minute, String second) {

            }
        });

        dtv.setOnFinishListener(new DatetimeView.OnFinishListener() {
            @Override
            public void onOk() {
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
                m_dst_start_time = fmt.format(dtv.getValue());
                m_tv_start_time.setText(m_dst_start_time);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void onDstOffset() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_dst_offset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_offset_index = which;
                        m_tv_dst_offset.setText(m_ary_dst_offset[which]);
                    }
                }).create().show();
    }

    private void onBtnEndWeek() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_week, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_end_week_index = which;
                        m_btn_end_week.setText(m_ary_week[which]);
                    }
                }).create().show();
    }

    private void onBtnEndWeekNum() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_week_num, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_end_week_num_index = which;
                        m_btn_end_week_num.setText(m_ary_week_num[which]);
                    }
                }).create().show();
    }

    private void onBtnEndMonth() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_month, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_end_month_index = which;
                        m_btn_end_month.setText(m_ary_month[which]);
                    }
                }).create().show();
    }

    private void onBtnStartWeek() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_week, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_start_week_index = which;
                        m_btn_start_week.setText(m_ary_week[which]);
                    }
                }).create().show();
    }

    private void onBtnStartWeekNum() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_week_num, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_start_week_num_index = which;
                        m_btn_start_week_num.setText(m_ary_week_num[which]);
                    }
                }).create().show();
    }

    private void onBtnStartMonth() {
        new AlertDialog.Builder(this)
                .setItems(m_ary_month, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_dst_start_month_index = which;
                        m_btn_start_month.setText(m_ary_month[which]);
                    }
                }).create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibImpl.getInstance().addHandler(m_handler);
    }

    @Override
    protected void onDestroy() {
        LibImpl.getInstance().removeHandler(m_handler);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void onBtnFinish() {
        saveData();
    }

    @Override
    public void handleMessage(Message msg) {
        int flag = msg.arg1;
        switch (msg.what) {
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_TIME_CONFIG:
                NetSDK_TimeZone_DST_Config cfg = (NetSDK_TimeZone_DST_Config) msg.obj;
                onGetTimeZoneDstConfig(flag, cfg);
                break;
            case NetSDK_CMD_TYPE.CMD_SET_SYSTEM_TIME_CONFIG:
                onSetTimeZoneDstConfig(flag);
                break;
            case 1005:
                onSetTimeConfig(flag);
                break;
            case 1048:
                NetSDK_TimeConfig nstc = (NetSDK_TimeConfig) msg.obj;
                onGetTimeConfig(flag, nstc);
                break;
        }
    }

    private void onSetTimeConfig(int flag) {
        if (!mTipDlg.isShowing()) return;
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.dlg_set_config_info_failed_tip);
        } else {
            toast(R.string.dlg_set_config_info_succeed_tip);
            finish();
        }
    }

    private void onGetTimeConfig(int flag, NetSDK_TimeConfig nstc) {
        mTipDlg.dismiss();
        if (flag != 0 || null == nstc) {
            toast(R.string.dlg_get_config_info_failed_tip);
            finish();
            return;
        }

        m_time_config = nstc;
        for (int i = 0; i < m_timezoneMergerSortedList.size(); i++) {
            int tz = (int) m_timezoneMergerSortedList.get(i).get(KEY_TZ_VALUE);
            if (tz == Integer.valueOf(m_time_config.TimeZone)) {
                m_zone_index = i;
                String tz_id = (String)m_timezoneMergerSortedList.get(m_zone_index).get(KEY_ID);
                m_tz_sel = TimeZone.getTimeZone(tz_id);
                m_tv_zone.setText(m_timezoneMergerSortedList.get(i).get(KEY_DISPLAYNAME).toString());
                break;
            }
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.valueOf(m_time_config.Year));
        c.set(Calendar.MONTH, Integer.valueOf(m_time_config.Month) - 1);
        c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(m_time_config.Day));
        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(m_time_config.Hour));
        c.set(Calendar.MINUTE, Integer.valueOf(m_time_config.Minute));
        c.set(Calendar.SECOND, Integer.valueOf(m_time_config.Second));
        m_time = c.getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String v = m_time_config.Year + "-" + m_time_config.Month + "-" + m_time_config.Day + " " + m_time_config.Hour + ":" + m_time_config.Minute + ":" + m_time_config.Second;
        m_tv_time.setText(fmt.format(m_time));
    }

    private void onGetTimeZoneDstConfig(int flag, NetSDK_TimeZone_DST_Config cfg) {
        mTipDlg.dismiss();
        if (flag != 0 || null == cfg) {
            toast(R.string.dlg_get_config_info_failed_tip);
            finish();
            return;
        }

        m_timezone_dst_config = cfg;
        Log.d(TAG, "device timezone=" + m_timezone_dst_config.TimeZone);
        for (int i = 0; i < m_timezoneMergerSortedList.size(); i++) {
            int tz = (int) m_timezoneMergerSortedList.get(i).get(KEY_TZ_VALUE);
            if (tz == Integer.valueOf(m_timezone_dst_config.TimeZone)) {
                m_zone_index = i;
                String tz_id = (String)m_timezoneMergerSortedList.get(m_zone_index).get(KEY_ID);
                m_tz_sel = TimeZone.getTimeZone(tz_id);
                m_tv_zone.setText(m_timezoneMergerSortedList.get(i).get(KEY_DISPLAYNAME).toString());
                break;
            }
        }

        m_time = getLocalTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        m_tv_time.setText(fmt.format(m_time));

        // 根据获取到的数据设置界面
        m_tb_dts.setChecked("1".equals(m_timezone_dst_config.dst.Enable));
        if ("1".equals(m_timezone_dst_config.dst.Enable)) {
            if (!TextUtils.isEmpty(m_timezone_dst_config.dst.StartTime)) {
                String v = m_timezone_dst_config.dst.StartTime.substring(1, 3);
                m_dst_start_month_index = Integer.valueOf(v) - 1;
                m_btn_start_month.setText(m_ary_month[m_dst_start_month_index]);
                m_dst_start_week_num_index = Integer.valueOf(m_timezone_dst_config.dst.StartTime.substring(4, 5)) - 1;
                m_btn_start_week_num.setText(m_ary_week_num[m_dst_start_week_num_index]);
                m_dst_start_week_index = Integer.valueOf(m_timezone_dst_config.dst.StartTime.substring(6, 7));
                m_btn_start_week.setText(m_ary_week[m_dst_start_week_index]);
                m_dst_start_time = m_timezone_dst_config.dst.StartTime.substring(8);
                m_tv_start_time.setText(m_dst_start_time);
            }

            if (!TextUtils.isEmpty(m_timezone_dst_config.dst.EndTime)) {
                String v = m_timezone_dst_config.dst.EndTime.substring(1, 3);
                m_dst_end_month_index = Integer.valueOf(v) - 1;
                m_btn_end_month.setText(m_ary_month[m_dst_end_month_index]);
                m_dst_end_week_num_index = Integer.valueOf(m_timezone_dst_config.dst.EndTime.substring(4, 5)) - 1;
                m_btn_end_week_num.setText(m_ary_week_num[m_dst_end_week_num_index]);
                m_dst_end_week_index = Integer.valueOf(m_timezone_dst_config.dst.EndTime.substring(6, 7));
                m_btn_end_week.setText(m_ary_week[m_dst_end_week_index]);
                m_dst_end_time = m_timezone_dst_config.dst.EndTime.substring(8);
                m_tv_end_time.setText(m_dst_end_time);
            }

            for (int i = 0; i < m_ary_dst_offset_val.length; i++) {
                if (m_ary_dst_offset_val[i] == Integer.valueOf(m_timezone_dst_config.dst.Delta)) {
                    m_dst_offset_index = i;
                    m_tv_dst_offset.setText(m_ary_dst_offset[i]);
                }
            }
        }
    }

    private void onSetTimeZoneDstConfig(int flag) {
        if (!mTipDlg.isShowing()) return;
        mTipDlg.dismiss();
        if (0 != flag) {
            toast(R.string.dlg_set_config_info_failed_tip);
        } else {
            toast(R.string.dlg_set_config_info_succeed_tip);
            finish();
        }
    }
}
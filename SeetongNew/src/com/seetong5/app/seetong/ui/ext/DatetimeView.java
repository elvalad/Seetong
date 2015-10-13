package com.seetong5.app.seetong.ui.ext;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong.app.seetong.R;

import java.util.*;

public class DatetimeView extends Dialog {
    private static final String TAG = DatetimeView.class.getName();
    private Context m_act;
    private YearAdapter m_year_adapter;
    private MonthAdapter m_month_adapter;
    private DayAdapter m_day_adapter;
    private HourAdapter m_hour_adapter;
    private MinutesAdapter m_minute_adapter;
    private SecondsAdapter m_second_adapter;
    private ListView m_lv_year;
    private ListView m_lv_month;
    private ListView m_lv_day;
    private ListView m_lv_hour;
    private ListView m_lv_minute;
    private ListView m_lv_second;
    private Calendar m_calendar;
    private String m_year;
    private String m_month;
    private String m_day;
    private String m_hour;
    private String m_minute;
    private String m_second;
    private OnDatetimeChangedListener m_dtChangedCb;
    private OnFinishListener m_dtFinishCb;
    private Button m_btnOk;
    private Button m_btnCancel;


    public DatetimeView(Context act) {
        super(act);
        m_act = act;
    }

    public DatetimeView(Context context, int theme) {
        super(context, theme);
        m_act = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.datetime_view);
        setCanceledOnTouchOutside(false);

        m_calendar = Calendar.getInstance();
        m_calendar.setTime(new Date());

        m_lv_year = (ListView) findViewById(R.id.id_year);
        m_lv_month = (ListView) findViewById(R.id.id_month);
        m_lv_day = (ListView) findViewById(R.id.id_day);
        m_lv_hour = (ListView) findViewById(R.id.id_hour);
        m_lv_minute = (ListView) findViewById(R.id.id_mitune);
        m_lv_second = (ListView) findViewById(R.id.id_second);

        m_year_adapter = new YearAdapter(m_act);
        m_month_adapter = new MonthAdapter(m_act);
        m_day_adapter = new DayAdapter(m_act);
        m_hour_adapter = new HourAdapter(m_act);
        m_minute_adapter = new MinutesAdapter(m_act);
        m_second_adapter = new SecondsAdapter(m_act);

        m_lv_year.setAdapter(m_year_adapter);
        m_lv_year.setOnScrollListener(m_year_adapter);

        m_lv_month.setAdapter(m_month_adapter);
        m_lv_month.setOnScrollListener(m_month_adapter);

        m_lv_day.setAdapter(m_day_adapter);
        m_lv_day.setOnScrollListener(m_day_adapter);

        m_lv_hour.setAdapter(m_hour_adapter);
        m_lv_hour.setOnScrollListener(m_hour_adapter);

        m_lv_minute.setAdapter(m_minute_adapter);
        m_lv_minute.setOnScrollListener(m_minute_adapter);

        m_lv_second.setAdapter(m_second_adapter);
        m_lv_second.setOnScrollListener(m_second_adapter);

        m_btnOk = (Button) findViewById(R.id.btn_ok);
        m_btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != m_dtFinishCb) m_dtFinishCb.onOk();
                dismiss();
            }
        });

        m_btnCancel = (Button) findViewById(R.id.btn_cancel);
        m_btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != m_dtFinishCb) m_dtFinishCb.onCancel();
                dismiss();
            }
        });

        m_year_adapter.setYear(m_calendar.get(Calendar.YEAR));
        m_month_adapter.setMonth(m_calendar.get(Calendar.MONTH) + 1);
        m_day_adapter.setDay(m_calendar.get(Calendar.DAY_OF_MONTH));
        m_hour_adapter.setHour(m_calendar.get(Calendar.HOUR_OF_DAY));
        m_minute_adapter.setMinute(m_calendar.get(Calendar.MINUTE));
        m_second_adapter.setSecond(m_calendar.get(Calendar.SECOND));
    }

    public void setDate(int year, int month, int day) {
        setDatetime(year, month, day, 0, 0, 0);
    }

    public void setTime(int hour, int minute, int second) {
        setDatetime(1970, 1, 1, hour, minute, second);
    }

    public void setDatetime(int year, int month, int day, int hour, int minute, int second) {
        m_year_adapter.setYear(year);
        m_month_adapter.setMonth(month);
        m_day_adapter.setDay(day);
        m_hour_adapter.setHour(hour);
        m_minute_adapter.setMinute(minute);
        m_second_adapter.setSecond(second);
    }

    public Date getValue() {
        return m_calendar.getTime();
    }

    public void setShowDate() {
        findViewById(R.id.tv_year).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_month).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_day).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_hour).setVisibility(View.GONE);
        findViewById(R.id.tv_minute).setVisibility(View.GONE);
        findViewById(R.id.tv_second).setVisibility(View.GONE);
        findViewById(R.id.ll_year).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_month).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_day).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_hour).setVisibility(View.GONE);
        findViewById(R.id.ll_minute).setVisibility(View.GONE);
        findViewById(R.id.ll_second).setVisibility(View.GONE);
    }

    public void setShowTime() {
        findViewById(R.id.tv_year).setVisibility(View.GONE);
        findViewById(R.id.tv_month).setVisibility(View.GONE);
        findViewById(R.id.tv_day).setVisibility(View.GONE);
        findViewById(R.id.tv_hour).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_minute).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_second).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_year).setVisibility(View.GONE);
        findViewById(R.id.ll_month).setVisibility(View.GONE);
        findViewById(R.id.ll_day).setVisibility(View.GONE);
        findViewById(R.id.ll_hour).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_minute).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_second).setVisibility(View.VISIBLE);
    }

    public void setShowAll() {
        findViewById(R.id.tv_year).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_month).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_day).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_hour).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_minute).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_second).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_year).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_month).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_day).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_hour).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_minute).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_second).setVisibility(View.VISIBLE);
    }

    public interface OnDatetimeChangedListener {
        void onChanged(String year, String month, String day, String hour, String minute, String second);
    }

    public interface OnFinishListener {
        void onOk();
        void onCancel();
    }

    public void setOnDatetimeChangedListener(OnDatetimeChangedListener l) {
        m_dtChangedCb = l;
    }

    public void setOnFinishListener(OnFinishListener l) {
        m_dtFinishCb = l;
    }

    class YearAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<String> m_years = new ArrayList<String>();


        public YearAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            setYearsRect(1970, 2037);
        }

        public void setYearsRect(int min, int max) {
            m_years.clear();
            for (int i = min; i <= max; i++) {
                m_years.add(String.valueOf(i));
            }
        }

        public void setYear(final int year) {
            m_calendar.set(Calendar.YEAR, year);
            m_year = String.valueOf(m_calendar.get(Calendar.YEAR));
            m_lv_year.post(new Runnable() {
                @Override
                public void run() {
                    int pos = year - 1970;
                    int fvp = m_lv_year.getFirstVisiblePosition();
                    int lvp = m_lv_year.getLastVisiblePosition();
                    int diff = ((lvp - fvp) / 2);
                    pos = pos >= diff ? pos - diff : m_years.size() + (pos - diff);
                    m_lv_year.setSelection(pos);
                }
            });
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, final int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                int firstPostion = view.getFirstVisiblePosition();
                View firstChild = view.getChildAt(0);
                final int height = firstChild.getHeight();
                final int top = -(firstChild.getTop());
                if (top > (height / 2)) {
                    //view.smoothScrollToPosition(firstPostion + 1);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.smoothScrollBy(height - top, 0);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2) + 1;
                            m_year = (String) getItem(cvp);
                            m_calendar.set(Calendar.YEAR, Integer.valueOf(m_year));
                            onDatetimeChanged();
                        }
                    });
                } else {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.smoothScrollBy(-top, 0);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2);
                            m_year = (String) getItem(cvp);
                            m_calendar.set(Calendar.YEAR, Integer.valueOf(m_year));
                            onDatetimeChanged();
                        }
                    });
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int selection = 0;
            if (firstVisibleItem == 0) {
                selection = m_years.size();
                view.setSelection(selection);
            } else if (firstVisibleItem <= 2) {
                selection = m_years.size() + 2;
                view.setSelection(selection);
            } else if (firstVisibleItem + visibleItemCount > m_year_adapter.getCount() - 2) {//到底部添加数据
                selection = firstVisibleItem - m_years.size();
                view.setSelection(selection);
            }
        }

        public class ViewHolder {
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_years.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return m_years.get(position % m_years.size());
        }

        @Override
        public long getItemId(int position) {
            return position % m_years.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.datetime_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.tvCaption.setText(m_years.get(position % m_years.size()));
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        }
    }

    class MonthAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<String> m_months = new ArrayList<String>();


        public MonthAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            setMonthsRect(1, 12);
        }

        public void setMonthsRect(int min, int max) {
            m_months.clear();
            for (int i = min; i <= max; i++) {
                m_months.add(String.format("%02d", i));
            }
        }

        public void setMonth(final int month) {
            m_calendar.set(Calendar.MONTH, month - 1);
            m_month = String.format("%02d", m_calendar.get(Calendar.MONTH) + 1);
            m_lv_month.post(new Runnable() {
                @Override
                public void run() {
                    int pos = month - 1;
                    int fvp = m_lv_month.getFirstVisiblePosition();
                    int lvp = m_lv_month.getLastVisiblePosition();
                    int diff = ((lvp - fvp) / 2);
                    pos = pos >= diff ? pos - diff : m_months.size() + (pos - diff);
                    m_lv_month.setSelection(pos);
                }
            });
        }

        private void onDatetimeChange(int pos) {
            m_month = (String) getItem(pos);
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, m_calendar.get(Calendar.YEAR));
            c.set(Calendar.MONTH, Integer.valueOf(m_month) - 1);
            c.set(Calendar.DAY_OF_MONTH, 1);
            int min = c.getActualMinimum(Calendar.DAY_OF_MONTH);
            int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            int day = Integer.valueOf(m_day);
            m_day_adapter.setDaysRect(min, max);
            m_day_adapter.notifyDataSetChanged();

            if (day > max) m_day_adapter.setDay(max);
            m_calendar.set(Calendar.MONTH, Integer.valueOf(m_month) - 1);
            onDatetimeChanged();
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                int firstPostion = view.getFirstVisiblePosition();
                View firstChild = view.getChildAt(0);
                final int height = firstChild.getHeight();
                final int top = -(firstChild.getTop());
                if (top > (height / 2)) {
                    //view.smoothScrollToPosition(firstPostion + 1);
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(height - top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2) + 1;
                            onDatetimeChange(cvp);
                        }
                    });
                } else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(-top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2);
                            onDatetimeChange(cvp);
                        }
                    });
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                view.setSelection(m_months.size());
            } else if (firstVisibleItem <= 2) {
                view.setSelection(m_months.size() + 2);
            } else if (firstVisibleItem + visibleItemCount > m_month_adapter.getCount() - 2) {//到底部添加数据
                view.setSelection(firstVisibleItem - m_months.size());
            }
        }

        public class ViewHolder {
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_months.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return m_months.get(position % m_months.size());
        }

        @Override
        public long getItemId(int position) {
            return position % m_months.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.datetime_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.tvCaption.setText(m_months.get(position % m_months.size()));
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        }
    }

    class DayAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<String> m_days = new ArrayList<String>();


        public DayAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            int min = m_calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
            int max = m_calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            setDaysRect(min, max);
        }

        public void setDaysRect(int min, int max) {
            m_days.clear();
            for (int i = min; i <= max; i++) {
                m_days.add(String.format("%02d", i));
            }
        }

        public void setDay(final int day) {
            m_calendar.set(Calendar.DAY_OF_MONTH, day);
            m_day = String.format("%02d", m_calendar.get(Calendar.DAY_OF_MONTH));
            m_lv_day.post(new Runnable() {
                @Override
                public void run() {
                    int pos = day - 1;
                    int fvp = m_lv_day.getFirstVisiblePosition();
                    int lvp = m_lv_day.getLastVisiblePosition();
                    int diff = ((lvp - fvp) / 2);
                    pos = pos >= diff ? pos - diff : m_days.size() + (pos - diff);
                    m_lv_day.setSelection(pos);
                }
            });
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                int firstPostion = view.getFirstVisiblePosition();
                View firstChild = view.getChildAt(0);
                final int height = firstChild.getHeight();
                final int top = -(firstChild.getTop());
                if (top > (height / 2)) {
                    //view.smoothScrollToPosition(firstPostion + 1);
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(height - top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2) + 1;
                            m_day = (String) getItem(cvp);
                            m_calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(m_day));
                            onDatetimeChanged();
                        }
                    });
                } else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(-top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2);
                            m_day = (String) getItem(cvp);
                            m_calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(m_day));
                            onDatetimeChanged();
                        }
                    });
                }

                int fvp = view.getFirstVisiblePosition();
                int lvp = view.getLastVisiblePosition();
                int cvp = fvp + ((lvp - fvp) / 2);
                m_day = (String) getItem(cvp);
                m_calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(m_day));
                if (m_dtChangedCb != null) m_dtChangedCb.onChanged(m_year, m_month, m_day, m_hour, m_minute, m_second);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                view.setSelection(m_days.size());
            } else if (firstVisibleItem <= 2) {
                view.setSelection(m_days.size() + 2);
            } else if (firstVisibleItem + visibleItemCount > m_day_adapter.getCount() - 2) {//到底部添加数据
                view.setSelection(firstVisibleItem - m_days.size());
            }
        }

        public class ViewHolder {
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_days.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return m_days.get(position % m_days.size());
        }

        @Override
        public long getItemId(int position) {
            return position % m_days.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.datetime_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.tvCaption.setText(m_days.get(position % m_days.size()));
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        }
    }

    class HourAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<String> m_hours = new ArrayList<String>();


        public HourAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            setHoursRect(0, 23);
        }

        public void setHoursRect(int min, int max) {
            m_hours.clear();
            for (int i = min; i <= max; i++) {
                m_hours.add(String.format("%02d", i));
            }
        }

        public void setHour(final int hour) {
            m_calendar.set(Calendar.HOUR_OF_DAY, hour);
            m_hour = String.format("%02d", m_calendar.get(Calendar.HOUR_OF_DAY));
            m_lv_hour.post(new Runnable() {
                @Override
                public void run() {
                    int pos = hour;
                    int fvp = m_lv_hour.getFirstVisiblePosition();
                    int lvp = m_lv_hour.getLastVisiblePosition();
                    int diff = ((lvp - fvp) / 2);
                    pos = pos >= diff ? pos - diff : m_hours.size() + (pos - diff);
                    m_lv_hour.setSelection(pos);
                }
            });
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                int firstPostion = view.getFirstVisiblePosition();
                View firstChild = view.getChildAt(0);
                final int height = firstChild.getHeight();
                final int top = -(firstChild.getTop());
                if (top > (height / 2)) {
                    //view.smoothScrollToPosition(firstPostion + 1);
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(height - top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2) + 1;
                            m_hour = (String) getItem(cvp);
                            m_calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(m_hour));
                            onDatetimeChanged();
                        }
                    });
                } else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(-top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2);
                            m_hour = (String) getItem(cvp);
                            m_calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(m_hour));
                            onDatetimeChanged();
                        }
                    });
                }

                int fvp = view.getFirstVisiblePosition();
                int lvp = view.getLastVisiblePosition();
                int cvp = fvp + ((lvp - fvp) / 2);
                m_hour = (String) getItem(cvp);
                m_calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(m_hour));
                if (m_dtChangedCb != null) m_dtChangedCb.onChanged(m_year, m_month, m_day, m_hour, m_minute, m_second);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                view.setSelection(m_hours.size());
            } else if (firstVisibleItem <= 2) {
                view.setSelection(m_hours.size() + 2);
            } else if (firstVisibleItem + visibleItemCount > m_hour_adapter.getCount() - 2) {//到底部添加数据
                view.setSelection(firstVisibleItem - m_hours.size());
            }
        }

        public class ViewHolder {
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_hours.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return m_hours.get(position % m_hours.size());
        }

        @Override
        public long getItemId(int position) {
            return position % m_hours.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.datetime_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.tvCaption.setText(m_hours.get(position % m_hours.size()));
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        }
    }

    class MinutesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<String> m_minutes = new ArrayList<String>();


        public MinutesAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            setMinutesRect(0, 59);
        }

        public void setMinutesRect(int min, int max) {
            m_minutes.clear();
            for (int i = min; i <= max; i++) {
                m_minutes.add(String.format("%02d", i));
            }
        }

        public void setMinute(final int minute) {
            m_calendar.set(Calendar.MINUTE, minute);
            m_minute = String.format("%02d", m_calendar.get(Calendar.MINUTE));
            m_lv_minute.post(new Runnable() {
                @Override
                public void run() {
                    int pos = minute;
                    int fvp = m_lv_minute.getFirstVisiblePosition();
                    int lvp = m_lv_minute.getLastVisiblePosition();
                    int diff = ((lvp - fvp) / 2);
                    pos = pos >= diff ? pos - diff : m_minutes.size() + (pos - diff);
                    m_lv_minute.setSelection(pos);
                }
            });
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                int firstPostion = view.getFirstVisiblePosition();
                View firstChild = view.getChildAt(0);
                final int height = firstChild.getHeight();
                final int top = -(firstChild.getTop());
                if (top > (height / 2)) {
                    //view.smoothScrollToPosition(firstPostion + 1);
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(height - top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2) + 1;
                            m_minute = (String) getItem(cvp);
                            m_calendar.set(Calendar.MINUTE, Integer.valueOf(m_minute));
                            onDatetimeChanged();
                        }
                    });
                } else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(-top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2);
                            m_minute = (String) getItem(cvp);
                            m_calendar.set(Calendar.MINUTE, Integer.valueOf(m_minute));
                            onDatetimeChanged();
                        }
                    });
                }

                int fvp = view.getFirstVisiblePosition();
                int lvp = view.getLastVisiblePosition();
                int cvp = fvp + ((lvp - fvp) / 2);
                m_minute = (String) getItem(cvp);
                m_calendar.set(Calendar.MINUTE, Integer.valueOf(m_minute));
                if (m_dtChangedCb != null) m_dtChangedCb.onChanged(m_year, m_month, m_day, m_hour, m_minute, m_second);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                view.setSelection(m_minutes.size());
            } else if (firstVisibleItem <= 2) {
                view.setSelection(m_minutes.size() + 2);
            } else if (firstVisibleItem + visibleItemCount > m_minute_adapter.getCount() - 2) {//到底部添加数据
                view.setSelection(firstVisibleItem - m_minutes.size());
            }
        }

        public class ViewHolder {
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_minutes.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return m_minutes.get(position % m_minutes.size());
        }

        @Override
        public long getItemId(int position) {
            return position % m_minutes.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.datetime_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.tvCaption.setText(m_minutes.get(position % m_minutes.size()));
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        }
    }

    class SecondsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        private Context m_ctx;
        private LayoutInflater mInflater;
        private List<String> m_seconds = new ArrayList<String>();


        public SecondsAdapter(Context ctx) {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            setHoursRect(0, 59);
        }

        public void setHoursRect(int min, int max) {
            m_seconds.clear();
            for (int i = min; i <= max; i++) {
                m_seconds.add(String.format("%02d", i));
            }
        }

        public void setSecond(final int second) {
            m_calendar.set(Calendar.SECOND, second);
            m_second = String.format("%02d", m_calendar.get(Calendar.SECOND));
            m_lv_second.post(new Runnable() {
                @Override
                public void run() {
                    int pos = second;
                    int fvp = m_lv_second.getFirstVisiblePosition();
                    int lvp = m_lv_second.getLastVisiblePosition();
                    int diff = ((lvp - fvp) / 2);
                    pos = pos >= diff ? pos - diff : m_seconds.size() + (pos - diff);
                    m_lv_second.setSelection(pos);
                }
            });
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, int scrollState) {
            if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                int firstPostion = view.getFirstVisiblePosition();
                View firstChild = view.getChildAt(0);
                final int height = firstChild.getHeight();
                final int top = -(firstChild.getTop());
                if (top > (height / 2)) {
                    //view.smoothScrollToPosition(firstPostion + 1);
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(height - top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2) + 1;
                            m_second = (String) getItem(cvp);
                            m_calendar.set(Calendar.SECOND, Integer.valueOf(m_second));
                            onDatetimeChanged();
                        }
                    });
                } else {
                    view.post(new Runnable() {

                        @Override
                        public void run() {
                            view.smoothScrollBy(-top, 200);
                            int fvp = view.getFirstVisiblePosition();
                            int lvp = view.getLastVisiblePosition();
                            int cvp = fvp + ((lvp - fvp) / 2);
                            m_second = (String) getItem(cvp);
                            m_calendar.set(Calendar.SECOND, Integer.valueOf(m_second));
                            onDatetimeChanged();
                        }
                    });
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                view.setSelection(m_seconds.size());
            } else if (firstVisibleItem <= 2) {
                view.setSelection(m_seconds.size() + 2);
            } else if (firstVisibleItem + visibleItemCount > m_second_adapter.getCount() - 2) {//到底部添加数据
                view.setSelection(firstVisibleItem - m_seconds.size());
            }
        }

        public class ViewHolder {
            public TextView tvCaption;
        }

        @Override
        public int getCount() {
            return m_seconds.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return m_seconds.get(position % m_seconds.size());
        }

        @Override
        public long getItemId(int position) {
            return position % m_seconds.size();
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder viewHolder;
            if (v == null) {
                v = mInflater.inflate(R.layout.datetime_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tvCaption = (TextView) v.findViewById(R.id.tvCaption);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            viewHolder.tvCaption.setText(m_seconds.get(position % m_seconds.size()));
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        }
    }

    private void onDatetimeChanged() {
        Log.d(TAG, "year=" + m_year + ",month=" + m_month + ",day=" + m_day + ",hour=" + m_hour + ",minute=" + m_minute + ",second=" + m_second);
        if (m_dtChangedCb != null) m_dtChangedCb.onChanged(m_year, m_month, m_day, m_hour, m_minute, m_second);
    }
}

package com.seetong.app.seetong.ui.ext;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.exina.android.calendar.CalendarView;
import com.exina.android.calendar.Cell;
import com.seetong.app.seetong.R;

import java.util.Calendar;

/**
 * Created by Administrator on 2014-09-03.
 */
public class MyCalendar extends LinearLayout implements CalendarView.OnCellTouchListener {

    public interface OnCellTouchListener {
        public void onTouch(Cell cell);
    }

    View m_view = null;
    CalendarView m_calendar = null;
    OnCellTouchListener m_cellTouchListener = null;

    public MyCalendar(Context context) {
        this(context, null);
    }

    public MyCalendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MyCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_view = inflater.inflate(R.layout.my_calendar, null);
        m_calendar = (CalendarView) m_view.findViewById(R.id.calendar);
        m_calendar.setOnCellTouchListener(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        this.addView(m_view, params);
    }

    @Override
    public void onTouch(Cell cell) {
        if (m_cellTouchListener != null) m_cellTouchListener.onTouch(cell);
    }

    public void setOnCellTouchListener(OnCellTouchListener listener) {
        m_cellTouchListener = listener;
    }

    public int getYear() {
        return m_calendar.getYear();
    }

    public int getMonth() {
        return m_calendar.getMonth();
    }

    public Calendar getDate() {
        return m_calendar.getDate();
    }
}

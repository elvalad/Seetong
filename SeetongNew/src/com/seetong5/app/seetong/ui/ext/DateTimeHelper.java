package com.seetong5.app.seetong.ui.ext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class DateTimeHelper {
    private static final DateFormat mDateFormatter = DateFormat.getDateInstance();
    private static final DateFormat mDateTimeFormatter;
    private static final DateFormat mTimeFormatter = DateFormat.getTimeInstance();
    private static final DateFormat mTimeFormatterFull;
    private static final DateFormat mTimeFormatterGlue;
    private static final DateFormat mTimeFormatterShort;
    private static TimeZone mTimeZone;

    static {
        mDateTimeFormatter = DateFormat.getDateTimeInstance(3, 2);
        mTimeFormatterShort = new SimpleDateFormat("HH:mm");
        mTimeFormatterFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z");
        mTimeFormatterGlue = new SimpleDateFormat("ddMMyyHHmmss");
        mTimeZone = Calendar.getInstance().getTimeZone();
    }

    public static boolean belongToSameDay(long t1, long t2) {
        GregorianCalendar localGregorianCalendar1 = new GregorianCalendar();
        localGregorianCalendar1.setTimeInMillis(t1);
        GregorianCalendar localGregorianCalendar2 = new GregorianCalendar();
        localGregorianCalendar2.setTimeInMillis(t2);
        return (localGregorianCalendar1.get(Calendar.YEAR) == localGregorianCalendar2.get(Calendar.YEAR)) && (localGregorianCalendar1.get(Calendar.MONTH) == localGregorianCalendar2.get(Calendar.MONTH)) && (localGregorianCalendar1.get(Calendar.DAY_OF_MONTH) == localGregorianCalendar2.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean belongToSameMonth(long t1, long t2) {
        GregorianCalendar localGregorianCalendar1 = new GregorianCalendar();
        localGregorianCalendar1.setTimeInMillis(t1);
        GregorianCalendar localGregorianCalendar2 = new GregorianCalendar();
        localGregorianCalendar2.setTimeInMillis(t2);
        return (localGregorianCalendar1.get(Calendar.YEAR) == localGregorianCalendar2.get(Calendar.YEAR)) && (localGregorianCalendar1.get(Calendar.MONTH) == localGregorianCalendar2.get(Calendar.MONTH));
    }

    public static DateFormat formatDate() {
        return mDateFormatter;
    }

    public static DateFormat formatDateTime() {
        return mDateTimeFormatter;
    }

    public static DateFormat formatTime() {
        return mTimeFormatter;
    }

    public static DateFormat formatTimeFull() {
        return mTimeFormatterFull;
    }

    public static DateFormat formatTimeGlued() {
        return mTimeFormatterGlue;
    }

    public static DateFormat formatTimeShort() {
        return mTimeFormatterShort;
    }

    public static long getDayEndMark(long t) {
        GregorianCalendar localGregorianCalendar = new GregorianCalendar();
        localGregorianCalendar.setTime(new Date(t));
        localGregorianCalendar.set(Calendar.HOUR_OF_DAY, 23);
        localGregorianCalendar.set(Calendar.MINUTE, 59);
        localGregorianCalendar.set(Calendar.SECOND, 59);
        localGregorianCalendar.set(Calendar.MILLISECOND, 999);
        return localGregorianCalendar.getTimeInMillis();
    }

    public static long getDayStartMark(long t) {
        GregorianCalendar localGregorianCalendar = new GregorianCalendar(mTimeZone);
        localGregorianCalendar.setTime(new Date(t));
        localGregorianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localGregorianCalendar.set(Calendar.MINUTE, 0);
        localGregorianCalendar.set(Calendar.SECOND, 0);
        localGregorianCalendar.set(Calendar.MILLISECOND, 0);
        return localGregorianCalendar.getTimeInMillis();
    }

    public static int getDaysCountForMonth(int ind, boolean isLeap) {
        if ((ind < 0) || (ind > 11)) {
            throw new IllegalArgumentException("Invalid month index!");
        }
        switch (ind) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                return isLeap ? 29 : 28;
        }

        return 30;
    }

    public static long getMonthEndMark(long t)
            throws Exception {
        throw new Exception("Not implemented!");
    }

    public static long getMonthStartMark(long t) {
        GregorianCalendar localGregorianCalendar = new GregorianCalendar(mTimeZone);
        localGregorianCalendar.setTime(new Date(t));
        localGregorianCalendar.set(Calendar.DAY_OF_MONTH, 1);
        localGregorianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localGregorianCalendar.set(Calendar.MINUTE, 0);
        localGregorianCalendar.set(Calendar.SECOND, 0);
        localGregorianCalendar.set(Calendar.MILLISECOND, 0);
        return localGregorianCalendar.getTimeInMillis();
    }
}

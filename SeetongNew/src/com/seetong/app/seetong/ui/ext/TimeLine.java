package com.seetong.app.seetong.ui.ext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.seetong.app.seetong.model.ArchiveRecord;
import com.seetong.app.seetong.model.ObjectsRoster;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2014-10-24.
 */
public class TimeLine extends View {
    private static final int MAX_IMG_MOVE = 6;
    public static final short TMLN_FLG_DEFAULT = 0;
    public static final short TMLN_FLG_SHOW_DATE = 1;
    private static final String mImaginaryMovePattern = "^02{0,6}1$";
    private final float BOUND_MARKER_W = 36.0F;
    private final float OFFSET = 16.0F;
    private final double SCALE_RANGE_MAX = 240.0D;
    private final double SCALE_RANGE_MIN = 1.0D;
    private boolean drawDate = true;
    private TimelineActionHandler mActionHandler;
    private MotionEvent mCashedEvent;
    private int mDayIndex;
    private TimelineDelegate mDelegate;
    private float mDensScaleFactor;
    private short mDisplayFlags = 0;
    private float mFrameW;
    private float mFrameX;
    private StringBuilder mImaginaryMoveSequence;
    private boolean mIsEnabled = true;
    private GregorianCalendar mLimDate;
    private Paint mPaintBkg;
    private Paint mPaintDate;
    private int mPaintRecColor = 0xFF80FFFF;
    private int mPaintRecTinyColor = 0xFF80FFFF;
    private int mPaintSliderColor = 0xFFFF0000;
    private Paint mPaintRec;
    private Paint mPaintRecTiny;
    private Paint mPaintScratch;
    private Paint mPaintSlider;
    private ObjectsRoster<ArchiveRecord> mRecords;
    private ObjectsRoster<ArchiveRecord> mRecordsCashe;
    private ScaleGestureDetector mScaleDetector;
    private double mScaleFactor = SCALE_RANGE_MIN;
    private long mSliderTime;
    private GestureDetector mSwipeDetector;
    private int mViewH;
    private int mViewW;

    public TimeLine(Context context) {
        super(context);
        init(context);
    }

    public TimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setRecordColor(int color) {
        mPaintRecColor = color;
        mPaintRecTinyColor = color;
    }

    public void setIndicatorColor(int color) {
        mPaintSliderColor = color;
    }

    private ScaleDrawingParams calcScaleParams(double q) {
        ScaleDrawingParams res = new ScaleDrawingParams();
        if ((q >= 0.0D) && (q < 0.5D)) {
            res.nScratch = 1;
            res.sL = 2;
            res.sM = 0;
            res.sS = -1;
            res.tInterval = 240;
            res.sT = 4;
            return res;
        }
        if ((q >= 0.5D) && (q < 1.5D)) {
            res.nScratch = 2;
            res.sL = 4;
            res.sM = 2;
            res.sS = 0;
            res.tInterval = 120;
            res.sT = 4;
            return res;
        }
        if ((q >= 1.5D) && (q < 4.0D)) {
            res.nScratch = 6;
            res.sL = 6;
            res.sM = 3;
            res.sS = 0;
            res.tInterval = 60;
            res.sT = 6;
            return res;
        }
        if ((q >= 4.0D) && (q < 8.0D)) {
            res.nScratch = 12;
            res.sL = 6;
            res.sM = 3;
            res.sS = 0;
            res.tInterval = 30;
            res.sT = 6;
            return res;
        }
        if ((q >= 8.0D) && (q < 30.0D)) {
            res.nScratch = 60;
            res.sL = 10;
            res.sM = 5;
            res.sS = 0;
            res.tInterval = 10;
            res.sT = 10;
            return res;
        }
        if ((q >= 30.0D) && (q < 75.0D)) {
            res.nScratch = 120;
            res.sL = 10;
            res.sM = 2;
            res.sS = 0;
            res.tInterval = 5;
            res.sT = 10;
            return res;
        }

        res.nScratch = 360;
        res.sL = 6;
        res.sM = 3;
        res.sS = 0;
        res.tInterval = 1;
        res.sT = 6;
        return res;
    }

    private double calculateScale(float dx) {
        double acc = (4.0 * (mScaleFactor - SCALE_RANGE_MIN)) / 239.0;
        double k = (double) (dx / (float) mViewW);
        double dScale = SCALE_RANGE_MIN + ((SCALE_RANGE_MIN + acc) * k);
        mScaleFactor = (mScaleFactor * dScale);
        if (mScaleFactor > SCALE_RANGE_MAX) {
            dScale = (dScale * SCALE_RANGE_MAX) / mScaleFactor;
            mScaleFactor = SCALE_RANGE_MAX;
            return dScale;
        }
        if (mScaleFactor < SCALE_RANGE_MIN) {
            dScale = (dScale * SCALE_RANGE_MIN) / mScaleFactor;
            mScaleFactor = SCALE_RANGE_MIN;
        }
        return dScale;
    }

    private void drawBackground(Canvas c) {
        c.drawPaint(this.mPaintBkg);
    }

    private void drawDayScale(float xb, float xe, double hstep, ScaleDrawingParams p, int ind, Canvas c) {
        int SCRATCH_LEN_L = (int) (12.0f * mDensScaleFactor);
        int SCRATCH_LEN_M = (int) (9.0f * mDensScaleFactor);
        int SCRATCH_LEN_S = (int) (6.0f * mDensScaleFactor);
        float LABEL_BASE_H = (float) (int) (24.0f * mDensScaleFactor);
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        if ((mDisplayFlags & 0x1) != 0) {
            String dispDate = DateTimeHelper.formatDate().format(new Date(getCurrentTimestamp()));
            float dw = mPaintDate.measureText(dispDate);
            if (drawDate) {
                c.drawText(dispDate, ((((xe - xb) / 2.0f) + xb) - (dw / 2.0f)), ((float) (mViewH / 0x2) + (mPaintDate.getTextSize() / 2.0f)), mPaintDate);
            }
        }
        c.drawRect((xb - BOUND_MARKER_W), 0.0f, xb, (float) mViewH, mPaintScratch);
        c.drawRect(xe, 0.0f, (xe + BOUND_MARKER_W), (float) mViewH, mPaintScratch);
        int i = 0, len = 0;
        double step = hstep / (double) p.nScratch;
        double x = (double) ((OFFSET * mDensScaleFactor) + xb);
        double xee = (double) (xe - (OFFSET * mDensScaleFactor)) + 0.001D;
        while (x <= xee) {
            if ((x >= 0.0) && (x <= (double) mViewW)) {
                if ((i == 0) || ((i % p.sL) == 0)) {
                    len = SCRATCH_LEN_L;
                } else if ((p.sM == 0) || ((i % p.sM) == 0)) {
                    len = SCRATCH_LEN_M;
                } else if ((p.sS == 0) || ((i % p.sS) == 0)) {
                    len = SCRATCH_LEN_S;
                }
                c.drawLine((float) x, 0.0f, (float) x, (float) len, mPaintScratch);
                if ((i % p.sT) == 0) {
                    String dispTime = DateTimeHelper.formatTimeShort().format(gc.getTime());
                    if ((x + step) > (double) (xe - (OFFSET * mDensScaleFactor))) {
                        dispTime = "24:00";
                    }
                    float tw = mPaintScratch.measureText(dispTime);
                    if (drawDate) {
                        c.drawText(dispTime, ((float) x - (tw / 2.0f)), LABEL_BASE_H, mPaintScratch);
                    }
                }
            }
            x = x + step;
            if ((i % p.sT) == 0) {
                gc.add(Calendar.MINUTE, p.tInterval);
            }
            i += 1;
        }
    }

    private void drawRecords(float xb, float xe, Canvas c) {
        if ((mRecords != null) && (mRecords.objectCount() > 0)) {
            long t0 = getCurrentTimestamp();
            long t1 = DateTimeHelper.getDayStartMark(t0);
            long t2 = DateTimeHelper.getDayEndMark(t0);
            double fb = (double) ((OFFSET * mDensScaleFactor) + xb);
            double fe = (double) (xe - (OFFSET * mDensScaleFactor));
            int count = mRecords.objectCount();
            for (int i = 0; i < count; i++) {
                ArchiveRecord r = mRecords.objectAt(i);
                double rb = fb + ((fe - fb) * ((double) (r.startsAt() - t1) / (double) (t2 - t1)));
                double re = fb + ((fe - fb) * ((double) (r.startsAt() + r.duration() - t1) / (double) (t2 - t1)));
                if ((rb >= 0.0) && (rb < (double) mViewW)
                        || (re > 0.0) && (re <= (double) mViewW)
                        || (rb <= 0.0) && (re >= (double) mViewW)) {
                    mPaintRecTiny.setColor(mPaintRecTinyColor);
                    mPaintRec.setColor(mPaintRecColor);
                    if (r.getColorStartTime() != 0 && r.getColorEndTime() != 0) {
                        double rb2 = fb + ((fe - fb) * ((double) (r.getColorStartTime() - t1) / (double) (t2 - t1)));
                        double re2 = fb + ((fe - fb) * ((double) (r.getColorEndTime() - t1) / (double) (t2 - t1)));
                        if (rb2 >= rb && re2 <= re) {
                            mPaintRecTiny.setColor(r.getColor() & 0xBBFFFFFF);
                            mPaintRec.setColor(r.getColor());
                        }
                    }

                    if ((re - rb) < SCALE_RANGE_MIN) {
                        c.drawLine((float) rb, 0.0f, (float) rb, (float) mViewH, mPaintRecTiny);
                        continue;
                    }

                    c.drawRect((float) rb, 0.0f, (float) re, (float) mViewH, mPaintRec);
                }
            }
        }
    }

    private void drawScale(Canvas c) {
        double hstep = (double) (mFrameW - (32.0f * mDensScaleFactor)) / 24;
        double q = ((mScaleFactor - SCALE_RANGE_MIN) / 239.0) * 100.0;
        ScaleDrawingParams p = calcScaleParams(q);
        drawDayScale(mFrameX, (mFrameW + mFrameX), hstep, p, mDayIndex, c);
        if (mFrameX > BOUND_MARKER_W) {
            drawDayScale(((mFrameX - BOUND_MARKER_W) - mFrameW), (mFrameX - BOUND_MARKER_W), hstep, p, (mDayIndex - 1), c);
        }
        if (((mFrameX + mFrameW) + BOUND_MARKER_W) < (float) mViewW) {
            drawDayScale(((mFrameX + mFrameW) + BOUND_MARKER_W), (mFrameW + ((mFrameX + mFrameW) + BOUND_MARKER_W)), hstep, p, mDayIndex, c);
        }
    }

    private void drawSlider(float xb, float xe, Canvas c) {
        long t0 = getCurrentTimestamp();
        Long t1 = DateTimeHelper.getDayStartMark(t0);
        Long t2 = DateTimeHelper.getDayEndMark(t0);
        if ((mSliderTime >= t1) && (mSliderTime <= t2)) {
            double fb = (double) ((OFFSET * mDensScaleFactor) + xb);
            double fe = (double) (xe - (OFFSET * mDensScaleFactor));
            double pos = fb + ((fe - fb) * ((double) (mSliderTime - t1) / (double) (t2 - t1)));
            if ((pos >= 0.0) && (pos <= (double) mViewW)) {
                c.drawRect((float) (pos - SCALE_RANGE_MIN), 0.0f, (float) (SCALE_RANGE_MIN + pos), (float) mViewH, mPaintSlider);
            }
        }
    }

    private int findRecByTime(long t) {
        int res = -1;
        if((mRecords != null) && (mRecords.objectCount() > 0)) {
            int count = mRecords.objectCount();
            for(int i = 0; i < count; i++) {
                ArchiveRecord tmp = mRecords.objectAt(i);
                if(!tmp.includes(t)) continue;
                res = i;
                break;
            }
        }
        return res;
    }

    private long getCurrentTimestamp() {
        Calendar c = (Calendar) mLimDate.clone();
        c.add(Calendar.DAY_OF_MONTH, mDayIndex);
        return c.getTimeInMillis();
    }

    private void init(Context context) {
        DisplayMetrics localDisplayMetrics = context.getResources().getDisplayMetrics();
        this.mDensScaleFactor = localDisplayMetrics.scaledDensity;
        this.mLimDate = new GregorianCalendar();
        this.mDayIndex = 0;
        this.mSliderTime = 0L;
        this.mDisplayFlags = ((short) (0x1 | this.mDisplayFlags));
        initPainters();
        this.mSwipeDetector = new GestureDetector(context, new SwipeGestureListener());
        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleGestureListener());

        this.mActionHandler = new TimelineActionHandler() {
            private final int DAY_CHG_OFFSET = 300;
            public boolean mScaling = false;
            public boolean mSwiping = false;
            private float mLastFrameX = 0.0F;
            private float mLastFrameW = mViewW;
            private double mLastScaleFactor = SCALE_RANGE_MIN;

            private void adjustFramePosition() {
                if (mFrameX > BOUND_MARKER_W + DAY_CHG_OFFSET) {
                    switchToPrevDay();
                    return;
                } else if ((mFrameX + mFrameW) < (mViewW - BOUND_MARKER_W - DAY_CHG_OFFSET)) {
                    if (mDayIndex != 0) {
                        switchToNextDay();
                        return;
                    }
                }

                // 左右拖动时边界未进入mViewW的范围
                if (mFrameX < 0 && (mFrameX + mFrameW) > mViewW) return;

                if (!isSwiping()) return;
                mFrameX = mLastFrameX;
                mFrameW = mLastFrameW;
                mScaleFactor = mLastScaleFactor;
            }

            public boolean isScaling() {
                return this.mScaling;
            }

            public boolean isSwiping() {
                return this.mSwiping;
            }

            public void onInteractionFinished() {
                adjustFramePosition();
                this.mSwiping = false;
                this.mScaling = false;
                invalidate();
            }

            public void onScaling(float x0, float dx) {
                if ((dx > 0) && (mScaleFactor < SCALE_RANGE_MAX) || (dx < 0) && (mScaleFactor > SCALE_RANGE_MIN)) {
                    double dScale = calculateScale(dx);
                    mFrameW = (float)(mFrameW * dScale);
                    mFrameX = (float) ((double) x0 - ((double) (x0 - mFrameX) * dScale));
                    adjustFramePosition();
                    invalidate();
                }
            }

            public void onScalingStart() {
                this.mScaling = true;
                this.mSwiping = false;
            }

            public void onSwipingStart() {
                mSwiping = true;
                mScaling = false;
                mLastFrameX = mFrameX;
                mLastFrameW = mFrameW;
                mLastScaleFactor = mScaleFactor;
            }

            public void onSelection(float x0) {
                long l1 = getCurrentTimestamp();
                long start = DateTimeHelper.getDayStartMark(l1);
                long end = DateTimeHelper.getDayEndMark(l1);
                double d1 = mFrameX + OFFSET * mDensScaleFactor;
                double d2 = mFrameX + mFrameW - OFFSET * mDensScaleFactor;
                long l2 = (long) (start + (end - start) * ((x0 - d1) / (d2 - d1)));
                int i = findRecByTime(l2);
                if (i == -1) return;

                ArchiveRecord record = mRecords.objectAt(i);
                if ((mDelegate != null) && (record != null))
                {
                    mRecordsCashe = mRecords;
                    if (!record.includes(l2)) {
                        l2 = record.startsAt();
                    }

                    mDelegate.onRecordSelected(record, i, l2);
                }
            }

            public void onSwiping(float dx) {
                if ((mDayIndex == 0) && (dx + (mFrameX + mFrameW) < mViewW)) {
                    return;
                }

                mFrameX += dx;
                invalidate();
            }
        };
    }

    private void initPainters() {
        this.mPaintRec = new Paint();
        this.mPaintRec.setColor(mPaintRecColor);
        this.mPaintRec.setStyle(Paint.Style.FILL);
        this.mPaintRec.setStrokeWidth(0.0F);
        this.mPaintRecTiny = new Paint();
        this.mPaintRecTiny.setColor(mPaintRecTinyColor);
        this.mPaintRecTiny.setStyle(Paint.Style.FILL);
        this.mPaintRecTiny.setStrokeWidth(0.0F);
        this.mPaintSlider = new Paint();
        this.mPaintSlider.setColor(mPaintSliderColor);
        this.mPaintSlider.setStyle(Paint.Style.FILL);
        this.mPaintSlider.setStrokeWidth(0.0F);
        this.mPaintDate = new Paint();
        this.mPaintDate.setColor(0x77A4A4A4);
        this.mPaintDate.setStyle(Paint.Style.FILL);
        this.mPaintDate.setAntiAlias(true);
        this.mPaintDate.setTextSize(20.0F * this.mDensScaleFactor);
        this.mPaintDate.setTypeface(Typeface.create("Helvetica", 1));
        this.mPaintScratch = new Paint();
        this.mPaintScratch.setColor(0xFF383838);
        this.mPaintScratch.setStyle(Paint.Style.FILL);
        this.mPaintScratch.setAntiAlias(true);
        this.mPaintScratch.setStrokeWidth(0.0F);
        this.mPaintScratch.setTextSize(12.0F * this.mDensScaleFactor);
    }

    private void notifyAfterDateChange() {
        if (mDelegate != null) {
            mDelegate.onDateChanged(getCurrentTimestamp());
        }
    }

    private void switchToPrevDay() {
        mDayIndex = (mDayIndex - 1);
        mRecords = null;
        mFrameX = ((float) mViewW - mFrameW);
        notifyAfterDateChange();
    }

    private void switchToNextDay() {
        mDayIndex = (mDayIndex + 1);
        mRecords = null;
        mFrameX = 0.0f;
        notifyAfterDateChange();
    }

    public void applyZoom(boolean in) {
        if (mIsEnabled) {
            float focusX = (float) mViewW / 2.0f;
            float spanDefault = (float) mViewW / 8.0f;
            float zoomFactor = in ? 1.0f : -1.0f;
            mActionHandler.onScaling(focusX, (zoomFactor * spanDefault));
        }
    }

    public long date() {
        return getCurrentTimestamp();
    }

    public boolean drawDate() {
        return this.drawDate;
    }

    public ArchiveRecord getNext(int index)
    {
        ArchiveRecord rec = null;
        if(mRecordsCashe != null) {
            rec = mRecordsCashe.objectAt((index + 1));
        }
        return rec;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawRecords(mFrameX, mFrameX + mFrameW, canvas);
        drawSlider(mFrameX, mFrameX + mFrameW, canvas);
        drawScale(canvas);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float ratio = mViewW != 0 ? ((float) w / (float) mViewW) : 0;
        mViewW = w;
        mViewH = h;
        mPaintBkg = new Paint();
        mPaintBkg.setAntiAlias(true);
        int clWhite = -1;
        mPaintBkg.setColor(-1);
        mFrameX = (mFrameX * ratio);
        mFrameW = (float) mViewW;
        mFrameW = (float) ((double) mFrameW * mScaleFactor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean consumed = false;
        if (!mIsEnabled) return false;

        int actionCode = e.getAction();
        if (actionCode == MotionEvent.ACTION_DOWN) {
            if (mImaginaryMoveSequence == null) {
                mImaginaryMoveSequence = new StringBuilder(Integer.toString(actionCode));
            }
        }

        if (actionCode == MotionEvent.ACTION_UP) {
            if (mImaginaryMoveSequence != null) {
                mImaginaryMoveSequence.append(actionCode);
                if (Pattern.matches("^02{0,6}1$", mImaginaryMoveSequence.toString())) {
                    mActionHandler.onSelection(e.getX());
                    mImaginaryMoveSequence = null;
                    consumed = true;
                }
            }

            mActionHandler.onInteractionFinished();
        } else if (actionCode == MotionEvent.ACTION_MOVE) {
            if (mImaginaryMoveSequence != null) {
                if (mImaginaryMoveSequence.toString().length() <= 6) {
                    mImaginaryMoveSequence.append(actionCode);
                } else {
                    mImaginaryMoveSequence = null;
                }
            }
        }

        if ((!consumed) && (!mActionHandler.isScaling())) {
            consumed = mSwipeDetector.onTouchEvent(e);
        }

        if (!consumed) {
            consumed = mScaleDetector.onTouchEvent(e);
        }

        return consumed;
    }

    private void reset() {
        this.mFrameX = 0.0F;
        this.mFrameW = this.mViewW;
        this.mScaleFactor = SCALE_RANGE_MIN;
    }

    public void selectTime(long time) {
        ArchiveRecord rec = null;
        int recIndex = findRecByTime(time);
        if(recIndex != -1) {
            rec = mRecords.objectAt(recIndex);
        }
        if(mDelegate != null) {
            if(rec != null) {
                mRecordsCashe = null;
                mRecordsCashe = mRecords;
                mDelegate.onRecordSelected(rec, recIndex, time);
                return;
            }
            mDelegate.onRecordNotFound();
        }
    }

    public void setDelegate(TimelineDelegate delegate) {
        mDelegate = delegate;
    }

    public void setDisplayFlags(short flg, boolean on) {
        if (flg == 0) {
            mDisplayFlags = 0;
            return;
        }
        if (on) {
            mDisplayFlags = (short) (mDisplayFlags | flg);
            return;
        }

        mDisplayFlags = ((short) (mDisplayFlags & (~flg)));
    }

    public void setDisplayedDate(long date) {
        if (date < mLimDate.getTimeInMillis()) {
            long diff = mLimDate.getTimeInMillis() - date;
            mDayIndex = (int) (-diff / 86400000L);
            invalidate();
        }
    }

    public void setDrawDate(boolean value) {
        drawDate = value;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void setRecords(ObjectsRoster<ArchiveRecord> rec)
    {
        mRecords = null;
        if(rec != null) {
            mRecords = correctOverlappedRec(rec, getCurrentTimestamp());
        }
        invalidate();
    }

    public void setSliderTimestamp(long t) {
        mSliderTime = t;
        invalidate();
    }

    public long getSliderTimestamp() {
        return mSliderTime;
    }

    private ObjectsRoster<ArchiveRecord> correctOverlappedRec(ObjectsRoster<ArchiveRecord> rec, long day) {
        ObjectsRoster<ArchiveRecord> res = new ObjectsRoster<ArchiveRecord>();
        long tmin = DateTimeHelper.getDayStartMark(day);
        long tmax = DateTimeHelper.getDayEndMark(day);
        int count = rec.objectCount();
        for (int i = 0; i < count; i++) {
            ArchiveRecord r = rec.objectAt(i);
            long dur = r.duration();
            long tb = r.startsAt();
            long te = tb + dur;
            ArchiveRecord ar = r;
            if(tb < tmin) {
                ar = new ArchiveRecord(r.idStart(), r.idEnd(), tmin, (dur - (tmin - tb)), r.getName(), "", r.getDir(), r.getDevId(), r.getSize());
            } else if(te > tmax) {
                ar = new ArchiveRecord(r.idStart(), r.idEnd(), tb, (dur - (te - tmax)), r.getName(), "", r.getDir(), r.getDevId(), r.getSize());
            }

            ar.setLocalName(r.getLocalName());
            ar.setRecType(r.getRecType());
            res.objectAdd(ar, false);
        }
        return res;
    }

    private class ScaleDrawingParams {
        public int nScratch = 1;
        public int sL;
        public int sM;
        public int sS;
        public int sT;
        public int tInterval;

        private ScaleDrawingParams() {
        }
    }

    private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        private float mFocusX;
        private float mSpanPrev;

        private ScaleGestureListener() {
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float span = detector.getCurrentSpan();
            float delta = span - mSpanPrev;
            mSpanPrev = span;
            mActionHandler.onScaling(mFocusX, delta);
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mFocusX = detector.getFocusX();
            mSpanPrev = detector.getCurrentSpan();
            mActionHandler.onScalingStart();
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector paramScaleGestureDetector) {
        }
    }

    private class SwipeGestureListener implements GestureDetector.OnGestureListener {
        private boolean mIsScroling;
        private float mPrevX;

        private SwipeGestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            mPrevX = e.getX();
            mIsScroling = true;
            mActionHandler.onSwipingStart();
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mIsScroling = false;
            mActionHandler.onInteractionFinished();
            return true;
        }

        public void onLongPress(MotionEvent paramMotionEvent) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mIsScroling) {
                float dx = e2.getX() - mPrevX;
                mPrevX = e2.getX();
                mActionHandler.onSwiping(dx);
            }
            return false;
        }

        public void onShowPress(MotionEvent paramMotionEvent) {
        }

        public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
            return false;
        }
    }

    private static abstract interface TimelineActionHandler {
        public abstract boolean isScaling();
        public abstract boolean isSwiping();
        public abstract void onInteractionFinished();
        public abstract void onScaling(float x0, float dx);
        public abstract void onScalingStart();
        public abstract void onSelection(float x0);
        public abstract void onSwiping(float dx);
        public abstract void onSwipingStart();
    }

    public static abstract interface TimelineDelegate {
        public abstract void onDateChanged(long time);
        public abstract void onRecordNotFound();
        public abstract void onRecordSelected(ArchiveRecord record, int index, long time);
    }
}

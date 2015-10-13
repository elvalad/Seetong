package com.seetong5.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.ComparatorFile;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.comm.Tools;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.*;

/**
 * Created by Administrator on 2014-07-28.
 */
public class ImageFile extends BaseActivity implements View.OnClickListener/*, ViewSwitcher.ViewFactory*/ {
    private TextView m_txtImgPath;
    //private ImageSwitcher m_imgSwitcher;
    private ImageView m_imgView;
    //private LinearLayout m_layout;
    //private HorizontalScrollView m_hScrollView;
    private List<File> m_lstFile = new ArrayList<>();
    //private Map<Integer, ImageView> m_lstImgView = new HashMap<>();
    private int m_currentIndex = 0;

    private Gallery mGallery;
    private ImageAdapt mImageAdapt;
    private Bitmap mCurBitmap;
    private int mCurBmpPos;
    private int mItemHeight =220;

    private Button m_btn_right;
    private PopupWindow m_menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_file);
        m_txtImgPath = (TextView) findViewById(R.id.txt_image_path);
        m_imgView = (ImageView) findViewById(R.id.img_view);
        m_imgView.setOnTouchListener(new TouchListener());
        /*m_imgSwitcher = (ImageSwitcher) findViewById(R.id.img_switcher);
        m_imgSwitcher.setFactory(this);
        m_imgSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        m_imgSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));*/
        m_btn_right = (Button) findViewById(R.id.btn_title_right);
        m_btn_right.setText(R.string.more);
        m_btn_right.setVisibility(View.VISIBLE);
        m_btn_right.setOnClickListener(this);
        //m_hScrollView = (HorizontalScrollView) findViewById(R.id.h_scroll_view);
        //m_layout = (LinearLayout) findViewById(R.id.layout_image_file);

        initImageFile(Global.getImageDir());

        mGallery = (Gallery) findViewById(R.id.picturesTaken);
        mImageAdapt = new ImageAdapt(this);
        mGallery.setOnItemSelectedListener(mImageAdapt);
        mGallery.setAdapter(mImageAdapt);

        setCurrentFile(m_currentIndex);

        View menu = getLayoutInflater().inflate(R.layout.image_file_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_share).setOnClickListener(this);
        menu.findViewById(R.id.btn_delete).setOnClickListener(this);
    }

    private void initImageFile(String path) {
        m_lstFile.clear();
        //m_lstImgView.clear();
        //m_layout.removeAllViews();
        File file = new File(path);
        if (!file.exists()) return;

        File[] fileAry = file.listFiles(Tools.getFilenameFilter(Define.ImageExts));

        if (fileAry != null && fileAry.length > 0) {
            List<File> lstFile = new ArrayList<>();
            for (File f : fileAry) {
                if (f.isFile() && !f.isHidden()) {
                    lstFile.add(f);
                }
            }
            ComparatorFile cmpFile = new ComparatorFile();
            Collections.sort(lstFile, cmpFile);

            int index = 0;
            for (File f : lstFile) {
                /*ImageView v = new ImageView(this);
                v.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
                v.setBackgroundResource(R.drawable.bg_border);
                v.setPadding(3, 3, 3, 3);
                v.setScaleType(ImageView.ScaleType.CENTER);
                v.setTag(index);
                v.setOnClickListener(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 10, 10, 10);
                m_layout.addView(v, params);
                m_lstImgView.put(index, v);*/
                m_lstFile.add(f);
                index++;
            }
        }

        if (m_lstFile != null && m_lstFile.size() < 1) {
            toast(R.string.album_no_any_picture);
        }
    }

    private void setCurrentFile(int pos) {
        int size = m_lstFile.size();
        if (size < 1) {
            m_btn_right.setVisibility(View.GONE);
            m_txtImgPath.setText("");
            m_imgView.setImageDrawable(null);
            Toast.makeText(this, R.string.album_no_any_picture, Toast.LENGTH_SHORT).show();
            return;
        }

        m_btn_right.setVisibility(View.VISIBLE);
        File file = m_lstFile.get(pos);
        if (null == file) return;
        String filePath = file.getAbsolutePath();
        m_txtImgPath.setText(filePath);
        Drawable d = Drawable.createFromPath(filePath);
        //m_imgSwitcher.setImageDrawable(d);
        m_imgView.setImageDrawable(d);
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
                //m_currentIndex = (int) v.getTag();
                //setCurrentFile();
            /*if (null != m_imgViewPrev) {
                m_imgViewPrev.setBackgroundResource(R.drawable.bg_border);
            }
            v.setBackgroundResource(R.drawable.bg_border1);
            m_imgViewPrev = v;*/

                //m_imgSwitcher.setImageDrawable(Drawable.createFromPath(filePath));
        }
    }

    private void onBtnDelete(final View v) {
        new AlertDialog.Builder(this)
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
                }).create().show();
    }

    private void onBtnShare(View v) {
        File file = m_lstFile.get(m_currentIndex);
        if (null == file) return;
        shareImage(file.getPath(), "");
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

    private void deleteImg() {
        int size = m_lstFile.size();
        if (size < 1) {
            Toast.makeText(this, R.string.album_no_any_picture, Toast.LENGTH_SHORT).show();
            return;
        }

        m_lstFile.get(mCurBmpPos).delete();
        m_lstFile.remove(mCurBmpPos);
        mImageAdapt.notifyDataSetChanged();

        int pos = (m_lstFile.size() < 1) ? -1 : mCurBmpPos % m_lstFile.size();
        setCurrentFile(pos);

        /*File file = m_lstFile.get(m_currentIndex);
        if (null == file) return;
        ImageView v = m_lstImgView.get(m_currentIndex);
        if (null == v) return;

        m_lstImgView.remove(m_currentIndex);
        m_layout.removeView(v);
        m_layout.requestLayout();
        m_hScrollView.requestLayout();
        m_layout.invalidate();
        m_hScrollView.invalidate();
        findViewById(R.id.layout_body).invalidate();

        *//*v.invalidate();
        v.postInvalidate();
        v.forceLayout();
        v.refreshDrawableState();
        m_layout.invalidate();
        m_layout.forceLayout();
        m_layout.requestFocusFromTouch();
        m_layout.postInvalidate();
        m_layout.refreshDrawableState();
        m_hScrollView.invalidate();
        m_hScrollView.requestFocusFromTouch();
        m_hScrollView.forceLayout();
        m_hScrollView.requestLayout();
        m_hScrollView.scrollTo(0, 0);
        m_hScrollView.refreshDrawableState();*//*

        m_lstFile.remove(m_currentIndex);
        file.delete();

        initImageFile(Define.ImageDirPath);
        m_layout.getChildAt(0).performClick();

        *//*v.invalidate();
        v.postInvalidate();
        v.forceLayout();
        v.refreshDrawableState();
        m_layout.invalidate();
        m_layout.forceLayout();
        m_layout.requestFocusFromTouch();
        m_layout.postInvalidate();
        m_layout.refreshDrawableState();
        m_hScrollView.invalidate();
        m_hScrollView.requestFocusFromTouch();
        m_hScrollView.forceLayout();
        m_hScrollView.requestLayout();
        m_hScrollView.scrollTo(0, 0);
        m_hScrollView.refreshDrawableState();*//*

        *//*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        m_layout.setLayoutParams(params);*//*

        //v = m_lstImgView.get(m_lstImgView.keySet().toArray()[0]);
        //m_currentIndex =
        //setCurrentFile();*/
    }

    private class ImageAdapt extends BaseAdapter implements AdapterView.OnItemSelectedListener {
        private final Context mContext;
        private final int mGalleryItemBackground;

        public ImageAdapt(Context context) {
            mContext = context;
            //设置背景风格，Gallery背景风格定义在attrs.xml中
            TypedArray typedArray = mContext.obtainStyledAttributes(R.styleable.Gallery);
            mGalleryItemBackground = typedArray.getResourceId(0, 0);
            typedArray.recycle();
        }

        @Override
        public int getCount() {
            return m_lstFile.size();
        }

        @Override
        public Object getItem(int position) {
            return m_lstFile.get(position).getAbsolutePath();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            try {
                ImageView img = new ImageView(mContext);
                // Bitmap viewBitmap = BitmapFactory.decodeFile(mPathImages.get(position));
                SoftReference<Bitmap> srBitmap = new SoftReference<Bitmap>(Tools.loadOrigPic(m_lstFile.get(position).getAbsolutePath(), 220, 220));
                Bitmap viewBitmap = srBitmap.get();
                img.setImageBitmap(viewBitmap);
                img.setLayoutParams(new Gallery.LayoutParams(mItemHeight, mItemHeight));
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                img.setBackgroundResource(mGalleryItemBackground);
                return img;
            } catch (OutOfMemoryError err) {
                //err.printStackTrace();
                v = new ImageView(mContext);
                ((ImageView) v).setImageResource(R.drawable.fourview_2);
                v.setLayoutParams(new Gallery.LayoutParams(mItemHeight, mItemHeight));
                ((ImageView) v).setScaleType(ImageView.ScaleType.FIT_XY);
                v.setBackgroundResource(mGalleryItemBackground);
                return v;
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCurBmpPos = position;
            setCurrentFile(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    /*@Override
    public View makeView() {
        ImageView i = new ImageView(this);
        i.setBackgroundColor(0xFFFFFF);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
        i.setOnTouchListener(new TouchListener());
        return i;
    }*/

    private class TouchListener implements View.OnTouchListener {
        private PointF startPoint = new PointF();
        private Matrix matrix = new Matrix();
        private Matrix currentMatrix = new Matrix();
        private int mode = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private float startDis;//开始距离
        private PointF midPoint;//中间点
        private int m_offset = 10;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            boolean m_left_out = false;
            boolean m_top_out = false;
            boolean m_right_out = false;
            boolean m_bottom_out = false;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN://手指压下屏幕
                    mode = DRAG;
                    currentMatrix.set(imageView.getImageMatrix());//记录ImageView当前的移动位置
                    startPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE://手指在屏幕移动，该事件会不断地触发
                    if(mode == DRAG) {
                        if (imageView.getScaleType() == ImageView.ScaleType.FIT_CENTER) return true;
                        float dx = event.getX() - startPoint.x;//得到在x轴的移动距离
                        float dy = event.getY() - startPoint.y;//得到在y轴的移动距离

                        int w = imageView.getWidth();
                        int h = imageView.getHeight();

                        // 测试拖动到新位置
                        Matrix m = new Matrix();
                        m.set(currentMatrix);
                        m.postTranslate(dx, dy);

                        RectF rcf = new RectF();
                        Rect rect = imageView.getDrawable().getBounds();
                        float values[] = new float[9];
                        m.getValues(values);
                        rcf.left = values[2];
                        rcf.top = values[5];
                        rcf.right = rcf.left + rect.width() * values[0];
                        rcf.bottom = rcf.top + rect.height() * values[0];

                        // 向右拖动
                        if (dx > 0) {
                            // 限制左边界
                            if (rcf.width() > w && rcf.left > m_offset) {
                                values[2] = m_offset;
                                m_left_out = true;
                            } else if (rcf.width() <= h && rcf.left > m_offset) {
                                values[2] = m_offset;
                                m_top_out = true;
                            } else {
                                m_left_out = false;
                                matrix.set(currentMatrix);
                            }
                        }

                        // 向左拖动
                        if (dx < 0) {
                            // 左边界为 -(图片宽度-视图宽度+偏移量)
                            float x = -(rcf.width() - w + m_offset);
                            if (rcf.width() > w && rcf.left < x) {
                                values[2] = x;
                                m_right_out = true;
                            } else if (rcf.width() <= h && rcf.right > (w - m_offset)) {
                                values[2] = w - rcf.width() - m_offset;
                                m_top_out = true;
                            } else {
                                m_right_out = false;
                                matrix.set(currentMatrix);
                            }
                        }

                        if (dy > 0) {
                            if (rcf.height() > h && rcf.top > m_offset) {
                                values[5] = m_offset;
                                m_top_out = true;
                            } else if (rcf.height() <= h && rcf.bottom > (h - m_offset)) {
                                values[5] = h - rcf.height() - m_offset;
                                m_top_out = true;
                            } else {
                                m_top_out = false;
                                matrix.set(currentMatrix);
                            }
                        }

                        if (dy < 0) {
                            float y = -(rcf.height() - h + m_offset);
                            if (rcf.height() > h && rcf.top < y) {
                                values[5] = y;
                                m_bottom_out = true;
                            } else if (rcf.height() <= h && rcf.top < m_offset) {
                                values[5] = m_offset;
                                m_bottom_out = true;
                            } else {
                                m_bottom_out = false;
                                matrix.set(currentMatrix);
                            }
                        }

                        if (m_left_out || m_top_out || m_right_out || m_bottom_out) {
                            matrix.setValues(values);
                            imageView.setImageMatrix(matrix);
                            return true;
                        }

                        matrix.postTranslate(dx, dy);
                    }else if(mode == ZOOM) {//缩放
                        float endDis = distance(event);//结束距离
                        if(endDis > 10f) {
                            float scale = endDis / startDis;//得到缩放倍数
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP://手指离开屏
                    mode = 0;
                    // 当缩小时恢复成默认的
                    int w = imageView.getWidth();
                    int h = imageView.getHeight();
                    RectF rcf = new RectF();
                    Rect rect = imageView.getDrawable().getBounds();
                    float values[] = new float[9];
                    matrix.getValues(values);
                    rcf.left = values[2];
                    rcf.top = values[5];
                    rcf.right = rcf.left + rect.width() * values[0];
                    rcf.bottom = rcf.top + rect.height() * values[0];
                    if (rcf.width() < w && rcf.height() < h) {
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP://有手指离开屏幕,但屏幕还有触点（手指）
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN://当屏幕上已经有触点（手指），再有一个手指压下屏幕
                    mode = ZOOM;
                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
                    startDis = distance(event);
                    if(startDis > 10f) {
                        midPoint = mid(event);
                        currentMatrix.set(imageView.getImageMatrix());//记录ImageView当前的缩放倍数
                    }
                    break;
            }

            imageView.setImageMatrix(matrix);
            return true;
        }
    }

    /**
     * 计算两点之间的距离
     * @param event
     * @return
     */
    public static float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return FloatMath.sqrt(dx * dx + dy * dy);
    }
    /**
     * 计算两点之间的中间点
     * @param event
     * @return
     */
    public static PointF mid(MotionEvent event){
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
}
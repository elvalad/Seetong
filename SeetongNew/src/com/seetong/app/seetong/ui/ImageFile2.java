package com.seetong.app.seetong.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.*;
import android.widget.*;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.ComparatorFile;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.comm.Tools;

import java.io.File;
import java.util.*;

/**
 * Created by Administrator on 2014-07-28.
 */
public class ImageFile2 extends BaseActivity implements View.OnClickListener {
    private ViewPager m_pager;
    private TextView m_filepath;
    private MyPagerAdapter m_adapter;
    private GridView m_grid;
    private ImageAdapter m_grid_adapter;
    boolean m_is_check[];
    Map<Integer, File> m_check_del = new HashMap<>();
    Map<Integer, Bitmap> m_bmpCache = new HashMap<>();

    private LinearLayout m_toolbar;

    private Button m_btn_right;
    private PopupWindow m_menu;

    private boolean m_is_layout_land = false;

    private int m_currentIndex = 0;
    private List<File> m_lstFile = new ArrayList<>();

    private TouchListener m_touchListener = new TouchListener();
    private GestureDetector m_gesture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_file2);
        m_pager = (ViewPager) findViewById(R.id.pager);

        m_filepath = (TextView) findViewById(R.id.txt_image_path);

        //m_imgView.setOnTouchListener(new TouchListener());

        m_btn_right = (Button) findViewById(R.id.btn_title_right);
        m_btn_right.setText(R.string.more);
        m_btn_right.setOnClickListener(this);

        m_gesture = new GestureDetector(this, new MyGestureListener());

        initImageFile(Global.getImageDir());

        m_toolbar = (LinearLayout) findViewById(R.id.layout_toolbar);
        m_toolbar.findViewById(R.id.btn_multi_delete).setOnClickListener(this);

        m_grid = (GridView) findViewById(R.id.grid_images);
        m_grid_adapter = new ImageAdapter(this);
        m_grid.setAdapter(m_grid_adapter);
        /*m_grid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        m_grid.setMultiChoiceModeListener(this);*/

        m_adapter = new MyPagerAdapter(null);
        m_pager.setAdapter(m_adapter);

        m_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                m_currentIndex = i;
                m_filepath.setText(m_lstFile.get(i).getAbsolutePath());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        View menu = getLayoutInflater().inflate(R.layout.image_file_menu, null);
        m_menu = new PopupWindow(menu, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menu.findViewById(R.id.btn_share).setOnClickListener(this);
        menu.findViewById(R.id.btn_delete).setOnClickListener(this);
    }

    private void initImageFile(String path) {
        m_lstFile.clear();
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

            for (File f : lstFile) {
                m_lstFile.add(f);
            }
        }

        if (m_lstFile != null && m_lstFile.size() < 1) {
            m_btn_right.setVisibility(View.GONE);
            toast(R.string.album_no_any_picture);
            return;
        }

        m_filepath.setText(m_lstFile.get(0).getAbsolutePath());
    }



    @Override
    public void onBackPressed() {
        if (m_pager.isShown()) {
            m_btn_right.setVisibility(View.GONE);
            m_pager.setVisibility(View.GONE);
            m_filepath.setVisibility(View.GONE);
            m_grid.setVisibility(View.VISIBLE);
            return;
        }

        if (m_grid_adapter.m_check) {
            exitCheckMode();
            return;
        }

        super.onBackPressed();
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
            case R.id.btn_delete:
                m_menu.dismiss();
                onBtnDelete(v);
                break;
            case R.id.btn_multi_delete:
                onBtnMultiDelete();
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
        int size = m_lstFile.size();
        if (size < 1) {
            m_btn_right.setVisibility(View.GONE);
            Toast.makeText(this, R.string.album_no_any_picture, Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void onBtnMultiDelete() {
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
                        for (Integer key : m_check_del.keySet()) {
                            File f = m_check_del.get(key);
                            m_lstFile.remove(f);
                            f.delete();
                            m_bmpCache.remove(key);
                        }

                        m_adapter.notifyDataSetChanged();
                        exitCheckMode();
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

        m_touchListener.m_zoomed = false;
        m_lstFile.get(m_currentIndex).delete();
        m_lstFile.remove(m_currentIndex);
        m_adapter.notifyDataSetChanged();

        /*int pos = (m_lstFile.size() < 1) ? -1 : m_currentIndex % m_lstFile.size();
        setCurrentFile(pos);*/
    }

    public void exitCheckMode() {
        m_grid_adapter.m_check = false;
        m_grid_adapter.clearCheckStatus();
        m_toolbar.setVisibility(View.GONE);
    }

    public class MyPagerAdapter extends PagerAdapter {

        private Map<Integer, View> m_views = new HashMap<>();

        public MyPagerAdapter(ArrayList<View> views) {

        }

        @Override
        public int getCount() {
            return m_lstFile.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
            container.removeView(m_views.get(position));
            m_views.put(position, null);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (null != m_views.get(position)) return m_views.get(position);
            ImageView v = new ImageView(ImageFile2.this);
            Drawable d = Drawable.createFromPath(m_lstFile.get(position).getAbsolutePath());
            v.setImageDrawable(d);
            v.setScaleType(ImageView.ScaleType.FIT_CENTER);
            v.setOnTouchListener(m_touchListener);
            v.setTag(position);
            container.addView(v);
            m_views.put(position, v);
            return v;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
            /*View v = m_pager.findViewWithTag(m_currentIndex);
            if (null != v) return POSITION_NONE;
            //View v = (View) object;
            //v.findViewWithTag(1)
            //if (null != v.getTag() && ((int)v.getTag() == 1)) return POSITION_NONE;
            return super.getItemPosition(object);*/
        }
    }

    public class GridItem extends RelativeLayout implements Checkable {

        private Context mContext;
        private boolean mChecked;
        public ImageView m_img_thumb = null;
        public ImageView m_img_check = null;

        public GridItem(Context context) {
            this(context, null, 0);
        }

        public GridItem(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public GridItem(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mContext = context;
            LayoutInflater.from(mContext).inflate(R.layout.image_file_grid_item, this);
            m_img_thumb = (ImageView) findViewById(R.id.img_thumb);
            m_img_check = (ImageView) findViewById(R.id.img_check);
        }

        @Override
        public void setChecked(boolean checked) {
            mChecked = checked;
            m_img_check.setImageResource(mChecked ? R.drawable.btn_checked_1 : R.drawable.btn_checked_0);
            m_img_check.setVisibility(checked ? View.VISIBLE : View.GONE);
        }

        @Override
        public boolean isChecked() {
            return mChecked;
        }

        @Override
        public void toggle() {
            setChecked(!mChecked);
        }

        public void showCheckBox(boolean show) {
            m_img_check.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        public void setImgResId(int resId) {
            if (m_img_thumb != null) {
                m_img_thumb.setBackgroundResource(resId);
            }
        }

        public void setImageBitmap(Bitmap bmp) {
            if (m_img_thumb != null) {
                m_img_thumb.setImageBitmap(bmp);
            }
        }
    }


    class ImageAdapter extends BaseAdapter
    {
        private Context m_ctx;
        private LayoutInflater mInflater;
        public boolean m_check = false;

        public ImageAdapter(Context ctx)
        {
            m_ctx = ctx;
            mInflater = LayoutInflater.from(ctx);
            m_is_check = new boolean[m_lstFile.size()];
        }

        // 获取图片的个数
        public int getCount()
        {
            return m_lstFile.size();
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


        public View getView(final int position, View v, ViewGroup parent)
        {
            final GridItem item;
            if (v == null) {
                item = new GridItem(m_ctx);
                item.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.MATCH_PARENT));
            } else {
                item = (GridItem) v;
            }

            final File file = m_lstFile.get(position);
            if (null == file) return v;

            String fileName = file.getAbsolutePath();

            Bitmap bmp = m_bmpCache.get(position);
            if (null == bmp) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(fileName, opts);
                opts.inSampleSize = Math.max((int) (opts.outHeight / (float) 256), (int) (opts.outWidth / (float) 256));
                opts.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeFile(fileName, opts);
                m_bmpCache.put(position, bmp);

                /*Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeFile(fileName);
                    if (null == bmp) bmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
                } catch (OutOfMemoryError err) {
                    // err.printStackTrace();
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
                }*/
            }

            item.setImageBitmap(bmp);
            item.setChecked(m_is_check[position]);
            item.showCheckBox(m_check);

            item.m_img_thumb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (m_check) {
                        m_is_check[position] = !m_is_check[position];
                        if (m_is_check[position]) {
                            m_check_del.put(position, m_lstFile.get(position));
                        } else {
                            m_check_del.remove(position);
                        }

                        if (m_check_del.isEmpty()) {
                            exitCheckMode();
                        } else {
                            notifyDataSetChanged();
                        }
                    } else {
                        m_btn_right.setVisibility(View.VISIBLE);
                        m_pager.setCurrentItem(position);
                        m_grid.setVisibility(View.GONE);
                        m_pager.setVisibility(View.VISIBLE);
                        m_filepath.setVisibility(View.VISIBLE);
                    }
                }
            });

            item.m_img_thumb.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    m_check = true;
                    m_is_check[position] = true;
                    m_toolbar.setVisibility(View.VISIBLE);
                    m_check_del.put(position, m_lstFile.get(position));
                    notifyDataSetChanged();
                    return true;
                }
            });

            return item;
        }

        public void clearCheckStatus() {
            for (int i = 0; i < m_is_check.length; i++) {
                m_is_check[i] = false;
            }

            notifyDataSetChanged();
        }

        private LayerDrawable getItemImage(int pos) {

            Bitmap bitmap = m_bmpCache.get(pos);
            Bitmap bitmap2=null;
            LayerDrawable la=null;
            if (m_check) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inScaled = true;
                if (m_is_check[pos]) {
                    bitmap2 = BitmapFactory.decodeResource(m_ctx.getResources(), R.drawable.btn_checked_1);
                } else {
                    bitmap2 = BitmapFactory.decodeResource(m_ctx.getResources(), R.drawable.btn_checked_0);
                }
            }
            if (bitmap2!=null) {
                Drawable[] array = new Drawable[2];
                array[0] = new BitmapDrawable(m_ctx.getResources(), bitmap);
                array[1] = new BitmapDrawable(m_ctx.getResources(), bitmap2);
                la= new LayerDrawable(array);
                la.setLayerInset(0, 0, 0, 0, 0);   //第几张图离各边的间距
                la.setLayerInset(1, 0, 0, 0, 0);
            }
            else {
                Drawable[] array = new Drawable[1];
                array[0] = new BitmapDrawable(m_ctx.getResources(), bitmap);
                la= new LayerDrawable(array);
                la.setLayerInset(0, 0, 0, 0, 0);
            }
            return la; // 返回叠加后的图
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
        private float m_last_x = 0;

        public boolean m_zoomed = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (m_zoomed) {
                imageView.getParent().requestDisallowInterceptTouchEvent(true);
            } else {
                imageView.getParent().requestDisallowInterceptTouchEvent(false);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
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

                        if (m_left_out) {
                            float x1 = event.getX(0);
                            float x2 = event.getX(event.getPointerCount() - 1);
                            if (Math.max(Math.abs(dx), Math.abs(m_last_x)) - Math.min(Math.abs(dx), Math.abs(m_last_x)) > 130) {
                                m_zoomed = false;
                                imageView.getParent().requestDisallowInterceptTouchEvent(false);
                                return true;
                            }
                        } else if (m_right_out) {
                            float x1 = event.getX(0);
                            float x2 = event.getX(event.getPointerCount() - 1);
                            if (Math.max(Math.abs(dx), Math.abs(m_last_x)) - Math.min(Math.abs(dx), Math.abs(m_last_x)) > 130) {
                                m_zoomed = false;
                                imageView.getParent().requestDisallowInterceptTouchEvent(false);
                                return true;
                            }
                        }

                        m_last_x = dx;

                        if (m_left_out || m_top_out || m_right_out || m_bottom_out) {
                            matrix.setValues(values);
                            imageView.setImageMatrix(matrix);
                            return true;
                        }

                        matrix.postTranslate(dx, dy);
                    }else if(mode == ZOOM) {//缩放
                        float endDis = distance(event);//结束距离
                        if(endDis > 10f) {
                            imageView.setScaleType(ImageView.ScaleType.MATRIX);
                            float scale = endDis / startDis;//得到缩放倍数
                            float values[] = new float[9];
                            currentMatrix.getValues(values);
                            float val1 = values[0];

                            Matrix m = new Matrix();
                            m.set(currentMatrix);
                            m.postScale(scale, scale, midPoint.x, midPoint.y);
                            m.getValues(values);
                            float val2 = values[0];
                            if (val2 > val1 && values[0] > 10.0) break;

                            matrix.set(currentMatrix);
                            midPoint = mid(event);
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
                        m_zoomed = false;
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP://有手指离开屏幕,但屏幕还有触点（手指）
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN://当屏幕上已经有触点（手指），再有一个手指压下屏幕
                    mode = ZOOM;
                    startDis = distance(event);
                    if(startDis > 10f) {
                        m_zoomed = true;
                        //imageView.setScaleType(ImageView.ScaleType.MATRIX);
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

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
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
            return super.onSingleTapConfirmed(e);
        }
    }
}
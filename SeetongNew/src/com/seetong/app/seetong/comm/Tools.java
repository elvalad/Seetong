package com.seetong.app.seetong.comm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * Created by Administrator on 2014-07-03.
 */
public class Tools {
    private static final String TAG = "Tools";
    public static String getExternalStoragePath() {
        boolean exists = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (exists) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return "/";
        }
    }

    public static String concatString(String split, Object... intAry) {
        if (intAry == null) {
            return null;
        }

        int len = intAry.length;
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < len - 1; i++) {
            strBuf.append(intAry[i]);
            strBuf.append(split);
        }
        strBuf.append(intAry[len - 1]);
        return strBuf.toString();
    }

    public static FilenameFilter getFilenameFilter(final String extNames){
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                File file = new File(dir.getAbsolutePath() + "/" + filename);
                if (file.isDirectory()) {
                    return true;
                }

                String name = filename.toLowerCase();
                String exts = extNames;

                String[] temp = exts.split(Define.SPLIT_CHAR);
                for (String s : temp) {
                    if (name.endsWith(s)) {
                        return true;
                    }
                }
                return false;
            }
        };
        return filter;
    }

    public static FilenameFilter getFilenameFilter(final String extNames, final boolean includeDir){
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                File file = new File(dir.getAbsolutePath() + "/" + filename);
                if (file.isDirectory()) {
                    return includeDir;
                }

                String name = filename.toLowerCase();
                String exts = extNames;

                String[] temp = exts.split(Define.SPLIT_CHAR);
                for (String s : temp) {
                    if (name.endsWith(s)) {
                        return true;
                    }
                }
                return false;
            }
        };
        return filter;
    }

    public static String getStringByLength(long fileLength){
        float perLength = 1024.0f;
        String str = null;
        double _len = fileLength;
        if (_len < perLength) {
            str = String.format("%.0fBytes", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fKB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fMB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fGB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fTB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fPB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fEB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fZB", _len);
            return str;
        }

        _len /= perLength;
        if (_len < perLength) {
            str = String.format("%.2fYB", _len);
            return str;
        }
        return str;
    }

    public static Bitmap loadOrigPic(String filePath, float w, float h) {
        Bitmap bm = null;
        if (bm == null) {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, op);
            op.inJustDecodeBounds = false;
            if (op.outHeight < h && op.outWidth < w) {
                op.inSampleSize = 1;
            } else {
                float heightscale = op.outHeight / h;
                float widthscale = op.outWidth / w;
                float maxscale = Math.max(heightscale, widthscale);
                int scale = 1;
                for (; (maxscale / 2) > 1;) {
                    scale *= 2;
                    maxscale = maxscale / 2;
                }
                op.inSampleSize = scale;
            }
            try {
                bm = BitmapFactory.decodeFile(filePath, op);
                if (bm.getWidth() > bm.getHeight()
                        && (op.inSampleSize > 1 || bm.getWidth() > w || bm
                        .getHeight() > h)) {
                    float ht = h * bm.getHeight() / bm.getWidth();
                    if (ht - (int) ht > 0.5) {
                        ht = (int) ht + 1;
                    }
                    bm = Bitmap.createScaledBitmap(bm, (int) w, (int) ht, false);
                } else if (bm.getWidth() <= bm.getHeight()
                        && (op.inSampleSize > 1 || bm.getWidth() > w || bm
                        .getHeight() > h)) {
                    float wt = w * bm.getWidth() / bm.getHeight();
                    if (wt - (int) wt > 0.5) {
                        wt = (int) wt + 1;
                    }
                    bm = Bitmap.createScaledBitmap(bm, (int) wt, (int) h, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "原图读取完毕");
        return bm;
    }

    public static boolean deleteDirectory(String dir) {
        File file = new File(dir);
        if (!file.exists()) return false;
        if (file.isFile()) return file.delete();
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (File f : files) {
                deleteDirectory(f.getAbsolutePath());
            }

            return file.delete();
        }

        return false;
    }

    /**
     * 得到语言类型
     * @return 0 简体中文,1 繁体中文, 2 英文
     */
    public static int getLanguageTypes(){
        int flags = 0;
        String str = Locale.getDefault().getLanguage();
        if ("zh".equals(str)){
            str = Locale.getDefault().getCountry();
            if ("CN".equals(str)){
                flags = 0;
            }else if("TW".equals(str)){
                flags = 1;
            }
        }else {
            flags = 2;
        }
        return flags;
    }
}

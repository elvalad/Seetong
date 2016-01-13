package com.seetong5.app.seetong.ui.utils;

import android.content.Context;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.comm.Define;

import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2016/1/13.
 */
public class LogcatUtil {
    private static String SEETONG_LOGCAT_NOW = "seetong_logcat_0.log";
    private static String SEETONG_LOGCAT_PRE = "seetong_logcat_1.log";
    private String logcatFile = SEETONG_LOGCAT_NOW;
    private static int MAX_LINE_NUMBER = 50000;
    private static LogcatUtil INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPid;

    public static LogcatUtil getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatUtil(context);
        }
        return INSTANCE;
    }

    private LogcatUtil(Context context) {
        init(context);
        mPid = android.os.Process.myPid();
    }

    public void init(Context context) {
        PATH_LOGCAT = Define.RootDirPath + "/log";
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void start() {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPid), PATH_LOGCAT);
        }
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            File s = new File(PATH_LOGCAT, SEETONG_LOGCAT_NOW);
            File t = new File(PATH_LOGCAT, SEETONG_LOGCAT_PRE);
            if (logcatFile.equals(SEETONG_LOGCAT_NOW)) {
                fileChannelCopy(s, t);
            } else if (logcatFile.equals(SEETONG_LOGCAT_PRE)) {
                fileChannelCopy(t, s);
            }
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private void fileChannelCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fi != null) {
                    fi.close();
                }
                if (fo != null) {
                    fo.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class LogDumper extends Thread {
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPid;
        private String mDir;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPid = pid;
            mDir = dir;
            try {
                logcatFile = Global.m_spu.loadStringSharedPreference(Define.CFG_LOGCAT_FILE_NAME);
                if (logcatFile == null) {
                    logcatFile = SEETONG_LOGCAT_NOW;
                } else {
                    if (logcatFile.equals(SEETONG_LOGCAT_NOW)) {
                        logcatFile = SEETONG_LOGCAT_PRE;
                    } else if (logcatFile.equals(SEETONG_LOGCAT_PRE)) {
                        logcatFile = SEETONG_LOGCAT_NOW;
                    }
                }
                out = new FileOutputStream(new File(dir, logcatFile));
                Global.m_spu.saveSharedPreferences(Define.CFG_LOGCAT_FILE_NAME, logcatFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            cmds = "logcat  | grep \"(" + mPid + ")\"";
        }

        public void stopLogs() {
            mRunning = false;
        }

        private String getDateEN() {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = format1.format(new Date(System.currentTimeMillis()));
            return date;
        }

        @Override
        public void run() {
            super.run();
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                int lineNumber = 0;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }

                    if (out != null && line.contains(mPid)) {
                        out.write((getDateEN() + "  " + line + "\n").getBytes());
                        lineNumber++;
                    }

                    if (lineNumber >  MAX_LINE_NUMBER) {
                        if (out != null) {
                            out.close();
                        }
                        try {
                            out = new FileOutputStream(new File(mDir, "seetong_logcat" + ".log"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        lineNumber = 0;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }

                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (out != null) {
                    try {
                        out.close();
                        out = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
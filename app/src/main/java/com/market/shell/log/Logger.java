package com.market.shell.log;

import android.util.Log;

/**
 * Created by mac on 2018/1/19.
 */

public class Logger {

    public static void i(String msg) {
        i(null, msg);
    }

    public static void i(String title, String msg) {
        String str = formatString(title, msg);
        if (IConfig.getInstance().getIsShowLog())
            Log.i(IConfig.getInstance().getTag(), str);
        if (IConfig.getInstance().getIsWriteLog())
            FileUtils.writeLogFile(str);
    }

    public static void w(String msg) {
        w(null, msg);
    }

    public static void w(String title, String msg) {
        String str = formatString(title, msg);
        if (IConfig.getInstance().getIsShowLog())
            Log.w(IConfig.getInstance().getTag(), str);
        if (IConfig.getInstance().getIsWriteLog())
            FileUtils.writeLogFile(str);
    }

    public static void e(String msg) {
        e(null, msg);
    }

    public static void e(String title, String msg) {
        String str = formatString(title, msg);
        if (IConfig.getInstance().getIsShowLog())
            Log.e(IConfig.getInstance().getTag(), str);
        if (IConfig.getInstance().getIsWriteLog())
            FileUtils.writeLogFile(str);
    }

    public static String formatString(String title, String msg) {
        if (title == null) {
            return msg == null ? "" : msg;
        }
        return String.format("[%s]: %s", title, msg == null ? "" : msg);
    }
}

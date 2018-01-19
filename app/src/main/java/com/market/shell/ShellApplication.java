package com.market.shell;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.market.shell.log.IConfig;
import com.market.shell.log.TLogApplication;

/**
 * Created by mac on 2018/1/4.
 */

public class ShellApplication extends Application {

    private static final String TAG = "ShellApp";

    public static ShellHandler handler;
    public static MainActivity activity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ShellApplication onCreate");
        TLogApplication.initialize(this);
        IConfig.getInstance().isShowLog(true)//是否在logcat中打印log,默认不打印
                .isWriteLog(true)//是否在文件中记录，默认不记录
                .fileSize(100000);//日志文件的大小，默认0.1M,以bytes为单位
//                .tag("myTag");//logcat 日志过滤tag
        if (handler == null) {
            handler = new ShellHandler();
        }
    }

    public static void sendMessage(Message msg){
        handler.sendMessage(msg);
    }

    private static class ShellHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            activity.receiveMessage(msg);

//           switch (msg.what){
//               case 1:
//                    activity.receiveMessage(msg);
//                   break;
//               default:
//                   break;
//           }
        }
    }

}

package com.market.shell;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

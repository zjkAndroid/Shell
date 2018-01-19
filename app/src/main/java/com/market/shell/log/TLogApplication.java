package com.market.shell.log;

import android.app.Application;
import android.content.Context;

/**
 * Created by mac on 2018/1/19.
 */

public class TLogApplication extends Application {

    private static Context mContenx;

    public static Context getAPP(){
        return mContenx;
    }

    public static void initialize(Context context){
        mContenx = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContenx = this;
    }
}

package com.market.shell;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShellApp";

    //设备策略管理器
    private  DevicePolicyManager dpm = null;
    private static final int DEVICE_ADMIN = 10001;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShellApplication.activity = this;

        initPushService();
        hideUIMenu();
        configWebView();

    }

    private void initPushService(){
        // 为第三方自定义推送服务
        PushManager.getInstance().initialize(this.getApplicationContext(), ShellPushService.class);

        // 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), ShellIntentService.class);
    }

    public void receiveMessage(Message msg){
        Log.d(TAG,"receiveMessage:" + msg.what);
        switch (msg.what){
            case Constant.LOCK_SCREEN:
                lockScreen();
                break;
            case Constant.UNLOCK_SCREEN:
                wakeUpAndUnlock(this);
                break;
            default:
                break;
        }
    }



    /**
     * 锁屏
     */
    private void lockScreen(){
        ComponentName who = new ComponentName(this, ShellAdminReceiver.class);
        dpm = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);

        if(dpm.isAdminActive((who))){ // 已激活管理员权限
            dpm.lockNow();
            return;
        }

        // 未激活管理员权限
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, who);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"开启可以一键锁屏，防止勿碰");
        //所以要等打开的Activity关闭后的回调函数里去判断是否真正激活
        startActivityForResult(intent,DEVICE_ADMIN);
        Toast.makeText(MainActivity.this,"管理员权限已开启", Toast.LENGTH_LONG).show();

    }

    /**
     * 关闭激活Activity后的回调函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DEVICE_ADMIN){
            dpm.lockNow();
        }
    }


    private void wakeUpAndUnlock(Context context){
        // 屏锁管理器
        KeyguardManager km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        // 解锁
        kl.disableKeyguard();

        //获取电源管理器对象
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP| PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        wl.release();
    }




    /**
     * 隐藏菜单栏
     */
    private void hideUIMenu(){
        int flags;
        int curApiVersion = Build.VERSION.SDK_INT;

        // 隐藏标题栏
        // manifest 配置 activity    android:theme="@style/Theme.AppCompat.DayNight.NoActionBar"
        // 隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){

        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                return true;
            case  KeyEvent.KEYCODE_MENU:
            case  KeyEvent.KEYCODE_HOME:
                return false;
        }
        return  super.onKeyDown(keyCode,keyEvent);
    }




    /**
     * WebViewConfig
     */
    private void configWebView(){
        WebView mWebView = (WebView)findViewById(R.id.mWebView);
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
//        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        mWebView.addJavascriptInterface(new ShellJSHandler(handler), "bridge");
        mWebView.setWebChromeClient(new WebChromeClient());
        String url = "file:///android_asset/index.html";
        mWebView.loadUrl(url);
    }
}

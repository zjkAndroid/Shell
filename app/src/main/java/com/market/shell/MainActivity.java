package com.market.shell;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.igexin.sdk.PushManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShellApp";

    //设备策略管理器
    private  DevicePolicyManager dpm = null;
    private static final int DEVICE_ADMIN = 10001;
    private static final int IMG_REQUEST_CODE = 10001;

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
                wakeUpAndUnlock();
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
        if(requestCode == IMG_REQUEST_CODE){
            onImgSelected(resultCode, data);
        }
    }

    /**
     * 唤醒手机屏幕并解锁
     */
    public void wakeUpAndUnlock() {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000); // 点亮屏幕
            wl.release(); // 释放
        }
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager)this.getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
    }

//    private void wakeUpAndUnlock(Context context){
//        // 屏锁管理器
//        KeyguardManager km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
//        // 解锁
//        kl.disableKeyguard();
//
//        //获取电源管理器对象
//        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
//        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP| PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
//        //点亮屏幕
//        wl.acquire();
//        wl.release();
//    }




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



    WebView mWebView;
    /**
     * WebViewConfig
     */
    private void configWebView(){
        mWebView = (WebView)findViewById(R.id.mWebView);
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
//        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                //title 就是网页的title
                super.onReceivedTitle(view, title);
                Log.d(TAG, "title=" + title);
                if (!title.endsWith(".html")) {
                    MainActivity.this.setTitle(title);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient(){
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){                 // Handle the error
                Log.d(TAG, description);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });

        String url = "file:///android_asset/index.html";
        mWebView.loadUrl(url);
    }



    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    // 读取系统图库
    public void openAblum(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMG_REQUEST_CODE);
    }

    private void onImgSelected(int resultCode, Intent data){
        if(resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                Log.d(TAG, "image uri is null");
                Toast.makeText(this, "获取图片Uri失败", Toast.LENGTH_LONG).show();
                return;
            }
            final String path = getRealPathFromUri(this, uri);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:bridge.afterAblum('"+ path +"')");
                }
            });
        }
    }


    /**
     * 根据图片的Uri获取图片的绝对路径。@uri 图片的uri
     * @return 如果Uri对应的图片存在,那么返回该图片的绝对路径,否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        if(context == null || uri == null) {
            return null;
        }
        if("file".equalsIgnoreCase(uri.getScheme())) {
            return getRealPathFromUri_Byfile(context,uri);
        }
        if("content".equalsIgnoreCase(uri.getScheme())) {
            return getRealPathFromUri_Api11To18(context,uri);
        }
        return getRealPathFromUri_AboveApi19(context, uri);//没用到
    }

    //针对图片URI格式为Uri:: file:///storage/emulated/0/DCIM/Camera/IMG_20170613_132837.jpg
    private static String getRealPathFromUri_Byfile(Context context,Uri uri){
        String uri2Str = uri.toString();
        String filePath = uri2Str.substring(uri2Str.indexOf(":") + 3);
        return filePath;
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUri_AboveApi19(Context context, Uri uri) {
        String filePath = null;
        String wholeID = null;

        wholeID = DocumentsContract.getDocumentId(uri);

        // 使用':'分割
        String id = wholeID.split(":")[1];

        String[] projection = { MediaStore.Images.Media.DATA };
        String selection = MediaStore.Images.Media._ID + "=?";
        String[] selectionArgs = { id };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, null);
        int columnIndex = cursor.getColumnIndex(projection[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    /**
     * //适配api11-api18,根据uri获取图片的绝对路径。
     * 针对图片URI格式为Uri:: content://media/external/images/media/1028
     */
    private static String getRealPathFromUri_Api11To18(Context context, Uri uri) {
        String filePath = null;
        String[] projection = { MediaStore.Images.Media.DATA };

        CursorLoader loader = new CursorLoader(context, uri, projection, null,
                null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
        String filePath = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

}

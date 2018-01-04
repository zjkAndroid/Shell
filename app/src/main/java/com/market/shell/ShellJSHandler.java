package com.market.shell;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mac on 2018/1/4.
 */

public class ShellJSHandler {

    private Handler handler = null;
    private WebView webView = null;

    public ShellJSHandler(Handler handler){
        this.webView = (WebView)ShellApplication.activity.findViewById(R.id.mWebView);
        this.handler = handler;
    }

    @JavascriptInterface
    public void init(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:setContactInfo('"+ getJsonStr()+"')");
            }
        });
    }

    @Nullable
    private String getJsonStr(){
        try{
            JSONObject obj1 = new JSONObject();
            obj1.put("id", 1);
            obj1.put("name", "张三");
            obj1.put("phone", "123456");

            JSONObject obj2 = new JSONObject();
            obj2.put("id", 2);
            obj2.put("name", "李四");
            obj2.put("phone", "123123123");

            JSONArray array = new JSONArray();
            array.put(obj1);
            array.put(obj2);

            return array.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }

    @JavascriptInterface
    public void lockScreen(){
        Message message = new Message();
        message.what = Constant.LOCK_SCREEN;
        message.obj = "LockScreen";
        ShellApplication.sendMessage(message);
    }

}

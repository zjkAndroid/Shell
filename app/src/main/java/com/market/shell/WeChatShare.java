package com.market.shell;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mac on 2018/12/6.
 */

public class WeChatShare {
    private static boolean isInsatllWeChat(Context context){
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.tencent.mm", 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }

        return  packageInfo != null;
    }

    // 分享图片到微信，分享的是本地图片
    public  static void shareToTimeLine(Context context, Uri uri){
        if(!isInsatllWeChat(context)){
            Toast.makeText(context.getApplicationContext(),"没有安装微信", Toast.LENGTH_LONG).show();
            return;
        }

        try{
            ArrayList<Uri> imageList = new ArrayList<>();
            imageList.add(uri);



//            File[] files = new File(path).listFiles();
//            if(files != null){
//                for (File file:files){
//                    Log.d("SHAREIMAGE", file.getAbsolutePath());
//                    File f = new File(file.getAbsolutePath());
//                    if(f.exists()){
//                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){// 24以下  android 7.0以下
//                           imageList.add(Uri.fromFile(f));
//                        } else { // android 7.0 以上
//                            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), f.getAbsolutePath(), f.getName(),null));
//                            imageList.add(uri);
//                        }
//                    }
//                }
//            }

            ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", "ClipData text");
            clipboardManager.setPrimaryClip(clipData);


            Intent weChatIntent = new Intent();
            //com.tencent.mm.ui.tools.ShareImgUI 是分享到微信好友
            // com.tencent.mm.ui.tools.ShareToTimeLineUI 是分享到微信朋友圈，最多可以分享九张图到微信朋友圈
            weChatIntent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI"));

            weChatIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
//            weChatIntent.putExtra(Intent.EXTRA_TEXT, "sssssssss");
//            weChatIntent.putExtra("Kdescription", "wwwwwwwwwwww");
//            weChatIntent.setAction(Intent.ACTION_SEND);
//            weChatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 开启一个新的进程做分享
            weChatIntent.putExtra(Intent.EXTRA_STREAM, imageList);
//            weChatIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageList);
            weChatIntent.setType("image/*");
            ((MainActivity)context).startActivityForResult(weChatIntent, 10002);
        } catch (Exception e){
            throw e;
//            Toast.makeText(context.getApplicationContext(),"分享失败", Toast.LENGTH_LONG).show();
        }
    }

    // 分享图片到微信，分享的是本地图片
    public  static void shareToTimeLine(Context context, String jsonData){
        if(!isInsatllWeChat(context)){
            Toast.makeText(context.getApplicationContext(),"没有安装微信", Toast.LENGTH_LONG).show();
            return;
        }

        try{
            JSONObject obj = new JSONObject(jsonData);
            Uri uri;
            File file = new File(obj.getString("imgPath")).getAbsoluteFile();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){// 24以下  android 7.0以下
                uri = Uri.fromFile(file);
            } else { // android 7.0 以上
                uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(),null));
            }
            ArrayList<Uri> imageList = new ArrayList<>();
            imageList.add(uri);

            ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", obj.getString("desc"));
            clipboardManager.setPrimaryClip(clipData);


            Intent weChatIntent = new Intent();
            //com.tencent.mm.ui.tools.ShareImgUI 是分享到微信好友
            // com.tencent.mm.ui.tools.ShareToTimeLineUI 是分享到微信朋友圈，最多可以分享九张图到微信朋友圈
            weChatIntent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI"));

            weChatIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            weChatIntent.putExtra(Intent.EXTRA_STREAM, imageList);
            weChatIntent.setType("image/*");
            context.startActivity(weChatIntent);
        } catch (Exception e){
            Toast.makeText(context.getApplicationContext(),"分享失败", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}

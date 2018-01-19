package com.market.shell;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.market.shell.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;



public class ShellIntentService extends GTIntentService {

    private static final String TAG = "ShellApp";

    @Override
    public void onReceiveServicePid(Context context, int pid) {
        Log.d(TAG, "onReceiveServicePid -> " + pid);
        Logger.i("onReceiveServicePid -> " + pid);
    }

    @Override
    public void onReceiveClientId(Context context, String clientId) {
        Log.e(TAG, "onReceiveClientId -> clientid = " + clientId);
        Logger.i(TAG, "onReceiveClientId -> clientid = " + clientId);

    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        Log.e(TAG, "onReceiveMessageData -> gtTransmitMessage = " + msg.toString());
        Logger.i(TAG, "onReceiveMessageData -> gtTransmitMessage = " + msg.toString());

        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();

        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
        Log.d(TAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));
        Logger.i(TAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));

        Log.d(TAG, "onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
                + "\ncid = " + cid);
        Logger.i(TAG, "onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
                + "\ncid = " + cid);

        if (payload == null) {
            Log.e(TAG, "receiver payload = null");
            Logger.i(TAG, "receiver payload = null");

        } else {
            String data = new String(payload);
            Log.d(TAG, "receiver payload = " + data);
            Logger.i(TAG, "receiver payload = " + data);

            try {
                JSONObject obj = new JSONObject(data);
                Message message = new Message();
                message.what = obj.getInt("value");
                message.obj = obj;
                ShellApplication.sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        Log.d(TAG, "----------------------------------------------------------------------------------------------");
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        Log.d(TAG, "onReceiveOnlineState -> " + (online ? "online" : "offline"));
        Logger.i(TAG, "onReceiveOnlineState -> " + (online ? "online" : "offline"));
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        Log.d(TAG, "onReceiveCommandResult -> " + gtCmdMessage);
        Logger.i(TAG, "onReceiveCommandResult -> " + gtCmdMessage);
    }
}

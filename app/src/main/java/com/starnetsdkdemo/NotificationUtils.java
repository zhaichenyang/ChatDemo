package com.starnetsdkdemo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by zhaichenyang on 2018/9/13.
 */

public class NotificationUtils {

    public static void showMessage(Context context, String title,String desc){
        Intent broadcastIntent = new Intent(context, CSNotificationReceiver.class);
        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Android8.0适配
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId="chat";
            String channelName="聊天信息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel=new NotificationChannel(channelId,channelName,importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(desc)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())         //发送时间
                .setDefaults(Notification.DEFAULT_ALL)      //设置默认的提示音，振动方式，灯光
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);                        //设置点击自动消失，测试期间保持不消失
        notificationManager.notify(111, builder.build());
    }

    //客服服务通知
    public static class CSNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //打开到客服界面
            Log.e("CSNotificationReceiver","y");
            Intent detailIntent = new Intent(context, MainActivity.class);
            context.startActivity(detailIntent);
        }
    }
}

package com.jayhsugo.orderlunchbox;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Administrator on 2017/10/13.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {


        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String text = intent.getStringExtra("totalAmount");
        Log.d("MyLog", "AlarmReceiver, intent.getStringExtra:"+text);

        PendingIntent piRemind = PendingIntent.getBroadcast(context, 0, intent, 0);
        PendingIntent piOpenActivity = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //設置通知內容並在onReceive()這個函數執行時開啟
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);


        Notification notification = new NotificationCompat.Builder(context)

                //狀態列的文字 (Android 5.0以後已不再顯示)
//                .setTicker(getString(R.string.ticker))
                //訊息面板的標題
                .setContentTitle("繳費了嗎?")
                //等使用者點了之後才會開啟指定的Activity
                .setContentIntent(pi)
                //訊息面板的內容文字
                .setContentText("費用是 "+ intent.getStringExtra("totalAmount") + " 元")
                //訊息的圖示
                .setSmallIcon(R.drawable.ic_small_notification)
                //點擊後會自動移除狀態列上的通知訊息
                .setAutoCancel(true)
                //加入聲音
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //加入狀態列下拉後的進一步操作
//                .addAction(0, "稍後提醒", pi)
//                .addAction(0, "已繳", pi)
                .build();


        //呼叫notify()送出通知訊息
        notificationManager.notify(1, notification);

        //再次開啟LongRunningService這個服務
//        Intent i = new Intent(context, LongRunningService.class);
//        context.startService(i);
    }
}

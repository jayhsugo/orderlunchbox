package com.jayhsugo.orderlunchbox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Administrator on 2017/10/13.
 */

public class LongRunningService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //int time = 1*1000; // 毫秒
        //SystemClock.elapsedRealtime() 表示的是從開機到現在的毫秒數
        //long triggerAtTime = SystemClock.elapsedRealtime() + time;

        //此處設置開啟AlarmReceiver這個Service
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("totalAmount", intent.getStringExtra("totalAmount"));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String text = intent.getStringExtra("totalAmount");
        Log.d("MyLog", "LongRunningService, intent.getStringExtra:" + text);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        //ELAPSED_REALTIME_WAKEUP表示讓定時任務的出發時間從系統開機算起，並且換喚醒CPU。
        //alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        //參數1,我們選擇一個會在指定時間喚醒裝置的警報類型
            //AlarmManager.ELAPSED_REALTIME表示鬧鐘在手機睡眠狀態下不可用，該狀態下鬧鐘使用相對時間（相對於系統啟動開始），狀態值為3；
            //AlarmManager.ELAPSED_REALTIME_WAKEUP表示鬧鐘在睡眠狀態下會喚醒系統並執行提示功能，該狀態下鬧鐘也使用相對時間，狀態值為2；
            //AlarmManager.RTC表示鬧鐘在睡眠狀態下不可用，該狀態下鬧鐘使用絕對時間，即當前系統時間，狀態值為1；
            //AlarmManager.RTC_WAKEUP表示鬧鐘在睡眠狀態下會喚醒系統並執行提示功能，該狀態下鬧鐘使用絕對時間，狀態值為0；
            //AlarmManager.POWER_OFF_WAKEUP表示鬧鐘在手機關機狀態下也能正常進行提示功能，所以是5個狀態中用的最多的狀態之一，該狀態下鬧鐘也是用絕對時間，狀態值為4；不過本狀態好像受SDK版本影響，某些版本並不支持；
        //參數2,將指定的時間以millisecond傳入
        //參數3,傳入待處理意圖
        //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

        //重覆性警報(Repeating Alarm)
        //setRepeating() 時間精確型警報:精確到毫秒(較耗電)
//        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15 * 1000, 15 * 1000, pi);

        //setInexactRepeating() 時間非精確型警報:參數如下列
            //INTERVAL_FIFTEEN_MINUTES
            //INTERVAL_HALF_HOUR
            //INTERVAL_HOUR
            //INTERVAL_HALF_DAY
            //INTERVAL_DAY

        long firstime= SystemClock.elapsedRealtime();
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //在Service结束後關閉AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pi);

    }
}

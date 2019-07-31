package com.rizsi.servermonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MonitorService extends Service {
    public MonitorService() {
    }

    public static void startPeriodic(Context context)
    {
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;
        PendingIntent pi=MonitorService.createIntent(context);

        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, interval, interval, pi);
        Toast.makeText(context, "Set up timer", Toast.LENGTH_LONG).show();
    }
    public static PendingIntent createIntent(Context context) {
        Intent i = new Intent(context, MonitorService.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        return pi;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e("SRV", "P2!");

        Toast.makeText(getApplicationContext(), "Start periodic command2", Toast.LENGTH_LONG).show();
//        alarm.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Log.e("SRV", "P1!");
        Toast.makeText(getApplicationContext(), "Start periodic command1", Toast.LENGTH_LONG).show();
//        alarm.setAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("SRV", "P3!");
        Toast.makeText(getApplicationContext(), "Start periodic command3", Toast.LENGTH_LONG).show();
        super.onCreate();
    }
}

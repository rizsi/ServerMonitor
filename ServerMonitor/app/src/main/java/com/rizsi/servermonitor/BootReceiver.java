package com.rizsi.servermonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Auto-called by the system when the device was booted.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent)
    {

        // Your code to execute when Boot Completd
        Log.e("SRV", "BOOTED!");
        Toast.makeText(context, "Booting Completed", Toast.LENGTH_LONG).show();
//        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 //       int interval = 10000;
  //      PendingIntent pi=MonitorService.createIntent(context);

   //     manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, interval, interval, pi);
//        manager.cancel(pi);

        //MonitorService2.reschedule(context);
        // Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();


        createNotificationChannel(context);

        AlarmReceiver.setAlarm(context);
    }
    public static final String CHANNEL_ID="ServerMonitor";
    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ServerMonitor";
            String description = "Monitors status of your servers";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendNotification(Context context, String text)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("ServerMonitor notification")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
        Log.d("NOTIFICATION", "Sent!");
    }
    public static void deleteNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.cancel(0);
        Log.d("NOTIFICATION", "Cancelled!");
    }

}

package com.android.queue.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.queue.R;

public class NotificationDevice extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        notificationChanel();
    }
    private void notificationChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(  UltisNotification.CHANEL_ID
                    ,UltisNotification.CHANEL_NAME
                    , NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(UltisNotification.CHANEL_DESC);

            NotificationManager managerCompat = getSystemService(NotificationManager.class);
            managerCompat.createNotificationChannel(channel);
        }
    }

    public static void headsUpNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,UltisNotification.CHANEL_ID)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentTitle(UltisNotification.NOTI_TITLE)
                                                .setContentText(UltisNotification.NOTI_DESC)
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(UltisNotification.NOTI_ID,builder.build());
    }
}


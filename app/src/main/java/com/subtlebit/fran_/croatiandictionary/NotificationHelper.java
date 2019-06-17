package com.subtlebit.fran_.croatiandictionary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.os.Build;

public class NotificationHelper {

    Context context;

    public NotificationHelper(Context context_){
        context = context_;
    }

    void sendNotification(String title, String content, int id) {
        if (Build.VERSION.SDK_INT < 16) return;

        Notification noty;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

          /*========================================================================================*\
        CREATE INTENT FOR WHEN NOTIFICATION IS CLICKED
        \*========================================================================================*/
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("wordID",id);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        /*====================================================================================*\
        BUILD NOTIFICATION FOR ANDROID OREO
        \*====================================================================================*/

            //CREATE CHANNEL ///////////////////////////////////////////////////////////////////////
            String channelID = "GrandDictionaries";
            NotificationChannel channel = new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Grand Dictionaries Notifications");

            //LIGHTS, VIBRATION, BADGE AND SOUND ///////////////////////////////////////////////////
            channel.enableLights(false);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.setShowBadge(false);

            //CREATE CHANNEL ///////////////////////////////////////////////////////////////////////
            notificationManager.createNotificationChannel(channel);

            //NOTIFICATION BUILDER /////////////////////////////////////////////////////////////////
            Notification.Builder NB = new Notification.Builder(context, channelID);
            NB.setSmallIcon(R.drawable.flag);
            NB.setChannelId(channelID);
            NB.setContentTitle(title);
            NB.setContentText(content);
            NB.setAutoCancel(true);
            NB.setColor(context.getResources().getColor(R.color.colorPrimary,context.getTheme()));
            NB.setContentIntent(pendingIntent);
            NB.setStyle(new Notification.BigTextStyle().bigText(content));

            noty = NB.build();


        } else {
        /*====================================================================================*\
        BUILD NOTIFICATION FOR PREVIOUS ANDROID VERSIONS
        \*====================================================================================*/

            //NOTIFICATION BUILDER /////////////////////////////////////////////////////////////////
            Notification.Builder NB = new Notification.Builder(context);
            NB.setSmallIcon(R.drawable.flag);
            NB.setContentTitle(title);
            NB.setContentText(content);
            NB.setAutoCancel(true);
            NB.setContentIntent(pendingIntent);
            NB.setStyle(new Notification.BigTextStyle().bigText(content));

            if (Build.VERSION.SDK_INT >= 21)
                NB.setColor(context.getResources().getColor(R.color.colorPrimary));

            noty = NB.build();
        }


        /*====================================================================================*\
        SEND NOTIFICATION
        \*====================================================================================*/
        notificationManager.notify(id, noty);
    }
}

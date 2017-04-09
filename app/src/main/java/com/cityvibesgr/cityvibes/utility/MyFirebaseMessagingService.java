package com.cityvibesgr.cityvibes.utility;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.cityvibesgr.cityvibes.R;
import com.cityvibesgr.cityvibes.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by alexsideris on 02/04/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Toast.makeText(this, "Message Received from: "+remoteMessage.getFrom(), Toast.LENGTH_SHORT).show();
        if(remoteMessage.getData().size()>0){
            //Toast.makeText(this, "Data: "+remoteMessage.getData(), Toast.LENGTH_SHORT).show();
        }
        if(remoteMessage.getNotification()!=null){
            //Toast.makeText(this, "Message Body: "+remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
            sendNotification(remoteMessage.getNotification().getBody());
        }else{
            sendNotification("New uploads in your area! Feel the vibes now!");
        }

    }

    private void sendNotification(String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_green);
        notificationBuilder.setContentTitle("City Vibes");
        notificationBuilder.setContentText(body);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(notificationSound);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());
    }
}

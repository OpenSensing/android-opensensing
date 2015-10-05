package org.opensensing.opensensingdemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/**
 * Created by arks on 28/09/15.
 */
public class AuthErrorBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MainActivity.TAG, "**** authError received! ****");

        if (AuthenticationActivity.isActive) {
            //do nothing
        }
        else if (MainActivity.isActive) {
            launchAuthenticationActivity(context);
        }
        else {
            showNotification(context);
        }
    }


    private void launchAuthenticationActivity(Context context) {
        Intent intent = new Intent(context, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showNotification(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("openPDS authentication error")
                        .setContentText("Something went wrong uploading data to openPDS");

        Intent resultIntent = new Intent(context, AuthenticationActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(AuthenticationActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(context.getResources().getInteger(R.integer.notification_id), mBuilder.build());
    }

}

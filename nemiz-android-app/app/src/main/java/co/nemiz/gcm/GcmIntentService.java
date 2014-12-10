package co.nemiz.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import co.nemiz.R;
import co.nemiz.domain.User;
import co.nemiz.services.AudioManager;
import co.nemiz.ui.MainActivity;

public class GcmIntentService extends IntentService {
    public GcmIntentService() {
        super("GcmIntentService");
    }

    private int numberOfMessages = 0;

    private NotificationManager notificationManager;

    private void sendNotification(String msg) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(this, 0, intent,0);

        Gson gson = new Gson();
        User user = gson.fromJson(msg, User.class);

        AudioManager audioManager = AudioManager.get(this);
        Uri sound = audioManager.getRandomAudio(); // must be accessible by public

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getString(R.string.txt_notification_title))
                    .setNumber(++numberOfMessages)
                    .setAutoCancel(true)
                    .setSound(sound)
                    .setContentText(getString(R.string.txt_notification_text, user.getName()))
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationManager.notify(user.getId().intValue(), notification);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            sendNotification(extras.getString("default"));
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}

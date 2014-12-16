package co.nemiz.gcm;

import android.app.IntentService;
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

    private Gson gson = new Gson();
    private int numberOfMessages = 0;

    private void sendNotification(String msg) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent =
            PendingIntent.getActivity(this, 0, intent,0);

        User user = gson.fromJson(msg, User.class);

        AudioManager audioManager = AudioManager.get();
        Uri sound = audioManager.getRandomAudio(); // must be publicly accessible

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
                    .setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getString(R.string.txt_notification_title))
                    .setNumber(++numberOfMessages)
                    .setAutoCancel(true)
                    .setSound(sound, android.media.AudioManager.STREAM_MUSIC)
                    .setContentText(getString(R.string.txt_notification_text, user.getName()))
                .setContentIntent(pendingIntent);

        notificationManager.notify(user.getId().intValue(), builder.build());
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

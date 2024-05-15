package or.nevet.cloudMess;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import androidx.core.app.NotificationCompat;

import static or.nevet.cloudMess.MyFirebaseMessagingService.notifications;

public class NotificatiOr extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public NotificatiOr() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String mess = intent.getStringExtra("mess");
        String name = intent.getStringExtra("name");
        long time = intent.getLongExtra("time", 0);
        Random rnd = new Random();
        ArrayList<Integer> arr = new ArrayList<>(notifications.values().size());
        arr.addAll(notifications.values());
        Collections.sort(arr);
        Integer[] array = new Integer[arr.size()];
        int id = getRandomWithExclusion(rnd, 0, Integer.MAX_VALUE - 1, arr.toArray(array));
        notifications.put(time, id);
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("started_from", "notification");
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cool Message\uD83D\uDE0E:")
                .setContentText(name + ":\n" + mess)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.mess_icon)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(getDeleteIntent(time))
                .setAutoCancel(true)
                .setOngoing(false)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        startForeground(id, notification);
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopForeground(false);
            }
        }).start();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    protected PendingIntent getDeleteIntent(long time)
    {
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("notification_cancelled");
        intent.putExtra("time", time);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public int getRandomWithExclusion(Random rnd, int start, int end, Integer ... exclude) {
        int random = start + rnd.nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

}
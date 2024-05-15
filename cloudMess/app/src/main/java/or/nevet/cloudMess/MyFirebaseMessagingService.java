package or.nevet.cloudMess;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static HashMap<Long, Integer> notifications = new HashMap<>();

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (!isOnForeground()) {
            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("delete")) {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(notifications.get(Long.parseLong(data.get("time"))));
                notifications.remove(Long.parseLong(data.get("time")));
            } else if (!MainActivity.name.equals(data.get("name"))) {
                String mess = data.get("mess");
                String name = data.get("name");
                long time = Long.parseLong(data.get("time"));
                //start
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
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(id, notification);
                //end

                /*Intent service = new Intent(this, NotificatiOr.class);
                service.putExtra("name", name);
                service.putExtra("mess", mess);
                service.putExtra("time", time);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(service);
                }
                else
                    startService(service);*/
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificatiOr.CHANNEL_ID,
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

    private boolean isOnForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

}

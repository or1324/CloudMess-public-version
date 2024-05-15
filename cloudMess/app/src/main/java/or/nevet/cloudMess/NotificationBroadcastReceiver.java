package or.nevet.cloudMess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action.equals("notification_cancelled"))
        {
            MyFirebaseMessagingService.notifications.remove(intent.getExtras().getLong("time"));
        }
    }

}

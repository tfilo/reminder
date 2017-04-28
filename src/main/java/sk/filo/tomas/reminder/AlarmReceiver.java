package sk.filo.tomas.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmExtendedItem;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomas on 27.10.2016.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "AlarmReceiver");
        wakeLock.acquire();
        try {
            DatabaseHelper dbH = new DatabaseHelper(context);

            Long id = intent.getLongExtra("alarmId", -1L);
            if (!id.equals(-1L)) {
                Log.d(TAG, "Receiving id " + id);

                AlarmExtendedItem aei = dbH.getExtendedAlarmInfo(id);

                if (aei != null) {
                    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                    switch (aei.type) {
                        case CONTACT:
                            builder.setSmallIcon(R.drawable.ic_redeem_white_24dp).setTicker(context.getString(R.string.birthday)).setWhen(System.currentTimeMillis())
                                    .setContentTitle(context.getString(R.string.birthday)).setContentText(aei.name + " " + context.getString(R.string.has_birthday)).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH);
                            break;
                        case REMINDER:
                            builder.setSmallIcon(R.drawable.ic_alarm_white_24dp).setTicker(context.getString(R.string.reminder)).setWhen(System.currentTimeMillis())
                                    .setContentTitle(aei.name).setContentText(aei.description).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH);
                            dbH.enableDisableAlarm(id, false);
                            break;
                    }

                    if (Build.VERSION.SDK_INT >= 16) {
                        mNotificationManager.notify(aei.id.intValue(), builder.build());
                    } else {
                        mNotificationManager.notify(aei.id.intValue(), builder.getNotification());
                    }


                }
            }
        } finally {
            wakeLock.release();
        }
    }
}

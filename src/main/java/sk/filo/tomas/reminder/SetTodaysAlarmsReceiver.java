package sk.filo.tomas.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmItem;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomas on 27.10.2016.
 */

@Deprecated
public class SetTodaysAlarmsReceiver extends BroadcastReceiver {

    private final String TAG = "SetTodaysAlarmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "SetTodaysAlarmsReceiver");
            wakeLock.acquire();
            try {
                Log.d(TAG, "Alarm will be updated");
                DatabaseHelper dbH = new DatabaseHelper(context);
                List<AlarmItem> todaysAlarms = dbH.getTodaysEnabledAlarms();
                AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                if (!todaysAlarms.isEmpty()) {
                    for (AlarmItem ai : todaysAlarms) { // Set alarm for all valid todays alarms
                        Intent i = new Intent(context, AlarmReceiver.class);
                        i.putExtra("alarmId", ai.id);
                        i.setAction(ai.id.toString());
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ai.id.intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);

                        if (ai.alarmTime.before(new Date())) { // if alarm passed already when device was turned off, just show it one minute after boot
                            ai.alarmTime.setTime(new Date().getTime() + 60000L);
                        }
                        Log.d(TAG, "Alarm set to " + ai.alarmTime.getTime());
                        am.set(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                    }
                }

                Intent i = new Intent(context, SetTodaysAlarmsReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar cal = Calendar.getInstance();

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);

                am.set(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent); // update alarms next day on midnight
            } finally {
                wakeLock.release();
            }
        }
    }
}

package sk.filo.tomas.reminder.receiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sk.filo.tomas.reminder.util.ContactsUtil;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmItem;
import sk.filo.tomas.reminder.util.DateTimeUtil;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomas on 27.10.2016.
 */

public class SetTodaysAlarmsReceiver extends BroadcastReceiver {

    private final String TAG = "SetTodaysAlarmsReceiver";

    private final ContactsUtil contactsUtil = new ContactsUtil();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
            if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
                contactsUtil.updateContactDatabase(context);
            }

            DatabaseHelper dbH = new DatabaseHelper(context);
            List<AlarmItem> alarmsToSetup = dbH.getAlarmsToSetup();
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            if (!alarmsToSetup.isEmpty()) {
                Date now = new Date();
                now.setTime(now.getTime() + 60000L); // set time one minute in future
                for (AlarmItem ai : alarmsToSetup) { // Set alarm for all valid alarms
                    Intent i = new Intent(context, AlarmReceiver.class);
                    i.putExtra("alarmId", ai.id);
                    i.setAction(ai.id.toString());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ai.id.intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (ai.alarmTime.before(now)) { // if alarm passed already when device was turned off, just show it one minute after boot/execution of this receiver
                        ai.alarmTime.setTime(now.getTime());
                    }
                    Log.d(TAG, "Alarm set to " + ai.alarmTime.getTime());

                    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                        am.set(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                    } else {
                        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                            am.setExact(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                        } else {
                            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                        }
                    }
                }
            }

            Intent i = new Intent(context, SetTodaysAlarmsReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar cal = DateTimeUtil.getLastMidnight();
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);

            // update alarms next day on midnight
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent);
            } else {
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                    am.setExact(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent);
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent);
                }
            }
        } finally {
            wakeLock.release();
        }
    }
}

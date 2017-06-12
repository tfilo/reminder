package sk.filo.tomas.reminder.receiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sk.filo.tomas.reminder.helper.ContactsHelper;
import sk.filo.tomas.reminder.MainActivity;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmItem;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomas on 27.10.2016.
 */

public class SetTodaysAlarmsReceiver extends BroadcastReceiver {

    private final String TAG = "SetTodaysAlarmsReceiver";

    private final ContactsHelper contactsHelper = new ContactsHelper();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "SetTodaysAlarmsReceiver");
        wakeLock.acquire();
        try {

            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
            if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
                contactsHelper.updateContactDatabase(context);
            }

            // When new year, need to update all contact birthday alerts to this year
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.YEAR) > sharedPreferences.getInt(MainActivity.LAST_YEAR, 2000)) {
                Log.d(TAG, "Contact birthday update for new year");
                DatabaseHelper dbH = new DatabaseHelper(context);
                dbH.updateContactAlarmDatesAndTimes();
                sharedPreferences.edit().putInt(MainActivity.LAST_YEAR, cal.get(Calendar.YEAR)).commit();
            }

            Log.d(TAG, "Alarm will be updated");
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

                    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT){
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

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);

            // update alarms next day on midnight
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT){
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

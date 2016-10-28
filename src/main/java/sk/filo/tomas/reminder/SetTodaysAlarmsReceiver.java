package sk.filo.tomas.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmItem;

/**
 * Created by tomas on 27.10.2016.
 */

public class SetTodaysAlarmsReceiver extends BroadcastReceiver {

    private final String TAG = "SetTodaysAlarmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm will be updated");
        DatabaseHelper dbH = new DatabaseHelper(context);
        List<AlarmItem> todaysAlarms = dbH.getNextAlarmsThisDay(); // ordered by dateTime
        if (!todaysAlarms.isEmpty()) {
            AlarmManager am = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
            Intent i = new Intent(context, AlarmReceiver.class);
            StringBuilder sb = new StringBuilder();
            for (AlarmItem ai : todaysAlarms) {
                sb.append(ai.id.toString());
                sb.append(",");
            }
            if (sb.length()>0) {
                sb.deleteCharAt(sb.length()-1);
            }

            i.putExtra("alarmIds", sb.toString());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, todaysAlarms.get(0).alarmTime.getTime(), pendingIntent);
        }
    }
}

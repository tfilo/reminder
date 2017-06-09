package sk.filo.tomas.reminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmItem;
import sk.filo.tomas.reminder.item.ContactItem;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomas on 27.10.2016.
 */

public class SetTodaysAlarmsReceiver extends BroadcastReceiver {

    private final String TAG = "SetTodaysAlarmsReceiver";
    private final static String CONTACT_ALARM_TIME = "contact_alarm_time";
    private static final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy.MM.dd"),
            new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yy.MM.dd"),
            new SimpleDateFormat("yy/MM/dd"),
            new SimpleDateFormat("MMM dd, yyyy")
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "SetTodaysAlarmsReceiver");
        wakeLock.acquire();
        try {

            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
            if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
                updateContactDatabase(context);
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

    private void updateContactDatabase(Context context) {
        Log.d(TAG, "UPDATE START TIME: " + System.currentTimeMillis() );
        Cursor contact = getContactsBirthdays(context);
        Map<Long, ContactItem> contacts = new HashMap<Long, ContactItem>();
        if (contact.moveToFirst()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor edit = sharedPreferences.edit();

            do {
                String name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Long contactId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                String icon = contact.getString(contact.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));
                String bDay = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                Date alarm = null;
                Date birthday = null;
                for (SimpleDateFormat f : birthdayFormats) {
                    try {
                        alarm = f.parse(bDay);
                        birthday = alarm;
                        Calendar cal = Calendar.getInstance();
                        Integer year = cal.get(Calendar.YEAR);
                        cal.setTime(alarm);
                        cal.set(Calendar.HOUR_OF_DAY, sharedPreferences.getInt(CONTACT_ALARM_TIME,10)); // TODO update to coresponding time
                        cal.set(Calendar.YEAR, year);
                        alarm = cal.getTime();
                        break;
                    } catch (ParseException e) {
                    }
                }
                ContactItem contactItem = new ContactItem(contactId, null, name, icon, birthday, alarm, null, null);
                contacts.put(contactItem.id, contactItem);
            } while (contact.moveToNext());
        }

        DatabaseHelper dbH = new DatabaseHelper(context);
        dbH.replaceUpdateContactsByMap(contacts);
        Log.d(TAG, "UPDATE END TIME: " + System.currentTimeMillis());
    }

    private Cursor getContactsBirthdays(Context context) {
        Uri uri = ContactsContract.Data.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.NAME_RAW_CONTACT_ID,
                ContactsContract.Data.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Event.START_DATE
        };

        String where = ContactsContract.Data.MIMETYPE + "= ? AND " +
                ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        String[] selectionArgs = new String[]{
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
        };
        String sortOrder = null;
        return context.getContentResolver().query(uri, projection, where, selectionArgs, sortOrder);
    }
}

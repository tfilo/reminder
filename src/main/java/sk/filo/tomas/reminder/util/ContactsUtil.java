package sk.filo.tomas.reminder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.ContactItem;

/**
 * Created by tomas on 10.6.2017.
 */

public class ContactsUtil {

    private static String TAG = "ContactsUtil";

    public final static String CONTACT_ALARM_TIME = "contact_alarm_time";
    public final static String CONTACT_ALARM_TIME_DEFAULT = "10:00";

    public void updateContactDatabase(Context context) {
        Log.d(TAG, "UPDATE START TIME: " + System.currentTimeMillis() );
        Cursor contact = getContactsBirthdays(context);
        Map<Long, ContactItem> contacts = new HashMap<Long, ContactItem>();
        if (contact.moveToFirst()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            do {
                String name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Long contactId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                String icon = contact.getString(contact.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));
                String bDay = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                Date alarm = null;
                Date birthday = null;
                Boolean hasYear = true;
                for (SimpleDateFormat f : DateTimeUtil.birthdayFormats) {
                    try {
                        birthday = f.parse(bDay);
                        alarm = prepareAlarmTime(birthday, sharedPreferences, context);
                        break;
                    } catch (ParseException e) {
                        try {
                            birthday = DateTimeUtil.birthdayFormatNoYear.parse(bDay);
                            hasYear = false;
                            alarm = prepareAlarmTime(birthday, sharedPreferences, context);
                        } catch (ParseException e1) {}
                    }
                }
                if (alarm!=null) {
                    ContactItem contactItem = new ContactItem(contactId, null, name, icon, birthday, alarm, null, null, hasYear);
                    contacts.put(contactItem.id, contactItem);
                }
            } while (contact.moveToNext());
        }

        DatabaseHelper dbH = new DatabaseHelper(context);
        dbH.replaceUpdateContactsByMap(contacts);
        Log.d(TAG, "UPDATE END TIME: " + System.currentTimeMillis());
    }

    private Date prepareAlarmTime(Date birthday, SharedPreferences sharedPreferences, Context context) {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        String timeString = sharedPreferences.getString(CONTACT_ALARM_TIME,CONTACT_ALARM_TIME_DEFAULT);
        Integer hours;
        Integer minutes;
        try {
            Date time = timeFormat.parse(timeString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            hours = cal.get(Calendar.HOUR_OF_DAY);
            minutes = cal.get(Calendar.MINUTE);
        } catch (ParseException e) {
            Log.d(TAG, "Error parsing time of birthday notification");
            hours = 10;
            minutes = 0;
        }
        Calendar cal = Calendar.getInstance();
        Integer year = cal.get(Calendar.YEAR);
        cal.setTime(birthday);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.YEAR, year);
        return cal.getTime();
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

    public void removeContacts(Context context) {
        DatabaseHelper dbH = new DatabaseHelper(context);
        dbH.removeAllContacts();
    }

    public void updateContactsAlarmTime(Context context) {
        DatabaseHelper dbH = new DatabaseHelper(context);
        dbH.updateContactAlarmDatesAndTimes();
    }

    public BirthDayTime getBirthDayNotificationTime(Context context) {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String timeString = sharedPreferences.getString(ContactsUtil.CONTACT_ALARM_TIME, ContactsUtil.CONTACT_ALARM_TIME_DEFAULT);
        BirthDayTime bday;
        try {
            Date time = timeFormat.parse(timeString);
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(time);
            bday = new BirthDayTime(cal1.get(Calendar.HOUR_OF_DAY), cal1.get(Calendar.MINUTE));
        } catch (ParseException e) {
            Log.d(TAG, "Error parsing time of birthday notification");
            bday = new BirthDayTime(10, 0);
        }
        return bday;
    }
}

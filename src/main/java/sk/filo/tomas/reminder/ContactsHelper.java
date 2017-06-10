package sk.filo.tomas.reminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

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

public class ContactsHelper {

    private static String TAG = "ContactsHelper";

    private static final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy.MM.dd"),
            new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yy.MM.dd"),
            new SimpleDateFormat("yy/MM/dd"),
            new SimpleDateFormat("MMM dd, yyyy")
    };

    public final static String CONTACT_ALARM_TIME = "contact_alarm_time";
    public final static int CONTACT_ALARM_TIME_DEFAULT = 10;

    public void updateContactDatabase(Context context) {
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
                        cal.set(Calendar.HOUR_OF_DAY, sharedPreferences.getInt(CONTACT_ALARM_TIME,CONTACT_ALARM_TIME_DEFAULT));
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

    public void removeContacts(Context context) {
        DatabaseHelper dbH = new DatabaseHelper(context);
        dbH.removeAllContacts();
    }

    public void updateContactsAlarmTime(Context context) {
        DatabaseHelper dbH = new DatabaseHelper(context);
        dbH.updateContactAlarmDatesAndTimes();
    }
}

package sk.filo.tomas.reminder.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import sk.filo.tomas.reminder.item.ContactItem;
import sk.filo.tomas.reminder.item.NoteItem;
import sk.filo.tomas.reminder.item.ReminderItem;

/**
 * Created by tomas on 18.10.2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "reminder.sqlite";
    private static final int DB_VERSION = 1;
    private static String TAG = DatabaseHelper.class.getSimpleName();

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contacts " +
                "(id INTEGER PRIMARY KEY," +
                " name TEXT NOT NULL," +
                " img_url TEXT," +
                " hour INTEGER," +
                " minute INTEGER," +
                " day INTEGER NOT NULL," +
                " month INTEGER NOT NULL," +
                " year INTEGER NOT NULL," +
                " day_of_week INTEGER NOT NULL," +
                " alarm_enabled INTEGER NOT NULL DEFAULT 1);");
        db.execSQL("CREATE TABLE notes " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name TEXT NOT NULL," +
                " description TEXT);");
        db.execSQL("CREATE TABLE reminders " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name TEXT NOT NULL," +
                " description TEXT," +
                " hour INTEGER," +
                " minute INTEGER," +
                " day INTEGER NOT NULL," +
                " month INTEGER NOT NULL," +
                " year INTEGER NOT NULL," +
                " day_of_week INTEGER NOT NULL," +
                " alarm_enabled INTEGER NOT NULL DEFAULT 1);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void enableDisableContactAlarm(@NonNull final Long id, @NonNull final Boolean enabled) {
        enableDisableAlarm(id, enabled, "contacts");
    }

    public void enableDisableReminderAlarm(@NonNull final Long id, @NonNull final Boolean enabled) {
        enableDisableAlarm(id, enabled, "reminders");
    }

    private void enableDisableAlarm(Long id, Boolean enabled, String tableName) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            ContentValues content = new ContentValues();
            content.put("alarm_enabled", enabled);
            String where = "id=?";
            String[] args = new String[]{id.toString()};
            wd.update(tableName, content, where, args);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot update item in database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }
    }

    // This method update contacts if changed compare to map, remove if db record is missing in map, add if new in map
    public void replaceUpdateContactsByMap(Map<Long, ContactItem> contacts) {
        List<ContactItem> contactItems = readContacts();
        for (ContactItem oldContact : contactItems) {
            if (contacts.containsKey(oldContact.id)) {
                ContactItem newContact = contacts.get(oldContact.id);
                if (newContact.contactChanged(oldContact)) { // if some contact information changed, update it in old contact in databaze, this preserve alarm_enabled etc
                    oldContact.icon = newContact.icon;
                    oldContact.birthday = newContact.birthday;
                    oldContact.name = newContact.name;
                    replaceContact(oldContact);
                    Log.d(TAG, "Updating contact: " + oldContact.toString());
                }
                contacts.remove(oldContact.id); // after update we dont need contact in map
            } else {
                Log.d(TAG, "Removing contact: " + oldContact.toString());
                removeContact(oldContact.id, "contacts");
            }
        }
        for (Long key : contacts.keySet()) {
            ContactItem contactItem = contacts.get(key);
            Log.d(TAG, "Adding new contact: " + contactItem.toString());
            replaceContact(contactItem); // insert new records to DB
        }
    }


    public long replaceContact(ContactItem item) {
        SQLiteDatabase wd = this.getWritableDatabase();
        long contact_id = -1L;

        wd.beginTransaction();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(item.birthday);

            ContentValues contact = new ContentValues();
            contact.put("id", item.id);
            contact.put("name", item.name);
            contact.put("img_url", item.icon);
            contact.put("day", cal.get(Calendar.DAY_OF_MONTH));
            contact.put("month", cal.get(Calendar.MONTH));
            contact.put("year", cal.get(Calendar.YEAR));
            contact.put("day_of_week", cal.get(Calendar.DAY_OF_WEEK));
            if (item.alarmEnabled != null) {
                contact.put("alarm_enabled", item.alarmEnabled);
            }
            contact_id = wd.replaceOrThrow("contacts", null, contact);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot write ContactItem to database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }

        return contact_id;
    }

    public long replaceReminder(ReminderItem item) {
        SQLiteDatabase wd = this.getWritableDatabase();
        long reminder_id = -1L;

        wd.beginTransaction();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(item.notificationTime);

            ContentValues reminder = new ContentValues();
            if (item.id != null) {
                reminder.put("id", item.id);
            }
            reminder.put("name", item.name);
            reminder.put("description", item.description);
            reminder.put("day", cal.get(Calendar.DAY_OF_MONTH));
            reminder.put("month", cal.get(Calendar.MONTH));
            reminder.put("year", cal.get(Calendar.YEAR));
            reminder.put("day_of_week", cal.get(Calendar.DAY_OF_WEEK));
            if (item.alarmEnabled != null) {
                reminder.put("alarm_enabled", item.alarmEnabled);
            }
            reminder_id = wd.replaceOrThrow("reminders", null, reminder);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot write ReminderItem to database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }

        return reminder_id;
    }

    public long replaceNote(NoteItem item) {
        SQLiteDatabase wd = this.getWritableDatabase();
        long note_id = -1L;

        wd.beginTransaction();
        try {
            ContentValues note = new ContentValues();
            if (item.id != null) {
                note.put("id", item.id);
            }
            note.put("name", item.name);
            note.put("description", item.description);
            note_id = wd.replaceOrThrow("notes", null, note);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot write NoteItem to database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }

        return note_id;
    }

    public List<ContactItem> readContacts() {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery("SELECT id, name, img_url, day, month, year, alarm_enabled FROM contacts " +
                "ORDER BY month, day ASC;", null);
        List<ContactItem> contacts = new ArrayList<ContactItem>();
        try {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String img_url = cursor.getString(cursor.getColumnIndexOrThrow("img_url"));
                Integer day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                Integer month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                Integer year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(year, month, day, 0, 0, 0);
                contacts.add(new ContactItem(id, name, img_url, cal.getTime(), alarmEnabled));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read ContactItem from database, " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return contacts;
    }

    public void removeContact(@NonNull Long id, @NonNull String table) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            wd.delete(table, "id=?", new String[]{id.toString()});
            wd.setTransactionSuccessful();
        } finally {
            wd.endTransaction();
            wd.close();
        }
    }
}

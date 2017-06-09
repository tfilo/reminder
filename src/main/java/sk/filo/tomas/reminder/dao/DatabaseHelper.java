package sk.filo.tomas.reminder.dao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sk.filo.tomas.reminder.AlarmReceiver;
import sk.filo.tomas.reminder.MainActivity;
import sk.filo.tomas.reminder.item.AlarmExtendedItem;
import sk.filo.tomas.reminder.item.AlarmItem;
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
    private Context mCtx;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mCtx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contacts " +
                "(id INTEGER PRIMARY KEY," +
                " name TEXT NOT NULL," +
                " img_url TEXT," +
                " birthday INTEGER NOT NULL, " +
                " alarm_fk INTEGER NOT NULL," +
                "FOREIGN KEY(alarm_fk) REFERENCES alarms(id));");
        db.execSQL("CREATE TABLE notes " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name TEXT," +
                " description TEXT);");
        db.execSQL("CREATE TABLE reminders " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name TEXT," +
                " description TEXT," +
                " alarm_fk INTEGER NOT NULL," +
                "FOREIGN KEY(alarm_fk) REFERENCES alarms(id));");

        db.execSQL("CREATE TABLE alarms " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " alarm_time INTEGER NOT NULL, " +
                " every_year INTEGER NOT NULL DEFAULT 0," +
                " alarm_enabled INTEGER NOT NULL DEFAULT 1," +
                " last_executed INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private Integer getDateTime(Date date) {
        if (date == null) return null;
        return new Long(date.getTime() / 1000).intValue();
    }

    private Date getDateTime(Integer date) {
        if (date == null) return null;
        return new Date(date * 1000L);
    }

    public AlarmItem getEnabledAlarmItem(Long alarmId) {
        AlarmItem ai = null;
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor alarmCursor = rd.rawQuery(
                "SELECT id, alarm_time, last_executed FROM alarms " +
                        "WHERE id = ? AND alarm_enabled = 1",
                new String[]{alarmId.toString()}
        );
        try {
            if (alarmCursor.moveToNext()) {
                Long id = alarmCursor.getLong(alarmCursor.getColumnIndexOrThrow("id"));
                Integer alarmDate = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("alarm_time"));
                Integer lastExecuted = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("last_executed"));
                ai = new AlarmItem(id, getDateTime(alarmDate), getDateTime(lastExecuted));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read AlarmItem from database, " + iae.getMessage());
        } finally {
            alarmCursor.close();
            rd.close();
        }
        return ai;
    }

    public AlarmExtendedItem getExtendedAlarmInfo(Long alarmid) {
        SQLiteDatabase rd = this.getReadableDatabase();
        AlarmExtendedItem aei = null;
        if (alarmid != null) {

            AlarmItem alarmItem = null;
            Cursor alarmCursor = rd.rawQuery(
                    "SELECT id, alarm_time, last_executed FROM alarms " +
                            "WHERE id = ? AND alarm_enabled = 1",
                    new String[]{alarmid.toString()}
            );
            try {
                if (alarmCursor.moveToNext()) {
                    Long id = alarmCursor.getLong(alarmCursor.getColumnIndexOrThrow("id"));
                    Integer alarmDate = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("alarm_time"));
                    Integer lastExecuted = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("last_executed"));
                    alarmItem = new AlarmItem(id, getDateTime(alarmDate), getDateTime(lastExecuted));
                }
            } catch (IllegalArgumentException iae) {
                Log.d(TAG, "Cannot read AlarmItem from database, " + iae.getMessage());
            } finally {
                alarmCursor.close();
            }
            if (alarmItem == null) return null;

            Cursor reminder = rd.rawQuery("SELECT id, name, description FROM reminders WHERE alarm_fk = ? ", new String[]{alarmid.toString()});
            try {
                if (reminder.moveToNext()) {
                    Long parId = reminder.getLong(reminder.getColumnIndexOrThrow("id"));
                    String name = reminder.getString(reminder.getColumnIndexOrThrow("name"));
                    String description = reminder.getString(reminder.getColumnIndexOrThrow("description"));
                    aei = new AlarmExtendedItem(alarmItem, parId, name, description, AlarmExtendedItem.Type.REMINDER);
                } else {
                    Cursor contact = rd.rawQuery("SELECT id, name FROM contacts WHERE alarm_fk = ? ", new String[]{alarmid.toString()});
                    try {
                        if (contact.moveToNext()) {
                            Long parId = contact.getLong(contact.getColumnIndexOrThrow("id"));
                            String name = contact.getString(contact.getColumnIndexOrThrow("name"));
                            aei = new AlarmExtendedItem(alarmItem, parId, name, null, AlarmExtendedItem.Type.CONTACT);
                        }
                    } finally {
                        contact.close();
                    }
                }
            } finally {
                reminder.close();
                rd.close();
            }
        }
        return aei;
    }

    public List<AlarmItem> getAlarmsToSetup() {
        List<AlarmItem> aiList = new ArrayList<AlarmItem>();

        SQLiteDatabase rd = this.getReadableDatabase();

        Calendar nextMidnight = Calendar.getInstance();
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);
        nextMidnight.set(Calendar.DAY_OF_YEAR, nextMidnight.get(Calendar.DAY_OF_YEAR) + 1);

        Calendar monthBefore = Calendar.getInstance();
        monthBefore.setTime(nextMidnight.getTime());
        monthBefore.set(Calendar.MONTH, nextMidnight.get(Calendar.MONTH) - 1);

        String[] args = new String[]{
                String.valueOf(new Long(nextMidnight.getTime().getTime() / 1000).intValue()),
                String.valueOf(new Long(monthBefore.getTime().getTime() / 1000).intValue())
        };
        Cursor alarmCursor = rd.rawQuery(
                "SELECT id, alarm_time, last_executed FROM alarms " +
                        "WHERE alarm_enabled=1 " +
                        "AND (last_executed IS NULL OR last_executed < alarm_time) " +
                        "AND alarm_time < ? " +
                        "AND alarm_time > ?",
                args
        );
        try {
            while (alarmCursor.moveToNext()) {
                Long id = alarmCursor.getLong(alarmCursor.getColumnIndexOrThrow("id"));
                Integer alarmDate = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("alarm_time"));
                Integer lastExecuted = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("last_executed"));
                AlarmItem ai = new AlarmItem(id, getDateTime(alarmDate), getDateTime(lastExecuted));
                aiList.add(ai);
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read AlarmItem from database, " + iae.getMessage());
        } finally {
            alarmCursor.close();
            rd.close();
        }
        return aiList;
    }

    public void enableDisableAlarm(@NonNull final Long id, @NonNull final Boolean enabled) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            ContentValues content = new ContentValues();
            content.put("alarm_enabled", enabled);
            String where = "id=?";
            String[] args = new String[]{id.toString()};
            wd.update("alarms", content, where, args);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot update alarm in database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
            addUpdateOrRemoveAlarm(id);
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
                removeRecord(oldContact.id, oldContact.alarm_fk, "contacts");
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
        Long alarm_id = null;

        wd.beginTransaction();
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mCtx);

            Calendar cal = Calendar.getInstance();
            Integer year = cal.get(Calendar.YEAR);
            cal.setTime(item.birthday);
            cal.set(Calendar.HOUR_OF_DAY, sharedPreferences.getInt(MainActivity.CONTACT_ALARM_TIME, 10));
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.YEAR, year);

            ContentValues alarm = new ContentValues();
            if (item.alarm_fk != null) {
                alarm.put("id", item.alarm_fk);
            }
            alarm.put("alarm_time", getDateTime(cal.getTime()));
            alarm.put("every_year", true);
            if (item.alarmEnabled != null) {
                alarm.put("alarm_enabled", item.alarmEnabled);
            }
            if (item.lastExecuted != null) {
                alarm.put("last_executed", getDateTime(item.lastExecuted));
            }

            alarm_id = wd.replaceOrThrow("alarms", null, alarm);

            ContentValues contact = new ContentValues();
            contact.put("id", item.id);
            contact.put("name", item.name);
            contact.put("img_url", item.icon);
            contact.put("alarm_fk", alarm_id);
            contact.put("birthday", getDateTime(item.birthday));

            contact_id = wd.replaceOrThrow("contacts", null, contact);

            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot write ContactItem or Alarm to database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
            addUpdateOrRemoveAlarm(alarm_id);
        }

        return contact_id;
    }

    public long replaceReminder(ReminderItem item) {
        Log.d(TAG, "replaceReminder()");
        SQLiteDatabase wd = this.getWritableDatabase();
        long reminder_id = -1L;
        Long alarm_id = null;

        wd.beginTransaction();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(item.notificationTime);

            ContentValues alarm = new ContentValues();
            if (item.alarm_fk != null) {
                alarm.put("id", item.alarm_fk);
            }
            alarm.put("alarm_time", getDateTime(cal.getTime()));
            if (item.alarmEnabled != null) {
                alarm.put("alarm_enabled", item.alarmEnabled);
            }
            if (item.lastExecuted != null) {
                alarm.put("last_executed", getDateTime(item.lastExecuted));
            }

            alarm_id = wd.replaceOrThrow("alarms", null, alarm);

            ContentValues reminder = new ContentValues();
            if (item.id != null) {
                reminder.put("id", item.id);
            }
            reminder.put("name", item.name);
            reminder.put("description", item.description);
            reminder.put("alarm_fk", alarm_id);

            reminder_id = wd.replaceOrThrow("reminders", null, reminder);

            wd.setTransactionSuccessful();
            Log.d(TAG, "Reminder with id " + reminder_id + " stored in database with alarm " + alarm_id);
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot write ReminderItem to database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
            addUpdateOrRemoveAlarm(alarm_id);
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

    public List<ReminderItem> readReminders() {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery("SELECT reminders.id AS id, name, description, alarm_time, alarm_enabled, alarm_fk, last_executed FROM reminders " +
                "JOIN alarms ON alarms.id = reminders.alarm_fk " +
                "ORDER BY alarm_time ASC;", null);
        List<ReminderItem> reminders = new ArrayList<ReminderItem>();
        try {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                Integer alarmDate = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_time"));
                Integer lastExecuted = cursor.getInt(cursor.getColumnIndexOrThrow("last_executed"));
                Long alarmFk = cursor.getLong(cursor.getColumnIndexOrThrow("alarm_fk"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;
                reminders.add(new ReminderItem(id, alarmFk, name, description, getDateTime(alarmDate), alarmEnabled, getDateTime(lastExecuted)));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read ReminderItems from database, " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return reminders;
    }

    public ReminderItem readReminder(Long recId) {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery(
                "SELECT reminders.id AS id, name, description, alarm_time, alarm_enabled, alarm_fk, last_executed FROM reminders " +
                        "JOIN alarms ON alarms.id = reminders.alarm_fk " +
                        "WHERE reminders.id=? ",
                new String[]{recId.toString()}
        );

        ReminderItem reminder = null;
        try {
            if (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                Integer alarmDate = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_time"));
                Integer lastExecuted = cursor.getInt(cursor.getColumnIndexOrThrow("last_executed"));
                Long alarmFk = cursor.getLong(cursor.getColumnIndexOrThrow("alarm_fk"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;
                reminder = new ReminderItem(id, alarmFk, name, description, getDateTime(alarmDate), alarmEnabled, getDateTime(lastExecuted));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read ReminderItem from database, " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return reminder;
    }

    public List<NoteItem> readNotes() {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery("SELECT id, name, description FROM notes " +
                "ORDER BY name, description ASC;", null);
        List<NoteItem> notes = new ArrayList<NoteItem>();
        try {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                notes.add(new NoteItem(id, name, description));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read NoteItems from database, " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return notes;
    }

    public NoteItem readNote(Long recId) {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery(
                "SELECT id, name, description FROM notes WHERE id=? ",
                new String[]{recId.toString()}
        );

        NoteItem note = null;
        try {
            if (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                note = new NoteItem(id, name, description);
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read NoteItem from database, " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return note;
    }

    public List<ContactItem> readContacts() {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery("SELECT contacts.id AS id, name, img_url, alarm_time, alarm_enabled, birthday, alarm_fk, last_executed FROM contacts " +
                "JOIN alarms ON alarms.id = contacts.alarm_fk " +
                "ORDER BY alarm_time ASC;", null);
        List<ContactItem> contacts = new ArrayList<ContactItem>();
        try {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String img_url = cursor.getString(cursor.getColumnIndexOrThrow("img_url"));

                Integer birthday = cursor.getInt(cursor.getColumnIndexOrThrow("birthday"));
                Integer alarmDate = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_time"));
                Integer lastExecuted = cursor.getInt(cursor.getColumnIndexOrThrow("last_executed"));

                Long alarmFk = cursor.getLong(cursor.getColumnIndexOrThrow("alarm_fk"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;

                contacts.add(new ContactItem(id, alarmFk, name, img_url, getDateTime(birthday), getDateTime(alarmDate), alarmEnabled, getDateTime(lastExecuted)));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Cannot read ContactItem from database, " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return contacts;
    }

    public void removeRecord(@NonNull Long id, Long alarmId, @NonNull String table) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            wd.delete(table, "id=?", new String[]{id.toString()});
            if (alarmId != null) {
                wd.delete("alarms", "id=?", new String[]{alarmId.toString()});
            }
            wd.setTransactionSuccessful();
        } finally {
            wd.endTransaction();
            wd.close();
            if (!table.equals("notes")) {
                addUpdateOrRemoveAlarm(alarmId);
            }
        }
    }

    public void addUpdateOrRemoveAlarm(Long id) {
        AlarmItem ai = getEnabledAlarmItem(id);

        AlarmManager am = (AlarmManager) mCtx.getSystemService(mCtx.ALARM_SERVICE);
        Intent i = new Intent(mCtx, AlarmReceiver.class);
        i.putExtra("alarmId", id);
        i.setAction(id.toString());

        if (ai == null) {
            // Alarm under id is disabled or deleted
            PendingIntent broadcast = PendingIntent.getBroadcast(mCtx, id.intValue(), i, PendingIntent.FLAG_NO_CREATE);
            if (broadcast != null) {
                broadcast.cancel();
                Log.d(TAG, "Alarm with id " + id + " deleted");
            }
        } else {
            Calendar thisMidnight = Calendar.getInstance();
            thisMidnight.set(Calendar.HOUR_OF_DAY, 0);
            thisMidnight.set(Calendar.MINUTE, 0);
            thisMidnight.set(Calendar.SECOND, 0);
            thisMidnight.set(Calendar.MILLISECOND, 0);
            thisMidnight.set(Calendar.DAY_OF_YEAR, thisMidnight.get(Calendar.DAY_OF_YEAR) + 1);

            if (ai.alarmTime.after(new Date()) && ai.alarmTime.before(thisMidnight.getTime())) { // set alarms from now to midnight
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, id.intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
                am.set(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                Log.d(TAG, "Alarm with id " + id + " updated");
            }
        }
    }

    public void updateLastExecuted(Long id, Date alarmTime) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            ContentValues content = new ContentValues();
            content.put("last_executed", getDateTime(alarmTime));
            String where = "id=?";
            String[] args = new String[]{id.toString()};
            wd.update("alarms", content, where, args);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "Cannot update alarm in database, " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }
    }
}

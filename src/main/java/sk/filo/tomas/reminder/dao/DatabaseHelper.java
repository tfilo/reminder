package sk.filo.tomas.reminder.dao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import sk.filo.tomas.reminder.item.AlarmExtendedItem;
import sk.filo.tomas.reminder.item.AlarmItem;
import sk.filo.tomas.reminder.item.ContactItem;
import sk.filo.tomas.reminder.item.NoteItem;
import sk.filo.tomas.reminder.item.ReminderItem;
import sk.filo.tomas.reminder.receiver.AlarmReceiver;
import sk.filo.tomas.reminder.util.BirthDayTime;
import sk.filo.tomas.reminder.util.ContactsUtil;
import sk.filo.tomas.reminder.util.DateTimeUtil;

/**
 * Created by tomas on 18.10.2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "reminder.sqlite";
    private static final int DB_VERSION = 1;
    private static String TAG = DatabaseHelper.class.getSimpleName();
    private Context mCtx;
    private ContactsUtil contactsUtil = new ContactsUtil();

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
                " has_year INTEGER NOT NULL DEFAULT 1, " +
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
                " hour INTEGER, " + // used only when reminder
                " minute INTEGER, " + // used only when reminder
                " day INTEGER NOT NULL, " +
                " month INTEGER NOT NULL, " +
                " year INTEGER, " + // used only when reminder
                " alarm_enabled INTEGER NOT NULL DEFAULT 1," +
                " last_executed INTEGER);"); // only year
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public AlarmItem getEnabledAlarmItem(@NonNull Long alarmId) {
        AlarmItem ai = null;
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor alarmCursor = rd.rawQuery(
                "SELECT id, hour, minute, day, month, year, last_executed FROM alarms " +
                        "WHERE id = ? AND alarm_enabled = 1",
                new String[]{alarmId.toString()}
        );
        try {
            if (alarmCursor.moveToNext()) {
                Long id = alarmCursor.getLong(alarmCursor.getColumnIndexOrThrow("id"));
                Integer hour, minute, year = null;
                if (alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("hour")) ||
                        alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("minute"))) {
                    BirthDayTime birthDayNotificationTime = contactsUtil.getBirthDayNotificationTime(mCtx);
                    hour = birthDayNotificationTime.hours;
                    minute = birthDayNotificationTime.minutes;
                } else {
                    hour = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("hour"));
                    minute = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("minute"));
                }
                if (!alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("year"))) {
                    year = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("year"));
                }
                Integer day = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("day"));
                Integer month = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("month"));
                Integer lastExecuted = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("last_executed"));
                Date alarmTime = DateTimeUtil.getDateFromSeparateFields(hour, minute, day, month, year);
                ai = new AlarmItem(id, alarmTime, lastExecuted);
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "getEnabledAlarmItem ERROR: " + iae.getMessage());
        } finally {
            alarmCursor.close();
            rd.close();
        }
        return ai;
    }

    public AlarmExtendedItem getExtendedAlarmInfo(@NonNull Long alarmid) {
        SQLiteDatabase rd = this.getReadableDatabase();
        AlarmExtendedItem aei = null;
        AlarmItem alarmItem = null;
        Cursor alarmCursor = rd.rawQuery(
                "SELECT id, hour, minute, day, month, year, last_executed FROM alarms " +
                        "WHERE id = ? AND alarm_enabled = 1",
                new String[]{alarmid.toString()}
        );
        try {
            if (alarmCursor.moveToNext()) {
                Long id = alarmCursor.getLong(alarmCursor.getColumnIndexOrThrow("id"));
                Integer hour, minute, year = null;
                if (alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("hour")) ||
                        alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("minute"))) {
                    BirthDayTime birthDayNotificationTime = contactsUtil.getBirthDayNotificationTime(mCtx);
                    hour = birthDayNotificationTime.hours;
                    minute = birthDayNotificationTime.minutes;
                } else {
                    hour = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("hour"));
                    minute = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("minute"));
                }
                if (!alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("year"))) {
                    year = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("year"));
                }
                Integer day = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("day"));
                Integer month = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("month"));
                Integer lastExecuted = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("last_executed"));
                Date alarmTime = DateTimeUtil.getDateFromSeparateFields(hour, minute, day, month, year);
                alarmItem = new AlarmItem(id, alarmTime, lastExecuted);
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "getExtendedAlarmInfo alarm ERROR: " + iae.getMessage());
        } finally {
            alarmCursor.close();
        }
        if (alarmItem == null) {
            rd.close();
            return null;
        }

        Cursor reminder = rd.rawQuery("SELECT id, name, description FROM reminders WHERE alarm_fk = ? ", new String[]{alarmid.toString()});
        try {
            if (reminder.moveToNext()) {
                Long parentId = reminder.getLong(reminder.getColumnIndexOrThrow("id"));
                String name = reminder.getString(reminder.getColumnIndexOrThrow("name"));
                String description = reminder.getString(reminder.getColumnIndexOrThrow("description"));
                aei = new AlarmExtendedItem(alarmItem, parentId, name, description, AlarmExtendedItem.Type.REMINDER);
            } else {
                Cursor contact = rd.rawQuery("SELECT id, name FROM contacts WHERE alarm_fk = ? ", new String[]{alarmid.toString()});
                try {
                    if (contact.moveToNext()) {
                        Long parId = contact.getLong(contact.getColumnIndexOrThrow("id"));
                        String name = contact.getString(contact.getColumnIndexOrThrow("name"));
                        aei = new AlarmExtendedItem(alarmItem, parId, name, null, AlarmExtendedItem.Type.CONTACT);
                    }
                } catch (IllegalArgumentException iae) {
                    Log.e(TAG, "getExtendedAlarmInfo contact ERROR: " + iae.getMessage());
                } finally {
                    contact.close();
                }
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "getExtendedAlarmInfo reminder ERROR: " + iae.getMessage());
        } finally {
            reminder.close();
            rd.close();
        }
        return aei;
    }

    public List<AlarmItem> getAlarmsToSetup() {
        List<AlarmItem> aiList = new ArrayList<AlarmItem>();
        SQLiteDatabase rd = this.getReadableDatabase();
        Calendar nextMidnight = DateTimeUtil.getNextMidnight();
        Calendar lastMidnight = DateTimeUtil.getLastMidnight();
        Calendar monthBefore = Calendar.getInstance();
        monthBefore.setTime(nextMidnight.getTime());
        monthBefore.set(Calendar.MONTH, nextMidnight.get(Calendar.MONTH) - 1);

        String[] args = new String[]{
                String.valueOf(lastMidnight.get(Calendar.YEAR)),
                String.valueOf(lastMidnight.get(Calendar.YEAR)),
                String.valueOf(monthBefore.get(Calendar.YEAR)),
                String.valueOf(lastMidnight.get(Calendar.MONTH)),
                String.valueOf(lastMidnight.get(Calendar.DAY_OF_MONTH)),
                String.valueOf(monthBefore.get(Calendar.MONTH)),
                String.valueOf(monthBefore.get(Calendar.DAY_OF_MONTH))
        };
        Cursor alarmCursor = rd.rawQuery(
                "SELECT id, hour, minute, day, month, year, last_executed FROM alarms " +
                        "WHERE alarm_enabled=1 " +
                        "AND (last_executed IS NULL OR last_executed < ?) " + // only not executed now
                        "AND (year IS NULL OR year = ? OR year = ? ) " + // valid only this year or year before (if month before is in last year)
                        "AND ((month = ? AND day <= ?) OR (month = ? AND day >= ?)) " + // valid only this month or month before
                        "ORDER BY year, month, day, hour, minute",
                args
        );
        try {
            while (alarmCursor.moveToNext()) {
                Long id = alarmCursor.getLong(alarmCursor.getColumnIndexOrThrow("id"));
                Integer hour, minute, year = null;
                if (alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("hour")) ||
                        alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("minute"))) { // when time not specified use time from settings
                    BirthDayTime birthDayNotificationTime = contactsUtil.getBirthDayNotificationTime(mCtx);
                    hour = birthDayNotificationTime.hours;
                    minute = birthDayNotificationTime.minutes;
                } else {
                    hour = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("hour"));
                    minute = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("minute"));
                }
                if (!alarmCursor.isNull(alarmCursor.getColumnIndexOrThrow("year"))) {
                    year = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("year"));
                }
                Integer day = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("day"));
                Integer month = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("month"));
                Integer lastExecuted = alarmCursor.getInt(alarmCursor.getColumnIndexOrThrow("last_executed"));
                Date alarmTime = DateTimeUtil.getDateFromSeparateFields(hour, minute, day, month, year);
                AlarmItem ai = new AlarmItem(id, alarmTime, lastExecuted);
                aiList.add(ai);
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "getAlarmsToSetup ERROR " + iae.getMessage());
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
            Log.e(TAG, "enableDisableAlarm ERROR: " + sqlE.getMessage());
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
                if (newContact.contactChanged(oldContact)) { // if some contact information changed, update it in old contact in database, this preserve alarm_enabled etc
                    oldContact.icon = newContact.icon;
                    oldContact.birthday = newContact.birthday;
                    oldContact.name = newContact.name;
                    oldContact.hasYear = newContact.hasYear;
                    oldContact.lastExecuted = null;
                    Log.d(TAG, "replaceUpdateContactsByMap Updating contact: " + oldContact.toString());
                    replaceContact(oldContact);
                }
                contacts.remove(oldContact.id); // after update we dont need contact in map
            } else {
                Log.d(TAG, "replaceUpdateContactsByMap Removing contact: " + oldContact.toString());
                removeRecord(oldContact.id, oldContact.alarm_fk, "contacts");
            }
        }
        for (Long key : contacts.keySet()) {
            ContactItem contactItem = contacts.get(key);
            Log.d(TAG, "replaceUpdateContactsByMap Adding new contact: " + contactItem.toString());
            replaceContact(contactItem); // insert new records to DB
        }
    }

    public void removeAllContacts() {
        List<ContactItem> contactItems = readContacts();
        for (ContactItem contact : contactItems) {
            Log.d(TAG, "removeAllContacts : " + contact.toString());
            removeRecord(contact.id, contact.alarm_fk, "contacts");
        }
    }

    public void updateContactAlarmsTime() {
        List<ContactItem> contactItems = readContacts();
        for (ContactItem contact : contactItems) {
            Log.d(TAG, "updateContactAlarmsTime : " + contact.toString());
            addUpdateOrRemoveAlarm(contact.alarm_fk); // update every setted alarm
        }
    }

    public void replaceContact(@NonNull ContactItem item) {
        SQLiteDatabase wd = this.getWritableDatabase();
        Long alarm_id = null;

        wd.beginTransaction();
        try {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(item.alarmTime);
            ContentValues alarm = new ContentValues();
            if (item.alarm_fk != null) {
                alarm.put("id", item.alarm_fk);
            }
            alarm.put("day", alarmTime.get(Calendar.DAY_OF_MONTH));
            alarm.put("month", alarmTime.get(Calendar.MONTH));
            alarm.putNull("hour");
            alarm.putNull("minute");
            alarm.putNull("year");
            if (item.alarmEnabled != null) {
                alarm.put("alarm_enabled", item.alarmEnabled);
            }
            if (item.lastExecuted != null) {
                alarm.put("last_executed", item.lastExecuted);
            } else { // if adding new contact or contact newer execuded before, use actual date to prevent reminder on next device boot or midnight when missed alarms are set again
                Calendar now = Calendar.getInstance();
                if (alarmTime.after(now)) {
                    alarm.put("last_executed", now.get(Calendar.YEAR) - 1);
                } else {
                    alarm.put("last_executed", now.get(Calendar.YEAR));
                }
            }
            alarm_id = wd.replaceOrThrow("alarms", null, alarm);
            ContentValues contact = new ContentValues();
            contact.put("id", item.id);
            contact.put("name", item.name);
            contact.put("img_url", item.icon);
            contact.put("alarm_fk", alarm_id);
            contact.put("has_year", item.hasYear);
            contact.put("birthday", DateTimeUtil.getDateTime(item.birthday));
            wd.replaceOrThrow("contacts", null, contact);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.e(TAG, "replaceContact ERROR: " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
            if (alarm_id != null) {
                addUpdateOrRemoveAlarm(alarm_id);
            }
        }
    }

    public void replaceReminder(@NonNull ReminderItem item) {
        SQLiteDatabase wd = this.getWritableDatabase();
        Long alarm_id = null;
        wd.beginTransaction();
        try {
            Calendar notification = Calendar.getInstance();
            notification.setTime(item.notificationTime);
            ContentValues alarm = new ContentValues();
            if (item.alarm_fk != null) {
                alarm.put("id", item.alarm_fk);
            }
            alarm.put("day", notification.get(Calendar.DAY_OF_MONTH));
            alarm.put("month", notification.get(Calendar.MONTH));
            alarm.put("hour", notification.get(Calendar.HOUR_OF_DAY));
            alarm.put("minute", notification.get(Calendar.MINUTE));
            alarm.put("year", notification.get(Calendar.YEAR));
            if (item.alarmEnabled != null) {
                alarm.put("alarm_enabled", item.alarmEnabled);
            }
            if (item.lastExecuted != null) {
                alarm.put("last_executed", item.lastExecuted);
            }
            alarm_id = wd.replaceOrThrow("alarms", null, alarm);
            ContentValues reminder = new ContentValues();
            if (item.id != null) {
                reminder.put("id", item.id);
            }
            reminder.put("name", item.name);
            reminder.put("description", item.description);
            reminder.put("alarm_fk", alarm_id);
            wd.replaceOrThrow("reminders", null, reminder);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "replaceReminder ERROR: " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
            if (alarm_id != null) {
                addUpdateOrRemoveAlarm(alarm_id);
            }
        }
    }

    public void replaceNote(@NonNull NoteItem item) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            ContentValues note = new ContentValues();
            if (item.id != null) {
                note.put("id", item.id);
            }
            note.put("name", item.name);
            note.put("description", item.description);
            wd.replaceOrThrow("notes", null, note);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.d(TAG, "replaceNote ERROR: " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }
    }

    public List<ReminderItem> readReminders() {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery("SELECT reminders.id AS id, name, description, hour, minute, day, month, year, alarm_enabled, alarm_fk, last_executed FROM reminders " +
                "JOIN alarms ON alarms.id = reminders.alarm_fk " +
                "ORDER BY year, month, month, day, hour, minute ASC;", null);
        List<ReminderItem> reminders = new ArrayList<ReminderItem>();
        try {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                Integer hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
                Integer minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));
                Integer day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                Integer month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                Integer year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                Integer lastExecuted = cursor.getInt(cursor.getColumnIndexOrThrow("last_executed"));
                Long alarmFk = cursor.getLong(cursor.getColumnIndexOrThrow("alarm_fk"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;
                Date alarmTime = DateTimeUtil.getDateFromSeparateFields(hour, minute, day, month, year);
                reminders.add(new ReminderItem(id, alarmFk, name, description, alarmTime, alarmEnabled, lastExecuted));
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "readReminders ERROR: " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }

        return reminders;
    }

    public ReminderItem readReminder(@NonNull Long recId) {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery(
                "SELECT reminders.id AS id, name, description, hour, minute, day, month, year, alarm_enabled, alarm_fk, last_executed FROM reminders " +
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
                Integer hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
                Integer minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));
                Integer day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                Integer month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                Integer year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                Integer lastExecuted = cursor.getInt(cursor.getColumnIndexOrThrow("last_executed"));
                Long alarmFk = cursor.getLong(cursor.getColumnIndexOrThrow("alarm_fk"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;
                Date alarmTime = DateTimeUtil.getDateFromSeparateFields(hour, minute, day, month, year);
                reminder = new ReminderItem(id, alarmFk, name, description, alarmTime, alarmEnabled, lastExecuted);
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "readReminder ERROR: " + iae.getMessage());
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
            Log.e(TAG, "readNotes " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }
        return notes;
    }

    public NoteItem readNote(@NonNull Long recId) {
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
            Log.e(TAG, "readNote ERROR: " + iae.getMessage());
        } finally {
            cursor.close();
            rd.close();
        }
        return note;
    }

    public List<ContactItem> readContacts() {
        SQLiteDatabase rd = this.getReadableDatabase();
        Cursor cursor = rd.rawQuery("SELECT contacts.id AS id, name, img_url, day, month, alarm_enabled, birthday, alarm_fk, last_executed, has_year FROM contacts " +
                "JOIN alarms ON alarms.id = contacts.alarm_fk " +
                "ORDER BY year, month, month, day, hour, minute ASC;", null);
        List<ContactItem> contacts = new ArrayList<ContactItem>();
        try {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String img_url = cursor.getString(cursor.getColumnIndexOrThrow("img_url"));
                Integer birthday = cursor.getInt(cursor.getColumnIndexOrThrow("birthday"));
                Integer day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                Integer month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                Integer lastExecuted = cursor.getInt(cursor.getColumnIndexOrThrow("last_executed"));
                Long alarmFk = cursor.getLong(cursor.getColumnIndexOrThrow("alarm_fk"));
                Boolean alarmEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("alarm_enabled")) == 0 ? false : true;
                Boolean hasYear = cursor.getInt(cursor.getColumnIndexOrThrow("has_year")) == 0 ? false : true;
                BirthDayTime birthDayNotificationTime = contactsUtil.getBirthDayNotificationTime(mCtx);
                Integer hour = birthDayNotificationTime.hours;
                Integer minute = birthDayNotificationTime.minutes;
                Date alarmTime = DateTimeUtil.getDateFromSeparateFields(hour, minute, day, month, null);
                contacts.add(new ContactItem(id, alarmFk, name, img_url, DateTimeUtil.getDateTime(birthday), alarmTime, alarmEnabled, lastExecuted, hasYear));
            }
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "readContacts ERROR: " + iae.getMessage());
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
            if (!table.equals("notes") && alarmId != null) {
                addUpdateOrRemoveAlarm(alarmId);
            }
        }
    }

    public void addUpdateOrRemoveAlarm(@NonNull Long id) {
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
            Calendar nextMidnight = DateTimeUtil.getNextMidnight();
            if (ai.alarmTime.after(new Date()) && ai.alarmTime.before(nextMidnight.getTime())) { // set alarms from now to midnight
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, id.intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                    am.set(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                } else {
                    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                        am.setExact(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                    } else {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ai.alarmTime.getTime(), pendingIntent);
                    }
                }
                Log.d(TAG, "Alarm with id " + id + " updated");
            }
            Log.d(TAG, "Alarm with id " + id + " not updated");
        }
    }

    public void updateLastExecuted(@NonNull Long id, @NonNull Date alarmTime) {
        SQLiteDatabase wd = this.getWritableDatabase();
        wd.beginTransaction();
        try {
            ContentValues content = new ContentValues();
            Calendar cal = Calendar.getInstance();
            cal.setTime(alarmTime);
            content.put("last_executed", cal.get(Calendar.YEAR));
            String where = "id=?";
            String[] args = new String[]{id.toString()};
            wd.update("alarms", content, where, args);
            wd.setTransactionSuccessful();
        } catch (SQLException sqlE) {
            Log.e(TAG, "updateLastExecuted ERROR: " + sqlE.getMessage());
        } finally {
            wd.endTransaction();
            wd.close();
        }
    }
}

package sk.filo.tomas.reminder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sk.filo.tomas.reminder.adapter.ViewPagerAdapter;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.fragment.ContactsFragment;
import sk.filo.tomas.reminder.fragment.MainFragment;
import sk.filo.tomas.reminder.item.ContactItem;

public class MainActivity extends AppCompatActivity {

    public static final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy.MM.dd"),
            new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yy.MM.dd"),
            new SimpleDateFormat("yy/MM/dd"),
            new SimpleDateFormat("MMM dd, yyyy")
    };

    private static final String TAG = "MainActivity";

    private final static int REQUEST_READ_CONTACTS = 1;
    public final static String USE_CONTACTS = "use_contacts";
    public final static String CONTACT_ALARM_TIME = "contact_alarm_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (savedInstanceState == null) {
            // UPDATE CONTACTS IN DB, synchronous because we don't want to display not valid contacts
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            if (sharedPreferences.getBoolean(USE_CONTACTS, true)) {
                if (PackageManager.PERMISSION_GRANTED != permissionCheck) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTACTS);
                } else {
                    updateContactDatabase();
                }
            }

            Fragment fg = getSupportFragmentManager().findFragmentByTag(MainFragment.class.getName());
            if (fg == null) {
                fg = new MainFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fg, MainFragment.class.getName()).commit();
        }
    }

    private void updateContactDatabase() {
        Log.d(TAG, "UPDATE START TIME: " + System.currentTimeMillis() );
        Cursor contact = getContactsBirthdays();
        Map<Long, ContactItem> contacts = new HashMap<Long, ContactItem>();
        if (contact.moveToFirst()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = sharedPreferences.edit();

            do {
                String name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Long contactId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                String icon = contact.getString(contact.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));
                String bDay = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                Date birthday = null;
                for (SimpleDateFormat f : birthdayFormats) {
                    try {
                        birthday = f.parse(bDay);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(birthday);
                        cal.set(Calendar.HOUR_OF_DAY, sharedPreferences.getInt(CONTACT_ALARM_TIME,8)); // TODO update to coresponding time
                        birthday = cal.getTime();
                        break;
                    } catch (ParseException e) {
                    }
                }
                ContactItem contactItem = new ContactItem(contactId, null, name, icon, birthday, null);
                contacts.put(contactItem.id, contactItem);
            } while (contact.moveToNext());
        }

        DatabaseHelper dbH = new DatabaseHelper(getApplicationContext());
        dbH.replaceUpdateContactsByMap(contacts);
        Log.d(TAG, "UPDATE END TIME: " + System.currentTimeMillis());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor edit = sharedPreferences.edit();
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateContactDatabase();
                    edit.putBoolean(USE_CONTACTS, true);
                } else {
                    Log.d(TAG, "PERMISSION DENIED");
                    edit.putBoolean(USE_CONTACTS, false);
                    Fragment fg = getSupportFragmentManager().findFragmentByTag(MainFragment.class.getName());
                    if (fg != null) {
                        ViewPager mViewPager = (ViewPager) fg.getView().findViewById(R.id.viewpager);
                        if (mViewPager != null) {
                            Log.d(TAG, "Search for fragment");
                            ViewPagerAdapter adapter = (ViewPagerAdapter) mViewPager.getAdapter();
                            Fragment selected = null;
                            for (Fragment f : adapter.mFragmentList) {
                                Log.d(TAG, f.toString());
                                if (f instanceof ContactsFragment) {
                                    selected = f;
                                    Log.d(TAG, "Selected " + selected);
                                }
                            }
                            if (selected != null) {
                                adapter.mFragmentList.remove(selected);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                edit.commit();
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Cursor getContactsBirthdays() {
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
        return getContentResolver().query(uri, projection, where, selectionArgs, sortOrder);
    }
}

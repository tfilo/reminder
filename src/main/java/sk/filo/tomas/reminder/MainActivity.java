package sk.filo.tomas.reminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import sk.filo.tomas.reminder.adapter.ViewPagerAdapter;
import sk.filo.tomas.reminder.fragment.ContactsFragment;
import sk.filo.tomas.reminder.fragment.MainFragment;
import sk.filo.tomas.reminder.fragment.NewReminderFragment;
import sk.filo.tomas.reminder.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final static int REQUEST_READ_CONTACTS = 1;
    public final static int REQUEST_READ_CONTACTS_FROM_SETTINGS = 2;
    private final static String USE_CONTACTS = "use_contacts";
    public final static String LAST_YEAR = "LAST_YEAR";
    public final static String RING_TONE = "RING_TONE";

    private final ContactsHelper contactHelper = new ContactsHelper();

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

            Calendar cal = Calendar.getInstance(); // setup year to preferences if not setup yet
            if (!sharedPreferences.contains(LAST_YEAR)) {
                sharedPreferences.edit().putInt(MainActivity.LAST_YEAR, cal.get(Calendar.YEAR)).commit();
            }

            if (!sharedPreferences.contains(RING_TONE)) {
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if(alarmSound == null){
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    if(alarmSound == null){
                        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    }
                }
                sharedPreferences.edit().putString(RING_TONE, alarmSound.toString()).commit();
            }

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            if (sharedPreferences.getBoolean(USE_CONTACTS, true)) {
                if (PackageManager.PERMISSION_GRANTED != permissionCheck) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTACTS);
                } else {
                    contactHelper.updateContactDatabase(getApplicationContext());
                }
            }
            AlarmManager am = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
            Intent i = new Intent(this, SetTodaysAlarmsReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);

            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT){
                am.set(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent);
            } else {
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                    am.setExact(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent);
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), pendingIntent);
                }
            }

            Fragment fg = getSupportFragmentManager().findFragmentByTag(MainFragment.class.getName());
            if (fg == null) {
                fg = new MainFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fg, MainFragment.class.getName()).commit();
        }
        openReminderDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openReminderDetail();
    }

    public void openSettings(MenuItem item){
        FragmentManager sfm = getSupportFragmentManager();
        SettingsFragment fragment = (SettingsFragment) sfm.findFragmentByTag(SettingsFragment.class.getName());
        if (fragment == null) {
            fragment = new SettingsFragment();
        }
        sfm.beginTransaction().replace(R.id.main_layout, fragment, SettingsFragment.class.getName()).addToBackStack(SettingsFragment.class.getName()).commit();
    }

    private void openReminderDetail() {
        String openFragment = getIntent().getStringExtra("openFragment");
        Log.d(TAG, "openFragment: " + openFragment );
        if ("NewReminderFragment".equals(openFragment)) {
            Long id = getIntent().getLongExtra("reminderId", 0);
            Log.d(TAG, "reminderId: " + id );
            Fragment fg = new NewReminderFragment();
            Bundle args = new Bundle();
            args.putLong("RecordId", id);
            fg.setArguments(args);

            getIntent().removeExtra("openFragment");
            getIntent().removeExtra("reminderId");
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fg, NewReminderFragment.class.getName()).addToBackStack(NewReminderFragment.class.getName()).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contactHelper.updateContactDatabase(getApplicationContext());
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
                                    break;
                                }
                            }
                            if (selected != null) {
                                adapter.mFragmentList.remove(selected);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                break;
            }
        }
        edit.commit();
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
}

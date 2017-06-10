package sk.filo.tomas.reminder.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import sk.filo.tomas.reminder.ContactsHelper;
import sk.filo.tomas.reminder.InputMinMaxFilter;
import sk.filo.tomas.reminder.MainActivity;
import sk.filo.tomas.reminder.R;

public class SettingsFragment extends Fragment {

    private static String TAG = "SettingsFragment";

    private Switch mBdayAlerts;
    private EditText mBdayNotificationTime;
    private Button mChooseNotification;
    private Button mSave;

    private Uri notificationUri;
    private Integer notificationTime;
    private Boolean notificationEnabled;

    private final ContactsHelper contactsHelper = new ContactsHelper();

    public final static String USE_CONTACTS = "use_contacts";
    public final static String CONTACT_ALARM_TIME = "contact_alarm_time";

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_settings, container, false);

        mBdayAlerts = (Switch) w.findViewById(R.id.use_contacts);
        mBdayNotificationTime = (EditText) w.findViewById(R.id.defaultContactAlert);
        mChooseNotification = (Button) w.findViewById(R.id.ringtone_picker);
        mSave = (Button) w.findViewById(R.id.save_settings);

        mBdayNotificationTime.setFilters(new InputFilter[]{ new InputMinMaxFilter("0", "23")});

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        notificationEnabled = sharedPreferences.getBoolean(USE_CONTACTS, true);
        notificationTime = sharedPreferences.getInt(CONTACT_ALARM_TIME,10);
        notificationUri = Uri.parse(sharedPreferences.getString(MainActivity.RING_TONE, ""));

        mBdayAlerts.setChecked(notificationEnabled);
        mBdayNotificationTime.setText(String.valueOf(notificationTime));

        mChooseNotification.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationUri);
                startActivityForResult( intent, 123);
            }
        });

        mBdayAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBdayAlerts.isChecked()) {
                    int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
                    if (PackageManager.PERMISSION_GRANTED != permissionCheck) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MainActivity.REQUEST_READ_CONTACTS_FROM_SETTINGS);
                    }
                }
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeString = mBdayNotificationTime.getText().toString();
                if (timeString.isEmpty()) {
                    mBdayNotificationTime.setError(getResources().getString(R.string.insert_time));
                } else {
                    Integer i = Integer.parseInt(timeString);
                    if (i>=0 && i<=23) {
                        sharedPreferences.edit().putInt(CONTACT_ALARM_TIME, i).commit();
                        sharedPreferences.edit().putBoolean(USE_CONTACTS, mBdayAlerts.isChecked()).commit();
                        sharedPreferences.edit().putString(MainActivity.RING_TONE, notificationUri.toString()).commit();

                        if (!notificationTime.equals(i)) { // if contact birthday notification time changed, need to update
                            contactsHelper.updateContactsAlarmTime(getContext());
                        }
                        if (mBdayAlerts.isChecked()!=notificationEnabled) { // if birthday notification changed
                           if (mBdayAlerts.isChecked()) {
                               int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
                               if (PackageManager.PERMISSION_GRANTED != permissionCheck) { // and permision NOT granted
                                   sharedPreferences.edit().putBoolean(USE_CONTACTS, false).commit(); // setup to false because user rejected permission
                               } else { // update contacts
                                   contactsHelper.updateContactDatabase(getContext());
                               }
                           } else { // remove contacts when disabled
                               contactsHelper.removeContacts(getContext());
                           }
                        }
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        mBdayNotificationTime.setError(getResources().getString(R.string.insert_time));
                    }
                }
            }
        });

        return w;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == 123) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                notificationUri = uri;
            } else {
                Toast toast = Toast.makeText(getContext(), R.string.invalid_ringtone, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}




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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import sk.filo.tomas.reminder.helper.ContactsHelper;
import sk.filo.tomas.reminder.MainActivity;
import sk.filo.tomas.reminder.R;

public class SettingsFragment extends Fragment {

    private static String TAG = "SettingsFragment";

    private Switch mBdayAlerts;
    private EditText mBdayNotificationTime;
    private Button mChooseNotification;

    private final ContactsHelper contactsHelper = new ContactsHelper();

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_settings, container, false);

        mBdayAlerts = (Switch) w.findViewById(R.id.use_contacts);
        mBdayNotificationTime = (EditText) w.findViewById(R.id.defaultContactAlert);
        mChooseNotification = (Button) w.findViewById(R.id.ringtone_picker);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        mBdayAlerts.setChecked(sharedPreferences.getBoolean(MainActivity.USE_CONTACTS, true));

        mBdayNotificationTime.setText(sharedPreferences.getString(ContactsHelper.CONTACT_ALARM_TIME,"10:00"));

        mChooseNotification.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sharedPreferences.getString(MainActivity.RING_TONE, "")));
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
                    } else {
                        contactsHelper.updateContactDatabase(getContext());
                        sharedPreferences.edit().putBoolean(MainActivity.USE_CONTACTS, true).commit();
                    }
                } else {
                    sharedPreferences.edit().putBoolean(MainActivity.USE_CONTACTS, false).commit();
                    contactsHelper.removeContacts(getContext());
                }
            }
        });

        mBdayNotificationTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String newValue = editable.toString();
                if (!newValue.isEmpty()) {
                    sharedPreferences.edit().putString(ContactsHelper.CONTACT_ALARM_TIME, newValue).commit();
                    contactsHelper.updateContactsAlarmTime(getContext());
                }
            }
        });

        mBdayNotificationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putString(TimePickerFragment.TIME, mBdayNotificationTime.getText().toString());
                bundle.putInt(TimePickerFragment.TARGET_ID, R.id.defaultContactAlert);
                newFragment.setArguments(bundle);
                newFragment.show(getActivity().getSupportFragmentManager(), TimePickerFragment.class.getName());
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
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                sharedPreferences.edit().putString(MainActivity.RING_TONE, uri.toString()).commit();
            } else {
                Toast toast = Toast.makeText(getContext(), R.string.invalid_ringtone, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}




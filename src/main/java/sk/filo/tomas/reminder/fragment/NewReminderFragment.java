package sk.filo.tomas.reminder.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.ReminderItem;

public class NewReminderFragment extends Fragment {

    private static String TAG = "NewReminderFragment";

    private EditText mTime;
    private EditText mDate;
    private EditText mName;
    private EditText mDescription;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mRemoveFab;
    private Switch mAlarmEnabled;
    private DatabaseHelper mDbH;
    private Long id;
    private Long alarmFk;

    public NewReminderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.new_reminder_fragment, container, false);

        mDbH = new DatabaseHelper(getContext());
        mTime = (EditText) w.findViewById(R.id.new_time);
        mDate = (EditText) w.findViewById(R.id.new_date);
        mName = (EditText) w.findViewById(R.id.new_name);
        mDescription = (EditText) w.findViewById(R.id.new_description);
        mAlarmEnabled = (Switch) w.findViewById(R.id.alarm_enabled);
        final DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext());
        final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());

        Bundle arguments = this.getArguments();
        ReminderItem reminderItem = null;
        if (arguments!=null && arguments.containsKey("RecordId")) {
            id = arguments.getLong("RecordId");
            reminderItem = mDbH.readReminder(id);
        }
        if (reminderItem!=null) {
            mTime.setText(timeFormat.format(reminderItem.notificationTime));
            mDate.setText(dateFormat.format(reminderItem.notificationTime));
            mName.setText(reminderItem.name);
            mDescription.setText(reminderItem.description);
            mAlarmEnabled.setChecked(reminderItem.alarmEnabled);
            alarmFk = reminderItem.alarm_fk;
        } else {
            id = null;
            alarmFk = null;
            mTime.setText(timeFormat.format(new Date()));
            mDate.setText(dateFormat.format(new Date()));
            mAlarmEnabled.setChecked(true);
        }

        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(TimePickerFragment.TARGET_ID, R.id.new_time);
                newFragment.setArguments(bundle);
                newFragment.show(getActivity().getSupportFragmentManager(), TimePickerFragment.class.getName());
            }
        });

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), DatePickerFragment.class.getName());
            }
        });

        mSaveFab = (FloatingActionButton) w.findViewById(R.id.save);
        mSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date alarmTime = null;
                try {
                    Date time = timeFormat.parse(mTime.getText().toString());
                    Date date = dateFormat.parse(mDate.getText().toString());

                    Calendar timeCal = Calendar.getInstance();
                    Calendar dateCal = Calendar.getInstance();

                    timeCal.setTime(time);
                    dateCal.setTime(date);

                    Calendar dateTimeCal = Calendar.getInstance();

                    dateTimeCal.set(
                            dateCal.get(Calendar.YEAR),
                            dateCal.get(Calendar.MONTH),
                            dateCal.get(Calendar.DAY_OF_MONTH),
                            timeCal.get(Calendar.HOUR_OF_DAY),
                            timeCal.get(Calendar.MINUTE),
                            0
                    );

                    alarmTime = dateTimeCal.getTime();
                } catch (ParseException e) {
                    Log.d(TAG, "Unable parse dateTime string");
                }

                if (alarmTime.before(new Date()) && mAlarmEnabled.isChecked()) {
                    Toast toast = Toast.makeText(getContext(), R.string.date_time_before, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                ReminderItem ri = new ReminderItem(
                        id,
                        alarmFk,
                        mName.getText().toString(),
                        mDescription.getText().toString(),
                        alarmTime,
                        mAlarmEnabled.isChecked(),
                        null);
                Log.d(TAG, ri.toString());
                mDbH.replaceReminder(ri);

                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mRemoveFab = (FloatingActionButton) w.findViewById(R.id.remove);
        mRemoveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id!=null) {
                    mDbH.removeRecord(id, alarmFk, "reminders");
                }

                getActivity().getSupportFragmentManager().popBackStack();
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

}




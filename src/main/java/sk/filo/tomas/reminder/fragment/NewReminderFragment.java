package sk.filo.tomas.reminder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Date;

import sk.filo.tomas.reminder.R;

public class NewReminderFragment extends Fragment {

    private static String TAG = "NewReminderFragment";

    private EditText mTime;
    private EditText mDate;
    private EditText mName;
    private EditText mDescription;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mRemoveFab;

    public NewReminderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.new_reminder_fragment, container, false);

        mTime = (EditText) w.findViewById(R.id.new_time);
        mDate = (EditText) w.findViewById(R.id.new_date);
        mName = (EditText) w.findViewById(R.id.new_name);
        mDescription = (EditText) w.findViewById(R.id.new_description);

        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());

        mTime.setText(timeFormat.format(new Date()));
        mDate.setText(dateFormat.format(new Date()));

        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
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
                Snackbar.make(view, "Will save new reminder", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRemoveFab = (FloatingActionButton) w.findViewById(R.id.remove);
        mRemoveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Will delete or cancel reminder", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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




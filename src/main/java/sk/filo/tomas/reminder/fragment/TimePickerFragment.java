package sk.filo.tomas.reminder.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.util.DateTimeUtil;

/**
 * Created by tomas on 19.10.2016.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private  static final String TAG = "TimePickerFragment";
    private java.text.DateFormat timeFormat;
    public static final String TARGET_ID = "target";
    public static final String TIME = "time";
    private int target;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());
        target = getArguments().getInt(TARGET_ID);
        int hour, minute;
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        if (getArguments().containsKey(TIME)) {
            Date time = DateTimeUtil.parseTime(getArguments().getString(TIME));
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
        }
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        EditText time = (EditText) getActivity().findViewById(target);
        Calendar cal = DateTimeUtil.getLastMidnight();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        time.setText(timeFormat.format(cal.getTime()));
    }
}

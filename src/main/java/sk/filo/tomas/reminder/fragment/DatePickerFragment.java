package sk.filo.tomas.reminder.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.util.DateTimeUtil;

/**
 * Created by tomas on 19.10.2016.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        EditText time = (EditText) getActivity().findViewById(R.id.new_date);
        Calendar cal = DateTimeUtil.getLastMidnight();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext());
        time.setText(dateFormat.format(cal.getTime()));
    }
}

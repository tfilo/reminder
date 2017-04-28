package sk.filo.tomas.reminder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.NoteItem;
import sk.filo.tomas.reminder.item.ReminderItem;

public class NewNoteFragment extends Fragment {

    private static String TAG = "NewNoteFragment";

    private EditText mName;
    private EditText mDescription;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mRemoveFab;
    private DatabaseHelper mDbH;
    private Long id;

    public NewNoteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.new_note_fragment, container, false);

        mDbH = new DatabaseHelper(getContext());
        mName = (EditText) w.findViewById(R.id.new_name);
        mDescription = (EditText) w.findViewById(R.id.new_description);

        Bundle arguments = this.getArguments();
        if (arguments!=null && arguments.containsKey("RecordId")) {
            id = arguments.getLong("RecordId");
            NoteItem noteItem = mDbH.readNote(id);
            mName.setText(noteItem.name);
            mDescription.setText(noteItem.description);
        } else {
            id = null;
        }

        mSaveFab = (FloatingActionButton) w.findViewById(R.id.save);
        mSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteItem ni = new NoteItem(
                        id,
                        mName.getText().toString(),
                        mDescription.getText().toString()
                );
                Log.d(TAG, ni.toString());
                mDbH.replaceNote(ni);

                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mRemoveFab = (FloatingActionButton) w.findViewById(R.id.remove);
        mRemoveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id!=null) {
                    mDbH.removeRecord(id, null, "notes");
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




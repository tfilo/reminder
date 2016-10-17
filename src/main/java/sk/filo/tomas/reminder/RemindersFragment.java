package sk.filo.tomas.reminder;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemindersFragment extends Fragment {

    private static String TAG = "RemindersFragment";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public RemindersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_reminders, container, false);
        mRecyclerView = (RecyclerView) w.findViewById(R.id.reminders_content_main);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        List<ReminderItem> mDataset = new ArrayList<ReminderItem>();



        Log.d(TAG, "Start: " + System.currentTimeMillis() + "ms");
        for (int i=0; i<20; i++) {
            mDataset.add(new ReminderItem("Name " + i, "Popis " + i, new Date()));
        }
        Log.d(TAG, "End: " + System.currentTimeMillis() + "ms");

        mAdapter = new ReminderItemAdapter(mDataset, getContext());
        mRecyclerView.setAdapter(mAdapter);
        return w;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}




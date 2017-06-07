package sk.filo.tomas.reminder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.adapter.BasicItemAdapter;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.BasicItem;
import sk.filo.tomas.reminder.item.ReminderItem;
import sk.filo.tomas.reminder.listener.CustomItemClickListener;

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

        final List<BasicItem> mDataset = new ArrayList<BasicItem>();

        DatabaseHelper mDbH = new DatabaseHelper(getContext());
        List<ReminderItem> reminderItems = mDbH.readReminders();
        mDataset.addAll(reminderItems);

        CustomItemClickListener listener = new CustomItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                FragmentManager sfm = getActivity().getSupportFragmentManager();
                NewReminderFragment fragment = (NewReminderFragment) sfm.findFragmentByTag(NewReminderFragment.class.getName());
                if (fragment == null) {
                    fragment = new NewReminderFragment();
                }
                Bundle args = new Bundle();
                args.putLong("RecordId", mDataset.get(position).id);
                fragment.setArguments(args);
                sfm.beginTransaction().replace(R.id.main_layout, fragment, NewReminderFragment.class.getName()).addToBackStack(NewReminderFragment.class.getName()).commit();
            }
        };

        FloatingActionButton fab = (FloatingActionButton) w.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager sfm = getActivity().getSupportFragmentManager();
                NewReminderFragment fragment = (NewReminderFragment) sfm.findFragmentByTag(NewReminderFragment.class.getName());
                if (fragment == null) {
                    fragment = new NewReminderFragment();
                }
                sfm.beginTransaction().replace(R.id.main_layout, fragment, NewReminderFragment.class.getName()).addToBackStack(NewReminderFragment.class.getName()).commit();
            }
        });

        mAdapter = new BasicItemAdapter(mDataset, getContext(), listener);
        mRecyclerView.setAdapter(mAdapter);
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




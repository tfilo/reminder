package sk.filo.tomas.reminder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import sk.filo.tomas.reminder.item.ContactItem;

public class ContactsFragment extends Fragment {

    private static String TAG = "ContactsFragment";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_contacts, container, false);
        mRecyclerView = (RecyclerView) w.findViewById(R.id.contacts_content_main);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        final List<BasicItem> mDataset = new ArrayList<BasicItem>();
        DatabaseHelper dbH = new DatabaseHelper(getContext());
        List<ContactItem> contactItems = dbH.readContacts();
        mDataset.addAll(contactItems);

        mAdapter = new BasicItemAdapter(mDataset, getContext());
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




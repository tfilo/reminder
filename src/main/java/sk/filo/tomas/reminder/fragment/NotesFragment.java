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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.adapter.BasicItemAdapter;
import sk.filo.tomas.reminder.item.BasicItem;
import sk.filo.tomas.reminder.item.NoteItem;
import sk.filo.tomas.reminder.listener.CustomItemClickListener;

public class NotesFragment extends Fragment {

    private static String TAG = "NotesFragment";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public NotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_notes, container, false);
        mRecyclerView = (RecyclerView) w.findViewById(R.id.notes_content_main);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        final List<BasicItem> mDataset = new ArrayList<BasicItem>();

        for (int i = 0; i < 20; i++) {
            mDataset.add(new NoteItem(null, "Name " + i, "Popis " + i));
        }

        CustomItemClickListener listener = new CustomItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(getContext(), mDataset.get(position).toString(), Toast.LENGTH_LONG).show();
            }
        };

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




package sk.filo.tomas.reminder;

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
import java.util.Date;
import java.util.List;

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

        List<NoteItem> mDataset = new ArrayList<NoteItem>();

        Log.d(TAG, "Start: " + System.currentTimeMillis() + "ms");
        for (int i=0; i<20; i++) {
            mDataset.add(new NoteItem("Name " + i, "Popis " + i));
        }
        Log.d(TAG, "End: " + System.currentTimeMillis() + "ms");

        mAdapter = new NoteItemAdapter(mDataset, getContext());
        mRecyclerView.setAdapter(mAdapter);
        return w;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}




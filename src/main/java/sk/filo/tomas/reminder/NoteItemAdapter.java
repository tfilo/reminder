package sk.filo.tomas.reminder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tomas on 17.10.2016.
 */

public class NoteItemAdapter extends RecyclerView.Adapter<NoteItemAdapter.ViewHolder> {

    private final List<NoteItem> mDataset;
    private final Context mCtx;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description;

        public ViewHolder(LinearLayout ll) {
            super(ll);
            name = (TextView)ll.findViewById(R.id.note_name);
            description = (TextView)ll.findViewById(R.id.note_description);
        }
    }

    public NoteItemAdapter(List<NoteItem> dataset, Context ctx) {
        mDataset = dataset;
        mCtx = ctx;
    }

    @Override
    public NoteItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        ViewHolder vh = new ViewHolder(ll);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NoteItem noteItem = mDataset.get(position);

        holder.name.setText(noteItem.name);
        holder.description.setText(noteItem.description);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

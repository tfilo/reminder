package sk.filo.tomas.reminder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by tomas on 17.10.2016.
 */

public class ReminderItemAdapter extends RecyclerView.Adapter<ReminderItemAdapter.ViewHolder> {

    private final List<ReminderItem> mDataset;
    private final Context mCtx;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description, notificationTime;

        public ViewHolder(LinearLayout ll) {
            super(ll);
            name = (TextView)ll.findViewById(R.id.reminder_name);
            notificationTime = (TextView)ll.findViewById(R.id.reminder_time);
            description = (TextView)ll.findViewById(R.id.reminder_description);
        }
    }

    public ReminderItemAdapter(List<ReminderItem> dataset, Context ctx) {
        mDataset = dataset;
        mCtx = ctx;
    }

    @Override
    public ReminderItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_item, parent, false);
        ViewHolder vh = new ViewHolder(ll);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.notificationTime.setText("");

        ReminderItem reminderItem = mDataset.get(position);

        holder.name.setText(reminderItem.name);
        holder.description.setText(reminderItem.description);

        String date = DateFormat.getDateTimeInstance().format(reminderItem.notificationTime);

        holder.notificationTime.setText(date);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

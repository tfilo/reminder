package sk.filo.tomas.reminder;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * Created by tomas on 17.10.2016.
 */

public class ContactItemAdapter extends RecyclerView.Adapter<ContactItemAdapter.ViewHolder> {

    private final String TAG = "ContactItemAdapter";
    private final List<ContactItem> mDataset;
    private final Context mCtx;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, birthday;
        public ImageView icon;

        public ViewHolder(LinearLayout ll) {
            super(ll);
            name = (TextView)ll.findViewById(R.id.contact_name);
            birthday = (TextView)ll.findViewById(R.id.contact_birthday);
            icon = (ImageView)ll.findViewById(R.id.contact_icon);
        }

        public void setItem(ContactItem contactItem) {
            name.setText(contactItem.name);
            if (contactItem.birthday!=null) {
                String month = (String) android.text.format.DateFormat.format("MMM", contactItem.birthday);
                String day = (String) android.text.format.DateFormat.format("dd", contactItem.birthday);
                Integer year = Integer.valueOf((String)android.text.format.DateFormat.format("yyyy", contactItem.birthday));
                Integer actualYear = Integer.valueOf((String)android.text.format.DateFormat.format("yyyy", new Date()));
                StringBuilder sb = new StringBuilder();
                sb.append(day);
                sb.append(". ");
                sb.append(month);
                sb.append(" (");
                sb.append(actualYear-year);
                sb.append(" ");
                sb.append(mCtx.getResources().getString(R.string.years));
                sb.append(")");
                birthday.setText(sb.toString());
            } else {
                birthday.setText("");
            }
            if (contactItem.icon != null) {
                Uri iconUri = Uri.parse(contactItem.icon);
                icon.setImageURI(iconUri);
            } else {
                icon.setImageResource(R.drawable.ic_person_black_36dp);
            }
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick " + getLayoutPosition()+ " " + name);
        }
    }

    public ContactItemAdapter(List<ContactItem> dataset, Context ctx) {
        mDataset = dataset;
        mCtx = ctx;
    }

    @Override
    public ContactItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        ViewHolder vh = new ViewHolder(ll);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setItem(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

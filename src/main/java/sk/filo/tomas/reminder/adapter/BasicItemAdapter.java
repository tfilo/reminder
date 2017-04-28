package sk.filo.tomas.reminder.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.BasicItem;
import sk.filo.tomas.reminder.item.ContactItem;
import sk.filo.tomas.reminder.item.NoteItem;
import sk.filo.tomas.reminder.item.ReminderItem;
import sk.filo.tomas.reminder.listener.CustomItemClickListener;

/**
 * Created by tomas on 17.10.2016.
 */

public class BasicItemAdapter extends RecyclerView.Adapter<BasicItemAdapter.BasicHolder> {

    private final String TAG = "BasicItemAdapter";
    private final List<BasicItem> mDataset;
    private final Context mCtx;
    private final CustomItemClickListener mListener;

    public BasicItemAdapter(List<BasicItem> dataset, Context ctx) {
        mDataset = dataset;
        mCtx = ctx;
        mListener = null;
    }

    public BasicItemAdapter(List<BasicItem> dataset, Context ctx, CustomItemClickListener listener) {
        mDataset = dataset;
        mCtx = ctx;
        mListener = listener;
    }

    @Override
    public BasicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = null;
        BasicHolder mViewHolder = null;

        switch (viewType) {
            case 0:
                break;
            case 1:
                mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
                mViewHolder = new ReminderHolder(mView);
                break;
            case 2:
                mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
                mViewHolder = new NoteHolder(mView);
                break;
            case 3:
                mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
                mViewHolder = new ContactHolder(mView);
                break;
        }

        final BasicHolder mHolder = mViewHolder;

        if (mView != null && mListener != null) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(v, mHolder.getAdapterPosition());
                }
            });
        }

        return mViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset.get(position) instanceof ReminderItem) {
            return 1;
        }
        if (mDataset.get(position) instanceof NoteItem) {
            return 2;
        }
        if (mDataset.get(position) instanceof ContactItem) {
            return 3;
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(BasicHolder holder, int position) {
        holder.bind(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class BasicHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public BasicHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.basic_name);
        }

        public void bind(BasicItem item) {
            name.setText(item.name);
        }
    }

    public class ContactHolder extends BasicHolder {
        public TextView birthday;
        public ImageView icon;
        public Switch alarmEnabled;

        public ContactHolder(View itemView) {
            super(itemView);
            birthday = (TextView) itemView.findViewById(R.id.contact_birthday);
            icon = (ImageView) itemView.findViewById(R.id.contact_icon);
            alarmEnabled = (Switch) itemView.findViewById(R.id.alarm_enabled);
        }

        public void bind(BasicItem item) {
            super.bind(item);
            ContactItem contactItem = (ContactItem) item;
            if (contactItem.birthday != null) {
                String month = (String) android.text.format.DateFormat.format("MMM", contactItem.birthday);
                String day = (String) android.text.format.DateFormat.format("dd", contactItem.birthday);
                Integer year = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", contactItem.birthday));
                Integer actualYear = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", new Date()));
                StringBuilder sb = new StringBuilder();
                sb.append(day);
                sb.append(".");
                sb.append(month);
                sb.append(" (");
                sb.append(actualYear - year);
                sb.append(" ");
                sb.append(mCtx.getResources().getString(R.string.years));
                sb.append(")");
                birthday.setText(sb.toString());

                Calendar birthdayCal = Calendar.getInstance();
                birthdayCal.setTime(contactItem.birthday);

                Calendar actualDay = Calendar.getInstance();

                if (birthdayCal.get(Calendar.DAY_OF_YEAR) < actualDay.get(Calendar.DAY_OF_YEAR)) {
                    name.setEnabled(false);
                    birthday.setEnabled(false);
                } else {
                    name.setEnabled(true);
                    birthday.setEnabled(true);
                }
            } else {
                birthday.setText("");
            }
            if (contactItem.icon != null) {
                Uri iconUri = Uri.parse(contactItem.icon);
                icon.setImageURI(iconUri);
            } else {
                icon.setImageResource(R.drawable.ic_person_black_36dp);
            }
            alarmEnabled.setChecked(contactItem.alarmEnabled);
            alarmEnabled.setTag(contactItem);
            alarmEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHelper dbH = new DatabaseHelper(mCtx);
                    Switch sw = (Switch) v;
                    ContactItem ci = (ContactItem) sw.getTag();
                    if (sw.isChecked()) {
                        dbH.enableDisableAlarm(ci.alarm_fk, sw.isChecked());
                        ci.alarmEnabled = sw.isChecked();
                        Log.d(TAG,"Checked contact " + ci.name);
                    } else {
                        dbH.enableDisableAlarm(ci.alarm_fk, sw.isChecked());
                        ci.alarmEnabled = sw.isChecked();
                        Log.d(TAG,"Checked contact " + ci.name);
                    }
                }
            });
        }
    }

    public class NoteHolder extends BasicHolder {
        public TextView description;

        public NoteHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.note_description);
        }

        public void bind(BasicItem item) {
            super.bind(item);
            NoteItem noteItem = (NoteItem) item;
            description.setText(noteItem.description);
        }
    }

    public class ReminderHolder extends BasicHolder {
        public TextView description, notificationTime;
        public Switch alarmEnabled;

        public ReminderHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.reminder_description);
            notificationTime = (TextView) itemView.findViewById(R.id.reminder_time);
            alarmEnabled = (Switch) itemView.findViewById(R.id.alarm_enabled);
        }

        public void bind(BasicItem item) {
            super.bind(item);
            ReminderItem reminderItem = (ReminderItem) item;
            description.setText(reminderItem.description);
            String date = DateFormat.getDateTimeInstance().format(reminderItem.notificationTime);
            notificationTime.setText(date);
            alarmEnabled.setChecked(reminderItem.alarmEnabled);
            alarmEnabled.setEnabled(reminderItem.alarmEnabled);
            description.setEnabled(reminderItem.alarmEnabled);
            notificationTime.setEnabled(reminderItem.alarmEnabled);
            alarmEnabled.setTag(reminderItem);
            alarmEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHelper dbH = new DatabaseHelper(mCtx);
                    Switch sw = (Switch) v;
                    ReminderItem ri = (ReminderItem) sw.getTag();
                    if (sw.isChecked()) {
                        dbH.enableDisableAlarm(ri.alarm_fk, sw.isChecked());
                        ri.alarmEnabled = sw.isChecked();
                        Log.d(TAG,"Checked reminder " + ri.name);
                    } else {
                        dbH.enableDisableAlarm(ri.alarm_fk, sw.isChecked());
                        ri.alarmEnabled = sw.isChecked();
                        Log.d(TAG,"Unchecked reminder " + ri.name);
                    }
                }
            });
        }
    }
}

package sk.filo.tomas.reminder;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContactsFragment extends Fragment {

    public static final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy.MM.dd"),
            new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yy.MM.dd"),
            new SimpleDateFormat("yy/MM/dd"),
            new SimpleDateFormat("MMM dd, yyyy")
    };
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

        final List<ContactItem> mDataset = new ArrayList<ContactItem>();

        Log.d(TAG, "Start: " + System.currentTimeMillis() + "ms");

        Cursor contact = getContactsBirthdays();

        if (contact.moveToFirst()) {
            do {
                String name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String icon = contact.getString(contact.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));
                String bDay = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                Date birthday = null;
                for (SimpleDateFormat f : birthdayFormats) {
                    try {
                        birthday = f.parse(bDay);
                        break;
                    } catch (ParseException e) {
                    }
                }
                mDataset.add(new ContactItem(name, icon, birthday));
            } while (contact.moveToNext());
        }

        Log.d(TAG, "End: " + System.currentTimeMillis() + "ms");

        mAdapter = new ContactItemAdapter(mDataset, getContext());
        mRecyclerView.setAdapter(mAdapter);

        return w;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Cursor getContactsBirthdays() {
        Uri uri = ContactsContract.Data.CONTENT_URI;

        String[] projection = new String[] {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Data.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Event.START_DATE
        };

        String where =  ContactsContract.Data.MIMETYPE + "= ? AND " +
                        ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        String[] selectionArgs = new String[] {
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
        };
        String sortOrder = null;
        return getActivity().getContentResolver().query(uri, projection, where, selectionArgs, sortOrder);
    }

}




package sk.filo.tomas.reminder.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.filo.tomas.reminder.MainActivity;
import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.adapter.ViewPagerAdapter;

/**
 * Created by tomas on 19.10.2016.
 */

public class MainFragment extends Fragment {

    private final static String TAG = "MainFragment";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_main, container, false);
        mViewPager = (ViewPager) w.findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) w.findViewById(R.id.tabs);
        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
        return w;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new RemindersFragment(), getString(R.string.reminders));
        adapter.addFragment(new NotesFragment(), getString(R.string.notes));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (sharedPreferences.getBoolean(MainActivity.USE_CONTACTS, true)) {
            adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        }
        viewPager.setAdapter(adapter);
    }


}

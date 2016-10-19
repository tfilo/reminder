package sk.filo.tomas.reminder.item;

import android.util.Log;

import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class ContactItem extends BasicItem {
    public String icon;
    public Date birthday;
    public Boolean alarmEnabled;

    public ContactItem(Long id, String name, String icon, Date birthday, Boolean alarmEnabled) {
        super(id, name);
        this.icon = icon;
        this.birthday = birthday;
        this.alarmEnabled = alarmEnabled;
    }

    public boolean contactChanged(ContactItem compare) {
        if (!(compare.birthday.compareTo(this.birthday) == 0)) {
            Log.d("contactChanged", "compare.birthday");
            Log.d("contactChanged", String.valueOf(compare.birthday.getTime()));
            Log.d("contactChanged", String.valueOf(this.birthday.getTime()));
            return true;
        }
        if (compare.icon != null && this.icon != null && !compare.icon.equals(this.icon)) {
            Log.d("contactChanged", "compare.icon");
            return true;
        }
        if (compare.icon == null && this.icon != null) {
            Log.d("contactChanged", "compare.icon");
            return true;
        }
        if (compare.icon != null && this.icon == null) {
            Log.d("contactChanged", "compare.icon");
            return true;
        }
        if (!compare.name.equals(this.name)) {
            Log.d("contactChanged", "compare.name");
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " ContactItem{" +
                "icon='" + icon + '\'' +
                ", birthday=" + birthday +
                ", alarmEnabled=" + alarmEnabled +
                '}';
    }
}

package sk.filo.tomas.reminder.item;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class ContactItem extends BasicItem {
    public String icon;
    public Date birthday;
    public Date alarmTime;
    public Boolean alarmEnabled;
    public Integer lastExecuted;
    public Boolean hasYear;

    public ContactItem(Long id, Long alarmFk, String name, String icon, Date birthday, Date alarmTime, Boolean alarmEnabled, Integer lastExecuted, Boolean hasYear) {
        super(id, alarmFk, name);
        this.icon = icon;
        this.birthday = birthday;
        this.alarmTime = alarmTime;
        this.alarmEnabled = alarmEnabled;
        this.lastExecuted = lastExecuted;
        this.hasYear = hasYear;
    }

    public boolean executedThisYear() {
        Calendar cal = Calendar.getInstance();
        Integer year = cal.get(Calendar.YEAR);
        if (lastExecuted!=null && lastExecuted >= year) {
            return true;
        }
        return false;
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
        if (!compare.hasYear.equals(this.hasYear)) {
            Log.d("contactChanged", "compare.hasYear");
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " ContactItem{" +
                "icon='" + icon + '\'' +
                ", birthday=" + birthday +
                ", alarmTime=" + alarmTime +
                ", alarmEnabled=" + alarmEnabled +
                ", lastExecuted=" + lastExecuted +
                ", hasYear=" + hasYear +
                '}';
    }
}

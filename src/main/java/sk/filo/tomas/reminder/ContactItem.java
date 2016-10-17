package sk.filo.tomas.reminder;

import android.net.Uri;

import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class ContactItem {
    public String name;
    public String icon;
    public Date birthday;

    public ContactItem(String name, String icon, Date birthday) {
        this.name = name;
        this.icon = icon;
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "ContactItem{" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}

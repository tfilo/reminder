package sk.filo.tomas.reminder;

import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class ReminderItem {
    public String name;
    public String description;
    public Date notificationTime;

    public ReminderItem(String name, String description, Date notificationTime) {
        this.name = name;
        this.description = description;
        this.notificationTime = notificationTime;
    }

    @Override
    public String toString() {
        return "ReminderItem{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", notificationTime=" + notificationTime +
                '}';
    }
}

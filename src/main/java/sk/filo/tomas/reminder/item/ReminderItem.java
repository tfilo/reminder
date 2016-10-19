package sk.filo.tomas.reminder.item;

import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class ReminderItem extends BasicItem {
    public String description;
    public Date notificationTime;
    public Boolean alarmEnabled;

    public ReminderItem(Long id, String name, String description, Date notificationTime, Boolean alarmEnabled) {
        super(id, name);
        this.description = description;
        this.notificationTime = notificationTime;
        this.alarmEnabled = alarmEnabled;
    }

    @Override
    public String toString() {
        return super.toString() + " ReminderItem{" +
                "description='" + description + '\'' +
                ", notificationTime=" + notificationTime +
                ", alarmEnabled=" + alarmEnabled +
                '}';
    }
}

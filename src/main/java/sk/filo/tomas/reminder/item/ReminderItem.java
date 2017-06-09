package sk.filo.tomas.reminder.item;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class ReminderItem extends BasicItem {
    public String description;
    public Date notificationTime;
    public Boolean alarmEnabled;
    public Date lastExecuted;

    public ReminderItem(Long id, Long alarmFk, String name, String description, Date notificationTime, Boolean alarmEnabled, Date lastExecuted) {
        super(id, alarmFk, name);
        this.description = description;
        this.notificationTime = notificationTime;
        this.alarmEnabled = alarmEnabled;
        this.lastExecuted = lastExecuted;
    }

    public boolean executedThisYear() {
        if (lastExecuted!=null && lastExecuted.getTime() >= notificationTime.getTime()) {
            return true;
        }
        return false;
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

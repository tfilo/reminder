package sk.filo.tomas.reminder.item;

import java.util.Date;
import java.util.List;

/**
 * Created by tomas on 27.10.2016.
 */

public class AlarmItem {

    public Long id;
    public Date alarmTime;
    public Date lastExecuted;

    public AlarmItem(Long id, Date alarmTime, Date lastExecuted) {
        this.id = id;
        this.alarmTime = alarmTime;
        this.lastExecuted = lastExecuted;
    }

    @Override
    public String toString() {
        return "AlarmItem{" +
                "id=" + id +
                ", alarmTime=" + alarmTime +
                ", lastExecuted=" + lastExecuted +
                '}';
    }
}

package sk.filo.tomas.reminder.item;

import java.util.Date;

/**
 * Created by tomas on 27.10.2016.
 */

public class AlarmExtendedItem extends AlarmItem {
    public String name;
    public String description;
    public Type type;
    public Long parentId;

    public enum Type {
        REMINDER,
        CONTACT;
    }

    public AlarmExtendedItem(AlarmItem ai, Long parentId, String name, String description, Type type) {
        super(ai.id, ai.alarmTime, ai.lastExecuted);
        this.name = name;
        this.description = description;
        this.type = type;
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "AlarmExtendedItem{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", parentId=" + parentId +
                '}';
    }
}

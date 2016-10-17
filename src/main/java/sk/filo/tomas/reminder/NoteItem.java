package sk.filo.tomas.reminder;

import java.util.Date;

/**
 * Created by tomas on 17.10.2016.
 */

public class NoteItem {
    public String name;
    public String description;

    public NoteItem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "ReminderItem{" +
                "name='" + name + '\'' +
                ", description='" + description +
                '}';
    }
}

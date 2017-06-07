package sk.filo.tomas.reminder.item;

/**
 * Created by tomas on 18.10.2016.
 */

public class BasicItem {
    public String name;
    public Long id;
    public Long alarm_fk;

    public BasicItem(Long id, Long alarm_fk, String name) {
        this.id = id;
        this.name = name;
        this.alarm_fk = alarm_fk;
    }

    @Override
    public String toString() {
        return "BasicItem{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", alarm_fk=" + alarm_fk +
                '}';
    }
}

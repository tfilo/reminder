package sk.filo.tomas.reminder.item;

/**
 * Created by tomas on 18.10.2016.
 */

public class BasicItem {
    public String name;
    public Long id;

    public BasicItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "BasicItem{" +
                "id='" + id + '\'' +
                "name='" + name + '\'' +
                '}';
    }
}

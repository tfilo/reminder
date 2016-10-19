package sk.filo.tomas.reminder.item;

/**
 * Created by tomas on 17.10.2016.
 */

public class NoteItem extends BasicItem {
    public String description;

    public NoteItem(Long id, String name, String description) {
        super(id, name);
        this.description = description;
    }

    @Override
    public String toString() {
        return super.toString() + " NoteItem{" +
                "description='" + description + '\'' +
                '}';
    }
}

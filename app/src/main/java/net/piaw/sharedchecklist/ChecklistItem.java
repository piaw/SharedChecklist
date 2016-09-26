package net.piaw.sharedchecklist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by piaw on 9/22/2016.
 */

public class ChecklistItem implements Serializable {
    private static final long serialVersionUID = -6661982485168319635L;

    private boolean checked;
    private String label;
    private Long dateModified;
    private Long dateCreated;
    private String creator;
    private Long deadline;

    ChecklistItem() {
        checked = false;
        label = "";
        dateModified = System.currentTimeMillis();
        dateCreated = System.currentTimeMillis();
        creator = "";
        deadline = 0L;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getDateModified() {
        return dateModified;
    }

    public void setDateModified(Long dateModified) {
        this.dateModified = dateModified;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getDeadline() {
        return deadline;
    }

    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }

    private void readObject(ObjectInputStream inputStream)
            throws ClassNotFoundException, IOException {
        inputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();
    }
}

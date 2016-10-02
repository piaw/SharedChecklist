package net.piaw.sharedchecklist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piaw on 9/21/2016.
 */

public class Checklist implements Serializable {

    private static final long serialVersionUID = -6931513467134399532L;
    private String id; // key for Checklist in DB
    private String checklist_name; // user-defined name for checklist --- unused for now
    private String creator; // user email
    private String owner; // user email
    private ArrayList<ChecklistItem> items;
    private ArrayList<String> acl;

    Checklist() {
        id = "";
        creator = "";
        owner = "";
        checklist_name = "default";
        setItems(new ArrayList<ChecklistItem>());
        setAcl(new ArrayList<String>());
    }

    public Checklist DeepCopy() {
        Checklist copy = new Checklist();
        copy.id = this.id;
        copy.creator = this.creator;
        copy.owner = this.owner;
        copy.checklist_name = "Copy of " + this.checklist_name;
        for (int i = 0; i < items.size(); ++i) {
            ChecklistItem item_copy = items.get(i).DeepCopy();
            copy.items.add(item_copy);
        }
        for (int i = 0; i < acl.size(); ++i) {
            copy.acl.add(acl.get(i));
        }
        return copy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<ChecklistItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ChecklistItem> items) {
        this.items = items;
    }

    public void addItem(ChecklistItem item) {
        items.add(item);
    }

    public ArrayList<String> getAcl() {
        return acl;
    }

    public void setAcl(ArrayList<String> acl) {
        this.acl = acl;
    }

    public void addAcl(String emailId) {
        acl.add(emailId);
    }

    private void readObject(ObjectInputStream inputStream)
            throws ClassNotFoundException, IOException {
        inputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();

    }

    public String getChecklist_name() {
        return checklist_name;
    }

    public void setChecklist_name(String checklist_name) {
        this.checklist_name = checklist_name;
    }
}

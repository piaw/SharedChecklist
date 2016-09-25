package net.piaw.sharedchecklist;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piaw on 9/21/2016.
 */

public class Checklist implements Serializable {

    private String creator; // user email
    private String owner; // user email
    private ArrayList<ChecklistItem> items;
    private ArrayList<String> acl;

    Checklist() {
        creator = "";
        owner = "";
        setItems(new ArrayList<ChecklistItem>());
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
}

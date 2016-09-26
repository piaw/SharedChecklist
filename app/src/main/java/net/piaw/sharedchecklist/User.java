package net.piaw.sharedchecklist;

import java.util.ArrayList;

/**
 * Created by piaw on 9/23/2016.
 */

public class User {
    private String email;
    private ArrayList<String> checklists;
    private String default_checklist;

    public User() {
        email = "";
        checklists = new ArrayList<String>();
        default_checklist = "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getChecklists() {
        return checklists;
    }

    public void setChecklists(ArrayList<String> checklists) {
        this.checklists = checklists;
    }

    public void addChecklist(String checklist) {
        checklists.add(checklist);
    }

    public String getDefault_checklist() {
        return default_checklist;
    }

    public void setDefault_checklist(String default_checklist) {
        this.default_checklist = default_checklist;
    }
}

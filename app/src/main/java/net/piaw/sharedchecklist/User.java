package net.piaw.sharedchecklist;

import java.util.ArrayList;

/**
 * Created by piaw on 9/23/2016.
 */

public class User {
    private String email;
    private ArrayList<String> checklists;
    private ArrayList<String> pending_checklists;
    private String default_checklist;
    private boolean isPro;

    public User() {
        email = "";
        checklists = new ArrayList<>();
        pending_checklists = new ArrayList<>();
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

    public ArrayList<String> getPending_checklists() {
        return pending_checklists;
    }

    public void setPending_checklists(ArrayList<String> pending_checklists) {
        this.pending_checklists = pending_checklists;
    }

    public boolean isPro() {
        return isPro;
    }

    public void setPro(boolean pro) {
        isPro = pro;
    }
}

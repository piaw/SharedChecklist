package net.piaw.sharedchecklist;

import java.util.List;

/**
 * Created by piaw on 9/23/2016.
 */

public class User {
    private String email;
    private List<String> checklists;
    private String default_checklist;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getChecklists() {
        return checklists;
    }

    public void setChecklists(List<String> checklists) {
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

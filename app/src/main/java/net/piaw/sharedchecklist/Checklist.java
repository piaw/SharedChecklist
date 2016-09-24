package net.piaw.sharedchecklist;

/**
 * Created by piaw on 9/21/2016.
 */

public class Checklist {
    private boolean redirect;
    private String redirect_key;  // user/checklist #
    private String creator; // user email
    private String owner; // user email

    Checklist() {
        redirect = false;
        redirect_key = "";
        creator = "";
        owner = "";
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public String getRedirect_key() {
        return redirect_key;
    }

    public void setRedirect_key(String redirect_key) {
        this.redirect_key = redirect_key;
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
}

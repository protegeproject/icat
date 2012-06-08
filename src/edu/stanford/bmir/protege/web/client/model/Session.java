package edu.stanford.bmir.protege.web.client.model;


public class Session {
    private String userName;

    public Session() {
        this(null);
    }

    public Session(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        String oldName = this.userName;
        this.userName = userName;
        SystemEventManager.getSystemEventManager().notifyLoginChanged(oldName, this.userName);
    }

}

package edu.stanford.bmir.protege.web.client.model;

/**
 * Class that holds all the global settings of the client, such as the global
 * session.
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 * 
 */
public class GlobalSettings {

    private static GlobalSettings globalSettingsInstance;

    private Session globalSession = new Session(null);

    private GlobalSettings() {
    }

    public static GlobalSettings getGlobalSettings() {
        if (globalSettingsInstance == null) {
            globalSettingsInstance = new GlobalSettings();
        }
        return globalSettingsInstance;
    }

    public Session getGlobalSession() {
        return globalSession;
    }

    public String getUserName() {
        return globalSession == null ? null : globalSession.getUserName();
    }

    public void setUserName(String newUser) {
        if (globalSession == null) {
            globalSession = new Session(newUser);
        }
        globalSession.setUserName(newUser);
    }

    public boolean isLoggedIn() {
        return getUserName() != null;
    }
}

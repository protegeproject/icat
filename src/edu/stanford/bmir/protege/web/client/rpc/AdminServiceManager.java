package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class AdminServiceManager {

    private static AdminServiceAsync proxy;
    private static AdminServiceManager instance;

    private AdminServiceManager() {
        proxy = (AdminServiceAsync) GWT.create(AdminService.class);
    }

    public static AdminServiceManager getInstance() {
        if (instance == null) {
            instance = new AdminServiceManager();
        }
        return instance;
    }

    public void validateUser(String name, String password, AsyncCallback<UserData> cb) {
        proxy.validateUser(name, password, cb);
    }

    public void registerUser(String name, String password, AsyncCallback<UserData> cb) {
        proxy.registerUser(name, password, cb);
    }

    public void getUserEmail(String userName, AsyncCallback<String> callback) {
        proxy.getUserEmail(userName, callback);
    }

    public void setUserEmail(String userName, String email, AsyncCallback<Void> callback) {
        proxy.setUserEmail(userName, email, callback);
    }

    public void getProjects(String user, AsyncCallback<Collection<ProjectData>> cb) {
        proxy.getProjects(user, cb);
    }

    public void getAllowedOperations(String project, String user, AsyncCallback<Collection<String>> cb) {
        proxy.getAllowedOperations(project, user, cb);
    }

    public void getAllowedServerOperations(String userName,  AsyncCallback<Collection<String>> cb) {
        proxy.getAllowedServerOperations(userName, cb);
    }

    public void changePassword(String name, String password, AsyncCallback<Void> cb) {
        proxy.changePassword(name, password, cb);
    }

    public void refreshMetaproject(AsyncCallback<Void> cb) {
        proxy.refreshMetaproject(cb);
    }
    public void sendPasswordReminder(String userName, AsyncCallback<Void> cb) {
        proxy.sendPasswordReminder(userName, cb);
    }

    public void getCurrentUserInSession(AsyncCallback<String> cb) {
        proxy.getCurrentUserInSession(cb);
    }

    public void logout(AsyncCallback<Void> cb) {
        proxy.logout(cb);
    }
}

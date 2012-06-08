package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public interface AdminServiceAsync {

    void validateUser(String name, String password, AsyncCallback<UserData> cb);

    void registerUser(String name, String password, AsyncCallback<UserData> cb);

    void changePassword(String userName, String password, AsyncCallback<Void> callback);

    void getUserEmail(String userName, AsyncCallback<String> callback);

    void setUserEmail(String userName, String email, AsyncCallback<Void> callback);

    void sendPasswordReminder(String userName, AsyncCallback<Void> callback);

    void getProjects(String user, AsyncCallback<Collection<ProjectData>> cb);

    void getAllowedOperations(String project, String user, AsyncCallback<Collection<String>> cb);

    void getAllowedServerOperations(String userName, AsyncCallback<Collection<String>> callback);

    void refreshMetaproject(AsyncCallback<Void> cb);

    void getCurrentUserInSession(AsyncCallback<String> async);

    void logout(AsyncCallback<Void> async);
}

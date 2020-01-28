package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.User;

public interface MetaProjectManager {

    public boolean hasValidCredentials(String userName, String password);

    public UserData registerUser(String userName, String password);

    public void changePassword(String userName, String password);
    
    public List<UserData> getUsers(String userName);

    public String getUserEmail(String userName);

    public void setUserEmail(String userName, String email);

    public ArrayList<ProjectData> getProjectsData(String userName);

    public Collection<Operation> getDefinedOperations();

    public Collection<Operation> getAllowedOperations(String project, String userName);

    public Collection<Operation> getAllowedServerOperations(String userName);
    
    public boolean isServerOperationAllowed(String userName, String operation);

    public Project openProject(String name);

    public User getUser(String userNameOrEmail);

    public MetaProject getMetaProject();

    /**
     * Reloads the metaproject. Should be used with care, because it reloads
     * besides the user/password info, also the projects info. Clients should be
     * notified of the change.
     */
    public void reloadMetaProject();

    public void dispose();

    public UserData getUserAssociatedWithOpenId(String userOpenId);

    public boolean allowsCreateUser();

}
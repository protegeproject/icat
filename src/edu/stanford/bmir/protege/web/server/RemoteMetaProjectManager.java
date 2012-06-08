package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.ui.openid.constants.OpenIdConstants;
import edu.stanford.smi.protege.event.ServerProjectAdapter;
import edu.stanford.smi.protege.event.ServerProjectListener;
import edu.stanford.smi.protege.event.ServerProjectSessionClosedEvent;
import edu.stanford.smi.protege.event.ServerProjectStatusChangeEvent;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.*;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.*;
import edu.stanford.smi.protege.server.util.ProjectInfo;
import edu.stanford.smi.protege.util.Log;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteMetaProjectManager extends AbstractMetaProjectManager {
    private static Logger log = Log.getLogger(RemoteMetaProjectManager.class);

    private RemoteServer server;
    private MetaProject metaproject;
    private ServerProjectListener _shutdownListener;

    public RemoteServer getServer() {
        if (server == null) {
            String protegeServerName = ApplicationProperties.getProtegeServerHostName();
            try {
                server = (RemoteServer) Naming.lookup("//" + protegeServerName + "/" + Server.getBoundName());
            } catch (Exception e) {
                if (Log.getLogger().isLoggable(Level.FINE)) {
                    Log.getLogger().log(Level.FINE, "Could not connect to server: " + protegeServerName, e);
                }
            }
            if (server == null) {
                Log.getLogger().severe("Could not connect to server: " + protegeServerName);
            }
        }
        return server;
    }

    public Project openProject(String projectName) {
        Project project = null;
        Log.getLogger().info(
                "Loading project " + projectName + " from Protege server "
                        + ApplicationProperties.getProtegeServerHostName());

        try {
            project = RemoteProjectManager.getInstance().getProject(ApplicationProperties.getProtegeServerHostName(),
                    ApplicationProperties.getProtegeServerUser(), ApplicationProperties.getProtegeServerPassword(),
                    projectName, true);
            if (project != null) {
                project.getKnowledgeBase().addServerProjectListener(
                        _shutdownListener = createRemoteProjectShutdownListener());
            }

        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "There were exceptions at loading project " + projectName, e);
            throw new RuntimeException("Cannot open project " + projectName, e);
        }

        return project;
    }

    public ArrayList<ProjectData> getProjectsData(String user) {
        //TODO: How to handle this?
        if (user == null) {
            user = "Guest";
        }

        ArrayList<ProjectData> projectData = new ArrayList<ProjectData>();
        RemoteServer server = getServer();
        if (server == null) {
            Log.getLogger().warning("Could not get remote projects. Reason: Cannot connect to remote server.");
            throw new RuntimeException(
                    "Could not retrieve the remote projects from the Protege server. Reason: Cannot connect to remote Protege server.");
        }
        Collection<ProjectInfo> prjInfos = new ArrayList<ProjectInfo>();
        try {
            prjInfos = server.getAvailableProjectInfo(new Session(user, null, false));
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Could not retrieve available projects from the Protege server.", e);
            throw new RuntimeException("Could not retrieve available projects from the Protege server.");
        }
        for (ProjectInfo prjInfo : prjInfos) {
            ProjectData pd = new ProjectData();
            pd.setName(prjInfo.getName());
            pd.setDescription(prjInfo.getDescription());
            pd.setOwner(prjInfo.getOwner());
            projectData.add(pd);
        }
        return projectData;
    }

    public void reloadMetaProject() {

    }

    public UserData registerUser(String name, String password) {
        boolean success = false;
        UserData data = null;

        RemoteServer server = getServer();
        if (server == null) {
            Log.getLogger().warning("Could not get remote projects. Reason: Cannot connect to remote server.");
            throw new RuntimeException(
                    "Could not retrieve the remote projects from the Protege server. Reason: Cannot connect to remote Protege server.");
        }

        try {
            success = server.createUser(name, password);
            if (success) {
                data = new UserData(name, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create user " + name + ". Reason: " + e.getMessage(), e);
        }
        return data;
    }

    public Collection<Operation> getAllowedOperations(String projectName, String userName) {
        Collection<Operation> allowedOps = new ArrayList<Operation>();
        Policy policy = getMetaProject().getPolicy();
        User user = policy.getUserByName(userName);
        ProjectInstance project = getMetaProject().getPolicy().getProjectInstanceByName(projectName);
        if (user == null || project == null) {
            return allowedOps;
        }
        for (Operation op : policy.getKnownOperations()) {
            if (policy.isOperationAuthorized(user, op, project)) {
                allowedOps.add(op);
            }
        }
        return allowedOps;
    }

    public Collection<Operation> getAllowedServerOperations(String userName) {
        Collection<Operation> allowedOps = new ArrayList<Operation>();
        if (userName == null) {
            return allowedOps;
        }
        Policy policy = getMetaProject().getPolicy();
        User user = policy.getUserByName(userName);
        ServerInstance firstServerInstance = getMetaProject().getPolicy().getFirstServerInstance();
        if (user == null || firstServerInstance == null) {
            return allowedOps;
        }
        for (Operation op : policy.getKnownOperations()) {
            if (policy.isOperationAuthorized(user, op, firstServerInstance)) {
                allowedOps.add(op);
            }
        }
        return allowedOps;
    }

    public Session getSession(String projectName, boolean create) {
        Project prj = Protege3ProjectManager.getProjectManager().getProject(projectName);
        if (prj == null && !create) {
            return null;
        }
        KnowledgeBase kb = prj.getKnowledgeBase();
        return (Session) RemoteClientFrameStore.getCurrentSession(kb);
    }


    protected ServerProjectListener createRemoteProjectShutdownListener() {
        return new ServerProjectAdapter() {
            @Override
            public void projectStatusChanged(ServerProjectStatusChangeEvent event) {
                ProjectStatus newStatus = event.getNewStatus();
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Project status changed: " + newStatus);
                }
                if (newStatus.equals(ProjectStatus.CLOSED_FOR_MAINTENANCE)) {
                    log.info("Project " + event.getProjectName()
                            + " has been closed for maintainance on the Protege server and it is unavailable.");
                    closeProjectRemote(event.getProjectName());
                }
            }

            @Override
            public void beforeProjectSessionClosed(ServerProjectSessionClosedEvent event) { //TODO: check implementation
                String projectName = event.getProjectName();
                ServerProject<Project> serverProject = Protege3ProjectManager.getProjectManager().getOpenedServerProject(projectName);
                if (serverProject != null) {
                    if (RemoteClientFrameStore.getCurrentSession(serverProject.getProject().getKnowledgeBase()).equals(
                            event.getSessionToKill())) {
                        log.info("Session for project " + event.getProjectName()
                                + " has been killed. This project is unavailable.");
                        closeProjectRemote(event.getProjectName());
                    }
                }
            }
        };
    }

    protected void closeProjectRemote(String projectName) {
        ServerProject<Project> serverProject = Protege3ProjectManager.getProjectManager().removeServerProject(projectName);
        if (serverProject != null) {
            serverProject.getProject().getKnowledgeBase().removeServerProjectListener(_shutdownListener);
            serverProject.dispose();
        }
    }

    public MetaProject getMetaProject() {
        if (metaproject == null) {
            //this is kind of cheating
            RemoteSession session = new Session(ApplicationProperties.getProtegeServerUser(), null, false);
            RemoteServer server = getServer();
            if (server == null) {
                return null;
            }
            try {
                metaproject = RemoteProjectManager.getInstance().connectToMetaProject(server, session);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error at getting metaproject. Message: " + e.getMessage(), e);
            }
        }
        return metaproject;
    }

    public Date getLastLogin(String name) {
        Date date = null;
        MetaProject metaproject = getMetaProject();
        User user = metaproject.getUser(name);
        if (user != null) {
            date = user.getLastLogin();
        }
        return date;
    }

    public void dispose() {
        if (metaproject != null) {
            metaproject.dispose();
        }
    }

    public UserData getUserAssociatedWithOpenId(String userOpenId) {
        UserData uData = null;
        if (userOpenId != null) {
            uData = new UserData();
            Set<User> users = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUsers();

            boolean gotUser = false;
            for (Object element : users) {
                if (gotUser) {
                    break;
                }
                User user = (User) element;
                Collection<PropertyValue> propColl = user.getPropertyValues();
                for (Object element2 : propColl) {
                    PropertyValue propertyValue = (PropertyValue) element2;
                    if (propertyValue.getPropertyName().startsWith(OpenIdConstants.OPENID_PROPERTY_PREFIX)
                            && propertyValue.getPropertyName().endsWith(OpenIdConstants.OPENID_PROPERTY_URL_SUFFIX)) {
                        if (propertyValue.getPropertyValue().trim().equalsIgnoreCase(userOpenId)) {
                            uData.setName(user.getName());
                            gotUser = true;
                            break;
                        }
                    }
                }
            }
        }
        return uData;
    }

    public String getUserSalt(String userName) {
        User user = getMetaProject().getUser(userName);
        if (user == null) {
            return null;
        }
        return user.getSalt();
    }

}

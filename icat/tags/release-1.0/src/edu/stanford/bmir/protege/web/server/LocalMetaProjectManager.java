package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;

public class LocalMetaProjectManager extends AbstractMetaProjectManager {

    private MetaProject metaproject;
    boolean runsInClientServerMode;

    //cache for performance reasons
    private ArrayList<ProjectData> projectData;

    private int _saveIntervalMsec = edu.stanford.bmir.protege.web.server.ApplicationProperties.NO_SAVE;
    //thread that save periodically the projects for local mode
    private Thread _updateThread;

    public LocalMetaProjectManager() {
        metaproject = new MetaProjectImpl(ApplicationProperties.getLocalMetaprojectURI());
        //automatic save logic
        int saveInt = ApplicationProperties.getLocalProjectSaveInterval();
        if (saveInt != ApplicationProperties.NO_SAVE) {
            _saveIntervalMsec = saveInt * 1000;
        }
        startProjectUpdateThread();
    }

    public MetaProject getMetaProject() {
        return metaproject;
    }

    public Project openProject(String projectName) {
        Project project = null;
        URI uri = getProjectURI(projectName);
        Log.getLogger().info("Loading project " + projectName + " from " + uri);

        Collection errors = new ArrayList();
        try {
            project = Project.loadProjectFromURI(uri, errors);
            if (errors.size() > 0) {
                Log.getLogger().warning("There were errors at loading project " + projectName);
                Log.handleErrors(Log.getLogger(), Level.WARNING, errors);
            }
        } catch (Throwable e) {
            Log.getLogger().log(Level.WARNING, "There were exceptions at loading project " + projectName, e);
            throw new RuntimeException("Cannot open project " + projectName, e);
        }
        return project;
    }

    public UserData registerUser(String userName, String password) {
        if (!ServerProperties.getAllowsCreateUsers()) {
            throw new RuntimeException(
            "The server does not allow the creation of new users. Please contact the administartor to create a new user account.");
        }
        User user = metaproject.getUser(userName);
        if (user != null) {
            throw new RuntimeException("Username: " + userName + " is already taken. Please choose another user name.");
        }
        user = metaproject.createUser(userName, password);
        return AuthenticationUtil.createUserData(user.getName());
    }

    public ArrayList<ProjectData> getProjectsData(String userName) {
        if (projectData != null) {
            return projectData;
        }

        if (userName == null) {
            userName = "Guest";
        }
        //TODO: check with Tim if it needs synchronization
        projectData = new ArrayList<ProjectData>();

        Policy policy = metaproject.getPolicy();
        User user = policy.getUserByName(userName);

        for (ProjectInstance projectInstance : metaproject.getProjects()) {
            if (user != null
                    && (!policy.isOperationAuthorized(user, MetaProjectConstants.OPERATION_DISPLAY_IN_PROJECT_LIST,
                            projectInstance) || !policy.isOperationAuthorized(user,
                                    MetaProjectConstants.OPERATION_READ, projectInstance))) {
                continue;
            }

            try {
                ProjectData pd = new ProjectData();

                // JV: I think there is a problem with the getDescription method
                // in protege-core, i.e., if the metaproject has a null value for
                // the description, the getDescription method throws an exception.
                // Why is this value required in the metaproject?
                pd.setDescription(projectInstance.getDescription());

                pd.setLocation(projectInstance.getLocation());
                pd.setName(projectInstance.getName());

                User owner = projectInstance.getOwner();
                if (owner != null) {
                    pd.setOwner(owner.getName());
                }

                Log.getLogger().info("Found project def in metaproject: " + pd.getName() + " at: " + pd.getLocation());
                projectData.add(pd);

            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING,
                        "Found project def with problems: " + projectInstance + " Message: " + e.getMessage(), e);
            }
        }

        Collections.sort(projectData, new ProjectsDataComparator());

        return projectData;
    }

    /*
     * Path methods
     */
    public URI getProjectURI(String projectName) {
        for (ProjectInstance projectInstance : metaproject.getProjects()) {
            String name = projectInstance.getName();
            if (name.equals(projectName)) {
                String path = projectInstance.getLocation();
                URL url = URIUtilities.toURL(path, ApplicationProperties.getWeprotegeDirectory());
                URI uri = null;
                try {
                    uri = url.toURI();
                } catch (URISyntaxException e) {
                    Log.getLogger().log(Level.SEVERE,
                            "Error at getting path for project " + projectName + ". Computed path: " + url, e);
                }
                return uri;
            }
        }
        return null;
    }

    public void init(boolean loadOntologiesFromServer) {
        runsInClientServerMode = loadOntologiesFromServer;
    }

    /* (non-Javadoc)
     * @see edu.stanford.bmir.protege.web.server.MetaProjectManager#reloadMetaProject()
     */
    public void reloadMetaProject() {
        if (metaproject != null) {
            ((MetaProjectImpl) metaproject).getKnowledgeBase().getProject().dispose();
        }
        metaproject = new MetaProjectImpl(ApplicationProperties.getLocalMetaprojectURI());
    }


    /*
     * Saving automatically the projects - to be removed when integrated with server	 *
     */
    private void startProjectUpdateThread() {
        if (_saveIntervalMsec != edu.stanford.bmir.protege.web.server.ApplicationProperties.NO_SAVE) {
            _updateThread = new Thread("Save Projects") {
                @Override
                public void run() {
                    try {
                        while (true) {
                            synchronized (LocalMetaProjectManager.this) {
                                LocalMetaProjectManager.this.wait(_saveIntervalMsec);
                            }
                            if (_updateThread != this) {
                                break;
                            }
                            saveAllProjects();
                        }
                    } catch (Throwable e) {
                        Log.getLogger().log(Level.INFO, "Exception caught", e);
                    }
                }
            };
            _updateThread.setDaemon(true);
            _updateThread.start();
        }
    }

    //just for the local loading of ontologies
    private void saveAllProjects() {
        try {
            for (ServerProject<Project> serverProject : Protege3ProjectManager.getProjectManager().getOpenServerProjects()) {
                Project prj = serverProject.getProject();
                if (prj != null) {
                    save(prj);
                    prj.getKnowledgeBase().setChanged(false);

                    KnowledgeBase changesKb = ChAOUtil.getChangesKb(prj.getKnowledgeBase());
                    if (changesKb != null && changesKb.hasChanged()) {
                        save(changesKb.getProject());
                        changesKb.setChanged(false);
                    }
                }
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Errors at saving server projects", e);
        }
    }

    //just for the local loading of ontologies
    private static void save(Project project) {
        Log.getLogger().info("Saving " + project);
        Collection errors = new ArrayList();
        /*
         * The order of these synchronize statements is critical.  There is some
         * OWLFrameStore code (which holds the knowledgebase lock) that makes calls
         * to the internal project knowledge base to get configuration parameters.
         */
        synchronized (project.getKnowledgeBase()) {
            synchronized (project.getInternalProjectKnowledgeBase()) {
                /* TT: Save only the domain kb, not the prj kb.
                 * Saving the prj kb while a client opens a
                 * remote project can corrupt the client prj kb.
                 */
                KnowledgeBase kb = project.getKnowledgeBase();
                KnowledgeBaseFactory factory = kb.getKnowledgeBaseFactory();
                if (!(factory instanceof DatabaseKnowledgeBaseFactory)) {
                    factory.saveKnowledgeBase(kb, project.getSources(), errors);
                }
            }
        }
        dumpErrors(project, errors);
    }

    private static void dumpErrors(Project p, Collection errors) {
        if (!errors.isEmpty()) {
            Log.getLogger().warning("Unable to save project " + p);
            Iterator i = errors.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                Log.getLogger().warning("\t" + o.toString());
            }
        }
    }

    public void dispose() {
        metaproject.dispose();
        _updateThread = null;
        synchronized (this) {
            notifyAll();
        }
    }

    /*
     * Helper class
     */
    class ProjectsDataComparator implements Comparator<ProjectData> {
        public int compare(ProjectData prj1, ProjectData prj2) {
            return prj1.getName().compareTo(prj2.getName());
        }
    }

    public boolean allowsCreateUser() {
        return ServerProperties.getAllowsCreateUsers();
    }
}

package edu.stanford.bmir.protege.web.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.stanford.smi.protege.util.Log;

public class AbstractProjectManager<P> {

    static private MetaProjectManager metaProjectManager;

    protected Map<String, ServerProject<P>> openProjectsMap = new HashMap<String, ServerProject<P>>();

    protected AbstractProjectManager() {
        initMetaProjectManager();
    }

    protected void initMetaProjectManager() {
        if (!ApplicationProperties.getLoadOntologiesFromServer()) {
            metaProjectManager = new LocalMetaProjectManager();
            Log.getLogger().info("WebProtege server running with local projects");
        } else { //load ontologies from server //TODO: check if this makes sense for OWL-API config
            metaProjectManager = new RemoteMetaProjectManager();
            Log.getLogger().info("WebProtege server running with remote projects loaded" +
                    " from the Protege server: " + ApplicationProperties.getProtegeServerHostName());
        }
    }

    public ServerProject<P> getServerProject(String projectName, boolean create) {
        ServerProject<P> serverProject;
        synchronized (openProjectsMap) {
            serverProject = openProjectsMap.get(projectName);
            if (serverProject == null && create) {
                serverProject = new ServerProject<P>();
                openProjectsMap.put(projectName, serverProject);
            }
        }
        if (create) {
            ensureProjectOpen(projectName, serverProject);
        }
        return serverProject;
    }

    //Subclasses should implement
    protected void ensureProjectOpen(String projectName, ServerProject<P> serverProject) {

    }

    synchronized Collection<ServerProject<P>> getOpenServerProjects() {
        return new HashSet<ServerProject<P>>(openProjectsMap.values());
    }

    synchronized ServerProject<P> getOpenedServerProject(String name) {
        return openProjectsMap.get(name);
    }

    ServerProject<P> removeServerProject(String name) {
        ServerProject<P> serverProject = null;
        synchronized (openProjectsMap) {
            serverProject = openProjectsMap.remove(name);
        }
        return serverProject;
    }


    public MetaProjectManager getMetaProjectManager() {
        return metaProjectManager;
    }

    public P getProject(String projectName) {
        return getProject(projectName, true);
    }

    private P getProject(String projectName, boolean create) {
        ServerProject<P> serverProject = getServerProject(projectName);
        return (serverProject == null && create) ? null : serverProject.getProject();
    }

    public ServerProject<P> getServerProject(String projectName) {
        return getServerProject(projectName, true);
    }


    public void dispose() {
        synchronized (openProjectsMap) {
            for (ServerProject<P> sproject : openProjectsMap.values()) {
                sproject.dispose();
            }
            openProjectsMap.clear();
        }
    }

    public boolean isSuitable(String prjName) {
        return false; //TODO
    }

}

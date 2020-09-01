package edu.stanford.bmir.protege.web.client.model;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.model.listener.OntologyListener;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.ui.LayoutManager;

/**
 * A project on the client side. A project has information about:
 * <ul>
 * <li>project data (owner, description, etc.)</li>
 * <li>ontology event manager</li>
 * <li>project configuration (layout, portlets configuration, etc.)</li>
 * </ul>
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class Project {

    private ProjectData projectData;
    private final OntologyEventManager eventManager;
    private final ProjectPermissionManager permissionManager;
    private ProjectConfiguration projectConfiguration;
    private LayoutManager layoutManager;

    public Project(String projectName) {
        this(new ProjectData(null, null, projectName, null));
    }

    public Project(ProjectData projectData) {
        this.projectData = projectData;
        this.eventManager = new OntologyEventManager(this);
        this.permissionManager = new ProjectPermissionManager(this);
        this.layoutManager = new LayoutManager(this);
    }

    public ProjectData getProjectData() {
        return projectData;
    }

    public void setProjectData(ProjectData projectData) {
        this.projectData = projectData;
    }

    public void setServerVersion(int serverVersion) {
        eventManager.setServerVersion(serverVersion);
    }

    public String getProjectName() {
        return projectData == null ? null : projectData.getName();
    }

    public void addOntologyListener(OntologyListener ontologyListener) {
        eventManager.addOntologyListener(ontologyListener);
    }

    public void removeOntologyListener(OntologyListener ontologyListener) {
        eventManager.removeOntologyListener(ontologyListener);
    }

    public void forceGetEvents() {
        eventManager.getEventsFromServer();
    }
    
    public void fireOntologyEvent(OntologyEvent event) {
    	eventManager.fireEvent(event);
    }

    public void setProjectConfiguration(ProjectConfiguration projectConfiguration) {
        this.projectConfiguration = projectConfiguration;
    }

    public ProjectConfiguration getProjectConfiguration() {
        return projectConfiguration;
    }

    public void setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public ProjectPermissionManager getProjectPermissionManager() {
        return permissionManager;
    }

    public boolean hasWritePermission(String user) {
        return getProjectPermissionManager().hasPermission(user, PermissionConstants.WRITE);
    }

    public boolean hasWritePermission() {
        return getProjectPermissionManager().hasPermission(GlobalSettings.getGlobalSettings().getUserName(), PermissionConstants.WRITE);
    }

    public boolean hasPermission(String operation) {
        return getProjectPermissionManager().hasPermission(GlobalSettings.getGlobalSettings().getUserName(), operation);
    }

    public void dispose() {
        // TODO: we might notify the session that project has been closed
        eventManager.dispose();
        permissionManager.dispose();
    }
}

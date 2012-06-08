package edu.stanford.bmir.protege.web.client.util;

import edu.stanford.bmir.protege.web.client.event.OntologyListener;
import edu.stanford.bmir.protege.web.client.event.ProjectEventManager;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.ui.LayoutManager;

public class Project {
	
	private Session session;
	private ProjectData projectData;	
	private ProjectEventManager eventManager;
	private ProjectConfiguration projectConfiguration;
	private LayoutManager layoutManager;
	private boolean hasWritePermission = false;
	
	public Project(Session session, ProjectData projectData) {	
		this.session = session;
		this.projectData = projectData;
		this.eventManager = new ProjectEventManager(this);
		this.layoutManager = new LayoutManager(this);
	}


	public Session getSession() {
		return session;
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
	
	public String getUserName() {
		return session == null ? null : session.getUserName();
	}
	
	public void setUserName(String newUser) {
		if (session == null) { session = new Session(newUser); }
		session.setUserName(newUser);		
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

	public boolean hasWritePermission() {
		return hasWritePermission;
	}
	
	public void setHasWritePermission(boolean hasWritePermission) {
		this.hasWritePermission = hasWritePermission; 
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
	
	public void dispose() {
		//TODO: we might notify the session that project has been closed
		eventManager.dispose();
	}
}

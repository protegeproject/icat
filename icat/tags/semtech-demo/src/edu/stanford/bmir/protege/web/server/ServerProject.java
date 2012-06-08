package edu.stanford.bmir.protege.web.server;

import java.util.List;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.Log;

public class ServerProject<P> {

	private P project;
	private String projectName;
	private ServerEventManager eventManager;

	public P getProject() {
		return project;
	}

	synchronized void setProject(P project) {
		this.project = project;
		//TODO: temporarily commmented out
		eventManager = new ServerEventManager(this);
	}

	public List<OntologyEvent> getEvents(long fromVersion) {
		return eventManager.getEvents(fromVersion);
	}

	public int getServerVersion(){
		return eventManager.getServerRevision();
	}

	public boolean isLoaded() { //TODO: check if you need both conditions
		return project != null && eventManager != null;
	}

	public String getProjectName() {
        return projectName;
    }

	public void setProjectName(String name) {
        this.projectName = name;
    }

	public void dispose() {
		eventManager.dispose();
		try {//TODO: we don't want to import the Disposable class - find a better solution
		    if (project.getClass().isAssignableFrom(Disposable.class)) {
		        ((Disposable)project).dispose();
		    }
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Errors at disposing remote project", e);
		}
	}

}

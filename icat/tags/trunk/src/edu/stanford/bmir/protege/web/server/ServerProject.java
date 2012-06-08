package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.client.event.AbstractEvent;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;

public class ServerProject {
	
	private Project project;
	private ServerEventManager eventManager;

	public Project getProject() {
		return project;
	}
	
	void setProject(Project project) {
		this.project = project;
		eventManager = new ServerEventManager(this);
	}

	public ArrayList<AbstractEvent> getEvents(long fromVersion) {
		return eventManager.getEvents(fromVersion);
	}
	
	public int getServerVersion(){
		return eventManager.getServerVersion();
	}
	
	public void dispose() {
		eventManager.dispose();
		try {
			project.dispose();
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Errors at disposing remote project", e);
		}		
	}
	
}

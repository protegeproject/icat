package edu.stanford.bmir.protege.web.server;

import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.rpc.EventsService;
import edu.stanford.smi.protege.model.Project;

public class EventsServiceImpl extends RemoteServiceServlet implements EventsService {

	private static final long serialVersionUID = 9134954484233658820L;

	public List<OntologyEvent> getEvents(String projectName, long fromVersion) {
        ServerProject<Project> serverProject = Protege3ProjectManager.getProjectManager().getServerProject(projectName, false);
        if (serverProject == null) {
            throw new RuntimeException("Could not get ontology: " + projectName + " from server.");
        }
        return serverProject.isLoaded() ? serverProject.getEvents(fromVersion) : null;
    }
	
}

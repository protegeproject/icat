package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;

/**
 * A service for dealing with events.
 * 
 * @author ttania
 *
 */
@RemoteServiceRelativePath("events")
public interface EventsService extends RemoteService {

	 /**
	 * A method for retrieving the events from the server from a particular revision to the 
	 * end revision (head).
	 * Used to update the values in the user interface.
	 *
	 * @param projectName - the project name
	 * @param fromVersion - the revision number from which the events should be fetched 
	 * 
	 * @return - A list of {@link OntologyEvent} events
	 */
	public List<OntologyEvent> getEvents(String projectName, long fromVersion);

}

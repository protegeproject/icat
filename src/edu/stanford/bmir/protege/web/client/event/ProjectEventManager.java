package edu.stanford.bmir.protege.web.client.event;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * Polls the server for events every x seconds.
 * Keeps the current version of the project on the client.
 * Dispatches events to the listeners.
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ProjectEventManager {
	
	/**
	 * Sets the time interval in seconds in which the
	 * client will periodically ask the server for new events.
	 * If SERVER_POLL_DELAY = -1, then the client will not poll
	 * for events.
	 */
	private static int SERVER_POLL_DELAY = 5;
	private int errorsFromServer = 0;
	
	private Project project;
	private long currentVersion;
	private Timer getEventsTimer;
	private ArrayList<AbstractEvent> events;	
	private ArrayList<OntologyListener> ontologyListeners;

	public ProjectEventManager(Project project) {
		this.project = project;
		this.ontologyListeners = new ArrayList<OntologyListener>();
		this.events = new ArrayList<AbstractEvent>();
	}

	
	/**
	 * Should happen only at init
	 * @param serverVersion
	 */
	public void setServerVersion(int serverVersion) {
		currentVersion = serverVersion;
	}
	
	
	public void addOntologyListener(OntologyListener ontologyListener) {
		ontologyListeners.add(ontologyListener);
		//only start time if somebody is interested..
		startGetEventsTimer(SERVER_POLL_DELAY);
	}
	
	public void removeOntologyListener(OntologyListener ontologyListener) {
		ontologyListeners.remove(ontologyListener);
	}
	
	private void dispatchEvents(ArrayList<AbstractEvent> events) {
		for (Iterator<AbstractEvent> iterator = events.iterator(); iterator.hasNext();) {
			AbstractEvent event = (AbstractEvent) iterator.next();
			dispatchEvent(event);
			iterator.remove();
		}
	}

	private void dispatchEvent(AbstractEvent event) {
		for (Iterator<OntologyListener> iterator = ontologyListeners.iterator(); iterator.hasNext();) {
			OntologyListener listener = (OntologyListener) iterator.next();
			try {
				if (event instanceof EntityCreateEvent) {
					listener.entityCreated((EntityCreateEvent)event);
				} else  if (event instanceof EntityDeleteEvent) {
					listener.entityDeleted((EntityDeleteEvent)event);
				} else  if (event instanceof EntityRenameEvent) {
					listener.entityRenamed((EntityRenameEvent)event);
				}else {
					GWT.log("Unknown type of event: " + event, null);
				}
			} catch (Exception e) {
				GWT.log("Failed at event dispatch " + event, e);
			}
		}		
	}
	
	/*
	 * Timer methods
	 */
	private void startGetEventsTimer(int seconds) {
		if (getEventsTimer != null) {
			stopGetEventsTimer();
		}
		if (seconds > 0) {
			getEventsTimer = new Timer() {
				public void run() {
					getEventsFromServer();
				}
			};	
			getEventsTimer.scheduleRepeating(seconds * 1000);
		}
	}

	private void stopGetEventsTimer() {
		if (getEventsTimer == null) {
			return;
		}		
		getEventsTimer.cancel();
		getEventsTimer = null;
	}
	
	
	/*
	 * Get events from server methods
	 */
	
	public void getEventsFromServer() {
		OntologyServiceManager.getInstance().getEvents(project.getProjectName(), currentVersion, 
				new GetEventsFromServer());		
	}
	
	class GetEventsFromServer extends AbstractAsyncHandler<ArrayList<AbstractEvent>> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting events from server", caught);
			errorsFromServer ++ ;
			if (errorsFromServer > 10) {
				Window.alert("There are problems communicating with the server." +
						"Please restart the browser and try again.");
				stopGetEventsTimer();
				errorsFromServer = 0;
			}
		}

		public void handleSuccess(ArrayList<AbstractEvent> serverEvents) {				
			//TODO: Make a better implementation later
			long addedEntriesSize = serverEvents.size();			
			currentVersion = currentVersion + addedEntriesSize;
			
			events.addAll(serverEvents);
			
			dispatchEvents(events);
		}
	}

	public void dispose() {
		stopGetEventsTimer();
	}
	
}

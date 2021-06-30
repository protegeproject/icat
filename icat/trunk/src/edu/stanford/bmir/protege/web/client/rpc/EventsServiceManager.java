package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;

public class EventsServiceManager {
	
    private static EventsServiceAsync proxy;
    static EventsServiceManager instance;

    public static EventsServiceManager getInstance() {
        if (instance == null) {
            instance = new EventsServiceManager();
        }
        return instance;
    }

    private EventsServiceManager() {
        proxy = (EventsServiceAsync) GWT.create(EventsService.class);
    }

    public void getEvents(String projectName, long fromVersion, AsyncCallback<List<OntologyEvent>> cb) {
        proxy.getEvents(projectName, fromVersion, cb);
    }
	
}

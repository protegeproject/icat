package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;

public interface EventsServiceAsync {

	void getEvents(String projectName, long fromVersion, AsyncCallback<List<OntologyEvent>> cb);
	
}

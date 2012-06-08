package edu.stanford.bmir.protege.web.client.event;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class OntologyEvent extends AbstractEvent {

	public OntologyEvent() {
		
	}
	
	public OntologyEvent(EntityData entity, int type, String user) {
		super(entity, type, user);
	}

	
}

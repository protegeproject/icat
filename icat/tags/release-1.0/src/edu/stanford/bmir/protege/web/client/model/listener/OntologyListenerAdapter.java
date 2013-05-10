package edu.stanford.bmir.protege.web.client.model.listener;

import edu.stanford.bmir.protege.web.client.model.event.EntityCreateEvent;
import edu.stanford.bmir.protege.web.client.model.event.EntityDeleteEvent;
import edu.stanford.bmir.protege.web.client.model.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.model.event.PropertyValueEvent;

public class OntologyListenerAdapter implements OntologyListener {

	public void entityCreated(EntityCreateEvent ontologyEvent) {
		// TODO Auto-generated method stub
	}

	public void entityDeleted(EntityDeleteEvent ontologyEvent) {
		// TODO Auto-generated method stub
	}

	public void entityRenamed(EntityRenameEvent renameEvent) {
		// TODO Auto-generated method stub
	}

	public void propertyValueAdded(PropertyValueEvent propertyValueEvent) {
		// TODO Auto-generated method stub
	}

	public void propertyValueRemoved(PropertyValueEvent propertyValueEvent) {
		// TODO Auto-generated method stub
	}

	public void propertyValueChanged(PropertyValueEvent propertyValueEvent) {
		// TODO Auto-generated method stub
	}

    public void individualAddedRemoved(OntologyEvent ontologyEvent) {
        // TODO Auto-generated method stub
    }

}

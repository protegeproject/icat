package edu.stanford.bmir.protege.web.client.model.listener;

import edu.stanford.bmir.protege.web.client.model.event.EntityCreateEvent;
import edu.stanford.bmir.protege.web.client.model.event.EntityDeleteEvent;
import edu.stanford.bmir.protege.web.client.model.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.model.event.PropertyValueEvent;

public interface OntologyListener {

	public void entityCreated(EntityCreateEvent createEvent);

	public void entityDeleted(EntityDeleteEvent deleteEvent);

	public void entityRenamed(EntityRenameEvent renameEvent);

	public void propertyValueAdded(PropertyValueEvent propertyValueEvent);

	public void propertyValueRemoved(PropertyValueEvent propertyValueEvent);

	public void propertyValueChanged(PropertyValueEvent propertyValueEvent);

	public void individualAddedRemoved(OntologyEvent ontologyEvent);

}

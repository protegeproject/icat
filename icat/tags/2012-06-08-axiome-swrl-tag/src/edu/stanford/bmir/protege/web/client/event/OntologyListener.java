package edu.stanford.bmir.protege.web.client.event;

public interface OntologyListener {
	
	public void entityCreated(EntityCreateEvent createEvent);
	
	public void entityDeleted(EntityDeleteEvent deleteEvent);
	
	public void entityRenamed(EntityRenameEvent renameEvent);

}

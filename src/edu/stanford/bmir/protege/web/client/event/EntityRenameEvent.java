package edu.stanford.bmir.protege.web.client.event;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class EntityRenameEvent extends OntologyEvent {

	private String oldName;

	public EntityRenameEvent() {	}
	
	public EntityRenameEvent(EntityData entity, String oldName, String user) {
		super(entity, EventType.ENTITY_RENAMED, user);	
		this.oldName = oldName;
	}

	public String getOldName() {
		return oldName;
	}
		
}

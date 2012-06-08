package edu.stanford.bmir.protege.web.client.event;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class EntityCreateEvent extends OntologyEvent {
	
	protected ArrayList<EntityData> superEntities;
	
	public EntityCreateEvent() {
		
	}
	
	public EntityCreateEvent(EntityData entity, String user) {
		this(entity, EventType.ENTITY_CREATED, user, null);
	}
	
	public EntityCreateEvent(EntityData entity, int type, String user, ArrayList superEntities) {
		super(entity, type, user);
		this.superEntities = superEntities;
	}

	public ArrayList<EntityData> getSuperEntities() {
		return superEntities;
	}
	
}

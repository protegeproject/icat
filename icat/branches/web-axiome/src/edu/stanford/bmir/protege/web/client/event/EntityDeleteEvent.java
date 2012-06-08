package edu.stanford.bmir.protege.web.client.event;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class EntityDeleteEvent extends AbstractEvent {

	protected ArrayList<EntityData> superEntities;
	
	public EntityDeleteEvent() {
		// TODO Auto-generated constructor stub
	}	
	
	public EntityDeleteEvent(EntityData entity, String user) {
		this(entity, EventType.ENTITY_DELETED, user, null);
	}
	
	public EntityDeleteEvent(EntityData entity, int type, String user, ArrayList superEntities) {
		super(entity, type, user);
		this.superEntities = superEntities;
	}

	public ArrayList getSuperEntities() {
		return superEntities;
	}


}

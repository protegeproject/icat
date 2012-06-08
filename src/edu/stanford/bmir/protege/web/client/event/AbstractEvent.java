package edu.stanford.bmir.protege.web.client.event;

import java.io.Serializable;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class AbstractEvent implements Serializable, Event {
	protected int type;
	protected String user;
	protected EntityData source;

		
	public AbstractEvent(EntityData source, int type, String user) {
		super();
		this.source = source;
		this.type = type;
		this.user = user;
	}

	public AbstractEvent() {
		
	}
	
	public String getUser() {
		return user;
	}
	
	public int getType() {
		return type;
	}	
		
	public EntityData getEntity() {
		return source;
	}

}

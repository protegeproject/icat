package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;


public class Triple implements Serializable {
	
	private EntityData entity;
	private PropertyEntityData property;
	private EntityData value; 
	
	public Triple() {
	}
	
	public Triple(EntityData entity, PropertyEntityData property, EntityData value) {
		this.entity = entity;
		this.property = property;		
		this.value = value;
	}

	public EntityData getEntity() {
		return entity;
	}

	public PropertyEntityData getProperty() {
		return property;
	}

	public EntityData getValue() {
		return value;
	}
	
}

package edu.stanford.bmir.protege.web.client.rpc.data.icd;

import java.io.Serializable;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;

public class InternalReferenceData implements Serializable {
	
	private static final long serialVersionUID = 5555630327693711809L;
	
	private PropertyEntityData property;
	private EntityData referencedEntity;
	private PropertyEntityData referenceValueProperty;

	public InternalReferenceData() {}
	
	public PropertyEntityData getProperty() {
		return property;
	}
	
	public void setProperty(PropertyEntityData property) {
		this.property = property;
	}
	
	public EntityData getReferencedEntity() {
		return referencedEntity;
	}
	
	public void setReferencedEntity(EntityData referencedEntity) {
		this.referencedEntity = referencedEntity;
	}
	
	public PropertyEntityData getReferenceValueProperty() {
		return referenceValueProperty;
	}
	
	public void setReferenceValueProperty(PropertyEntityData referenceValueProperty) {
		this.referenceValueProperty = referenceValueProperty;
	}

}

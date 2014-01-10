package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that holds a compressed form of the triples for a subject
 * to enable efficient processing on the client side, where ordering 
 * of properties, and their values is preserved.
 * <br /><br />
 * Example of data structure:
 * subj1 -> {prop1, prop2, ...} -> {{value1, value2}, {value3}, ...} }
 *
 * @author csnyulas
 *
 */
public class EntityPropertyValuesList implements Serializable {

	private static final long serialVersionUID = 7748731638176720736L;

    private EntityData subject;
    private List<PropertyEntityData> properties = new ArrayList<PropertyEntityData>();
    private List<List<EntityData>> propertyValues = new ArrayList<List<EntityData>>();

    public EntityPropertyValuesList() { }

    public EntityPropertyValuesList(EntityData subject) {
        this.subject = subject;
    }

    public EntityPropertyValuesList(EntityPropertyValuesList bluePrint) {
    	this.subject = bluePrint.subject;
    	this.properties = new ArrayList<PropertyEntityData>(bluePrint.properties);
    	this.propertyValues = new ArrayList<List<EntityData>>(bluePrint.propertyValues);
    }
    
    public void setSubject(EntityData subject) {
        this.subject = subject;
    }
    public EntityData getSubject() {
        return subject;
    }

    public Collection<PropertyEntityData> getProperties() {
        return properties;
    }
    
    public List<List<EntityData>> getAllPropertyValues() {
    	return propertyValues;
    }
    
    public PropertyEntityData getProperty(int propIndex) {
    	return properties.get(propIndex);
    }

    public List<EntityData> getPropertyValues(int propIndex) {
        return propertyValues.get(propIndex);
    }
    
    public void setProperties(List<PropertyEntityData> properties) {
    	this.properties = properties;
    }

    public void setPropertyValues(int propIndex, List<EntityData> values) {
    	fillPropertyValueList(propIndex);
    	propertyValues.set(propIndex, values);
    }

    public void addPropertyValue(EntityData value) {
    	addPropertyValue(propertyValues.size(), value);
    }
    
    public void addPropertyValue(int propIndex, EntityData value) {
    	fillPropertyValueList(propIndex);
    	
        List<EntityData> values = getPropertyValues(propIndex);
        if (values == null) {
            values = new ArrayList<EntityData>();
            propertyValues.set(propIndex, values);
        }
        values.add(value);
    }

    public void addPropertyValues(int propIndex, List<EntityData> value) {
    	fillPropertyValueList(propIndex);
    	
        List<EntityData> values = getPropertyValues(propIndex);
        if (values == null) {
            values = new ArrayList<EntityData>();
            propertyValues.set(propIndex, values);
        }
        values.addAll(value);
    }

	/**
	 * adds placeholder property values
	 * up to, and including, the desired insertion index
	 * 
	 * @param propIndex minimum desired size of the property values list
	 */
	private void fillPropertyValueList(int propIndex) {
    	for (int i=propertyValues.size(); i<=propIndex; i++) {
    		propertyValues.add(null);
    	}
	}

}

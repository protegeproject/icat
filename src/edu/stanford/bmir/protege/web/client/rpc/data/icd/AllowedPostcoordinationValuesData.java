package edu.stanford.bmir.protege.web.client.rpc.data.icd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;

public class AllowedPostcoordinationValuesData implements Serializable {
	
	private static final long serialVersionUID = -6670351084732240270L;

	private PropertyEntityData property;
	private List<EntityData> values;

	@SuppressWarnings("unused")
	private AllowedPostcoordinationValuesData() {
	}
	
	public AllowedPostcoordinationValuesData(String property) {
		setProperty(property);
	}
	
	public AllowedPostcoordinationValuesData(PropertyEntityData property) {
		this.property = property;
	}
	
	public AllowedPostcoordinationValuesData(String property,
			List<EntityData> values) {
		setProperty(property);
		setValues(values);
	}
	
	public PropertyEntityData getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = new PropertyEntityData(property);
	}
	public void setProperty(PropertyEntityData property) {
		this.property = property;
	}
	
	public List<EntityData> getValues() {
		return values;
	}

	public void setValues(List<EntityData> values) {
		this.values = values;
	}
	
	public void addValue(EntityData value) {
		if (this.values == null) {
			this.values = new ArrayList<EntityData>();
		}
		this.values.add(value);
	}
	
	public void addValue(String name, String browserText, ValueType valueType) {
		EntityData value = new EntityData(name, browserText);
		value.setValueType(valueType);
		addValue(value);
	}

	@Override
	public String toString() {
		return "AllowedPostcoordinationValuesData(" +
				"property: " + property.getName() + ", " +
				"values: " + values;
	}
}

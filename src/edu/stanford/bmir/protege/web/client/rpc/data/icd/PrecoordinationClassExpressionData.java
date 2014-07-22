package edu.stanford.bmir.protege.web.client.rpc.data.icd;

import java.io.Serializable;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;

public class PrecoordinationClassExpressionData implements Serializable {
	
	private static final long serialVersionUID = -6931445646976524941L;

	private PropertyEntityData property;
	private EntityData value;
	private boolean isDefinitional;

	@SuppressWarnings("unused")
	private PrecoordinationClassExpressionData() {
	}
	
	public PrecoordinationClassExpressionData(boolean isDefinitional) {
		this.isDefinitional = isDefinitional;
	}
	
	public PrecoordinationClassExpressionData(String property,
			boolean isDefinitional) {
		this(isDefinitional);
		this.property = new PropertyEntityData(property);
	}
	
	public PropertyEntityData getProperty() {
		return property;
	}
	public void setProperty(PropertyEntityData property) {
		this.property = property;
	}
	
	public EntityData getValue() {
		return value;
	}
	public void setValue(EntityData value) {
		this.value = value;
	}
	public void setValue(String name, String browserText, ValueType valueType) {
		this.value = new EntityData(name, browserText);
		value.setValueType(valueType);
	}
	
	public boolean isDefinitional() {
		return isDefinitional;
	}
	public void setDefinitional(boolean isDefinitional) {
		this.isDefinitional = isDefinitional;
	}

	@Override
	public String toString() {
		return "PrecoordinationClassExpressionData(" +
				"property: " + property.getName() + ", " +
				"value: " + value + ", " + 
				"isDefinitional: " + isDefinitional + ")";
	}
}

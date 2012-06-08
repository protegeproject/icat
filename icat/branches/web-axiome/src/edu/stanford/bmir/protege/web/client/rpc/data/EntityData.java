package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

public class EntityData implements Serializable {
	
	private String name;
	private String browserText;
	private boolean hasAnnotation;
	private EntityData type;
	private ValueType valueType;

	public EntityData() {
		this(null, null);
	}
	
	public EntityData(String name) {
		this(name, name);
	}
	
	public EntityData(String name, String browserText) {
		this(name, browserText, null, false);
	}
	
	public EntityData(String name, String browserText, EntityData type, boolean hasAnnotation) {
		this.name = name;
		this.browserText = browserText;
		this.type = type;
		this.hasAnnotation = hasAnnotation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrowserText() {
		return browserText;
	}

	public void setBrowserText(String browserText) {
		this.browserText = browserText;
	}

	public EntityData getType() {
		return type;
	}

	public void setType(EntityData type) {
		this.type = type;
	}

	public boolean hasAnnotation() {
		return hasAnnotation;
	}

	public void setHasAnnotation(boolean hasAnnotation) {
		this.hasAnnotation = hasAnnotation;
	}
	
	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EntityData)) { return false; }
		return ((EntityData)obj).getName().equals(this.getName());		
	}
	
	@Override
	public int hashCode() {
		if (name != null) {
			return name.length()*11 + 42 + name.hashCode(); 
		}
		return 42;
	}
	
	public String toString() {
		/*
		StringBuffer buffer = new StringBuffer();
		buffer.append(name);
		buffer.append(", browser text: ");
		buffer.append(browserText);
		return buffer.toString();
		*/
		return browserText;
	}
}

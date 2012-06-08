package edu.stanford.bmir.protege.web.client.rpc.data.layout;

public class PropertyToValue {
	private String property;
	private Object value;
	
	public PropertyToValue(String property, Object value) {
		this.property = property;
		this.value = value;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	public String getProperty() {
		return property;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public Object getValue() {
		return value;
	}
}

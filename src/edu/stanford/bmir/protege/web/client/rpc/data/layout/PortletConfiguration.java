package edu.stanford.bmir.protege.web.client.rpc.data.layout;

import java.io.Serializable;
import java.util.Map;

public class PortletConfiguration implements Serializable {

	private static final long serialVersionUID = -1067323872900631937L;
	
	private String name;
	private int height;
	private int width;
	private Map<String, Object> properties;
		
	public PortletConfiguration() {
		this.name = "unnamed"; 
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

/*
	public void setPropertyToValue(List<PropertyToValue> propertyToValue) {
		this.propertyToValue = propertyToValue;
	}

	public List<PropertyToValue> getPropertyToValue() {
		return propertyToValue;
	}
	*/	
}

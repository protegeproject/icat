package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.List;

public class PropertyPairs implements Serializable {
	
	private static final long serialVersionUID = 8828854768601590076L;

	private List<String> properties;
	private List<String> reifiedProperties;
	
	
	public PropertyPairs() {
	}
	
	public PropertyPairs(List<String> properties, List<String> reifiedProperties) {
		this.properties = properties;
		this.reifiedProperties = reifiedProperties;
	}

	public List<String> getProperties() {
		return properties;
	}

	public List<String> getReifiedProperties() {
		return reifiedProperties;
	}

}

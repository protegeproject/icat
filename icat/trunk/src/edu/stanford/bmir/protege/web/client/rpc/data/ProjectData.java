package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class ProjectData implements Serializable {
	private String description;
	private String location;
	private String name;
	private String owner;
	
	public ProjectData() {
	}
	
	public ProjectData(String description, String location, 
					   String name, String owner) {
		this.description = description;
		this.location = location;
		this.name = name;
		this.owner = owner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}

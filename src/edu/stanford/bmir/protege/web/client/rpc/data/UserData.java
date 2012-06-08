package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class UserData implements Serializable {
	private String name;
	private String password;

	public UserData() {
	}

	public UserData(String name) {
	    this.name = name;
	}

	public UserData(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

package edu.stanford.bmir.protege.web.client.rpc.data.scripting;

import java.io.Serializable;

public class ScriptCommand implements Serializable {
	
	private String command;
	
	public ScriptCommand() {}
	
	public ScriptCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
}

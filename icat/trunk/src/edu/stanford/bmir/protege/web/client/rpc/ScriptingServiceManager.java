package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;

public class ScriptingServiceManager {

	private static ScriptingServiceAsync proxy;
	private static ScriptingServiceManager instance;
	
	public static ScriptingServiceManager getInstance() {
		if (instance == null) {
			instance = new ScriptingServiceManager();
		}
		return instance;
	}
	
	public ScriptingServiceManager() {
		proxy = (ScriptingServiceAsync) GWT.create(ScriptingService.class);
	}
	
	public void executePythonScript(String projectName, String user, ScriptCommand cmd, 
			AsyncCallback<ScriptResult> cb) {
		proxy.executePythonScript(projectName, user, cmd, cb);
	}
	
}

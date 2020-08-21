package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;

public interface ScriptingServiceAsync {
	
	void executePythonScript(String projectName, String user, ScriptCommand cmd, AsyncCallback<ScriptResult> cb);

}

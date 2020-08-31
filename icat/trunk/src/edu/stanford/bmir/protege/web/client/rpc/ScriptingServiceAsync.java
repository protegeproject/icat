package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;

public interface ScriptingServiceAsync {
	
	void executePythonScript(String projectName, String user, ScriptCommand cmd, AsyncCallback<ScriptResult> cb);

	void getCodeCompletion(String projectName, String cmdSubstr,  AsyncCallback<List<String>> cb);
}

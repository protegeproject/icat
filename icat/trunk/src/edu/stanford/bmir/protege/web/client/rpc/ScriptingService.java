package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;

@RemoteServiceRelativePath("scripting")
public interface ScriptingService extends RemoteService {

	public ScriptResult executePythonScript(String projectName, String user, ScriptCommand cmd);
	
}

package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;

@RemoteServiceRelativePath("scripting")
public interface ScriptingService extends RemoteService {

	public ScriptResult executePythonScript(String projectName, String user, ScriptCommand cmd);
	
	//TODO: if this works, use a proper class for the return
	public List<String> getCodeCompletion(String projectName, String cmdSubstr);
	
}

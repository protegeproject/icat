package edu.stanford.bmir.protege.web.server.scripting;

import javax.servlet.http.HttpSession;

import org.python.util.PythonInterpreter;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.ScriptingService;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;
import edu.stanford.bmir.protege.web.server.ProjectManagerFactory;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;

public class ScriptingServiceImpl extends RemoteServiceServlet implements ScriptingService {
	
	private static final long serialVersionUID = -3950687775803438561L;


	protected KnowledgeBase getKb(String projectName) {
        Project prj = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
        return prj == null ? null : prj.getKnowledgeBase();
    }
    
    
	@Override
	public ScriptResult executePythonScript(String projectName, String user, ScriptCommand cmd) {
		KnowledgeBase kb = getKb(projectName);
		
		if (kb == null) {
			return new ScriptResult("", "Could not find project: " + projectName);
		}
		
		if (cmd == null || cmd.getCommand().length() == 0) {
			return new ScriptResult("", "No command specified");
		}
		
		HttpSession session = getThreadLocalRequest().getSession();
		if (session == null) {
			return new ScriptResult("", "Expired session. Please log in again.");
		}
		
		PythonInterpreterManager manager = PythonInterpreterManager.getManager();
		PythonInterpreter interp = manager.getInterpreter(session, kb);
		
		if (interp == null) {
			return new ScriptResult("", "Could not initialize a Python interpreter.");
		}
		
		return executeScript(interp, cmd);
	}
	
	
	private ScriptResult executeScript(PythonInterpreter interp, ScriptCommand cmd) {
		return executeExecScript(interp, cmd);
	}

	private ScriptResult executeExecScript(PythonInterpreter interp, ScriptCommand cmd) {
		String cmdStr = getCmdString(cmd);
		
		try {
			StringOutputStream os = new StringOutputStream();
			interp.setOut(os);
			interp.exec(cmdStr);
			String result = os.toString();
			os.close();
			return new ScriptResult(result, "");
		} catch (Exception e) {
			Log.getLogger().warning("Error at executing: " + cmdStr + ". Error message: " + e.getMessage());
			return new ScriptResult("", e.getMessage());
		}
	}
	
	private String getCmdString(ScriptCommand cmd) {
		return cmd.getCommand();
	}
	
}

package edu.stanford.bmir.protege.web.server.scripting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.ScriptingService;
import edu.stanford.bmir.protege.web.client.rpc.iCATException;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptCommand;
import edu.stanford.bmir.protege.web.client.rpc.data.scripting.ScriptResult;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.server.MetaProjectManager;
import edu.stanford.bmir.protege.web.server.ProjectManagerFactory;
import edu.stanford.bmir.protege.web.server.Protege3ProjectManager;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;

public class ScriptingServiceImpl extends RemoteServiceServlet implements ScriptingService {
	
	private static final long serialVersionUID = -3950687775803438561L;
	
	private static final String RUN_PYTHON_SCRIPT = "RunPythonScript";


	protected KnowledgeBase getKb(String projectName) {
        Project prj = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
        return prj == null ? null : prj.getKnowledgeBase();
    }
    
	private boolean canRunPythonScript(String projectName, String user) {
		if (user == null) {
			return false;
		}
		
		MetaProjectManager mm =  Protege3ProjectManager.getProjectManager().getMetaProjectManager();
		return mm.isOperationAllowed(projectName, user, RUN_PYTHON_SCRIPT);
	}
	
	private String getUser() {
        final HttpSession session = getThreadLocalRequest().getSession();
        return (String) session.getAttribute(AuthenticationConstants.USER);
	}
	
	@Override
	public ScriptResult executePythonScript(String projectName, String user, ScriptCommand cmd) {
		KnowledgeBase kb = getKb(projectName);
		
		if (kb == null) {
			return new ScriptResult("", "Could not find project: " + projectName);
		}
		
		if (canRunPythonScript(projectName, user) == false) {
			return new ScriptResult("", user + " not authorized to run Python script on project " + projectName);
		}
		
		
		if (cmd == null || cmd.getCommand().length() == 0) {
			return new ScriptResult("", "No command specified");
		}
		
		PythonInterpreter interp = null;
		
		try {
			interp = getPythonInterpreter(kb);
		} catch (iCATException e) {
			return new ScriptResult("", e.getMessage());
		}
		
		if (interp == null) {
			return new ScriptResult("", "Could not initialize a Python interpreter.");
		}
		
		return executeScript(interp, cmd);
	}
	
	private PythonInterpreter getPythonInterpreter(KnowledgeBase kb) throws iCATException {
		HttpSession session = getThreadLocalRequest().getSession();
		if (session == null) {
			throw new iCATException("Expired session. Please log in again.");
		}
		
		PythonInterpreterManager manager = PythonInterpreterManager.getManager();
		PythonInterpreter interp = manager.getInterpreter(session, kb);
		
		return interp;
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

	// ************** Code completion *******************

	@Override
	public List<String> getCodeCompletion(String projectName, String cmdSubstr) {
		
		List<String> compls = new ArrayList<String>();
		
		if (canRunPythonScript(projectName, getUser()) == false) {
			return compls;
		}
		
		cmdSubstr = cmdSubstr.trim();
		
		//TODO: must expand to other characters as well
		int startIndex = cmdSubstr.lastIndexOf(" ");
		if (startIndex >= 0 ) {
			cmdSubstr = cmdSubstr.substring(startIndex + 1);
		}
		
		int cmdContextIndex = cmdSubstr.lastIndexOf(".");
		if (cmdContextIndex < 1) {
			return compls;
		}
		
		String cmdContext = cmdSubstr.substring(0, cmdContextIndex);
		String cmdPrefix = "";
		
		if (cmdContextIndex < cmdSubstr.length()) {
			cmdPrefix = cmdSubstr.substring(cmdContextIndex + 1);
		}
		
		KnowledgeBase kb = getKb(projectName);
		
		if (kb == null) {
			return compls;
		}

		PythonInterpreter interp = null;
		
		try {
			interp = getPythonInterpreter(kb);
		} catch (iCATException e) {
			return compls;
		}
		
		if (interp == null) {
			return compls;
		}
		
		try {
			//TODO: this actually evaluates the expression, and if there is no result, then 
			//it won't give any suggestions back.
			//Better to look two methods behind and get the return type and its methods
			PyObject result = interp.eval(cmdContext + ".getClass().getMethods()");
			Method[] meths = (Method[]) result.__tojava__(new java.lang.reflect.Method[0].getClass());
			return getCodeComplition(meths, cmdPrefix);
		} catch (Exception e) {
			return compls;
		}
		
	}
	
	private List<String> getCodeComplition(Method[] meths, String cmdPrefix) {
		List<String> suggestions = new ArrayList<String>();
		for (Method meth : meths) {
			String methName = meth.getName();
			String shortName = methName.substring(methName.lastIndexOf(".") + 1);
			if (cmdPrefix.length() == 0 || shortName.startsWith(cmdPrefix)) {
				suggestions.add(shortName);
				System.out.println("Suggestion: " + shortName);
			}
		}
		
		return sortSuggestions(suggestions);
	}


	private List<String> sortSuggestions(List<String> suggestions) {
		
		suggestions.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});;
		
		return suggestions;
	}
	
}

package edu.stanford.bmir.protege.web.server.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpSession;

import org.python.util.PythonInterpreter;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * This class is used to manage the Python interpreters for different users and different projects.
 * It maintains a map: session -> (kb -> Python interpreter).
 * 
 * A user might have multiple projects open, and each of them will have its own Python interpreter.
 * 
 * @author ttania
 *
 */
public class PythonInterpreterManager {

	private static PythonInterpreterManager manager;
	
	//TODO: using session as a key is not the best idea, as even if the user signs out
	//the session object is still the same. We have listeners to handle that, but
	//it is not an ideal situation.
	//TODO: We also need a good solution for user log out and sessions. We likely need 
	//a custom listerner for login/out events on the server side
	private Map<HttpSession, Map<KnowledgeBase, PythonInterpreter>> session2Kb2InterpMap = new HashMap<HttpSession, Map<KnowledgeBase, PythonInterpreter>>();
	
	private PythonInterpreterManager() {}
	
	public static PythonInterpreterManager getManager() {
		if (manager == null) {
			manager = new PythonInterpreterManager();
		}
		return manager;
	}

	
	public PythonInterpreter getInterpreter(HttpSession session, KnowledgeBase kb) {
		Map<KnowledgeBase, PythonInterpreter> kb2InterpMap = session2Kb2InterpMap.get(session);
		
		if (kb2InterpMap == null) {
			kb2InterpMap = new HashMap<KnowledgeBase, PythonInterpreter>();
			session2Kb2InterpMap.put(session, kb2InterpMap);
		}
		
		PythonInterpreter interp = kb2InterpMap.get(kb);
		
		if (interp == null) {
			interp = createPythonInterpreter(kb);
			kb2InterpMap.put(kb, interp);
		}
	
		return interp;
	}

	private PythonInterpreter createPythonInterpreter(KnowledgeBase kb) {
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.exec("import sys");
		try {
			interpreter.exec("print sys");
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Could not retrieve module sys" , e);
		}
		interpreter.set("kb", kb);
		if (kb instanceof OWLModel) {
			interpreter.set("cm", new ICDContentModel((OWLModel)kb));
		}
		
		return interpreter;
	}
	
	public void removeSession(HttpSession session) {
		Map<KnowledgeBase, PythonInterpreter> kb2InterpMap = session2Kb2InterpMap.get(session);
		
		if (kb2InterpMap != null) {
			for (PythonInterpreter interp : kb2InterpMap.values()) {
				interp.close();
				Log.getLogger().info("Removed Python interpreter for session: " + session);
			}
		}
		
		session2Kb2InterpMap.remove(session);
	}
	
	public void dispose() {
		for (HttpSession session : session2Kb2InterpMap.keySet()) {
			removeSession(session);
		}
		
		session2Kb2InterpMap.clear();
		manager = null;
	}
}

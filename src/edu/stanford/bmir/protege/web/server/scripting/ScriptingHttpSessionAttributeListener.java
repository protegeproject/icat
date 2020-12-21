package edu.stanford.bmir.protege.web.server.scripting;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.smi.protege.util.Log;

public class ScriptingHttpSessionAttributeListener implements HttpSessionAttributeListener {

	@Override
	public void attributeAdded(HttpSessionBindingEvent event) {
		// do nothing
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent event) {
		removePythonInterpreter(event.getSession(), event.getName(), event.getValue());
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent event) {
		removePythonInterpreter(event.getSession(), event.getName(), event.getValue());
	}

	
	private void removePythonInterpreter(HttpSession session, String name, Object value) {
		if (AuthenticationConstants.USER.equals(name) == false) {
			return;
		}
		
		String authUser = null;
		
		try {
			authUser =  (String) session.getAttribute(AuthenticationConstants.USER); //the new value for user
		} catch (IllegalStateException e) { //session is already invalidated
			// do nothing, likely the python interpreter was already cleaned up by the session listener
		}
		
		if (authUser == null) {
			Log.getLogger().info("Removing Python interpreter for user: " + value);
			PythonInterpreterManager.getManager().removeSession(session);
		}
	}
	
}

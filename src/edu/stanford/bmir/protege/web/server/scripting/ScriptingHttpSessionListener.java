package edu.stanford.bmir.protege.web.server.scripting;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class ScriptingHttpSessionListener implements HttpSessionListener {

	
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		//do nothing
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		
		if (session != null) {
			PythonInterpreterManager.getManager().removeSession(session);
		}
	}

}

package edu.stanford.bmir.protege.web.client.util;

import java.util.ArrayList;


public class Session {
	private String userName;
		
	private ArrayList<SessionListener> listeners = new ArrayList<SessionListener>();
	
	public Session() {
		this("(none)");
	}
	
	public Session(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		String oldName = this.userName;
		this.userName = userName;
		notifyLoginoutSessionListener(oldName, this.userName);
	}
	
	public void addSessionListener(SessionListener listener) {
		listeners.add(listener);
	}
	
	public void removeSessionListener(SessionListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyLoginoutSessionListener(String oldName, String newName) {
		if (newName.equals(oldName)) {
			return;
		}		
		if (!oldName.equals("No user")) { //logout
			for (SessionListener listener : listeners) {			
				listener.onLogout(oldName);
			}
		}
		if (!newName.equals("No user")) { //login
			for (SessionListener listener : listeners) {			
				listener.onLogin(newName);
			}			
		}			
	}	
	
}

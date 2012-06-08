package edu.stanford.bmir.protege.web.client.util;

public interface SessionListener {

	void onLogin(String userName);
	
	void onLogout(String userName);
}

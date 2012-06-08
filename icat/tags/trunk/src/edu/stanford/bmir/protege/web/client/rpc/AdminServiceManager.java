package edu.stanford.bmir.protege.web.client.rpc;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class AdminServiceManager {
	
	private static AdminServiceAsync proxy;
	private static AdminServiceManager instance;
	
	private AdminServiceManager() {		
		proxy = (AdminServiceAsync) GWT.create(AdminService.class);		
	}
	
	public static AdminServiceManager getInstance() {
		if (instance == null) {
			instance = new AdminServiceManager();
		}
		return instance;
	}
	
	public void validateUser(String name, String password, AsyncCallback<UserData> cb) {		
		proxy.validateUser(name, password, cb);		
	}
	
	public void registerUser(String name, String password, AsyncCallback<UserData> cb) {		
		proxy.registerUser(name, password, cb);		
	}
	
	public void getProjects(String user, AsyncCallback<ArrayList<ProjectData>> cb) {
		proxy.getProjects(user, cb);
	}
	
	public void refreshMetaproject(AsyncCallback cb) {
		proxy.refreshMetaproject(cb);
	}
}

package edu.stanford.bmir.protege.web.client.rpc;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public interface AdminServiceAsync {

	void validateUser(String name, String password, AsyncCallback<UserData> cb);
	
	void registerUser(String name, String password, AsyncCallback<UserData> cb);

	void getProjects(String user, AsyncCallback<ArrayList<ProjectData>> cb);
	
	void refreshMetaproject(AsyncCallback cb);
}

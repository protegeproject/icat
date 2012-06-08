package edu.stanford.bmir.protege.web.client.rpc;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

/**
 * A service for accessing administrative and configuration data stored in 
 * Protege's Metaproject. Examples of data stored in the Metaproject are: 
 * <ul>
 * 	 <li>user names and passwords</li>
 * 	 <li>user groups and permissions</li>
 *   <li>projects available on the server</li>
 *   <li>project locations, owners, and descriptions</li>
 * </ul>
 * The list above is not meant to be exhaustive.
 * <p />
 *   
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
@RemoteServiceRelativePath("admin")
public interface AdminService extends RemoteService {

	UserData validateUser(String name, String password);

	UserData registerUser(String name, String password);

	ArrayList<ProjectData> getProjects(String user);
	
	/**
	 * For now, it will refresh the users list. 
	 * Later: it should refresh also the projects list.
	 * TODO: Need to notify the users about this.
	 */
	void refreshMetaproject();
}

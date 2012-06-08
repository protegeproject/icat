package edu.stanford.bmir.protege.web.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.util.ProjectInfo;
import edu.stanford.smi.protege.util.Log;

public class RemoteMetaProjectManager implements MetaProjectManager {
	
	private ArrayList<ProjectData> projectData; 	//cached for performance reasons
	
	public boolean hasValidCredentials(String userName, String password) {
		RemoteServer server = ProjectManager.getProjectManager().getServer();
		if (server == null) {
			Log.getLogger().warning("Could not get remote projects. Reason: Cannot connect to remote server.");
			throw new RuntimeException("Could not retrieve the remote projects from the Protege server. Reason: Cannot connect to remote Protege server.");			
		}
		
		try {
			return server.hasValidCredentials(userName, password);
		} catch (RemoteException e) {
			Log.getLogger().log(Level.SEVERE, "Could not log in user: " + userName, e);
			//throw new RuntimeException("There was an error at logging in. Message: " + e.getMessage(), e);
			return false;
		}	
	}

	public ArrayList<ProjectData> getProjectsData(String user) {
		if (projectData != null) {	return projectData;	}
		
		//TODO: How to handle this?
		if (user == null) { user = ApplicationProperties.getProtegeServerUser(); }
		
		ArrayList<ProjectData> projectData = new ArrayList<ProjectData>();
		RemoteServer server = ProjectManager.getProjectManager().getServer();
		if (server == null) {
			Log.getLogger().warning("Could not get remote projects. Reason: Cannot connect to remote server.");
			throw new RuntimeException("Could not retrieve the remote projects from the Protege server. Reason: Cannot connect to remote Protege server.");			
		}
		Collection<ProjectInfo> prjInfos = new ArrayList<ProjectInfo>();
		try {
			prjInfos = server.getAvailableProjectInfo(new Session(user, null));
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Could not retrieve available projects from the Protege server.", e);
			throw new RuntimeException("Could not retrieve available projects from the Protege server.");
		}
		for (ProjectInfo prjInfo : prjInfos) {
			ProjectData pd = new ProjectData();
			pd.setName(prjInfo.getName());
			pd.setDescription(prjInfo.getDescription());
			pd.setOwner(prjInfo.getOwner());
			projectData.add(pd);
		}
		return projectData;
	}
	

	public void reloadMetaProject() {
		projectData.clear();
		projectData = null;		
	}

	
	public UserData registerUser(String name, String password) {		
		boolean success = false;		
		UserData data = null;	
		
		RemoteServer server = ProjectManager.getProjectManager().getServer();
		if (server == null) {
			Log.getLogger().warning("Could not get remote projects. Reason: Cannot connect to remote server.");
			throw new RuntimeException("Could not retrieve the remote projects from the Protege server. Reason: Cannot connect to remote Protege server.");			
		}	
		
		try {				
			success = server.createUser(name, password);
			if (success) { data = new UserData(name, password);	}
		} catch (Exception e) {
			throw new RuntimeException("Could not create user " + name + ". Reason: " + e.getMessage(), e);
		} 		
		return data;
	}


	
	/*
	 * Helper class
	 */
	class ProjectsDataComparator implements Comparator<ProjectData> {
		public int compare(ProjectData prj1, ProjectData prj2) {
			return prj1.getName().compareTo(prj2.getName());
		}		
	}

}

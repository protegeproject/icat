package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;

public class LocalMetaProjectManager implements MetaProjectManager {

	private MetaProject metaproject;
	boolean runsInClientServerMode;
	
	//cache for performance reasons
	private ArrayList<ProjectData> projectData;

	public LocalMetaProjectManager() {
		metaproject = new MetaProjectImpl(ApplicationProperties.getLocalMetaprojectURI());
	}
	
	public boolean hasValidCredentials(String userName, String password) {
		if (metaproject == null){
            return false;
        }
        User user = metaproject.getUser(userName);
        if (user == null) {
            return false;
        }
        return user.verifyPassword(password);
	}

	public ArrayList<ProjectData> getProjectsData(String userName) {
		if (projectData != null) {
			return projectData;
		}	
		
		if (userName == null || userName.equals("No user")) {
			userName = "Guest";
		}
		//TODO: check with Tim if it needs synchronization
		projectData = new ArrayList<ProjectData>();
		
		Policy policy = metaproject.getPolicy();
		User user = policy.getUserByName(userName);		

		for (ProjectInstance projectInstance : metaproject.getProjects()) {	
			if (user != null && (
					!policy.isOperationAuthorized(user,
							MetaProjectConstants.OPERATION_DISPLAY_IN_PROJECT_LIST,
							projectInstance) ||
							!policy.isOperationAuthorized(user,
									MetaProjectConstants.OPERATION_READ,
									projectInstance))) {
				continue;
			}

			try {
				ProjectData pd = new ProjectData();

				// JV: I think there is a problem with the getDescription method 
				// in protege-core, i.e., if the metaproject has a null value for 
				// the description, the getDescription method throws an exception.
				// Why is this value required in the metaproject?
				pd.setDescription(projectInstance.getDescription());

				pd.setLocation(projectInstance.getLocation());
				pd.setName(projectInstance.getName());

				User owner = projectInstance.getOwner();
				if (owner != null) {
					pd.setOwner(owner.getName());
				}

				Log.getLogger().info("Found project def in metaproject: " + pd.getName() + " at: " + pd.getLocation());
				projectData.add(pd);

			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Found project def with problems: " + projectInstance + " Message: " + e.getMessage(), e);
			}
		}

		Collections.sort(projectData, new ProjectsDataComparator());

		return projectData;
	}

	/*
	 * Path methods 
	 */
	public URI getProjectURI(String projectName) {
		for (ProjectInstance projectInstance : metaproject.getProjects()) {
			String name = projectInstance.getName();
			if (name.equals(projectName)) {
				String path = projectInstance.getLocation();
				URL url = URIUtilities.toURL(path, ApplicationProperties.getWeprotegeDirectory());
				URI uri = null;
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					Log.getLogger().log(Level.SEVERE, "Error at getting path for project " + projectName + ". Computed path: " +	url, e);
				}
				return uri;
			}
		}
		return null;
	}
		

	public void init(boolean loadOntologiesFromServer) {		
		runsInClientServerMode = loadOntologiesFromServer;
	}
	

	/* (non-Javadoc)
	 * @see edu.stanford.bmir.protege.web.server.MetaProjectManager#reloadMetaProject()
	 */
	public void reloadMetaProject() {
		if (metaproject != null) {
			((MetaProjectImpl)metaproject).getKnowledgeBase().getProject().dispose();
		}
		metaproject = new MetaProjectImpl(ApplicationProperties.getLocalMetaprojectURI());		
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

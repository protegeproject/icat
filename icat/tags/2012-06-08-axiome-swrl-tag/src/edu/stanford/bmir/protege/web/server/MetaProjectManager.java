package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;

public interface MetaProjectManager {

	public abstract boolean hasValidCredentials(String userName, String password);

	public abstract ArrayList<ProjectData> getProjectsData(String user);

	/**
	 * Reloads the metaproject. Should be used with care, because it reloads 
	 * besides the user/password info, also the projects info.
	 * Clients should be notified of the change.
	 */
	public abstract void reloadMetaProject();

}
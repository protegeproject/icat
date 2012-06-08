package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.event.ServerProjectAdapter;
import edu.stanford.smi.protege.event.ServerProjectListener;
import edu.stanford.smi.protege.event.ServerProjectSessionClosedEvent;
import edu.stanford.smi.protege.event.ServerProjectStatusChangeEvent;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.Log;

/**
 * Main class for managing projects on the server side. It has support for:
 * <ul>
 * <li> loading a local or remote project</li>
 * <li> get the remote Protege server </li>
 * <li> caches opened projects </li>
 * <li> has a thread for automatically saving local projects at a set interval </li> 
 * </ul>
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ProjectManager {
	private static Logger log = Log.getLogger(ProjectManager.class);

	private static ProjectManager projectManager;	
	private MetaProjectManager metaProjectManager;

	private Map<String, ServerProject> openProjectsMap;
	
	private boolean loadOntologiesFromServer;
	private String protegeServerName;
	private int _saveIntervalMsec = edu.stanford.bmir.protege.web.server.ApplicationProperties.NO_SAVE;
	//thread that save periodically the projects for local mode
	private Thread _updateThread;
	private RemoteServer server;
	private ServerProjectListener _shutdownListener;


	private ProjectManager() {
		openProjectsMap = new HashMap<String, ServerProject>();
		loadOntologiesFromServer = ApplicationProperties.getLoadOntologiesFromServer();

		if (!loadOntologiesFromServer) {
			metaProjectManager = new LocalMetaProjectManager();
			Log.getLogger().info("WebProtege server running with local projects");
			//automatic save logic
			int saveInt = ApplicationProperties.getLocalProjectSaveInterval();
			if (saveInt != ApplicationProperties.NO_SAVE) {
				_saveIntervalMsec = saveInt * 1000;
			}
			startProjectUpdateThread();
		} else { //load ontologies from server
			protegeServerName = ApplicationProperties.getProtegeServerHostName();
			server = getServer();
			Log.getLogger().info("WebProtege server running with remote projects loaded from the Protege server: " + getProtegeServerHostName());
			metaProjectManager = new RemoteMetaProjectManager();
		}
	}

	public static ProjectManager getProjectManager() {
		if (projectManager == null) {
			projectManager = new ProjectManager();			
		}		
		return projectManager;
	}


	/*
	 * Project management methods
	 */

	public MetaProjectManager getMetaProjectManager() {
		return metaProjectManager;
	}
	
	public Project getProject(String projectName) {
		return getProject(projectName, true);
	}
	
	public Project getProject(String projectName, boolean create) {
		ServerProject serverProject = getServerProject(projectName);
		return (serverProject == null && create) ? null : serverProject.getProject();
	}

	public ServerProject getServerProject(String projectName) {
		return getServerProject(projectName, true);
	}
	
	//not the protege server project, but the webprotege server project, which can be local (confusing, no?)
	public ServerProject getServerProject(String projectName, boolean create) {
		ServerProject serverProject;
		synchronized (openProjectsMap) {
			serverProject = openProjectsMap.get(projectName);
			if (serverProject == null && create) {
				serverProject = new ServerProject();
				openProjectsMap.put(projectName, serverProject);
			}
		}
		if (create) {
			ensureProjectOpen(projectName, serverProject);
		}
		return serverProject;
	}

	private void ensureProjectOpen(String projectName, ServerProject serverProject) {
		synchronized (serverProject) {
			if (serverProject.getProject() == null) {
				Project project = loadOntologiesFromServer ? openProjectRemote(projectName) : openProjectLocal(projectName);				
				if (project == null) {
					throw new RuntimeException("Cannot open project " + projectName);
				}
				//load also ChAO KB if available
				ChAOKbManager.getChAOKb(project.getKnowledgeBase());
				HasAnnotationCache.fillHasAnnotationCache(project.getKnowledgeBase());
				serverProject.setProject(project);	
			}
		}
	}

	private Project openProjectLocal(String projectName) {		
		Project project = null;
		URI uri = ((LocalMetaProjectManager)getMetaProjectManager()).getProjectURI(projectName);
		Log.getLogger().info("Loading project " + projectName + " from " + uri);

		Collection errors = new ArrayList();
		try {
			project = Project.loadProjectFromURI(uri, errors);
			if (errors.size() > 0) {
				Log.getLogger().warning("There were errors at loading project " + projectName +
						" Errors: " + errors);
			}			
		} catch (Throwable e) {
			Log.getLogger().log(Level.WARNING, "There were exceptions at loading project " + projectName, e);
			throw new RuntimeException("Cannot open project " + projectName, e);
		}
		return project;
	}


	private Project openProjectRemote(String projectName) {
		Project project = null;
		Log.getLogger().info("Loading project " + projectName + " from Protege server " + protegeServerName);

		try {			
			project = RemoteProjectManager.getInstance().getProject(protegeServerName,
					ApplicationProperties.getProtegeServerUser(), ApplicationProperties.getProtegeServerPassword(),
					projectName, true);
			if (project != null) {
				project.getKnowledgeBase().addServerProjectListener(_shutdownListener = createRemoteProjectShutdownListener());
			}

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "There were exceptions at loading project " + projectName, e);
			throw new RuntimeException("Cannot open project " + projectName, e);
		}
		return project;
	}


	protected void closeProjectRemote(String projectName) {	
		ServerProject serverProject;
		synchronized (openProjectsMap) {		
			serverProject = openProjectsMap.remove(projectName);					
		}
		if (serverProject != null) {
			serverProject.getProject().getKnowledgeBase().removeServerProjectListener(_shutdownListener);			
			serverProject.dispose();
		}
	}

	public String getProtegeServerHostName() {
		return protegeServerName;
	}

	public RemoteServer getServer() {
		if (server == null && loadOntologiesFromServer) {			
			try {
				server = (RemoteServer) Naming.lookup("//" + protegeServerName + "/" + Server.getBoundName());
			} catch (Exception e) {
				if (Log.getLogger().isLoggable(Level.FINE)) {
					Log.getLogger().log(Level.FINE, "Could not connect to server: " + protegeServerName, e);
				}
			}
			if (server == null) {
				Log.getLogger().severe("Could not connect to server: " + protegeServerName);
			}
		}
		return server;
	}


	/**
	 * Should always be called at initialization
	 * @param realPath
	 */
	public void setRealPath(String realPath) {
		FileUtil.init(realPath);
		if (metaProjectManager instanceof LocalMetaProjectManager) {
			((LocalMetaProjectManager) getMetaProjectManager()).init(loadOntologiesFromServer);
		}
	}

	/*
	 * Saving automatically the projects - to be removed when integrated with server	 * 
	 */
	private void startProjectUpdateThread() {
		if (_saveIntervalMsec != edu.stanford.bmir.protege.web.server.ApplicationProperties.NO_SAVE) {
			_updateThread = new Thread("Save Projects") {
				public void run() {
					try {
						while (_updateThread == this) {
							sleep(_saveIntervalMsec);
							saveAllProjects();
						}
					} catch (Throwable e) {
						Log.getLogger().log(Level.INFO, "Exception caught", e);
					}
				}
			};
			_updateThread.setDaemon(true);
			_updateThread.start();
		}
	}

	//just for the local loading of ontologies
	private void saveAllProjects() {
		try {
			for (ServerProject serverProject : openProjectsMap.values()) {
				Project prj = serverProject.getProject();
				KnowledgeBase changesKb = ChAOUtil.getChangesKb(prj.getKnowledgeBase());
				if (changesKb != null && changesKb.hasChanged()) {
					save(changesKb.getProject());
					changesKb.setChanged(false);
				}
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Errors at saving server projects", e);
		}
	}

	//just for the local loading of ontologies
	private static void save(Project project) {
		Log.getLogger().info("Saving " + project);
		Collection errors = new ArrayList();
		/*
		 * The order of these synchronize statements is critical.  There is some 
		 * OWLFrameStore code (which holds the knowledgebase lock) that makes calls
		 * to the internal project knowledge base to get configuration parameters.
		 */
		synchronized (project.getKnowledgeBase()) {
			synchronized (project.getInternalProjectKnowledgeBase()) {
				/* TT: Save only the domain kb, not the prj kb.
				 * Saving the prj kb while a client opens a
				 * remote project can corrupt the client prj kb.
				 */
				KnowledgeBase kb = project.getKnowledgeBase();            	
				KnowledgeBaseFactory factory = kb.getKnowledgeBaseFactory();
				if (!(factory instanceof DatabaseKnowledgeBaseFactory)) {
					factory.saveKnowledgeBase(kb, project.getSources(), errors);
				}
			}
		}      
		dumpErrors(project, errors);
	}

	private static void dumpErrors(Project p, Collection errors) {
		if (!errors.isEmpty()) {
			Log.getLogger().warning("Unable to save project " + p);
			Iterator i = errors.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				Log.getLogger().warning("\t" + o.toString());
			}
		}
	}

	public boolean loadOntologiesFromServer() {
		return loadOntologiesFromServer;
	}


	protected ServerProjectListener createRemoteProjectShutdownListener() {
		return new ServerProjectAdapter() {
			@Override
			public void projectStatusChanged(
					ServerProjectStatusChangeEvent event) {
				ProjectStatus newStatus = event.getNewStatus();
				if (log.isLoggable(Level.FINE)) {
					log.fine("Project status changed: " + newStatus);
				}
				if (newStatus.equals(ProjectStatus.CLOSED_FOR_MAINTENANCE)) {			
					log.info("Project " + event.getProjectName() + " has been closed for maintainance on the Protege server and it is unavailable.");
					ProjectManager.getProjectManager().closeProjectRemote(event.getProjectName());
				}
			}

			@Override
			public void beforeProjectSessionClosed(ServerProjectSessionClosedEvent event) { //TODO: check implementation
				String projectName = event.getProjectName();
				ServerProject serverProject = openProjectsMap.get(projectName);
				if (serverProject != null) {
					if (RemoteClientFrameStore.getCurrentSession(serverProject.getProject().getKnowledgeBase())
							.equals(event.getSessionToKill())) {
						log.info("Session for project " + event.getProjectName() + " has been killed. This project is unavailable.");
						ProjectManager.getProjectManager().closeProjectRemote(event.getProjectName());				
					}
				}
			}
		};
	}



}

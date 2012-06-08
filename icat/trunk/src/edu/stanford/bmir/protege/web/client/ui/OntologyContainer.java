package edu.stanford.bmir.protege.web.client.ui;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.event.PanelListenerAdapter;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.ui.ontology.home.MyWebProtegeTab;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.SelectionEvent;
import edu.stanford.bmir.protege.web.client.util.SelectionListener;
import edu.stanford.bmir.protege.web.client.util.Session;

//TODO: drag-n-dop of class tree does not work fine

/**
 * Class that holds all the tabs corresponding to ontologies. It also contains that MyWebProtege Tab.
 * This class manages the loading of projects and their configurations.
 *  
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class OntologyContainer extends TabPanel {
	private Session session;
	private MyWebProtegeTab myWebProTab;
	private LinkedHashMap<String, Ontology> loadedOntologiesMap = new LinkedHashMap<String, Ontology>();
	
	public OntologyContainer(Session session) {
		super();
		this.session = session;		
		buildUI();
	}
	
	protected void buildUI() {
		setLayoutOnTabChange(false); //TODO: check if necessary
		myWebProTab = new MyWebProtegeTab();
		myWebProTab.setTitle(myWebProTab.getLabel());
		add(myWebProTab);
				
		myWebProTab.getOntologiesPortlet().addSelectionListener(new SelectionListener() { 
			public void selectionChanged(SelectionEvent event) {		
				String projectName = event.getSelectable().getSelection().iterator().next().getName();				
				loadProject(projectName);
			}
		});		
		
	}
	
	protected void loadProject(String projectName) {
		Ontology ontTab = loadedOntologiesMap.get(projectName);
		if (ontTab != null) {
			activate(ontTab.getId());
			return;
		}
		
		Project project = new Project(session, myWebProTab.getOntologiesPortlet().getOntologyData(projectName));		
		UIUtil.showLoadProgessBar("Loading " + project.getProjectName(), "Loading");
		OntologyServiceManager.getInstance().loadProject(projectName, new LoadProjectHandler(project));		
	}

	protected void getProjectConfiguration(Project project) {
		UIUtil.showLoadProgessBar("Loading " + project.getProjectName() + " configuration", "Loading");
		ProjectConfigurationServiceManager.getInstance().getProjectConfiguration(project.getProjectName(),
				project.getSession().getUserName(), new GetProjectConfigurationHandler(project));
	}
	
	public void addTab(Project project) {
		Ontology ont = new Ontology(project);
		ont.setClosable(true);
		loadedOntologiesMap.put(project.getProjectName(), ont);		
		
		ont.addListener(new PanelListenerAdapter() {			
			@Override
			public boolean doBeforeDestroy(Component component) {				
				if (component instanceof Ontology) {
					Ontology o = (Ontology) component;			
					loadedOntologiesMap.remove(o.getProject().getProjectName());
					o.getProject().dispose();
					hideTabStripItem(o);
					o.hide();
					activate(0);
				}
				return true;
			}
		});
		
		add(ont);
		activate(ont.getId());
	}
	

	protected void layoutProject(Project project) {
		Ontology ontTab = loadedOntologiesMap.get(project.getProjectName());
		if (ontTab == null) {
			GWT.log("Could not find ontology tab for " + project.getProjectName(), null);
			return;
		}
		ontTab.layoutProject();
	}

	
	/*
	 * Remote calls
	 */
	class LoadProjectHandler extends AbstractAsyncHandler<Integer> {
		private Project project;		

		public LoadProjectHandler(Project project) {			
			this.project = project;			
		}

		public void handleFailure(Throwable caught) {			
			GWT.log("There were errors at loading project " + project.getProjectName(), caught);
			UIUtil.hideLoadProgessBar();
			MessageBox.alert("Load project " + project.getProjectName() + " failed.<br>" +
					" Message: " + caught.getMessage());			
		}

		public void handleSuccess(Integer Version) {
			int serverVersion = Version.intValue();
			project.setServerVersion(serverVersion);
			UIUtil.hideLoadProgessBar();			
			getProjectConfiguration(project);
			addTab(project);
		}
	}

	
	class GetProjectConfigurationHandler extends AbstractAsyncHandler<ProjectConfiguration> {
		private Project project;
		
		public GetProjectConfigurationHandler(Project project) {
			this.project = project;
		}
		
		public void handleFailure(Throwable caught) {			
			GWT.log("There were errors at loading project configuration for " + project.getProjectName(), caught);
			UIUtil.hideLoadProgessBar();			
			Window.alert("Load project configuration for " + project.getProjectName() + " failed.<br>" +
					" Message: " + caught.getMessage());
		}

		public void handleSuccess(ProjectConfiguration config) {			
			project.setProjectConfiguration(config);
			layoutProject(project);
			UIUtil.hideLoadProgessBar();
		}		
	}
	
}

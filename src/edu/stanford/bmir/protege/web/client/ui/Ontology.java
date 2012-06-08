package edu.stanford.bmir.protege.web.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarMenuButton;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.WindowListenerAdapter;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.TextFieldListenerAdapter;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.CheckItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;
import com.gwtext.client.widgets.menu.event.CheckItemListenerAdapter;
import com.gwtext.client.widgets.portal.Portlet;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabColumnConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabConfiguration;
import edu.stanford.bmir.protege.web.client.ui.generated.UIFactory;
import edu.stanford.bmir.protege.web.client.ui.login.LoginFormPanel;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassesTab;
import edu.stanford.bmir.protege.web.client.ui.portlet.EntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.search.SearchGridPanel;
import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.ui.tab.UserDefinedTab;
import edu.stanford.bmir.protege.web.client.util.Project;


/**
 * The main user interface for displaying one ontology.
 * 
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class Ontology extends TabPanel {
	protected Project project;

	private Map<String, String> shortToLongPortletNameMap;	
	private Map<String, String> shortToLongTabNameMap;	

	protected ToolbarTextItem ontologyNameItem;
	protected ToolbarButton loginoutButton;	

	private List<AbstractTab> tabs;

	private TextField searchField;


	public Ontology(Project project) {
		super();
		this.project = project;

		setTitle(getLabel());
		setTopToolbar(new Toolbar());	
		buildUI();		
	}


	public void buildUI() {
		tabs = new ArrayList<AbstractTab>();	

		loginoutButton = new ToolbarButton("<font color='#1542bb'><b><u>Login</u></b></font> for more features.");
		loginoutButton.addListener( new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {
				String userName = project.getSession().getUserName();
				if (userName.equals("No user")) {
					onLogin();
				} else {
					onLogout();
				}
			}
		});

		String userName = project.getSession().getUserName();
		ontologyNameItem = new ToolbarTextItem((userName.equals("No user") ? "" :				
			"Logged-in as <i><b>" + userName + "</b></i>"));

		setActiveTab(0);
	}	

	public void layoutProject() {
		List<AbstractTab> tabs = project.getLayoutManager().createTabs(project.getProjectConfiguration());
		for (AbstractTab tab : tabs) {
			addTab(tab);
		}
		if (tabs.size() > 0) {
			activate(0);
		}
		createToolbarButtons();
		adjustToolbarText();

		//doLayout();
	}

	protected void addTab(AbstractTab tab) {
		tabs.add(tab);
		tab.setClosable(true);
		add(tab);
	}


	protected void adjustToolbarText() {
		String userName = project.getSession().getUserName();
		ontologyNameItem.setText("Ontology: " + project.getProjectName() +  ".  " + 
				(userName.equals("No user") ? "" :					
					"Logged-in as <i><b>" + userName + "</b></i>"));
		loginoutButton.setText(userName.equals("No user") ? 
				"<font color='#1542bb'><b><u>Login</u></b></font> for more features." :
		"<font color='#1542bb'><b><u>Logout</u></b></font>");
	}


	protected void createToolbarButtons() {
		Toolbar toolbar = getTopToolbar();		
		toolbar.addItem(ontologyNameItem);
		toolbar.addSpacer();				
		toolbar.addText("&nbsp<i>Search</i>:&nbsp&nbsp");
		toolbar.addElement(getSearchField().getElement());
		toolbar.addSpacer();
		toolbar.addSpacer();
		toolbar.addButton(loginoutButton);
		toolbar.addSpacer();
		toolbar.addButton(getSaveConfigButton());	
		//toolbar.addButton(getSearchButton());
		toolbar.addFill();		
		toolbar.addButton(getAddPortletButton());		
		toolbar.addSpacer();
		toolbar.addButton(getAddTabButton());
	}


	protected Field getSearchField() {
		searchField = new TextField("Search: ", "search", 100);
		searchField.addListener(new TextFieldListenerAdapter() {			
			@Override
			public void onSpecialKey(Field field, EventObject e) {
				if (e.getKey() == EventObject.ENTER) {
					onSearch();
				}
			}
		});		
		return searchField;
	}
	
	protected ToolbarButton getSearchButton() {
		ToolbarButton searchButton = new ToolbarButton("Search");
		searchButton.addListener( new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {
				onSearch();
			}
		});
		return searchButton;
	}

	
	private void onSearch() {
		String searchText = searchField.getValueAsString().trim();
		search(searchText);
	}
	
	
	protected ToolbarMenuButton getAddPortletButton(){
		shortToLongPortletNameMap = UIFactory.getAvailablePortletNameMap();

		Menu addPortletMenu = new Menu();
		for (String portletName : shortToLongPortletNameMap.keySet()) {
			Item item = new Item(portletName);
			addPortletMenu.addItem(item);
			item.addListener(new BaseItemListenerAdapter() {
				@Override
				public void onClick(BaseItem item, EventObject e) {
					String javaClassName = shortToLongPortletNameMap.get(((Item)item).getText());
					onPortletAdded(javaClassName);
				}			
			});
		}
		ToolbarMenuButton addPortletButton = new ToolbarMenuButton("Add content to this tab", addPortletMenu);
		addPortletButton.setIcon("images/portlet_add.gif");         
		return addPortletButton;
	}


	protected void onPortletRemoved(String javaClassName) {		
		AbstractTab activeTab = (AbstractTab)getActiveTab();
		List<EntityPortlet> comps = activeTab.getPortlets();
		for (Iterator<EntityPortlet> iterator = comps.iterator(); iterator.hasNext();) {
			EntityPortlet entityPortlet = (EntityPortlet) iterator.next();
			if (entityPortlet.getClass().getName().equals(javaClassName)) {
				((Portlet)entityPortlet).setVisible(false);
				((Portlet)entityPortlet).destroy();
			}
		}		
	}


	protected void onPortletAdded(String javaClassName) {
		EntityPortlet portlet = UIFactory.createPortlet(project, javaClassName);
		if (portlet == null) { return; }
		AbstractTab activeTab = (AbstractTab)getActiveTab();
		activeTab.addPortlet(portlet, activeTab.getColumnCount() - 1);
		doLayout();
	}


	protected ToolbarMenuButton getAddTabButton() {
		shortToLongTabNameMap = UIFactory.getAvailableTabNameMap();	

		List<String> enabledTabs = new ArrayList<String>();
		for (AbstractTab tab : tabs) {
			enabledTabs.add(tab.getClass().getName());
		}

		Menu addTabMenu = new Menu();
		for (String tabName : shortToLongTabNameMap.keySet()) {
			CheckItem item = new CheckItem(tabName, enabledTabs.contains(shortToLongTabNameMap.get(tabName)));
			addTabMenu.addItem(item);
			item.addListener(new CheckItemListenerAdapter() {
				@Override
				public void onCheckChange(CheckItem item, boolean checked) {
					String javaClassName = shortToLongTabNameMap.get(item.getText());
					if (checked) {
						onTabAdded(javaClassName);
					} else {
						onTabRemoved(javaClassName);
					}
				}				
			});		
		}

		//Add the "Add user defined tab"
		addTabMenu.addSeparator();
		Item item = new Item("Add your own tab");
		addTabMenu.addItem(item);	
		item.addListener(new BaseItemListenerAdapter() {
			@Override
			public void onClick(BaseItem item, EventObject e) {
				onUserDefinedTabAdded();
			}
		});

		ToolbarMenuButton addTabButton = new ToolbarMenuButton("Add tab", addTabMenu);
		addTabButton.setIcon("images/tab_add.gif");
		return addTabButton;
	}

	protected void onUserDefinedTabAdded() {
		final Window window = new Window();  
		window.setTitle("Create your own tab");		
		window.setClosable(true);				
		window.setPaddings(7);
		window.setCloseAction(Window.HIDE);
		window.add(new CreateUserDefinedTabForm(window));
		window.show();
	}


	protected void onTabRemoved(String javaClassName) {
		AbstractTab tab = getTabByClassName(javaClassName);
		if (tab == null) { return; }
		project.getLayoutManager().removeTab(tab);
		tabs.remove(tab);
		hideTabStripItem(tab);
		remove(tab);
		tab.hide();		
		doLayout();
	}


	protected void onTabAdded(String javaClassName) {
		AbstractTab tab = project.getLayoutManager().addTab(javaClassName);
		if (tab == null) { return; }
		addTab(tab);
		//doLayout();
	}

	public AbstractTab getTabByClassName(String javaClassName) {
		for (AbstractTab tab : tabs) {
			if (tab.getClass().getName().equals(javaClassName)) {
				return tab;
			}
		}
		return null;
	}


	protected ToolbarButton getSaveConfigButton() {
		ToolbarButton saveConfigButton = new ToolbarButton("<font color='#1542bb'><b><u>Save Layout</u></b></font>");
		saveConfigButton.addListener( new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {
				saveProjectConfiguration();
			}
		});
		return saveConfigButton;
	}


	protected void saveProjectConfiguration() {
		if (project.getUserName() == null || project.getUserName().equals("No user")) {
			MessageBox.alert("Not logged in", "To save the layout, you need to login first.");
			return;
		}
		ProjectConfiguration config = project.getProjectConfiguration();
		config.setOntologyName(project.getProjectName());
		ProjectConfigurationServiceManager.getInstance().saveProjectConfiguration(project.getProjectName(), project.getSession().getUserName(), config, new SaveConfigHandler());		
	}


	protected void search(String text) {		
		OntologyServiceManager.getInstance().search(project.getProjectName(), text, new SearchHandler());		
	}	

	
	public String getLabel() {
		return project.getProjectName();
	}

	private void onLogin() {
		final Window window = new Window();  
		window.setTitle("Sign in to WebProt\u00E9g\u00E9");
		window.setAnimateTarget(loginoutButton.getButtonElement());		
		window.setClosable(true);				
		window.setPaddings(7);
		window.setCloseAction(Window.HIDE);
		window.add(new LoginFormPanel(window, project));
		window.show();

		window.addListener(new WindowListenerAdapter() {
			@Override
			public void onClose(Panel panel) {
				adjustToolbarText();
				super.onClose(panel);
			}
			@Override
			public void onHide(Component component) {
				refresh();
				super.onHide(component);
			}

			protected void refresh() {
				adjustToolbarText();
				//TODO: reload project layout
			}			
		});				
	}

	private void onLogout() {
		MessageBox.confirm("Log out", "Are you sure you want to log out?",
				new MessageBox.ConfirmCallback() {
			public void execute(String btnID) {
				project.getSession().setUserName("No user");
				refresh();
			}

			protected void refresh() {
				adjustToolbarText();
				//TODO: reload project layout
			}
		});
	}

	public Project getProject() {
		return project;
	}


	/*
	 * Remote calls
	 */

	class SaveConfigHandler extends AbstractAsyncHandler<Boolean> {
		@Override
		public void handleFailure(Throwable caught) {			
			GWT.log("Error in saving configurations (UI Layout)", caught);	
			MessageBox.alert("There were problems in saving UI Layout. Please try again later");
		}

		@Override
		public void handleSuccess(Boolean result) {			
			MessageBox.alert("Layout saved successfully.");			
		}		
	}


	class SearchHandler extends AbstractAsyncHandler<ArrayList<EntityData>> {
		@Override
		public void handleFailure(Throwable caught) {			
			GWT.log("Error at search", caught);	
			MessageBox.alert("There were problems at search. Please try again later");
		}

		@Override
		public void handleSuccess(ArrayList<EntityData> results) {			
			GWT.log("Search results: " + results, null);
			if (results == null || results.isEmpty()) {
				MessageBox.alert("No results", "Empty search results. Please try a different query.");
				return;
			}			
			showSearchResults(results);
		}		
	}


	private void showSearchResults(ArrayList<EntityData> results) {
		
        final Window window = new Window();
        window.setTitle("Search results");       

        FormPanel panel = new FormPanel();
        panel.setWidth(300);
        
        final SearchGridPanel searchGrid = new SearchGridPanel(results);
        searchGrid.setAutoScroll(true); 

        searchGrid.addGridRowListener(new GridRowListenerAdapter() {
        	public void onRowClick(GridPanel grid, int rowIndex, EventObject e) {
        	}
        	public void onRowDblClick(GridPanel grid, int rowIndex, EventObject e) {        		
        	}
        });

        Button showInTreeButton = new Button("Select in tree", 
				new ButtonListenerAdapter() {
        	public void onClick(Button button, EventObject e) {
        		EntityData selection = searchGrid.getSelection();
        		if (selection == null) { return; }
				ClassesTab tab = (ClassesTab) getTabByClassName(ClassesTab.class.getName());
				if (tab != null) {
					ClassTreePortlet portlet = (ClassTreePortlet) tab.getPortletByClassName(ClassTreePortlet.class.getName());
					Collection<EntityData> selectionCollection = new ArrayList<EntityData>();
					selectionCollection.add(selection);
					portlet.setSelection(selectionCollection);
				}				
        	}       	
		});
        
        Button closeButton = new Button("Close", new ButtonListenerAdapter(){
        	@Override
        	public void onClick(Button button, EventObject e) {
        		window.close(); //TODO: cancel existing search
        	}
        });

        panel.add(searchGrid); //, new BorderLayoutData(RegionPosition.CENTER));
        panel.addButton(showInTreeButton); //n, new BorderLayoutData(RegionPosition.SOUTH));
        panel.addButton(closeButton);
        
        window.add(panel);
        window.show();
				
	}	


	/*
	 * Internal class
	 */

	class CreateUserDefinedTabForm extends FormPanel {
		private Window parent;
		private TextField colNoTextField;
		private int colNo;
		private TextField[] colTextFields = new TextField[10];
		private TextField labelTextField;

		public CreateUserDefinedTabForm(Window parent) {
			super();
			this.parent = parent;
			
			setWidth(400);			
			setLabelWidth(150);
			setPaddings(15);

			labelTextField = new TextField("Tab Label", "label");
			labelTextField.setValue("New Tab");
			add(labelTextField);

			colNoTextField = new TextField("Number of columns", "noCols");
			colNoTextField.setValue("2");
			add(colNoTextField);
			colNoTextField.addListener(new TextFieldListenerAdapter() {
				@Override
				public void onChange(Field field, Object newVal, Object oldVal) {
					showColFields();
				}				
			});			

			showColFields();
			
			Button createButton = new Button("Create", new ButtonListenerAdapter() {
				public void onClick(Button button, EventObject e) { 
					AbstractTab tab = createTab();
					addTab(tab);
					CreateUserDefinedTabForm.this.parent.close();
					activate(tab.getId());					
				}
			});
			
			addButton(createButton);
		}

		private void showColFields() {
			removeExistingFields();
			String noOfColsStr = colNoTextField.getValueAsString();			
			try {
				colNo = Integer.parseInt(noOfColsStr);	
			} catch (NumberFormatException e) {	}

			int defaultWidth = colNo == 0 ? 100 : 100/colNo;
			String defaultWidthStr = new Integer(defaultWidth).toString();
			for (int i = 0; i < colNo; i++) {
				colTextFields[i] = new TextField("Width column " + i + " (%)", "col" + i);
				colTextFields[i].setValue(defaultWidthStr);
				add(colTextFields[i]);
			}
			doLayout();

		}

		private void removeExistingFields() {
			for(int i = 0; i < colNo; i++) {
				TextField tf = colTextFields[i];
				if (tf != null) { 
					remove(tf);
					colTextFields[i] = null;
				}
			}
			doLayout();
		}		

		public AbstractTab createTab() {
			UserDefinedTab userDefinedTab = new UserDefinedTab(project);		
			TabConfiguration userDefinedTabConfiguration = getUserDefinedTabConfiguration();
			project.getLayoutManager().setupTab(userDefinedTab, userDefinedTabConfiguration);
			project.getProjectConfiguration().addTab(userDefinedTabConfiguration);			
			return userDefinedTab;
		}
		
		private TabConfiguration getUserDefinedTabConfiguration() {
			TabConfiguration tabConfiguration = new TabConfiguration();
			tabConfiguration.setLabel(labelTextField.getValueAsString().trim());
			List<TabColumnConfiguration> tabColConf = new ArrayList<TabColumnConfiguration>(colNo);
			//TabColumnConfiguration[] tabColConf = new TabColumnConfiguration[colNo];
			for(int i = 0; i < colNo; i++) {
				TabColumnConfiguration tabCol = new TabColumnConfiguration();
				String widthStr = colTextFields[i].getValueAsString();
				int width = 0;
				try {
					width = Integer.parseInt(widthStr);
				} catch (NumberFormatException e) {	}
				//tabColConf[i].setWidth(width == 0 ? 0 : 1/width);
				tabCol.setWidth(width == 0 ? 0 : (float) width/100);
				tabColConf.add(tabCol);
			}
			tabConfiguration.setColumns(tabColConf);
			tabConfiguration.setName(UserDefinedTab.class.getName());
			return tabConfiguration;
		}
	}

}

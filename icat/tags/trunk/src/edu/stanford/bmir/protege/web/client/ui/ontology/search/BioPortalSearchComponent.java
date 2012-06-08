package edu.stanford.bmir.protege.web.client.ui.ontology.search;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.data.XmlReader;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.TextFieldListenerAdapter;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.util.Project;

public class BioPortalSearchComponent extends GridPanel {

	//private static String DEFAULT_BIOPORTAL_VISUALIZE_URL = "http://bioportal.bioontology.org/visualize/";
	private static String DEFAULT_BIOPORTAL_VISUALIZE_URL = "http://stage.bioontology.org/visualize/";
	//private static String DEFAULT_BIOPORTAL_ONTOLOGY_URL = "http://bioportal.bioontology.org/ontologies/";
	private static String DEFAULT_BIOPORTAL_ONTOLOGY_URL = "http://stage.bioontology.org/ontologies/";
	//private static String DEFAULT_BIOPORTAL_SEARCH_URL_PREFIX = "http://rest.bioontology.org/bioportal/search/"; //production
	private static String DEFAULT_BIOPORTAL_SEARCH_URL = "http://ncbo-core-stage1.stanford.edu/bioportal/search/"; //stage
	private static String DEFAULT_BIOPORTAL_SEARCH_ONE_PAGE_OPTION = "pagesize=10&pagenum=1";
	
	private static String BIOPORTAL_RECORD_TYPE_PREFERRED_NAME = "RECORD_TYPE_PREFERRED_NAME";

	private static String CONFIG_PROPERTY_CREATE_REFERENCE_AS_CLASS = "create_reference_as_class";
	private static String CONFIG_PROPERTY_REFERENCE_CLASS = "reference_class";
	private static String CONFIG_PROPERTY_REFERENCE_PROPERTY = "reference_property";
	private static String CONFIG_PROPERTY_BIOPORTAL_BASE_URL = "bioportal_base_url";
	private static String CONFIG_PROPERTY_BIOPORTAL_SEARCH_URL = "bioportal_search_url";
	private static String CONFIG_PROPERTY_SEARCH_ONTOLOGY_IDS = "search_ontology_ids";
	private static String CONFIG_PROPERTY_SEARCH_ONE_PAGE_OPTION = "search_one_page_option";
	
	public static final String BP_ONTOLOGY_STR = "ontologies";
	public static final String BP_VISUALIZE_STR = "visualize";

	protected Project project;
	protected Map<String, Object> configPropertiesMap;
	protected EntityData _currentEntity;
	protected Store store;
	protected boolean searchAll = false;
	protected TextField searchStringTextField;
	protected ToolbarButton searchButton;
	protected ToolbarButton searchAllButton;
	protected ToolbarTextItem searchCountText;
	protected boolean ignoreSearchAllPressed; 

	public BioPortalSearchComponent(Project project) {
		this.project = project;
		createGrid();
	}

	public void setConfigProperties(Map<String, Object> configPropertiesMap) {
		if (configPropertiesMap != null) {
			this.configPropertiesMap = configPropertiesMap;
		}
		else {
			GWT.log("The argument passed to setConfigurationProperties should not be null!", new NullPointerException("configPropertiesMap is null"));
			this.configPropertiesMap = new HashMap<String, Object>();
		}
	}
	
	private void createGrid() {								
		XmlReader reader = new XmlReader("searchBean", new RecordDef(
				new FieldDef[]{
						new StringFieldDef("contents"),
						new StringFieldDef("recordType"),
						new StringFieldDef("ontologyDisplayLabel"),
						new StringFieldDef("ontologyVersionId"),
						new StringFieldDef("conceptIdShort"),
				}
		));

		store = new Store(reader);                 

		//setup column model         
		ColumnConfig contentsCol = new ColumnConfig("Contents", "contents");
		contentsCol.setId("contents");
		ColumnConfig recordTypeCol = new ColumnConfig("Found in", "recordType");
		ColumnConfig ontologyCol = new ColumnConfig("Ontology", "ontologyDisplayLabel");
		ColumnConfig importCol = new ColumnConfig(" ", "importLink");

		recordTypeCol.setWidth(70);
		ontologyCol.setWidth(150);
		importCol.setWidth(60);
		
		contentsCol.setRenderer(new Renderer() {
			public String render(Object value, CellMetadata cellMetadata,
					Record record, int rowIndex, int colNum, Store store) {
				return "<a href= \"" + getBioPortalVisualizeURL() + record.getAsString("ontologyVersionId") + "/" +
				record.getAsString("conceptIdShort") + "\" target=_blank>" + record.getAsString("contents") + "</a>";
			}        	 
		});

		recordTypeCol.setRenderer(new Renderer() {
			public String render(Object value, CellMetadata cellMetadata,
					Record record, int rowIndex, int colNum, Store store) {
				String type = record.getAsString("recordType");
				if (type.equals(BIOPORTAL_RECORD_TYPE_PREFERRED_NAME)) {
					return "Preferred Name";
				} else {
					return "Name";
				}
			}        	 
		});
		recordTypeCol.setHidden(true);

		ontologyCol.setRenderer(new Renderer() {
			public String render(Object value, CellMetadata cellMetadata,
					Record record, int rowIndex, int colNum, Store store) {
				return "<a href= \"" + getBioPortalOntologyURL() + record.getAsString("ontologyVersionId") +
				"\" target=_blank>" + record.getAsString("ontologyDisplayLabel") + "</a>";
			}
		});                  

		importCol.setRenderer(new Renderer() {
			public String render(Object value, CellMetadata cellMetadata,
					final Record record, int rowIndex, int colNum, Store store) {
				// the return string may contain ONLY ONE HTML TAG before the text, 
				// otherwise GridCellListener would not receive the onClick event!
				return "<DIV style=\"color:#1542bb;text-decoration:underline;font-weight:bold\">Import</DIV>";
			}
		});
		importCol.setSortable(false);
		
		ColumnConfig[] columnConfigs = {
				contentsCol,
				recordTypeCol,                 
				ontologyCol,
				importCol
		};

		ColumnModel columnModel = new ColumnModel(columnConfigs);
		columnModel.setDefaultSortable(true);
		
		setHeight(200);
		setStore(store);
		setColumnModel(columnModel);      
		setAutoWidth(true);
		stripeRows(true);
		setAutoExpandColumn("contents");    
		
		addGridCellListener(new GridCellListenerAdapter() {
			public void onCellClick(GridPanel grid, int rowIndex, int colindex, 
					EventObject e) {			
				if (grid.getColumnModel().getDataIndex(colindex).equals("importLink")) {
					Record record = grid.getStore().getAt(rowIndex);
					onImport(record);
				}			
			}
		});		

		searchStringTextField = new TextField();
		searchStringTextField.addListener(new TextFieldListenerAdapter () {
			@Override
			public void onSpecialKey(Field field, EventObject e) {
				if (e.getKey() == EventObject.ENTER) {
					reload();
				}
			}
		});
		searchStringTextField.setWidth(250);
		
		searchButton = new ToolbarButton("<font color='#1542bb'><b><u>Search in BioPortal</u></b></font>");
		searchButton.addListener(new ButtonListenerAdapter () {
			@Override
			public void onClick(Button button, EventObject e) {
				reload();
			}
		});

		Toolbar topToolbar = new Toolbar();
		topToolbar.addText("&nbsp<i>Search for concept</i>:&nbsp&nbsp");
		topToolbar.addElement(searchStringTextField.getElement());
		topToolbar.addSpacer();
		topToolbar.addButton(searchButton);
		setTopToolbar(topToolbar);

		searchAllButton = new ToolbarButton("Show all search results");
		searchAllButton.setEnableToggle(true);
		searchAllButton.addListener(new ButtonListenerAdapter () {
			@Override
			public void onToggle(Button button, boolean pressed) {
				searchAll = pressed;
				if (!ignoreSearchAllPressed) {
					reload();
				}
			}
		});
		
		searchCountText = new ToolbarTextItem("No results");
		
		Toolbar toolbar = new Toolbar();
		toolbar.addItem(searchCountText);
		toolbar.addFill();
		toolbar.addButton(searchAllButton);
		setBottomToolbar(toolbar);
				
	}


	private void onImport(Record record) {
		String ontologyVersionId = record.getAsString("ontologyVersionId");
		String conceptId = record.getAsString("conceptIdShort");
		String ontologyName = record.getAsString("ontologyDisplayLabel");
		String preferredName = record.getAsString("contents");//TODO check if preferred name is always the same as contents. Otherwise, read the preferred_name, too 
		importReference(ontologyVersionId, conceptId, ontologyName, preferredName);
	}


	public EntityData getEntity() {
		return _currentEntity;
	}
	
	public void setEntity(EntityData newEntity) {		
		if (_currentEntity != null &&_currentEntity.equals(newEntity)) {
			return;
		}
		searchAll = false;
		ignoreSearchAllPressed = true;
		searchAllButton.setPressed(false);
		ignoreSearchAllPressed = false;
		searchCountText.setText("No results");
		_currentEntity = newEntity;		
		
		searchStringTextField.setValue(_currentEntity.getBrowserText());
		reload();
	}

	protected void reload() { 
		store.removeAll();
		
		if (searchStringTextField.getText() != null) {
			getEl().mask("Loading search results", true);
			BioPortalSearchData bpSearchData = new BioPortalSearchData();
			assert configPropertiesMap != null : "configPropertiesMap should have been initialized!";
			bpSearchData.setBpSearchUrl(getBioPortalSearchURL());
			bpSearchData.setSearchOntologyIds((String) configPropertiesMap.get(CONFIG_PROPERTY_SEARCH_ONTOLOGY_IDS));
			bpSearchData.setSearchPageOption(getBioPortalSearchPageOption(searchAll));
			OntologyServiceManager.getInstance().getBioPortalSearchContent(project.getProjectName(), 
					searchStringTextField.getText(), bpSearchData, new GetSearchURLContentHandler());
		}
	}	

	private String getBioPortalOntologyURL() {
		String res = DEFAULT_BIOPORTAL_ONTOLOGY_URL;
		if (configPropertiesMap != null) {
			res = (String) configPropertiesMap.get(CONFIG_PROPERTY_BIOPORTAL_BASE_URL);
			if (res != null) {
				res = res + BP_ONTOLOGY_STR + "/";
			}
			else {
				res = DEFAULT_BIOPORTAL_ONTOLOGY_URL;
			}
		}
		
		return res;
	}

	private String getBioPortalVisualizeURL() {
		String res = DEFAULT_BIOPORTAL_VISUALIZE_URL;
		if (configPropertiesMap != null) {
			res = (String) configPropertiesMap.get(CONFIG_PROPERTY_BIOPORTAL_BASE_URL);
			if (res != null) {
				res = res + BP_VISUALIZE_STR + "/";
			}
			else {
				res = DEFAULT_BIOPORTAL_VISUALIZE_URL;
			}
		}
		
		return res;
	}
	
	private String getBioPortalSearchURL() {
		String res = DEFAULT_BIOPORTAL_SEARCH_URL;
		if (configPropertiesMap != null) {
			res = (String) configPropertiesMap.get(CONFIG_PROPERTY_BIOPORTAL_SEARCH_URL);
			if (res != null) {
				//TODO activate this see if we decide to use the BASE url above, also for the search
				//res = res + BP_SEARCH_STR + "/";  
			}
			else {
				res = DEFAULT_BIOPORTAL_SEARCH_URL;
			}
		}
		
		return res;
	}
	
	private String getBioPortalSearchPageOption(boolean all) {
		if (all) {
			//do not restrict search pages
			return null;
		}
		String res = DEFAULT_BIOPORTAL_SEARCH_ONE_PAGE_OPTION;
		if (configPropertiesMap != null) {
			res = (String) configPropertiesMap.get(CONFIG_PROPERTY_SEARCH_ONE_PAGE_OPTION);
			if (res == null) {
				res = DEFAULT_BIOPORTAL_SEARCH_ONE_PAGE_OPTION;
			}
		}
		
		return res;
	}
	
	private void importReference(String ontologyVersionId, String conceptId, String ontologyName, String preferredName) {
		GWT.log("onImportReference", null);
		//MessageBox.alert("Importing from " + getBioPortalOntologyURL() + ontologyVersionId);
		BioPortalReferenceData bpRefData = new BioPortalReferenceData();
		assert configPropertiesMap != null : "configPropertiesMap should have been initialized!";
		bpRefData.setCreateAsClass((Boolean) configPropertiesMap.get(CONFIG_PROPERTY_CREATE_REFERENCE_AS_CLASS));
		bpRefData.setReferenceClassName((String) configPropertiesMap.get(CONFIG_PROPERTY_REFERENCE_CLASS));
		bpRefData.setReferencePropertyName((String) configPropertiesMap.get(CONFIG_PROPERTY_REFERENCE_PROPERTY));
		bpRefData.setBpBaseUrl((String) configPropertiesMap.get(CONFIG_PROPERTY_BIOPORTAL_BASE_URL));
		bpRefData.setConceptId(conceptId);
		bpRefData.setOntologyVersionId(ontologyVersionId);
		bpRefData.setOntologyName(ontologyName);
		bpRefData.setPreferredName(preferredName);
		OntologyServiceManager.getInstance().importBioPortalConcept(project.getProjectName(), 
				_currentEntity.getName(), bpRefData, getImportBioPortalConceptHandler());
	}							

	class GetSearchURLContentHandler extends AbstractAsyncHandler<String> {
		@Override
		public void handleFailure(Throwable caught) {
			getEl().unmask();
			GWT.log("Could not retrive BioPortal search results for " + _currentEntity, null);			
		}

		@Override
		public void handleSuccess(String searchXml) {
			getEl().unmask();			
			store.loadXmlData(searchXml, true);
			searchCountText.setText(store.getTotalCount() + " results.");
		}
	}
	
	protected AbstractAsyncHandler<Boolean> getImportBioPortalConceptHandler() {
		return new ImportBioPortalConceptHandler();
	}
	
	class ImportBioPortalConceptHandler extends AbstractAsyncHandler<Boolean> {
		@Override
		public void handleFailure(Throwable caught) {
			getEl().unmask();
			GWT.log("Could not import BioPortal concept for " + _currentEntity, null);			
			MessageBox.alert("Import operation failed!");
		}
		
		@Override
		public void handleSuccess(Boolean success) {
			getEl().unmask();			
 			MessageBox.alert(success ? "Import operation done!" : "Import operation DID NOT SUCCEDED!");
			
		}
	}
}

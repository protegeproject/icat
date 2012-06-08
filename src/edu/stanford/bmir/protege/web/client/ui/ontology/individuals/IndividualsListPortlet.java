package edu.stanford.bmir.protege.web.client.ui.ontology.individuals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.ObjectFieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridRowListener;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.SelectionEvent;

/**
 * Portlet for showing a list of individuals. The list is filled with the instances
 * of the class given as argument to <code>setEntity</code> method. 
 * Normally, it is used together with the class tree
 * portlet. The portlet can also be configured in the configuration file to
 * show always only the instances of a certain class by setting a property of the
 * portlet <code>showOnlyClass</code> to point to a class.
 *
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class IndividualsListPortlet extends AbstractEntityPortlet{
	
	private static final String PRECONFIGURED_CLASS = "showOnlyClass";
	
	protected GridPanel individualsGrid;	
	protected RecordDef recordDef;
	protected Store store;
	protected GridRowListener gridRowListener;
	protected ArrayList<EntityData> currentSelection;
	/*
	 * Retrieved from the project configuration. If it is set,
	 * then the individuals list will always display the instances
	 * of the preconfigured class. 
	 */
	protected EntityData preconfiguredClass; 
	
	public IndividualsListPortlet(Project project) {
		super(project);
	}

	public void intialize() {
		setLayout(new FitLayout());
		setTitle("Individuals");
			
		createGrid();	
		add(individualsGrid);		
		individualsGrid.addGridRowListener(getRowListener());
		
		initConfiguration();
		if (preconfiguredClass != null) {
			setEntity(preconfiguredClass);
		}
	}

	@Override
	public void setPortletConfiguration(
			PortletConfiguration portletConfiguration) {	
		super.setPortletConfiguration(portletConfiguration);
		initConfiguration();
		if (preconfiguredClass != null) {
			setEntity(preconfiguredClass);
		}
	}
	
	
	private void initConfiguration() {
		PortletConfiguration config = getPortletConfiguration();
		if (config == null) { return; }
		Map<String, Object> properties = config.getProperties();
		if (properties == null) { return; }
		String preconfiguredClassName = (String) properties.get(PRECONFIGURED_CLASS);
		preconfiguredClass = new EntityData(preconfiguredClassName);		
	}

	public void reload() {
		if (_currentEntity != null) {
			setTitle("Individuals for " + _currentEntity);
		} 		
		setEntity(null);
		setEntity(_currentEntity);
	}


	public void setEntity(EntityData newEntity) {
		if (preconfiguredClass != null) { newEntity = preconfiguredClass; }
		
		if (_currentEntity != null &&_currentEntity.equals(newEntity)) { return; }
		
		if (_currentEntity != null) {
			store.removeAll();
		}
		_currentEntity = newEntity;
		
		if (_currentEntity == null) { return; }
		
		OntologyServiceManager.getInstance().getIndividuals(project.getProjectName(), _currentEntity.getName(), new GetIndividuals());	
	}

	protected GridRowListener getRowListener() {
		if (gridRowListener == null) {
			gridRowListener = new GridRowListenerAdapter() {
				public void onRowClick(GridPanel grid, int rowIndex, EventObject e) {
					EntityData property = (EntityData) store.getAt(rowIndex).getAsObject("individuals");
					currentSelection = new ArrayList<EntityData>();
					currentSelection.add(property);
					notifySelectionListeners(new SelectionEvent(IndividualsListPortlet.this));
					
					super.onRowClick(grid, rowIndex, e);
				}
			};
		}		
		return gridRowListener;
	}
	
	
	protected void createGrid() {		
		recordDef = new RecordDef(
				new FieldDef[]{	new ObjectFieldDef("individuals")}
		);

		individualsGrid = new GridPanel();
		createColumns();

		individualsGrid.setHeight(590);
		individualsGrid.setAutoWidth(true);		
		individualsGrid.setAutoExpandColumn("individuals");

		ArrayReader reader = new ArrayReader(recordDef);		 
		MemoryProxy dataProxy = new MemoryProxy(new Object[][]{});
		store = new Store(dataProxy, reader);
		store.load();
		individualsGrid.setStore(store);		
	}

	protected void createColumns() {
		ColumnConfig indCol = new ColumnConfig();		
		indCol.setId("individuals");
		indCol.setDataIndex("individuals");
		indCol.setResizable(true);
		indCol.setSortable(true);
	
		//indCol.setRenderer(renderLast); //TODO: does not work, fix later
		
		ColumnConfig[] columns = new ColumnConfig[]{indCol};
		
		ColumnModel columnModel = new ColumnModel(columns);
		individualsGrid.setColumnModel(columnModel);
	}

	public ArrayList<EntityData> getSelection() {
		return currentSelection;
	}

	class GetIndividuals extends AbstractAsyncHandler<ArrayList<EntityData>> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting individuals for " + _currentEntity, caught);
		}

		public void handleSuccess(ArrayList<EntityData> indData) {		
			for (Iterator<EntityData> iterator = indData.iterator(); iterator.hasNext();) {
				EntityData instData = iterator.next();
				Record record = recordDef.createRecord(new Object[]{instData});
				store.add(record);
			}
		}
	}	

	Renderer renderLast = new Renderer() {
		public String render(Object value, CellMetadata cellMetadata, 
				Record record, int rowIndex, int colNum, Store store) {
			return new String("<a href=\"\">view recent changes</a>");
		}
		
	};	
	
}

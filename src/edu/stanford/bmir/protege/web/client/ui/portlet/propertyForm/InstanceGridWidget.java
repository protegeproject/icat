package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.BooleanFieldDef;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.EditorGridPanel;
import com.gwtext.client.widgets.grid.GridEditor;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.EditorGridListener;
import com.gwtext.client.widgets.grid.event.EditorGridListenerAdapter;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;
import com.gwtext.client.widgets.grid.event.GridRowListener;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.gwtext.client.widgets.portal.Portlet;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.UIUtil;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.EntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.PropertyValueUtil;

public class InstanceGridWidget extends AbstractPropertyWidget {

	private Panel wrappingPanel;
	private EditorGridPanel grid;
	protected RecordDef recordDef;
	protected Store store;	
	protected GridRowListener gridRowListener;
	protected EditorGridListener editorGridListener;
	protected List<String> properties = new ArrayList<String>(); //order of cols
	private String autoExpandColId;
	private String ontType;
	private Portlet portletId;//TODO this should be changed to the String element ID, but we could not retrieve the Portlet based on the ID
	
	private static String INSTANCE_FIELD_NAME = "@instance@";
	private static String DELETE_FIELD_NAME = "@delete@";
	private static String COMMENT_FIELD_NAME = "@comment@";
	
	public InstanceGridWidget(Project project) {
		super(project);
	}

	void setPortletId(Portlet id) {
		portletId = id;		
	}
	
	public void refresh() { //only works if portlet id has been set
		//TODO see if we can make this method work with String Portlet IDs
		if (portletId == null) { return;}
		//Element el = DOM.getElementById(portletId);
		//GWT.log(el.getId(), null);
		if (portletId instanceof EntityPortlet) {
			store.removeAll();
			((EntityPortlet)portletId).reload();
		}
//		RootPanel rootPanel = RootPanel.get(null);
////		Element portletElement = rootPanel.getElement();
//		//JavaScriptObject
//		Widget w = (Widget) rootPanel;
//		AbstractEntityPortlet aep = (AbstractEntityPortlet) w;
//		GWT.log("Found it! " + aep, null);
//		aep.reload();			
		
//		AbstractEntityPortlet x = new AbstractEntityPortlet(project);
//		portletElement.is(x);
//		java.util.Iterator<Widget> it = rootPanel.iterator();
//		while (it.hasNext()) {
//			Widget w = it.next();
//			if (w.getElement().getId().equals(portletId)) {
//				AbstractEntityPortlet aep = (AbstractEntityPortlet) w;
//				GWT.log("Found it! " + aep, null);
//				aep.reload();			
//			}
//		}
//		wrappingPanel.getComponent(portletId);	
	}
	
	@Override
	public Widget getComponent() {				
		return wrappingPanel;
	}
	
	public GridPanel getGridPanel() {
		return grid;
	}
	
	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {	
		super.setup(widgetConfiguration, propertyEntityData);
		
		//build the grid
		grid = createGrid();
		wrappingPanel = new Panel();
		wrappingPanel.setLayout(new ColumnLayout());
		wrappingPanel.setPaddings(5);
		
		String labelText = (String) widgetConfiguration.get(FormConstants.LABEL);
		Label label = new Label((labelText != null ? labelText : getProperty().getBrowserText()) + ":");		
		label.setCls("form_label");
		
		VerticalPanel labelPanel = new VerticalPanel();		
		labelPanel.add(label);		
		Hyperlink addNewLink = createAddNewHyperlink();
		labelPanel.add(addNewLink);
		grid.addEditorGridListener(getEditorGridListener());
		
		wrappingPanel.add(labelPanel);
		//wrappingPanel.add(addNewLink);
		wrappingPanel.add(grid, new ColumnLayoutData(1));
	}
	
	protected Hyperlink createAddNewHyperlink() {
		Hyperlink addNewLink = new Hyperlink("<br><img src=\"images/add.png\"></img>&nbsp Add new value", true, "");
		addNewLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				onAddNewValue();							
			}							
		});
		return addNewLink;
	}

	protected void onAddNewValue() {		
		List<EntityData> allowedValues = getProperty().getAllowedValues();
		String type = null;
		if (allowedValues != null && !allowedValues.isEmpty()) {
			type = allowedValues.iterator().next().getName();
		}
		
		OntologyServiceManager.getInstance().createInstanceValue(getProject().getProjectName(),
					null, type, getSubject().getName(), getProperty().getName(), new AddPropertyValueHandler());	
		
	}

	//TODO: should be configurable
	protected EditorGridPanel createGrid() {
		grid = new EditorGridPanel();
		grid.setCls("form_grid");
		
		grid.setAutoWidth(true);				
		grid.setStripeRows(true);	
		grid.setClicksToEdit(2);
		grid.setFrame(true);
		
		Map<String, Object> widgetConfig = getWidgetConfiguration();
		if (widgetConfig != null) {	
			String heigthStr = (String) widgetConfig.get(FormConstants.HEIGHT);
			if (heigthStr != null) {
				grid.setHeight(Integer.parseInt(heigthStr));
			} else {
				grid.setHeight(110);
			}
		}
		
		ontType = (String) widgetConfig.get(FormConstants.ONT_TYPE);
		
		createColumns();	
		createStore();
		attachListeners();
		
		if (autoExpandColId != null) {
			grid.setAutoExpandColumn(autoExpandColId);
		}
		
		return grid;
	}
	
	protected void attachListeners() {
		//TODO: may not works so well.. - check indexes
		grid.addGridCellListener(new GridCellListenerAdapter() {
			 public void onCellClick(GridPanel grid, int rowIndex, int colindex, EventObject e) {
				 if (colindex == properties.size() + 1)  {//FIXME!!!!
					 //should be delete - do nothing
					 Record record = store.getAt(rowIndex);
					 if (record != null) {						 
						 onDelete(rowIndex);
					 }
				 } else if (colindex == properties.size() + 2) {
					 //should be comment
					 MessageBox.show(new MessageBoxConfig() {
	                     {
	                         setTitle("Comment");
	                         setMsg("Please enter a comment on this property value:");
	                         setWidth(400);
	                         setButtons(MessageBox.OKCANCEL);
	                         setMultiline(true);
	                         setCallback(new MessageBox.PromptCallback() {
	                             public void execute(String btnID, String text) {
	                                 
	                             }
	                         });
	                         setAnimEl(InstanceGridWidget.this.grid.getId());
	                     }
	                 });
				 }
			 }						
		});		
		
	}
	
	protected EditorGridListener getEditorGridListener() {
		if (editorGridListener == null) {
			editorGridListener = new EditorGridListenerAdapter() {
				@Override
				public boolean doBeforeEdit(GridPanel grid, Record record,
						String field, Object value, int rowIndex, int colIndex) {					
					//if (!getProject().hasWritePermission()) { return false; } //TODO: comment out just for testing
					String valueType = record.getAsString("valueType");
					return valueType == null || valueType.equalsIgnoreCase("string") || valueType.equalsIgnoreCase("any"); //TODO: allow only the editing of String values for now						
				}
				
				@Override
				public void onAfterEdit(GridPanel grid, Record record,
						String field, Object newValue, Object oldValue,
						int rowIndex, int colIndex) {
					//special handling rdfs:Literal
					String valueType = record.getAsString("valueType"); 
					if (valueType == null) { //TODO: should be fixed
						valueType = ValueType.String.name();
					}
					/*
					String lang = record.getAsString("language");					
					if (lang != null && lang.length() > 0) {
						newValue = "~#" + lang + " " + newValue.toString();
						oldValue = "~#" + lang + " " + oldValue.toString();
						valueType = ValueType.LITERAL.name();
					}
					*/
					String selSubject = record.getAsString(INSTANCE_FIELD_NAME);
					if (selSubject != null) {
						new PropertyValueUtil().
							replacePropertyValue(getProject().getProjectName(), selSubject, properties.get(colIndex),
									ValueType.valueOf(valueType), oldValue.toString(), newValue.toString(), new ReplacePropertyValueHandler(new EntityData(newValue.toString(), newValue.toString())));
					}
				}
			};
		}
		return editorGridListener;
	}
	
	
	protected void onDelete(int index) {
		Record record = store.getAt(index);
		String value = record.getAsString(INSTANCE_FIELD_NAME);
		if (value != null) {
			GWT.log("Deleting " + value, null);
			
			new PropertyValueUtil().deletePropertyValue(getProject().getProjectName(), getSubject().getName(), 
					getProperty().getName(), ValueType.Instance, value, new RemovePropertyValueHandler(index));
								
		}
	}

	protected void createStore() {
		ArrayReader reader = new ArrayReader(recordDef);		 
		MemoryProxy dataProxy = new MemoryProxy(new Object[][]{});
		store = new Store(dataProxy, reader);
		//store.setSortInfo(new SortState("property", SortDir.ASC));        
		grid.setStore(store);		
		store.load();	
	}
	
	
	protected void createColumns() {
		Map<String, Object> widgetConfig = getWidgetConfiguration();
		if (widgetConfig == null) {	return;}
		
		int colCount = 0;
		
		for (String key : widgetConfig.keySet()) {
			if (key.startsWith(FormConstants.COLUMN_PREFIX)) {
				colCount ++;
			}
		}		
	
		FieldDef[] fieldDef = new FieldDef[colCount + 3];
		ColumnConfig[] columns = new ColumnConfig[colCount + 3];
		String[] props = new String[colCount];
		
		for (String key : widgetConfig.keySet()) {
			if (key.startsWith(FormConstants.COLUMN_PREFIX)) {
				createColumn((Map<String, Object>) widgetConfig.get(key), fieldDef, columns, props);
			}
		}
		
		properties = Arrays.asList(props);
		
		createInstanceColumn(fieldDef, columns, colCount);
		createActionColumns(fieldDef, columns, colCount);
		
		recordDef = new RecordDef(fieldDef);
		
		ColumnModel columnModel = new ColumnModel(columns);
		grid.setColumnModel(columnModel);	
	}
	
	
	protected void createInstanceColumn(FieldDef[] fieldDef, ColumnConfig[] columns, int colCount) {
		ColumnConfig instCol = new ColumnConfig("", INSTANCE_FIELD_NAME, 25);
		instCol.setTooltip("Attached instance name");
		instCol.setRenderer(new Renderer() {
			public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum, Store store) {
				String strValue = (String) value;
				return (strValue.contains("#") ? strValue.substring(strValue.lastIndexOf("#")) : strValue);
			}
		});
		instCol.setHidden(true);
		
        fieldDef[colCount] = new StringFieldDef(INSTANCE_FIELD_NAME);
        columns[colCount] = instCol;
	}
	
	protected void createActionColumns(FieldDef[] fieldDef, ColumnConfig[] columns, int colCount) {
		ColumnConfig deleteCol = new ColumnConfig("", DELETE_FIELD_NAME, 25);
		deleteCol.setTooltip("Delete this value");

        deleteCol.setRenderer(new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum, Store store) {              
                return "<img src=\"images/delete.png\"></img>";
            }
        });
        
        ColumnConfig commentCol = new ColumnConfig("", COMMENT_FIELD_NAME, 25);
        commentCol.setTooltip("Add a comment on this value");
        commentCol.setRenderer(new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum, Store store) {              
                return "<img src=\"images/comment.gif\"></img>";            	
            }
        });
        
		fieldDef[colCount + 1] = new BooleanFieldDef(DELETE_FIELD_NAME);
		fieldDef[colCount + 2] = new BooleanFieldDef(COMMENT_FIELD_NAME);
		columns[colCount + 1] = deleteCol;
		columns[colCount + 2] = commentCol;
	}

	//FIXME: protect against invalid config xml
	protected ColumnConfig createColumn(Map<String, Object> config, FieldDef[] fieldDef, ColumnConfig[] columnConfigs, String[] props) {
		ColumnConfig colConfig = new ColumnConfig();		
		String header = (String) config.get(FormConstants.HEADER);		
		colConfig.setHeader(header == null ? "" : header);		
		String property = (String) config.get(FormConstants.PROPERTY); //better not be null
		String indexStr = (String) config.get(FormConstants.INDEX);
		int index = Integer.parseInt(indexStr); //better be valid
		props[index] = property; 		
		String colName = FormConstants.COLUMN_PREFIX + index;
		colConfig.setDataIndex(colName);
		String widthStr = (String) config.get(FormConstants.WIDTH);
		if (widthStr != null) {
			if (widthStr.equalsIgnoreCase(FormConstants.WIDTH_ALL)) {
				autoExpandColId = HTMLPanel.createUniqueId();
				colConfig.setId(autoExpandColId);
			} else {
				int width = Integer.parseInt(widthStr);
				colConfig.setWidth(width); 
			}
		}
		
		//TODO: support other types as well
		GridEditor editor = getGridEditor();
		if (editor != null) {
			colConfig.setEditor(editor);
		}
		else {
		}
				
		colConfig.setResizable(true);
		colConfig.setSortable(true);
		colConfig.setCss("word-wrap: break-word ;");
		
		String fieldType = (String) config.get(FormConstants.FIELD_TYPE);
		if (fieldType != null && fieldType.equals("link")) {
			colConfig.setRenderer(new Renderer() {
				public String render(Object value, CellMetadata cellMetadata,
						Record record, int rowIndex, int colNum, Store store) {
					//return "<a href= \"" + value + "\" target=_blank>" + "<img src=\"images/Globe-Connected-16x16.png\"></img>" + "</a>";
					return "<a href= \"" + value + "\" target=_blank>" + "<img src=\"images/world_link.png\"></img>" + "</a>";
				}
				
			});
		}
		else {
			colConfig.setRenderer(getDataColumnRenderer());
		}
		
		//TODO: support other types as well
		fieldDef[index] = new StringFieldDef(colName);
		columnConfigs[index] = colConfig;
		
		return colConfig;
	}
	
	protected GridEditor getGridEditor() {
		return new GridEditor(new TextField());
	}

	protected Renderer getDataColumnRenderer() {
		return new Renderer() {
			public String render(Object value, CellMetadata cellMetadata, 
					Record record, int rowIndex, int colNum, Store store) {				
				String field = record.getAsString(store.getFields()[colNum]);
				if (field == null) { field = ""; }
				field = preRenderColumnContent(field);
				return Format.format("<style type=\"text/css\">.x-grid3-cell-inner, .x-grid3-hd-inner { white-space:normal !important; }</style> {0}", 
						new String[]{(field)});
			}
		};
	}

	protected String preRenderColumnContent(String content) {
		return content;
	}
	
	@Override
	public void setValues(Collection<EntityData> values) {
		store.removeAll();
		ArrayList<String> entities = new ArrayList<String>();
		for (EntityData entityData : values) {
			entities.add(entityData.getName());
		}
		OntologyServiceManager.getInstance().getEntityTriples(getProject().getProjectName(),
				entities, properties, new GetTriples());		
	}
	
	
	
	/*
	 * Remote calls
	 */

	class GetTriples extends AbstractAsyncHandler<ArrayList<Triple>> {

		public void handleFailure(Throwable caught) {
			GWT.log("Instance Grid Widget: Error at getting triples for " + getSubject(), caught);
		}

		public void handleSuccess(ArrayList<Triple> triples) {
			//TODO: make a more efficient call
			HashMap<String, List<Triple>> instToTriples = new HashMap<String, List<Triple>>();
			for (Triple triple : triples) {
				String subject = triple.getEntity().getName();
				List<Triple> instTriples = instToTriples.get(subject);
				if (instTriples == null) {
					instTriples = new ArrayList<Triple>();
				}
				instTriples.add(triple);
				instToTriples.put(subject, instTriples);
			}

			Object[][] data = new Object[instToTriples.size()][properties.size() + 3];
			
			int i = 0;
			for (String subject : instToTriples.keySet()) {
				List<Triple> subjTriples = instToTriples.get(subject);
				for (Triple triple : subjTriples) {
					String prop = triple.getProperty().getName();
					if (properties.contains(prop)) {
						int index = properties.indexOf(prop);
						while (index >= 0) {
							data[i][index] = UIUtil.getDisplayText(triple.getValue());
							if (properties.lastIndexOf(prop) > index) {//checks for another occurrence
								index = properties.lastIndexOf(prop);
							}
							else {
								index = -1;
							}
						}
					}
				}
				//add the name of the subject instance
				data[i][properties.size()] = subject;
				//add delete and comment icons
				data[i][properties.size() + 1] = true;
				data[i][properties.size() + 2] = true;		
				i++;
			}
		
			store.setDataProxy(new MemoryProxy(data));
			store.load();
			wrappingPanel.doLayout();
		}		
	}	
	
	/*
	 * Remote calls
 	 */
	class RemovePropertyValueHandler extends AbstractAsyncHandler<Void> {
		private int removeInd;
		
		public RemovePropertyValueHandler(int removeIndex) {
			this.removeInd = removeIndex;
		}
		
		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at removing value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), caught);
			Window.alert("There was an error at removing the property value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText() + ".");
			//setValues(values);
		}

		@Override
		public void handleSuccess(Void result) {
			GWT.log("Success at removing value for "  + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
			Record recordToRemove = store.getAt(removeInd);
			if (recordToRemove != null) {
				store.remove(recordToRemove);
				store.commitChanges();
			}
		}
		
	}
	
	class ReplacePropertyValueHandler extends AbstractAsyncHandler<Void> {

		private EntityData newEntityData;
		
		public ReplacePropertyValueHandler(EntityData newEntityData) {
			this.newEntityData = newEntityData;
		}

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at replace property for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), caught);
			Window.alert("There was an error at setting the property value for " + getSubject().getBrowserText() + ".");
			//setValues(values);
		}

		@Override
		public void handleSuccess(Void result) {		
			GWT.log("Success at setting value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
			//FIXME: we need a reload method
			Collection<EntityData> ed = new ArrayList<EntityData>();
			ed.add(newEntityData);
			//setValues(ed);
		}
	}
	
	class AddPropertyValueHandler extends AbstractAsyncHandler<EntityData> {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at add property for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), caught);
			Window.alert("There was an error at adding the property value for " + getSubject().getBrowserText() + ".");
		}

		@Override
		public void handleSuccess(EntityData newInstance) {
			if (newInstance == null) {
				GWT.log("Error at add property for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
				return;
			} 
			
			GWT.log("Success at adding value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
			
			Object[] empty = new Object[properties.size() + 3];
			empty[properties.size()] = newInstance.getName();
			
			Record plant = recordDef.createRecord(empty);  
			grid.stopEditing();  
			store.insert(0, plant);  
			grid.startEditing(0, 0); 
			
		}
	}
	

}

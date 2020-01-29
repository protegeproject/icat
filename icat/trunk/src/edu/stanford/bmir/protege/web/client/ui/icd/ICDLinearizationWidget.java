package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ListBox;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.MenuItem;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.WidgetConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.MultilevelInstanceGridWidget;
import edu.stanford.bmir.protege.web.client.ui.util.UIConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class ICDLinearizationWidget extends MultilevelInstanceGridWidget {

    private static final String TOP_CLASS_PROP = "topClass";
    
    private static final String NO_PARENT_SELECTED = "<Linearization parent not set>";

    private static int OFFSET_DELETE_COLUMN = -1;
    private static int OFFSET_COMMENT_COLUMN = 1;;
    private static int OFFSET_MAX_COLUMN = OFFSET_COMMENT_COLUMN;

    private String propertyNameParent = null;
    private String fieldNameParent = null;
    private int colIndexParent = -1;

    private String fieldNameIsPart = null;
    private int colIndexIsPart = -1;
    private String fieldNameIsAuxAxisChild = null;
    private int colIndexIsAuxAxisChild = -1;

    private Record currentRecord;
    private Record currentShadowStoreRecord;

    //not used anymore, can be deleted
    private String topClass;
    
    public ICDLinearizationWidget(Project project) {
        super(project);
    }

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        topClass = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), getProject().getProjectConfiguration(), TOP_CLASS_PROP, null);
        allowedValues = new WidgetConfiguration(getWidgetConfiguration()).getUserSpecificAllowedValues();
    }

    @Override
    protected ColumnConfig createColumn(Map<String, Object> columnConfig, 
    		FieldDef[] fieldDef, ColumnConfig[] columnConfigs, String property, int index) {
        ColumnConfig colConfig = super.createColumn(columnConfig, fieldDef, columnConfigs, property, index);

        //do not allow users to rearrange instance order
        colConfig.setSortable(false);

        //hide and extract info from column storing the parent instance name
        if (colConfig.getHeader().toLowerCase().contains("parent")) {
        	propertyNameParent = property;
            fieldNameParent = colConfig.getDataIndex();
            //either this, or there is a better way to get colIndexParent using prop2Index (see below)
            //colIndexParent = Integer.parseInt(key.substring(FormConstants.COLUMN_PREFIX.length())) - 1; //Column1 -> index 0, Column2 -> index 1, etc.

            //hide parent column only when we create the parent display column below,
            //because we want to know what is the user specified setting
            //for the hidden status of this column
            //colConfig.setHidden(true);
            
            colConfig.setTooltip("The parent of the category in a certain linearization.\n "
                    + "Each class must have one and only one parent in every linearization.\n "
                    + "Click on the cell to select a (new) parent.");

            colIndexParent = getColumnIndexFromConfig(columnConfig);
            
            colConfig.setRenderer(new Renderer() {
                public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum, Store store) {
                    String entityDataValue = (String) value;
                    if (entityDataValue == null ) {
                        return "<DIV style=\"color:GRAY\">" +
                        		(isReadOnlyColumn(colNum) ? "Parent not set" : "Click here to select a parent") + "</DIV>";
                    } else if (isReadOnlyColumn(colNum) ||
                    		(entityDataValue.startsWith("[") && entityDataValue.endsWith("]") )) {
                        return "<DIV style=\"color:GRAY\">" + entityDataValue + "</DIV>";
                    } else {
                        return entityDataValue;
                    }
                }
            });

        }

        //set special columns name and column index:
        String colName = colConfig.getHeader().toLowerCase();
        if (colName.contains("part") || colName.contains("incl")) {
        	fieldNameIsPart = colConfig.getDataIndex();
        	colIndexIsPart = index;
        }
        else if (colName.contains("aux") && colName.contains("child")) {
        	fieldNameIsAuxAxisChild = colConfig.getDataIndex();
        	colIndexIsAuxAxisChild = index;
        }
        
        return colConfig;
    }

    @Override
    protected Anchor createAddNewValueHyperlink() {
        return null;
    }

    @Override
    protected Anchor createAddExistingHyperlink() {
        return null;
    }

    @Override
    protected void attachListeners() {
        getGridPanel().addGridCellListener(new GridCellListenerAdapter() {
            double timeOfLastClick = 0;

            @Override
            public void onCellClick(final GridPanel grid, final int rowIndex, final int colIndex, final EventObject e) {
                double eventTime = e.getTime();
                if (eventTime - timeOfLastClick > 500) { //not the second click in a double click
                    onCellClickOrDblClick(grid, rowIndex, colIndex, e);
                }
                /*
                 * Set new value for timeOfLastClick the time the last click was handled
                 * We use the current time (and not eventTime), because some time may have passed since eventTime
                 * while executing the onCellClickOrDblClick method.
                 */
                timeOfLastClick = new Date().getTime();
            }

            private void onCellClickOrDblClick(GridPanel grid, final int rowIndex, int colIndex, EventObject e) {
                // int offsetDeleteColumn = getOffsetDeleteColumn();
                int offsetCommentColumn = getOffsetCommentColumn();
                if (e.getTarget(".checkbox", 1) != null) {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        if (isWriteOperationAllowed() && !isReadOnlyColumn(colIndex)) {
                            String field = record.getFields()[colIndex];
                            String value = record.getAsString(field);
                            if (Boolean.parseBoolean(value) == true) {
                                record.set(field, Boolean.FALSE.toString());
                            } else {
                                record.set(field, Boolean.TRUE.toString());
                            }
                            updateInstanceValue(record, colIndex, value == null ? "" : value, record.getAsString(field), ValueType.Boolean, false);
                        }
                    }
                } else if (colIndex == colIndexParent) {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        if (isWriteOperationAllowed() && !isReadOnlyColumn(colIndex)) {
                        	Record shadowStoreRecord = getShadowStore().getAt(rowIndex);
                            String field = record.getFields()[colIndex];
                            selectNewParents(record, shadowStoreRecord, field);
                        }
                    }
                } else if (offsetCommentColumn != -1 && colIndex == properties.size() + offsetCommentColumn) {
                    onCommentColumnClicked(rowIndex);
                } else {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        String fieldValueType = (String) getColumnConfiguration(colIndex, FormConstants.FIELD_VALUE_TYPE);
                    	ICDLinearizationWidget.super.onValueColumnClicked(grid, rowIndex, colIndex);
                        if (fieldValueType != null && (!fieldValueType.equalsIgnoreCase("string"))) {
							//TODO check this - this may not be a good idea, as it may allow the user to edit columns that should not be edited
                            //((EditorGridPanel)grid).startEditing(rowIndex, colIndex);
                        }
                    }
                }
            }

            @Override
            public void onCellContextMenu(final GridPanel grid, final int rowIndex, final int colIndex, final EventObject e) {
                e.stopEvent();
                if (e.getTarget(".checkbox", 1) != null) {
                    final Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        if (isWriteOperationAllowed() && !isReadOnlyColumn(colIndex)) {
                            String field = record.getFields()[colIndex];
                            String value = record.getAsString(field);
                            if (value != null && !"".equals(value)) {
                                Menu contextMenu = new DeleteContextMenu(
                                    "Unset value (i.e. set to 'Unknown')", UIConstants.ICON_CHECKBOX_UNKNOWN,
                                    record, rowIndex, colIndex, ValueType.Boolean, false);
                                contextMenu.showAt(e.getXY()[0] + 5, e.getXY()[1] + 5);
                            }
                        }
                    }
                } else if (colIndex == colIndexParent) {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        if (isWriteOperationAllowed() && !isReadOnlyColumn(colIndex)) {
                            if (colIndexParent >= 0) {
                                String field = record.getFields()[colIndexParent];
                                String value = record.getAsString(field);
                                if (value != null && !"".equals(value)) {
                                    Menu contextMenu = new DeleteContextMenu(
                                            "Remove linearization parent", "images/delete_small_16x16.png",
                                            record, rowIndex, colIndexParent, ValueType.Instance, true);
                                    contextMenu.showAt(e.getXY()[0] + 5, e.getXY()[1] + 5);
                                }
                            }
                        }
                    }
                } else {
                	ICDLinearizationWidget.super.onContextMenuClicked(rowIndex, colIndex, e);
                }
            }

            final class DeleteContextMenu extends Menu{
                public DeleteContextMenu(String menuText, String menuIcon, final Record record, 
                		final int rowIndex, final int colIndex, final ValueType valueType, final boolean resetParentDisplayName) {
                    MenuItem item = new MenuItem();
                    item.setText(menuText);
                    item.setIcon(menuIcon);
                    item.addListener(new BaseItemListenerAdapter() {
                        @Override
                        public void onClick(BaseItem item, EventObject e) {
                          super.onClick(item, e);
                          String field = record.getFields()[colIndex];
                          EntityData value = (EntityData)getShadowStore().getAt(rowIndex).getAsObject(field);
                          record.set(field, (String)null);
                          updateInstanceValue(record, colIndex, value == null ? "" : value, record.getAsString(field), valueType, resetParentDisplayName);
                          //alternative solution, if we don't want to refresh the widget, but prefer to display a non-precise message
//                          if (resetParentDisplayName) {
//                              record.set(ICD_CATEGORY_DISPLAY_FIELD_NAME, (String)null); //this is optimistic and not precise
//                          }
                        }
                    });
                    addItem(item);
                }
            }
        });
    }



    protected void updateInstanceValue(Record record, int colIndex, Object oldValue, Object newValue,
            ValueType valueType, final boolean updateLinParentName) {
        if (valueType == null) { //TODO: should be fixed
            valueType = ValueType.String;
        }
        String selSubject = record.getAsString(INSTANCE_FIELD_NAME);
        if (selSubject != null) {
            Object oldValueId = (oldValue == null ? "" : oldValue);
            oldValueId = (oldValueId instanceof EntityData) ? ((EntityData)oldValueId).getName() : oldValueId.toString();

            Object newValueId = newValue == null ? null :
                (newValue instanceof EntityData) ? ((EntityData)newValue).getName() : newValue.toString();

            propertyValueUtil.replacePropertyValue(getProject().getProjectName(), selSubject,
                    properties .get(colIndex), valueType, (String)oldValueId, (String)newValueId, 
                    getCopyIfTemplateOption(),
                    GlobalSettings.getGlobalSettings().getUserName(),
                    getReplaceValueOperationDescription(colIndex, oldValue, newValue),	//TODO create a better op. description that contains the linearization name (see if there are similar messages to be updated) 
                    new ReplacePropertyValueHandlerForICDLinearizationWidget(new EntityData(
                            newValue == null ? null : newValue.toString(),
                                    newValue == null ? null : newValue.toString()), updateLinParentName));
        }
    }

    class ReplacePropertyValueHandlerForICDLinearizationWidget extends ReplacePropertyValueHandler {
        private boolean updateLinParentName;

        public ReplacePropertyValueHandlerForICDLinearizationWidget(EntityData newEntityData, boolean updateLinParentName) {
            super(newEntityData);
            this.updateLinParentName = updateLinParentName;
        }

        @Override
        public void onSuccess(Void result) {
            super.onSuccess(result);
            if (updateLinParentName) {
                refresh();
            }
        }
    }

    @Override
    protected int getOffsetDeleteColumn() {
        return OFFSET_DELETE_COLUMN;
    }

    @Override
    protected int getOffsetCommentColumn() {
        return OFFSET_COMMENT_COLUMN;
    }

    @Override
    protected int getMaxColumnOffset() {
        return OFFSET_MAX_COLUMN;
    }

    @Override
    protected int getExtraColumnCount() {
        return OFFSET_MAX_COLUMN + 1; //1 for the instance field
    }

    
    protected void selectNewParents(Record record, Record shadowStoreRecord, String field) {
        currentRecord = record;
        currentShadowStoreRecord = shadowStoreRecord;
 
        getParents();
    }
    
    private ListBox getParents() {
    	ListBox lb = new ListBox();
    	
    	EntityData subject = getSubject();
    	if (subject == null || subject.getName() == null) {
    		return lb;
    	}
    	
    	OntologyServiceManager.getInstance().getParents(getProject().getProjectName(), subject.getName(), true, 
    			new AsyncCallback<List<EntityData>>() {
			
    		@Override
			public void onFailure(Throwable caught) {
				MessageBox.alert("Error", "There was an error at retrieving the direct parents.");
			}
    		
			@Override
			public void onSuccess(List<EntityData> parents) {
				List<EntityData> directParents = new ArrayList<EntityData>();
				
				lb.addItem(NO_PARENT_SELECTED);
				directParents.add(new EntityData(NO_PARENT_SELECTED, NO_PARENT_SELECTED));
				
				for (EntityData parent : parents) {
					lb.addItem(UIUtil.getDisplayText(parent));
					directParents.add(parent);
				}
				
				int visibileRows = parents.size();
				lb.setVisibleItemCount(visibileRows <= 1 ? 2: visibileRows);
				
				lb.setMultipleSelect(false);
				
				selectOldParent(lb, directParents);
				
				showParentsList(lb, directParents);
			}
			
		});
    	
        return lb;
    }
    
    private void selectOldParent(ListBox lb, List<EntityData> directParents) {

    	EntityData oldParent = (EntityData)currentShadowStoreRecord.getAsObject(fieldNameParent);
    	int index = getOldParentIndex(directParents, oldParent);
    	
    	if (index != -1) {
    		lb.setSelectedIndex(index);
    		lb.setItemText(index, "* " + lb.getItemText(index));
    	}

  	}
    
    private int getOldParentIndex(List<EntityData> directParents, EntityData oldParent) {
    	if (oldParent == null) {
    		return 0;
    	}
    	
    	for (int i = 0; i < directParents.size(); i++) {
			EntityData parent = directParents.get(i);
			if (parent.equals(oldParent)) {
				return i;
			}
		}
    	
    	return -1;
    }

	private void showParentsList(final ListBox parentsListBox, final List<EntityData> directParents) {
    	Window win = createParentsSelectionWindow();
        
        Button cancelButton = new Button("Cancel");
        cancelButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                win.hide();
                win.close();
            }
        });

        Button selectButton = new Button("Select");
        selectButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
            	int selectedIndex = parentsListBox.getSelectedIndex();
                
                if (selectedIndex == -1) {
                    MessageBox.alert("No selection", "No class selected. Please select a parent from the list.");
                    return;
                }
                
                EntityData parent = directParents.get(selectedIndex);
                if (NO_PARENT_SELECTED.equals(parent.getName())) { //this is a delete case
                	parent = null;
                }
               
                if (currentRecord != null) {

                    EntityData oldParent = (EntityData)currentShadowStoreRecord.getAsObject(fieldNameParent);

                    if (fieldNameParent != null) {
                    	//this is optimistic
                        currentRecord.set(fieldNameParent, parent == null ? null : parent.getBrowserText());
                        currentShadowStoreRecord.set(fieldNameParent, parent);
                    }
                    if (colIndexParent >= 0) {
                        updateInstanceValue(currentRecord, colIndexParent, oldParent, parent, ValueType.Instance, false); //false - because above we have already set the lin. parent name optimistically
                    }
                }
                              
                win.hide();
                win.close();
            }
        });

        win.add(parentsListBox);
        win.addButton(selectButton);
        win.addButton(cancelButton);
       
        win.setModal(true);
       
        win.show();
        win.center();
        
	}
    
    private Window createParentsSelectionWindow() {
    	Window win = new Window();
        win.setTitle("Select linearization parent (one of the direct Foundation parents)");
        win.setWidth(400);
        win.setHeight(200);
       
        win.setLayout(new FitLayout());
        win.setPaddings(5);
        win.setButtonAlign(Position.CENTER);

        win.setCloseAction(Window.HIDE);
        win.setPlain(true);
        return win;
    }




    @Override
    protected boolean isAllowedValueForUser(EntityPropertyValuesList epv) {
    	if (allowedValues == null) {
    		return true;
    	}
    	else {
    		if (allowedValuesColumnIndex < 0 || allowedValuesColumnIndex >= epv.getProperties().size()) {
    			return true;
    		}
    		
			List<EntityData> allowedValuesPropertyValues = epv.getPropertyValues(allowedValuesColumnIndex);
			if (allowedValuesPropertyValues == null) {
				return true;
			}
			
			EntityData firstAllowedValuesPropertyValue = allowedValuesPropertyValues.get(0);
    		return firstAllowedValuesPropertyValue == null  ?  true  : 
    			allowedValues.contains(firstAllowedValuesPropertyValue.getName());
    	}
    }
    
    @Override
    protected void setExtraColumnValues(Object[] datarow, EntityPropertyValues epv) {
        super.setExtraColumnValues(datarow, epv);

        //add parent display text
        String parentName = null;
        for (PropertyEntityData ped : epv.getProperties()) {
            //datarow[getIndexOfProperty(ped.getName())] = UIUtil.commaSeparatedList(epv.getPropertyValues(ped));
            if (ped.getName().equals(propertyNameParent)) {
                List<EntityData> values = epv.getPropertyValues(ped);
                if (values != null && values.size() > 0) {
                    parentName = UIUtil.getDisplayText(values.get(0));
                    break;
                }
            }
        }

        datarow[colIndexParent] = parentName;
    }

    @Override
    protected void fillValues(List<String> subjects, List<String> props) {
        getStore().removeAll();
		ICDServiceManager.getInstance().getEntityPropertyValuesForLinearization(getProject().getProjectName(), subjects, UIUtil.getFirstItem(props), properties,
                subjectEntityColumns, new GetTriplesHandler(getSubject()));
    }

    @Override
    protected String getCellText(EntityPropertyValues epv, PropertyEntityData ped) {
        if (ped.getName().equals(propertyNameParent)) {
            List<EntityData> values = epv.getPropertyValues(ped);
            if (values != null && values.size() > 0) {
                return values.get(0).getName();
            }
            return null;
        } else {
            return super.getCellText(epv, ped);
        }
    }

    @Override
    protected void editClassFieldType(Record record, int rowIndex, int colIndex) {
        if (record != null) {
            if (isWriteOperationAllowed() && !isReadOnlyColumn(colIndex)) {
            	Record shadowStoreRecord = getShadowStore().getAt(rowIndex);
                String field = record.getFields()[colIndex];
                selectNewParents(record, shadowStoreRecord, field);
            }
        }

    }

    @Override
    protected String getWarningText(Object value, Record record, int rowIndex, int colNum, Store store) {
    	if (colIndexIsPart > -1 && colIndexIsAuxAxisChild > -1 &&
    			(colNum == colIndexIsPart || colNum == colIndexIsAuxAxisChild) &&
    			Boolean.valueOf(record.getAsString(fieldNameIsPart)) == true &&		//we are using this method of evaluation as record.getAsBoolean() does not return the set value, but rather it returns "true" if any value is set (true or false)
    			Boolean.valueOf(record.getAsString(fieldNameIsAuxAxisChild)) == true ) {
    		return "It is not permitted to have both \"Is part of the linearization\" and \"Is auxiliary axis child\" checked at the same time";
    	}
    	else {
    		return null;
    	}
	}
}

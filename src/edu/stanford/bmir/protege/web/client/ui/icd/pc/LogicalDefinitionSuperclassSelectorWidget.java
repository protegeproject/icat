/**
 * 
 */
package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.CellSelectionModel;
import com.gwtext.client.widgets.grid.EditorGridPanel;
import com.gwtext.client.widgets.grid.event.CellSelectionModelListener;
import com.gwtext.client.widgets.grid.event.CellSelectionModelListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceGridWidget;
import edu.stanford.bmir.protege.web.client.ui.util.SelectionUtil;
import edu.stanford.bmir.protege.web.client.ui.util.SelectionUtil.SelectionCallback;

/**
 *  
 * 
 * @author csnyulas
 */
public class LogicalDefinitionSuperclassSelectorWidget extends InstanceGridWidget  {

    private static int OFFSET_DELETE_COLUMN = 1;
    private static int OFFSET_COMMENT_COLUMN = -1;
    private static int OFFSET_MAX_COLUMN = OFFSET_DELETE_COLUMN;

//    private String propertyNameParent = null;
    private String fieldNameParent = INSTANCE_FIELD_NAME;
    //private int colIndexParent = -1; //should be colIndex "instance"

    private Record currentRecord;
    private Record currentShadowStoreRecord;
    private int currentRow;

	private SuperclassSelectorContainer<LogicalDefinitionSuperclassSelectorWidget> logicalDefWidget = null;	//reference to the LogicalDefinitionWidget that contains this, if any
    
	private LogicalDefinitionWidgetController<?> widgetController;

	/**
	 * @param project
	 */
	public LogicalDefinitionSuperclassSelectorWidget(Project project, LogicalDefinitionWidgetController<?> widgetController) {
		super(project);
		this.widgetController= widgetController;
	}

	public void setContainerWidget(SuperclassSelectorContainer<LogicalDefinitionSuperclassSelectorWidget> logicalDefinitionWidget) {
		this.logicalDefWidget = logicalDefinitionWidget; 
	}

    protected void setSelectionModel() { 
    	//TODO see if we keep this, or rather just go with the default CellSelectionModel
    	//grid.setSelectionModel(new RowSelectionModel(false));
    }

	@Override
	public void setSubject(EntityData subject) {
		super.setSubject(subject);
		widgetController.onSubjectChanged(subject);
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

    @Override
    protected void onContextMenuClicked(int rowIndex, int colIndex, EventObject e) {
    	//do nothing, as we don't want to allow modification of the field values (e.g. deletion of entity title)
    }
    
    protected void onDelete(int index) {
        Record record = getStore().getAt(index);
        String value = record.getAsString(INSTANCE_FIELD_NAME);
        if (value != null) {
            ICDServiceManager.getInstance().removeLogicalDefinitionSuperclass(getProject().getProjectName(), getSubject().getName(),
                    value, new RemovePropertyValueHandler(index));
        }
    }

    @Override
    protected String getDeleteValueConfirmationMessage(Record record) {
    	String fieldParentTitle = record.getFields()[0];
    	String parentTitle = record.getAsString(fieldParentTitle);
    	return "You are about to remove the logical definition with <BR>superclass <b>" + parentTitle + "</b>.<BR> Are you sure you want to proceed?";
    }
    
    @Override
    protected CellSelectionModelListener getSelectionModelListener() {
    	CellSelectionModelListenerAdapter selectionListener = new CellSelectionModelListenerAdapter() {
    		private Record lastSelectedRecord = null;
    		
    		@Override
    		public void onSelectionChange(CellSelectionModel sm, Record record, int[] rowIndexColIndex) {
    			super.onSelectionChange(sm, record, rowIndexColIndex);

    			if (record == null) {
    				if (getStore().getCount() == 0) {
    					selectionChanged(null);
    				}
    				return;
    			}
    			
    			if (record != lastSelectedRecord) {
    				String selectedSuperclassName = record.getAsString(INSTANCE_FIELD_NAME);
    				selectionChanged( new EntityData(selectedSuperclassName) );
    				lastSelectedRecord = record;
    			}
    		}
    	};
    	
    	return selectionListener;
    }
    
    @Override
	protected void setWidgetPropertyValues(EntityData mySubject, List<EntityPropertyValues> entityPropertyValues) {
    	super.setWidgetPropertyValues(mySubject, entityPropertyValues);

    	if ( entityPropertyValues != null && entityPropertyValues.size() > 0 ) {
    		EditorGridPanel gridPanel = (EditorGridPanel) getGridPanel();
			gridPanel.getCellSelectionModel().select(0, 0);

			String selectedSuperclassName = getCurrentSelection();
			selectionChanged(new EntityData(selectedSuperclassName));
    	}
	}
	
	private void selectionChanged(EntityData newVal) {
		if (logicalDefWidget != null) {
			logicalDefWidget.onSuperclassChanged(newVal);
		}
		if (widgetController != null) {
			widgetController.onSuperclassChanged(newVal);
		}
	}
	

    @Override
    protected void onAddExistingValue(int rowToReplace) {
//    	currentRecord = null;
    	EntityData oldParent = null;
    	SelectionUtil.selectSuperclass(getProject(), getSubject(), oldParent, false, new SelectionCallback() {
    		@Override
    		public void onSelect(Collection<EntityData> selection) {
//                if (currentRecord != null) {
//                	onDelete(currentRecord);
//                }
                if ( rowToReplace >=0 ) {
                	onDelete(rowToReplace);
                }
                if (selection == null || selection.isEmpty()) {
                	return;
                }
                EntityData parent = selection.iterator().next();
                addExistingValues( Arrays.asList( new EntityData[] {parent} ) );
    		}
    	});
    }

    
    @Override
    protected void editClassFieldType(Record record, int rowIndex, int colIndex) {
        if (record != null) {
            if (isWriteOperationAllowed() && !isReadOnlyColumn(colIndex)) {
            	currentRecord = record;
            	currentShadowStoreRecord = getShadowRecord(rowIndex);
                
                //TODO temporary solution. Delete it
                int colIndexParent = 0;
                
                currentRow = rowIndex;
            	EntityData oldParent = new EntityData(currentRecord.getAsString(INSTANCE_FIELD_NAME));
                SelectionUtil.selectSuperclass(getProject(), getSubject(), oldParent, false, new SelectionCallback() {
					
					@Override
					public void onSelect(Collection<EntityData> selection) {
			               
		                if (selection == null || selection.isEmpty()) {
		                	return;
		                }
		                EntityData parent = selection.iterator().next();

		                if (currentRecord != null) {

		                    if (fieldNameParent != null) {
		                    	//this is optimistic
		                        currentRecord.set(fieldNameParent, parent == null ? null : parent.getName());
		                        String fieldParentTitle = currentRecord.getFields()[0];
								currentRecord.set(fieldParentTitle, parent == null ? null : parent.getBrowserText());
		                        currentShadowStoreRecord.set(fieldParentTitle, parent);
		                    }
		                    if (colIndexParent >= 0) {
		                        propertyValueUtil.replacePropertyValue(getProject().getProjectName(), getSubject().getName(),
		                        		getProperty().getName(), ValueType.Class, oldParent.getName(), parent.getName(), 
		                        		getCopyIfTemplateOption(),
		                        		GlobalSettings.getGlobalSettings().getUserName(),
		                        		getReplaceValueOperationDescription(record, colIndex, oldParent, parent),	//TODO create a better op. description that contains the linearization name (see if there are similar messages to be updated) 
		                        		new ReplacePropertyValueHandler(parent));
		                        
		                        if (oldParent != null) {
		                            ICDServiceManager.getInstance().removeLogicalDefinitionForSuperclass(getProject().getProjectName(), getSubject().getName(),
		                            		oldParent.getName(), new RemoveLogicalDefinitionHandler(getSubject(), oldParent));
		                        }
		                    }
		                }
					}
				});
            }
        }

    }

	public String getCurrentSelection() {
		int[] selectedCell = getGridPanel().getCellSelectionModel().getSelectedCell();
		if (selectedCell == null || selectedCell.length == 0) {
			return null;
		}
		
		int selRow = selectedCell[0];
		Record selRecord = getStore().getAt(selRow);
		return selRecord.getAsString(INSTANCE_FIELD_NAME);
	}


    protected class RemoveLogicalDefinitionHandler extends AbstractAsyncHandler<Void> {
        private EntityData subject;
		private EntityData superclass;

        public RemoveLogicalDefinitionHandler(EntityData subject, EntityData superclass) {
        	this.subject = subject;
        	this.superclass = superclass;
		}

		@Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at removing logical definition on " + subject.getBrowserText() + 
            		" for superclass " + superclass.getBrowserText(), caught);
            MessageBox.alert("There was an error at deleting logical definition on " + subject.getBrowserText() + 
            		" for superclass " + superclass.getBrowserText() + ".");
        }

        @Override
        public void handleSuccess(Void result) {
    		GWT.log("Success at removing logical definition on " + subject.getBrowserText() + 
            		" for superclass " + superclass.getBrowserText(), null);
			String selectedSuperclassName = getCurrentSelection();
			selectionChanged(new EntityData(selectedSuperclassName));
        }
    }

}

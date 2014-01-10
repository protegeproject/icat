package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
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
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.ontology.search.DefaultSearchStringTypeEnum;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceGridWidget;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.util.UIConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

/**
 * 
 * @deprecated This class is currently not used. It was created to edit base inclusion terms from descendants, 
 * but it seems that the simple InstanceGridWidget can do the job. 
 * Remove the deprecated flag, when the implementation of this class will be finished, and the class will be used.
 * 
 **/
public class ICDInclusionWidget extends InstanceGridWidget {

    //private static final String TOP_CLASS_PROP = "topClass";

    protected static String ICD_CATEGORY_DISPLAY_FIELD_NAME = "@icd_category@";

    private static int OFFSET_ICD_CATEGORY_ENTITY_COLUMN = 1;

    private static int OFFSET_DELETE_COLUMN = 2;
    private static int OFFSET_COMMENT_COLUMN = 3;
    private static int OFFSET_MAX_COLUMN = OFFSET_COMMENT_COLUMN;

    private String propertyNameIcdCategory = null;
    private String fieldNameIcdCategory = null;
    private int colIndexIcdCategory = -1;

    private Record currentRecord;
    private Window selectWindow;
    private Selectable selectable;
    private String topClass;

    public ICDInclusionWidget(Project project) {
        super(project);
    }


    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        //topClass = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), TOP_CLASS_PROP, null);
        this.topClass = (String) widgetConfiguration.get(FormConstants.TOP_CLASS);
    }

    @Override
    protected void createColumns() {
        Map<String, Object> widgetConfig = getWidgetConfiguration();
        if (widgetConfig == null) {
            return;
        }

        int colCount = 0;

        for (String key : widgetConfig.keySet()) {
            if (key.startsWith(FormConstants.COLUMN_PREFIX)) {
                colCount++;
            }
        }

        FieldDef[] fieldDef = new FieldDef[colCount + getExtraColumnCount()];
        ColumnConfig[] columns = new ColumnConfig[colCount + getExtraColumnCount()];
        String[] props = new String[colCount];

        for (String key : widgetConfig.keySet()) {
            if (key.startsWith(FormConstants.COLUMN_PREFIX)) {
                Map<String, Object> columnConfig = (Map<String, Object>) widgetConfig.get(key);
                
                String property = getPropertyNameFromConfig(columnConfig);
                int index = getColumnIndexFromConfig(columnConfig);
                props[index] = property;

                ColumnConfig colConfig = createColumn(columnConfig, fieldDef, columns, property, index);
//
//                //do not allow users to rearrange instance order
//                colConfig.setSortable(false);

                //hide and extract info from column storing the parent instance name
                if (colConfig.getHeader().toLowerCase().contains("category")) {
                	propertyNameIcdCategory = property;
                    fieldNameIcdCategory = colConfig.getDataIndex();
                    //either this, or there is a better way to get colIndexParent using prop2Index (see below)
                    //colIndexParent = Integer.parseInt(key.substring(FormConstants.COLUMN_PREFIX.length())) - 1; //Column1 -> index 0, Column2 -> index 1, etc.
                    colConfig.setHidden(true);
                }
            }
        }

        properties = Arrays.asList(props);
        for (int i = 0; i < props.length; i++) {
            prop2Index.put(props[i], i);
        }
        //set the colIndexParent
        if (propertyNameIcdCategory != null) {
            Integer fieldNameParentIndex = prop2Index.get(propertyNameIcdCategory);
            colIndexIcdCategory = fieldNameParentIndex == null ? -1 : fieldNameParentIndex.intValue();
        }

        createInstanceColumn(fieldDef, columns, colCount);
        createParentEntityColumn(fieldDef, columns, colCount);
        createActionColumns(fieldDef, columns, colCount);

        recordDef = new RecordDef(fieldDef);

        ColumnModel columnModel = new ColumnModel(columns);
        getGridPanel().setColumnModel(columnModel);
    }


    protected void createParentEntityColumn(FieldDef[] fieldDef, ColumnConfig[] columns, int colCount) {
        ColumnConfig instCol = new ColumnConfig("", ICD_CATEGORY_DISPLAY_FIELD_NAME, 25);
        instCol.setTooltip("The parent of the category in a certain linearization.\n "
                + "Each class must have one and only one parent in every linearization.\n "
                + "Click on the cell to select a (new) parent.");
        if (fieldNameIcdCategory != null) {
            ColumnConfig userSpecParentCol = null;
            for (int i = 0; i < fieldDef.length; i++) {
                if (fieldDef[i] != null && fieldDef[i].getName().equals(fieldNameIcdCategory)) {
                    userSpecParentCol = columns[i];
                    break;
                }
            }
            if (userSpecParentCol != null) {
                instCol.setHeader(userSpecParentCol.getHeader());
                userSpecParentCol.setHeader(instCol.getHeader() + " Instance");
                String userSpecTooltip = userSpecParentCol.getTooltip();
                if (userSpecTooltip != null && userSpecTooltip.length() > 0) {
                    instCol.setTooltip(userSpecTooltip);
                }
            }
        }
        instCol.setRenderer(new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum, Store store) {
                String entityDataValue = (String) value;
                if (entityDataValue == null ) {
                    return "<DIV style=\"color:GRAY\">Click here to select a class</DIV>";
                } else if (entityDataValue.startsWith("[") && entityDataValue.endsWith("]") ) {
                    return "<DIV style=\"color:GRAY\">" + entityDataValue + "</DIV>";
                } else {
                    return entityDataValue;
                }
            }
        });

        autoExpandColId = HTMLPanel.createUniqueId();
        instCol.setId(autoExpandColId);

        fieldDef[colCount + OFFSET_ICD_CATEGORY_ENTITY_COLUMN] = new StringFieldDef(ICD_CATEGORY_DISPLAY_FIELD_NAME);
        columns[colCount + OFFSET_ICD_CATEGORY_ENTITY_COLUMN] = instCol;
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
                        if (isWriteOperationAllowed()) {
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
                } else if (colIndex == properties.size() + OFFSET_ICD_CATEGORY_ENTITY_COLUMN) {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        if (isWriteOperationAllowed()) {
                            String field = record.getFields()[colIndex];
                            selectNewICDCategory(record, field);
                        }
                    }
                } else if (offsetCommentColumn != -1 && colIndex == properties.size() + offsetCommentColumn) {
                    onCommentColumnClicked(rowIndex);
                } else {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        String valueType = record.getAsString("valueType");
                        if (valueType != null && (!valueType.equalsIgnoreCase("string"))) {
                            //TODO - implement later
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
                        if (isWriteOperationAllowed()) {
                            String field = record.getFields()[colIndex];
                            String value = record.getAsString(field);
                            if (value != null && !"".equals(value)) {
                                Menu contextMenu = new DeleteContextMenu(
                                    "Unset value (i.e. set to 'Unknown')", UIConstants.ICON_CHECKBOX_UNKNOWN,
                                    record, colIndex, ValueType.Boolean, false);
                                contextMenu.showAt(e.getXY()[0] + 5, e.getXY()[1] + 5);
                            }
                        }
                    }
                } else if (colIndex == properties.size() + OFFSET_ICD_CATEGORY_ENTITY_COLUMN) {
                    Record record = getStore().getAt(rowIndex);
                    if (record != null) {
                        if (isWriteOperationAllowed()) {
                            if (colIndexIcdCategory >= 0) {
                                String field = record.getFields()[colIndexIcdCategory];
                                String value = record.getAsString(field);
                                if (value != null && !"".equals(value)) {
                                    Menu contextMenu = new DeleteContextMenu(
                                            "Remove linearization parent", "images/delete_small_16x16.png",
                                            record, colIndexIcdCategory, ValueType.Instance, true);
                                    contextMenu.showAt(e.getXY()[0] + 5, e.getXY()[1] + 5);
                                }
                            }
                        }
                    }
                }
            }

            final class DeleteContextMenu extends Menu{
                public DeleteContextMenu(String menuText, String menuIcon, final Record record, final int colIndex, final ValueType valueType, final boolean resetParentDisplayName) {
                    MenuItem item = new MenuItem();
                    item.setText(menuText);
                    item.setIcon(menuIcon);
                    item.addListener(new BaseItemListenerAdapter() {
                        @Override
                        public void onClick(BaseItem item, EventObject e) {
                          super.onClick(item, e);
                          String field = record.getFields()[colIndex];
                          String value = record.getAsString(field);
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

    protected void selectNewICDCategory(Record record, String field) {
        currentRecord = record;
        selectWindow = getSelectionWindow();
        if (!selectWindow.isVisible()) {
            selectWindow.show();
            selectWindow.center();
        }
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
                    false, GlobalSettings.getGlobalSettings().getUserName(),
                    getReplaceValueOperationDescription(colIndex, oldValue, newValue),
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
        return OFFSET_MAX_COLUMN + 1;   //1 for the instance field
    }


    protected Window getSelectionWindow() {
        if (selectWindow == null) {
            selectWindow = new com.gwtext.client.widgets.Window();
            selectWindow.setTitle("Select parent");
            selectWindow.setWidth(600);
            selectWindow.setHeight(480);
            selectWindow.setMinWidth(300);
            selectWindow.setMinHeight(350);
            selectWindow.setLayout(new FitLayout());
            selectWindow.setPaddings(5);
            selectWindow.setButtonAlign(Position.CENTER);

            selectWindow.setCloseAction(Window.HIDE);
            selectWindow.setPlain(true);

            com.gwtext.client.widgets.Button cancelButton = new com.gwtext.client.widgets.Button("Cancel");
            cancelButton.addListener(new ButtonListenerAdapter() {
                @Override
                public void onClick(Button button, EventObject e) {
                    selectWindow.hide();
                }
            });

            com.gwtext.client.widgets.Button selectButton = new com.gwtext.client.widgets.Button("Select");
            selectButton.addListener(new ButtonListenerAdapter() {
                @Override
                public void onClick(Button button, EventObject e) {
                    Collection<EntityData> selection = getSelectable().getSelection();
                    if (selection == null || selection.size() == 0) {
                        MessageBox.alert("No selection", "No class selected. Please select a class from the tree.");
                        return;
                    }

                    if (currentRecord != null) {
                        EntityData firstSelectedParent = UIUtil.getFirstItem(selection);

                        EntityData oldValue = new EntityData(currentRecord.getAsString(fieldNameIcdCategory), currentRecord.getAsString(ICD_CATEGORY_DISPLAY_FIELD_NAME));

                        currentRecord.set(ICD_CATEGORY_DISPLAY_FIELD_NAME, firstSelectedParent.getBrowserText()); //this is optimistic
                        if (fieldNameIcdCategory != null) {
                            currentRecord.set(fieldNameIcdCategory, firstSelectedParent.getName());
                        }
                        if (colIndexIcdCategory >= 0) { //this col is wrong
                          //  int colIndex = getGridPanel().getColumnModel().getColumnCount() + OFFSET_PARENT_ENTITY_COLUMN - OFFSET_MAX_COLUMN - 1; //TT: not sure this works
                            updateInstanceValue(currentRecord, colIndexIcdCategory, oldValue, firstSelectedParent, ValueType.Instance, false); //false - because above we have already set the lin. parent name optimistically
                        }
                    }

                    selectWindow.hide();
                }
            });

            selectWindow.add((Component) getSelectable());
            selectWindow.addButton(selectButton);
            selectWindow.addButton(cancelButton);
        }
        return selectWindow;
    }


    public Selectable getSelectable() {
        String currClass = getSubject().getName();
        boolean flexibleTopClass = DefaultSearchStringTypeEnum.Entity.toString().equals(topClass);
        if (selectable == null) {
            String currTopClass = (flexibleTopClass ? currClass : topClass);;
            ClassTreePortlet selectableTree = new ICDClassTreePortlet(getProject(), true, false, false, true, currTopClass);
            selectableTree.setDraggable(false);
            selectableTree.setClosable(false);
            selectableTree.setCollapsible(false);
            selectableTree.setHeight(300);
            selectableTree.setWidth(450);
            selectable = selectableTree;
        }

        if (flexibleTopClass) {
            ((ClassTreePortlet)selectable).setTopClass(currClass);
        }
        selectable.setSelection(Collections.singleton(getSubject()));

        return selectable;
    }

}

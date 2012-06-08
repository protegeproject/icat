package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Anchor;
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
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridEditor;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.PropertyValueUtil;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceGridWidget;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class ICDLinearizationWidget extends InstanceGridWidget {

    private static final String TOP_CLASS_PROP = "topClass";

    protected static String PARENT_DISPLAY_FIELD_NAME = "@parent@";

    private static int OFFSET_PARENT_ENTITY_COLUMN = 1;

    private static int OFFSET_DELETE_COLUMN = -1;
    private static int OFFSET_COMMENT_COLUMN = 2;
    private static int OFFSET_MAX_COLUMN = OFFSET_COMMENT_COLUMN;

    private String fieldNameParent = null;
    private int colIndexParent = -1;

    private Record currentRecord;
    private Window selectWindow;
    private Selectable selectable;
    private String topClass;

    public ICDLinearizationWidget(Project project) {
        super(project);
    }

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        topClass = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), TOP_CLASS_PROP, null);
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
                ColumnConfig colConfig = createColumn((Map<String, Object>) widgetConfig.get(key), fieldDef, columns,
                        props);

                //do not allow users to rearrange instance order
                colConfig.setSortable(false);

                //hide and extract info from column storing the parent instance name
                if (colConfig.getHeader().toLowerCase().contains("parent")) {
                    fieldNameParent = colConfig.getDataIndex();
                    //either this, or there is a better way to get colIndexParent using prop2Index (see below)
                    //colIndexParent = Integer.parseInt(key.substring(FormConstants.COLUMN_PREFIX.length())) - 1; //Column1 -> index 0, Column2 -> index 1, etc.
                    colConfig.setHidden(true);
                }

                //hide and extract info from column storing the sequence number
                if (colConfig.getHeader().toLowerCase().contains("#")) {
                    colConfig.setHidden(true);
                }
                
                //make sorting label column editable
                if (colConfig.getHeader().toLowerCase().contains("sort") || 
                        colConfig.getHeader().toLowerCase().contains("label") ) {
                    colConfig.setEditor(new GridEditor(new TextField()));
                }
            }
        }

        properties = Arrays.asList(props);
        for (int i = 0; i < props.length; i++) {
            prop2Index.put(props[i], i);
        }
        //set the colIndexParent
        if (fieldNameParent != null) {
            Integer fieldNameParentIndex = prop2Index.get(fieldNameParent);
            colIndexParent = fieldNameParentIndex == null ? -1 : fieldNameParentIndex.intValue();
        }

        createInstanceColumn(fieldDef, columns, colCount);
        createParentEntityColumn(fieldDef, columns, colCount);
        createActionColumns(fieldDef, columns, colCount);

        recordDef = new RecordDef(fieldDef);

        ColumnModel columnModel = new ColumnModel(columns);
        getGridPanel().setColumnModel(columnModel);
    }

    @Override
    protected Anchor createAddNewValueHyperlink() {
        return null;
    }

    protected void createParentEntityColumn(FieldDef[] fieldDef, ColumnConfig[] columns, int colCount) {
        ColumnConfig instCol = new ColumnConfig("", PARENT_DISPLAY_FIELD_NAME, 25);
        instCol.setTooltip("The parent of the category in a certain linearization.\n "
                + "Each class must have one and only one parent in every linearization.\n "
                + "Click on the cell to select a (new) parent.");
        if (fieldNameParent != null) {
            ColumnConfig userSpecParentCol = null;
            for (int i = 0; i < fieldDef.length; i++) {
                if (fieldDef[i] != null && fieldDef[i].getName().equals(fieldNameParent)) {
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
                if (entityDataValue == null) {
                    return "<DIV style=\"color:GRAY\">Click here to select a parent</p>";
                } else {
                    return entityDataValue;
                }
            }
        });

        autoExpandColId = HTMLPanel.createUniqueId();
        instCol.setId(autoExpandColId);

        fieldDef[colCount + OFFSET_PARENT_ENTITY_COLUMN] = new StringFieldDef(PARENT_DISPLAY_FIELD_NAME);
        columns[colCount + OFFSET_PARENT_ENTITY_COLUMN] = instCol;
    }

    @Override
    protected void attachListeners() {
        getGridPanel().addGridCellListener(new GridCellListenerAdapter() {
            double timeOfLastClick = 0;

            @Override
            public void onCellClick(final GridPanel grid, final int rowIndex, final int colindex, final EventObject e) {
                double eventTime = e.getTime();
                if (eventTime - timeOfLastClick > 500) { //not the second click in a double click
                    onCellClickOrDblClick(grid, rowIndex, colindex, e);
                }
                /*
                 * Set new value for timeOfLastClick the time the last click was handled
                 * We use the current time (and not eventTime), because some time may have passed since eventTime
                 * while executing the onCellClickOrDblClick method.
                 */
                timeOfLastClick = new Date().getTime();
            }

            private void onCellClickOrDblClick(GridPanel grid, final int rowIndex, int colindex, EventObject e) {
                // int offsetDeleteColumn = getOffsetDeleteColumn();
                int offsetCommentColumn = getOffsetCommentColumn();
                if (e.getTarget(".checkbox", 1) != null) {
                    Record record = store.getAt(rowIndex);
                    if (record != null) {
                        if (UIUtil.confirmOperationAllowed(getProject())) {
                            String field = record.getFields()[colindex];
                            String value = record.getAsString(field);
                            if (Boolean.parseBoolean(value) == true) {
                                record.set(field, Boolean.FALSE.toString());
                            } else {
                                record.set(field, Boolean.TRUE.toString());
                            }
                            updateInstanceValue(record, colindex, value == null ? "" : value,
                                    record.getAsString(field), ValueType.Boolean);
                        }
                    }
                } else if (colindex == properties.size() + OFFSET_PARENT_ENTITY_COLUMN) {
                    Record record = store.getAt(rowIndex);
                    if (record != null) {
                        if (UIUtil.confirmOperationAllowed(getProject())) {
                            String field = record.getFields()[colindex];
                            selectNewParents(record, field);
                        }
                    }
                } else if (offsetCommentColumn != -1 && colindex == properties.size() + offsetCommentColumn) {
                    Record record = store.getAt(rowIndex);
                    if (record != null) {
                        if (UIUtil.confirmOperationAllowed(getProject())) {
                            onEditNotes(rowIndex);
                        }
                    }
                } else {
                    Record record = store.getAt(rowIndex);
                    if (record != null) {
                        String valueType = record.getAsString("valueType");
                        if (valueType != null && (!valueType.equalsIgnoreCase("string"))) {
                            //TODO - implement later
                        }
                    }
                }
            }
        });
    }

    protected void selectNewParents(Record record, String field) {
        currentRecord = record;
        selectWindow = getSelectionWindow();
        if (!selectWindow.isVisible()) {
            selectWindow.show();
            selectWindow.center();
        }
    }

    protected void updateInstanceValue(Record record, int colIndex, Object oldValue, Object newValue,
            ValueType valueType) {
        if (valueType == null) { //TODO: should be fixed
            valueType = ValueType.String;
        }
        String selSubject = record.getAsString(INSTANCE_FIELD_NAME);
        if (selSubject != null) {
            new PropertyValueUtil().replacePropertyValue(getProject().getProjectName(), selSubject, properties
                    .get(colIndex), valueType, null, oldValue == null ? "" : oldValue.toString(), newValue == null ? ""
                    : newValue.toString(), GlobalSettings.getGlobalSettings().getUserName(),
                    getReplaceValueOperationDescription(properties.get(colIndex), oldValue, newValue),
                    new ReplacePropertyValueHandler(new EntityData(newValue.toString(), newValue.toString())));
        }
    }

    @Override
    protected GridEditor getGridEditor(final String fieldType) {
        if (FormConstants.FIELD_TYPE_CHECKBOX.equals(fieldType)) {
            return null;
        }
        return null;
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
                        String oldValue = currentRecord.getAsString(fieldNameParent);
                        currentRecord.set(PARENT_DISPLAY_FIELD_NAME, firstSelectedParent.getBrowserText());
                        if (fieldNameParent != null) {
                            currentRecord.set(fieldNameParent, firstSelectedParent.getName());
                        }
                        if (colIndexParent >= 0) {
                            updateInstanceValue(currentRecord, colIndexParent, oldValue, firstSelectedParent.getName(),
                                    ValueType.Instance);
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
        if (selectable == null) {
            ClassTreePortlet selectableTree = new ClassTreePortlet(getProject(), false, false, false, true, topClass);
            selectableTree.setDraggable(false);
            selectableTree.setClosable(false);
            selectableTree.setCollapsible(false);
            selectableTree.setHeight(300);
            selectableTree.setWidth(450);
            selectable = selectableTree;
        }
        return selectable;
    }

    @Override
    protected void setExtraColumnValues(Object[][] data, int i, EntityPropertyValues epv) {
        super.setExtraColumnValues(data, i, epv);

        //add parent display text
        String parentName = null;
        for (PropertyEntityData ped : epv.getProperties()) {
            //data[i][getIndexOfProperty(ped.getName())] = UIUtil.commaSeparatedList(epv.getPropertyValues(ped));
            if (ped.getName().equals(fieldNameParent)) {
                List<EntityData> values = epv.getPropertyValues(ped);
                if (values != null && values.size() > 0) {
                    parentName = UIUtil.getDisplayText(values.get(0));
                    break;
                }
            }
        }

        data[i][properties.size() + OFFSET_PARENT_ENTITY_COLUMN] = parentName;
    }

    @Override
    protected String getCellText(EntityPropertyValues epv, PropertyEntityData ped) {
        if (ped.getName().equals(fieldNameParent)) {
            List<EntityData> values = epv.getPropertyValues(ped);
            if (values != null && values.size() > 0) {
                return values.get(0).getName();
            }
            return null;
        } else {
            return super.getCellText(epv, ped);
        }
    }

}

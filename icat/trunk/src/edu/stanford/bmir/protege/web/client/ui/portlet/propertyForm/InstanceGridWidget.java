package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Function;
import com.gwtext.client.core.SortDir;
import com.gwtext.client.core.TextAlign;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.BooleanFieldDef;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.IntegerFieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.ObjectFieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.Radio;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.CheckboxListenerAdapter;
import com.gwtext.client.widgets.form.event.ComboBoxCallback;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;
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
import com.gwtext.client.widgets.grid.event.GridListenerAdapter;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.MenuItem;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.PropertyValueUtil;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.ontology.search.DefaultSearchStringTypeEnum;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidgetWithNotes;
import edu.stanford.bmir.protege.web.client.ui.util.SelectionUtil;
import edu.stanford.bmir.protege.web.client.ui.util.SelectionUtil.SelectionCallback;
import edu.stanford.bmir.protege.web.client.ui.util.UIConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class InstanceGridWidget extends AbstractPropertyWidgetWithNotes {

    protected static String FIELD_NAME_INDEX_SEPARATOR = "@";
    protected static String INSTANCE_FIELD_NAME = "@instance@";
    protected static String DELETE_FIELD_NAME = "@delete@";
    protected static String COMMENT_FIELD_NAME = "@comment@";

    private static int OFFSET_DELETE_COLUMN = 1;   //use -1 if not present
    private static int OFFSET_COMMENT_COLUMN = 2;
    private static int OFFSET_MAX_COLUMN = OFFSET_COMMENT_COLUMN; //use 0 if all other column offsets are -1

    private Panel wrappingPanel;
    private EditorGridPanel grid;

    private String labelText;
    private HTML loadingIcon;

    //FIXME: Should not be protected!! Fix logic in subclasses!
    protected RecordDef recordDef;
    private Store store;
    /*
     * Workaround for grid GWT-Ext bug: does not work with ObjectFieldDef and custom renderer.
     * It contains the same data as the main store, but it stores EntityData rather than strings.
     * Needs to be kept in sync with the main store.
     */
    private Store shadowStore;
    private RecordDef shadowRecordDef;


    private EditorGridListener editorGridListener;
    //Workaround for ColumnModel bug. Keep our own map of column index to editor
    Map<Integer, GridEditor> colIndex2Editor = new HashMap<Integer, GridEditor>();

    //FIXME: Should not be protected!! Fix logic in subclasses!
    protected  List<String> properties = new ArrayList<String>(); //stores the order of cols
    //FIXME: Should not be protected!! Fix logic in subclasses!
    protected Map<String, Integer> prop2Index = new HashMap<String, Integer>();
    //FIXME: Should not be protected!! Fix logic in subclasses! Should be a list, not an array!
   // protected String[] columnEditorConfigurations;
    //FIXME: Should not be protected!! Fix logic in subclasses!
    protected String autoExpandColId;

    private String fieldNameSorted = null;

    //when creating a new instance, the editor for this column will automatically be activated
    private int defaultColumnToEdit = 0;

    private boolean multiValue = true;
    private Anchor addExistingLink;
    private Anchor addNewLink;
    private Anchor replaceExistingLink;
    private Anchor replaceNewLink;
    private com.google.gwt.user.client.ui.Panel labelPanel;

    protected PropertyValueUtil propertyValueUtil;


    public InstanceGridWidget(Project project) {
        super(project);
        propertyValueUtil = new PropertyValueUtil();
    }

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);

        if (getProperty() != null) {
            //If the property does not have a browserText, use the label of the field. This is experimental to see if it has the desirable behavior.
            String label = UIUtil.getStringConfigurationProperty(widgetConfiguration, FormConstants.LABEL, getProperty().getBrowserText());
            getProperty().setBrowserText(label);
        }
    }

    @Override
    public Component createComponent() {
        wrappingPanel = createWrappingPanel();

        labelPanel = createLabelPanel();

        grid = createGrid();
        grid.addEditorGridListener(getEditorGridListener());

        wrappingPanel.add(labelPanel);
        wrappingPanel.add(grid, new ColumnLayoutData(1));

        return wrappingPanel;
    }

    @Override
    public Component getComponent() {
        return wrappingPanel;
    }

    protected Panel createWrappingPanel() {
        Panel panel = new Panel();
        panel.setLayout(new ColumnLayout());
        panel.setPaddings(5);
        return panel;
    }

    protected com.google.gwt.user.client.ui.Panel createLabelPanel() {
        HorizontalPanel horizLabelPanel = new HorizontalPanel();
        labelText = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), FormConstants.LABEL, getProperty().getBrowserText());
        Label label = new Label();
        label.setHtml(getLabelHtml(labelText, getHelpURL(), getTooltipText()) + AbstractFieldWidget.LABEL_SEPARATOR);
        horizLabelPanel.add(label);
        loadingIcon = new HTML("<img src=\"images/invisible12.png\"/>");
        loadingIcon.setStyleName("loading-img");
        horizLabelPanel.add(loadingIcon);
        horizLabelPanel.setStyleName("form_label");

        labelPanel = new VerticalPanel();

        labelPanel.setStyleName("action_link");

        labelPanel.add(horizLabelPanel);
        createActionLinks();

        return labelPanel;
    }

    /*
     * Create action links
     */

    protected void createActionLinks() {
        if (InstanceGridWidgetConstants.showAddExistingActionLink(getWidgetConfiguration(), getProject().getProjectConfiguration())) {
            addExistingLink = createAddExistingHyperlink();
            if (addExistingLink != null) {
                labelPanel.add(addExistingLink);
            }
        }

        if (InstanceGridWidgetConstants.showAddNewActionLink(getWidgetConfiguration(), getProject().getProjectConfiguration())) {
            addNewLink = createAddNewValueHyperlink();
            if (addNewLink != null) {
                labelPanel.add(addNewLink);
            }
        }

        if (InstanceGridWidgetConstants.showReplaceExistingActionLink(getWidgetConfiguration(), getProject().getProjectConfiguration())) {
            replaceExistingLink = createReplaceExistingHyperlink();
            if (replaceExistingLink != null && isReplace()) {
                labelPanel.add(replaceExistingLink);
            }
        }

        if (InstanceGridWidgetConstants.showReplaceNewActionLink(getWidgetConfiguration(), getProject().getProjectConfiguration())) {
            replaceNewLink = createReplaceNewValueHyperlink();
            if (replaceNewLink != null && isReplace()) {
                labelPanel.add(replaceNewLink);
            }
        }
    }

    protected Anchor createAddNewValueHyperlink() {
        Anchor addNewLink = new Anchor(
                InstanceGridWidgetConstants.getAddNewLink(getWidgetConfiguration(), getProject().getProjectConfiguration()), true);
        addNewLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isWriteOperationAllowed()) {
                    onAddNewValue();
                }
            }
        });
        return addNewLink;
    }

    protected Anchor createAddExistingHyperlink() {
        Anchor addExistingLink = new Anchor(
                InstanceGridWidgetConstants.getAddExistingLink(getWidgetConfiguration(), getProject().getProjectConfiguration()), true);
        addExistingLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isWriteOperationAllowed()) {
                    onAddExistingValue();
                }
            }
        });
        return addExistingLink;
    }

    protected Anchor createReplaceNewValueHyperlink() {
        Anchor replaceNewLink = new Anchor(
                InstanceGridWidgetConstants.getReplaceNewLink(getWidgetConfiguration(), getProject().getProjectConfiguration()), true);
        replaceNewLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isWriteOperationAllowed()) {
                    onReplaceNewValue();
                }
            }
        });
        return replaceNewLink;
    }

    protected Anchor createReplaceExistingHyperlink() {
        Anchor replaceExistingLink = new Anchor(
                InstanceGridWidgetConstants.getReplaceExistingLink(getWidgetConfiguration(), getProject().getProjectConfiguration()), true);
        replaceExistingLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isWriteOperationAllowed()) {
                    onReplaceExisitingValue();
                }
            }
        });
        return replaceExistingLink;
    }


    protected void updateActionLinks(boolean isReplace) {
        if (addExistingLink != null) {
            if (isReplace) { labelPanel.remove(addExistingLink);}
                else { labelPanel.add(addExistingLink); }
        }
        if (addNewLink != null) {
            if (isReplace) { labelPanel.remove(addNewLink);}
                else { labelPanel.add(addNewLink); }
        }
        if (replaceExistingLink != null) {
            if (isReplace) { labelPanel.add(replaceExistingLink);}
                else { labelPanel.remove(replaceExistingLink); }
        }
        if (replaceNewLink != null) {
            if (isReplace) { labelPanel.add(replaceNewLink);}
                else { labelPanel.remove(replaceNewLink); }
        }
    }


    /*
     * Grid actions (on add, on replace, on delete, etc.)
     */

    protected void onAddNewValue() {
        List<EntityData> allowedValues = getProperty().getAllowedValues();
        String type = null;
        if (allowedValues != null && !allowedValues.isEmpty()) {
            type = allowedValues.iterator().next().getName();
        }

        OntologyServiceManager.getInstance().createInstanceValue(getProject().getProjectName(), null, type,
                getSubject().getName(), getProperty().getName(), GlobalSettings.getGlobalSettings().getUserName(),
                getAddValueOperationDescription(), new AddPropertyValueHandler());
    }


    protected void onAddExistingValue() {
        String type = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), FormConstants.ONT_TYPE, null);
        if (type == null) { return;  } //TODO: not type specified, maybe use range of property

        SelectionUtil.selectIndividuals(getProject(), UIUtil.createCollection(new EntityData(type)), true, false, new SelectionCallback() {
            public void onSelect(Collection<EntityData> selection) {
                addExistingValues(selection);
            }
        });
    }

    protected void addExistingValues(Collection<EntityData> values) {
        //TODO: later optimize this in a single remote call
        for (EntityData value : values) {
            OntologyServiceManager.getInstance().addPropertyValue(getProject().getProjectName(), getSubject().getName(), getProperty(), value,
                    GlobalSettings.getGlobalSettings().getUserName(), getAddExistingOperationDescription(value), new AddExistingValueHandler(getSubject()));
        }
    }

    protected void onInsertNewValue(EntityData newInstance) {
        Object[] empty = new Object[properties.size() + getExtraColumnCount()];
        empty[properties.size()] = newInstance.getName();
        setExtraColumnValues(empty, new EntityPropertyValues(newInstance));

        Record newRecord = recordDef.createRecord(empty);
        grid.stopEditing();
        store.insert(0, newRecord);
        shadowStore.insert(0, shadowRecordDef.createRecord(new Object[properties.size()]));

        if (hasGridEditor(defaultColumnToEdit)) {
            grid.startEditing(0, defaultColumnToEdit);
        } else {
            onValueColumnClicked(grid, 0, defaultColumnToEdit);
        }
    }

    private boolean hasGridEditor(int columnIndex) {
        return colIndex2Editor.get(columnIndex) != null;
    }

    protected void onReplaceNewValue() {
        onDelete(0);
        onAddNewValue();
    }

    protected void onReplaceExisitingValue() {
        onDelete(0);
        onAddExistingValue();
    }

    protected void onEditNotes(int index) {
        Record record = store.getAt(index);
        String value = record.getAsString(INSTANCE_FIELD_NAME);
        super.onEditNotes(value);
    }

    protected void onDeleteColumnValue(final Record record, final int rowIndex, final int colIndex) {
        String field = record.getFields()[colIndex];
        EntityData oldValue = getEntityDataValueAt(record, rowIndex, colIndex);

        record.set(field, (String)null);
        shadowStore.getRecordAt(rowIndex).set(field, (EntityData)null);

        changeValue(record, null, oldValue, rowIndex, colIndex);
    }

    protected void onDelete(int index) {
        Record record = store.getAt(index);
        String value = record.getAsString(INSTANCE_FIELD_NAME);
        if (value != null) {
            propertyValueUtil.deletePropertyValue(getProject().getProjectName(), getSubject().getName(),
                    getProperty().getName(), ValueType.Instance, value, GlobalSettings.getGlobalSettings()
                    .getUserName(), getDeleteValueOperationDescription(index), new RemovePropertyValueHandler(
                            index));

        }
    }


    /*
     * Operation descriptions
     */
    protected String getAddValueOperationDescription() {
        return UIUtil.getAppliedToTransactionString("Added a new "
                + UIUtil.getShortName(getProperty().getBrowserText()) + " to " + getSubject().getBrowserText(),
                getSubject().getName());
    }

    protected String getAddExistingOperationDescription(EntityData value) {
        return UIUtil.getAppliedToTransactionString("Added " + UIUtil.getDisplayText(value) + " as "
                + UIUtil.getShortName(getProperty().getBrowserText()) + " to " + getSubject().getBrowserText(),
                getSubject().getName());
    }

    protected String getReplaceValueOperationDescription(int colIndex, Object oldValue, Object newValue) {
        String header = grid.getColumnModel().getColumnHeader(colIndex);
        header = header == null ? "(no header)" : header;

        oldValue = UIUtil.getDisplayText(oldValue);
        oldValue = ((String)oldValue).length() == 0 ? "(empty)" : oldValue;
        newValue = UIUtil.getDisplayText(newValue);
        newValue = ((String)newValue).length() == 0 ? "(empty)" : newValue;

        return UIUtil.getAppliedToTransactionString("Replaced '" + header + "' for '"
                + UIUtil.getDisplayText(getProperty()) + "' of " + getSubject().getBrowserText()
                + ". Old value: " + oldValue +
                ". New value: " + newValue,
                getSubject().getName());
    }

    protected String getDeleteValueOperationDescription(int index) {
        Record record = store.getAt(index);
        String value = record.getAsString(INSTANCE_FIELD_NAME);
        String deletedValueDesc = new String("(");
        String[] fields = record.getFields();
        if (fields.length > getMaxColumnOffset()) {
            for (int i = 0; i < fields.length - getMaxColumnOffset(); i++) {
                if (!grid.getColumnModel().isHidden(i)) {
                    String fieldValue = record.getAsString(fields[i]);
                    String fieldHeader = grid.getColumnModel().getColumnHeader(i);
                    String fieldValuePair = (fieldHeader == null ? "no field header" : fieldHeader) + ": " + (fieldValue == null ? "empty" : fieldValue);
                    deletedValueDesc = deletedValueDesc + fieldValuePair + ", ";
                }
            }
            deletedValueDesc = deletedValueDesc.substring(0, deletedValueDesc.length() - 2);
        }
        deletedValueDesc = deletedValueDesc + ")";

        return UIUtil.getAppliedToTransactionString("Deleted " + UIUtil.getShortName(getProperty().getBrowserText())
                + " from " + getSubject().getBrowserText() + ". Deleted value: "
                + (value == null || value.toString().length() == 0 ? "(empty)" : deletedValueDesc), getSubject()
                .getName());
    }


    /*
     * Grid creation
     */

    protected EditorGridPanel createGrid() {
        grid = new EditorGridPanel();

        grid.setCls("form_grid");
        grid.setAutoWidth(true);
        grid.setStripeRows(true);
        grid.setClicksToEdit(getClicksToEdit());
        grid.setEnableHdMenu(getEnableHeaderMenu());
        grid.setFrame(true);

        Map<String, Object> widgetConfig = getWidgetConfiguration();
        if (widgetConfig != null) {
            String heigthStr = (String) widgetConfig.get(FormConstants.HEIGHT);
            if (heigthStr != null) {
                grid.setHeight(Integer.parseInt(heigthStr));
            } else {
                grid.setHeight(110);
            }

            multiValue = UIUtil.getBooleanConfigurationProperty(widgetConfig, FormConstants.MULTIPLE_VALUES_ALLOWED, true);
        }

        createColumns();
        createStore();
        attachListeners();

        if (autoExpandColId != null) {
            grid.setAutoExpandColumn(autoExpandColId);
        }

        grid.getView().setScrollOffset(25);

        //default height of grid header (25) + default height of a row (25) + default height of horiz. scrollbar (20)
        if (grid.getHeight() < 25 + 25 + 20) {
            grid.setAutoScroll(false);
        }

        return grid;
    }

	protected int getClicksToEdit() {
		return UIUtil.getIntegerConfigurationProperty(
                getProject().getProjectConfiguration(),
                FormConstants.CLICKS_TO_EDIT,
                FormConstants.DEFAULT_CLICKS_TO_EDIT);
	}

	protected boolean getOneClickComboboxEditingEnabled() {
		return UIUtil.getBooleanConfigurationProperty(
                getProject().getProjectConfiguration(),
                FormConstants.ONE_CLICK_COMBOBOX_EDITING,
                true);
	}
	
	protected boolean getEnableHeaderMenu() {
		return UIUtil.getBooleanConfigurationProperty(
                getWidgetConfiguration(), getProject().getProjectConfiguration(), 
                FormConstants.ENABLE_HEADER_MENU, getEnableHeaderMenuDefault());
	}

    protected boolean getEnableHeaderMenuDefault() {
		return true;
	}

	protected boolean getIsSortable(Map<String, Object> columnConfig) {
		boolean isSortableGrid = UIUtil.getBooleanConfigurationProperty(getWidgetConfiguration(), FormConstants.IS_SORTABLE, getIsSortableDefault());
		return UIUtil.getBooleanConfigurationProperty(columnConfig, FormConstants.IS_SORTABLE, isSortableGrid);
	}

    protected boolean getIsSortableDefault() {
		return true;
	}

	protected String getPropertyFieldName(final int colIndex) {
		return getPropertyFieldName(properties.get(colIndex), colIndex);
	}

	protected String getPropertyFieldName(final String property, final int colIndex) {
		return property + FIELD_NAME_INDEX_SEPARATOR + colIndex;
	}

	protected void createStore() {
        ArrayReader reader = new ArrayReader(recordDef);
        MemoryProxy dataProxy = new MemoryProxy(new Object[][] {});
        store = new Store(dataProxy, reader);
        grid.setStore(store);
        store.load();

        //create also shadow store
        createShadowStore();
    }


    private void createShadowStore() {
        FieldDef[] fieldDefs = new FieldDef[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            fieldDefs[i] = new ObjectFieldDef(getPropertyFieldName(i));
        }
        shadowRecordDef = new RecordDef(fieldDefs);

        ArrayReader reader = new ArrayReader(shadowRecordDef);
        MemoryProxy dataProxy = new MemoryProxy(new Object[][] {});
        shadowStore = new Store(dataProxy, reader);
        shadowStore.load();
    }

    public GridPanel getGridPanel() {
        return grid;
    }

    protected void attachListeners() {
        //TODO: may not work so well.. - check indexes
		grid.addGridCellListener(getGridMouseListener());

        //TODO we should find a solution for removing the cell selection once focus is lost (i.e. another widget is selected)
//        grid.addListener(Event.ONBLUR, new Function() {
//            public void execute() {
//                System.out.println("Focus out!!!");
//            }
//        });

        grid.addGridListener(new GridListenerAdapter(){
            @Override
            public void onKeyPress(EventObject e) {
                int key = e.getKey();
                if (key == EventObject.ENTER && !e.isCtrlKey()) {
                    int[] selectedCell = grid.getCellSelectionModel().getSelectedCell();
                    if (selectedCell != null) {
                        onValueColumnClicked(grid, selectedCell[0], selectedCell[1]);
                    }
                }
            }
        });
    }

	protected InstanceGridCellMouseListener getGridMouseListener() {
		return new InstanceGridCellMouseListener();
	}

    protected void onDeleteColumnClicked(final int rowIndex) {
        Record record = store.getAt(rowIndex);
        if (record != null) {
            boolean value = record.getAsBoolean(DELETE_FIELD_NAME);
            if (value == false) {
                return; //read only value
            }
            if (isWriteOperationAllowed()) {
                MessageBox.confirm("Confirm", "Are you sure you want to delete this value?",
                        new MessageBox.ConfirmCallback() {
                    public void execute(String btnID) {
                        if (btnID.equals("yes")) {
                            onDelete(rowIndex);
                        }
                    }
                });
            }
        }
    }

    protected void onCommentColumnClicked(final int rowIndex) {
        Record record = store.getAt(rowIndex);
        if (record != null) {
            if (UIUtil.confirmOperationAllowed(getProject())) {
                onEditNotes(rowIndex);
            }
        }
    }

    protected void onValueColumnClicked(final GridPanel grid, final int rowIndex, final int colIndex) {
        //To be overridden in subclasses, if needed

        //FIXME: must treat differently if clicked to edit or to view!

        final Record record = store.getAt(rowIndex);
        String gridEditorOption = (String) getColumnConfiguration(colIndex, FormConstants.FIELD_EDITOR);

        if (!isWriteOperationAllowed()) {
            return;
        }

        //text editing
        if (record != null && gridEditorOption != null) {
            if (gridEditorOption.equals(FormConstants.FIELD_EDITOR_MULTILINE)) {
                editWithPopupGridEditor(PopupGridEditor.TEXT_AREA, grid, rowIndex, colIndex, record);
            } else if (gridEditorOption.equals(FormConstants.FIELD_EDITOR_HTML)) {
                editWithPopupGridEditor(PopupGridEditor.HTML, grid, rowIndex, colIndex, record);
            }
        }

        String fieldType = (String) getColumnConfiguration(colIndex, FormConstants.FIELD_TYPE);
        if (fieldType == null) {
            return;
        }

        //other value types editing
        if (fieldType.equals(FormConstants.FIELD_TYPE_CLASS)) {
            editClassFieldType(record, rowIndex, colIndex);
        }else if (fieldType.equals(FormConstants.FIELD_TYPE_INSTANCE)) {
            editInstanceFieldType(record, rowIndex, colIndex);
        }
    }


    private void editWithPopupGridEditor(final PopupGridEditor editor, final GridPanel grid, final int rowIndex, final int colIndex, final Record record) {
        final String field = record.getFields()[colIndex];
        final String value = record.getAsString(field);
        editor.show(labelText, grid.getColumnModel().getColumnHeader(colIndex), value, isWriteOperationAllowed(false));
        editor.setCallbackFunction( new Function() {
            public void execute() {
                if (editor.hasValueChanged()) {
                    String newValue = editor.getValue();
                    record.set(field, newValue);
                    changeValue(record, newValue, value, rowIndex, colIndex);
                }
            }
        });
    }

    private void editInstanceFieldType(final Record record, final int rowIndex, final int colIndex) {
        final EntityData oldValue = getEntityDataValueAt(record, rowIndex, colIndex);

        Collection<EntityData> clses = null;

        String topCls = (String) getColumnConfiguration(colIndex, FormConstants.ONT_TYPE);
        if (topCls != null) {
            if (topCls.equals(DefaultSearchStringTypeEnum.Entity.toString())) {
                clses = UIUtil.createCollection(getSubject());
            }
            else {
                clses = UIUtil.createCollection(new EntityData(topCls));
            }
        }

        SelectionUtil.selectIndividuals(getProject(), clses, false, true, new SelectionCallback() {
            public void onSelect(Collection<EntityData> selection) {
                replaceEntityValue(record, oldValue, rowIndex, colIndex, selection);
            }
        });
    }

    private void editClassFieldType(final Record record, final int rowIndex, final int colIndex) {
        final Object oldValue = getEntityDataValueAt(record, rowIndex, colIndex);

        String topCls = (String) getColumnConfiguration(colIndex, FormConstants.TOP_CLASS);

        if (topCls != null && topCls.equals(DefaultSearchStringTypeEnum.Entity.toString())) {
            topCls = getSubject().getName();
        }

        SelectionUtil.selectClses(getProject(), false, topCls, new SelectionCallback() {
            public void onSelect(Collection<EntityData> selection) {
                replaceEntityValue(record, oldValue, rowIndex, colIndex, selection);
            }
        });
    }

    private void replaceEntityValue(Record record, Object oldValue,  int rowIndex,  int colIndex, Collection<EntityData> newValues) {
        //deal with one value for now, could be modified to handle all values
        if (newValues == null || newValues.size() == 0) {
            return;
        }
        //TODO: how to deal with deletion of value
        EntityData newValue = UIUtil.getFirstItem(newValues);
        final String field = record.getFields()[colIndex];
        record.set(field, UIUtil.getDisplayText(newValue));
        shadowStore.getRecordAt(rowIndex).set(field, newValue);

        changeValue(record, newValue, oldValue, rowIndex, colIndex);
    }

    protected void onContextMenuCheckboxClicked(final int rowIndex, final int colIndex,  final EventObject e) {
        final Record record = store.getAt(rowIndex);
        if (record != null) {
            if (isWriteOperationAllowed()) {
                String field = record.getFields()[colIndex];
                String value = record.getAsString(field);
                if (value != null && !"".equals(value)) {
                    Menu contextMenu = new DeleteContextMenu(
                            "Unset value (i.e. set to 'Unknown')", UIConstants.ICON_CHECKBOX_UNKNOWN,
                            record, rowIndex, colIndex);
                    contextMenu.showAt(e.getXY()[0] + 5, e.getXY()[1] + 5);
                }
            }
        }
    }

    protected void onContextMenuClicked(final int rowIndex, final int colIndex,  final EventObject e) {
        final Record record = store.getAt(rowIndex);
        if (record != null) {
            if (isWriteOperationAllowed()) {
                String colType = (String)getColumnConfiguration(colIndex, FormConstants.FIELD_TYPE);
                if (ValueType.Instance.toString().equalsIgnoreCase(colType) ||
                        ValueType.Cls.toString().equalsIgnoreCase(colType) ||
                        ValueType.Class.toString().equalsIgnoreCase(colType)) {
                    String field = record.getFields()[colIndex];
                    String value = record.getAsString(field);
                    if (value != null && !value.isEmpty()) {
                        Menu contextMenu = new DeleteContextMenu(
                                "Remove this value", "images/delete_small_16x16.png",
                                record, rowIndex, colIndex);
                        contextMenu.showAt(e.getXY()[0] + 5, e.getXY()[1] + 5);
                    }
                }
            }
        }
    }

    protected EditorGridListener getEditorGridListener() {
        if (editorGridListener == null) {
            editorGridListener = new EditorGridListenerAdapter() {
                @Override
                public boolean doBeforeEdit(GridPanel grid, Record record, String field, Object value, int rowIndex, int colIndex) {
                    if (!isWriteOperationAllowed()) {
                        return false;
                    }
                    String valueType = record.getAsString("valueType");
                    return valueType == null || valueType.equalsIgnoreCase("string") || valueType.equalsIgnoreCase("any"); //TODO: allow only the editing of String values for now
                }

                @Override
                public void onAfterEdit(GridPanel grid, Record record, String field, Object newValue, Object oldValue,
                        int rowIndex, int colIndex) {
                    EntityData oldValueEntityData = getEntityDataValueAt(record, rowIndex,colIndex);
                    changeValue(record, newValue, oldValueEntityData, rowIndex, colIndex);
                }
            };
        }
        return editorGridListener;
    }


	private EntityData getEntityDataValueAt(Record record, int rowIndex, int colIndex) {
		//this would work only for columns that represent property names
		//String field = getPropertyFieldName(colIndex);
		String field = record.getFields()[colIndex];

		return (EntityData) shadowStore.getRecordAt(rowIndex).getAsObject(field);
	}

    private void changeValue(Record record, Object newValue, Object oldValue, int rowIndex, int colIndex) {
        //special handling rdfs:Literal
        String valueType = record.getAsString("valueType");
       // if (valueType == null) { //TODO: should be fixed
       //     valueType = ValueType.String.name();
       // }
        String selSubject = getSubjectOfPropertyValue(record, rowIndex, colIndex);
        if (selSubject != null) {
            //FIXME: don't use strings for the values, but entity data
            propertyValueUtil.replacePropertyValue(getProject().getProjectName(), selSubject,
                    properties.get(colIndex), valueType == null ? null : ValueType.valueOf(valueType),
                            getStringValue(oldValue),
                            getStringValue(newValue),
                            GlobalSettings.getGlobalSettings().getUserName(),
                            getReplaceValueOperationDescription(colIndex, oldValue, newValue),
                            new ReplacePropertyValueHandler(new EntityData(newValue == null ? null : newValue.toString(),
                                    newValue == null ? null : newValue.toString())));
        }
    }

    protected String getSubjectOfPropertyValue(Record record, int rowIndex, int colIndex) {
    	return record.getAsString(INSTANCE_FIELD_NAME);
    }
    
    private String getStringValue(Object value) {
        if (value == null) { return null; }
        if (value instanceof EntityData) {
            return ((EntityData)value).getName();
        }
        return value.toString();
    }

    /**
     * Creates the column configurations for this grid, based on the widget configuration.
     * <br><br>
     * <b>IMPORTNAT!!!</b> Please be careful when changing this method, and add <b>all relevant changes</b> also to 
     * the subclasses that override this method (especially {@link MultilevelInstanceGridWidget}).
     */
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
                prop2Index.put(property, index);

                String cloneOf = isCloneColumn(columnConfig);
                if (cloneOf != null) {
                	Map<String, Object> origColumnConfig = getOriginalOfClone(widgetConfig, columnConfig);
                	createCloneColumn(columnConfig, origColumnConfig, cloneOf, fieldDef, columns, property, index);
                }
                else {
                	createColumn(columnConfig, fieldDef, columns, property, index);
                }
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
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                    Store store) {
                String strValue = (String) value;
                return (strValue != null && strValue.contains("#") ? strValue.substring(strValue.lastIndexOf("#")) : strValue);
            }
        });
        instCol.setHidden(true);

        fieldDef[colCount] = new StringFieldDef(INSTANCE_FIELD_NAME);
        columns[colCount] = instCol;
    }
    
    protected void createActionColumns(FieldDef[] fieldDef, ColumnConfig[] columns, int colCount) {
        int offsetDeleteColumn = getOffsetDeleteColumn();
        if (offsetDeleteColumn != -1) {
            ColumnConfig deleteCol = createDeleteColumn();
            fieldDef[colCount + offsetDeleteColumn] = new BooleanFieldDef(DELETE_FIELD_NAME);
            columns[colCount + offsetDeleteColumn] = deleteCol;
        }

        int offsetCommentColumn = getOffsetCommentColumn();
        if (offsetCommentColumn != -1) {
            ColumnConfig commentCol = createCommentsColumn();
            fieldDef[colCount + offsetCommentColumn] = new IntegerFieldDef(COMMENT_FIELD_NAME);
            columns[colCount + offsetCommentColumn] = commentCol;
        }
    }

    protected ColumnConfig createDeleteColumn() {
        ColumnConfig deleteCol = new ColumnConfig("", DELETE_FIELD_NAME, 25);
        deleteCol.setTooltip("Delete this value");

        deleteCol.setRenderer(createDeleteColumnRenderer());
        return deleteCol;
    }

    protected Renderer createDeleteColumnRenderer() {
        return new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum, Store store) {
                boolean isDeleteEnabled = (Boolean) value;
                return isDeleteEnabled ?
                        "<img src=\"images/delete.png\" title=\" Click on the icon to remove value.\"></img>" :
                        "<img src=\"images/delete_grey.png\" title=\" Delete is disabled for this value.\"></img>"  ;
            }
        };
    }

    //TODO: refactor
    protected ColumnConfig createCommentsColumn() {
        ColumnConfig commentCol = new ColumnConfig("", COMMENT_FIELD_NAME, 40);
        commentCol.setTooltip("Add a comment on this value");
        commentCol.setRenderer(createCommentColumnRenderer());
        return commentCol;
    }

    protected Renderer createCommentColumnRenderer() {
        return new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                    Store store) {
                // TODO: add a css for this
                String text = "<img src=\"images/comment.gif\" title=\""
                    + " Click on the icon to add new note(s).\"></img>";
                int annotationsCount = (value == null ? 0 : value instanceof Integer ? ((Integer) value) : ((Long) value).intValue());
                if (annotationsCount > 0) {
                    text = "<img src=\"images/comment.gif\" title=\""
                        + UIUtil.getNiceNoteCountText(annotationsCount)
                        + " on this value. \nClick on the icon to see existing or to add new note(s).\"></img>"
                        + "<span style=\"vertical-align:super;font-size:95%;color:#15428B;font-weight:bold;\">"
                        + "&nbsp;" + annotationsCount + "</span>";
                }
                return text;
            }
        };
    }

	protected String isCloneColumn(Map<String, Object> columnConfig) {
        return UIUtil.getStringConfigurationProperty(columnConfig, FormConstants.CLONE_OF, null);
	}

	protected Map<String, Object> getOriginalOfClone(
			Map<String, Object> widgetConfig, Map<String, Object> columnConfig) {
        String cloneOf = UIUtil.getStringConfigurationProperty(columnConfig, FormConstants.CLONE_OF, null);
        if (cloneOf != null) {
        	return (Map<String, Object>) widgetConfig.get(cloneOf);
        }
        else {
        	//TODO error
        	return null;
        }
	}

    protected ColumnConfig createCloneColumn(Map<String, Object> columnConfig,
			Map<String, Object> origColumnConfig, String originalCol, 
			FieldDef[] fieldDef, ColumnConfig[] columnConfigs, String property, int index) {
        ColumnConfig gridColConfig = new ColumnConfig();
        
        gridColConfig.setDataIndex(getPropertyFieldName(property, index));

        int origColIndex = getColumnIndexFromConfig(origColumnConfig);

        initializeColumnStyle(columnConfig, gridColConfig);
        initializeColumnHeader(origColumnConfig, gridColConfig);
        initializeColumnTooltip(origColumnConfig, gridColConfig);
        initializeColumnWidth(origColumnConfig, gridColConfig);
        initializeColumnHiddenFlag(origColumnConfig, gridColConfig);
        initializeColumnDefaultToEdit(origColumnConfig, index);
        initializeColumnAlignment(origColumnConfig, gridColConfig);
        initializeColumnSortedFlag(origColumnConfig, gridColConfig);
        initializeCloneColumnRenderer(origColIndex, origColumnConfig, gridColConfig);

        //initializeColumnGridEditor(index, columnConfig, gridColConfig);

        //TODO: support other types as well
        fieldDef[index] = new StringFieldDef(getPropertyFieldName(property, index));
        columnConfigs[index] = gridColConfig;

        return gridColConfig;
	}

    //FIXME: protect against invalid config xml
    protected ColumnConfig createColumn(Map<String, Object> columnConfig, 
    		FieldDef[] fieldDef, ColumnConfig[] columnConfigs, String property, int index) {
        ColumnConfig gridColConfig = new ColumnConfig();
        
        gridColConfig.setDataIndex(getPropertyFieldName(property, index));

        initializeColumnStyle(columnConfig, gridColConfig);
        initializeColumnHeader(columnConfig, gridColConfig);
        initializeColumnTooltip(columnConfig, gridColConfig);
        initializeColumnWidth(columnConfig, gridColConfig);
        initializeColumnHiddenFlag(columnConfig, gridColConfig);
        initializeColumnDefaultToEdit(columnConfig, index);
        initializeColumnAlignment(columnConfig, gridColConfig);
        initializeColumnSortedFlag(columnConfig, gridColConfig);
        initializeColumnRenderer(columnConfig, gridColConfig);

        initializeColumnGridEditor(index, columnConfig, gridColConfig);

        //TODO: support other types as well
        fieldDef[index] = new StringFieldDef(getPropertyFieldName(property, index));
        columnConfigs[index] = gridColConfig;

        return gridColConfig;
    }

	protected final String getPropertyNameFromConfig(Map<String, Object> columnConfig) {
		String property = (String) columnConfig.get(FormConstants.PROPERTY); //better not be null
		return property;
	}

	protected final int getColumnIndexFromConfig(Map<String, Object> columnConfig) {
		String indexStr = (String) columnConfig.get(FormConstants.INDEX);
        int index = Integer.parseInt(indexStr); //better be valid
		return index;
	}
    
    private void initializeCloneColumnRenderer(int origColIndex, Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
    	String fieldType = (String) columnConfig.get(FormConstants.FIELD_TYPE);
    	gridColConfig.setRenderer(createCloneColumnRenderer(origColIndex, fieldType, columnConfig));
    }

    private void initializeColumnRenderer(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        String fieldType = (String) columnConfig.get(FormConstants.FIELD_TYPE);
        gridColConfig.setRenderer(createColumnRenderer(fieldType, columnConfig));
    }

    private void initializeColumnStyle(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        gridColConfig.setResizable(true);
        gridColConfig.setSortable(getIsSortable(columnConfig));
        String bgColor = (String) columnConfig.get(FormConstants.FIELD_BG_COLOR);
        String bgColorStatement = "";
        if (bgColor != null) {
        	bgColorStatement = " background-color: " + bgColor + ";";
        	//alternatively we could set the cell style by specifying CSS classes
        	//for the CellMetadata but apparently the only place we can do this 
        	//would be in the renderer and it would be rather expensive to do that
        	//every time the render is called
        }
        gridColConfig.setCss("word-wrap: break-word;" + bgColorStatement);
    }

	private void initializeColumnSortedFlag(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        Boolean sorted = (Boolean) columnConfig.get(FormConstants.SORTED);
        if (Boolean.TRUE.equals(sorted)) {
            fieldNameSorted = gridColConfig.getDataIndex();
        }
    }

    private void initializeColumnGridEditor(int index, Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        String fieldType = (String) columnConfig.get(FormConstants.FIELD_TYPE);

        GridEditor editor = createGridEditor(fieldType, columnConfig);

        if (editor != null) {
            gridColConfig.setEditor(editor);
            colIndex2Editor.put(index, editor);
        }
    }

    private void initializeColumnAlignment(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        String fieldTextAlign = (String) columnConfig.get(FormConstants.FIELD_ALIGN);
        if (fieldTextAlign != null) {
            gridColConfig.setAlign(getTextAlign(fieldTextAlign));
        }
        //we could center checkboxes and radio buttons by default...
        //else if (FormConstants.FIELD_TYPE_CHECKBOX.equals(fieldType)
        //        || FormConstants.FIELD_TYPE_RADIO.equals(fieldType)) {
        //    colConfig.setAlign(getTextAlign("center"));
        //}
    }

    private void initializeColumnDefaultToEdit(Map<String, Object> columnConfig, int index) {
        Boolean defaultColToEdit = UIUtil.getBooleanConfigurationProperty(columnConfig, FormConstants.DEFAULT_COLUMN_TO_EDIT, false);
        if (defaultColToEdit != null && defaultColToEdit == true) {
            defaultColumnToEdit = index;
        }
    }

    private void initializeColumnHiddenFlag(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        boolean hidden = UIUtil.getBooleanConfigurationProperty(columnConfig, FormConstants.HIDDEN, false);
        if (hidden) {
            gridColConfig.setHidden(true);
        }
    }

    private void initializeColumnWidth(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        String widthStr = (String) columnConfig.get(FormConstants.WIDTH);
        if (widthStr != null) {
            if (widthStr.equalsIgnoreCase(FormConstants.WIDTH_ALL)) {
                autoExpandColId = HTMLPanel.createUniqueId();
                gridColConfig.setId(autoExpandColId);
            } else {
                int width = Integer.parseInt(widthStr);
                gridColConfig.setWidth(width);
            }
        }
    }

    protected void initializeColumnTooltip(Map<String, Object> columnConfig, ColumnConfig gridColConfig) {
        String tooltip = (String) columnConfig.get(FormConstants.TOOLTIP);
        if (tooltip != null) {
            gridColConfig.setTooltip(tooltip);
        }
    }

    protected void initializeColumnHeader(Map<String, Object> columnConfig, ColumnConfig gridColumnConfig) {
        String header = (String) columnConfig.get(FormConstants.HEADER);
        String htmlHeader = (header == null ? "" : header);
        
        String headerCss = (String) columnConfig.get(FormConstants.HEADER_CSS_CLASS);
        
        String height = (String) columnConfig.get(FormConstants.HEIGHT);
        String heightStr = (height == null ? "" : 
        	"height:"+ (height.endsWith("px") ? height : height + "px") + ";");
        	
        if (headerCss != null || height != null) {
        	htmlHeader = "<span class=" + headerCss + " style=\"" + heightStr + "\">" + htmlHeader + "</span>";
        }
        gridColumnConfig.setHeader(htmlHeader);
    }



    protected int getIndexOfProperty(String prop) {
        return prop2Index.get(prop);
    }

    protected TextAlign getTextAlign(String fieldTextAlign) {
        if (fieldTextAlign == null ||
                fieldTextAlign.equalsIgnoreCase("left")) {
            return TextAlign.LEFT;
        }
        if (fieldTextAlign.equalsIgnoreCase("center")) {
            return TextAlign.CENTER;
        }
        if (fieldTextAlign.equalsIgnoreCase("right")) {
            return TextAlign.RIGHT;
        }
        if (fieldTextAlign.equalsIgnoreCase("justify")) {
            return TextAlign.JUSTIFY;
        }
        return TextAlign.LEFT;
    }


    protected GridEditor createGridEditor(final String fieldType, final Map<String, Object> config) {

        if (fieldType != null) {
            if (fieldType.equals(FormConstants.FIELD_TYPE_COMBOBOX)) {
                return createComboBoxGridEditor(config);
            } else if (fieldType.equals(FormConstants.FIELD_TYPE_CLASS)) {
                return createClassGridEditor(config);
            } else if (fieldType.equals(FormConstants.FIELD_TYPE_INSTANCE)) {
                return createInstanceGridEditor(config);
            }
        }

        //TODO - use a text area as the default editor for now, support more later
        String gridEditorOption = (String) config.get(FormConstants.FIELD_EDITOR);

        //null is default
        if (gridEditorOption == null || gridEditorOption.equals(FormConstants.FIELD_EDITOR_INLINE)) {
            return new GridEditor(new TextField());
        } else if (gridEditorOption.equals(FormConstants.FIELD_EDITOR_MULTILINE)) {
            return null;
        } else if (gridEditorOption.equals(FormConstants.FIELD_EDITOR_HTML)) {
            return null;
        } else if (gridEditorOption.equals(FormConstants.FIELD_EDITOR_FLEXIBLE)) {
            return createFlexibleGridEditor();
        }

        //in other cases: create text field or text area depending on the size of the grid.
        //This behavior probably does not make sense anymore, and we should treat all the
        //different valid editor options above
        return createFlexibleGridEditor() ;
    }

    private GridEditor createFlexibleGridEditor() {
        TextField textEditor;
        if (grid.getHeight() < 25 + 50) {	//default height of grid header (25) + default height of a TextArea (50)
            textEditor = new TextField();
        } else {
            textEditor = new TextArea();
        }
        return new GridEditor(textEditor);
    }

    private GridEditor createComboBoxGridEditor(final Map<String, Object> config) {
        Map<String, String> allowedValues = UIUtil.getAllowedValuesConfigurationProperty(config);

        String[][] displayValues;
        if (allowedValues == null) {
            displayValues = new String[][]{};
        }
        else {
            displayValues = new String[allowedValues.size()][2];
            int i=0;
            for (String key : allowedValues.keySet()) {
                displayValues[i][0] = key;
                displayValues[i][1] = allowedValues.get(key);
                i++;
            }
        }
        SimpleStore cbStore = new SimpleStore(new String[]{"displayText", "value"}, displayValues);
        cbStore.load();

        ComboBox cb = new ComboBox();
        cb.setStore(cbStore);
        cb.setDisplayField("displayText");
        cb.setValueField("value");
        boolean allowedValuesOnly = UIUtil.getBooleanConfigurationProperty(config, FormConstants.ALLOWED_VALUES_ONLY, true);

        cb.setForceSelection(allowedValuesOnly);
        if (! allowedValuesOnly) {
            //Apparently we need to add this because the 'setForceSelection(boolean)' method
            //is not doing what it suppose to by itself, and we need to enforce the raw value to be used.
            cb.addListener(new ComboBoxListenerAdapter() {
                @Override
                public boolean doBeforeQuery(ComboBox comboBox, ComboBoxCallback cbcb) {
                    String lastQueried = comboBox.getRawValue();
                    comboBox.setValue(lastQueried);
                    return true;
                }
            });
        }
        return new GridEditor(cb);
    }

    private GridEditor createInstanceGridEditor(Map<String, Object> config) {
        return null;
    }

    private GridEditor createClassGridEditor(Map<String, Object> config) {
        return null;
    }

    
    protected Renderer createCloneColumnRenderer(final int origColIndex, final String fieldType, Map<String, Object> config) {
//    	if (FormConstants.FIELD_TYPE_COMBOBOX.equals(fieldType)) {
//    		return createComboBoxFieldRenderer(fieldType, config);
//    	}
//    	
    	InstanceGridColumnRenderer renderer = new InstanceGridCloneColumnRenderer(fieldType, origColIndex);
    	return renderer;
    }

    protected Renderer createColumnRenderer(final String fieldType, Map<String, Object> config) {
        if (FormConstants.FIELD_TYPE_COMBOBOX.equals(fieldType)) {
            return createComboBoxFieldRenderer(fieldType, config);
        }

        InstanceGridColumnRenderer renderer = new InstanceGridColumnRenderer(fieldType, null, getColumnEmptyText(config));
        return renderer;
    }

    private Renderer createComboBoxFieldRenderer(final String fieldType, Map<String, Object> config) {
        Map<String, String> valueToDisplayTextMap = null;
        Map<String, String> allowedValues = UIUtil.getAllowedValuesConfigurationProperty(config);
        if (allowedValues != null) {
            valueToDisplayTextMap = new HashMap<String, String>();
            for (String key : allowedValues.keySet()) {
                valueToDisplayTextMap.put(allowedValues.get(key), key);
            }
        }
        return new InstanceGridColumnRenderer(fieldType, valueToDisplayTextMap, getColumnEmptyText(config));
    }

    protected String getColumnEmptyText(Map<String, Object> config) {
    	return UIUtil.getStringConfigurationProperty(config, FormConstants.EMPTY_TEXT, null);
    }

    protected String preRenderColumnContent(String content, String fieldType, String emptyText) {
    	return getContentOrEmptyText(content, emptyText);
    }

	protected String getContentOrEmptyText(String content, String emptyText) {
		if ( (content == null || content.isEmpty()) &&
    		 (emptyText != null && emptyText.length() > 0) ) {
    		content = "<div style=\"color: gray;\">" + emptyText + "</div>";
    	}
		return content;
	}

    @Override
    public void setValues(Collection<EntityData> values) {
        //This method is not invoked by this widget. It bypasses the parent mechanism for retrieving
        //the widget values and makes an optimized call.
    }

    @Override
    protected void fillValues(List<String> subjects, List<String> props) {
        store.removeAll();
        shadowStore.removeAll();
        OntologyServiceManager.getInstance().getEntityPropertyValues(getProject().getProjectName(), subjects, props, properties,
                new GetTriplesHandler(getSubject()));
    }

    /**
     * Fills the extra column values for a data row, based on an EntityPropertyValues.
     * <B>Important note:</B> Please make sure that both this method and all the
     * methods that override this method will correctly handle the situation when
     * the property-value map in <code>epv</code> is empty, in case when a new instance
     * is created in the grid.
     *
     * @param datarow a grid data row
     * @param epv an EntityPropertyValues instance.
     */
    protected void setExtraColumnValues(Object[] datarow, EntityPropertyValues epv) {
        setExtraColumnValues(datarow, epv.getSubject());
    }

    protected Object[][] createDataArray(List<EntityPropertyValues> entityPropertyValues) {
        return createDataArray(entityPropertyValues, false);
    }

    protected Object[][] createDataArray(List<EntityPropertyValues> entityPropertyValues, boolean asEntityData) {
        int i = 0;
        Object[][] data = new Object[entityPropertyValues.size()][properties.size() + getExtraColumnCount()];
        for (EntityPropertyValues epv : entityPropertyValues) {
            for (PropertyEntityData ped : epv.getProperties()) {
                if (asEntityData == true) {
                    List<EntityData> values = epv.getPropertyValues(ped);
                    //FIXME: just take the first
                    EntityData value = UIUtil.getFirstItem(values);
                    data[i][getIndexOfProperty(ped.getName())] = value;
                } else {
                    data[i][getIndexOfProperty(ped.getName())] = getCellText(epv, ped);
                }
            }

            if (!asEntityData) {
                setExtraColumnValues(data[i], epv);
            }
            i++;
        }
        return data;
    }

    protected void setExtraColumnValues(Object[] datarow, EntityData subject) {
        //add the name of the subject instance
        datarow[properties.size()] = subject.getName();
        //add delete and comment icons
        int offsetDeleteColumn = getOffsetDeleteColumn();
        if (offsetDeleteColumn != -1) {
            datarow[properties.size() + offsetDeleteColumn] = true;
        }

        int offsetCommentColumn = getOffsetCommentColumn();
        if (offsetCommentColumn != -1) {
            datarow[properties.size() + offsetCommentColumn] = new Integer(subject.getLocalAnnotationsCount());
        }
    }

    protected boolean isReplace() {
        if (store == null){
            return false;
        }
        if (store.getRecords() == null){
            return false;
        }
        return store.getRecords().length > 0 && !isMultiValue();
    }

    @Override
    public void setLoadingStatus(boolean loading) {
        super.setLoadingStatus(loading);
        loadingIcon.setHTML(loading ? "<img src=\"images/loading.gif\"/>" : "<img src=\"images/invisible12.png\"/>");
    }


    protected Store getStore() {
        return store;
    }
    
    protected Store getShadowStore() {
    	return shadowStore;
    }

    /*
     * Not quite right: it depends on the configuration that the column is named: "Column2" and the index is 1.
     * Should make it more robust. Column name should not matter.
     */
    protected Map<String, Object> getColumnConfiguration(int colIndex) {
        Map<String, Object> widgetConfiguration = getWidgetConfiguration();
        if (widgetConfiguration == null) {
            return null;
        }
        return (Map<String, Object>) widgetConfiguration.get(FormConstants.COLUMN_PREFIX + (colIndex+1));
    }

    protected Object getColumnConfiguration(int colIndex, String prop) {
        Map<String, Object> colConfig = getColumnConfiguration(colIndex);
        if (colConfig == null) {
            return null;
        }
        return colConfig.get(prop);
    }

    protected int getOffsetDeleteColumn() {
        return OFFSET_DELETE_COLUMN;
    }

    protected int getOffsetCommentColumn() {
        return OFFSET_COMMENT_COLUMN;
    }

    protected int getMaxColumnOffset() {
        return OFFSET_MAX_COLUMN;
    }

    protected int getExtraColumnCount() {
        return OFFSET_MAX_COLUMN + 1;   //1 for the instance field
    }

    protected boolean isMultiValue() {
        return multiValue;
    }

    /*
     * Remote calls
     */

    protected class GetTriplesHandler extends AbstractAsyncHandler<List<EntityPropertyValues>> {

        private EntityData mySubject = null;

        public GetTriplesHandler(EntityData subject) {
            mySubject = subject;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Instance Grid Widget: Error at getting triples for " + getSubject(), caught);
            updateActionLinks(isReplace());
        }

        @Override
        public void handleSuccess(List<EntityPropertyValues> entityPropertyValues) {
            if (!UIUtil.equals(mySubject, getSubject())) {  return; }
            store.removeAll();
            shadowStore.removeAll();

            if (entityPropertyValues != null) {
                Object[][] data = createDataArray(entityPropertyValues);
                store.setDataProxy(new MemoryProxy(data));
                store.load();

                //load also the shadow store
                Object[][] shadowData = createDataArray(entityPropertyValues, true);
                shadowStore.setDataProxy(new MemoryProxy(shadowData));
                shadowStore.load();

                if (fieldNameSorted != null) {
                    store.sort(fieldNameSorted, SortDir.ASC);   //WARNING! This seems to be very slow
                }
            }

            setOldDisplayedSubject(getSubject());

            updateActionLinks(isReplace());
            setLoadingStatus(false);

        }
    }

    protected String getCellText(EntityPropertyValues epv, PropertyEntityData ped) {
        return UIUtil.prettyPrintList(epv.getPropertyValues(ped));
    }

    protected class InstanceGridCellMouseListener extends GridCellListenerAdapter {
        double timeOfLastClick = 0;
        int clicksToEdit = getClicksToEdit();
        boolean oneClickComboboxEditingEnabled = getOneClickComboboxEditingEnabled();

        @Override
        public void onCellClick(final GridPanel grid, final int rowIndex, final int colIndex, final EventObject e) {
            double eventTime = e.getTime();
            if (eventTime - timeOfLastClick > 500) { //not the second click in a double click
                onCellClickOrDblClick(grid, rowIndex, colIndex, e);
            };

            /*
             * Set new value for timeOfLastClick the time the last click was handled
             * We use the current time (and not eventTime), because some time may have passed since eventTime
             * while executing the onCellClickOrDblClick method.
             */
            timeOfLastClick = new Date().getTime();
        }

        protected void onCellClickOrDblClick(GridPanel grid, final int rowIndex, int colIndex, EventObject e) {
            int offsetDeleteColumn = getOffsetDeleteColumn();
            int offsetCommentColumn = getOffsetCommentColumn();
            if (offsetDeleteColumn != -1 && colIndex == properties.size() + offsetDeleteColumn) {
                onDeleteColumnClicked(rowIndex);
            } else if (offsetCommentColumn != -1 && colIndex == properties.size() + offsetCommentColumn) {
                onCommentColumnClicked(rowIndex);
            } else {
                if (clicksToEdit == 1) {
                    onValueColumnClicked(grid, rowIndex, colIndex);
                }
                else {
                    //overriding clicksToEdit behavior: forcing single click to work for specific field types, such as comboboxes
                    checkSpecialColumnsAndStartEditing(grid, rowIndex, colIndex);
                }
            }
        }

        private void checkSpecialColumnsAndStartEditing(GridPanel grid, final int rowIndex, int colIndex) {
            if (oneClickComboboxEditingEnabled) {
                String fieldType = (String) getColumnConfiguration(colIndex, FormConstants.FIELD_TYPE);
                if (FormConstants.FIELD_TYPE_COMBOBOX.equals(fieldType)) {
                    ((EditorGridPanel)grid).startEditing(rowIndex, colIndex);
                }
            }
        }

        @Override
        public void onCellDblClick(GridPanel grid, int rowIndex, int colIndex, EventObject e) {
            if (clicksToEdit == 2) {
                onValueColumnClicked(grid, rowIndex, colIndex);
            }
        }

        @Override
        public void onCellContextMenu(final GridPanel grid, final int rowIndex, final int colIndex, final EventObject e) {
            e.stopEvent();
            if (e.getTarget(".checkbox", 1) != null) {
                onContextMenuCheckboxClicked(rowIndex, colIndex, e);
            } else {
                onContextMenuClicked(rowIndex, colIndex, e);
            }
        }
    }

    
    protected class RemovePropertyValueHandler extends AbstractAsyncHandler<Void> {
        private int removeInd;

        public RemovePropertyValueHandler(int removeIndex) {
            this.removeInd = removeIndex;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at removing value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            MessageBox.alert("There was an error at removing the property value for " + getProperty().getBrowserText()
                    + " and " + getSubject().getBrowserText() + ".");
            updateActionLinks(isReplace());
        }

        @Override
        public void handleSuccess(Void result) {
            GWT.log("Success at removing value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);
            Record recordToRemove = store.getAt(removeInd);
            if (recordToRemove != null) {
                store.remove(recordToRemove);
                updateActionLinks(isReplace());
            }

            //update shadow store
            Record shadowRecordToRemove = shadowStore.getAt(removeInd);
            if (shadowRecordToRemove != null) {
                shadowStore.remove(shadowRecordToRemove);
            }
        }
    }


    protected class ReplacePropertyValueHandler extends AbstractAsyncHandler<Void> {

        private EntityData newEntityData;

        public ReplacePropertyValueHandler(EntityData newEntityData) {
            this.newEntityData = newEntityData;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at replace property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            MessageBox.alert("There was an error at setting the property value for " + getSubject().getBrowserText() + ".");
            InstanceGridWidget.this.refresh();
            updateActionLinks(isReplace());
        }

        @Override
        public void handleSuccess(Void result) {
            InstanceGridWidget.this.grid.getStore().commitChanges();
            updateActionLinks(isReplace());
        }
    }


    class AddPropertyValueHandler extends AbstractAsyncHandler<EntityData> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at add property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            MessageBox.alert("There was an error at adding the property value for " + getSubject().getBrowserText() + ".");
            updateActionLinks(isReplace());
        }

        @Override
        public void handleSuccess(EntityData newInstance) {
            if (newInstance == null) {
                GWT.log("Error at add property for " + getProperty().getBrowserText() + " and "  + getSubject().getBrowserText(), null);
                updateActionLinks(isReplace());
                return;
            }

            updateActionLinks(isReplace());

            onInsertNewValue(newInstance);
        }
    }


    protected class AddExistingValueHandler extends AbstractAsyncHandler<Void> {

        private EntityData newEntityData;

        public AddExistingValueHandler(EntityData newEntityData) {
            this.newEntityData = newEntityData;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at adding property for " + getProperty().getBrowserText() + " and "  + getSubject().getBrowserText(), caught);
            MessageBox.alert("There was an error at adding the property value(s) for " + getSubject().getBrowserText() + ".");
            InstanceGridWidget.this.refresh();
            updateActionLinks(isReplace());
        }

        @Override
        public void handleSuccess(Void result) {
            InstanceGridWidget.this.refresh();
            updateActionLinks(isReplace());
        }
    }


    /*
     * Inner classes
     */
    
    class InstanceGridCloneColumnRenderer extends InstanceGridColumnRenderer {
    	//private String type = "";
    	private int origColumnIndex;
    	
    	public InstanceGridCloneColumnRenderer(final String fieldType, int origColIndex) {
    		super(fieldType);
    		//this.type = fieldType;
    		this.origColumnIndex= origColIndex;
    	}

		@Override
		public String render(Object value, CellMetadata cellMetadata,
				Record record, int rowIndex, int colNum, Store store) {
              Object origValue = record.getAsObject(store.getFields()[origColumnIndex]);
              return super.render(origValue, cellMetadata, record, rowIndex, origColumnIndex, store);
		}
    }
    
    protected class InstanceGridColumnRenderer implements Renderer {
        private String type = "";
        private Map<String, String> valueToDisplayTextMap = null;
        private String emptyTextPrefix;
        private int emptyTextRefColumn = -1;
        private String emptyTextSuffix;

        public InstanceGridColumnRenderer(final String fieldType) {
            this(fieldType, null, null);
        }

        public InstanceGridColumnRenderer(final String fieldType, Map<String, String> valueToDisplayTextMap, String emptyText) {
            this.type = fieldType;
            this.valueToDisplayTextMap = valueToDisplayTextMap;
            initializeEmptyTextComponents(emptyText);
        }
        
        private void initializeEmptyTextComponents(String emptyText) {
        	if (emptyText == null) {
//        		emptyText = "";
        		emptyTextPrefix = "";
        		emptyTextSuffix = "";
        		return;
        	}
        	String[] split = ("?" + emptyText + "?").split("@" + FormConstants.COLUMN_PREFIX + "(\\d+)@");
        	if (split.length > 1) {
        		emptyTextPrefix = split[0].substring(1);
        		emptyTextSuffix = split[1].substring(0, split[1].length()-1);
        		String columnNr = emptyText.substring(emptyTextPrefix.length() + ("@" + FormConstants.COLUMN_PREFIX).length());
        		columnNr = columnNr.substring(0, columnNr.indexOf("@"));
        		emptyTextRefColumn = Integer.parseInt(columnNr) - 1;
        	}
        	else {
        		emptyTextPrefix = emptyText;
        		emptyTextSuffix = "";
        	}
		}

        public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            String field = record.getAsString(store.getFields()[colNum]);

            String warningText = getWarningText(value, record, rowIndex, colNum, store);
        	if (warningText != null) {
            	cellMetadata.setCssClass("grid-cell-warning");
            	cellMetadata.setHtmlAttribute("title='" + warningText + "'");
        	}

            //This would be an elegant way to set the background color and other styling with CSS
            //but it would be quite inefficient to read the configuration options at the 
            //time of rendering each cell.
            //cellMetadata.setCssClass("table-cell-style1");

            if (type != null) {
                if (type.equals(FormConstants.FIELD_TYPE_LINK_ICON)) {
                    return renderLinkIcon(value, cellMetadata, record, rowIndex, colNum, store);
                }
                else if (type.equals(FormConstants.FIELD_TYPE_CHECKBOX)) {
                    return renderCheckBox(value, cellMetadata, record, rowIndex, colNum, store);
                }
                else if (type.equals(FormConstants.FIELD_TYPE_RADIO)) {
                    return renderRadioButton(value, cellMetadata, record, rowIndex, colNum, store);
                }
            }

            if (valueToDisplayTextMap != null && field != null) {
                String newFieldValue = valueToDisplayTextMap.get(field);
                if (newFieldValue != null) {
                    field = newFieldValue;
                }
            }
            if (field == null) {
                field = "";
            }
            String emptyText = getEmptyText(record);
            field = preRenderColumnContent(field, type, emptyText);
            return Format.format(
                    "<style type=\"text/css\">.x-grid3-cell-inner, .x-grid3-hd-inner { white-space:normal !important; }</style> {0}",
                    new String[] { (field) });
        }

        private String getEmptyText(Record record) {
        	String emptyTextColumnContent = "";
        	if (emptyTextRefColumn >= 0) {
        		emptyTextColumnContent = record.getAsString(store.getFields()[emptyTextRefColumn]);
        		if (emptyTextColumnContent == null) {
        			emptyTextColumnContent = "";
        		}
        	}
			return emptyTextPrefix + emptyTextColumnContent + emptyTextSuffix;
		}

        private String renderLinkIcon(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            if (value == null || value.toString().length() == 0) {
                return "";
            } else {
                return "<a href= \"" + value + "\" target=\"_blank\">"
                + "<img src=\"images/world_link.png\"></img>" + "</a>";
            }
        }

        private String renderCheckBox(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            boolean checked = false;
            boolean unknown = (value == null);
            try {
                if (value instanceof Boolean) {
                    checked = ((Boolean)value).booleanValue();
                }
                else {
                    checked = new Boolean((String)value).booleanValue();
                }
            }
            catch (Exception e) {
                unknown = true;
            }

            if (unknown) {
                return "<img class=\"checkbox\" src=\"" + UIConstants.ICON_CHECKBOX_UNKNOWN + "\"/>";//<div style=\"text-align: center;\"> IMG_TAG </div>;
            }

            return "<img class=\"checkbox\" " +
            		"src=\"" +
                    (checked ? UIConstants.ICON_CHECKBOX_CHECKED : UIConstants.ICON_CHECKBOX_UNCHECKED) + 
                    "\"/>";//<div style=\"text-align: center;\"> IMG_TAG </div>;
        }

        private String renderRadioButton(final Object value, final CellMetadata cellMetadata,
                final Record record, final int rowIndex, final int colNum, final Store store) {
            boolean checked = false;
            boolean unknown = (value == null);
            try {
                if (value instanceof Boolean) {
                    checked = ((Boolean)value).booleanValue();
                }
                else {
                    checked = new Boolean((String)value).booleanValue();
                }
            }
            catch (Exception e) {
                unknown = true;
            }

            if (unknown) {
                checked = false;
            }

            final boolean oldValue = checked;

            final Radio btn = new Radio("", new CheckboxListenerAdapter(){
                @Override
                public void onCheck(Checkbox field, boolean checked) {
                    if (checked != oldValue) {
                        if (isWriteOperationAllowed()) {
                            onRadioButtonChecked(checked, value, cellMetadata, record, rowIndex, colNum, store);
                        }
                        else {
                            record.set(record.getFields()[colNum], oldValue);
                            record.commit();
                        }
                    }
                }
            });
            final String id = Ext.generateId();
            btn.setRenderToID(id);
            btn.setChecked(checked);
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    if (DOM.getElementById(id) != null) {
                        btn.render(id);
                    }
                }
            });
            return Format.format(
                    "<div id='{0}' style='padding:4px 0px 0px 0px;width:100%;height:20px;'></div>", id);

        }
    }

    protected void onRadioButtonChecked(boolean checked, Object value, CellMetadata cellMetadata,
            Record record, int rowIndex, int colNum, Store store) {
        //override this in subclasses
    }

    protected String getWarningText(Object value, Record record,
			int rowIndex, int colNum, Store store) {
    	//by default we should not return a warning text
   		return null;
	}


    final class DeleteContextMenu extends Menu{
        public DeleteContextMenu(String menuText, String menuIcon, final Record record, final int rowIndex, final int colIndex) {
            MenuItem item = new MenuItem();
            item.setText(menuText);
            item.setIcon(menuIcon);
            item.addListener(new BaseItemListenerAdapter() {
                @Override
                public void onClick(BaseItem item, EventObject e) {
                    super.onClick(item, e);
                    onDeleteColumnValue(record, rowIndex, colIndex);
                }
            });
            addItem(item);
        }
    }
}
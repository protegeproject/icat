package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.SortDir;
import com.gwtext.client.core.TextAlign;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.BooleanFieldDef;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.IntegerFieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.TextArea;
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

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.PropertyValueUtil;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidgetWithNotes;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class InstanceGridWidget extends AbstractPropertyWidgetWithNotes {

    protected static String INSTANCE_FIELD_NAME = "@instance@";
    protected static String DELETE_FIELD_NAME = "@delete@";
    protected static String COMMENT_FIELD_NAME = "@comment@";

    private static int OFFSET_DELETE_COLUMN = 1;   //use -1 if not present
    private static int OFFSET_COMMENT_COLUMN = 2;
    private static int OFFSET_MAX_COLUMN = OFFSET_COMMENT_COLUMN; //use 0 if all other column offsets are -1

    private Panel wrappingPanel;
    private EditorGridPanel grid;

    private HTML loadingIcon;

    protected RecordDef recordDef;
    protected Store store;

    protected GridRowListener gridRowListener;
    protected EditorGridListener editorGridListener;

    protected List<String> properties = new ArrayList<String>(); //order of cols
    protected Map<String, Integer> prop2Index = new HashMap<String, Integer>();
    protected String autoExpandColId;

    private String fieldNameSorted = null;

    protected boolean multiValue = true;
    private Anchor addExistingLink;
    private Anchor addNewLink;
    private Anchor replaceExistingLink;
    private Anchor replaceNewLink;
    private VerticalPanel labelPanel;

    protected PropertyValueUtil propertyValueUtil;

    public InstanceGridWidget(Project project) {
        super(project);
        propertyValueUtil = new PropertyValueUtil();
    }


    @Override
    public Component getComponent() {
        return wrappingPanel;
    }

    public GridPanel getGridPanel() {
        return grid;
    }


    @Override
    public Component createComponent() {
        wrappingPanel = new Panel();
        wrappingPanel.setLayout(new ColumnLayout());
        wrappingPanel.setPaddings(5);

        HorizontalPanel horizLabelPanel = new HorizontalPanel();
        String labelText = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), FormConstants.LABEL, getProperty().getBrowserText());
        Label label = new Label();
        label.setHtml(getLabelHtml(labelText, getHelpURL(), getTooltipText()) + AbstractFieldWidget.LABEL_SEPARATOR);
        horizLabelPanel.add(label);
        loadingIcon = new HTML("<img src=\"images/invisible12.png\"/>");
        loadingIcon.setStyleName("loading-img");
        horizLabelPanel.add(loadingIcon);
        horizLabelPanel.setStyleName("form_label");

        labelPanel = new VerticalPanel();
        //labelPanel.add(label);
        labelPanel.add(horizLabelPanel);

        addExistingLink = createAddExistingHyperlink();
        if (addExistingLink != null) {
            labelPanel.add(addExistingLink);
        }

        addNewLink = createAddNewValueHyperlink();
        if (addNewLink != null) {
            labelPanel.add(addNewLink);
        }

        replaceExistingLink = createReplaceExistingHyperlink();
        if (replaceExistingLink != null && isReplace()) {
            labelPanel.add(replaceExistingLink);
        }

        replaceNewLink = createReplaceNewValueHyperlink();
        if (replaceNewLink != null && isReplace()) {
            labelPanel.add(replaceNewLink);
        }

        grid = createGrid();
        grid.addEditorGridListener(getEditorGridListener());

        if (grid.getHeight() < 25 + 25 + 20) {  //default height of grid header (25) + default height of a row (25) + default height of horiz. scrollbar (20)
            grid.setAutoScroll(false);
        }

        wrappingPanel.add(labelPanel);
        wrappingPanel.add(grid, new ColumnLayoutData(1));

        return wrappingPanel;

    }

    @Override
    public void setLoadingStatus(boolean loading) {
        super.setLoadingStatus(loading);
        if (loading) {
            loadingIcon.setHTML("<img src=\"images/loading.gif\"/>");
        }
        else {
            loadingIcon.setHTML("<img src=\"images/invisible12.png\"/>");
        }
    }

    /**
     * Subclasses can override this to show the Add link.
     * @return
     */
    protected Anchor createAddExistingHyperlink() {
        return null;
    }

    /**
     * Subclasses can override this to show the Add link.
     * @return
     */
    protected Anchor createReplaceExistingHyperlink() {
        return null;
    }

    protected Anchor createAddNewValueHyperlink() {
       Anchor addNewLink = new Anchor("<br><img src=\"images/add.png\"></img>&nbsp Add new value", true);
        addNewLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (UIUtil.confirmOperationAllowed(getProject())) {
                    onAddNewValue();
                }
            }
        });
        return addNewLink;
    }

    protected Anchor createReplaceNewValueHyperlink() {
       return null;
    }

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

    //TODO - check if this is ICD specific
    protected String getAddValueOperationDescription() {
        return UIUtil.getAppliedToTransactionString("Added a new "
                + UIUtil.getShortName(getProperty().getBrowserText()) + " to " + getSubject().getBrowserText(),
                getSubject().getName());
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

            multiValue = UIUtil.getBooleanConfigurationProperty(widgetConfig, FormConstants.MULTIPLE_VALUES_ALLOWED, true);
        }

        createColumns();
        createStore();
        attachListeners();

        /*
        //remove reserved space for vertical scroll bar (could be problematic when the height of single row does not fit in the grid)
        if (! multiValue) {
	       	grid.getView().setScrollOffset(2);
        }
        */

        if (autoExpandColId != null) {
            grid.setAutoExpandColumn(autoExpandColId);
        }

        grid.getView().setScrollOffset(25);

        return grid;
    }

    protected void attachListeners() {
        //TODO: may not work so well.. - check indexes
        grid.addGridCellListener(new GridCellListenerAdapter() {
        	double timeOfLastClick = 0;

        	@Override
        	public void onCellClick(final GridPanel grid, final int rowIndex, final int colindex, final EventObject e) {
        		double eventTime = e.getTime();
        		if (eventTime - timeOfLastClick > 500) { //not the second click in a double click
					onCellClickOrDblClick(grid, rowIndex, colindex, e);
        		};

        		/*
        		 * Set new value for timeOfLastClick the time the last click was handled
        		 * We use the current time (and not eventTime), because some time may have passed since eventTime
        		 * while executing the onCellClickOrDblClick method.
        		 */

        		timeOfLastClick = new Date().getTime();
        	}

            private void onCellClickOrDblClick(GridPanel grid, final int rowIndex, int colindex, EventObject e) {
                int offsetDeleteColumn = getOffsetDeleteColumn();
                int offsetCommentColumn = getOffsetCommentColumn();
                if (offsetDeleteColumn != -1 && colindex == properties.size() + offsetDeleteColumn) {//FIXME!!!!
                    //should be delete - do nothing
                    Record record = store.getAt(rowIndex);
                    if (record != null) {
                        if (UIUtil.confirmOperationAllowed(getProject())) {
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
                            System.out.println("TODO continue here");
                        }
                    }
                }
            }
        });

    }

    protected EditorGridListener getEditorGridListener() {
        if (editorGridListener == null) {
            editorGridListener = new EditorGridListenerAdapter() {
                @Override
                public boolean doBeforeEdit(GridPanel grid, Record record, String field, Object value, int rowIndex,
                        int colIndex) {
                    if (!UIUtil.confirmOperationAllowed(getProject())) {
                        return false;
                    }
                    String valueType = record.getAsString("valueType");
                    return valueType == null || valueType.equalsIgnoreCase("string")
                            || valueType.equalsIgnoreCase("any"); //TODO: allow only the editing of String values for now
                }

                @Override
                public void onAfterEdit(GridPanel grid, Record record, String field, Object newValue, Object oldValue,
                        int rowIndex, int colIndex) {
                    //special handling rdfs:Literal
                    String valueType = record.getAsString("valueType");
                    if (valueType == null) { //TODO: should be fixed
                        valueType = ValueType.String.name();
                    }
                    String selSubject = record.getAsString(INSTANCE_FIELD_NAME);
                    if (selSubject != null) {
                        propertyValueUtil.replacePropertyValue(getProject().getProjectName(), selSubject,
                                properties.get(colIndex), ValueType.valueOf(valueType), oldValue.toString(), newValue.toString(),
                                GlobalSettings.getGlobalSettings().getUserName(),
                                getReplaceValueOperationDescription(colIndex, oldValue, newValue),
                                new ReplacePropertyValueHandler(new EntityData(newValue.toString(), newValue.toString())));
                    }
                }
            };
        }
        return editorGridListener;
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
                getSubject() .getName());
    }

    protected void onDelete(int index) {
        Record record = store.getAt(index);
        String value = record.getAsString(INSTANCE_FIELD_NAME);
        if (value != null) {
            GWT.log("Deleting " + value, null);

            propertyValueUtil.deletePropertyValue(getProject().getProjectName(), getSubject().getName(),
                    getProperty().getName(), ValueType.Instance, value, GlobalSettings.getGlobalSettings()
                            .getUserName(), getDeleteValueOperationDescription(index), new RemovePropertyValueHandler(
                            index));

        }
    }

    //TODO - check if this is ICD specific
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

    protected void onEditNotes(int index) {
        Record record = store.getAt(index);
        String value = record.getAsString(INSTANCE_FIELD_NAME);
        super.onEditNotes(value);
    }

    protected void createStore() {
        ArrayReader reader = new ArrayReader(recordDef);
        MemoryProxy dataProxy = new MemoryProxy(new Object[][] {});
        store = new Store(dataProxy, reader);
        grid.setStore(store);
        store.load();
        //Note: this would be a more efficient solution if it would work, but since it does not,
        //      we moved it in the GetTriplesHandler.setValue() method
        //if (fieldNameSorted != null) {
        //    store.sort(fieldNameSorted, SortDir.ASC);
        //}
    }

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
                createColumn((Map<String, Object>) widgetConfig.get(key), fieldDef, columns, props);
            }
        }

        properties = Arrays.asList(props);
        for (int i = 0; i < props.length; i++) {
            prop2Index.put(props[i], i);
        }

        createInstanceColumn(fieldDef, columns, colCount);
        createActionColumns(fieldDef, columns, colCount);

        recordDef = new RecordDef(fieldDef);

        ColumnModel columnModel = new ColumnModel(columns);
        grid.setColumnModel(columnModel);

        //would be nice but it would prevent automatic component resize :(
       	//grid.setHideColumnHeader(! multiValue);
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

        deleteCol.setRenderer(new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                    Store store) {
                return "<img src=\"images/delete.png\" title=\""
                        + " Click on the icon to remove value.\"></img>";
            }
        });
        return deleteCol;
    }

    protected ColumnConfig createCommentsColumn() {
        ColumnConfig commentCol = new ColumnConfig("", COMMENT_FIELD_NAME, 40);
        commentCol.setTooltip("Add a comment on this value");
        commentCol.setRenderer(new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                    Store store) {
                // TODO: add a css for this
                String text = "<img src=\"images/comment.gif\" title=\""
                    	+ " Click on the icon to add new note(s).\"></img>";
                int annotationsCount = value == null ? 0 : ((Integer) value);
                if (annotationsCount > 0) {
                    text = "<img src=\"images/comment.gif\" title=\""
                            + UIUtil.getNiceNoteCountText(annotationsCount)
                            + " on this value. \nClick on the icon to see existing or to add new note(s).\"></img>"
                            + "<span style=\"vertical-align:super;font-size:95%;color:#15428B;font-weight:bold;\">"
                            + "&nbsp;" + annotationsCount + "</span>";
                }
                return text;
            }
        });
        return commentCol;
    }

    //FIXME: protect against invalid config xml
    protected ColumnConfig createColumn(Map<String, Object> config, FieldDef[] fieldDef, ColumnConfig[] columnConfigs,
            String[] props) {
        ColumnConfig colConfig = new ColumnConfig();

        String header = (String) config.get(FormConstants.HEADER);
        colConfig.setHeader(header == null ? "" : header);

        String tooltip = (String) config.get(FormConstants.TOOLTIP);
        if (tooltip != null) {
            colConfig.setTooltip(tooltip);
        }

        String property = (String) config.get(FormConstants.PROPERTY); //better not be null
        String indexStr = (String) config.get(FormConstants.INDEX);

        int index = Integer.parseInt(indexStr); //better be valid
        props[index] = property;
         colConfig.setDataIndex(property);

        //String colName = FormConstants.COLUMN_PREFIX + index;
        //colConfig.setDataIndex(colName);

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

        colConfig.setResizable(true);
        colConfig.setSortable(true);
        colConfig.setCss("word-wrap: break-word ;");

        String fieldType = (String) config.get(FormConstants.FIELD_TYPE);
        colConfig.setRenderer(getColumnRenderer(fieldType, config));

        String fieldTextAlign = (String) config.get(FormConstants.FIELD_ALIGN);
        if (fieldTextAlign != null) {
            colConfig.setAlign(getTextAlign(fieldTextAlign));
        }

     //TODO Nyu: move this just before the next TODO
        GridEditor editor = getGridEditor(fieldType, config);
        if (editor != null) {
            colConfig.setEditor(editor);
        }

        //TODO: support other types as well
        fieldDef[index] = new StringFieldDef(property);
        columnConfigs[index] = colConfig;


        Boolean sorted = (Boolean) config.get(FormConstants.SORTED);
        if (Boolean.TRUE.equals(sorted)) {
            fieldNameSorted = colConfig.getDataIndex();
        }

        return colConfig;
    }

    protected int getIndexOfProperty(String prop) {
        return prop2Index.get(prop);
    }

    private TextAlign getTextAlign(String fieldTextAlign) {
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


    protected GridEditor getGridEditor(final String fieldType, final Map<String, Object> config) {

        if (fieldType != null) {
            if (fieldType.equals(FormConstants.FIELD_TYPE_DROPDOWN)) {
                Map<String, String> allowedValues = (Map<String, String>)config.get(FormConstants.ALLOWED_VALUES);
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
                cb.setEditable(false);
                return new GridEditor(cb);
            }
        }

        //TODO - use a text area as the default editor for now, support more later
        TextField textEditor;
        if (grid.getHeight() < 25 + 50) {	//default height of grid header (25) + default height of a TextArea (50)
        	textEditor = new TextField();
        }
        else {
        	textEditor = new TextArea();
        }

		return new GridEditor(textEditor);
    }

    protected Renderer getColumnRenderer(final String fieldType, Map<String, Object> config) {
        Map<String, String> valueToDisplayTextMap = null;
        if (FormConstants.FIELD_TYPE_DROPDOWN.equals(fieldType)) {
            Map<String, String> allowedValues = (Map<String, String>)config.get(FormConstants.ALLOWED_VALUES);
            if (allowedValues != null) {
                valueToDisplayTextMap = new HashMap<String, String>();
                for (String key : allowedValues.keySet()) {
                    valueToDisplayTextMap.put(allowedValues.get(key), key);
                }
            }
        }
        ColumnRenderer renderer = new ColumnRenderer(fieldType, valueToDisplayTextMap);
        return renderer;
    }

    protected String preRenderColumnContent(String content, String fieldType) {
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
        OntologyServiceManager.getInstance().getEntityPropertyValues(getProject().getProjectName(), subjects, props, properties,
                new GetTriplesHandler(getSubject()));
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


    protected void setExtraColumnValues(Object[][] data, int i, EntityPropertyValues epv) {
        EntityData subject = epv.getSubject();

        //add the name of the subject instance
        data[i][properties.size()] = subject.getName();
        //add delete and comment icons
        int offsetDeleteColumn = getOffsetDeleteColumn();
        if (offsetDeleteColumn != -1) {
            data[i][properties.size() + offsetDeleteColumn] = true;
        }

        int offsetCommentColumn = getOffsetCommentColumn();
        if (offsetCommentColumn != -1) {
            data[i][properties.size() + offsetCommentColumn] = new Integer(subject.getLocalAnnotationsCount());
        }
    }

    protected boolean isReplace() {
        if (store == null){
            return false;
        }
        if (store.getRecords() == null){
            return false;
        }
        return store.getRecords().length > 0 && !multiValue;
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
        }

          @Override
        public void handleSuccess(List<EntityPropertyValues> entityPropertyValues) {
            /*
             * This check is necessary because of the async nature of the call.
             * We should never add values to a widget, if the subject has already changed.
             */
            if (!UIUtil.equals(mySubject, getSubject())) {  return; }

            store.removeAll(); //sometimes we get multiple selection events

            if (entityPropertyValues != null) {
                int i = 0;
                Object[][] data = new Object[entityPropertyValues.size()][properties.size() + getExtraColumnCount()];
                for (EntityPropertyValues epv : entityPropertyValues) {
                    for (PropertyEntityData ped : epv.getProperties()) {
                        data[i][getIndexOfProperty(ped.getName())] = getCellText(epv, ped);
                    }
                    setExtraColumnValues(data, i, epv);
                    i++;
                }
                store.setDataProxy(new MemoryProxy(data));
                store.load();

                if (fieldNameSorted != null) {
                    store.sort(fieldNameSorted, SortDir.ASC);   //WARNING! This seems to be very slow
                }
            }
              setOldDisplayedSubject(getSubject());
              if (!multiValue) {
                  if (isReplace() && entityPropertyValues != null && entityPropertyValues.size()>0) {
                      setReplaceLinks();
                  } else {
                      setAddLinks();
                  }
              }
              setLoadingStatus(false);

        }
    }

    private void setReplaceLinks() {
        if (addExistingLink != null) {
            labelPanel.remove(addExistingLink);
        }
        if (addNewLink != null) {
            labelPanel.remove(addNewLink);
        }
        if (replaceExistingLink != null) {
            labelPanel.add(replaceExistingLink);
        }
        if (replaceNewLink != null) {
            labelPanel.add(replaceNewLink);
        }
    }

    private void setAddLinks() {
        if (replaceExistingLink != null) {
            labelPanel.remove(replaceExistingLink);
        }
        if (replaceNewLink != null) {
            labelPanel.remove(replaceNewLink);
        }
        if (addExistingLink != null) {
            labelPanel.add(addExistingLink);
        }
        if (addNewLink != null) {
            labelPanel.add(addNewLink);
        }
    }

    protected String getCellText(EntityPropertyValues epv, PropertyEntityData ped) {
        return UIUtil.commaSeparatedList(epv.getPropertyValues(ped));
    }

    class RemovePropertyValueHandler extends AbstractAsyncHandler<Void> {
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
            //setValues(values);
        }

        @Override
        public void handleSuccess(Void result) {
            GWT.log("Success at removing value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);
            Record recordToRemove = store.getAt(removeInd);
            if (recordToRemove != null) {
                store.remove(recordToRemove);
                if (!multiValue) {
                    if(store.getCount() == 0){
                        setAddLinks();
                    }
                    if(store.getCount() > 0){
                        setReplaceLinks();
                    }
                }
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
            MessageBox.alert("There was an error at setting the property value for " + getSubject().getBrowserText()
                    + ".");
            //setValues(values);
            InstanceGridWidget.this.refresh();
        }

        @Override
        public void handleSuccess(Void result) {
            GWT.log("Success at setting value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);
            //to avoid whole unnecessary reloading do not call:
            //InstanceGridWidget.this.refresh();
            //just commit the changes in the store to get rid of the "dirty" flag (small red triangle)
            InstanceGridWidget.this.grid.getStore().commitChanges();
        }
    }

    class AddPropertyValueHandler extends AbstractAsyncHandler<EntityData> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at add property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            MessageBox.alert("There was an error at adding the property value for " + getSubject().getBrowserText()
                    + ".");
        }

        @Override
        public void handleSuccess(EntityData newInstance) {
            if (newInstance == null) {
                GWT.log("Error at add property for " + getProperty().getBrowserText() + " and "
                        + getSubject().getBrowserText(), null);
                return;
            }

            GWT.log("Success at adding value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);

            if (!multiValue) {
                setReplaceLinks();
            }

            Object[] empty = new Object[properties.size() + getExtraColumnCount()];
            empty[properties.size()] = newInstance.getName();
            int offsetCommentColumn = getOffsetCommentColumn();
            if (offsetCommentColumn != -1) {
                empty[properties.size() + offsetCommentColumn] = new Integer(0);
            }

            Record plant = recordDef.createRecord(empty);
            grid.stopEditing();
            store.insert(0, plant);
            grid.startEditing(0, 0);

        }
    }

    class ColumnRenderer implements Renderer {
    	private String type = "";
    	private Map<String, String> valueToDisplayTextMap = null;

    	public ColumnRenderer(final String fieldType) {
    	    this(fieldType, null);
    	}

    	public ColumnRenderer(final String fieldType, Map<String, String> valueToDisplayTextMap) {
    		this.type = fieldType;
    		this.valueToDisplayTextMap = valueToDisplayTextMap;
		}

        public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            String field = record.getAsString(store.getFields()[colNum]);

            if (type != null) {
                if (type.equals(FormConstants.FIELD_TYPE_LINK_ICON)) {
                    return renderLinkIcon(value, cellMetadata, record, rowIndex, colNum, store);
                }
                else if (type.equals(FormConstants.FIELD_TYPE_CHECKBOX)) {
                    return renderCheckBox(value, cellMetadata, record, rowIndex, colNum, store);
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
            field = preRenderColumnContent(field, type);
            return Format
                    .format(
                            "<style type=\"text/css\">.x-grid3-cell-inner, .x-grid3-hd-inner { white-space:normal !important; }</style> {0}",
                            new String[] { (field) });
        }

        private String renderLinkIcon(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            //return "<a href= \"" + value + "\" target=_blank>" + "<img src=\"images/Globe-Connected-16x16.png\"></img>" + "</a>";
            if (value == null || value.toString().length() == 0) {
                return "";
            } else {
                return "<a href= \"" + value + "\" target=_blank>"
                        + "<img src=\"images/world_link.png\"></img>" + "</a>";
            }
        }

        private String renderCheckBox(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            boolean checked = false;
            boolean unknown = (value == null);
            try {
                checked = new Boolean((String)value).booleanValue();
            }
            catch (Exception e) {
                unknown = true;
            }

            if (unknown) {
                return "<img class=\"checkbox\" src=\"images/unknown_check.gif\"/>";//<div style=\"text-align: center;\">  </div>";
            }
            //boolean checked = ((Boolean) value).booleanValue();
            return "<img class=\"checkbox\" " +
            		"src=\"js/ext/resources/images/default/menu/" +
                                (checked ? "checked.gif" : "unchecked.gif") + "\"/>";
        }


    }
}
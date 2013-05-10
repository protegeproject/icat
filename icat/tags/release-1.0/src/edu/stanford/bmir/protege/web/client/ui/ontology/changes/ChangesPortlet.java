package edu.stanford.bmir.protege.web.client.ui.ontology.changes;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.SortDir;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.DateFieldDef;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.PagingToolbar;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.event.GridRowListener;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.layout.FormLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.data.ChangeData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.util.PaginationUtil;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;


public class ChangesPortlet extends AbstractEntityPortlet {

	private static final String COLUMN_NAME_AUTHOR = "Author";
	private static final String COLUMN_NAME_DESCRIPTION = "Description";
	private static final String COLUMN_NAME_TIMESTAMP = "Timestamp";
	private static final String COLUMN_NAME_APPLIES_TO = "Applies to";

	private static final String FIELD_LABEL_AUTHOR = COLUMN_NAME_AUTHOR;
	private static final String FIELD_LABEL_DESCRIPTION = COLUMN_NAME_DESCRIPTION;
	private static final String FIELD_LABEL_TIMESTAMP = "Time of change";
	private static final String FIELD_LABEL_APPLIES_TO = "Applies to (entity)";
	private static final String FIELD_LABEL_APPLIES_TO_DISPLAY_TEXT = "Applies to (label)";

	protected GridPanel changesGrid;
	protected RecordDef recordDef;
	protected Store store;
	protected ChangesProxyImpl proxy;

	public ChangesPortlet(Project project) {
		super(project);
	}

	@Override
	public void initialize() {
		createGrid();
		setHeight(200);
		add(changesGrid);
	}

	@Override
	public void reload() {
		store.removeAll();

		String entityName = "";
		String projectName = "";

		EntityData entity = getEntity();

		if (entity != null) {
			entityName = entity.getName();
			setTitle("Change history for " + UIUtil.getDisplayText(getEntity()));
		} else {
			setTitle("Change history (nothing selected)");
		}

		if (project != null) {
			projectName = project.getProjectName();
		} else {
		    return;
		}

		proxy.resetParams();
		proxy.setProjectName(projectName);
		proxy.setEntityName(entityName);

		PagingToolbar pToolbar = (PagingToolbar) changesGrid.getBottomToolbar();
		store.load(0, pToolbar.getPageSize());

	}

	public Collection<EntityData> getSelection() {
		return new ArrayList<EntityData>();
	}

	private void createGrid() {
		changesGrid = new GridPanel();

		changesGrid.setAutoWidth(true);
		changesGrid.setAutoExpandColumn("ChangesGrid_ChangeDescCol");
		changesGrid.setStripeRows(true);
		changesGrid.setFrame(true);
		changesGrid.addGridRowListener(createGridRowListener());
		createColumns();

		recordDef = new RecordDef(new FieldDef[] { new StringFieldDef("desc"),
				new StringFieldDef("author"), new DateFieldDef("timestamp"),
				new StringFieldDef("applies") });


		ArrayReader reader = new ArrayReader(recordDef);
		proxy = new ChangesProxyImpl();
		store = new Store(proxy, reader);

		PagingToolbar pToolbar = PaginationUtil.getNewPagingToolbar(store, 20);

		changesGrid.setBottomToolbar(pToolbar);

		changesGrid.setStore(store);

		if (changesGrid.getStore() == null) {
			changesGrid.setStore(store);
		}

		setTitle("Changes");

		//TODO: Uncomment this code after the patch to set entityName for a newly created portlet is set
		//reload();
	}

	private GridRowListener createGridRowListener() {
		GridRowListener rowListener = new GridRowListenerAdapter() {
			@Override
            public void onRowDblClick(GridPanel grid, int rowIndex, EventObject e) {
				Record record = store.getRecordAt(rowIndex);
				showChangeDetails(record);
			}
		};

		return rowListener;
	}

	private void showChangeDetails(Record record) {
        final Window window = new Window();
        window.setTitle("Change details");
        window.setWidth(500);
        window.setHeight(365);
        window.setLayout(new FitLayout());

        Panel propertyValuesPanel = new Panel();
        propertyValuesPanel.setLayout(new FormLayout());
        propertyValuesPanel.setPaddings(5);
        propertyValuesPanel.setBorder(false);
        propertyValuesPanel.setAutoScroll(true);

        String author = record.getAsString("author");
        addFieldToPanel(propertyValuesPanel, FIELD_LABEL_AUTHOR, author, TextField.class);
        String description = record.getAsString("desc");
        addFieldToPanel(propertyValuesPanel, FIELD_LABEL_DESCRIPTION, description, TextArea.class);
        String timestamp = record.getAsString("timestamp");
        addFieldToPanel(propertyValuesPanel, FIELD_LABEL_TIMESTAMP, timestamp, TextField.class);
        String applies = record.getAsString("applies");
        addFieldToPanel(propertyValuesPanel, FIELD_LABEL_APPLIES_TO, applies, TextField.class);
        String appliesDisplayText = (getEntity().getName().equals(applies) ? getEntity().getBrowserText() : "");
        addFieldToPanel(propertyValuesPanel, FIELD_LABEL_APPLIES_TO_DISPLAY_TEXT, appliesDisplayText, TextField.class);

		window.add(propertyValuesPanel);

        window.show();
	}

	private <T extends Component> void addFieldToPanel(Panel infoPanel, String label, String value, Class<T> type) {
		Component fieldComponent = null;
		if (type.equals(TextField.class)) {
			TextField textField = new TextField(label);
			textField.setValue(value);
			textField.setReadOnly(true);
			fieldComponent = textField;
		}
		else if (type.equals(TextArea.class)) {
			TextArea textArea = new TextArea(label);
			textArea.setValue(value);
			textArea.setReadOnly(true);
			textArea.setHeight("125px");
			fieldComponent = textArea;
		}
		else if (type.equals(Checkbox.class)) {
			Checkbox checkbox = new Checkbox(label);
			checkbox.setValue("true".equals(value.toLowerCase()));
			checkbox.setReadOnly(true);
			fieldComponent = checkbox;
		}

		if (fieldComponent != null) {
			fieldComponent.setWidth("80%");
			infoPanel.add(fieldComponent);
		}
		else {
			GWT.log("Could not understand type " + type + ". Information about '" + label + ": " + value + "' will not be displayed");
		}
	}

	private void createColumns() {
		ColumnConfig changeDescCol = new ColumnConfig(COLUMN_NAME_DESCRIPTION, "desc");
		changeDescCol.setId("ChangesGrid_ChangeDescCol");
		changeDescCol.setResizable(true);
		changeDescCol.setSortable(true);

		ColumnConfig authorCol = new ColumnConfig(COLUMN_NAME_AUTHOR, "author");
		authorCol.setResizable(true);
		authorCol.setSortable(true);

		ColumnConfig timestampCol = new ColumnConfig(COLUMN_NAME_TIMESTAMP, "timestamp");
		timestampCol.setResizable(true);
		timestampCol.setSortable(true);

		ColumnConfig appliesToCol = new ColumnConfig(COLUMN_NAME_APPLIES_TO, "applies");
		appliesToCol.setResizable(true);
		appliesToCol.setSortable(true);
		appliesToCol.setHidden(true);

		ColumnConfig[] columns = new ColumnConfig[] { changeDescCol, authorCol,
				timestampCol, appliesToCol };
		ColumnModel columnModel = new ColumnModel(columns);
		changesGrid.setColumnModel(columnModel);
	}

	class GetChangesHandler extends
			AbstractAsyncHandler<Collection<ChangeData>> {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("RPC error getting changes for the "
					+ project.getProjectName() + "ontology", caught);
		}

		@Override
		public void handleSuccess(Collection<ChangeData> result) {
			for (ChangeData data : result) {
				Record record = recordDef.createRecord(new Object[] {
						data.getDescription(), data.getAuthor(),
						data.getTimestamp(), getEntity() });
				store.add(record);
			}
			store.sort("timestamp", SortDir.DESC);
			changesGrid.getView().refresh();
		}
	}
}

package edu.stanford.bmir.protege.web.client.ui.ontology.SWRL;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.BooleanFieldDef;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.SWRLServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SWRLData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * A portlet which displays SWRL rules that are defined in an ontology.
 * 
 * @author Mike Uehara-Bingen <mike.bingen@stanford.edu>
 */
public class SWRLTabPortlet
        extends AbstractEntityPortlet {

	private List<SWRLData> data = null;

	public SWRLTabPortlet(Project project) {
		super(project);
	}

	@Override
	public void reload() {
		// TODO Mike
	}

	public Collection<EntityData> getSelection() {
		throw new RuntimeException(
		        "Selections are currently unsupported in the SWRLTabPortlet");
	}

	@Override
	public void intialize() {
		setTitle("SWRL Rules");
		SWRLServiceManager.getInstance().getData(this.project.getProjectName(),
		        new GetSWRLData());
	}

	private class GetSWRLData
	        extends AbstractAsyncHandler<List<SWRLData>> {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting SWRL Data from server", caught);
		}

		@Override
		public void handleSuccess(List<SWRLData> data) {
			SWRLTabPortlet.this.data = data;
			createUI();
		}
	}

	private void createUI() {
		Store store = loadData();
		ColumnModel columnModel = createColumnModel();
		this.add(createGridPanel(store, columnModel));
	}

	private GridPanel createGridPanel(Store store, ColumnModel columnModel) {
		GridPanel gridPanel = new GridPanel(store, columnModel);
		gridPanel.setStripeRows(true);
		gridPanel.setAutoExpandColumn("exp");
		gridPanel.setAutoHeight(true);
		return gridPanel;
	}

	private Store loadData() {
		Object[][] all = to2dArray(this.data);
		ArrayReader reader =
		        new ArrayReader(new RecordDef(new FieldDef[] {
		                new BooleanFieldDef("Enabled", 0),
		                new StringFieldDef("Name", 1),
		                new StringFieldDef("Expression", 2), }));
		MemoryProxy dataProxy = new MemoryProxy(all);
		Store store = new Store(dataProxy, reader);
		store.load();
		return store;
	}

	private ColumnModel createColumnModel() {

		ColumnConfig[] columnConfig =
		        new ColumnConfig[] {
		                (new ColumnConfig("Enabled", "Enabled", 100, true)),
		                (new ColumnConfig("Name", "Name", 500, true)),
		                createExpressionColumnConfig() };

		return new ColumnModel(columnConfig);

	}

	private ColumnConfig createExpressionColumnConfig() {
		ColumnConfig expressionColumnConfig =
		        new ColumnConfig("Expression", "Expression", 1000, true);
		expressionColumnConfig.setId("exp");
		return expressionColumnConfig;
	}

	private static Object[][] to2dArray(List<SWRLData> data) {
		Object[][] all = new Object[data.size()][];
		for (int i = 0; i < data.size(); i++) {
			all[i] = toArray(data.get(i));
		}
		return all;
	}

	private static Object[] toArray(SWRLData data) {
		return new Object[] { data.isEnabled(), data.getName(), data.getRule() };
	}

}

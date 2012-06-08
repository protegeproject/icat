package edu.stanford.bmir.protege.web.client.ui.ontology.metadata;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class MetricsGrid extends GridPanel {

	protected ColumnModel columnModel;
	protected RecordDef recordDef;
	protected Store store;
	protected Project project;
	protected EntityData _currentEntity;

	public MetricsGrid(Project project) {
		this.project = project;
		this._currentEntity = new EntityData(project.getProjectName());
		createGrid();
	}
	
	public void setEntity(EntityData newEntity) {		
		if (_currentEntity != null && _currentEntity.equals(newEntity)) {
			return;
		}
		
		store.removeAll();
		_currentEntity = newEntity;
		
		if (_currentEntity == null) {			
			return;
		}
		
		reload();
	}
	
	protected void createGrid() {
		ColumnConfig metricCol = new ColumnConfig();
		metricCol.setHeader("Metric");
		metricCol.setDataIndex("metric");
		metricCol.setId("MetricsGrid_MetricColumn");
		metricCol.setResizable(true);
		metricCol.setSortable(true);
		
		ColumnConfig valueCol = new ColumnConfig();
		valueCol.setHeader("Value");
		valueCol.setDataIndex("val");
		valueCol.setResizable(true);
		valueCol.setSortable(true);
		
		ColumnConfig[] columns = new ColumnConfig[]{metricCol, valueCol};
		columnModel = new ColumnModel(columns);
		setColumnModel(columnModel);
		
		recordDef = new RecordDef(
			new FieldDef[] {
				new StringFieldDef("metric"),
				new StringFieldDef("val"),
			}
		);
		
		ArrayReader reader = new ArrayReader(recordDef);
		MemoryProxy proxy = new MemoryProxy(new Object[][]{});
		store = new Store(proxy, reader);
		store.load();
		setStore(store);		
		
		setHeight(200);
		setAutoWidth(true);
		setAutoExpandColumn("MetricsGrid_MetricColumn");
		setId("metrics-grid");
	}
	
	public void reload() {
		OntologyServiceManager.getInstance().getMetrics(project.getProjectName(),
				new GetMetrics());
	}
	
	class GetMetrics extends AbstractAsyncHandler {

		public void handleFailure(Throwable caught) {
			GWT.log("RPC error getting ontology metrics", caught);
		}

		public void handleSuccess(Object result) {
			ArrayList metrics = (ArrayList) result;
			Iterator iterator = metrics.iterator();
			while (iterator.hasNext()) {
				MetricData metric = (MetricData) iterator.next();
				Record record = recordDef.createRecord(new Object[] {
						metric.getMetricName(), metric.getMetricValue() });
				store.add(record);
			}
			
			/*
			 * Metrics grid will not refresh properly w/out these lines.
			 */
			store.commitChanges();
			//TODO: - throws exception
			//reconfigure(store, columnModel);
		}
	}
}

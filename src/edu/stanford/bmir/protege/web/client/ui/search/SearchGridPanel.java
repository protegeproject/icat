package edu.stanford.bmir.protege.web.client.ui.search;


import java.util.ArrayList;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.ObjectFieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridRowListener;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class SearchGridPanel extends GridPanel{
	protected RecordDef recordDef;
	protected Store store;
	protected EntityData currentSelection;
	protected GridRowListener gridRowListener;
		
	
	public SearchGridPanel(ArrayList<EntityData> entities) {
		createGrid();		
		fillStore(entities);
		addGridRowListener(getRowListener());
	}
	
	protected GridRowListener getRowListener() {
		if (gridRowListener == null) {
			gridRowListener = new GridRowListenerAdapter() {
				public void onRowClick(GridPanel grid, int rowIndex, EventObject e) {
					EntityData entity = (EntityData) store.getAt(rowIndex).getAsObject("entity");					
					currentSelection = entity;
				}
			};
		}		
		return gridRowListener;
	}
	
	protected void createGrid() {		
		ColumnConfig entitytCol = new ColumnConfig();
		entitytCol.setHeader("Results");
		entitytCol.setId("entity");
		entitytCol.setDataIndex("entity");
		entitytCol.setResizable(true);
		entitytCol.setSortable(true);
		//entitytCol.setRenderer(renderer);
		
		ColumnConfig[] columns = new ColumnConfig[]{entitytCol};
		
		ColumnModel columnModel = new ColumnModel(columns);
		setColumnModel(columnModel);

		recordDef = new RecordDef(
				new FieldDef[]{new ObjectFieldDef("entity")}
		);

		ArrayReader reader = new ArrayReader(recordDef);		 
		MemoryProxy dataProxy = new MemoryProxy(new Object[][]{});
		store = new Store(dataProxy, reader);
		store.load();
		setStore(store);
		
		setHeight(300);
		setAutoWidth(true);
	
		setStripeRows(true);
		setAutoExpandColumn("entity");		
	}
	
	private void fillStore(ArrayList<EntityData> entities) {
		for (EntityData entityData : entities) {
			Record record = recordDef.createRecord(new Object[]{entityData});
			store.add(record);
		}		
	}
	
	public EntityData getSelection() {
		return currentSelection;
	}

	
	Renderer renderer = new Renderer() {
		public String render(Object value, CellMetadata cellMetadata, 
				Record record, int rowIndex, int colNum, Store store) {				
			return Format.format("<style type=\"text/css\">.x-grid3-cell-inner, .x-grid3-hd-inner { white-space:normal !important; }</style> {0}", 
					new String[]{((EntityData)record.getAsObject("entity")).getBrowserText()});
		}
	};
	
}

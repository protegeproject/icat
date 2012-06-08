package edu.stanford.bmir.protege.web.client.ui.search;

import java.util.List;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.ObjectFieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.event.StoreListenerAdapter;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.PagingToolbar;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.GridRowListener;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.util.PaginationUtil;

/**
 * The panel used for displaying the search results.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class SearchGridPanel extends GridPanel {
    protected RecordDef recordDef;
    protected Store store;
    protected GridRowListener gridRowListener;
    protected SearchResultsProxyImpl proxy;
    protected Window window;

    public SearchGridPanel(List<EntityData> entities) {
        createGrid();
        fillStore(entities);
        addGridRowListener(getRowListener());
    }

    public SearchGridPanel(){
        createGrid();
        addGridRowListener(getRowListener());
    }

    public void setBusyComponent(Component busyComponent){
        proxy.setBusyComponent(busyComponent);
    }


    public void reload(String projectName, String searchText, ValueType valueType, Window window)
    {
        this.window = window;
        proxy.resetParams();
        proxy.setProjectName(projectName);
        proxy.setSearchText(searchText);
        proxy.setValueType(valueType);

        store.load(0, ((PagingToolbar) this.getBottomToolbar()).getPageSize());
    }

    protected GridRowListener getRowListener() {
        if (gridRowListener == null) {
            gridRowListener = new GridRowListenerAdapter() { //FIXME: does not work
                @Override
                public void onRowDblClick(GridPanel grid, int rowIndex, EventObject e) {
                	onEntityDblClick();
                }
            };
        }
        return gridRowListener;
    }

    protected void onEntityDblClick() { //TODO: not called; listener does not work
    }

    protected void createGrid() {
        ColumnConfig entitytCol = new ColumnConfig();
        entitytCol.setHeader("Results");
        entitytCol.setId("entity");
        entitytCol.setDataIndex("entity");
        entitytCol.setResizable(true);
        entitytCol.setSortable(true);
        //entitytCol.setRenderer(renderer);

        ColumnConfig[] columns = new ColumnConfig[] { entitytCol };

        ColumnModel columnModel = new ColumnModel(columns);
        setColumnModel(columnModel);

        recordDef = new RecordDef(new FieldDef[] { new ObjectFieldDef("entity") });

        ArrayReader reader = new ArrayReader(recordDef);

        this.proxy = new SearchResultsProxyImpl();
        store = new Store(proxy, reader);
        store.addStoreListener(new StoreListenerAdapter() {
            @Override
            public void onLoad(Store store, Record[] records) {
                if(window != null){
                    window.show();
                }
            }
        });
        setStore(store);

        PagingToolbar pToolbar = PaginationUtil.getNewPagingToolbar(store, 20);
        this.setBottomToolbar(pToolbar);

        setStripeRows(true);
        setAutoExpandColumn("entity");
    }

    private void fillStore(List<EntityData> entities) {
        for (EntityData entityData : entities) {
            Record record = recordDef.createRecord(new Object[] { entityData });
            store.add(record);
        }
    }

    public EntityData getSelection() {
        Record selRecord = getSelectionModel().getSelected();
        if (selRecord == null) { return null; }
        return (EntityData) selRecord.getAsObject("entity");
    }

    Renderer renderer = new Renderer() {
        public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                Store store) {
            return Format
                    .format(
                            "<style type=\"text/css\">.x-grid3-cell-inner, .x-grid3-hd-inner { white-space:normal !important; }</style> {0}",
                            new String[] { ((EntityData) record.getAsObject("entity")).getBrowserText() });
        }
    };

}

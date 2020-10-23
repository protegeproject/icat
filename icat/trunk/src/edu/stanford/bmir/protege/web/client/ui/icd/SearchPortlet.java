package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.ObjectFieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.event.ComboBoxCallback;
import com.gwtext.client.widgets.form.event.ComboBoxListener;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.search.SearchResultsProxyImpl;
import edu.stanford.bmir.protege.web.client.ui.selection.EntitySelectionEvent;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class SearchPortlet extends AbstractEntityPortlet {

	private ComboBox searchCb;
	
	private EntityData currentSelection;
	
	
	public SearchPortlet(Project project) {
		super(project);
	}
	
	@Override
	public void initialize() {
		setTitle("Search");
		setLayout(new FitLayout());
		add(createSearchField());
	}
	
	
    protected Component createSearchField() {
        RecordDef recordDef = new RecordDef(new FieldDef[] { new ObjectFieldDef("entity"), new StringFieldDef("browserText") });

        ArrayReader reader = new ArrayReader(recordDef);
        final SearchResultsProxyImpl proxy = new SearchResultsProxyImpl();
        proxy.setProjectName(getProject().getProjectName());
        proxy.setValueType(ValueType.Cls);
        final Store store = new Store(proxy, reader);

        searchCb = new ComboBox();
        searchCb.setStore(store);
        searchCb.setDisplayField("browserText");
        searchCb.setTypeAhead(false);
        searchCb.setLoadingText("Searching...");
        //searchCb.setListWidth(400);
        //searchCb.setWidth(150);
        searchCb.setPageSize(10);
        searchCb.setMinChars(3);
        searchCb.setQueryDelay(500);
        searchCb.setHideTrigger(true);
        searchCb.setHideLabel(true);
        searchCb.setResizable(true);
        searchCb.setEmptyText("Type search string");
        searchCb.setValueNotFoundText("No results");

        searchCb.addListener(createComboBoxListener(store, proxy));

        return searchCb;
    }

    
    private ComboBoxListener createComboBoxListener(Store store, SearchResultsProxyImpl proxy) {
    	return new ComboBoxListenerAdapter() {
        	
            @Override
            public boolean doBeforeQuery(ComboBox comboBox, ComboBoxCallback cb) {
                proxy.setSearchText(searchCb.getValueAsString());
                return true;
            }
            
            @Override
            public void onSelect(ComboBox comboBox, Record record, int index) {
                currentSelection = (EntityData)record.getAsObject("entity");
                
                comboBox.setValue(UIUtil.getDisplayText(currentSelection).trim());
                
                notifyOfSelectionChange();
            }
            
            @Override
            public void onSpecialKey(Field field, EventObject e) {
                if (e.getKey() == EventObject.ENTER) {
                    String searchText = searchCb.getValueAsString();
                    if (searchText != null) {
                        searchText = searchText.trim();
                        if (searchText.length() > 0) {
                            proxy.setSearchText(searchText);
                            store.load(0, 10);
                        } else {
                            store.removeAll();
                        }
                    } else {
                        store.removeAll();
                        currentSelection = null;
                    }
                }
            }

            @Override
            public void onValid(Field field) {
                String searchText = searchCb.getValueAsString();
                if (searchText == null || searchText.length() < 3) {
                    store.removeAll();
                    currentSelection = null;
                    searchCb.collapse();
                }
            }
        };
    }
    
    
	@Override
	public void reload() {
		//maybe this is not ideal; we can also just leave the value as it is
		EntityData entity = getEntity();
		if (entity != null) {
			searchCb.setValue(UIUtil.getDisplayText(entity));
		}
	}

	@Override
	public Collection<EntityData> getSelection() {
		return currentSelection == null ? null : UIUtil.createCollection(currentSelection);
	}

	private void notifyOfSelectionChange() {
		if (currentSelection == null) {
			return;
		}
		
		OntologyServiceManager.getInstance().getEntity(getProject().getProjectName(), currentSelection.getName(), 
				new AsyncCallback<EntityData>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Could not retrieve from server the details of " + currentSelection);
					}

					@Override
					public void onSuccess(EntityData entity) {
						currentSelection = entity;
						notifySelectionListeners(new EntitySelectionEvent(SearchPortlet.this, entity));
					}
		});
	}

}

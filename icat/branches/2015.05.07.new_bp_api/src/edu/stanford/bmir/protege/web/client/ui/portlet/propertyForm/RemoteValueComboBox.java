package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.DataProxy;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.event.ComboBoxCallback;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.listener.OntologyListener;
import edu.stanford.bmir.protege.web.client.model.listener.OntologyListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.util.UIConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public abstract class RemoteValueComboBox extends AbstractFieldWidget {

    private static final String ENTITY_NAME_FIELD = "entityName";
    private static final String BROWSER_TEXT_FIELD = "browserText";

    private static int DEFAULT_PAGE_SIZE = 20;

//    String labelText;
//    private HTML loadingIcon;
    protected ComboBox comboBox;
    protected Store store;
    private DataProxy proxy;
    private RecordDef recordDef;

	
	public RemoteValueComboBox(Project project) {
		super(project);
	}

	
    @Override
    public Field createFieldComponent() {
        comboBox = new ComboBox();
        comboBox.setForceSelection(true);
        comboBox.setMinChars(1);
        comboBox.setTriggerAction(ComboBox.ALL);
        comboBox.setEmptyText("Select a value");
        comboBox.setTypeAhead(true);
        comboBox.setForceSelection(true);
        comboBox.setSelectOnFocus(true);
        comboBox.setHideTrigger(false);
        comboBox.setPageSize(UIUtil.getIntegerConfigurationProperty(getWidgetConfiguration(), FormConstants.PAGE_SIZE, DEFAULT_PAGE_SIZE));

//        loadingIcon = new HTML("<img src=\"" + UIConstants.ICON_LOADING_PLACEHOLDER + "\"/>");
//        loadingIcon.setStyleName("loading-img");
        
        String labelText = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), FormConstants.LABEL, getProperty().getBrowserText());
        comboBox.setLabel(labelText);

        recordDef = new RecordDef(
                new FieldDef[] {
                    //would be better to store the EntityData itself, but I could not get the selected record from the combobox
                    new StringFieldDef(ENTITY_NAME_FIELD),
                    new StringFieldDef(BROWSER_TEXT_FIELD),
                }
            );
        ArrayReader reader = new ArrayReader(recordDef);

        readConfiguration();
        
        proxy = createProxy();
        store = new Store(proxy, reader);

        comboBox.setDisplayField(BROWSER_TEXT_FIELD);
        comboBox.setValueField(ENTITY_NAME_FIELD);
        comboBox.setStore(store);

        return comboBox;
    }

//	@Override
//	public void setLoadingStatus(boolean loading) {
//		// TODO Auto-generated method stub
//		System.out.println("set loading status to " + loading);
//		super.setLoadingStatus(loading);
//        loadingIcon.setHTML("<img src=\"" + (loading ? "images/loading.gif" : UIConstants.ICON_LOADING_PLACEHOLDER) + "\"/>");
//        getField().setLabel(getLabelHtml(labelText, getHelpURL(), getTooltipText()));
//        //getField().setLabel(""+  loading + "" +loadingIcon.getHTML());
//	}

    /**
     * Read widget specific configuration values and initialize
     * private/protected fields if necessary.
     */
	protected abstract void readConfiguration();

	/**
	 * Create a DataProxy to store the allowed values for the combobox.
	 * The implementation of this method would return a GWTProxy in case
	 * of paginated content.
	 * 
	 * @return a DataProxy to present the allowed values of this combo box.
	 */
	protected abstract DataProxy createProxy();


//	@Override
//	protected String getLabelHtml(String label, String helpURL, String tooltip) {
//		String htmlLabel = super.getLabelHtml(label, helpURL, tooltip);
//		if (htmlLabel.contains("</")) {
//			int insPos = htmlLabel.lastIndexOf("</");
//			htmlLabel = htmlLabel.substring(0, insPos) + loadingIcon.getHTML() + htmlLabel.substring(insPos);
//		}
//		return htmlLabel;
//	}
	
    @Override
    protected void attachFieldChangeListener() {
        comboBox.addListener(getComboBoxListenerListener());
    }

	protected ComboBoxListenerAdapter getComboBoxListenerListener() {
		return new RemoteValueComboBoxListenerAdapter();
	}

    @Override
    protected void displayValues(Collection<EntityData> values) {
        if (values == null || values.size() == 0) {
            comboBox.clearValue();
        } else {
            //FIXME: for now only deal with one value
            EntityData value = values.iterator().next();
            if (value != null) {
                comboBox.setValue(value.getName());
            } else {
                comboBox.clearValue();
            }
        }
    }

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        cacheAllowedValues();
        addOntolgyChangeListener();
    }

    protected void addOntolgyChangeListener() {
        OntologyListenerAdapter ontologyListener = getOntologyListener();
        if (ontologyListener != null) {
        	getProject().addOntologyListener(ontologyListener);
        }
    }

    /**
     * Returns an ontology listener attached to this combo box.
     * @return an {@link OntologyListener}
     */
	protected abstract OntologyListenerAdapter getOntologyListener();

	/**
	 * Caches allowed values. This method will be called at least once,
	 * during the widget setup, and possibly other times as well, when 
	 * the content of the drop down menu needs to be changed because
	 * of selection change or changes in the ontology.
	 */
    protected abstract void cacheAllowedValues();

    /*
     * Combobox listener
     */
    
    protected class RemoteValueComboBoxListenerAdapter extends ComboBoxListenerAdapter {
        @Override
        public boolean doBeforeQuery(ComboBox comboBox, ComboBoxCallback cb) {
            return isWriteOperationAllowed();
        }

        @Override
        public boolean doBeforeSelect(ComboBox comboBox, Record record, int index) {
            if (!isWriteOperationAllowed()) {
                return false;
            }
            int oldIndex = store.find(ENTITY_NAME_FIELD, comboBox.getValue(), 0, true, true);
            EntityData oldValueEd = new EntityData(comboBox.getValue(), oldIndex >= 0 ? store.getAt(oldIndex).getAsString(BROWSER_TEXT_FIELD) : null);
            EntityData newValue = new EntityData(record.getAsString(ENTITY_NAME_FIELD), record.getAsString(BROWSER_TEXT_FIELD));
            onChangeValue(getSubject(), oldValueEd, newValue);
            return true;
        }
    }
    
    /*
     * Remote handlers
     */

    protected class FillAllowedValuesCacheHandler extends AbstractAsyncHandler<List<EntityData>> {
    	
        public FillAllowedValuesCacheHandler() {
			// TODO Auto-generated constructor stub
		}

		@Override
        public void handleFailure(Throwable caught) {
            GWT.log("Could not retrieve allowed values for combobox " + getProperty(), caught);
        }

        @Override
        public void handleSuccess(List<EntityData> superclses) {
        	loadDropDownValues(superclses);
        }
    }
    
	protected void loadDropDownValues(List<EntityData> instances) {
		
        store.removeAll();
        setLoadingStatus(false);
		System.out.println("In fill values handler: " + instances);
		
		Object[][] results = getRows(instances);
		System.out.println(" Results: " + results);
		
        store.setDataProxy(new MemoryProxy(results));
        store.load();
	}
    
    private Object[][] getRows(List<EntityData> instances) {
		if (instances != null) {
			Object[][] resultAsObjects = new Object[instances.size()][2];
	        int i = 0;
	        for (EntityData inst : instances) {
	            resultAsObjects[i++] =new Object[]{inst.getName(), UIUtil.getDisplayText(inst)};
	        }
	        return resultAsObjects;
		}
		else {
			return new Object[0][2];
		}
    }


}

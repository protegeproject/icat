package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.data.DataProxy;
import com.gwtext.client.data.MemoryProxy;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.listener.OntologyListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.RemoteValueComboBox;

public class SuperclassSelectorWidget extends RemoteValueComboBox {

	private FillAllowedValuesCacheHandler fillValuesHandler = null;
	private PreCoordinationWidget preCoordinationWidget = null;	//reference to the PreCoordinationWidget that contains this, if any
    
    private PreCoordinationWidgetController widgetController;	//external widget controller

	public SuperclassSelectorWidget(Project project, PreCoordinationWidgetController widgetController) {
		super(project);
		this.widgetController = widgetController;
	}

	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {
		System.out.println("Print setup superclass widget for subject: " + getSubject());
		super.setup(widgetConfiguration, propertyEntityData);
	}
	
	@Override
	public void setSubject(EntityData subject) {
		super.setSubject(subject);
		System.out.println("Set subject: " + subject );
		setLoadingStatus(true);
		ICDServiceManager.getInstance().getAllSuperEntities(getProject().getProjectName(), getSubject(), getFillValuesHandler());
		widgetController.onSubjectChanged(subject);
	}
	
	@Override
	public void fillValues() {
		// TODO Auto-generated method stub
		super.fillValues();
//		selectionChanged(firstValue);
	}

	@Override
	public void setValues(Collection<EntityData> values) {
		Collection<EntityData> oldValues = getValues();
		super.setValues(values);
		System.out.println("Set values for superclass, subject: " + getSubject() + ", values: " + values);
		EntityData firstValue = null;
		if ( ! values.isEmpty()) {
			firstValue = values.iterator().next();
		}
		if (differentCollections(oldValues, values)) {
			selectionChanged(firstValue);
		}
	}
	
	private boolean differentCollections(Collection<EntityData> oldValues, Collection<EntityData> newValues) {
		if (oldValues == null) {
			if (newValues == null) {
				//they are the same (both are null)
				return false;
			}
			else {
				//they are different (one is null the other is not)
				return true;
			}
		}
		else {
			if (newValues == null) {
				//they are different (one is null the other is not)
				return true;
			}
			else {
				if (oldValues.size() != newValues.size()) {
					//they are different (they have different size)
					return true;
				}
				else {
					//if retaining all values in newValues in oldValues would modify oldValues
					//(i.e. the intersection of the two collection is different from oldValues)
					//then return true, otherwise they must be equal, so return false.
					return oldValues.retainAll(newValues);
				}
			}
		}
	}
	
	@Override
	public void refresh() {
		System.out.println("refresh");
		super.refresh();
		//TODO maybe add this selectionChanged(getSelection());
	}
	
	private FillAllowedValuesCacheHandler getFillValuesHandler() {
		if (fillValuesHandler != null) {
			return fillValuesHandler;
		}
		else {
			return fillValuesHandler = new FillAllowedValuesCacheHandler();
		}
	}

	public void setPreCoordinationWidget(
			PreCoordinationWidget preCoordinationWidget) {
		this.preCoordinationWidget  = preCoordinationWidget;
	}

	@Override
	protected void onChangeValue(EntityData subj, Object oldVal, Object newVal) {
		super.onChangeValue(subj, oldVal, newVal);
		selectionChanged((EntityData)newVal);
	}
	
	private void selectionChanged(EntityData newVal) {
		if (preCoordinationWidget != null) {
			preCoordinationWidget.onSuperclassChanged(newVal);
		}
		if (widgetController != null) {
			widgetController.onSuperclassChanged(newVal);
		}
	}
	
	public String getSelection() {
		return getField().getValueAsString();
	}

	@Override
	protected void readConfiguration() {
		//Load widget specific configurations, if necessary
	}

	@Override
	protected DataProxy createProxy() {
        readConfiguration();

		MemoryProxy proxy = new MemoryProxy(new Object[][] {{}});
        return proxy;
	}

	@Override
	protected OntologyListenerAdapter getOntologyListener() {
		//We should watch for superclasses added/removed events,
		//but currently there is no support for such events, and 
		//it is not worth adding it at the moment. People can always
		//refresh the content of the containing portlet if they don't 
		//see newly added superclasses.
		
		return null;
	}

	@Override
	protected void cacheAllowedValues() {
		System.out.println("Cache allowed values for: " + getSubject());
		setLoadingStatus(true);
		ICDServiceManager.getInstance().getAllSuperEntities(getProject().getProjectName(), getSubject(), getFillValuesHandler());
	}
	
	/* Remote calls */
	
    private class FillAllowedValuesCacheHandler extends AbstractAsyncHandler<List<EntityData>> {
    	
        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Could not retrieve allowed values for combobox " + getProperty(), caught);
        }

        @Override
        public void handleSuccess(List<EntityData> superclses) {
            store.removeAll();
            setLoadingStatus(false);
			System.out.println("In fill values handler: " + superclses);
			
			Object[][] results = getRows(superclses);
			System.out.println(" Results: " + results);
			
            store.setDataProxy(new MemoryProxy(results));
            store.load();
        }
        
        private Object[][] getRows(List<EntityData> superclses) {
            Object[][] resultAsObjects = new Object[superclses.size()][2];
            int i = 0;
            for (EntityData supercls : superclses) {
                resultAsObjects[i++] =new Object[]{supercls.getName(), supercls.getBrowserText()};
            }
            return resultAsObjects;
        }

    }
	
}

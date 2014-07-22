package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
import edu.stanford.bmir.protege.web.client.model.listener.OntologyListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.ontology.individuals.PagedIndividualsProxyImpl;
import edu.stanford.bmir.protege.web.client.ui.util.GWTProxy;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class InstanceComboBox extends RemoteValueComboBox {

	private String allowedType;

    public InstanceComboBox(Project project) {
        super(project);
    }

    @Override
	protected void readConfiguration() {
		allowedType = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), FormConstants.ONT_TYPE, null);
	}
    
    @Override
	protected GWTProxy createProxy() {
        readConfiguration();

		PagedIndividualsProxyImpl proxy = new PagedIndividualsProxyImpl();
        proxy.setProjectName(getProject().getProjectName());
        proxy.setClassName(allowedType);
        return proxy;
	}

    @Override
	protected OntologyListenerAdapter getOntologyListener() {
		return new OntologyListenerAdapter() {
            @Override
            public void individualAddedRemoved(OntologyEvent ontologyEvent) {
            	EntityData eventEntity = ontologyEvent.getEntity();
            	String clsName = (eventEntity == null ? null : eventEntity.getName());
            	//if individual was added or removed to the class 
            	//whose instances are displayed in this combobox (or to an unknown class)
            	if (clsName == null || clsName.equals(allowedType)) {
            		cacheAllowedValues(); //reset cache
            	}
            }

        };
	}

    @Override
    protected void cacheAllowedValues() {
        if (allowedType == null) {
        	readConfiguration();
        }
        store.removeAll();
        OntologyServiceManager.getInstance().getIndividuals(getProject().getProjectName(), allowedType, new FillAllowedValuesCacheHandler());
    }


}

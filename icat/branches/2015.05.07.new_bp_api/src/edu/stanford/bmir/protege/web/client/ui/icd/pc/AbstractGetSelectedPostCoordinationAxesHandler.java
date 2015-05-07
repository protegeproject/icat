package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.List;

import com.google.gwt.core.client.GWT;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.PropertyWidget;

abstract public class AbstractGetSelectedPostCoordinationAxesHandler extends AbstractAsyncHandler<List<String>> {
	private final PropertyWidget widget;
	private EntityData subject;
	private List<String> properties;
	
    public AbstractGetSelectedPostCoordinationAxesHandler(PropertyWidget widget, EntityData subject, List<String> properties) {
		this.widget = widget;
		this.subject = subject;
		this.properties = properties;
	}

	@Override
	public void handleFailure(Throwable caught) {
        GWT.log("Error at getting list if selected post-cooordination axes properties for " + subject, caught);
    	for (String propertyName : properties) {
    		//activateValueSelectionWidget(propertyName);
    		updateUI(propertyName, true);
    	}
	}

	@Override
    public void handleSuccess(List<String> selectedProperties) {
		//in case the selection did not change since the remote method was called
		if (subject != null && subject.equals(widget.getSubject())) {
        	for (String propertyName : properties) {
        		if (selectedProperties.contains(propertyName)) {
        			//activateValueSelectionWidget(propertyName);
        			updateUI(propertyName, true);
        		}
        		else {
        			//deactivateValueSelectionWidget(propertyName);
        			updateUI(propertyName, false);
        		}
        	}
		}
    }
	
	abstract public void updateUI(String propertyName, boolean selected);
	
}
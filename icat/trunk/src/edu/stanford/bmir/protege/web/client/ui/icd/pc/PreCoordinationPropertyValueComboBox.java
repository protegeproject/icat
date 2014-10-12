package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.List;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceComboBox;

public class PreCoordinationPropertyValueComboBox extends InstanceComboBox {

	public PreCoordinationPropertyValueComboBox(Project project) {
		super(project);
	}

	public void updateAllowedValues(List<EntityData> allowedValues) {
		loadDropDownValues(allowedValues);
	}
	
	@Override
	protected void cacheAllowedValues() {
		//We do not need to get all the instances of the class set in the configuration
		//since the allowed values will be set through the widget controller
	}
}
 
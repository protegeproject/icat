package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Map;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.generated.ClassTreeFactory;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;

public class ClassSelectionFieldWidget extends AbstractSelectionFieldWidget {

	private String topClass;

	public ClassSelectionFieldWidget(Project project) {
		super(project);		
	}

	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {
		super.setup(widgetConfiguration, propertyEntityData);
        resetTopClass();
	}

	public void resetTopClass() {
		this.topClass = (String) getWidgetConfiguration().get(FormConstants.TOP_CLASS);
	}

	public void setTopClass(String topClass) {
		this.topClass = topClass;
	}
	
	public Selectable createSelectable() {
		ClassTreePortlet classTreePortlet = ClassTreeFactory.getICDClassTreePortlet(getProject(), false, false, false, false, topClass);
		return classTreePortlet;
	}
	
	
}

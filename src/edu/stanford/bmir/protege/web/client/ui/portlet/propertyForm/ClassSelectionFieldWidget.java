package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Map;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
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
        topClass = (String) widgetConfiguration.get(FormConstants.TOP_CLASS);
	}
	
	public Selectable createSelectable() {
		ClassTreePortlet classTreePortlet = new ClassTreePortlet(getProject());
		classTreePortlet.setHeight(250);
		classTreePortlet.setWidth(200);
		classTreePortlet.setTopClass(topClass);
		return classTreePortlet;
	}
}

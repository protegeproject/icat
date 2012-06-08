package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import edu.stanford.bmir.protege.web.client.ui.ontology.properties.PropertiesTreePortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.Selectable;

public class PropertySelectionFieldWidget extends AbstractSelectionFieldWidget {

	public PropertySelectionFieldWidget(Project project) {
		super(project);		
	}

	@Override
	public Selectable createSelectable() {
		PropertiesTreePortlet propertiesTreePortlet = new PropertiesTreePortlet(getProject());
		propertiesTreePortlet.setHeight(250);
		propertiesTreePortlet.setWidth(200);
		return propertiesTreePortlet;
	}

}

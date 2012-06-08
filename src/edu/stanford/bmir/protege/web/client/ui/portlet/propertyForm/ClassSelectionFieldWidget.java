package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.Selectable;

public class ClassSelectionFieldWidget extends AbstractSelectionFieldWidget {

	public ClassSelectionFieldWidget(Project project) {
		super(project);		
	}

	
	public Selectable createSelectable() {
		ClassTreePortlet classTreePortlet = new ClassTreePortlet(getProject());
		classTreePortlet.setHeight(250);
		classTreePortlet.setWidth(200);
		return classTreePortlet;
	}
}

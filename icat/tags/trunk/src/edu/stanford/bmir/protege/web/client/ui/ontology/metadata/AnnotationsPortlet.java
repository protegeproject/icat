package edu.stanford.bmir.protege.web.client.ui.ontology.metadata;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Jennifer Vendetti
 */
public class AnnotationsPortlet extends AbstractEntityPortlet {

	protected AnnotationsGrid annotationsGrid;
	
	public AnnotationsPortlet(Project project) {
		super(project);
	}

	public void intialize() {
		setTitle("Ontology Annotations");
		this.annotationsGrid = new AnnotationsGrid(project.getProjectName());
		add(annotationsGrid);
	}

	public void reload() { 
		if (_currentEntity != null) {
			String title = _currentEntity.getBrowserText();
			if (title.length() > 20) {
				title = "   ..." + title.substring(title.length() - 20, title.length());
			}
			setTitle("Ontology Annotations for " + title);
		}
		
		annotationsGrid.setEntity(_currentEntity);
	}

	public ArrayList getSelection() {
		return null;
	}
}

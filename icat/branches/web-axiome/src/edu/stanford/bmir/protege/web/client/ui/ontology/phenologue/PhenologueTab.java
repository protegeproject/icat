package edu.stanford.bmir.protege.web.client.ui.ontology.phenologue;

import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Saeed
 * 
 */
public class PhenologueTab extends AbstractTab {

	public PhenologueTab(Project project) {
		super(project);
	}

	@Override
	public String getLabel() {
		return "Axiome";
	}

}

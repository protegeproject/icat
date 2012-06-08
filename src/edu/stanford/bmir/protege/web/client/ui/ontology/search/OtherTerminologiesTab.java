package edu.stanford.bmir.protege.web.client.ui.ontology.search;

import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.util.Project;

public class OtherTerminologiesTab extends AbstractTab {

	public OtherTerminologiesTab(Project project) {				
		super(project);		
	}
	
	@Override
	public String getLabel() {
		return "Other Terminologies"; 
	}
}

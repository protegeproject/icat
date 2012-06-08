package edu.stanford.bmir.protege.web.client.ui.ontology.SWRL;

import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * A tab which displays the SWRLTab Portlet
 * 
 * @author Mike Uehara-Bingen <mike.bingen@stanford.edu>
 */
public class SWRLTab
        extends AbstractTab {

	public SWRLTab(Project project) {
		super(project);
	}

	@Override
	public String getLabel() {
		return "SWRL";
	}

}

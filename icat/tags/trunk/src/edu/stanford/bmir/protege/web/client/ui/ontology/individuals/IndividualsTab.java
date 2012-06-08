package edu.stanford.bmir.protege.web.client.ui.ontology.individuals;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.SelectionEvent;
import edu.stanford.bmir.protege.web.client.util.SelectionListener;

/**
 * A single view that shows the classes in an ontology.
 * 
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class IndividualsTab extends AbstractTab {

	protected ClassTreePortlet clsTreePortlet;
	protected IndividualsListPortlet indListPorlet;	

	public IndividualsTab(Project project) {		
		super(project);
	}

	public void setup() {
		super.setup();

		clsTreePortlet = (ClassTreePortlet) getPortletByClassName(ClassTreePortlet.class.getName());		
		indListPorlet = (IndividualsListPortlet) getPortletByClassName(IndividualsListPortlet.class.getName());

		setControllingPortlet(indListPorlet);

		if (clsTreePortlet != null && indListPorlet != null) {
			clsTreePortlet.addSelectionListener(new SelectionListener() {
				public void selectionChanged(SelectionEvent event) {
					indListPorlet.setEntity((EntityData) clsTreePortlet.getSelection().get(0));				
				}
			});		
		}
	}
	
}

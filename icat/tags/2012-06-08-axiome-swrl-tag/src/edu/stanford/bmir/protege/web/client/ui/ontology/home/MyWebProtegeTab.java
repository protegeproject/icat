package edu.stanford.bmir.protege.web.client.ui.ontology.home;

import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Label;

import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class MyWebProtegeTab extends Panel {
	
	private Project project;	
	protected OntologiesPortlet ontologiesPortlet;

	public MyWebProtegeTab() {
		this(null);
	}
	
	public MyWebProtegeTab(Project project) {
		this.project = project;
		initializeUI();
	}
	
	public String getLabel() {
		return "My WebProt\u00E9g\u00E9";
	}
	
	public OntologiesPortlet getOntologiesPortlet() {
		return ontologiesPortlet;
	}

	Label label ;
	public void initializeUI() {	
		ontologiesPortlet = new OntologiesPortlet(project);
		add(ontologiesPortlet);		
	}
	
}

package edu.stanford.bmir.protege.web.client.ui.ontology.metadata;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Jennifer Vendetti
 */
public class MetricsPortlet extends AbstractEntityPortlet {
	
	protected static boolean initialized = false;
	protected MetricsGrid metricsGrid;

	public MetricsPortlet(Project project) {
		super(project);
	}

	public void reload() {
		if (initialized) return;
		metricsGrid.setEntity(_currentEntity);
	}
	
	public void intialize() {
		setTitle("Ontology Metrics for " + project.getProjectName());
		
		metricsGrid = new MetricsGrid(project);
		add(metricsGrid);
		
		metricsGrid.reload();
		
		/*
		 * Don't want metrics to change based on what is selected in the 
		 * imported ontologies tree (unlike other portlets that do update).
		 * This portlet is part of the Metadata tab and the controlling 
		 * portlet for that tab is the ImportsTreePortlet.
		 */
		initialized = true;		
	}
	
	public ArrayList getSelection() {
		return null;
	}
}

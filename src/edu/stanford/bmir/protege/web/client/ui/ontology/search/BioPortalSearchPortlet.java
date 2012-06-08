package edu.stanford.bmir.protege.web.client.ui.ontology.search;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

public class BioPortalSearchPortlet extends AbstractEntityPortlet {

	private BioPortalSearchComponent searchComp;
	
	public BioPortalSearchPortlet(Project project) {
		super(project);	
	}

	@Override
	public void intialize() {
		setTitle("BioPortal Search");
		searchComp = new BioPortalSearchComponent(project);
		add(searchComp);
	}

	public void setPortletConfiguration(PortletConfiguration portletConfiguration) {
		super.setPortletConfiguration(portletConfiguration);
		searchComp.setConfigProperties(portletConfiguration.getProperties());
	}
	
	@Override
	public void reload() {
		if (_currentEntity == null) {
			return;
		}
		setTitle("BioPortal search results for " + _currentEntity.getBrowserText());
		searchComp.setEntity(_currentEntity);
	}

	public ArrayList<EntityData> getSelection() {		
		return null;
	}

}

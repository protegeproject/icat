package edu.stanford.bmir.protege.web.client.ui.ontology.classes;

import java.util.ArrayList;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class AllPropertiesPortlet extends AbstractEntityPortlet {

	protected AllPropertiesGrid propGrid;
	
	public AllPropertiesPortlet(Project project) {
		super(project);				
	}
	
	public void reload() {
		if (_currentEntity != null) {
			setTitle("Properties for " + _currentEntity.getBrowserText());
		}

		propGrid.setEntity(_currentEntity);
	}

	public void intialize() {
		setTitle("Properties");
		this.propGrid = new AllPropertiesGrid(project);		
		add(propGrid);
	}

	public ArrayList<EntityData> getSelection() {
		return propGrid.getSelection();
	}
	
	@Override
	protected void onRefresh() {
		propGrid.refresh();
	}	
	
	@Override
	public void onLogin(String userName) {
		propGrid.updateButtonStates();
	}
	
	@Override
	public void onLogout(String userName) {
		propGrid.updateButtonStates();
	}

}

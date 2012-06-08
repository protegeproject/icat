package edu.stanford.bmir.protege.web.client.ui.ontology.restrictions;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

public class RestrictionsPortlet extends AbstractEntityPortlet {
	
	private Panel panel;
	
	public RestrictionsPortlet(Project project) {
		super(project);
	}

	@Override
	public void intialize() {
		setTitle("Axioms");
		panel = new Panel();
		panel.setHeight(100);
		panel.setCls("restriction_panel");
		panel.setAutoScroll(true);
		add(panel);
	}

	@Override
	public void reload() {
		if (_currentEntity != null) {
			setTitle("Axioms for " + _currentEntity.getBrowserText());
		}						
		OntologyServiceManager.getInstance().getRestrictionHtml(project.getProjectName(),
				_currentEntity.getName(), new GetRestrictionsHtmlHandler());
	}	

	public ArrayList<EntityData> getSelection() {
		return new ArrayList<EntityData>();
	}
	
	/*
	 * RPC
	 */

	class GetRestrictionsHtmlHandler extends AbstractAsyncHandler<String> {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting restrictions", caught);			
		}

		@Override
		public void handleSuccess(String html) {
			panel.setHtml(html);			
		}		
	}
	
}

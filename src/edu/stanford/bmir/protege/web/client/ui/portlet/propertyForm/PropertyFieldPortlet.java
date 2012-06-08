package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FormLayout;
import com.gwtext.client.widgets.portal.Portlet;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

public class PropertyFieldPortlet extends AbstractEntityPortlet {

	private Map<PropertyEntityData, List<EntityData>> prop2values = new HashMap<PropertyEntityData, List<EntityData>>();
	private TabPanel wrappingPanel;
	private boolean needsNewFormGenerator = false;
	private FormGenerator formGenerator;
	
	/*
	 * This portlet should replace the displayed form based on the configuration.
	 * However! I had problems with adding and removing dynamically a TabPanel
	 * in the portlet and that's why I have made a workaround in which
	 * the form generator will add the tabs to an existing tab panel (the wrapping panel).
	 * In the ideal case, there should be no wrapping panel, and the generator 
	 * should generate a tab panel (methods are already available), but for
	 * some mysterious reason, this does not work.
	 */
	
	
	public PropertyFieldPortlet(Project project) {
		super(project);		
	}

		
	@Override
	public void intialize() {		
		setTitle("Properties");			
		wrappingPanel = new TabPanel();		
		wrappingPanel.setBorder(false);
		wrappingPanel.setHeight(700);		
		add(wrappingPanel);
	}
	
	@Override
	public void setEntity(EntityData newEntity) {
		//just being lazy ...
		try {
			needsNewFormGenerator = _currentEntity.getType().equals(newEntity.getType());
		} catch (Exception e) {
			needsNewFormGenerator = true;
		}	
		super.setEntity(newEntity);
	}
	
	@Override
	public void reload() {
		OntologyServiceManager.getInstance().getEntityTriples(project.getProjectName(), _currentEntity.getName(), new GetTriples());
	}

	public ArrayList<EntityData> getSelection() {		
		return new ArrayList<EntityData>();
		// TODO Auto-generated method stub
	}


	protected boolean needsNewFormGenerator() {
		//return needsNewFormGenerator; //TODO: commented out just for debugging
		return false;
	}
	
	protected FormGenerator getFormGenerator() {
		Map<String, Object> properties = getPortletConfiguration().getProperties();
		if (properties == null) { return null; }
		//String portletId = wrappingPanel.getElement().getId();
		//String portletId = getElement()  == null ? null : getElement().getId();
		Portlet portletId = this;
		if (formGenerator == null) { //at init
			formGenerator = new FormGenerator(project, properties);
			formGenerator.setPortletId(portletId);
			wrappingPanel.removeAll(true);	
			formGenerator.addFormToTabPanel(wrappingPanel);
			wrappingPanel.activate(0);
		}
		else {
			if (needsNewFormGenerator()) {
				formGenerator = new FormGenerator(project, properties);
			}
			formGenerator.setPortletId(portletId);
		}
		return formGenerator;
	}
	
	/*
	 * Remote calls
	 */

	class GetTriples extends AbstractAsyncHandler<ArrayList<Triple>> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting triples for " + _currentEntity, caught);
		}

		public void handleSuccess(ArrayList<Triple> triples) {				
			FormGenerator generator = getFormGenerator();		
			if (generator != null) {				
				generator.fillValues(triples, getEntity());			
			} else {
				addDefaultConfig(triples);
			}	
				
			PropertyFieldPortlet.this.doLayout();
		}		

		private void add(PropertyEntityData prop, EntityData value) {
			List<EntityData> values = prop2values.get(prop);
			if (values == null) {
				values = new ArrayList<EntityData>();
				prop2values.put(prop, values);
			}
			values.add(value);
		}

		private Panel addDefaultConfig(ArrayList<Triple> triples) {			
			wrappingPanel.removeAll();
			
			Panel panel1 = new Panel();	
			panel1.setLayout(new FormLayout());						
			panel1.setPaddings(5);	
			panel1.setBorder(false);
			panel1.setTitle("Properties");		
			
			for (Iterator<Triple> iterator = triples.iterator(); iterator.hasNext();) {
				Triple triple = (Triple) iterator.next();
				add(triple.getProperty(), triple.getValue());

				TextField tf = new TextField(triple.getProperty().getBrowserText());				
				tf.setValue(triple.getValue().getBrowserText());						
				tf.setReadOnly(false);				
				tf.setDisabled(false);				
				panel1.add(tf, new AnchorLayoutData("100%"));
			}
			wrappingPanel.add(panel1);
						
			return null;
		}		
		
	}	


}

package edu.stanford.bmir.protege.web.client.ui.portlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.util.Project;

public abstract class AbstractPropertyWidget implements PropertyWidget {

	private Project project;
	private EntityData subject;
	private PropertyEntityData property;
	private Map<String, Object> widgetConfiguration;

	public AbstractPropertyWidget(Project project) {
		this.project = project;
	}
	
	public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
		setProperty(propertyEntityData);
		setWidgetConfiguration(widgetConfiguration);
	}
	
	//public abstract Component getComponent(); 
	public abstract Widget getComponent();
	
	public Collection<EntityData> getValues() {	
		return new ArrayList<EntityData>();
	}
	
	public void setValues(Collection<EntityData> values) {}
	
	public void setSubject(EntityData subject) {
		this.subject = subject;
	}
	
	public EntityData getSubject() {	
		return subject;
	}
	
	public void setProperty(PropertyEntityData property) {
		this.property = property;
	}
	
	public PropertyEntityData getProperty() {	
		return property;
	}

	public Project getProject() {
		return project;
	}

	public void setWidgetConfiguration(Map<String, Object> widgetConfiguration) {
		this.widgetConfiguration = widgetConfiguration; //TODO: maybe need to save
	}

	public Map<String, Object> getWidgetConfiguration() {
		return widgetConfiguration;
	}
}

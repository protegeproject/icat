package edu.stanford.bmir.protege.web.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;

public class PropertyValueUtil {
	
	public void deletePropertyValue(String projectName, 
			String entityName, String propName, ValueType propValueType, String value, AsyncCallback<Void> asyncCallback) {
		if (propValueType == null) { propValueType = ValueType.String; }
		EntityData oldEntityData = new EntityData(value);
		oldEntityData.setValueType(propValueType);
		OntologyServiceManager.getInstance().removePropertyValue(projectName, entityName,
				new PropertyEntityData(propName), oldEntityData, new RemovePropertyValueHandler(entityName, propName, asyncCallback));
	}
	
	//TODO: assume value value type is the same as the property value type, fix later
	public void replacePropertyValue(String projectName, String entityName, String propName, ValueType propValueType, String oldValue, String newValue, AsyncCallback<Void> asyncCallback) {
		if (propValueType == null) { propValueType = ValueType.String; }
		EntityData oldEntityData = new EntityData(oldValue);
		oldEntityData.setValueType(propValueType);
		EntityData newEntityData = new EntityData(newValue);
		newEntityData.setValueType(propValueType);
		OntologyServiceManager.getInstance().replacePropertyValue(projectName, entityName,
				new PropertyEntityData(propName), oldEntityData, newEntityData, new ReplacePropertyValueHandler(entityName, propName, newEntityData, asyncCallback));
	}	
	
	//TODO: assume value value type is the same as the property value type, fix later
	public void addPropertyValue(String projectName, String entityName, String propName, ValueType propValueType, String newValue, AsyncCallback<Void> asyncCallback) {				
		EntityData newEntityData = new EntityData(newValue);
		newEntityData.setValueType(propValueType);
		OntologyServiceManager.getInstance().addPropertyValue(projectName, entityName,
				new PropertyEntityData(propName), newEntityData, new AddPropertyValueHandler(entityName, propName, asyncCallback));
	}	
	
	
	abstract class AbstractPropertyHandler<T> extends AbstractAsyncHandler<T> {
		private AsyncCallback<T> asyncCallback;
		private String subject;
		private String property;
		
		public AbstractPropertyHandler(String subject, String property, AsyncCallback<T> asyncCallback) {
			this.asyncCallback = asyncCallback;
			this.subject = subject;
			this.property = property;
		}
		
		public String getSubject() {
			return subject;
		}
		
		public String getProperty() {
			return property;
		}
		
		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at removing value for " + getProperty() + " and " + getSubject(), caught);
			Window.alert("There was an error at removing the property value for " + getProperty() + " and " + getSubject() + ".");
			asyncCallback.onFailure(caught);
		}
		
		@Override
		public void handleSuccess(T result) {
			asyncCallback.onSuccess(result);			
		}
	}
	
	
	/*
	 * Remote calls
 	 */
	class RemovePropertyValueHandler extends AbstractPropertyHandler<Void> {

		public RemovePropertyValueHandler(String subject, String property, AsyncCallback<Void> asyncCallback) {
			super(subject, property, asyncCallback);
		}


		@Override
		public void handleSuccess(Void result) {
			GWT.log("* Success at removing value for "  + getProperty() + " and " + getSubject(), null);
			super.handleSuccess(result);
		}
		
	}
	
	class ReplacePropertyValueHandler extends AbstractPropertyHandler<Void> {

		private EntityData newEntityData;
		
		public ReplacePropertyValueHandler(String subject, String property, EntityData newEntityData, AsyncCallback<Void> asyncCallback) {
			super(subject, property, asyncCallback);
			this.newEntityData = newEntityData;
		}

		@Override
		public void handleSuccess(Void result) {		
			GWT.log("Success at setting value for " + getProperty() + " and " + getSubject(), null);
			super.handleSuccess(result);
		}
	}
	
	class AddPropertyValueHandler extends AbstractPropertyHandler<Void> {	
	
		public AddPropertyValueHandler(String subject, String property, AsyncCallback<Void> asyncCallback) {
			super(subject, property, asyncCallback);
		}

		@Override
		public void handleSuccess(Void result) {		
			GWT.log("Success at adding value for " + getProperty() + " and " + getSubject(), null);
			super.handleSuccess(result);
		}
	}

	
	
}

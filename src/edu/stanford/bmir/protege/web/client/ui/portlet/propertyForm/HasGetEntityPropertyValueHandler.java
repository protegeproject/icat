package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.OntologyService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;

/**
 * This interface should be implemented by all widgets which want to retrieve
 * their values via a bulk remote call to {@link OntologyService#getEntityPropertyValues(String, List, java.util.Map)}
 * 
 * The widget can retrieve its values by overriding the method {@link #setPreloadedValues(EntityData, List)}.
 * 
 * @author ttania
 *
 */
public interface HasGetEntityPropertyValueHandler {

	/**
	 * This method is called by the {@link GetEntityPropertyValuesHandler} before it does a bulk call to
     * retrieve the values for several widgets that implement {@link HasGetEntityPropertyValueHandler}  
     * A widget can implement this method if it needs to do some other calls before the 
     * bulk call to retrieve the remote values.
	 */
	void beforeFillValues();
	
	/**
	 * This method is called by the {@link GetEntityPropertyValuesHandler}, after it did a bulk call to
     * retrieve the values for several widgets that implement {@link HasGetEntityPropertyValueHandler}. 
     * This method should be used by the widget to fill its values.
     * 
	 * @param subject
	 * @param propValues
	 */
	void setPreloadedPropertyValues(EntityData subject, List<EntityPropertyValues> propValues);
	
}

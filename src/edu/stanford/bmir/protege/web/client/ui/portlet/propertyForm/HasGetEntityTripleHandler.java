package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.OntologyService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;

/**
 * This interface should be implemented by all widgets which want to retrieve
 * their values via a bulk remote call to {@link OntologyService#getEntityTriples(String, List, java.util.Map, java.util.Map)}
 * 
 * The widget can retrieve its values by overriding the method {@link #setPreloadedValues(EntityData, List)}.
 * 
 * @author ttania
 *
 */
public interface HasGetEntityTripleHandler {

	/**
	 * This method is called by the {@link GetEntityTripleHandler} before it does a bulk call to
     * retrieve the values for several widgets that implement {@link HasGetEntityTripleHandler}.  
     * A widget can implement this method if it needs to do some other calls before the 
     * bulk call to retrieve the remote values.
	 */
	void beforeFillValues();
	
	/**
	 * This method is called by the {@link GetEntityTripleHandler}, after it did a bulk call to
     * retrieve the values for several widgets that implement {@link HasGetEntityTripleHandler}. 
     * This method should be used by the widget to fill its values.
     * 
	 * @param subject
	 * @param triples
	 */
	void setPreloadedValues(EntityData subject, List<Triple> triples);
}

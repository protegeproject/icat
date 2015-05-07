package edu.stanford.bmir.protege.web.server.bioportal;

import java.util.HashMap;
import java.util.Map;

import org.bioontology.ontologies.api.models.NCBOOntology;

public class BioPortalViewOntologyMap {

	private Map<Integer, NCBOOntology> viewOntologyMap = new HashMap<Integer, NCBOOntology>();
	private Map<Integer, Integer> viewIdOntologyIdMap = new HashMap<Integer, Integer>();

	public static final int NOT_A_VIEW = 0;
	public static final int UNKNOWN = -1;

	/**
	 * Returns a BioPortal ontology ID on which the view with id
	 * <code>viewId</code> is defined.
	 * @param viewId
	 */
	public int getViewOnOntologyId(int viewId) {
		Integer viewOnOntologyId = viewIdOntologyIdMap.get(viewId);
		return (viewOnOntologyId == null ? UNKNOWN : viewOnOntologyId);
	}

	/**
	 * Returns the display label of the BioPortal ontology
	 * on which the view with id <code>viewId</code> is defined.
	 * @param viewId
	 */
	public String getViewOnOntologyDisplayLabel(int viewId) {
		NCBOOntology viewOnOntology = viewOntologyMap.get(viewId);
		return (viewOnOntology == null ? "" : viewOnOntology.getName());
	}

	/**
	 * Returns an ontology bean corresponding to a BioPortal ontology
	 * on which the view with id <code>viewId</code> is defined.
	 * @param viewId
	 */
	public NCBOOntology getViewOnOntology(int viewId) {
		return viewOntologyMap.get(viewId);
	}

	public void setViewOnOntologyId(int viewId, int ontologyId, NCBOOntology NCBOOntology) {
		viewIdOntologyIdMap.put(viewId, ontologyId);
		viewOntologyMap.put(viewId, NCBOOntology);
	}

	public void reset() {
		viewIdOntologyIdMap.clear();
		viewOntologyMap.clear();
	}
}
